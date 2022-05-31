package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.RowUtils;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
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

    @Lookup
    public RowForTransform getRowForTransform(XWPFTableRow row) {return null;}

    public void transform(Map<String, Object> model) {
        row.getTableCells().stream().map(this::getCellForTransform).forEach(cellForTransform -> cellForTransform.transform(model));
    }

    public void removeRow() {
        XWPFTable table = row.getTable();
        table.removeRow(getPosOfRow());
    }

    public int getPosOfRow() {
        return RowUtils.getPosOfRow(row);
    }

    public void addVariablesValue(String value) {
        row.getTableCells().forEach(xwpfTableCell -> {
            XmlCursor cursor = xwpfTableCell.getParagraphArray(0).getCTP().newCursor();
            XWPFParagraph paragraph = xwpfTableCell.insertNewParagraph(cursor);
            paragraph.createRun().setText(value);
        });
    }

    public RowForTransform copyRow(int newPosition) {
        XWPFTable table = row.getTable();

        XWPFTableRow copiedRow = new XWPFTableRow((CTRow) row.getCtRow().copy(), table);

        table.addRow(copiedRow, newPosition);

        return getRowForTransform(copiedRow);
    }

}
