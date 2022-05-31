package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.models.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class Main implements CommandLineRunner {

    DocumentGenerator documentGenerator;

    @Autowired
    public Main(DocumentGenerator documentGenerator) {
        this.documentGenerator = documentGenerator;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    public void run(java.lang.String... args) throws Exception {
        Model myModel = Model.getModelFromFile(new File("src/main/resources/source.xml"));
        File  sourceDocxFile = new File("src/main/resources/romSourceBack.docx");

        documentGenerator.generate(myModel, sourceDocxFile);
    }
}
