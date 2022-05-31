package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.TableToRowBlockConverter;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class RowBlock implements RootBlock {

    private final List<RowForTransform> rows;
    private final List<RowBlock> nestedBlocs;
    private MetaInfoRow meta;

    @Lookup
    public RowBlock getRowBlock(List<RowForTransform> rows, MetaInfoRow meta, List<RowBlock> nestedBlocs) {return null;}

    @Autowired
    private FreemarkerService freemarkerService;

    @Autowired
    private TableToRowBlockConverter tableToRowBlockConverter;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowBlock(List<RowForTransform> rows, MetaInfoRow meta, List<RowBlock> nestedBlocs) {
        this.rows = rows;
        this.meta = meta;
        this.nestedBlocs = nestedBlocs;
    }

    @Override
    public void transform(Map<String, Object> model) {
        nestedBlocs.forEach(nestedBloc -> nestedBloc.insertRootLoopCondition(meta));

        if (!meta.isNeedToRender()) {
            removeRowBlock();
            return;
        }

        String processedLoopCondition = getProcessedLoopCondition(model);

        if (processedLoopCondition.isEmpty()) {
            rows.forEach(rowForTransform -> rowForTransform.transform(model));
        } else {
            String[] values = processedLoopCondition.split("\\n");

            nestedBlocs.forEach(x -> x.transform(model));

            for (int i = values.length - 1; i > 0; i--) {
                RowBlock newRowBlock = copyRowBlockAfterThis();
                newRowBlock.addValuesToRows(values[i]);
                newRowBlock.transform(model);
            }

            addValuesToRows(values[0]);

            getRowBlock(rows, new MetaInfoRow(), new ArrayList<>()).transform(model);

        }
    }

    public void insertRootLoopCondition(MetaInfoRow rootMeta) {
        meta.insertRootLoopCondition(rootMeta);
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

        return getRowBlock(newRows, new MetaInfoRow(), new ArrayList<>());
    }

    private String getProcessedLoopCondition(Map<String, Object> model) {
        return freemarkerService.getProcessedText(meta.getLoopCondition(), model);
    }

    private void removeRowBlock() {
        rows.forEach(RowForTransform::removeRow);
    }
}
