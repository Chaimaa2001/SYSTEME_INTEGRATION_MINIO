package com.example.testminio.entities;

import com.example.testminio.converter.JsonConverter;
import com.example.testminio.interfacedemarquage.FileModel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING,name="document")
public abstract class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    private String name;
    private MediaType mimetype;
    private String extension;
    @Column(columnDefinition = "json") // Spécifie que la colonne est de type JSON
    @Convert(converter = JsonConverter.class)
    private Object details;

    public abstract FileModel getFileModel();


}
