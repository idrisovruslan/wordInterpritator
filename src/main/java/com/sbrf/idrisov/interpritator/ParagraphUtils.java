package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

//TODO проксю запели или что то типа того
public class ParagraphUtils {
    private ParagraphUtils() {
    }

    public static String removeRumMetaInfo(String paragraphText) {
        return paragraphText.replaceAll("\\{MetaInfoRun: .*?}", "");
    }

    public static void removeParagraphOnDocument(XWPFParagraph paragraph) {
        if (paragraph.getBody() instanceof XWPFTableCell) {
            XWPFTableCell cell = (XWPFTableCell) paragraph.getBody();
            cell.removeParagraph(getPosOfBodyElement(paragraph, cell.getParagraphs()));
        } else {
            XWPFDocument document = paragraph.getDocument();
            document.removeBodyElement(document.getPosOfParagraph(paragraph));
        }
    }

    public static int getPosOfBodyElement(IBodyElement needle, List<? extends IBodyElement> bodyElements) {
        BodyElementType type = needle.getElementType();
        IBodyElement current;
        for (int i = 0; i < bodyElements.size(); i++) {
            current = bodyElements.get(i);
            if (current.getElementType() == type) {
                if (current.equals(needle)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
