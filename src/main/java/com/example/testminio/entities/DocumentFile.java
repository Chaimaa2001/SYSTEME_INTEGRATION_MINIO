package com.example.testminio.entities;

import com.example.testminio.interfacedemarquage.FileModel;
import com.example.testminio.interfacedemarquage.FileModelAncienneVersion;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Entity
@DiscriminatorValue("DocumentFile")
@JsonTypeName("DocumentFile")
@Data
@NoArgsConstructor
public class DocumentFile extends Document {

    @Override
    public FileModel getFileModel() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String detailsJson = mapper.writeValueAsString(super.getDetails());
            Map<String, Object> detailsMap = mapper.readValue(detailsJson, Map.class);
            String path = (String) detailsMap.get("path");
            return new FileModelAncienneVersion(super.getName(), path, super.getMimetype());
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Ou throw une exception
        }
    }
}