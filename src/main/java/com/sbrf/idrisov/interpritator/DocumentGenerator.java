package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.RootBlock;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class DocumentGenerator {

    @Autowired
    private DocumentToBodyBlockConverter documentToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    @SneakyThrows
    public void generate(Map<String, Object> model, File sourceDocxFile) {
        try (InputStream fileInputStream = new FileInputStream(sourceDocxFile);
             XWPFDocument document = new XWPFDocument(fileInputStream);
             FileOutputStream out = new FileOutputStream("src/main/resources/result.docx")) {

            List<RootBlock> paragraphsBlocks = documentToBodyBlockConverter
                    .generateBlocksForTransform(document);

            paragraphsBlocks.forEach(paragraphsBlock -> paragraphsBlock.transform(model));

            squashParagraphsService.squashParagraphs(document);

            document.write(out);
        }
    }
}
