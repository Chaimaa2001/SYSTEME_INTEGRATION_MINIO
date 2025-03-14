package com.example.testminio.entities;

import com.example.testminio.interfacedemarquage.FileModel;
import com.example.testminio.interfacedemarquage.FileModelMinIO;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@Entity
@DiscriminatorValue("DocumentMinio")
@JsonTypeName("DocumentMinio")
@Data
@NoArgsConstructor
public class DocumentMinio extends Document {

    @Override
    public FileModel getFileModel() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode detailsNode = mapper.readTree(mapper.writeValueAsString(super.getDetails()));
            String bucketName = detailsNode.get("bucket_name").asText();
            String minioName = detailsNode.get("minio_name").asText();

            return new FileModelMinIO(super.getName(), super.getMimetype(), super.getExtension());
        } catch (IOException e) {
            // Handle exception appropriately (e.g., log, throw a custom exception)
            e.printStackTrace();
            return null; // Or throw an exception
        }
    }
}