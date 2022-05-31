package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class RowBlock implements RootBlock {

    private final List<RowForTransform> rows;
    private final String meta;

    @Autowired
    private FreemarkerService freemarkerService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowBlock(List<RowForTransform> rows, String meta) {
        this.rows = rows;
        this.meta = meta;
    }

    @Override
    public void transform(Map<String, Object> model) {
        String processedMeta = getProcessedMeta(model);

        if (!needToRender(processedMeta)) {
            removeRowBlock();
            return;
        }

        String loopCondition = getLoopCondition(processedMeta);

        if (loopCondition.isEmpty()) {
            rows.forEach(rowForTransform -> rowForTransform.transform(model));
        } else {
            String[] values = loopCondition.split("\\n");

            for (int i = values.length - 1; i > 0; i--) {
                RowBlock newRowBlock = copyRowBlockAfterThis();
                newRowBlock.addValuesToRows(values[i]);
                newRowBlock.transform(model);
            }

            addValuesToRows(values[0]);
            new RowBlock(rows, "").transform(model);
        }
    }

    private void addValuesToRows(String value) {
        rows.forEach(rowForTransform -> rowForTransform.addVariablesValue(value));
    }

    private RowBlock copyRowBlockAfterThis() {
        List<RowForTransform> newRows = new ArrayList<>();

        int positionAfterThis = rows.get(rows.size() - 1).getPosOfRow() + 1;

        for (int i = rows.size() - 1; i >= 0; i--) {
            newRows.add(rows.get(i).copyRow(positionAfterThis));
        }

        return new RowBlock(newRows, "");
    }

    private String getProcessedMeta(Map<String, Object> model) {
        if (meta.isEmpty()) {
            return "";
        }

        return freemarkerService.getProcessedText(meta, model);
    }

    private String getLoopCondition(String processedMeta) {
        if (processedMeta.isEmpty()) {
            return "";
        }

        //TODO  в объект
        Pattern pattern = Pattern.compile("(?<=loopCondition = )((.|\\n)*?)(?=})");
        Matcher matcher = pattern.matcher(processedMeta);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private boolean needToRender(String processedMeta) {

        if (processedMeta.isEmpty()) {
            return true;
        }

        //TODO  в объект
        Pattern patternRender = Pattern.compile("(?<=needToRender = )(.*?)(?=}|,)");
        Matcher matcherRender = patternRender.matcher(processedMeta);

        if (matcherRender.find()) {
            return Boolean.parseBoolean(matcherRender.group());
        }

        return true;
    }

    private void removeRowBlock() {
        rows.forEach(RowForTransform::removeRow);
    }
}
