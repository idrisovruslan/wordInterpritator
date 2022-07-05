package com.sbrf.idrisov.interpritator.services;

import com.sbrf.idrisov.interpritator.converters.DocumentToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.entitys.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class DocumentGeneratorService {

    @Autowired
    private DocumentToBodyBlockConverter documentToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    public XWPFDocument generate(Map<String, Object> model, File sourceDocxFile) {
        try (InputStream fileInputStream = new FileInputStream(sourceDocxFile);
             XWPFDocument document = new XWPFDocument(fileInputStream)) {

            return generateExceptionAware(model, document);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private XWPFDocument generateExceptionAware(Map<String, Object> model, XWPFDocument document) {

        List<RootBlock> paragraphsBlocks = documentToBodyBlockConverter
                .generateBlocksForTransform(document);

        paragraphsBlocks.forEach(paragraphsBlock -> paragraphsBlock.transform(model));

        squashParagraphsService.squashParagraphs(document);

        headerFooterTransform(document, model);

        return document;
    }

    //TODO вынести в сервис
    public void headerFooterTransform(XWPFDocument document, Map<String, Object> model) {
        List<XWPFFooter> footerList = document.getFooterList();
        for (XWPFFooter footer:footerList) {
            List<RootBlock> rootBlocks = documentToBodyBlockConverter.generateBlocksForTransform(footer);
            rootBlocks.forEach(paragraphsBlock -> paragraphsBlock.transform(model));
        }

        List<XWPFHeader> headerList = document.getHeaderList();
        for (XWPFHeader header:headerList) {
            List<RootBlock> rootBlocks = documentToBodyBlockConverter.generateBlocksForTransform(header);
            rootBlocks.forEach(paragraphsBlock -> paragraphsBlock.transform(model));
        }
    }
}
