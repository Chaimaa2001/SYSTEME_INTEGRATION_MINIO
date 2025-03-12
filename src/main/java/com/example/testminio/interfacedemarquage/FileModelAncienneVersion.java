package com.example.testminio.interfacedemarquage;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Embeddable
@JsonTypeName("ancienne")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileModelAncienneVersion implements FileModel{

    private String name;
    private String path;
    private MediaType mimeType;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public MediaType getMimeType() {
        return mimeType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMimeType(MediaType mimeType) {
        this.mimeType = mimeType;
    }
}
