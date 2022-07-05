package com.sbrf.idrisov.interpritator.service;

import com.sbrf.idrisov.interpritator.converter.IBodyToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.entity.BodyBlock;
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
public class DocumentTransformerService {

    @Autowired
    private IBodyToBodyBlockConverter IBodyToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    public XWPFDocument generate(Map<String, Object> model, File sourceDocxFile) {
        try (InputStream fileInputStream = new FileInputStream(sourceDocxFile)) {
            XWPFDocument document = new XWPFDocument(fileInputStream);
            return generateExceptionAware(document, model);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private XWPFDocument generateExceptionAware(XWPFDocument document, Map<String, Object> model) {

        List<BodyBlock> bodyBlocks = IBodyToBodyBlockConverter
                .generateBodyBlocks(document);

        bodyBlocks.forEach(bodyBlock -> bodyBlock.transformBlock(model));

        squashParagraphsService.squashParagraphs(document);

        headerTransform(document, model);
        footerTransform(document, model);

        return document;
    }

    public void footerTransform(XWPFDocument document, Map<String, Object> model) {
        List<XWPFFooter> footerList = document.getFooterList();
        for (XWPFFooter footer:footerList) {
            List<BodyBlock> bodyBlocks = IBodyToBodyBlockConverter.generateBodyBlocks(footer);
            bodyBlocks.forEach(paragraphsBlock -> paragraphsBlock.transformBlock(model));
        }
    }

    public void headerTransform(XWPFDocument document, Map<String, Object> model) {
        List<XWPFHeader> headerList = document.getHeaderList();
        for (XWPFHeader header:headerList) {
            List<BodyBlock> bodyBlocks = IBodyToBodyBlockConverter.generateBodyBlocks(header);
            bodyBlocks.forEach(paragraphsBlock -> paragraphsBlock.transformBlock(model));
        }
    }
}
