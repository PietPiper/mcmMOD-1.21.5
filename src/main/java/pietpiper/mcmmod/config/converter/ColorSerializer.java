package pietpiper.mcmmod.config.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.awt.Color;
import java.io.IOException;

@NoArgsConstructor
public class ColorSerializer extends JsonSerializer<Color> {

    @Override
    public void serialize(@NonNull final Color rgb, @NonNull final JsonGenerator generator, SerializerProvider serializers) throws IOException {
        String hex = String.format("#%02X%02X%02X", rgb.getRed(), rgb.getGreen(), rgb.getBlue());
        generator.writeString(hex);
    }
}
