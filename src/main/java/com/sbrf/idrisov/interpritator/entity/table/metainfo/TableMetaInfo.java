package com.sbrf.idrisov.interpritator.entity.table.metainfo;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableMetaInfo {

    //TODO тут обработай фримаркером
    @Getter
    private final String needToRenderCondition;

    public TableMetaInfo(String textMeta) {
        this.needToRenderCondition = parseNeedToRender(textMeta);
    }

    public TableMetaInfo() {
        this.needToRenderCondition = "true";
    }

    public static boolean isTableMetaInfo(String text) {
        Pattern pattern = Pattern.compile("\\{MetaInfoTable: .*?}$");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
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
}
