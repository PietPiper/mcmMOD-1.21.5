package pietpiper.mcmmod.config.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColorDeserializerTest {

  private static final String HEX_ORANGE = "FFAA00";
  private static final String HEX_GREEN_WITH_HASH = "#55FF55";
  private static final String INVALID_HEX = "NOT_HEX";

  @Mock private JsonParser parser;
  @Mock private DeserializationContext context;
  private ColorDeserializer deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new ColorDeserializer();
  }

  @Test
  void deserialize_ValidHex_ReturnsColor() throws Exception {
    when(parser.getText()).thenReturn(HEX_ORANGE);

    final Color result = deserializer.deserialize(parser, context);

    assertNotNull(result);
    assertEquals(255, result.getRed());
    assertEquals(170, result.getGreen());
    assertEquals(0, result.getBlue());
  }

  @Test
  void deserialize_HexWithHash_StripsHashAndReturnsColor() throws Exception {
    when(parser.getText()).thenReturn(HEX_GREEN_WITH_HASH);

    final Color result = deserializer.deserialize(parser, context);

    assertNotNull(result);
    assertEquals(85, result.getRed());
    assertEquals(255, result.getGreen());
    assertEquals(85, result.getBlue());
  }

  @Test
  void deserialize_InvalidHex_ReturnsNull() throws Exception {
    when(parser.getText()).thenReturn(INVALID_HEX);

    final Color result = deserializer.deserialize(parser, context);

    assertNull(result);
  }
}