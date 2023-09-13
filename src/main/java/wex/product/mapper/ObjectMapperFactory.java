package wex.product.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class ObjectMapperFactory {
    private ObjectMapperFactory() {
    }

    public static ObjectMapper create() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule()
                        .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_DATE))
                        .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_DATE)));
    }
}
