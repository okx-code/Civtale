package sh.okx.civtale;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReinforceHandler extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    protected ReinforceHandler() {
        super(DamageBlockEvent.class);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull DamageBlockEvent event) {
        System.out.println("damage block event");

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        Inventory inventory = player.getInventory();
        ItemStack item = inventory.getActiveHotbarItem();
        if (item == null || !item.getItemId().equals("Soil_Dirt")) {
            return;
        }


        System.out.println(inventory.getHotbar().setItemStackForSlot(inventory.getActiveHotbarSlot(), item.withQuantity(item.getQuantity() - 1)));

        ParticleUtil.spawnParticleEffect("GreenOrbImpact", event.getTargetBlock().toVector3d().add(0.5, 0.5, 0.5), store);
    }
}
