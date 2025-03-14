package com.example.testminio.entities;

import com.example.testminio.interfacedemarquage.FileModel;
import com.example.testminio.interfacedemarquage.FileModelAncienneVersion;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

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
            JsonNode detailsNode = mapper.readTree(mapper.writeValueAsString(super.getDetails()));
            String path = detailsNode.get("path").asText();
            return new FileModelAncienneVersion(super.getName(), path, super.getMimetype());
        } catch (IOException e) {
            // Handle exception appropriately (e.g., log, throw a custom exception)
            e.printStackTrace();
            return null; // Or throw an exception
        }
    }
}