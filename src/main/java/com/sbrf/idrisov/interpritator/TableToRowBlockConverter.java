package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.table.MetaInfoRow;
import com.sbrf.idrisov.interpritator.entity.table.RowBlock;
import com.sbrf.idrisov.interpritator.entity.table.RowForTransform;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sbrf.idrisov.interpritator.RowUtils.getPosOfRow;

@Service
public class TableToRowBlockConverter {

    @Lookup
    public RowBlock getRowBlock(List<RowForTransform> rows, MetaInfoRow meta, List<RowBlock> nestedBlocs) {return null;}

    @Lookup
    public RowForTransform getRowForTransform(XWPFTableRow row) {return null;}

    public List<RowBlock> getRowBlocks(XWPFTable table) {
        List<XWPFTableRow> rows = new ArrayList<>(table.getRows());
        List<RowBlock> rowBlocks = getRowBlocks(table, rows);
        return rowBlocks;
    }

    private List<RowBlock> getRowBlocks(List<XWPFTableRow> rows) {

        if (rows.isEmpty()) {
            throw new RuntimeException();
        }

        XWPFTable table = rows.get(0).getTable();
        List<RowBlock> rowBlocks = getRowBlocks(table, rows);
        return rowBlocks;
    }

    private List<RowBlock> getRowBlocks(XWPFTable table, List<XWPFTableRow> rows) {
        List<RowBlock> blocks = new ArrayList<>();
        List<RowBlock> nestedBlocks = new ArrayList<>();

        List<RowForTransform> temp = new ArrayList<>();
        MetaInfoRow meta = new MetaInfoRow();

        Deque<XWPFTableRow> rowsToRemove = new LinkedList<>();

        int counterStartsBlocks = 0;
        while (rows.size() != 0) {
            XWPFTableRow row = rows.get(0);
            if (counterStartsBlocks == 0 && isStartMetaRow(row)) {
                if (!temp.isEmpty()) {
                    blocks.add(getRowBlock(temp, meta, nestedBlocks));
                    temp = new ArrayList<>();
                }

                meta = new MetaInfoRow(row.getCell(0).getText());
                rowsToRemove.addFirst(row);
                counterStartsBlocks++;
                rows.remove(0);
                continue;
            }

            if (!isMetaRow(row)) {
                temp.add(getRowForTransform(row));
                rows.remove(0);
                continue;
            }

            if (counterStartsBlocks == 1 && isEndMetaRow(row)) {
                blocks.add(getRowBlock(temp, meta, nestedBlocks));
                rowsToRemove.addFirst(row);

                counterStartsBlocks = 0;
                temp = new ArrayList<>();
                meta = new MetaInfoRow();
                rows.remove(0);
                continue;
            }

            if (counterStartsBlocks == 1 && isStartMetaRow(row)) {
                nestedBlocks = getRowBlocks(rows);
                continue;
            }

            if (counterStartsBlocks == 0 && isEndMetaRow(row)) {
                break;
            }
        }
        rowsToRemove.forEach(xwpfTableRow -> table.removeRow(getPosOfRow(xwpfTableRow)));
        return blocks;
    }

    private boolean isMetaRow(XWPFTableRow xwpfTableRow) {
        return isStartMetaRow(xwpfTableRow) || isEndMetaRow(xwpfTableRow);
    }

    private boolean isStartMetaRow(XWPFTableRow xwpfTableRow) {
        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoRow: .*?}$");
        Matcher matcher = pattern.matcher(xwpfTableRow.getCell(0).getText());
        return matcher.find();
    }

    private boolean isEndMetaRow(XWPFTableRow xwpfTableRow) {
        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoRow}$");
        Matcher matcher = pattern.matcher(xwpfTableRow.getCell(0).getText());
        return matcher.find();
    }
}
