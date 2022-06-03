package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.TableToRowBlockConverter;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class RowBlock implements RootBlock {

    @Getter
    private final List<RowForTransform> rows;
    private MetaInfoRow meta;
    @Setter
    private boolean haveMeta = false;

    @Autowired
    private FreemarkerService freemarkerService;

    @Lookup
    public RowBlock getRowBlock(List<RowForTransform> rows, MetaInfoRow meta, boolean haveMeta) {return null;}

    @Autowired
    private TableToRowBlockConverter tableToRowBlockConverter;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowBlock(List<RowForTransform> rows, MetaInfoRow meta, boolean haveMeta) {
        this.rows = rows;
        this.meta = meta;
        this.haveMeta = haveMeta;
    }

    @Override
    public void transform(Map<String, Object> model) {
        String needToRenderProcessed = freemarkerService.getProcessedText(meta.getNeedToRender(), model);
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
                RowBlock newRowBlock = copyRowBlockAfterThis();
                newRowBlock.addValuesToRows(values[i]);
                newRowBlock.transform(model);

                reRander(model, newRowBlock);
            }

            addValuesToRows(values[0]);

            RowBlock rowBlock = getRowBlock(rows, new MetaInfoRow(), haveMeta);
            rowBlock.transform(model);

            reRander(model, rowBlock);
        }
    }

    private void reRander(Map<String, Object> model, RowBlock newRowBlock) {
        List<XWPFTableRow> rows = newRowBlock.getRows().stream().map(RowForTransform::getRow).collect(Collectors.toList());
        List<RowBlock> nestedRowBlocks = tableToRowBlockConverter.getRowBlocks(rows, model);
        nestedRowBlocks.forEach(nestedRowBlock -> nestedRowBlock.transform(model));
    }

    private void addValuesToRows(String values) {
        rows.forEach(rowForTransform -> rowForTransform.addVariablesValue(values));
    }

    private RowBlock copyRowBlockAfterThis() {
        Deque<RowForTransform> newRows = new LinkedList<>();

        int positionAfterThis = rows.get(rows.size() - 1).getPosOfRow() + 1;

        for (int i = rows.size() - 1; i >= 0; i--) {
            newRows.addFirst(rows.get(i).copyRow(positionAfterThis));
        }

        return getRowBlock(new ArrayList<>(newRows), new MetaInfoRow(), haveMeta);
    }

    private String getProcessedLoopCondition(Map<String, Object> model) {
        return freemarkerService.getProcessedText(meta.getLoopCondition(), model);
    }

    private void removeRowBlock() {
        rows.forEach(RowForTransform::removeRow);
    }
}
