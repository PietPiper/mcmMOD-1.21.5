package pietpiper.mcmmod.config.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/** {@link Color} deserializer. **/
@Singleton
@NoArgsConstructor
public class ColorDeserializer extends JsonDeserializer<Color> {

    @Override
    @Nullable
    public Color deserialize(@NonNull final JsonParser parser,
                             @NonNull final DeserializationContext context) {
        try {
            String hex = parser.getText();
            if (hex.startsWith("#")) hex = hex.substring(1);
            int rgb = Integer.parseInt(hex, 16);
            return new Color(rgb);
        } catch (Exception e) {
            return null;
        }
    }
}
