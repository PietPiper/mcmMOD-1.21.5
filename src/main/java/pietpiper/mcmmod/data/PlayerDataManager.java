package pietpiper.mcmmod.data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import pietpiper.mcmmod.config.ConfigManager;
import pietpiper.mcmmod.skill.Skill;
import pietpiper.mcmmod.util.ServerReference;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    //Replace these with config file values eventually.
    public static final int STARTING_XP = 0;
    public static final int STARTING_REMAINING_XP = 1020;

    private static final Gson GSON = new Gson();

    // In-memory cache of active players
    private static final Map<UUID, McmPlayer> activePlayers = new HashMap<>();

    public static void initPlayer(UUID uuid) {
        PlayerSettings defaultSettings = new PlayerSettings();
        String insertUserSql = "INSERT OR IGNORE INTO users (uuid, settings) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(insertUserSql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, GSON.toJson(defaultSettings));
            ps.executeUpdate();
            ServerReference.logConsole("Initialized user row for player: [" + uuid + "]");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load player data into memory
        McmPlayer player = new McmPlayer(uuid);

        // Load existing skill data
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT skill_name, xp, remaining_xp, level FROM player_skills WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Skill skill = Skill.fromName(rs.getString("skill_name"));
                if (skill != null) {
                    player.setXP(skill, rs.getInt("xp"));
                    player.setRemainingXP(skill, rs.getInt("remaining_xp"));
                    player.setLevel(skill, rs.getInt("level"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load settings from DB
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT settings FROM users WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = rs.getString("settings");
                PlayerSettings settings = GSON.fromJson(json, PlayerSettings.class);
                player.updateSettings(settings);
            }
        } catch (SQLException | JsonSyntaxException e) {
            e.printStackTrace();
        }


        activePlayers.put(uuid, player);
    }

    public static int getXP(UUID uuid, Skill skill) {
        McmPlayer player = activePlayers.get(uuid);
        return player != null ? player.getXP(skill) : -1;
    }

    public static void updatePlayerExperience(UUID uuid, Skill skill, Integer xp, Integer remainingXP, Integer level) {
        McmPlayer player = activePlayers.get(uuid);

        if (player != null) {
            if (xp == null) xp = player.getXP(skill);
            if (remainingXP == null) remainingXP = player.getRemainingXP(skill);
            if (level == null) level = player.getLevel(skill);

            player.setXP(skill, xp);
            player.setRemainingXP(skill, remainingXP);
            player.setLevel(skill, level);
        } else {
            if (xp == null) xp = STARTING_XP;
            if (remainingXP == null) remainingXP = STARTING_REMAINING_XP;
            if (level == null) level = ConfigManager.getConfig().startingLevel;
        }

        String sql = """
            INSERT INTO player_skills (uuid, skill_name, xp, remaining_xp, level)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(uuid, skill_name) DO UPDATE SET
                xp = excluded.xp,
                remaining_xp = excluded.remaining_xp,
                level = excluded.level;
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, skill.getDisplayName());
            ps.setInt(3, xp);
            ps.setInt(4, remainingXP);
            ps.setInt(5, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getRemainingXP(UUID uuid, Skill skill) {
        McmPlayer player = activePlayers.get(uuid);
        return player != null ? player.getRemainingXP(skill) : STARTING_REMAINING_XP;
    }

    public static int getLevel(UUID uuid, Skill skill) {
        McmPlayer player = activePlayers.get(uuid);
        return player != null ? player.getLevel(skill) : ConfigManager.getConfig().startingLevel;
    }

    public static PlayerSettings getSettings(UUID uuid) {
        McmPlayer player = activePlayers.get(uuid);
        if (player != null && player.getSettings() != null) {
            return player.getSettings();
        }
        return new PlayerSettings();
    }

    public static void saveSettings(UUID uuid, PlayerSettings settings) {
        McmPlayer player = activePlayers.get(uuid);
        player.updateSettings(settings);
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE users SET settings = ? WHERE uuid = ?")) {
            ps.setString(1, GSON.toJson(settings));
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
