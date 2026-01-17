package sh.okx.civtale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import sh.okx.civtale.moderation.ModerationModule;
import sh.okx.civtale.database.Database;
import sh.okx.civtale.reinforcement.ReinforcementModule;

import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;

public class Civtale extends JavaPlugin {
    private List<CivModule> modules;
    private Database database;

    public Civtale(@Nonnull JavaPluginInit init) {
        super(init);

        this.modules = new ArrayList<>();
    }

    @Override
    protected void setup() {
        Path civtaleFolder = this.getFile().toAbsolutePath().getParent().getParent().resolve("civtale");
        civtaleFolder.toFile().mkdir();
        Path storage = civtaleFolder.resolve("storage");
        storage.toFile().mkdir();
        this.database = new Database(storage);

        ReinforcementModule reinforcementModule = new ReinforcementModule(database, this);
        reinforcementModule.setup();
        this.modules.add(reinforcementModule);

        ModerationModule moderationModule = new ModerationModule(database, this);
        moderationModule.setup();
        this.modules.add(moderationModule);

        getEventRegistry().register(RemoveWorldEvent.class, "default", new WorldUnloadHandler(getLogger()));
    }

    @Override
    protected void start() {
        for (CivModule module : modules) {
            module.start();
        }
    }

    @Override
    protected void shutdown() {
        for (CivModule module : modules) {
            module.shutdown();
        }
        try {
            this.database.getConnection().close();
        } catch (SQLException e) {
            getLogger().at(Level.SEVERE).log("Failed to close database connection", e);
        }
    }
}
