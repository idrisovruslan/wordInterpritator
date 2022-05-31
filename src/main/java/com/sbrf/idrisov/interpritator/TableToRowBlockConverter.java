package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.table.RowBlock;
import com.sbrf.idrisov.interpritator.entity.table.RowForTransform;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TableToRowBlockConverter {

    @Lookup
    private RowBlock getRowBlock(List<RowForTransform> rows, String meta) {return null;}

    @Lookup
    private RowBlock getRowBlock(List<RowForTransform> rows, String meta, List<RowBlock> nestedBlocs) {return null;}

    @Lookup
    private RowForTransform getRowForTransform(XWPFTableRow row) {return null;}

    public List<RowBlock> getRowBlocks(XWPFTable table) {
        List<XWPFTableRow> rows = table.getRows();
        return getRowBlocks(table, rows);
    }

    public List<RowBlock> getRowBlocks(List<XWPFTableRow> rows) {

        if (rows.isEmpty()) {
            throw new RuntimeException();
        }

        XWPFTable table = rows.get(0).getTable();
        return getRowBlocks(table, rows);
    }



    private List<RowBlock> getRowBlocks(XWPFTable table, List<XWPFTableRow> rows) {
        List<RowBlock> blocks = new ArrayList<>();

        List<RowForTransform> temp = new ArrayList<>();
        String meta = "";

        Deque<Integer> rowsToRemove = new LinkedList<>();

        int counterStartsBlocks = 0;
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            if (counterStartsBlocks == 0 && isStartMetaRow(row)) {
                if (!temp.isEmpty()) {
                    blocks.add(getRowBlock(temp, meta));
                    temp = new ArrayList<>();
                }

                meta = row.getCell(0).getText();
                rowsToRemove.addFirst(i);
                counterStartsBlocks++;
                continue;
            }

            if (!isMetaRow(row)) {
                temp.add(getRowForTransform(row));
                continue;
            }

            if (counterStartsBlocks == 1 && isEndMetaRow(row)) {
                blocks.add(getRowBlock(temp, meta));
                rowsToRemove.addFirst(i);

                counterStartsBlocks = 0;
                temp = new ArrayList<>();
                meta = "";
                continue;
            }

            if (counterStartsBlocks != 0 && isStartMetaRow(row)) {
                temp.add(getRowForTransform(row));
                counterStartsBlocks++;
                continue;
            }

            if (counterStartsBlocks > 1 && isEndMetaRow(row)) {
                temp.add(getRowForTransform(row));
                counterStartsBlocks--;
                continue;
            }

            throw new RuntimeException();
        }
        rowsToRemove.forEach(table::removeRow);
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
