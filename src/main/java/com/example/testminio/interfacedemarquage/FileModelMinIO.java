package com.example.testminio.interfacedemarquage;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.*;

import lombok.*;
import org.springframework.http.MediaType;

@Embeddable
@JsonTypeName("minio")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileModelMinIO implements FileModel{

    private String name;
    private MediaType mimeType;
    private String extension;
}
