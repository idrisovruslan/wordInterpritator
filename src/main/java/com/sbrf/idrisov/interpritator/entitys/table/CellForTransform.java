package com.sbrf.idrisov.interpritator.entitys.table;

import com.sbrf.idrisov.interpritator.converters.IBodyToBodyBlockConverter;
import com.sbrf.idrisov.interpritator.entitys.BodyBlock;
import com.sbrf.idrisov.interpritator.services.SquashParagraphsService;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class CellForTransform {

    private final XWPFTableCell cell;

    @Autowired
    private IBodyToBodyBlockConverter IBodyToBodyBlockConverter;

    @Autowired
    private SquashParagraphsService squashParagraphsService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CellForTransform(XWPFTableCell cell) {
        this.cell = cell;
    }

    public void transform(Map<String, Object> model) {
        List<BodyBlock> paragraphsBlocks = IBodyToBodyBlockConverter
                .generateBodyBlocks(cell);

        paragraphsBlocks.forEach(paragraphsBlock -> paragraphsBlock.transformBlock(model));

        squashParagraphsService.squashParagraphs(cell);
    }
}
