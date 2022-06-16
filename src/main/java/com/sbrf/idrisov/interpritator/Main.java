package com.sbrf.idrisov.interpritator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        File  sourceDocxFile = new File("src/main/resources/mock2.docx");

        //TODO И ЭТО НОРМАЛЬНОЕ ЗАПОЛНЕНИЕ МОДЕЛИ БЛЕАТЬ? ВСЕ ТАК ЗАПОЛНЯТЬ БУДЕШЬ?
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("fileName", freemarker.ext.dom.NodeModel.parse(new File("src/main/resources/mockSource2.xml")));

        documentGenerator.generate(objectMap, sourceDocxFile);
    }
}
