package com.sbrf.idrisov.interpritator.converters;

import com.sbrf.idrisov.interpritator.entitys.BodyBlock;
import com.sbrf.idrisov.interpritator.entitys.paragraph.ParagraphsBlock;
import com.sbrf.idrisov.interpritator.entitys.table.TableBlock;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IBodyToBodyBlockConverter {

    @Lookup
    public ParagraphsBlock getParagraphsBlock(List<? extends IBodyElement> paragraphsToTransform) {return null;}

    @Lookup
    public TableBlock getTableBlock(List<? extends IBodyElement> tables) {return null;}

    public List<BodyBlock> generateBodyBlocks(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        return splitParagraphsAndTablesByBlocks(bodyElements);
    }

    public List<BodyBlock> generateBodyBlocks(XWPFTableCell cell) {
        List<IBodyElement> bodyElements = cell.getBodyElements();
        return splitParagraphsAndTablesByBlocks(bodyElements);
    }

    public List<BodyBlock> generateBodyBlocks(XWPFHeaderFooter headerFooter) {
        List<IBodyElement> bodyElements = headerFooter.getBodyElements();
        return splitParagraphsAndTablesByBlocks(bodyElements);
    }

    private List<BodyBlock> splitParagraphsAndTablesByBlocks(List<IBodyElement> bodyElements) {
        List<BodyBlock> blocksForTransform = new ArrayList<>();

        List<IBodyElement> temp = new ArrayList<>();

        for (int i = 0; i < bodyElements.size(); i++) {
            if (bodyElements.get(i) instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElements.get(i);

                if (!temp.isEmpty() && lastElementIsNotParagraph(temp)) {
                    blocksForTransform.add(getTableBlock(temp));
                    temp = new ArrayList<>();
                }

                temp.add(paragraph);

                if (i == bodyElements.size() - 1) {
                    blocksForTransform.add(getParagraphsBlock(temp));
                }

            } else if (bodyElements.get(i) instanceof XWPFTable){
                XWPFTable table = (XWPFTable) bodyElements.get(i);

                if (!temp.isEmpty() && lastElementIsNotTable(temp)) {
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

    private boolean lastElementIsNotTable(List<IBodyElement> temp) {
        return !(temp.get(temp.size() - 1) instanceof XWPFTable);
    }

    private boolean lastElementIsNotParagraph(List<IBodyElement> temp) {
        return !(temp.get(temp.size() - 1) instanceof XWPFParagraph);
    }
}
