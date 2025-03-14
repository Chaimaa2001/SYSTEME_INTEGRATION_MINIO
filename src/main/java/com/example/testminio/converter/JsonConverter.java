package com.example.testminio.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter
public class JsonConverter implements AttributeConverter<Object, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            // Log the error or throw a runtime exception
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Object.class);
        } catch (IOException e) {
            // Log the error or throw a runtime exception
            throw new RuntimeException("Failed to convert JSON string to object", e);
        }
    }
}