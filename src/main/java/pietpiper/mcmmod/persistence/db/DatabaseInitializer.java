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

  /** Runs database initialization. */
  public void initialize() {
    log.info("Initializing database tables...");

    try {
      queryRunner.update(CREATE_PLAYERS_TABLE);
      log.info("Players table initialized");

      //TODO: More tables can be made here

      log.info("All database tables initialized successfully");

    } catch (Exception e) {
      log.error("Failed to initialize database tables", e);
    }
  }
}