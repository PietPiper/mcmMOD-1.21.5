package pietpiper.mcmmod.guice.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import pietpiper.mcmmod.config.converters.ColorDeserializer;
import pietpiper.mcmmod.config.converters.ColorSerializer;

import java.awt.Color;

public class MapperModule extends AbstractModule {

  /**
   * Provides the config mapper for serializing and deserializing.
   *
   * @return The {@link ObjectMapper} for configs.
   */
  @Provides
  @Singleton
  public ObjectMapper providerConfigMapper() {
    final ObjectMapper configMapper =
            new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                    .findAndRegisterModules();

    final SimpleModule module = new SimpleModule();
    module.addSerializer(Color.class, new ColorSerializer());
    module.addDeserializer(Color.class, new ColorDeserializer());
    configMapper.registerModule(module);

    return configMapper;
  }
}
