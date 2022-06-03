package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.table.MetaInfoRow;
import com.sbrf.idrisov.interpritator.entity.table.RowBlock;
import com.sbrf.idrisov.interpritator.entity.table.RowForTransform;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.sbrf.idrisov.interpritator.RowUtils.getPosOfRow;
import static com.sbrf.idrisov.interpritator.entity.table.MetaInfoRow.*;

@Service
public class TableToRowBlockConverter {

    @Lookup
    public RowBlock getRowBlock(List<RowForTransform> rows, MetaInfoRow meta, boolean haveMeta) {return null;}

    @Lookup
    public RowForTransform getRowForTransform(XWPFTableRow row) {return null;}

    public List<RowBlock> getRowBlocks(XWPFTable table, Map<String, Object> model) {
        List<XWPFTableRow> rows = new ArrayList<>(table.getRows());
        List<RowBlock> rowBlocks = getRowBlocks(table, rows, model);
        return rowBlocks;
    }

    public List<RowBlock> getRowBlocks(List<XWPFTableRow> rows, Map<String, Object> model) {

        if (rows.isEmpty()) {
            throw new RuntimeException();
        }

        XWPFTable table = rows.get(0).getTable();
        List<RowBlock> rowBlocks = getRowBlocks(table, rows, model);
        return rowBlocks;
    }

    private List<RowBlock> getRowBlocks(XWPFTable table, List<XWPFTableRow> rows, Map<String, Object> model) {
        List<RowBlock> blocks = new ArrayList<>();

        List<RowForTransform> temp = new ArrayList<>();
        MetaInfoRow meta = new MetaInfoRow();

        Deque<XWPFTableRow> rowsToRemove = new LinkedList<>();

        boolean haveNestedMeta = false;
        int counterStartsBlocks = 0;
        while (rows.size() != 0) {
            XWPFTableRow row = rows.get(0);
            //начало блока
            if (counterStartsBlocks == 0 && isStartMetaRow(row)) {
                if (!temp.isEmpty()) {
                    blocks.add(getRowBlock(temp, meta, haveNestedMeta));
                    temp = new ArrayList<>();
                    meta = new MetaInfoRow();
                    haveNestedMeta = false;
                }

                meta = new MetaInfoRow(row.getCell(0).getText(), model);
                rowsToRemove.addFirst(row);
                counterStartsBlocks++;
                rows.remove(0);
                continue;
            }

            if (counterStartsBlocks > 1 && isEndMetaRow(row)) {
                temp.add(getRowForTransform(row));
                rows.remove(0);
                counterStartsBlocks--;
                continue;
            }

            if (counterStartsBlocks > 0 && isStartMetaRow(row)) {
                temp.add(getRowForTransform(row));
                rows.remove(0);
                counterStartsBlocks++;
                haveNestedMeta = true;
                continue;
            }

            if (!isMetaRow(row)) {
                temp.add(getRowForTransform(row));
                rows.remove(0);
                continue;
            }

            //конец блока
            if (counterStartsBlocks == 1 && isEndMetaRow(row)) {
                blocks.add(getRowBlock(temp, meta, haveNestedMeta));
                temp = new ArrayList<>();
                rowsToRemove.addFirst(row);
                counterStartsBlocks = 0;
                rows.remove(0);
                meta = new MetaInfoRow();
                haveNestedMeta = false;
                continue;
            }
            throw new RuntimeException();
        }

        if (!temp.isEmpty()) {
            blocks.add(getRowBlock(temp, meta, haveNestedMeta));
        }
        rowsToRemove.forEach(xwpfTableRow -> table.removeRow(getPosOfRow(xwpfTableRow)));
        return blocks;
    }
}
