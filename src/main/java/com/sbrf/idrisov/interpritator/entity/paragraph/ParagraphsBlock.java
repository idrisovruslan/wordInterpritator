package com.sbrf.idrisov.interpritator.entity.paragraph;

import com.sbrf.idrisov.interpritator.entity.BodyBlock;
import com.sbrf.idrisov.interpritator.service.FreemarkerService;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.sbrf.idrisov.interpritator.util.ParagraphMetaInfoUtils.*;
import static com.sbrf.idrisov.interpritator.util.ParagraphUtils.removeParagraphOnDocument;
import static com.sbrf.idrisov.interpritator.util.RunUtils.isEquals;

@Component
@Scope("prototype")
public class ParagraphsBlock implements BodyBlock {

    @Autowired
    private FreemarkerService freemarkerService;

    private final List<XWPFParagraph> paragraphsToTransform;

    public ParagraphsBlock(List<XWPFParagraph> paragraphsToTransform) {
        this.paragraphsToTransform = paragraphsToTransform;
    }

    @Override
    public void transformBlock(Map<String, Object> model) {
        String blockText = getBlockTextWithMeta();
        String processedText = freemarkerService.getProcessedText(blockText, model);

        String[] processedTextByParagraphs = processedText.split("\\n");

        List<ParagraphForTransform> paragraphForTransformList = getParagraphsForTransform(processedTextByParagraphs);

        for (ParagraphForTransform paragraphForTransform : paragraphForTransformList) {
            paragraphForTransform.transform();
        }
    }
    //TODO баг если несколько параграфов в цикле (надо объединять текст не по параграфам, а по блокам)
    private List<ParagraphForTransform> getParagraphsForTransform(String[] paragraphsText) {
        List<ParagraphForTransform> result = new ArrayList<>();

        Map<Integer, List<String>> paragraphToTextsMap = parseTextsByParagraphs(paragraphsText);

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);

            ParagraphForTransform paragraphForTransform = new ParagraphForTransform(i, paragraph, paragraphToTextsMap.get(i));

            if (!paragraphToTextsMap.containsKey(i) || paragraphForTransform.isEmptyAfterTransform()) {
                removeParagraphOnDocument(paragraph);
                continue;
            }

            result.add(paragraphForTransform);
        }

        return result;
    }

    private Map<Integer, List<String>> parseTextsByParagraphs(String[] paragraphsTexts) {
        Map<Integer, List<String>> paragraphToTextsMap = new HashMap<>();
        ArrayList<String> tempTexts = new ArrayList<>();

        for (int i = 0; i < paragraphsTexts.length; i++) {
            String paragraphText = paragraphsTexts[i];

            if (containsParagraphMetaInfo(paragraphText)) {
                String processedText = removeParagraphMetaInfo(paragraphText);
                int serialNumberOfParagraph = getSerialNumberOfParagraphFromParagraphText(paragraphText);

                ArrayList<String> texts;
                if (tempTexts.isEmpty()) {
                    texts = new ArrayList<>();
                    texts.add(processedText);
                } else {
                    //TODO баг(не text + processedText, а text + runMetaInfo from processedText)
                    texts = tempTexts.stream().map(text -> text + processedText).collect(Collectors.toCollection(ArrayList::new));
                    tempTexts = new ArrayList<>();
                    texts.add(processedText);
                }

                if (paragraphToTextsMap.containsKey(serialNumberOfParagraph)) {
                    paragraphToTextsMap.get(serialNumberOfParagraph).addAll(texts);
                } else {
                    paragraphToTextsMap.put(serialNumberOfParagraph, texts);
                }
            } else {
                //Если пусто, значит мета инфы нет(это возможно если в моделе есть переводы строки у параметра)
                // тогда мету берем из следующего пункта с текстом
                tempTexts.add(paragraphsTexts[i]);
            }

        }
        return paragraphToTextsMap;
    }

    private String getBlockTextWithMeta() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);

            squashRuns(paragraph);
            addMetaInfoForRuns(paragraph);

            sb.append(paragraph.getText());

            sb.append(createParagraphMetaInfoText(i));

            if (i != paragraphsToTransform.size() - 1) {
                sb.append("${'\\n'}");
            }
        }
        return sb.toString();
    }

    public void squashRuns(XWPFParagraph paragraph) {
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