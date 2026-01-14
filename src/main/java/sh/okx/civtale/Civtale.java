package sh.okx.civtale;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import sh.okx.civtale.database.Database;
import sh.okx.civtale.database.store.ChunkPositionDatabaseStore;
import sh.okx.civtale.reinforcement.Reinforcement;
import sh.okx.civtale.reinforcement.ReinforcementStoreable;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class Civtale extends JavaPlugin {
    public Civtale(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Path civtaleFolder = this.getFile().toAbsolutePath().getParent().getParent().resolve("civtale");
        civtaleFolder.toFile().mkdir();
        Path storage = civtaleFolder.resolve("storage");
        storage.toFile().mkdir();
        Database database = new Database(storage);
        ChunkPositionDatabaseStore<Reinforcement> store = new ChunkPositionDatabaseStore<>(getLogger(), database, new ReinforcementStoreable(), "reinforcements");
        store.setup(getEntityStoreRegistry(), getEventRegistry());

        ComponentRegistryProxy<EntityStore> registry = this.getEntityStoreRegistry();
        registry.registerSystem(new BreakHandler(store, getLogger()));
        registry.registerSystem(new ReinforceHandler(store));

        getEventRegistry().register(RemoveWorldEvent.class, "default", new WorldUnloadHandler(getLogger()));
    }

    @Override
    protected void start() {
    }
}
