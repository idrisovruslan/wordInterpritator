package com.sbrf.idrisov.interpritator.entity.paragraph;

import com.sbrf.idrisov.interpritator.entity.BodyBlock;
import com.sbrf.idrisov.interpritator.service.FreemarkerService;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private Map<Integer, List<String>> parseTextsByParagraphs(String[] paragraphsText) {
        Map<Integer, List<String>> paragraphToTextsMap = new HashMap<>();
        ArrayList<String> tempTexts = new ArrayList<>();

        for (int i = 0; i < paragraphsText.length; i++) {
            //TODO вынестри объект MetaInfoParagraph
            Pattern pattern = Pattern.compile("\\{MetaInfoParagraph: .*?}$");
            Matcher matcher = pattern.matcher(paragraphsText[i]);

            if (matcher.find()) {
                String metaInfoParagraph = matcher.group(0);
                String result = matcher.replaceFirst("");

                ArrayList<String> texts;
                if (tempTexts.isEmpty()) {
                    texts = new ArrayList<>();
                    texts.add(result);
                } else {
                    texts = tempTexts.stream().map(text -> text + result).collect(Collectors.toCollection(ArrayList::new));
                    tempTexts = new ArrayList<>();
                    texts.add(result);
                }

                if (paragraphToTextsMap.containsKey(getNumOfParagraph(metaInfoParagraph))) {
                    paragraphToTextsMap.get(getNumOfParagraph(metaInfoParagraph)).addAll(texts);
                } else {
                    paragraphToTextsMap.put(getNumOfParagraph(metaInfoParagraph), texts);
                }
            } else {
                //Если пусто, значит мета инфы нет(это возможно например при дерективе list) тогда мету берем из следующего пункта с текстом
                tempTexts.add(paragraphsText[i]);
            }

        }
        return paragraphToTextsMap;
    }

    private Integer getNumOfParagraph(String meta) {
        //TODO вынестри объект MetaInfoParagraph
        Pattern pattern = Pattern.compile("(?<=\\{MetaInfoParagraph: numOfParagraph = )(.*?)(?=\\})");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        throw new RuntimeException();
    }

    private String getBlockTextWithMeta() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);

            squashRuns(paragraph);
            addMetaInfoForRuns(paragraph);

            sb.append(paragraph.getText());

            sb.append(String.format("{MetaInfoParagraph: numOfParagraph = %d}", i));

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