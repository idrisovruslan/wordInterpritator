package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
public class TableBlock implements RootBlock {

    private final XWPFTable table;

    @Autowired
    private FreemarkerService freemarkerService;

    public TableBlock(XWPFTable table) {
        this.table = table;
    }

    @Override
    public void transform(Map<String, Object> model) {
        System.out.println();
        System.out.println();

    }
}
