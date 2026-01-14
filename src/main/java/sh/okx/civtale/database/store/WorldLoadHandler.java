package sh.okx.civtale.database.store;

import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import sh.okx.civtale.reinforcement.WorldUnloadHandler;

import java.util.function.Consumer;

public class WorldLoadHandler<T extends PositionStoreable> implements Consumer<StartWorldEvent> {

    private final ChunkPositionDatabaseStore<T> db;

    public WorldLoadHandler(ChunkPositionDatabaseStore<T> db) {
        this.db = db;
    }

    @Override
    public void accept(StartWorldEvent event) {
        event.getWorld().getChunkStore().getStore().getRegistry().registerSystem(new WorldUnloadHandler(db));
    }
}
