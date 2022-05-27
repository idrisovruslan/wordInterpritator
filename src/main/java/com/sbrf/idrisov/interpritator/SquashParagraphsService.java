package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sbrf.idrisov.interpritator.RunUtils.copyPropertiesFromTo;
import static com.sbrf.idrisov.interpritator.RunUtils.removeFirstSymbol;

//TODO Refactor
@Service
public class SquashParagraphsService {

    private final char squashChar = '&';

    public void squashParagraphs(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        squashParagraphs(bodyElements);
    }

    public void squashParagraphs(XWPFTableCell cell) {
        List<IBodyElement> bodyElements = cell.getBodyElements();
        squashParagraphs(bodyElements);
    }

    private void squashParagraphs(List<IBodyElement> bodyElements) {
        for (int i = bodyElements.size() - 2; i >= 0; i--) {

            IBodyElement bodyElement = bodyElements.get(i);
            IBodyElement nextBodyElement = bodyElements.get(i + 1);

            if (bodyElement instanceof XWPFParagraph && nextBodyElement instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                XWPFParagraph nextParagraph = (XWPFParagraph) nextBodyElement;

                removeTabs(nextParagraph);

                if (nextParagraph.getText().length() != 0 && nextParagraph.getText().charAt(0) == squashChar) {
                    addNextParagraphToPrevious(paragraph, nextParagraph);
                    removeParagraph(paragraph.getDocument(), i + 1);
                }

            } else if (bodyElement instanceof XWPFTable) {
                //TODO реализовать таблицы
                System.out.println("нифига не делаем");
            }
        }
    }

    private void removeTabs(XWPFParagraph paragraph) {
        if (paragraph.getRuns().isEmpty()) {
            return;
        }

        CTR ctr = paragraph.getRuns().get(0).getCTR();

        for (int i = 0; i < ctr.sizeOfTabArray(); i++) {
            ctr.removeTab(i);
        }
    }

    private void removeParagraph(XWPFDocument document, int i) {
        document.removeBodyElement(i);
    }

    private void addNextParagraphToPrevious(XWPFParagraph paragraph, XWPFParagraph nextParagraph) {
        removeFirstSymbol(nextParagraph);

        if (nextParagraph.getRuns().isEmpty()) {
            return;
        }

        List<XWPFRun> nextParagraphRuns = nextParagraph.getRuns();

        nextParagraphRuns.forEach(oldRun -> copyXWPFRun(paragraph, oldRun));
    }

    private void copyXWPFRun(XWPFParagraph paragraph, XWPFRun oldRun) {
        XWPFRun newRun = paragraph.createRun();
        copyPropertiesFromTo(oldRun, newRun);
        newRun.setText(oldRun.text());
        paragraph.addRun(newRun);
    }
}
