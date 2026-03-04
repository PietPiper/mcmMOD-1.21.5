package pietpiper.mcmmod.persistence.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbutils.QueryRunner;

import static pietpiper.mcmmod.McmMod.log;

/** Initializer for the SQLite Db. */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DatabaseInitializer {

  private final QueryRunner queryRunner;

  private static final String CREATE_PLAYERS_TABLE =
          """
          CREATE TABLE IF NOT EXISTS players (
              id TEXT PRIMARY KEY,
              username TEXT NOT NULL
          )
          """;

  private static final String CREATE_PLAYER_SKILLS_TABLE =
          """
          CREATE TABLE IF NOT EXISTS player_skills (
              player_id TEXT NOT NULL,
              skill TEXT NOT NULL,
              level INTEGER NOT NULL,
              xp INTEGER NOT NULL,
              metadata TEXT,
              PRIMARY KEY (player_id, skill),
              FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
          )
          """;

  private static final String CREATE_PLAYER_SKILLS_INDEX =
          """
          CREATE INDEX IF NOT EXISTS idx_player_skills_player_id
          ON player_skills(player_id)
          """;

  /** Runs database initialization. */
  public void initialize() {
    log.info("Initializing database tables...");

    try {
      queryRunner.update(CREATE_PLAYERS_TABLE);
      log.info("Players table initialized");

      queryRunner.update(CREATE_PLAYER_SKILLS_TABLE);
      log.info("Player skills table initialized");

      queryRunner.update(CREATE_PLAYER_SKILLS_INDEX);

      log.info("All database tables initialized successfully");

    } catch (Exception e) {
      log.error("Failed to initialize database tables", e);
    }
  }
}
