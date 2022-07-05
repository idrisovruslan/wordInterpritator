package com.sbrf.idrisov.interpritator.entitys.paragraph;

import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sbrf.idrisov.interpritator.utils.ParagraphUtils.removeRumMetaInfo;

@Getter
public class RunForTransform {
    private final int id;
    private final String text;
    private final XWPFRun oldRun;

    public RunForTransform(XWPFRun oldRun, String textWithMeta) {
        this.id = getNumOfRunFromText(textWithMeta);
        this.text = removeRumMetaInfo(textWithMeta);
        this.oldRun = oldRun;
    }

    public static Integer getNumOfRunFromText(String textWithMeta) {
        //TODO вынестри объект MetaInfoRun
        Pattern runPattern = Pattern.compile("\\{MetaInfoRun: .*?}$");
        Matcher runMatcher = runPattern.matcher(textWithMeta);

        if (runMatcher.find()) {
            return getNumOfRunFromMeta(runMatcher.group());
        } else {
            throw new RuntimeException();
        }
    }

    public static Integer getNumOfRunFromMeta(String meta) {
        //TODO вынестри объект MetaInfoRun
        Pattern pattern = Pattern.compile("(?<=\\{MetaInfoRun: numOfRun = )(.*?)(?=\\})");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        throw new RuntimeException();
    }

    public void transform() {
        oldRun.setTextHighlightColor(STHighlightColor.NONE.toString());
        oldRun.setText(text, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunForTransform that = (RunForTransform) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
