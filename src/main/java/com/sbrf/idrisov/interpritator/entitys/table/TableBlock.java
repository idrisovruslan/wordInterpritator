package com.sbrf.idrisov.interpritator.entitys.table;

import com.sbrf.idrisov.interpritator.converters.DocumentToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.entitys.RootBlock;
import com.sbrf.idrisov.interpritator.services.SquashParagraphsService;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String meta = "";

            if (haveMetaRow(table)) {
                meta = table.getRow(0).getCell(0).getText();
                table.removeRow(0);
            }

            TableForTransform tableForTransform = getTableForTransform(table, meta);
            tableForTransform.transform(model);
        }
        tables.forEach(this::commitTableRows);
    }

    private boolean haveMetaRow(XWPFTable xwpfTable) {
        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoTable: .*?}$");
        Matcher matcher = pattern.matcher(xwpfTable.getRow(0).getCell(0).getText());
        return matcher.find();
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
