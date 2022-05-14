package com.sbrf.idrisov.interpritator.entity;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.ParagraphUtils;
import lombok.Getter;
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
        String blockText = getBlockText();
        String processedText = freemarkerService.getProcessedText(blockText, model);

        String[] paragraphsText = processedText.split("\\n");

        Map<Integer, String> paragraphsTextByNumsAfterFreemarker = getParagraphsTextWithMeta(paragraphsText);

        removeEmptyParagraphAfterTransform(paragraphsTextByNumsAfterFreemarker);

    }

    private Map<Integer, String> getParagraphsTextWithMeta(String[] paragraphsText) {
        Map<Integer, String> paragraphMap = new HashMap<>();

        for (int i = 0; i < paragraphsText.length; i++) {
            Pattern pattern = Pattern.compile("\\{MetaInfo: .*}$");
            Matcher matcher = pattern.matcher(paragraphsText[i]);

            if (matcher.find()) {
                String meta = matcher.group(0);
                String result = matcher.replaceFirst("");

                paragraphMap.put(getNumOfParagraph(meta), result);
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

    private void removeEmptyParagraphAfterTransform(Map<Integer, String> paragraphsTextByNumsAfterFreemarker) {
        List<XWPFParagraph> toRemove = new ArrayList<>();

        //removeEmptyParagraphWithoutEmptyLines(paragraphsText);

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);
            if (!paragraphsTextByNumsAfterFreemarker.containsKey(i) || isEmptyAfterTransform(paragraph, paragraphsTextByNumsAfterFreemarker.get(i))) {
                toRemove.add(paragraph);
            }
        }

        toRemove.forEach(ParagraphUtils::removeParagraphOnDocument);
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

    private String getBlockText() {
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