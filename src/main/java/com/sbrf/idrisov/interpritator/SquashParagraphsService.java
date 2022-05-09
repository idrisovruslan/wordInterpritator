package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.stereotype.Service;

import java.util.List;

//TODO Refactor
@Service
public class SquashParagraphsService {

    private final char squashChar = '&';

    public void squashParagraphs(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();

        for (int i = bodyElements.size() - 2; i >= 0; i--) {

            IBodyElement bodyElement = bodyElements.get(i);
            IBodyElement nextBodyElement = bodyElements.get(i + 1);

            if (bodyElement instanceof XWPFParagraph && nextBodyElement instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                XWPFParagraph nextParagraph = (XWPFParagraph) nextBodyElement;

                removeTabs(nextParagraph);

                if (nextParagraph.getText().length() != 0 && nextParagraph.getText().charAt(0) == squashChar) {
                    addNextParagraphToPrevious(document, i, paragraph, nextParagraph);
                    removeParagraph(document, i + 1);
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

    private void addNextParagraphToPrevious(XWPFDocument document, int previousParagraphPosition, XWPFParagraph paragraph, XWPFParagraph nextParagraph) {
        removeFirstSymbol(document, previousParagraphPosition + 1);

        if (nextParagraph.getRuns().isEmpty()) {
            return;
        }

        List<XWPFRun> nextParagraphRuns = nextParagraph.getRuns();

        nextParagraphRuns.forEach(oldRun -> copyXWPFRun(paragraph, oldRun));
    }

    private void copyXWPFRun(XWPFParagraph paragraph, XWPFRun oldRun) {
        XWPFRun newRun = paragraph.createRun();
        newRun.getCTR().setRPr(oldRun.getCTR().getRPr());
        newRun.setText(oldRun.text());
        paragraph.addRun(newRun);
    }

    private void removeFirstSymbol(XWPFDocument document, int paragraphPos) {
        XWPFParagraph paragraph = (XWPFParagraph)document.getBodyElements().get(paragraphPos);

        for (int i = 0; i < paragraph.getRuns().size(); i++) {
            String firstRunText = paragraph.getRuns().get(0).text();

            if (firstRunText.length() == 0) {
                paragraph.removeRun(0);
            } else if (firstRunText.length() == 1) {
                paragraph.removeRun(0);
                break;
            } else {
                paragraph.getRuns().get(0).setText(firstRunText.substring(1), 0);
            }
        }
    }
}
