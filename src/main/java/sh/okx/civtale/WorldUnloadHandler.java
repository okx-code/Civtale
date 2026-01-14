package sh.okx.civtale;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;

import java.util.function.Consumer;
import java.util.logging.Level;

public class WorldUnloadHandler implements Consumer<RemoveWorldEvent> {
    private final HytaleLogger logger;

    public WorldUnloadHandler(HytaleLogger logger) {
        this.logger = logger;
    }

    @Override
    public void accept(RemoveWorldEvent removeWorldEvent) {
        logger.at(Level.WARNING).log("Shutting down server due to default world removed");
        HytaleServer.get().shutdownServer(ShutdownReason.CRASH);
    }
}
