package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.DocumentToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.SquashParagraphsService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class TableBlock implements RootBlock {

    private final List<XWPFTable> tables;

    @Lookup
    public TableForTransform getTableForTransform(XWPFTable table, String meta) {return null;}

    @Autowired
    private DocumentToBodyBlockConverter documentToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TableBlock(List<XWPFTable> tables) {
        this.tables = tables;
    }

    @Override
    public void transform(Map<String, Object> model) {
        for (XWPFTable table : tables) {
            String meta = table.getRow(0).getCell(0).getText();
            table.removeRow(0);
            TableForTransform tableForTransform = getTableForTransform(table, meta);
            tableForTransform.transform(model);
        }
        tables.forEach(this::commitTableRows);
    }

    private void commitTableRows(XWPFTable table) {
        int rowNr = 0;
        for (XWPFTableRow tableRow : table.getRows()) {
            table.getCTTbl().setTrArray(rowNr++, tableRow.getCtRow());
        }
    }

}
