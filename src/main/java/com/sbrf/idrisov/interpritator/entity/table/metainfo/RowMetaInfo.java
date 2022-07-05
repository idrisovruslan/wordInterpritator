package com.sbrf.idrisov.interpritator.entity.table.metainfo;

import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RowMetaInfo {

    private String variables;
    private String loopCondition;
    //TODO тут обработай фримаркером
    @Getter
    private String needToRenderCondition;

    public RowMetaInfo(String textMeta) {
        parseMeta(textMeta);
    }

    public RowMetaInfo() {
        this.loopCondition = "";
        this.variables = "";
        this.needToRenderCondition = "true";
    }

    private void parseMeta(String textMeta) {
        this.loopCondition = parseLoopCondition(textMeta);
        this.variables = parseVariables(textMeta);
        this.needToRenderCondition = parseNeedToRender(textMeta);
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

    private String parseNeedToRender(String notProcessedMeta) {

        if (notProcessedMeta.isEmpty()) {
            return "true";
        }

        Pattern patternRender = Pattern.compile("(?<=needToRender = )((.|\\n)*?)(?=}$|,)");
        Matcher matcherRender = patternRender.matcher(notProcessedMeta);

        if (matcherRender.find()) {
            return matcherRender.group();
        }

        return "true";
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
