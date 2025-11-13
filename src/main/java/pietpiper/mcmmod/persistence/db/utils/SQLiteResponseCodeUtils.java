package pietpiper.mcmmod.persistence.db.utils;

import lombok.experimental.UtilityClass;

import static org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT;

@UtilityClass
public class SQLiteResponseCodeUtils {

  /**
   * Determines if an int error code is for violating a constraint.
   *
   * @param errorCode The error code to check.
   * @return true/false dependent on if it violates.
   */
  public boolean violatesConstraint(final int errorCode) {
    return errorCode == SQLITE_CONSTRAINT.code;
  }
}
