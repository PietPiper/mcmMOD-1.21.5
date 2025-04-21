package pietpiper.mcmmod.data;

import pietpiper.mcmmod.SQLiteManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.ServerReference;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDataManager {
    public static final int STARTING_LEVEL = 0;
    public static final int STARTING_XP = 0;
    public static final int STARTING_REMAINING_XP = 1020;

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

        String sql = "INSERT OR IGNORE INTO player_stats (" + columns + ") VALUES (" + placeholders + ")";

        try {
            PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(sql);
            ps.setString(1, uuid.toString());
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
            PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(query);
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
            PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(query);
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
            PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(query);
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
            PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(query);
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
            PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(query);
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
            PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(query);
            ps.setInt(1, level);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
