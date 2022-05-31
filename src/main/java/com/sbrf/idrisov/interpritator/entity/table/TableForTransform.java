package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.TableToRowBlockConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sbrf.idrisov.interpritator.ParagraphUtils.getPosOfBodyElement;

@Component
@Scope("prototype")
public class TableForTransform {

    private final XWPFTable table;
    private final String meta;

    @Autowired
    private FreemarkerService freemarkerService;

    @Autowired
    private TableToRowBlockConverter tableToRowBlockConverter;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TableForTransform(XWPFTable table, String meta) {
        this.table = table;
        this.meta = meta;
    }


    public void transform(Map<String, Object> model) {
        if (!needToRender(model)) {
            removeTable();
            return;
        }

        List<RowBlock> blocks = tableToRowBlockConverter.getRowBlocks(table);

        blocks.forEach(rowBlock -> rowBlock.transform(model));
    }

    private boolean needToRender(Map<String, Object> model) {

        if (meta.isEmpty()) {
            return true;
        }

        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoTable: .*?}$");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            String processedMeta = freemarkerService.getProcessedText(matcher.group(), model);

            //TODO  в объект
            Pattern patternRender = Pattern.compile("(?<=\\{MetaInfoTable: needToRender = )(.*?)(?=\\})");
            Matcher matcherRender = patternRender.matcher(processedMeta);

            if (matcherRender.find()) {
                return Boolean.parseBoolean(matcherRender.group());
            }

            throw new RuntimeException();
        }
        return true;
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
