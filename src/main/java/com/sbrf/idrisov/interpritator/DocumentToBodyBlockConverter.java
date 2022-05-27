package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.RootBlock;
import com.sbrf.idrisov.interpritator.entity.paragraph.ParagraphsBlock;
import com.sbrf.idrisov.interpritator.entity.table.TableForTransform;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static com.sbrf.idrisov.interpritator.RunUtils.isEquals;

@Service
public class DocumentToBodyBlockConverter {

    @Lookup
    public ParagraphsBlock getParagraphsBlock() {return null;}

    @Lookup
    public TableForTransform getTableBlock(XWPFTable table) {return null;}

    public List<RootBlock> generateBlocksForTransform(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        return generateBlocksForTransform(bodyElements);
    }

    public List<RootBlock> generateBlocksForTransform(XWPFTableCell cell) {
        List<IBodyElement> bodyElements = cell.getBodyElements();
        return generateBlocksForTransform(bodyElements);
    }

    private List<RootBlock> generateBlocksForTransform(List<IBodyElement> bodyElements) {
        List<RootBlock> blocksForTransform = new ArrayList<>();

        for (int i = 0; i < bodyElements.size(); i++) {
            if (bodyElements.get(i) instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElements.get(i);
                squashRuns(paragraph);

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
                TableForTransform tableForTransform = getTableBlock(table);
                blocksForTransform.add(tableForTransform);
            } else {
                throw new RuntimeException();
            }
        }

        return blocksForTransform;
    }

    public static void squashRuns(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        Deque<Integer> runsToRemove = new LinkedList<>();

        for (int i = runs.size() - 2; i >= 0; i--) {
            if (isEquals(runs.get(i), runs.get(i + 1))) {
                runs.get(i).setText(runs.get(i).text() + runs.get(i + 1).text(), 0);
                runsToRemove.add(i + 1);
            }
        }
        runsToRemove.forEach(paragraph::removeRun);
    }

    private void addMetaInfoForRuns(XWPFParagraph paragraph) {
        for (int i = 0; i < paragraph.getRuns().size(); i++) {
            XWPFRun run = paragraph.getRuns().get(i);
            String metaInfoRun = String.format("{MetaInfoRun: numOfRun = %d}", i);
            run.setText(run.text() + metaInfoRun, 0);
        }
    }
}
