package sh.okx.civtale.moderation;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import sh.okx.civtale.CivModule;
import sh.okx.civtale.database.Database;
import sh.okx.civtale.moderation.commands.BanCommand;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModerationModule implements CivModule {
    private static final HytaleLogger LOGGER = HytaleLogger.get("Civtale").getSubLogger("Ban");

    private final Database database;
    private final JavaPlugin plugin;

    private final Set<UUID> bannedPlayers = new HashSet<>();

    public ModerationModule(Database database, JavaPlugin plugin) {
        this.database = database;
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        this.banPlayer(UUID.fromString("b603c115-d7bd-4978-bc8d-237c9bc956a5"));

        // register event handlers
        this.plugin.getEventRegistry().register(PlayerSetupConnectEvent.class, new PlayerSetupConnectHandler(this, LOGGER));
        this.plugin.getEventRegistry().register(PlayerConnectEvent.class, new PlayerConnectHandler(this, LOGGER));

        // register commands
        this.plugin.getCommandRegistry().registerCommand(new BanCommand(this));
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    /**
     * Check if a player is banned
     *
     * @param uuid Hytale UUID of the player
     * @return true if the player is banned, false otherwise
     */
    public boolean playerIsBanned(UUID uuid) {
        return bannedPlayers.contains(uuid);
    }

    /**
     * Ban a player
     * @param uuid Hytale UUID of the player to ban
     */
    public void banPlayer(UUID uuid) {
        bannedPlayers.add(uuid);
    }
}
