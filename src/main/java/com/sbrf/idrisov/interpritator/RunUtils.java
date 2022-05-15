package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.Objects;

//TODO проксю запели или что то типа того
public class RunUtils {
    private RunUtils() {
    }

    public static boolean isEquals(XWPFRun run1, XWPFRun run2) {
        boolean equals = checkTextHighlightColor(run1, run2);
        equals = equals && Objects.equals(run1.isBold(), run2.isBold());
        equals = equals && Objects.equals(run1.getFontName(), run2.getFontName());
        equals = equals && Objects.equals(run1.getFontSizeAsDouble(), run2.getFontSizeAsDouble());
        equals = equals && Objects.equals(run1.isItalic(), run2.isItalic());
        //equals = equals && Objects.equals(run1.getColor(), run2.getColor());

        equals = equals && Objects.equals(run1.getTextScale(), run2.getTextScale());
        equals = equals && Objects.equals(run1.getTextPosition(), run2.getTextPosition());
        equals = equals && Objects.equals(run1.getUnderline(), run2.getUnderline());
        equals = equals && Objects.equals(run1.getUnderlineColor(), run2.getUnderlineColor());
        equals = equals && Objects.equals(run1.getUnderlineThemeColor(), run2.getUnderlineThemeColor());
        //TODO ломает к херам документ тк гет создает объект...
        //equals = equals && Objects.equals(run1.getVerticalAlignment(), run2.getVerticalAlignment());

        return equals;
    }

    private static boolean checkTextHighlightColor(XWPFRun run1, XWPFRun run2) {
        if (run1.isHighlighted() && run2.isHighlighted()) {
            return Objects.equals(run1.getTextHightlightColor(), run2.getTextHightlightColor());
        }
        return !run1.isHighlighted() && !run2.isHighlighted();
    }

    public static void removeFirstSymbol(XWPFDocument document, int paragraphPos) {
        XWPFParagraph paragraph = (XWPFParagraph)document.getBodyElements().get(paragraphPos);

        removeFirstSymbol(paragraph);
    }

    public static void removeFirstSymbol(XWPFParagraph paragraph) {
        for (int i = 0; i < paragraph.getRuns().size(); i++) {
            String firstRunText = paragraph.getRuns().get(0).text();

            if (firstRunText.length() == 0) {
                paragraph.removeRun(0);
            } else if (firstRunText.length() == 1) {
                paragraph.removeRun(0);
                break;
            } else {
                paragraph.getRuns().get(0).setText(firstRunText.substring(1), 0);
                break;
            }
        }
    }
}
