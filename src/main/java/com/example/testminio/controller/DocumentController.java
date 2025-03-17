package com.example.testminio.controller;

import com.example.testminio.entities.Document;
import com.example.testminio.service.DocumentService;
import net.kaine.entities.Base64FileRequest;
import net.kaine.entities.MessageResponse;
import net.kaine.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private static final String ACTION_2 = "Fichier téléversé avec succès";
    private static final String ACTION_3 = "Fichier mis à jour avec succès";
    private static final String ACTION_4=  "Fichier supprimé avec succès";

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // CREATE
    @PostMapping(value = "/upload/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> createDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "minioName", required = false) String minioName) {
        try {
            Document savedDocument = documentService.createDocument(file, bucketName, minioName);
            return ResponseEntity.ok(new MessageResponse(ACTION_2 + " " + savedDocument.getName()));
        } catch (FileUploadException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la création du document : " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping(value = "/upload/base64", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> createDocumentBase64(
            @RequestBody Base64FileRequest request,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "minioName", required = false) String minioName) {
        try {
            Document savedDocument = documentService.createDocumentBase64(request.getFileName(), request.getFileBase64(), bucketName, minioName);
            return ResponseEntity.ok(new MessageResponse(ACTION_2 + " " + savedDocument.getName()));
        } catch (FileUploadException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la création du document : " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    // READ (All)
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    // READ (One)
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping(value = "/{id}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> updateDocument(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "minioName", required = false) String minioName) {
        try {
            Document updatedDocument = documentService.updateDocument(id, file, bucketName, minioName);
            return ResponseEntity.ok(new MessageResponse(ACTION_3 + " " + updatedDocument.getName()));

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Document non trouvé : " + e.getMessage()));
        } catch (FileUploadException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la mise à jour du document : " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}/base64", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> updateDocumentBase64(
            @PathVariable Long id,
            @RequestBody Base64FileRequest request,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "minioName", required = false) String minioName) {
        try {
            Document updatedDocument = documentService.updateDocumentBase64(id, request.getFileName(), request.getFileBase64(), bucketName, minioName);
            return ResponseEntity.ok(new MessageResponse(ACTION_3 + " " + updatedDocument.getName()));

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Document non trouvé : " + e.getMessage()));
        } catch (FileUploadException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la mise à jour du document : " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok(new MessageResponse(ACTION_4 + " Document with ID " + id));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la suppression du document: " + e.getMessage()));
        }
    }
}