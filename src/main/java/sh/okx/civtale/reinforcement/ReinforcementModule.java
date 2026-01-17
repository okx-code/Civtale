package sh.okx.civtale.reinforcement;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import sh.okx.civtale.CivModule;
import sh.okx.civtale.database.Database;
import sh.okx.civtale.database.store.ChunkPositionDatabaseStore;

public class ReinforcementModule implements CivModule {
    private static final HytaleLogger LOGGER = HytaleLogger.get("Civtale").getSubLogger("Reinforcement");

    private final Database database;
    private final JavaPlugin plugin;

    public ReinforcementModule(Database database, JavaPlugin plugin) {
        this.database = database;
        this.plugin = plugin;
    }

    public void setup() {
        ChunkPositionDatabaseStore<Reinforcement> store = new ChunkPositionDatabaseStore<>(LOGGER, database, new ReinforcementStoreable(), "reinforcements");
        store.setup(plugin.getEntityStoreRegistry(), plugin.getEventRegistry());

        ComponentRegistryProxy<EntityStore> registry = plugin.getEntityStoreRegistry();
        registry.registerSystem(new BreakHandler(store, LOGGER));
        registry.registerSystem(new ReinforceHandler(store));
    }

    public void start() {
    }

    public void shutdown() {
    }
}
