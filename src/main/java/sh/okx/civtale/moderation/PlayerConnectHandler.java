package sh.okx.civtale.moderation;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;

import java.util.function.Consumer;

public class PlayerConnectHandler implements Consumer<PlayerConnectEvent> {
    private final HytaleLogger logger;
    private final ModerationModule moderationModule;

    public PlayerConnectHandler(ModerationModule moderationModule, HytaleLogger logger) {
        this.moderationModule = moderationModule;
        this.logger = logger;
    }

    @Override
    public void accept(PlayerConnectEvent event) {
        logger.atInfo().log("PlayerConnectEvent");
    }
}
