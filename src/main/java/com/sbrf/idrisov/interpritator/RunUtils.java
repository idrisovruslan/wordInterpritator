package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.Objects;

//TODO проксю запели или что то типа того
public class RunUtils {
    private RunUtils() {
    }

    static public boolean isEquals(XWPFRun run1, XWPFRun run2) {
        boolean equals = Objects.equals(run1.getColor(), run2.getColor());
        equals = equals && Objects.equals(run1.isBold(), run2.isBold());
        equals = equals && Objects.equals(run1.getFontName(), run2.getFontName());
        //TODO ломает к херам документ тк гет создает объект...
        equals = equals && Objects.equals(run1.getTextHightlightColor(), run2.getTextHightlightColor());
        equals = equals && Objects.equals(run1.getFontSizeAsDouble(), run2.getFontSizeAsDouble());
        equals = equals && Objects.equals(run1.getTextScale(), run2.getTextScale());
        equals = equals && Objects.equals(run1.isItalic(), run2.isItalic());

        equals = equals && Objects.equals(run1.getTextPosition(), run2.getTextPosition());
        equals = equals && Objects.equals(run1.getUnderline(), run2.getUnderline());
        equals = equals && Objects.equals(run1.getUnderlineColor(), run2.getUnderlineColor());
        equals = equals && Objects.equals(run1.getUnderlineThemeColor(), run2.getUnderlineThemeColor());
        //TODO ломает к херам документ тк гет создает объект...
        equals = equals && Objects.equals(run1.getVerticalAlignment(), run2.getVerticalAlignment());

        return equals;
    }
}
