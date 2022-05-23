package com.sbrf.idrisov.interpritator.entity;

import com.sbrf.idrisov.interpritator.FreemarkerService;
import com.sbrf.idrisov.interpritator.ParagraphUtils;
import com.sbrf.idrisov.interpritator.RunUtils;
import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sbrf.idrisov.interpritator.ParagraphUtils.isEmptyAfterTransform;
import static com.sbrf.idrisov.interpritator.ParagraphUtils.removeRumMetaInfo;

@Component
@Scope("prototype")
public class BodyBlock {

    @Autowired
    private FreemarkerService freemarkerService;

    private final List<XWPFParagraph> paragraphsToTransform = new ArrayList<>();

    private final Map<Integer, XWPFParagraph> paragraphsToTransformMap = new HashMap<>();

    @Getter
    private boolean isParagraphBlock = false;


    public void transform(Map<String, Object> model) {

        fillParagraphsToTransformMap();

        String blockText = getBlockTextWithMeta();
        String processedText = freemarkerService.getProcessedText(blockText, model);

        String[] paragraphsText = processedText.split("\\n");

        Map<Integer, List<String>> paragraphTextsByNumsAfterFreemarker = getParagraphsTextsByNumOfParagraphAfterTransform(paragraphsText);
        //TODO дальше не работает
        removeEmptyTextParagraphAfterTransform(paragraphTextsByNumsAfterFreemarker);

        Map<Integer, List<Map<Integer, String>>> paragraphsTextsWithRunsMeta = getParagraphsTextsFromRunsMeta(paragraphTextsByNumsAfterFreemarker);

        rewriteParagraphs(paragraphsTextsWithRunsMeta);
    }

