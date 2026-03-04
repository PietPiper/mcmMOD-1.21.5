package pietpiper.mcmmod.guice.modules;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.net.http.HttpClient;

public class ClientModule {

  @Singleton
  @Provides
  public HttpClient provideHttpClient() {
    return HttpClient.newHttpClient();
  }
}
