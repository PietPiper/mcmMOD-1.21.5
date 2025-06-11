package pietpiper.mcmmod.data;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.sql.*;
import java.util.*;

import java.nio.file.Path;


public class PlacedBlockDatabaseManager {

    private static Connection connection;
    private static final Map<RegistryKey<World>, Set<BlockPos>> placedCache = new HashMap<>();
    private static final String TABLE_NAME = "placed_blocks";

    /**
     * Initializes the SQLite database and loads all placed blocks into memory.
     */
    public static void initialize(MinecraftServer server) {
        try {
            Path dbPath = server.getSavePath(WorldSavePath.ROOT).resolve("placed_blocks.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS placed_blocks (
                        dimension TEXT NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        PRIMARY KEY (dimension, x, y, z)
                    );
                """);
            }

            loadAll(server);
            System.out.println("[SQLite] PlacedBlockDatabaseManager initialized at: " + dbPath);
        } catch (SQLException e) {
            System.err.println("[SQLite] Failed to initialize PlacedBlockDatabaseManager:");
            e.printStackTrace();
        }
    }

    /**
     * Loads all placed blocks from the database into the in-memory cache.
     */
    private static void loadAll(MinecraftServer server) {
        placedCache.clear();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT dimension, x, y, z FROM " + TABLE_NAME);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String dim = rs.getString("dimension");
                RegistryKey<World> dimension = RegistryKey.of(RegistryKey.ofRegistry(Identifier.of("minecraft", "dimension")), Identifier.of(dim));
                BlockPos pos = new BlockPos(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
                placedCache.computeIfAbsent(dimension, d -> new HashSet<>()).add(pos);
            }

        } catch (SQLException e) {
            System.err.println("[SQLite] Failed to load placed block data:");
            e.printStackTrace();
        }
    }

    public static void markPlaced(ServerWorld world, BlockPos pos) {
        RegistryKey<World> dimension = world.getRegistryKey();
        Set<BlockPos> set = placedCache.computeIfAbsent(dimension, d -> new HashSet<>());
        if (set.add(pos)) {
            try (PreparedStatement stmt = connection.prepareStatement("""
                INSERT OR IGNORE INTO placed_blocks (dimension, x, y, z)
                VALUES (?, ?, ?, ?)
            """)) {
                stmt.setString(1, dimension.getValue().toString());
                stmt.setInt(2, pos.getX());
                stmt.setInt(3, pos.getY());
                stmt.setInt(4, pos.getZ());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean wasPlaced(ServerWorld world, BlockPos pos) {
        return placedCache.getOrDefault(world.getRegistryKey(), Collections.emptySet()).contains(pos);
    }

    public static void unmarkPlaced(ServerWorld world, BlockPos pos) {
        RegistryKey<World> dimension = world.getRegistryKey();
        Set<BlockPos> set = placedCache.get(dimension);
        if (set != null && set.remove(pos)) {
            try (PreparedStatement stmt = connection.prepareStatement("""
                DELETE FROM placed_blocks
                WHERE dimension = ? AND x = ? AND y = ? AND z = ?
            """)) {
                stmt.setString(1, dimension.getValue().toString());
                stmt.setInt(2, pos.getX());
                stmt.setInt(3, pos.getY());
                stmt.setInt(4, pos.getZ());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void clearAll() {
        placedCache.clear();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM placed_blocks;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Set<BlockPos> getAllInDimension(RegistryKey<World> dimension) {
        return placedCache.getOrDefault(dimension, Collections.emptySet());
    }
}