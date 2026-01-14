package sh.okx.civtale.reinforcement;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import sh.okx.civtale.database.store.ChunkPositionDatabaseStore;
import sh.okx.civtale.structure.ChunkPos;

import javax.annotation.Nonnull;
import java.util.Set;

public class WorldUnloadHandler extends StoreSystem<ChunkStore> {

    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT_TYPE = WorldChunk.getComponentType();
    private static final Query<ChunkStore> QUERY = Query.and(WORLD_CHUNK_COMPONENT_TYPE, Query.not(ChunkStore.REGISTRY.getNonSerializedComponentType()));

    private final ChunkPositionDatabaseStore store;
    private final Set<Dependency<ChunkStore>> dependencies;

    public WorldUnloadHandler(ChunkPositionDatabaseStore store) {
        this.dependencies = Set.of(new SystemDependency<>(Order.BEFORE, ChunkSavingSystems.WorldRemoved.class));
        this.store = store;
    }

    @Nonnull
    @Override
    public Set<Dependency<ChunkStore>> getDependencies() {
        return dependencies;
    }

    @Override
    public void onSystemAddedToStore(@Nonnull Store<ChunkStore> var1) {

    }

    @Override
    public void onSystemRemovedFromStore(@Nonnull Store<ChunkStore> store) {
        World world = store.getExternalData().getWorld();
        store.forEachChunk(QUERY, ((archetypeChunk, b) -> {
            for(int index = 0; index < archetypeChunk.size(); ++index) {
                WorldChunk worldChunkComponent = archetypeChunk.getComponent(index, WORLD_CHUNK_COMPONENT_TYPE);
                this.store.saveChunk(world, true, new ChunkPos(world.getName(), worldChunkComponent.getX(), worldChunkComponent.getZ()));
            }
        }));
    }
}
