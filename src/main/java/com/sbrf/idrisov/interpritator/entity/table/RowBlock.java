package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.DocumentToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.SquashParagraphsService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class RowBlock implements RootBlock {

    private final List<RowForTransform> rows;

    @Autowired
    private DocumentToBodyBlockConverter documentToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RowBlock(List<RowForTransform> rows) {
        this.rows = rows;
    }

    @Override
    public void transform(Map<String, Object> model) {

    }
}
