package com.sbrf.idrisov.interpritator.entity.paragraph;

import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sbrf.idrisov.interpritator.entity.paragraph.RunForTransform.getNumOfRunFromText;

@Getter
public class TextByRuns {
    private final Set<RunForTransform> runForTransform;
    private final XWPFParagraph paragraph;

    public TextByRuns(XWPFParagraph paragraph, String paragraphText) {

        Set<RunForTransform> runTexts = new HashSet<>();
        //TODO вынестри объект MetaInfoParagraph
        Pattern pattern = Pattern.compile(".*?\\{MetaInfoRun: .*?}");
        Matcher matcher = pattern.matcher(paragraphText);

        matcher.results().forEach(matchResult -> {
            String textWithRunMeta = matchResult.group();
            int numOfRun = getNumOfRunFromText(textWithRunMeta);

            if (!paragraph.getRuns().isEmpty()) {
                RunForTransform runForTransform = new RunForTransform(paragraph.getRuns().get(numOfRun), textWithRunMeta);

                if (runTexts.contains(runForTransform)) {
                    throw new RuntimeException();
                }

                if (!runForTransform.getText().isEmpty()) {
                    runTexts.add(runForTransform);
                }
            }
        });

        this.paragraph = paragraph;
        this.runForTransform = runTexts;
    }

    public void transform() {
        removeWrongRuns();
        runForTransform.forEach(RunForTransform::transform);
    }

    private void removeWrongRuns() {
        List<XWPFRun> allRuns = paragraph.getRuns();
        Set<Integer> validRunIds = getIdsRun();

        for (int i = allRuns.size() - 1; i >= 0; i--) {
            if (!validRunIds.contains(i)) {
                paragraph.removeRun(i);
            }
        }
    }

    private Set<Integer> getIdsRun() {
        return runForTransform.stream().map(RunForTransform::getId).collect(Collectors.toSet());
    }
}
