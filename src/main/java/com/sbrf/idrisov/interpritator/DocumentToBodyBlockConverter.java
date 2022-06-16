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
    public ParagraphsBlock getParagraphsBlock(List<? extends IBodyElement> paragraphsToTransform) {return null;}

    @Lookup
    public TableBlock getTableBlock(List<? extends IBodyElement> tables) {return null;}

    public List<RootBlock> generateBlocksForTransform(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        return generateBlocksForTransform(bodyElements);
    }

    public List<RootBlock> generateBlocksForTransform(XWPFTableCell cell) {
        List<IBodyElement> bodyElements = cell.getBodyElements();
        return generateBlocksForTransform(bodyElements);
    }

    public List<RootBlock> generateBlocksForTransform(XWPFHeaderFooter headerFooter) {
        List<IBodyElement> bodyElements = headerFooter.getBodyElements();
        return generateBlocksForTransform(bodyElements);
    }

    private List<RootBlock> generateBlocksForTransform(List<IBodyElement> bodyElements) {
        List<RootBlock> blocksForTransform = new ArrayList<>();

        List<IBodyElement> temp = new ArrayList<>();

        for (int i = 0; i < bodyElements.size(); i++) {
            if (bodyElements.get(i) instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElements.get(i);

                if (!temp.isEmpty() && (!(temp.get(temp.size() - 1) instanceof XWPFParagraph))) {
                    blocksForTransform.add(getTableBlock(temp));
                    temp = new ArrayList<>();
                }

                temp.add(paragraph);

                if (i == bodyElements.size() - 1) {
                    blocksForTransform.add(getParagraphsBlock(temp));
                }

            } else if (bodyElements.get(i) instanceof XWPFTable){
                XWPFTable table = (XWPFTable) bodyElements.get(i);

                if (!temp.isEmpty() && (!(temp.get(temp.size() - 1) instanceof XWPFTable))) {
                    blocksForTransform.add(getParagraphsBlock(temp));
                    temp = new ArrayList<>();
                }

                temp.add(table);

                if (i == bodyElements.size() - 1) {
                    blocksForTransform.add(getTableBlock(temp));
                }
            } else if (!(bodyElements.get(i) instanceof XWPFSDT)){
                throw new RuntimeException();
            }
        }

        return blocksForTransform;
    }
}
