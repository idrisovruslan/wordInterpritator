package com.sbrf.idrisov.interpritator.entitys.table;

import com.sbrf.idrisov.interpritator.converters.TableToRowLogicalBlockConverter;
import com.sbrf.idrisov.interpritator.entitys.table.metainfo.TableMetaInfo;
import com.sbrf.idrisov.interpritator.services.FreemarkerService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.sbrf.idrisov.interpritator.utils.ParagraphUtils.getPosOfBodyElement;

@Component
@Scope("prototype")
public class TableForTransform {

    private final XWPFTable table;
    private final TableMetaInfo tableMetaInfo;

    @Autowired
    private FreemarkerService freemarkerService;

    @Autowired
    private TableToRowLogicalBlockConverter tableToRowLogicalBlockConverter;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TableForTransform(XWPFTable table, TableMetaInfo tableMetaInfo) {
        this.table = table;
        this.tableMetaInfo = tableMetaInfo;
    }


    public void transform(Map<String, Object> model) {
        String needToRenderProcessed = freemarkerService.getProcessedText(tableMetaInfo.getNeedToRenderCondition(), model);
        if (!Boolean.parseBoolean(needToRenderProcessed)) {
            removeTable();
            return;
        }

        List<RowLogicalBlock> blocks = tableToRowLogicalBlockConverter.getRowBlocks(table, model);

        blocks.forEach(rowLogicalBlock -> rowLogicalBlock.transform(model));
    }

    private void removeTable() {
        //TODO надо удалять следующий параграф тк таблица автоматом генерит параграф после себя и если его не удалить, булет лишний отступ
        if (table.getBody() instanceof XWPFTableCell) {
            XWPFTableCell cell = (XWPFTableCell) table.getBody();
            int pos = getPosOfBodyElement(table, cell.getTables());
            cell.removeTable(pos);
        } else if (table.getBody() instanceof XWPFDocument) {
            XWPFDocument document = (XWPFDocument) table.getBody();
            int pos = getPosOfBodyElement(table, document.getBodyElements());
            document.removeBodyElement(pos);
        } else {
            throw new RuntimeException();
        }
    }
}
