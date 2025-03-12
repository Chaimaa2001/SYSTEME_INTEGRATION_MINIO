package com.example.testminio.entities;

import com.example.testminio.interfacedemarquage.FileModel;
import com.example.testminio.interfacedemarquage.FileModelMinIO;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DocumentMinio")
@JsonTypeName("DocumentMinio")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentMinio extends Document{

    private String bucket_name;
    private String minio_name;
    @Override
    public FileModel getFileModel() {
        return new FileModelMinIO(super.getName(),super.getMimetype(),super.getExtension());
    }
}
