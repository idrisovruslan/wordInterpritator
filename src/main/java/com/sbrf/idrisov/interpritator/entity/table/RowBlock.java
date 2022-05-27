package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
public class RowBlock implements RootBlock {

    private final XWPFTableRow row;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowBlock(XWPFTableRow row) {
        this.row = row;
    }

    @Lookup
    public CellBlock getCellBlock(XWPFTableCell cell) {return null;}

    @Override
    public void transform(Map<String, Object> model) {
        row.getTableCells().stream().map(this::getCellBlock).forEach(cellBlock -> cellBlock.transform(model));
    }
}
