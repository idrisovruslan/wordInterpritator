package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.entity.BodyBlock;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentToBodyBlockConverter {

    @Lookup
    public BodyBlock getBodyBlock() {
        return null;
    }

    public List<BodyBlock> generateBlocksForTransform(XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        List<BodyBlock> blocksForTransform = new ArrayList<>();

        for (int i = 0; i < bodyElements.size(); i++) {
            if (bodyElements.get(i) instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElements.get(i);
                ParagraphUtils.squashRuns(paragraph);

                addMetaInfoForRuns(paragraph);

                if (blocksForTransform.isEmpty() || !blocksForTransform.get(blocksForTransform.size() - 1).isParagraphBlock()) {
                    BodyBlock bodyBlock = getBodyBlock();
                    bodyBlock.addNewParagraph(paragraph);
                    blocksForTransform.add(bodyBlock);
                    continue;
                }

                BodyBlock bodyBlock = blocksForTransform.get(blocksForTransform.size() - 1);
                bodyBlock.addNewParagraph(paragraph);

            } else {
                //TODO РЕАЛИЗУЙ ТАБЛИЦЫ БЛЕАТЬ! а пока пустой блок, чтоб разделить
                BodyBlock bodyBlock = getBodyBlock();
                blocksForTransform.add(bodyBlock);
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
