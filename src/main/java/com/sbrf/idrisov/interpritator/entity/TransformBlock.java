package com.sbrf.idrisov.interpritator.entity;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.ParagraphUtils;
import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sbrf.idrisov.interpritator.ParagraphUtils.isEmptyAfterTransform;

@Component
@Scope("prototype")
public class TransformBlock {

    @Autowired
    private FreemarkerService freemarkerService;

    private final List<XWPFParagraph> paragraphsToTransform = new ArrayList<>();

    @Getter
    private boolean isParagraphBlock = false;


    public void transform(Map<String, Object> model) {
        String blockText = getBlockTextWithMeta();
        String processedText = freemarkerService.getProcessedText(blockText, model);

        String[] paragraphsText = processedText.split("\\n");

        Map<Integer, List<String>> paragraphsTextByNumsAfterFreemarker = getParagraphsTextWithMeta(paragraphsText);
//TODO дальше не работает
        removeEmptyParagraphAfterTransform(paragraphsTextByNumsAfterFreemarker);

    }

    private Map<Integer, List<String>> getParagraphsTextWithMeta(String[] paragraphsText) {
        Map<Integer, List<String>> paragraphMap = new HashMap<>();
        ArrayList<String> tempTexts = new ArrayList<>();

        for (int i = 0; i < paragraphsText.length; i++) {
            Pattern pattern = Pattern.compile("\\{MetaInfo: .*}$");
            Matcher matcher = pattern.matcher(paragraphsText[i]);

            if (matcher.find()) {
                String meta = matcher.group(0);
                String result = matcher.replaceFirst("");

                ArrayList<String> texts = new ArrayList<>(tempTexts);
                tempTexts = new ArrayList<>();
                texts.add(result);
                if (paragraphMap.containsKey(getNumOfParagraph(meta))) {
                    paragraphMap.get(getNumOfParagraph(meta)).addAll(texts);
                } else {
                    paragraphMap.put(getNumOfParagraph(meta), texts);
                }
            } else {
                String result = matcher.replaceFirst("");
                tempTexts.add(result);
            }

        }
        return paragraphMap;
    }

    private Integer getNumOfParagraph(String meta) {
        Pattern pattern = Pattern.compile("(?<=\\{MetaInfo: num = )(.*?)(?=\\})");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        throw new RuntimeException();
    }

    private void removeEmptyParagraphAfterTransform(Map<Integer, List<String>> paragraphsTextByNumsAfterFreemarker) {
        List<XWPFParagraph> toRemoveFromDocument = new ArrayList<>();
        Deque<Integer> toRemoveFromThis = new LinkedList<>();
        //removeEmptyParagraphWithoutEmptyLines(paragraphsText);

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);
            if (!paragraphsTextByNumsAfterFreemarker.containsKey(i) || isEmptyAfterTransform(paragraph, paragraphsTextByNumsAfterFreemarker.get(i))) {
                toRemoveFromDocument.add(paragraph);
                toRemoveFromThis.addFirst(i);
            }
        }

        toRemoveFromThis.forEach(x -> paragraphsToTransform.remove((int)x));
        toRemoveFromDocument.forEach(ParagraphUtils::removeParagraphOnDocument);
    }

    private void removeEmptyParagraphWithoutEmptyLines(String[] paragraphsText) {
        boolean remove = false;
        List<XWPFParagraph> toRemove = new ArrayList<>();

        for (int i = paragraphsToTransform.size() - 2; i > paragraphsText.length; i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);

            if (paragraph.getText().isEmpty() && !remove) {
                continue;
            }

            remove = true;
            toRemove.add(paragraph);
        }
        toRemove.forEach(ParagraphUtils::removeParagraphOnDocument);
    }


    public void addNewParagraph(XWPFParagraph paragraph) {
        paragraphsToTransform.add(paragraph);
        isParagraphBlock = true;
    }

    private String getBlockTextWithMeta() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);
            sb.append(paragraph.getText());

            sb.append(String.format("{MetaInfo: num = %d}", i));

            if (i != paragraphsToTransform.size() - 1) {
                sb.append("${'\\n'}");
            }

        }

        return sb.toString();
    }
}