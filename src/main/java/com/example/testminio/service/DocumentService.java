package com.example.testminio.service;

import com.example.testminio.entities.Document;
import com.example.testminio.entities.DocumentFile;
import com.example.testminio.entities.DocumentMinio;
import com.example.testminio.interfacedemarquage.FileModelAncienneVersion;
import com.example.testminio.interfacedemarquage.FileModelMinIO;
import com.example.testminio.repository.DocumentRepository;
import net.kaine.exception.FileNotFoundException;
import net.kaine.exception.FileUploadException;
import net.kaine.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileUploadService fileUploadService;

    private static final String LOCAL_STORAGE_DIRECTORY = "C:\\Users\\CHAIMAA\\Desktop\\STAGE PFE\\asiaphotos2";

    @Autowired
    public DocumentService(DocumentRepository documentRepository, FileUploadService fileUploadService) {
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
        MediaType contentType = (file.getContentType() == null || file.getContentType().isEmpty()) ? MediaType.APPLICATION_OCTET_STREAM : MediaType.valueOf(file.getContentType());
        String fileExtension = getFileExtension(originalFilename);

        Document document = (bucketName != null && !bucketName.isEmpty() && minioName != null && !minioName.isEmpty()) ?
                createMinioDocument(file, bucketName, minioName, originalFilename, contentType, fileExtension) :
                createLocalDocument(file, originalFilename, contentType, fileExtension);

        document.setCreatedAt(LocalDateTime.now());
        return documentRepository.save(document);
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
        FileModelMinIO fileModel = new FileModelMinIO(
                originalFilename,
                contentType,
                fileExtension
        );

        DocumentMinio documentMinio = new DocumentMinio();
        documentMinio.setName(originalFilename);
        documentMinio.setMimetype(contentType);
        documentMinio.setExtension(fileExtension);
        documentMinio.setBucket_name(bucketName);
        documentMinio.setMinio_name(minioName);
        documentMinio.setFileModel(fileModel);

        fileUploadService.uploadMultipartFile(file);
        return documentMinio;
    }

    private Document createLocalDocument(MultipartFile file, String originalFilename, MediaType contentType, String fileExtension) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        FileModelAncienneVersion fileModel = new FileModelAncienneVersion(
                originalFilename,
                filePath.toString(),
                contentType
        );

        DocumentFile documentFile = new DocumentFile();
        documentFile.setName(originalFilename);
        documentFile.setMimetype(contentType);
        documentFile.setExtension(fileExtension);
        documentFile.setPath(filePath.toString());
        documentFile.setFileModel(fileModel);
        return documentFile;
    }

    private Document createMinioDocumentBase64(String fileName, String fileBase64, String bucketName, String minioName, MediaType mimeType, String fileExtension) throws FileUploadException, IOException {
        fileUploadService.uploadBase64File(fileName, fileBase64);

        FileModelMinIO fileModel = new FileModelMinIO(
                fileName,
                mimeType,
                fileExtension
        );

        DocumentMinio documentMinio = new DocumentMinio();
        documentMinio.setName(fileName);
        documentMinio.setMimetype(mimeType);
        documentMinio.setExtension(fileExtension);
        documentMinio.setBucket_name(bucketName);
        documentMinio.setMinio_name(minioName);
        documentMinio.setFileModel(fileModel);
        return documentMinio;
    }

    private Document createLocalDocumentBase64(String fileName, String fileBase64, MediaType mimeType, String fileExtension) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(fileBase64);
        Files.write(filePath, decodedBytes);

        FileModelAncienneVersion fileModel = new FileModelAncienneVersion(
                fileName,
                filePath.toString(),
                mimeType
        );

        DocumentFile documentFile = new DocumentFile();
        documentFile.setName(fileName);
        documentFile.setMimetype(mimeType);
        documentFile.setExtension(fileExtension);
        documentFile.setPath(filePath.toString());
        documentFile.setFileModel(fileModel);
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
                .orElseThrow(() -> new FileNotFoundException("Document with id " + id + " not found"));

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
                .orElseThrow(() -> new FileNotFoundException("Document with id " + id + " not found"));

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
                .orElseThrow(() -> new FileNotFoundException("DocumentMinio with id " + id + " not found"));

        fileUploadService.updateMultipartFile(documentMinio.getName(), file);

        if (documentMinio.getFileModel() instanceof FileModelMinIO fileModel) {
            fileModel.setName(originalFilename);
            fileModel.setMimeType(contentType);
            fileModel.setExtension(fileExtension);
        }

        documentMinio.setBucket_name(bucketName);
        documentMinio.setMinio_name(minioName);
        documentMinio.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(documentMinio);
    }

    private Document updateLocalDocument(Long id, MultipartFile file, String originalFilename, MediaType contentType, String fileExtension) throws IOException {
        DocumentFile documentFile = documentRepository.findById(id)
                .map(DocumentFile.class::cast)
                .orElseThrow(() -> new FileNotFoundException("DocumentFile with id " + id + " not found"));

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        FileModelAncienneVersion fileModel = new FileModelAncienneVersion(
                originalFilename,
                filePath.toString(),
                contentType
        );

        documentFile.setName(originalFilename);
        documentFile.setMimetype(contentType);
        documentFile.setExtension(fileExtension);
        documentFile.setPath(filePath.toString());
        documentFile.setFileModel(fileModel);

        documentFile.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(documentFile);
    }

    private Document updateMinioDocumentBase64(Long id, String fileName, String fileBase64, String bucketName, String minioName, MediaType mimeType, String fileExtension) throws FileUploadException, IOException {
        DocumentMinio documentMinio = documentRepository.findById(id)
                .map(DocumentMinio.class::cast)
                .orElseThrow(() -> new FileNotFoundException("DocumentMinio with id " + id + " not found"));

        fileUploadService.updateBase64File(documentMinio.getName(), fileBase64);

        if (documentMinio.getFileModel() instanceof FileModelMinIO fileModel) {
            fileModel.setName(fileName);
            fileModel.setMimeType(mimeType);
            fileModel.setExtension(fileExtension);
        }

        documentMinio.setBucket_name(bucketName);
        documentMinio.setMinio_name(minioName);
        documentMinio.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(documentMinio);
    }

    private Document updateLocalDocumentBase64(Long id, String fileName, String fileBase64, MediaType mimeType, String fileExtension, Document document) throws IOException {
        DocumentFile documentFile = documentRepository.findById(id)
                .map(DocumentFile.class::cast)
                .orElseThrow(() -> new FileNotFoundException("DocumentFile with id " + id + " not found"));

        String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = Paths.get(LOCAL_STORAGE_DIRECTORY, uniqueFilename);
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(fileBase64);
        Files.write(filePath, decodedBytes);

        FileModelAncienneVersion fileModel = new FileModelAncienneVersion(
                fileName,
                filePath.toString(),
                mimeType
        );

        documentFile.setName(fileName);
        documentFile.setMimetype(mimeType);
        documentFile.setExtension(fileExtension);
        documentFile.setPath(filePath.toString());
        documentFile.setFileModel(fileModel);
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
}