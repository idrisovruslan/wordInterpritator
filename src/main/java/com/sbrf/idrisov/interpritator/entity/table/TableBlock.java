package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
public class TableBlock implements RootBlock {

    private final XWPFTable table;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TableBlock(XWPFTable table) {
        this.table = table;
    }

    @Lookup
    public RowBlock getRowBlock(XWPFTableRow row) {return null;}

    @Override
    public void transform(Map<String, Object> model) {
        table.getRows().stream().map(this::getRowBlock).forEach(rowBlock -> rowBlock.transform(model));
    }
}
