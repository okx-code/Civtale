package sh.okx.civtale.database.store;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import sh.okx.civtale.database.Database;
import sh.okx.civtale.structure.BlockPos;
import sh.okx.civtale.structure.ChunkPos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChunkPositionDatabaseStore<T extends PositionStoreable> {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final HytaleLogger logger;
    private final Database database;
    private final DatabasePositionStoreable<T> databasePositionStoreable;
    private final String table;

    private final Map<ChunkPos, NavigableMap<BlockPos, DatabaseRecord<T>>> byChunk = new ConcurrentHashMap<>();

    public ChunkPositionDatabaseStore(HytaleLogger logger, Database database, DatabasePositionStoreable<T> databasePositionStoreable, String table) {
        this.logger = logger;
        this.database = database;
        this.databasePositionStoreable = databasePositionStoreable;
        this.table = table;
    }

    public void setup(ComponentRegistryProxy<EntityStore> registry, EventRegistry eventRegistry) {
        try {
            this.databasePositionStoreable.migrate(database.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        registry.registerSystem(new ChunkUnloadHandler<>(this));
        eventRegistry.registerGlobal(ChunkPreLoadProcessEvent.class, new ChunkLoadHandler<>(this));
        eventRegistry.registerGlobal(StartWorldEvent.class, new WorldLoadHandler<>(this));
    }

    public T get(String world, BlockPos pos) {
        NavigableMap<BlockPos, DatabaseRecord<T>> map = byChunk.get(new ChunkPos(world, ChunkUtil.chunkCoordinate(pos.x()), ChunkUtil.chunkCoordinate(pos.z())));
        DatabaseRecord<T> record = map.get(pos);
        if (record == null) {
            return null;
        } else {
            return record.value();
        }
    }

    public void add(T reinforcement) {
        BlockPos pos = reinforcement.blockPos();
        NavigableMap<BlockPos, DatabaseRecord<T>> map = byChunk.get(new ChunkPos(reinforcement.world(), ChunkUtil.chunkCoordinate(pos.x()), ChunkUtil.chunkCoordinate(pos.z())));
        if (map == null) {
            throw new IllegalStateException("cannot add to unloaded chunk: " + reinforcement);
        }
        map.put(pos, new DatabaseRecord<>(DatabaseRecord.RecordState.NEW, reinforcement));
    }

    public void update(T reinforcement) {
        BlockPos pos = reinforcement.blockPos();
        NavigableMap<BlockPos, DatabaseRecord<T>> map = byChunk.get(new ChunkPos(reinforcement.world(), ChunkUtil.chunkCoordinate(pos.x()), ChunkUtil.chunkCoordinate(pos.z())));
        if (map == null) {
            throw new IllegalStateException("cannot modify unloaded chunk: " + reinforcement);
        }
        DatabaseRecord<T> oldValue = map.computeIfPresent(pos, (k, current) -> {
            if (current.state() == DatabaseRecord.RecordState.DELETED || current.state() == DatabaseRecord.RecordState.DELETED_UNCONFIRMED) {
                throw new IllegalStateException("cannot update value that does not exist at position " + reinforcement.world() + " @ " + pos);
            } else if (current.state() == DatabaseRecord.RecordState.NEW) {
                return new DatabaseRecord<>(DatabaseRecord.RecordState.NEW, null);
            } else if (current.state() == DatabaseRecord.RecordState.UNMODIFIED || current.state() == DatabaseRecord.RecordState.MODIFIED || current.state() == DatabaseRecord.RecordState.UPDATED_UNCONFIRMED) {
                return new DatabaseRecord<>(DatabaseRecord.RecordState.MODIFIED, null);
            } else {
                throw new IllegalStateException();
            }
        });
        if (oldValue == null) {
            throw new IllegalStateException("cannot delete value that does not exist at position " + reinforcement.world() + " @ " + pos);
        }
    }

    public void delete(String world, BlockPos pos) {
        NavigableMap<BlockPos, DatabaseRecord<T>> map = byChunk.get(new ChunkPos(world, ChunkUtil.chunkCoordinate(pos.x()), ChunkUtil.chunkCoordinate(pos.z())));
        if (map == null) {
            throw new IllegalStateException("cannot delete from unloaded chunk: " + world + " @ " + pos);
        }
        DatabaseRecord<T> oldValue = map.computeIfPresent(pos, (k, current) -> {
            if (current.state() == DatabaseRecord.RecordState.DELETED || current.state() == DatabaseRecord.RecordState.DELETED_UNCONFIRMED) {
                throw new IllegalStateException("cannot delete value that does not exist at position " + world + " @ " + pos);
            } else if (current.state() == DatabaseRecord.RecordState.NEW) {
                return null;
            } else if (current.state() == DatabaseRecord.RecordState.UNMODIFIED || current.state() == DatabaseRecord.RecordState.MODIFIED || current.state() == DatabaseRecord.RecordState.UPDATED_UNCONFIRMED) {
                return new DatabaseRecord<>(DatabaseRecord.RecordState.DELETED, null);
            } else {
                throw new IllegalStateException();
            }
        });
        if (oldValue == null) {
            throw new IllegalStateException("cannot delete value that does not exist at position " + world + " @ " + pos);
        }
    }

    public void loadChunk(ChunkPos chunk) {
        try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM %s WHERE x >> 5 = ? AND z >> 5 = ? AND world = ?".formatted(table))) {
            statement.setInt(1, chunk.x());
            statement.setInt(2, chunk.z());
            statement.setString(3, chunk.world());
            ResultSet resultSet = statement.executeQuery();

            TreeMap<BlockPos, DatabaseRecord<T>> map = new TreeMap<>();
            while (resultSet.next()) {
                T value = this.databasePositionStoreable.deserialize(resultSet);
                map.put(value.blockPos(), new DatabaseRecord<>(DatabaseRecord.RecordState.UNMODIFIED, value));
            }
            byChunk.put(chunk, map);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveChunk(World world, boolean unload, ChunkPos chunk) {
        if (!world.isInThread()) {
            throw new IllegalStateException(Thread.currentThread().getName() + " is not world thread");
        }
        NavigableMap<BlockPos, DatabaseRecord<T>> map = byChunk.get(chunk);
        if (map.isEmpty()) {
            if (unload) {
                byChunk.remove(chunk);
            }
            return;
        }
        NavigableMap<BlockPos, DatabaseRecord<T>> updatedState = new TreeMap<>();
        Set<T> deleted = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<T> updated = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Map.Entry<BlockPos, DatabaseRecord<T>> entry : map.entrySet()) {
            DatabaseRecord<T> value = entry.getValue();
            if (value.state() == DatabaseRecord.RecordState.DELETED || value.state() == DatabaseRecord.RecordState.DELETED_UNCONFIRMED) {
                deleted.add(value.value());
                updatedState.put(entry.getKey(), new DatabaseRecord<>(DatabaseRecord.RecordState.DELETED_UNCONFIRMED, value.value()));
            } else if (value.state() == DatabaseRecord.RecordState.NEW || value.state() == DatabaseRecord.RecordState.MODIFIED || value.state() == DatabaseRecord.RecordState.UPDATED_UNCONFIRMED) {
                updated.add(value.value());
                updatedState.put(entry.getKey(), new DatabaseRecord<>(DatabaseRecord.RecordState.UPDATED_UNCONFIRMED, value.value()));
            } else if (value.state() == DatabaseRecord.RecordState.UNMODIFIED) {
                updatedState.put(entry.getKey(), new DatabaseRecord<>(DatabaseRecord.RecordState.UNMODIFIED, value.value()));
            }
        }
        if (!unload) {
            byChunk.put(chunk, updatedState);
        } else {
            byChunk.remove(chunk);
        }
        EXECUTOR.execute(() -> {
            try {
                // TODO process the database record into a list of statements which can then be applied sequentially
                // As if this errors right now we could end up in a corrupt state
                try (PreparedStatement deleteStatement = database.getConnection().prepareStatement("DELETE FROM " + table + " WHERE world = ? AND x = ? AND y = ? AND z = ?");
                     PreparedStatement updateReinforcement = database.getConnection().prepareStatement(this.databasePositionStoreable.replaceStatement(table))) {

                    database.getConnection().setAutoCommit(false);

                    for (T value : deleted) {
                        deleteStatement.setString(1, value.world());
                        BlockPos pos = value.blockPos();
                        deleteStatement.setLong(2, pos.x());
                        deleteStatement.setLong(3, pos.y());
                        deleteStatement.setLong(3, pos.z());
                        deleteStatement.addBatch();
                    }
                    deleteStatement.executeBatch();

                    database.getConnection().setAutoCommit(false);

                    for (T value : updated) {
                        this.databasePositionStoreable.serialize(value, updateReinforcement);
                        updateReinforcement.addBatch();
                    }
                    updateReinforcement.executeBatch();

                } finally {
                    database.getConnection().setAutoCommit(true);
                }

                if (unload) {
                    return;
                }
                world.execute(() -> {
                    NavigableMap<BlockPos, DatabaseRecord<T>> map1 = byChunk.get(chunk);
                    if (map1 == null) {
                        return;
                    }
                    for (Iterator<Map.Entry<BlockPos, DatabaseRecord<T>>> iterator = map1.entrySet().iterator(); iterator.hasNext(); ) {
                        Map.Entry<BlockPos, DatabaseRecord<T>> entry = iterator.next();
                        DatabaseRecord<T> record = entry.getValue();
                        if (record.state() == DatabaseRecord.RecordState.DELETED_UNCONFIRMED && deleted.contains(record.value())) {
                            iterator.remove();
                        } else if (record.state() == DatabaseRecord.RecordState.UPDATED_UNCONFIRMED && updated.contains(record.value())) {
                            entry.setValue(new DatabaseRecord<>(DatabaseRecord.RecordState.UNMODIFIED, record.value()));
                        }
                    }
                });
            } catch (SQLException e) {
                logger.atSevere().withCause(e).log("Error saving chunk");
                throw new RuntimeException(e);
            }
        });
    }
}
