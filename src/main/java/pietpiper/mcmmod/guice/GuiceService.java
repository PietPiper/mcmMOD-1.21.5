package pietpiper.mcmmod.guice;

import com.google.inject.Injector;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/** Utility class to set and have access to a Guice {@link Injector} statically. */
@UtilityClass
public final class GuiceService {

    @Getter
    private static Injector injector;

    /**
     * Singleton setter for the {@link Injector}.
     *
     * @param inj The injector to set for the GuiceService
     * @throws IllegalStateException if already set.
     */
    public static synchronized void setInjector(Injector inj) {
        if (injector != null) {
            throw new IllegalStateException("Guice injector already initialized!");
        }
        injector = inj;
    }

    /**
     * Static access to the {@link Injector} to retrieve instances from Guice.
     *
     * @param clazz The {@link Class} to get.
     * @return A {@link Class} instance
     * @param <T> The type of the {@link Class}
     */
    public static <T> T get(Class<T> clazz) {
        if (injector == null) {
            throw new IllegalStateException("Guice injector not initialized yet!");
        }
        return injector.getInstance(clazz);
    }
}
