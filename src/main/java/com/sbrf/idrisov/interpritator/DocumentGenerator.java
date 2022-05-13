package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.models.Model;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class DocumentGenerator {

    @Autowired
    private FreemarkerService freemarkerService;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    @SneakyThrows
    public void generate(Model model, File sourceDocxFile) {
        try (InputStream fileInputStream = new FileInputStream(sourceDocxFile);
             XWPFDocument document = new XWPFDocument(fileInputStream);
             FileOutputStream out = new FileOutputStream("src/main/resources/result.docx")) {

            //squashParagraphsService.squashParagraphs(document);

            //TODO И ЭТО НОРМАЛЬНОЕ ЗАПОЛНЕНИЕ МОДЕЛИ БЛЕАТЬ? ВСЕ ТАК ЗАПОЛНЯТЬ БУДЕШЬ?
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("model", model);

            freemarkerService.transformDocument(objectMap, document);

            document.write(out);
        }
    }

}
