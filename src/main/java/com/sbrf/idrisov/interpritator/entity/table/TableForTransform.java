package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
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

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TableForTransform(XWPFTable table, String meta) {
        this.table = table;
        this.meta = meta;
    }

    @Lookup
    public RowBlock getRowBlock(List<RowForTransform> rows, String meta) {return null;}

    @Lookup
    public RowForTransform getRowForTransform(XWPFTableRow row) {return null;}

    public void transform(Map<String, Object> model) {
        if (!needToRender(model)) {
            removeTable();
            return;
        }

        List<RowBlock> blocks = getRowBlocks();

        blocks.forEach(rowBlock -> rowBlock.transform(model));
    }

    private List<RowBlock> getRowBlocks() {
        List<RowBlock> blocks = new ArrayList<>();

        List<RowForTransform> temp = new ArrayList<>();
        String meta = "";
        boolean rowBlockStarted = false;

        List<XWPFTableRow> rows = table.getRows();

        Deque<Integer> rowsToRemove = new LinkedList<>();

        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            if (!rowBlockStarted && isMetaRow(row)) {
                meta = row.getCell(0).getText();
                rowsToRemove.addFirst(i);
                rowBlockStarted = true;
                continue;
            }

            if (rowBlockStarted && isMetaRow(row)) {
                blocks.add(getRowBlock(temp, meta));
                rowsToRemove.addFirst(i);

                rowBlockStarted = false;
                temp = new ArrayList<>();
                meta = "";
                continue;
            }

            if (rowBlockStarted && !isMetaRow(row)) {
                temp.add(getRowForTransform(row));
                continue;
            }

            if (!rowBlockStarted && !isMetaRow(row)) {
                temp.add(getRowForTransform(row));
                blocks.add(getRowBlock(temp, meta));

                temp = new ArrayList<>();
                continue;
            }
            throw new RuntimeException();
        }
        rowsToRemove.forEach(table::removeRow);
        return blocks;
    }

    private boolean isMetaRow(XWPFTableRow xwpfTableRow) {
        return isStartMetaRow(xwpfTableRow) || isEndMetaRow(xwpfTableRow);
    }

    private boolean isStartMetaRow(XWPFTableRow xwpfTableRow) {
        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoRow: .*?}$");
        Matcher matcher = pattern.matcher(xwpfTableRow.getCell(0).getText());
        return matcher.find();
    }

    private boolean isEndMetaRow(XWPFTableRow xwpfTableRow) {
        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoRow}$");
        Matcher matcher = pattern.matcher(xwpfTableRow.getCell(0).getText());
        return matcher.find();
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
