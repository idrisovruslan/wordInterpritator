package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import java.util.List;
import java.util.Objects;

//TODO проксю запели или что то типа того
public class RowUtils {
    private RowUtils() {
    }

    public static int getPosOfRow(XWPFTableRow row) {
        List<XWPFTableRow> tableRows = row.getTable().getRows();
        XWPFTableRow current;
        for (int i = 0; i < tableRows.size(); i++) {
            current = tableRows.get(i);
            if (current.equals(row)) {
                return i;
            }
        }
        return -1;
    }
}
