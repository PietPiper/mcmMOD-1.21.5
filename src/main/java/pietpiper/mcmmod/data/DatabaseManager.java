package pietpiper.mcmmod.data;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import pietpiper.mcmmod.skill.Skill;

import java.nio.file.Path;
import java.sql.*;

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
            // Resolve path to world directory
            Path dbPath = server.getSavePath(WorldSavePath.ROOT)
                    .resolve("playerdata.db");

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {
                // Enable foreign key support
                stmt.execute("PRAGMA foreign_keys = ON;");

                // USERS TABLE
                stmt.execute("""
                            CREATE TABLE IF NOT EXISTS users (
                                uuid TEXT PRIMARY KEY,
                                settings TEXT NOT NULL
                            );
                        """);

                // SKILLS TABLE
                stmt.execute("""
                            CREATE TABLE IF NOT EXISTS skills (
                                skill_name TEXT PRIMARY KEY,
                                description TEXT
                            );
                        """);

                // PLAYER_SKILLS TABLE (join table)
                stmt.execute("""
                            CREATE TABLE IF NOT EXISTS player_skills (
                                uuid TEXT NOT NULL,
                                skill_name TEXT NOT NULL,
                                xp INTEGER NOT NULL,
                                remaining_xp INTEGER NOT NULL,
                                level INTEGER NOT NULL,
                                PRIMARY KEY (uuid, skill_name),
                                FOREIGN KEY (uuid) REFERENCES users(uuid) ON DELETE CASCADE,
                                FOREIGN KEY (skill_name) REFERENCES skills(skill_name) ON DELETE CASCADE
                            );
                        """);
            }

            for (Skill skill : Skill.values()) {
                try (PreparedStatement ps = connection.prepareStatement("""
                INSERT OR IGNORE INTO skills (skill_name, description)
                VALUES (?, ?)
                """)) {
                    ps.setString(1, skill.getDisplayName());
                    ps.setString(2, skill.getDescription());
                    ps.executeUpdate();
                }
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
