package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.entity.BodyBlock;
import com.sbrf.idrisov.interpritator.entity.table.metainfo.TableMetaInfo;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.sbrf.idrisov.interpritator.entity.table.metainfo.TableMetaInfo.isTableMetaInfo;

@Component
@Scope("prototype")
public class TableBlock implements BodyBlock {

    private final List<XWPFTable> tables;

    @Lookup
    public TableForTransform getTableForTransform(XWPFTable table, TableMetaInfo tableMetaInfo) {return null;}

    public TableBlock(List<XWPFTable> tables) {
        this.tables = tables;
    }

    @Override
    public void transformBlock(Map<String, Object> model) {
        for (XWPFTable table : tables) {

            TableMetaInfo tableMetaInfo = new TableMetaInfo();

            String firstRowText = table.getRow(0).getCell(0).getText();
            if (isTableMetaInfo(firstRowText)) {
                table.removeRow(0);
                tableMetaInfo = new TableMetaInfo(firstRowText);
            }

            TableForTransform tableForTransform = getTableForTransform(table, tableMetaInfo);
            tableForTransform.transform(model);
        }
        tables.forEach(this::commitTableRows);
    }

    private void commitTableRows(XWPFTable table) {
        try {
            int rowNr = 0;
            for (XWPFTableRow tableRow : table.getRows()) {
                table.getCTTbl().setTrArray(rowNr++, tableRow.getCtRow());
            }
        } catch (XmlValueDisconnectedException ignored) {
            //TODO remove crutch
        }
    }

}
