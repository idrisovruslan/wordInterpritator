package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.service.DocumentTransformerService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private final DocumentTransformerService documentTransformerService;

    @Autowired
    public Main(DocumentTransformerService documentTransformerService) {
        this.documentTransformerService = documentTransformerService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    public void run(java.lang.String... args) throws Exception {
        File  sourceDocxFile = new File("src/main/resources/mock2.docx");
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("fileName", freemarker.ext.dom.NodeModel.parse(new File("src/main/resources/mockSource2.xml")));

        XWPFDocument document = documentTransformerService.generate(objectMap, sourceDocxFile);

        FileOutputStream out = new FileOutputStream("src/main/resources/result.docx");
        document.write(out);
        document.close();
    }
}
