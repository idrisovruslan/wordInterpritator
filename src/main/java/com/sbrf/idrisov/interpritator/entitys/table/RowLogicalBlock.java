package com.sbrf.idrisov.interpritator.entitys.table;

import com.sbrf.idrisov.interpritator.converters.TableToRowLogicalBlockConverter;
import com.sbrf.idrisov.interpritator.entitys.table.metainfo.RowMetaInfo;
import com.sbrf.idrisov.interpritator.services.FreemarkerService;
import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class RowLogicalBlock {

    @Getter
    private final List<RowForTransform> rows;
    private final RowMetaInfo meta;
    private boolean haveMeta = false;

    @Autowired
    private FreemarkerService freemarkerService;

    @Autowired
    private TableToRowLogicalBlockConverter tableToRowLogicalBlockConverter;

    @Lookup
    public RowLogicalBlock getRowLogicalBlock(List<RowForTransform> rows, RowMetaInfo meta, boolean haveMeta) {return null;}

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowLogicalBlock(List<RowForTransform> rows, RowMetaInfo meta, boolean haveMeta) {
        this.rows = rows;
        this.meta = meta;
        this.haveMeta = haveMeta;
    }

    public void transform(Map<String, Object> model) {
        String needToRenderProcessed = freemarkerService.getProcessedText(meta.getNeedToRenderCondition(), model);
        if (!Boolean.parseBoolean(needToRenderProcessed)) {
            removeRowBlock();
            return;
        }

        String processedLoopCondition = getProcessedLoopCondition(model);

        if (processedLoopCondition.isEmpty()) {
            rows.forEach(rowForTransform -> rowForTransform.transform(model, haveMeta));
        } else {
            String[] values = processedLoopCondition.split("\\n");

            for (int i = values.length - 1; i > 0; i--) {
                RowLogicalBlock newRowLogicalBlock = copyRowBlockAfterThis();
                newRowLogicalBlock.addValuesToRows(values[i]);
                newRowLogicalBlock.transform(model);

                reTransform(model, newRowLogicalBlock);
            }

            addValuesToRows(values[0]);

            RowLogicalBlock rowLogicalBlock = getRowLogicalBlock(rows, new RowMetaInfo(), haveMeta);
            rowLogicalBlock.transform(model);

            reTransform(model, rowLogicalBlock);
        }
    }

    /**
     * на случай многоуровневых циклов
     * @param model модель данных
     * @param newRowLogicalBlock полученный блок после раскрытия внешнего цикла
     */

    private void reTransform(Map<String, Object> model, RowLogicalBlock newRowLogicalBlock) {
        List<XWPFTableRow> rows = newRowLogicalBlock.getRows().stream().map(RowForTransform::getRow).collect(Collectors.toList());
        List<RowLogicalBlock> nestedRowLogicalBlocks = tableToRowLogicalBlockConverter.getRowBlocks(rows, model);
        nestedRowLogicalBlocks.forEach(nestedRowLogicalBlock -> nestedRowLogicalBlock.transform(model));
    }

    private void addValuesToRows(String values) {
        rows.forEach(rowForTransform -> rowForTransform.addVariablesValue(values));
    }

    private RowLogicalBlock copyRowBlockAfterThis() {
        Deque<RowForTransform> newRows = new LinkedList<>();

        int positionAfterThis = rows.get(rows.size() - 1).getPosOfRow() + 1;

        for (int i = rows.size() - 1; i >= 0; i--) {
            newRows.addFirst(rows.get(i).copyRow(positionAfterThis));
        }

        return getRowLogicalBlock(new ArrayList<>(newRows), new RowMetaInfo(), haveMeta);
    }

    private String getProcessedLoopCondition(Map<String, Object> model) {
        return freemarkerService.getProcessedText(meta.getLoopCondition(), model);
    }

    private void removeRowBlock() {
        rows.forEach(RowForTransform::removeRow);
    }
}
