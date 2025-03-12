package com.example.testminio.entities;

import com.example.testminio.interfacedemarquage.FileModel;
import com.example.testminio.interfacedemarquage.FileModelAncienneVersion;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DocumentFile")
@JsonTypeName("DocumentFile")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFile extends Document{
    private String path;
    @Override
    public FileModel getFileModel() {

        return new FileModelAncienneVersion(super.getName(),this.path,super.getMimetype());
    }
}
