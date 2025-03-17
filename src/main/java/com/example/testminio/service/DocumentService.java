package com.example.testminio.service;

import com.example.testminio.entities.Document;
import com.example.testminio.entities.DocumentFile;
import com.example.testminio.entities.DocumentMinio;
import com.example.testminio.repository.DocumentRepository;
import net.kaine.exception.FileNotFoundException;
import net.kaine.exception.FileUploadException;
import net.kaine.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileUploadService fileUploadService;
    private  static  String minioS="minio_name";
    private static String bucketS="bucket_name";


    private static final String LOCAL_STORAGE_DIRECTORY = "C:\\Users\\CHAIMAA\\Desktop\\STAGE PFE\\asiaphotos2";
    private static final String ACTION_1 = "Document with id not found";

    @Autowired
    public DocumentService(DocumentRepository documentRepository, FileUploadService fileUploadService, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.fileUploadService = fileUploadService;
        createLocalStorageDirectory();
    }

    private void createLocalStorageDirectory() {
        File directory = new File(LOCAL_STORAGE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Document createDocument(MultipartFile file, String bucketName, String minioName) throws FileUploadException, IOException {

        // 1. Validation
        validateFile(file);


        String originalFilename = file.getOriginalFilename();
        String contentTypeString = file.getContentType();
        MediaType contentType = determineMediaType(contentTypeString, originalFilename);
        String fileExtension = getFileExtension(originalFilename);

        Document document = (bucketName != null && !bucketName.isEmpty() && minioName != null && !minioName.isEmpty()) ?
                createMinioDocument(file, bucketName, minioName, originalFilename, contentType, fileExtension) :
                createLocalDocument(file, originalFilename, contentType, fileExtension);

        document.setCreatedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }

    private MediaType determineMediaType(String contentTypeString, String originalFilename) {
        if (contentTypeString != null && !contentTypeString.isEmpty()) {
            try {
                return MediaType.valueOf(contentTypeString);
            } catch (IllegalArgumentException e) {
                // Log the error, content type is invalid, fallback to default.
                System.out.println("Invalid content type: " + contentTypeString + ". Falling back to default.");
            }
        }


        String fileExtension = getFileExtension(originalFilename);
        if (fileExtension != null && !fileExtension.isEmpty()) {
            switch (fileExtension.toLowerCase()) {
                case "jpg":
                case "jpeg":
                    return MediaType.IMAGE_JPEG;
                case "png":
                    return MediaType.IMAGE_PNG;
                case "pdf":
                    return MediaType.APPLICATION_PDF;
                default:
                    break;
            }
        }

        // If all else fails, use a safe default
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @Transactional
    public Document createDocumentBase64(String fileName, String fileBase64, String bucketName, String minioName) throws FileUploadException, IOException {
        // Validation
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty.");
        }

        MediaType mimeType;
        try {
            String mimeTypeString = Files.probeContentType(Paths.get(fileName));
            mimeType = (mimeTypeString == null) ? MediaType.APPLICATION_OCTET_STREAM : MediaType.valueOf(mimeTypeString);
        } catch (IOException e) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM;
        }
        String fileExtension = getFileExtension(fileName);

        Document document = (bucketName != null && !bucketName.isEmpty() && minioName != null && !minioName.isEmpty()) ?
                createMinioDocumentBase64(fileName, fileBase64, bucketName, minioName, mimeType, fileExtension) :
                createLocalDocumentBase64(fileName, fileBase64, mimeType, fileExtension);

        document.setCreatedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }

    private Document createMinioDocument(MultipartFile file, String bucketName, String minioName, String originalFilename, MediaType contentType, String fileExtension) throws FileUploadException, IOException {

        DocumentMinio documentMinio = new DocumentMinio();
        documentMinio.setName(originalFilename);
        documentMinio.setMimetype(contentType);
        documentMinio.setExtension(fileExtension);


        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put(bucketS, bucketName);
        details.put(minioS, minioName);
        documentMinio.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        fileUploadService.uploadMultipartFile(file);
        return documentMinio;
    }

    private Document createLocalDocument(MultipartFile file, String originalFilename, MediaType contentType, String fileExtension) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        DocumentFile documentFile = new DocumentFile();
        documentFile.setName(originalFilename);
        documentFile.setMimetype(contentType);
        documentFile.setExtension(fileExtension);

        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put("path", filePath.toString());
        documentFile.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        return documentFile;
    }

    private Document createMinioDocumentBase64(String fileName, String fileBase64, String bucketName, String minioName, MediaType mimeType, String fileExtension) throws FileUploadException, IOException {
        fileUploadService.uploadBase64File(fileName, fileBase64);

        DocumentMinio documentMinio = new DocumentMinio();
        documentMinio.setName(fileName);
        documentMinio.setMimetype(mimeType);
        documentMinio.setExtension(fileExtension);

        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put(bucketS, bucketName);
        details.put(minioS, minioName);
        documentMinio.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        return documentMinio;
    }

    private Document createLocalDocumentBase64(String fileName, String fileBase64, MediaType mimeType, String fileExtension) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(fileBase64);
        Files.write(filePath, decodedBytes);

        DocumentFile documentFile = new DocumentFile();
        documentFile.setName(fileName);
        documentFile.setMimetype(mimeType);
        documentFile.setExtension(fileExtension);

        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put("path", filePath.toString());
        documentFile.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        return documentFile;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty.");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            throw new IllegalArgumentException("Original filename cannot be null or empty.");
        }
    }

    @Transactional(readOnly = true)
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    @Transactional
    public Document updateDocument(Long id, MultipartFile file, String bucketName, String minioName) throws FileUploadException, IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(ACTION_1 + id  ));

        String originalFilename = file.getOriginalFilename();
        MediaType contentType = (file.getContentType() == null || file.getContentType().isEmpty()) ? MediaType.APPLICATION_OCTET_STREAM : MediaType.valueOf(file.getContentType());
        String fileExtension = getFileExtension(originalFilename);

        if (document instanceof DocumentMinio) {
            return updateMinioDocument(id, file, bucketName, minioName, originalFilename, contentType, fileExtension);
        } else if (document instanceof DocumentFile) {
            return updateLocalDocument(id, file, originalFilename, contentType, fileExtension);
        } else {
            throw new IllegalArgumentException("Unknown Document Type");
        }
    }

    @Transactional
    public Document updateDocumentBase64(Long id, String fileName, String fileBase64, String bucketName, String minioName) throws FileUploadException, IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(ACTION_1 + id ));

        MediaType mimeType;
        try {
            String mimeTypeString = Files.probeContentType(Paths.get(fileName));
            mimeType = (mimeTypeString == null) ? MediaType.APPLICATION_OCTET_STREAM : MediaType.valueOf(mimeTypeString);
        } catch (IOException e) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM;
        }
        String fileExtension = getFileExtension(fileName);

        if (document instanceof DocumentMinio) {
            return updateMinioDocumentBase64(id, fileName, fileBase64, bucketName, minioName, mimeType, fileExtension);
        } else if (document instanceof DocumentFile) {
            return updateLocalDocumentBase64(id, fileName, fileBase64, mimeType, fileExtension, document);
        } else {
            throw new IllegalArgumentException("Unknown Document Type");
        }
    }

    private Document updateMinioDocument(Long id, MultipartFile file, String bucketName, String minioName, String originalFilename, MediaType contentType, String fileExtension) throws FileUploadException, IOException {
        DocumentMinio documentMinio = documentRepository.findById(id)
                .map(DocumentMinio.class::cast)
                .orElseThrow(() -> new FileNotFoundException(ACTION_1 + id ));

        fileUploadService.updateMultipartFile(documentMinio.getName(), file);

        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put(bucketS, bucketName);
        details.put(minioS, minioName);
        documentMinio.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        documentMinio.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(documentMinio);
    }

    private Document updateLocalDocument(Long id, MultipartFile file, String originalFilename, MediaType contentType, String fileExtension) throws IOException {
        DocumentFile documentFile = documentRepository.findById(id)
                .map(DocumentFile.class::cast)
                .orElseThrow(() -> new FileNotFoundException(ACTION_1 + id ));

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put("path", filePath.toString());
        documentFile.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        documentFile.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(documentFile);
    }

    private Document updateMinioDocumentBase64(Long id, String fileName, String fileBase64, String bucketName, String minioName, MediaType mimeType, String fileExtension) throws FileUploadException, IOException {
        DocumentMinio documentMinio = documentRepository.findById(id)
                .map(DocumentMinio.class::cast)
                .orElseThrow(() -> new FileNotFoundException(ACTION_1 + id ));

        fileUploadService.updateBase64File(documentMinio.getName(), fileBase64);

        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put(bucketS, bucketName);
        details.put(minioS, minioName);
        documentMinio.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        documentMinio.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(documentMinio);
    }

    private Document updateLocalDocumentBase64(Long id, String fileName, String fileBase64, MediaType mimeType, String fileExtension, Document document) throws IOException {
        DocumentFile documentFile = documentRepository.findById(id)
                .map(DocumentFile.class::cast)
                .orElseThrow(() -> new FileNotFoundException(ACTION_1 + id ));

        String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(fileBase64);
        Files.write(filePath, decodedBytes);

        // Prepare details as a map
        Map<String, Object> details = new HashMap<>();
        details.put("path", filePath.toString());
        documentFile.setDetails(details); // Set the map directly, the converter will handle the JSON conversion

        documentFile.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(documentFile);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    @Transactional
    public void deleteDocument(Long id) throws IOException {
        Optional<Document> optionalDocument = documentRepository.findById(id);

        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();

            if (document instanceof DocumentMinio) {
                // 1. DELETE FROM MINIO
                DocumentMinio documentMinio = (DocumentMinio) document;
                try {
                    Map<String, Object> details = documentMinio.getDetails();
                    // DEBUGGING: Print the details map
                    System.out.println("Details Map: " + details);

                    String bucketName = (String) details.get(bucketS); // "bucket_name"
                    // DEBUGGING: Print the bucket name
                    System.out.println("Bucket Name: " + bucketName);

                    String objectName = documentMinio.getName();
                    // DEBUGGING: Print the object name
                    System.out.println("Object Name: " + objectName);

                    fileUploadService.deleteFile(objectName, bucketName); // Calls FileUploadService

                } catch (Exception e) {
                    // Handle MinIO deletion errors!  VERY IMPORTANT.
                    // DEBUGGING: Print the stack trace
                    e.printStackTrace();
                    throw new IOException("Failed to delete from MinIO: " + e.getMessage(), e);
                }
            } else if (document instanceof DocumentFile) {
                // ... (local file deletion - not relevant to this error) ...
            }

            // 3. DELETE FROM DATABASE (ONLY IF 1 and 2 SUCCEED)
            documentRepository.deleteById(id);

        } else {
            throw new FileNotFoundException(ACTION_1 + id);
        }
    }

}