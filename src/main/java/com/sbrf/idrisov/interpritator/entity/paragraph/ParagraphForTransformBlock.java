package com.sbrf.idrisov.interpritator.entity.paragraph;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

public class ParagraphForTransformBlock {
    private final int id;

    private final List<ParagraphForTransform> paragraphs;

    private final List<String> processedTexts;

    public ParagraphForTransformBlock(int id, List<ParagraphForTransform> paragraphs, List<String> processedTexts) {
        this.id = id;
        this.paragraphs = paragraphs;
        this.processedTexts = processedTexts;
    }
}
