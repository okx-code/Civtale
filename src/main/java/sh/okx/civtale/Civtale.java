package sh.okx.civtale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import sh.okx.civtale.database.Database;
import sh.okx.civtale.reinforcement.ReinforcementModule;

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

        new ReinforcementModule(database, this).init();

        getEventRegistry().register(RemoveWorldEvent.class, "default", new WorldUnloadHandler(getLogger()));
    }

    @Override
    protected void start() {
    }
}
