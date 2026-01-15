package sh.okx.civtale.reinforcement;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import sh.okx.civtale.database.store.ChunkPositionDatabaseStore;
import sh.okx.civtale.structure.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BreakHandler extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private final ChunkPositionDatabaseStore<Reinforcement> reinforcementStore;
    private final HytaleLogger logger;

    public BreakHandler(ChunkPositionDatabaseStore<Reinforcement> reinforcementStore, HytaleLogger logger) {
        super(BreakBlockEvent.class);
        this.reinforcementStore = reinforcementStore;
        this.logger = logger;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {

        World world = commandBuffer.getExternalData().getWorld();

        Reinforcement reinforcement = reinforcementStore.get(world.getName(), BlockPos.fromVec(event.getTargetBlock()));
        if (reinforcement == null) {
            return;
        }

        event.setCancelled(true);
        logger.atInfo().log("Reinforcement: " + reinforcement);

        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(event.getTargetBlock().x, event.getTargetBlock().z);
        Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
        BlockHealthChunk blockHealthComponent = chunkStoreStore.getComponent(chunkReference, BlockHealthModule.get().getBlockHealthChunkComponentType());
        blockHealthComponent.repairBlock(world, event.getTargetBlock(), 1);
    }
}
