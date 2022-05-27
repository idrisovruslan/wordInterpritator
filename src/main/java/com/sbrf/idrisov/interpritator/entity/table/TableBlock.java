package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.DocumentToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.SquashParagraphsService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFTable;
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
    public TableForTransform getTableForTransform(XWPFTable table) {return null;}

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
        tables.stream().map(this::getTableForTransform).forEach(tableForTransform -> tableForTransform.transform(model));
    }
}
