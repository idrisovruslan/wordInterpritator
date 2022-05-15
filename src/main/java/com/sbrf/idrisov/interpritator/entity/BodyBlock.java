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
public class BodyBlock {

    @Autowired
    private FreemarkerService freemarkerService;

    private final List<XWPFParagraph> paragraphsToTransform = new ArrayList<>();

    @Getter
    private boolean isParagraphBlock = false;


    public void transform(Map<String, Object> model) {
        String blockText = getBlockTextWithMeta();
        String processedText = freemarkerService.getProcessedText(blockText, model);

        String[] paragraphsText = processedText.split("\\n");

        Map<Integer, List<String>> paragraphsTextByNumsAfterFreemarker = getParagraphsTextsByNumOfParagraphAfterTransform(paragraphsText);
        //TODO дальше не работает
        removeEmptyTextParagraphAfterTransform(paragraphsTextByNumsAfterFreemarker);

        System.out.println();
//        transformBlockParagraphsWithNewText(paragraphsTextByNumsAfterFreemarker);
    }

//    private void transformBlockParagraphsWithNewText(Map<Integer, List<String>> paragraphsTextByNumsAfterFreemarker) {
//        for (int i = 0; i < paragraphsToTransform.size(); i++) {
//            if (paragraphsToTransform.get(i) == null) {
//                continue;
//            }
//            XWPFParagraph paragraph = paragraphsToTransform.get(i);
//            List<String> texts = paragraphsTextByNumsAfterFreemarker.get(i);
//
//            for (int j = 0; j < texts.size(); j++) {
//
//                if (j == 0) {
//                    replaceText(paragraph, texts.get(j));
//                }
//
//                //TODO провыерить куда всавит
//                XmlCursor cursor = paragraph.getCTP().newCursor();
//                XWPFParagraph new_par = paragraph.getDocument().insertNewParagraph(cursor);
//                ParagraphUtils.copyPropertiesFromTo(paragraph, new_par);
//                XWPFRun newRun = new_par.createRun();
//                RunUtils.copyPropertiesFromTo(paragraph.getRuns().get(0), newRun);
//                newRun.setText(texts.get(j));
//            }
//        }
//    }

    private Map<Integer, List<String>> getParagraphsTextsByNumOfParagraphAfterTransform(String[] paragraphsText) {
        Map<Integer, List<String>> paragraphMap = new HashMap<>();
        ArrayList<String> tempTexts = new ArrayList<>();

        for (int i = 0; i < paragraphsText.length; i++) {
            //TODO вынестри объект MetaInfoParagraph
            Pattern pattern = Pattern.compile("\\{MetaInfoParagraph: .*}$");
            Matcher matcher = pattern.matcher(paragraphsText[i]);

            if (matcher.find()) {
                String metaInfoParagraph = matcher.group(0);
                String result = matcher.replaceFirst("");

                if (result.isEmpty()) {
                    continue;
                }

                ArrayList<String> texts = new ArrayList<>(tempTexts);
                tempTexts = new ArrayList<>();
                texts.add(result);
                if (paragraphMap.containsKey(getNumOfParagraph(metaInfoParagraph))) {
                    paragraphMap.get(getNumOfParagraph(metaInfoParagraph)).addAll(texts);
                } else {
                    paragraphMap.put(getNumOfParagraph(metaInfoParagraph), texts);
                }
            } else {
                //Если пусто, значит мета инфы нет(это возможно например при дерективе list) тогда мету берем из следующего пункта с текстом
                String result = matcher.replaceFirst("");

                if (result.isEmpty()) {
                    continue;
                }

                tempTexts.add(result);
            }

        }
        return paragraphMap;
    }

    private Integer getNumOfParagraph(String meta) {
        Pattern pattern = Pattern.compile("(?<=\\{MetaInfoParagraph: numOfParagraph = )(.*?)(?=\\})");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        throw new RuntimeException();
    }

    private void removeEmptyTextParagraphAfterTransform(Map<Integer, List<String>> paragraphsTextByNumsAfterFreemarker) {
        List<XWPFParagraph> toRemoveFromDocument = new ArrayList<>();
        Deque<Integer> toRemoveFromThis = new LinkedList<>();

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);
            if (!paragraphsTextByNumsAfterFreemarker.containsKey(i) || isEmptyAfterTransform(paragraph, paragraphsTextByNumsAfterFreemarker.get(i))) {
                toRemoveFromDocument.add(paragraph);
                toRemoveFromThis.addFirst(i);
                paragraphsTextByNumsAfterFreemarker.remove(i);
            }
        }

        //не удаляю чтоб сохранить нумирацию
        toRemoveFromThis.forEach(x -> paragraphsToTransform.set(x, null));
        toRemoveFromDocument.forEach(ParagraphUtils::removeParagraphOnDocument);
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

            sb.append(String.format("{MetaInfoParagraph: numOfParagraph = %d}", i));

            if (i != paragraphsToTransform.size() - 1) {
                sb.append("${'\\n'}");
            }

        }

        return sb.toString();
    }
}