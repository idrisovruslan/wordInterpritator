package com.sbrf.idrisov.interpritator.entity.table;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@Scope("prototype")
public class RowForTransform {

    private final XWPFTableRow row;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowForTransform(XWPFTableRow row) {
        this.row = row;
    }

    @Lookup
    public CellForTransform getCellForTransform(XWPFTableCell cell) {return null;}

    public void transform(Map<String, Object> model) {
        row.getTableCells().stream().map(this::getCellForTransform).forEach(cellForTransform -> cellForTransform.transform(model));
    }

    public void removeRow() {
        XWPFTable table = row.getTable();
        table.removeRow(getPosOfBodyElement(row, table.getRows()));
    }

    public static int getPosOfBodyElement(XWPFTableRow needle, List<XWPFTableRow> tableRows) {
        XWPFTableRow current;
        for (int i = 0; i < tableRows.size(); i++) {
            current = tableRows.get(i);
            if (current.equals(needle)) {
                return i;
            }
        }
        return -1;
    }
}
