package com.sbrf.idrisov.interpritator.entity.paragraph;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.entity.RootBlock;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ParagraphsBlock implements RootBlock {

    @Autowired
    private FreemarkerService freemarkerService;

    private final List<XWPFParagraph> paragraphsToTransform = new ArrayList<>();

    @Override
    public void transform(Map<String, Object> model) {
        String blockText = getBlockTextWithMeta();
        String processedText = freemarkerService.getProcessedText(blockText, model);

        String[] paragraphsText = processedText.split("\\n");

        List<ParagraphForTransform> paragraphForTransformList = getParagraphsForTransform(paragraphsText);

        for (ParagraphForTransform paragraphForTransform : paragraphForTransformList) {
            paragraphForTransform.transform();
        }
    }

    public void addNewElement(XWPFParagraph paragraph) {
        paragraphsToTransform.add(paragraph);
    }

    private List<ParagraphForTransform> getParagraphsForTransform(String[] paragraphsText) {
        Map<Integer, List<String>> paragraphToTexts = parseTextsByParagraphs(paragraphsText);

        return ParagraphForTransform.getParagraphForTransformList(paragraphToTexts, paragraphsToTransform);
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

            sb.append(paragraph.getText());

            sb.append(String.format("{MetaInfoParagraph: numOfParagraph = %d}", i));

            if (i != paragraphsToTransform.size() - 1) {
                sb.append("${'\\n'}");
            }
        }
        return sb.toString();
    }
}