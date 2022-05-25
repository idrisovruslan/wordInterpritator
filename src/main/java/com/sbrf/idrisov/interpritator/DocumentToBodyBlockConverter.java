package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.RootBlock;
import com.sbrf.idrisov.interpritator.entity.paragraph.ParagraphsBlock;
import com.sbrf.idrisov.interpritator.entity.table.TableBlock;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentToBodyBlockConverter {

    @Lookup
    public ParagraphsBlock getParagraphsBlock() {return null;}

    @Lookup
    public TableBlock getTableBlock(XWPFTable table) {return null;}

    public List<RootBlock> generateBlocksForTransform(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        List<RootBlock> blocksForTransform = new ArrayList<>();

        for (int i = 0; i < bodyElements.size(); i++) {
            if (bodyElements.get(i) instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElements.get(i);
                ParagraphUtils.squashRuns(paragraph);

                addMetaInfoForRuns(paragraph);

                if (blocksForTransform.isEmpty() || !(blocksForTransform.get(blocksForTransform.size() - 1) instanceof ParagraphsBlock)) {
                    ParagraphsBlock paragraphsBlock = getParagraphsBlock();
                    paragraphsBlock.addNewElement(paragraph);
                    blocksForTransform.add(paragraphsBlock);
                    continue;
                }

                ParagraphsBlock paragraphsBlock = (ParagraphsBlock) blocksForTransform.get(blocksForTransform.size() - 1);
                paragraphsBlock.addNewElement(paragraph);

            } else if (bodyElements.get(i) instanceof XWPFTable){
                XWPFTable table = (XWPFTable) bodyElements.get(i);
                TableBlock tableBlock = getTableBlock(table);
                blocksForTransform.add(tableBlock);
            } else {
                throw new RuntimeException();
            }
        }

        return blocksForTransform;
    }

    private void addMetaInfoForRuns(XWPFParagraph paragraph) {
        for (int i = 0; i < paragraph.getRuns().size(); i++) {
            XWPFRun run = paragraph.getRuns().get(i);
            String metaInfoRun = String.format("{MetaInfoRun: numOfRun = %d}", i);
            run.setText(run.text() + metaInfoRun, 0);
        }
    }
}
