package sh.okx.civtale.database.store;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.events.ecs.ChunkUnloadEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import sh.okx.civtale.structure.ChunkPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class ChunkUnloadHandler<T extends PositionStoreable> extends EntityEventSystem<EntityStore, ChunkUnloadEvent> {

    private final ChunkPositionDatabaseStore<T> store;

    public ChunkUnloadHandler(ChunkPositionDatabaseStore<T> store) {
        super(ChunkUnloadEvent.class);
        this.store = store;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return super.getDependencies();
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull ChunkUnloadEvent event) {
        World world = commandBuffer.getExternalData().getWorld();
        WorldChunk chunk = event.getChunk();
        System.out.println("unload "  + chunk.getX() + " " + chunk.getZ());
        this.store.saveChunk(world, true, new ChunkPos(world.getName(), chunk.getX(), chunk.getZ()));
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
