package sh.okx.civtale.reinforcement;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import sh.okx.civtale.database.store.ChunkPositionDatabaseStore;
import sh.okx.civtale.structure.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

public class ReinforceHandler extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    private final ChunkPositionDatabaseStore<Reinforcement> reinforcementStore;

    public ReinforceHandler(ChunkPositionDatabaseStore<Reinforcement> reinforcementStore) {
        super(DamageBlockEvent.class);
        this.reinforcementStore = reinforcementStore;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull DamageBlockEvent event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        Inventory inventory = player.getInventory();
        ItemStack item = inventory.getActiveHotbarItem();
        if (item == null || !item.getItemId().equals("Reinforcement_Basic")) {
            return;
        }

        Vector3i target = event.getTargetBlock();
        World world = commandBuffer.getExternalData().getWorld();

        if (reinforcementStore.get(world.getName(), BlockPos.fromVec(target)) != null) {
            player.sendMessage(Message.translation("civ.general.reinforcement.alreadyReinforced").color("#ff5555"));
            return;
        }

        ParticleUtil.spawnParticleEffect("GreenOrbImpact", target.toVector3d().add(0.5, 0.5, 0.5), store);

        inventory.getHotbar().setItemStackForSlot(inventory.getActiveHotbarSlot(), item.withQuantity(item.getQuantity() - 1));
        reinforcementStore.add(new Reinforcement(world.getName(), target.x, target.y, target.z, "stone", 1, 0, Instant.now().toEpochMilli()));
        player.sendMessage(Message.translation("civ.general.reinforcement.reinforcedBasic").color("#55ff55"));
        event.setCancelled(true);
    }
}
