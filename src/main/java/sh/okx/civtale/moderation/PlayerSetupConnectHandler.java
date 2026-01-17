package sh.okx.civtale.moderation;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;

import java.util.function.Consumer;

public class PlayerSetupConnectHandler implements Consumer<PlayerSetupConnectEvent> {
    private final HytaleLogger logger;
    private final ModerationModule moderationModule;

    public PlayerSetupConnectHandler(ModerationModule moderationModule, HytaleLogger logger) {
        this.moderationModule = moderationModule;
        this.logger = logger;
    }

    @Override
    public void accept(PlayerSetupConnectEvent event) {
        if (this.moderationModule.playerIsBanned(event.getUuid())) {
            logger.atInfo().log("Kicking banned player %s (%s)", event.getUsername(), event.getUuid());
            event.setReason(Message.translation("civ.moderation.ban.kickReason").getAnsiMessage());
            event.setCancelled(true);
        }
    }
}
