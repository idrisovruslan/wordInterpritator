package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.DocumentToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.SquashParagraphsService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class RowBlock implements RootBlock {

    private final List<RowForTransform> rows;
    private final String meta;

    @Autowired
    private DocumentToBodyBlockConverter documentToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    @Autowired
    private FreemarkerService freemarkerService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowBlock(List<RowForTransform> rows, String meta) {
        this.rows = rows;
        this.meta = meta;
    }

    @Override
    public void transform(Map<String, Object> model) {
        if (!needToRender(model)) {
            removeRowBlock();
            return;
        }

        rows.forEach(rowForTransform -> rowForTransform.transform(model));
    }

    private boolean needToRender(Map<String, Object> model) {

        if (meta.isEmpty()) {
            return true;
        }

        //TODO  в объект
        Pattern pattern = Pattern.compile("\\{MetaInfoRow: .*?}$");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            String processedMeta = freemarkerService.getProcessedText(matcher.group(), model);

            //TODO  в объект
            Pattern patternRender = Pattern.compile("(?<=\\{MetaInfoRow: needToRender = )(.*?)(?=\\})");
            Matcher matcherRender = patternRender.matcher(processedMeta);

            if (matcherRender.find()) {
                return Boolean.parseBoolean(matcherRender.group());
            }

            throw new RuntimeException();
        }
        throw new RuntimeException();
    }

    private void removeRowBlock() {
        rows.forEach(RowForTransform::removeRow);
    }
}
