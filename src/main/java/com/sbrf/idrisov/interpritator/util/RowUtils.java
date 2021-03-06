package com.sbrf.idrisov.interpritator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.List;

//TODO проксю запели или что то типа того
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RowUtils {

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
