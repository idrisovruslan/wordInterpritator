package com.sbrf.idrisov.interpritator.entity.table;

import com.sbrf.idrisov.interpritator.DocumentToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.SquashParagraphsService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class CellBlock implements RootBlock {

    private final XWPFTableCell cell;

    @Autowired
    private DocumentToBodyBlockConverter documentToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CellBlock(XWPFTableCell cell) {
        this.cell = cell;
    }

    @Override
    public void transform(Map<String, Object> model) {
        List<RootBlock> paragraphsBlocks = documentToBodyBlockConverter
                .generateBlocksForTransform(cell);

        paragraphsBlocks.forEach(paragraphsBlock -> paragraphsBlock.transform(model));

        squashParagraphsService.squashParagraphs(cell);
    }
}
