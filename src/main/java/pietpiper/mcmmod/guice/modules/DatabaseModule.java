package pietpiper.mcmmod.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.NonNull;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.nio.file.Path;

import static pietpiper.mcmmod.McmMod.log;
import static pietpiper.mcmmod.constants.ModConstants.MOD_ID;

/** Module configuring database components needed by the dal. */
public class DatabaseModule extends AbstractModule {

  /**
   * Provides the {@link DataSource} for the SQLite db.
   *
   * @param server The {@link MinecraftServer} instance running this mod.
   * @return {@link DataSource} to the SQLite db.
   */
  @Provides
  @Singleton
  public DataSource provideDataSource(@NonNull final MinecraftServer server) {

    // We currently assume that if there will be no issues with path existence.
    // We should anticipate errors, and fail gracefully.
    // Current idea is to essentially "self disable", but another idea is defaulting to an
    // in memory DB, and displaying a warning.

    final Path worldPath = server.getSavePath(WorldSavePath.ROOT);
    final Path modDataDir = worldPath.resolve(MOD_ID);
    final Path dbPath = modDataDir.resolve(MOD_ID.toLowerCase() + ".db");
    if (!modDataDir.toFile().exists() && !modDataDir.toFile().mkdirs()) {
      log.error("Unable to create directory: {}", modDataDir);
    }
    log.info("Database path: {}", dbPath);

    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbPath);
    hikariConfig.setMaximumPoolSize(3);
    hikariConfig.setConnectionTimeout(30000);

    return new HikariDataSource(hikariConfig);
  }

  /**
   * Provides the {@link QueryRunner} for database interactions.
   *
   * @param dataSource The {@link DataSource} associated to the SQLite db.
   * @return A {@link QueryRunner} for the database.
   */
  @Provides
  @Singleton
  public QueryRunner provideQueryRunner(@NonNull final DataSource dataSource) {
    return new QueryRunner(dataSource);
  }
}
