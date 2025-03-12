package com.example.testminio;

import com.example.testminio.entities.Document;
import com.example.testminio.interfacedemarquage.FileModel;
import com.example.testminio.interfacedemarquage.FileModelMinIO;
import com.example.testminio.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.example", "net.kaine"})
public class TestMinioApplication  {

    public static void main(String[] args) {
        SpringApplication.run(TestMinioApplication.class, args);
    }

    @Autowired
    private DocumentRepository documentRepository;

}
