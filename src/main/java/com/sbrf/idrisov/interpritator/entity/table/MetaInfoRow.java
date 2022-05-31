package com.sbrf.idrisov.interpritator.entity.table;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class MetaInfoRow {

    private String loopCondition;
    private boolean needToRender;

    public MetaInfoRow(String textMeta) {
        parseMeta(textMeta);
    }

    public MetaInfoRow() {
        this.loopCondition = "";
        this.needToRender = true;
    }

    private void parseMeta(String textMeta) {
        this.loopCondition = parseLoopCondition(textMeta);
        this.needToRender = parseNeedToRender(textMeta);
    }

    public void insertRootLoopCondition(MetaInfoRow rootMeta) {
        String rootLoopCondition = rootMeta.getLoopCondition();
        String thisLoopCondition = loopCondition;

        int insertPos = rootLoopCondition.indexOf("<#sep>${'\\n'}</#sep>");
        loopCondition = new StringBuilder(rootLoopCondition).insert(insertPos, thisLoopCondition).toString();
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
}
