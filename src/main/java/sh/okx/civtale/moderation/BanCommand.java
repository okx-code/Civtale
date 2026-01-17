package sh.okx.civtale.moderation;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public class BanCommand extends CommandBase {
    private @Nonnull static final Message MESSAGE_COMMANDS_BAN_SUCCESS = Message.translation("civ.commands.ban.success");

    private @Nonnull final ModerationModule moderationModule;
    private @Nonnull final RequiredArg<PlayerRef> playerArg;

    public BanCommand(@Nonnull ModerationModule moderationModule) {  super("ban", "civ.command.ban.description");
        this.moderationModule = moderationModule;
        this.playerArg = withRequiredArg("player", "civ.command.ban.arg.player", ArgTypes.PLAYER_REF);
    }

    protected void executeSync(@Nonnull CommandContext context) {
        PlayerRef playerToBan = this.playerArg.get(context);
        this.moderationModule.banPlayer(playerToBan.getUuid());
        playerToBan.getPacketHandler().disconnect("You were banned.");
        context.sendMessage(MESSAGE_COMMANDS_BAN_SUCCESS.param("username", playerToBan.getUsername()).param("uuid", playerToBan.getUuid().toString()));
    }
}
