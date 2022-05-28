package com.sbrf.idrisov.interpritator.entity.table;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
public class TableForTransform {

    private final XWPFTable table;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TableForTransform(XWPFTable table) {
        this.table = table;
    }

    @Lookup
    public RowForTransform getRowBlock(XWPFTableRow row) {return null;}

    public void transform(Map<String, Object> model) {
        table.getRows().stream().map(this::getRowBlock).forEach(rowForTransform -> rowForTransform.transform(model));
    }
}
