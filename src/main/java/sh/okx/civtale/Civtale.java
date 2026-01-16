package sh.okx.civtale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import sh.okx.civtale.database.Database;
import sh.okx.civtale.reinforcement.ReinforcementModule;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;

public class Civtale extends JavaPlugin {
    private ArrayList<CivModule> modules;

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
        Database database = new Database(storage);

        ReinforcementModule reinforcement = new ReinforcementModule(database, this);
        reinforcement.init();
        this.modules.add(reinforcement);

        getEventRegistry().register(RemoveWorldEvent.class, "default", new WorldUnloadHandler(getLogger()));
        super.setup();
    }

    @Override
    protected void start() {
        for (CivModule module : modules) {
            module.start();
        }
        super.start();
    }

    @Override
    protected void shutdown() {
        for (CivModule module : modules) {
            module.shutdown();
        }
        super.shutdown();
    }
}
