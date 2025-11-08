package pietpiper.mcmmod.config.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ColorSerializerTest {

  private static final Color COLOR_GREEN = new Color(85, 255, 85);
  private static final Color COLOR_BLACK = Color.BLACK;
  private static final Color COLOR_WHITE = Color.WHITE;

  @Mock private JsonGenerator generator;
  @Mock private SerializerProvider serializers;
  private ColorSerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new ColorSerializer();
  }

  @Test
  void serialize_GreenColor_WritesCorrectHex() throws IOException {
    serializer.serialize(COLOR_GREEN, generator, serializers);

    verify(generator).writeString("#55FF55");
    verifyNoMoreInteractions(generator, serializers);
  }

  @Test
  void serialize_BlackColor_WritesCorrectHex() throws IOException {
    serializer.serialize(COLOR_BLACK, generator, serializers);

    verify(generator).writeString("#000000");
    verifyNoMoreInteractions(generator, serializers);
  }

  @Test
  void serialize_WhiteColor_WritesCorrectHex() throws IOException {
    serializer.serialize(COLOR_WHITE, generator, serializers);

    verify(generator).writeString("#FFFFFF");
    verifyNoMoreInteractions(generator, serializers);
  }
}