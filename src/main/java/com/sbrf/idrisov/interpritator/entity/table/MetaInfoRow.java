package com.sbrf.idrisov.interpritator.entity.table;

import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaInfoRow {

    private String variables;
    private String loopCondition;
    @Getter
    private boolean needToRender;

    public MetaInfoRow(String textMeta) {
        parseMeta(textMeta);
    }

    public MetaInfoRow() {
        this.loopCondition = "";
        this.variables = "";
        this.needToRender = true;
    }

    private void parseMeta(String textMeta) {
        this.loopCondition = parseLoopCondition(textMeta);
        this.variables = parseVariables(textMeta);
        this.needToRender = parseNeedToRender(textMeta);
    }

    public String getLoopCondition() {
        return variables + loopCondition;
    }

    private String parseVariables(String rootMeta) {
        if (rootMeta.isEmpty()) {
            return "";
        }

        Pattern pattern = Pattern.compile("((.|\\n)*?)(?=\\{MetaInfoRow:)");
        Matcher matcher = pattern.matcher(rootMeta);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private String parseLoopCondition(String rootMeta) {
        if (rootMeta.isEmpty()) {
            return "";
        }

        Pattern pattern = Pattern.compile("(?<=loopCondition = )((.|\\n)*?)(?=}$|,)");
        Matcher matcher = pattern.matcher(rootMeta);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private boolean parseNeedToRender(String processedMeta) {

        if (processedMeta.isEmpty()) {
            return true;
        }

        Pattern patternRender = Pattern.compile("(?<=needToRender = )((.|\\n)*?)(?=}$|,)");
        Matcher matcherRender = patternRender.matcher(processedMeta);

        if (matcherRender.find()) {
            return Boolean.parseBoolean(matcherRender.group());
        }

        return true;
    }

    public static boolean isMetaRow(XWPFTableRow xwpfTableRow) {
        return isStartMetaRow(xwpfTableRow) || isEndMetaRow(xwpfTableRow);
    }

    public static boolean isStartMetaRow(XWPFTableRow xwpfTableRow) {
        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoRow: .*?}$");
        Matcher matcher = pattern.matcher(xwpfTableRow.getCell(0).getText());
        return matcher.find();
    }

    public static boolean isEndMetaRow(XWPFTableRow xwpfTableRow) {
        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoRow}$");
        Matcher matcher = pattern.matcher(xwpfTableRow.getCell(0).getText());
        return matcher.find();
    }
}
