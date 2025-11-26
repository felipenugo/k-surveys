package data.adapter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    // define el formato de la fecha: International Organization for Standardization (ISO) LocalDateTime
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // convierte LocalDateTime a String para que lo guarde en Json (escribir)
    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type var2, JsonSerializationContext var3){
        return new JsonPrimitive(localDateTime.format(FORMATTER));
    }

    // convierte String de Json a LocalDateTime (leer)
    @Override
    public LocalDateTime deserialize(JsonElement jsonElement, Type var2, JsonDeserializationContext var3) throws JsonParseException{
        return LocalDateTime.parse(jsonElement.getAsString(), FORMATTER);
    }
}