    private void fillParagraphsToTransformMap() {
        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            paragraphsToTransformMap.put(i, paragraphsToTransform.get(i));
        }
    }

    private void rewriteParagraphs(Map<Integer, List<Map<Integer, String>>> paragraphsTextsWithRunsMeta) {
        for (Map.Entry<Integer, List<Map<Integer, String>>> paragraph : paragraphsTextsWithRunsMeta.entrySet()) {
            int paragraphNum = paragraph.getKey();
            List<Map<Integer, String>> paragraphRuns = paragraph.getValue();

            rewriteParagraph(paragraphsToTransformMap.get(paragraphNum), paragraphRuns);
        }
    }

    private void rewriteParagraph(XWPFParagraph paragraph, List<Map<Integer, String>> newParagraphsRuns) {
        List<XWPFRun> runs = paragraph.getRuns();
        Deque<Integer> runsToRemove = new LinkedList<>();

        Map<Integer, String> newParagraphRuns = newParagraphsRuns.get(newParagraphsRuns.size() - 1);

        //TODO нахадить id не перебором а пересечением множеств
        for (int i = 0; i < runs.size(); i++) {
            if (newParagraphRuns.containsKey(i)) {
                runs.get(i).setText(newParagraphRuns.get(i), 0);
            } else {
                runsToRemove.addFirst(i);
            }
        }

        for (int i = 0; i < newParagraphsRuns.size() - 1; i++) {
            //TODO провыерить куда всавит
            XmlCursor cursor = paragraph.getCTP().newCursor();



            XWPFParagraph new_par = paragraph.getDocument().insertNewParagraph(cursor);
            ParagraphUtils.copyPropertiesFromTo(paragraph, new_par);

            Map<Integer, String> runsToCopy = newParagraphsRuns.get(i);

            for (Map.Entry<Integer, String> runToCopy : runsToCopy.entrySet()) {
                Integer runId = runToCopy.getKey();
                String runText = runToCopy.getValue();

                XWPFRun newRun = new_par.createRun();
                RunUtils.copyPropertiesFromTo(paragraph.getRuns().get(runId), newRun);
                newRun.setText(runText);
            }
        }

        runsToRemove.forEach(paragraph::removeRun);
    }

    private Map<Integer, List<Map<Integer, String>>> getParagraphsTextsFromRunsMeta(Map<Integer, List<String>> paragraphsTextByNumsAfterFreemarker) {
        Map<Integer, List<Map<Integer, String>>> result = new HashMap<>();

        for (Map.Entry<Integer, List<String>> paragraph : paragraphsTextByNumsAfterFreemarker.entrySet()) {

            int key = paragraph.getKey();
            List<String> paragraphTexts = paragraph.getValue();

            List<Map<Integer, String>> paragraphRunsList = new ArrayList<>();

            for (String paragraphText : paragraphTexts) {
                Map<Integer, String> runTexts = new HashMap<>();

                Pattern pattern = Pattern.compile(".*?\\{MetaInfoRun: .*?}");
                Matcher matcher = pattern.matcher(paragraphText);

                matcher.results().forEach(matchResult -> {
                    String runTextWithMeta = matchResult.group();

                    Pattern runPattern = Pattern.compile("\\{MetaInfoRun: .*?}$");
                    Matcher runMatcher = runPattern.matcher(runTextWithMeta);

                    if (runMatcher.find()) {
                        int runNum = getNumOfRun(runMatcher.group());

                        if (runTexts.containsKey(runNum)) {
                            throw new RuntimeException();
                        }

                        if (!removeRumMetaInfo(runTextWithMeta).isEmpty()) {
                            runTexts.put(runNum, removeRumMetaInfo(runTextWithMeta));
                        }
                    } else {
                        throw new RuntimeException();
                    }
                });
                if (!runTexts.isEmpty()) {
                    paragraphRunsList.add(runTexts);
                }
            }
            result.put(key, paragraphRunsList);
        }
        return result;
    }

    private Map<Integer, List<String>> getParagraphsTextsByNumOfParagraphAfterTransform(String[] paragraphsText) {
        Map<Integer, List<String>> paragraphMap = new HashMap<>();
        ArrayList<String> tempTexts = new ArrayList<>();

        for (int i = 0; i < paragraphsText.length; i++) {
            //TODO вынестри объект MetaInfoParagraph
            Pattern pattern = Pattern.compile("\\{MetaInfoParagraph: .*?}$");
            Matcher matcher = pattern.matcher(paragraphsText[i]);

            if (matcher.find()) {
                String metaInfoParagraph = matcher.group(0);
                String result = matcher.replaceFirst("");

                if (result.isEmpty()) {
                    continue;
                }

                ArrayList<String> texts;
                if (tempTexts.isEmpty()) {
                    texts = new ArrayList<>();
                    texts.add(result);
                } else {
                    texts = tempTexts.stream().map(text -> text + result).collect(Collectors.toCollection(ArrayList::new));
                    tempTexts = new ArrayList<>();
                    texts.add(result);
                }

                if (paragraphMap.containsKey(getNumOfParagraph(metaInfoParagraph))) {
                    paragraphMap.get(getNumOfParagraph(metaInfoParagraph)).addAll(texts);
                } else {
                    paragraphMap.put(getNumOfParagraph(metaInfoParagraph), texts);
                }
            } else {
                //Если пусто, значит мета инфы нет(это возможно например при дерективе list) тогда мету берем из следующего пункта с текстом
                tempTexts.add(paragraphsText[i]);
            }

        }
        return paragraphMap;
    }

    private Integer getNumOfRun(String meta) {
        Pattern pattern = Pattern.compile("(?<=\\{MetaInfoRun: numOfRun = )(.*?)(?=\\})");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        throw new RuntimeException();
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

        for (Map.Entry<Integer, XWPFParagraph> paragraphEntry : paragraphsToTransformMap.entrySet()) {
            int paragraphNum = paragraphEntry.getKey();
            XWPFParagraph paragraph = paragraphEntry.getValue();

            if (isEmptyAfterTransform(paragraph, paragraphsTextByNumsAfterFreemarker.get(paragraphNum))) {
                toRemoveFromDocument.add(paragraph);
                toRemoveFromThis.addFirst(paragraphNum);
                paragraphsTextByNumsAfterFreemarker.remove(paragraphNum);
            }
        }

        //не удаляю чтоб сохранить нумирацию
        toRemoveFromThis.forEach(paragraphsToTransformMap::remove);
        toRemoveFromDocument.forEach(ParagraphUtils::removeParagraphOnDocument);
    }

    public void addNewParagraph(XWPFParagraph paragraph) {
        paragraphsToTransform.add(paragraph);
        isParagraphBlock = true;
    }

    private String getBlockTextWithMeta() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Integer, XWPFParagraph> paragraphEntry : paragraphsToTransformMap.entrySet()) {
            int paragraphNum = paragraphEntry.getKey();
            XWPFParagraph paragraph = paragraphEntry.getValue();

            sb.append(paragraph.getText());

            sb.append(String.format("{MetaInfoParagraph: numOfParagraph = %d}", paragraphNum));

            if (paragraphNum != paragraphsToTransformMap.size() - 1) {
                sb.append("${'\\n'}");
            }

        }

        return sb.toString();
    }
}