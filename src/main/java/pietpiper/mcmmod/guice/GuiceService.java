package pietpiper.mcmmod.guice;

import com.google.inject.Injector;
import lombok.Setter;

public final class GuiceService {
    @Setter
    private static Injector injector;

    private GuiceService() {}

    public static <T> T get(Class<T> clazz) {
        if (injector == null) {
            throw new IllegalStateException("Guice injector not initialized yet!");
        }
        return injector.getInstance(clazz);
    }
}
