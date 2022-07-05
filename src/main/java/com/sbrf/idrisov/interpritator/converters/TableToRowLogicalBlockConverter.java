package com.sbrf.idrisov.interpritator.converters;

import com.sbrf.idrisov.interpritator.entitys.table.RowForTransform;
import com.sbrf.idrisov.interpritator.entitys.table.RowLogicalBlock;
import com.sbrf.idrisov.interpritator.entitys.table.metainfo.RowMetaInfo;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.sbrf.idrisov.interpritator.entitys.table.metainfo.RowMetaInfo.*;
import static com.sbrf.idrisov.interpritator.utils.RowUtils.getPosOfRow;

@Service
public class TableToRowLogicalBlockConverter {

    @Lookup
    public RowLogicalBlock getRowBlock(List<RowForTransform> rows, RowMetaInfo meta, boolean haveMeta) {return null;}

    @Lookup
    public RowForTransform getRowForTransform(XWPFTableRow row) {return null;}

    public List<RowLogicalBlock> getRowBlocks(XWPFTable table, Map<String, Object> model) {
        List<XWPFTableRow> rows = new ArrayList<>(table.getRows());
        List<RowLogicalBlock> rowLogicalBlocks = getRowBlocks(table, rows, model);
        return rowLogicalBlocks;
    }

    public List<RowLogicalBlock> getRowBlocks(List<XWPFTableRow> rows, Map<String, Object> model) {

        if (rows.isEmpty()) {
            throw new RuntimeException();
        }

        XWPFTable table = rows.get(0).getTable();
        List<RowLogicalBlock> rowLogicalBlocks = getRowBlocks(table, rows, model);
        return rowLogicalBlocks;
    }

    private List<RowLogicalBlock> getRowBlocks(XWPFTable table, List<XWPFTableRow> rows, Map<String, Object> model) {
        List<RowLogicalBlock> blocks = new ArrayList<>();

        List<RowForTransform> temp = new ArrayList<>();
        RowMetaInfo meta = new RowMetaInfo();

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
                    meta = new RowMetaInfo(row.getCell(0).getText());
                    haveNestedMeta = false;
                }

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
                meta = new RowMetaInfo();
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
