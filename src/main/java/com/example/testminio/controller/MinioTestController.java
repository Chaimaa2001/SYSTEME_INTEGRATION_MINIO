package com.example.testminio.controller;

import net.kaine.entities.Base64FileRequest;
import net.kaine.entities.MessageResponse;
import net.kaine.service.FileUploadService;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/test")
public class MinioTestController {

    private final FileUploadService fileService;
    private static final String ACTION_2="Fichier téléversé avec succès";
    private static final String ACTION_3="Fichier mis à jour avec succès";
    public MinioTestController(FileUploadService fileUploadService) {
        this.fileService=fileUploadService;
    }
    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> uploadMultipartFile(@RequestPart("file") MultipartFile file) throws FileUploadException {
       fileService.uploadMultipartFile(file);
        return ResponseEntity.ok(new MessageResponse(ACTION_2 + " "+file.getOriginalFilename()));

    }
    @PostMapping(value = "/base64", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> uploadBase64File(@RequestBody Base64FileRequest request) throws FileUploadException {
        fileService.uploadBase64File(request.getFileName(),request.getFileBase64());
        return ResponseEntity.ok(new MessageResponse(ACTION_2 +" "+ request.getFileName()));

    }
    @GetMapping("/{fileName}")
    public ResponseEntity<Object> getFile(@PathVariable String fileName) throws FileNotFoundException {
        return fileService.getFileResponse(fileName);
    }
    @PutMapping(value = "/multipart/{fileName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> updateMultipartFile(@PathVariable String fileName, @RequestPart("file") MultipartFile file) throws FileNotFoundException, FileUploadException {
        fileService.updateMultipartFile(fileName,file);
        return ResponseEntity.ok(new MessageResponse(ACTION_3 + " "+fileName ));
    }
    @PutMapping(value = "/base64/{fileName}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> updateBase64File(@PathVariable String fileName, @RequestBody Base64FileRequest request) throws FileNotFoundException, FileUploadException {
        fileService.updateBase64File(fileName,request.getFileBase64());
        return ResponseEntity.ok(new MessageResponse(ACTION_3+" "+ fileName ));
    }
}