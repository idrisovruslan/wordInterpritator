package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.TransformBlock;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentToTransformBlockConverter {

    @Lookup
    public TransformBlock getTransformBlock() {
        return null;
    }

    public List<TransformBlock> generateBlocksForTransform(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        List<TransformBlock> blocksForTransform = new ArrayList<>();

        for (int i = 0; i < bodyElements.size(); i++) {
            if (bodyElements.get(i) instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElements.get(i);

                if (blocksForTransform.isEmpty() || !blocksForTransform.get(blocksForTransform.size() - 1).isParagraphBlock()) {
                    TransformBlock transformBlock = getTransformBlock();
                    transformBlock.addNewParagraph(paragraph);
                    blocksForTransform.add(transformBlock);
                    continue;
                }

                TransformBlock transformBlock = blocksForTransform.get(blocksForTransform.size() - 1);
                transformBlock.addNewParagraph(paragraph);

            } else {
                //TODO РЕАЛИЗУЙ ТАБЛИЦЫ БЛЕАТЬ!
                TransformBlock transformBlock = getTransformBlock();
                blocksForTransform.add(transformBlock);
            }
        }

        return blocksForTransform;
    }
}
