package com.example.testminio.entities;

import com.example.testminio.interfacedemarquage.FileModel;
import com.example.testminio.interfacedemarquage.FileModelMinIO;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

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
            String detailsJson = mapper.writeValueAsString(super.getDetails());
            Map<String, Object> detailsMap = mapper.readValue(detailsJson, Map.class);
            String bucketName = (String) detailsMap.get("bucket_name");
            String minioName = (String) detailsMap.get("minio_name");

            return new FileModelMinIO(super.getName(), super.getMimetype(), super.getExtension());
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Ou throw une exception
        }
    }
}