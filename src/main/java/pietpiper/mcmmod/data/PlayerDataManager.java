package pietpiper.mcmmod.data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.ServerReference;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDataManager {
    //Replace these with config file values eventually.
    public static final int STARTING_LEVEL = 0;
    public static final int STARTING_XP = 0;
    public static final int STARTING_REMAINING_XP = 1020;
    private static final PlayerSettings DEFAULT_SETTINGS = new PlayerSettings();

    private static final Gson GSON = new Gson();

    public static void initPlayer(UUID uuid) {
        StringBuilder columns = new StringBuilder("uuid");
        StringBuilder placeholders = new StringBuilder("?");

        for (Skill skill : Skill.values()) {
            String name = skill.getDisplayName();
            columns.append(", ").append(name).append("_level");
            columns.append(", ").append(name).append("_xp");
            columns.append(", ").append(name).append("_remaining_xp");

            placeholders.append(", ").append(STARTING_LEVEL);
            placeholders.append(", ").append(STARTING_XP);
            placeholders.append(", ").append(STARTING_REMAINING_XP);
        }

        String sql = "INSERT OR IGNORE INTO player_stats (" + columns + ", settings) VALUES (" + placeholders + ", ?)";

        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, uuid.toString());
            ps.setString(2, GSON.toJson(DEFAULT_SETTINGS));
            ps.executeUpdate();
            ServerReference.logConsole("Database update for player: [" + uuid.toString() + "] successful. ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getXP(UUID uuid, Skill skill) {
        String dbColumn = skill.getDisplayName() + "_xp";
        String query = "SELECT " + dbColumn + " FROM player_stats WHERE uuid = ?";
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(dbColumn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setXP(UUID uuid, int xp, Skill skill) {
        String dbColumn = skill.getDisplayName() + "_xp";
        String query = "UPDATE player_stats SET " + dbColumn + " = ? WHERE uuid = ? " ;
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);
            ps.setInt(1, xp);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getRemainingXP(UUID uuid, Skill skill) {
        String dbColumn = skill.getDisplayName() + "_remaining_xp";
        String query = "SELECT " + dbColumn + " FROM player_stats WHERE uuid = ?";
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(dbColumn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setRemainingXP(UUID uuid, int remainingXP, Skill skill) {
        String dbColumn = skill.getDisplayName() + "_remaining_xp";
        String query = "UPDATE player_stats SET " + dbColumn + " = ? WHERE uuid = ? " ;
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);
            ps.setInt(1, remainingXP);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getLevel(UUID uuid, Skill skill) {
        String dbColumn = skill.getDisplayName() + "_level";
        String query = "SELECT " + dbColumn + " FROM player_stats WHERE uuid = ?";
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(dbColumn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setLevel(UUID uuid, int level, Skill skill) {
        String dbColumn = skill.getDisplayName() + "_level";
        String query = "UPDATE player_stats SET " + dbColumn + " = ? WHERE uuid = ? " ;
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);
            ps.setInt(1, level);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static PlayerSettings getSettings(UUID uuid) {
        String query = "SELECT settings FROM player_stats WHERE uuid = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = rs.getString("settings");
                return GSON.fromJson(json, PlayerSettings.class);
            }
        } catch (SQLException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return new PlayerSettings(); // fallback default
    }

    public static void saveSettings(UUID uuid, PlayerSettings settings) {
        String query = "UPDATE player_stats SET settings = ? WHERE uuid = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query)) {
            ps.setString(1, GSON.toJson(settings));
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
