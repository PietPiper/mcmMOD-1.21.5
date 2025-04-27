package pietpiper.mcmmod.data;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import pietpiper.mcmmod.skill.Skill;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLiteManager handles connecting to and initializing the mod's persistent SQLite database.
 *
 * It creates a table named 'player_stats' that stores XP, RemainingXP, and Level
 * for each defined skill per player (keyed by UUID).
 */
public class DatabaseManager {
    // The singleton database connection shared across the mod
    private static Connection connection;

    /**
     * Establishes a SQLite connection and initializes the player_stats table if it doesn't exist.
     * This should be called during server startup.
     */
    public static void connect(MinecraftServer server) {
        try {
            // Locate the path to the world save folder and create a database file inside it
            Path dbPath = server.getSavePath(WorldSavePath.ROOT) // Store the DB inside the active world folder
                    .resolve("playerdata.db");

            // Establish connection — assign to shared field, not a local shadow
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {
                StringBuilder sql = new StringBuilder("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        uuid TEXT PRIMARY KEY
                    """);

                // Dynamically add columns for each skill (xp, remaining_xp, level)
                for (Skill skill : Skill.values()) {
                    String name = skill.getDisplayName(); // e.g. "Fishing"
                    sql.append(",\n")
                            .append(name).append("_xp INTEGER NOT NULL")
                            .append(",\n")
                            .append(name).append("_remaining_xp INTEGER NOT NULL")
                            .append(",\n")
                            .append(name).append("_level INTEGER NOT NULL");
                }
                sql.append(",\n")
                        .append("settings TEXT NOT NULL");
                sql.append("\n);"); // Close the CREATE TABLE statement

                stmt.executeUpdate(sql.toString());
            }

            System.out.println("[SQLite] Connected and initialized at: " + dbPath);
        } catch (SQLException e) {
            System.err.println("[SQLite] Failed to connect or initialize database:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the shared database connection. Ensure `connect()` has been called first.
     */
    public static Connection getConnection() {
        return connection;
    }
}
