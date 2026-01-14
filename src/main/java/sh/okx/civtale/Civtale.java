package sh.okx.civtale;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class Civtale extends JavaPlugin {
    public Civtale(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        ComponentRegistryProxy<EntityStore> registry = this.getEntityStoreRegistry();
//        registry.registerSystem(new BreakHandler());
//        registry.registerSystem(new ReinforceHandler());
        getEventRegistry().register(RemoveWorldEvent.class, "default", new WorldUnloadHandler(getLogger()));
    }

    @Override
    protected void start() {
        super.start();
    }
}
