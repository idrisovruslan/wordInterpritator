package com.sbrf.idrisov.interpritator.entity.paragraph;

import com.sbrf.idrisov.interpritator.ParagraphUtils;
import com.sbrf.idrisov.interpritator.RunUtils;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sbrf.idrisov.interpritator.ParagraphUtils.isEmptyAfterTransform;

public class ParagraphForTransform {

    private final int id;

    private final XWPFParagraph paragraph;

    private final List<TextByRuns> textsByRuns;

    private ParagraphForTransform(int id, XWPFParagraph paragraph, List<TextByRuns> textsByRuns) {
        this.id = id;
        this.paragraph = paragraph;
        this.textsByRuns = textsByRuns;
    }

    public static List<ParagraphForTransform> getParagraphForTransformList(Map<Integer, List<String>> paragraphToTextsMap, List<XWPFParagraph> paragraphsToTransform) {
        List<ParagraphForTransform> result = new ArrayList<>();

        if (paragraphToTextsMap.size() != paragraphsToTransform.size()) {
            throw new RuntimeException();
        }

        for (int i = 0; i < paragraphsToTransform.size(); i++) {
            XWPFParagraph paragraph = paragraphsToTransform.get(i);

            if (isEmptyAfterTransform(paragraph, paragraphToTextsMap.get(i))) {
                ParagraphUtils.removeParagraphOnDocument(paragraph);
                continue;
            }

            result.add(new ParagraphForTransform(i, paragraph, getParagraphsTextsFromRunsMeta(paragraph, paragraphToTextsMap.get(i))));
        }

        return result;
    }

    private static List<TextByRuns> getParagraphsTextsFromRunsMeta(XWPFParagraph paragraph, List<String> texts) {
        List<TextByRuns> textByRunsList = new ArrayList<>();

        //сперва последний ибо XmlCursor(с его помощью инсертим копию параграфа) ставит перед вызывающим параграфом
        textByRunsList.add(new TextByRuns(paragraph, texts.get(texts.size() - 1)));

        for (int i = 0; i < texts.size() - 1; i++) {
            //TODO вынестри объект MetaInfoParagraph
            if (texts.get(i).replaceAll("\\{MetaInfoRun: .*?}$", "").isEmpty()) {
                continue;
            }

            XmlCursor cursor = paragraph.getCTP().newCursor();

            XWPFParagraph new_par = paragraph.getDocument().insertNewParagraph(cursor);
            ParagraphUtils.copyPropertiesFromTo(paragraph, new_par);

            List<XWPFRun> runsToCopy = paragraph.getRuns();

            for (XWPFRun runToCopy : runsToCopy) {
                XWPFRun newRun = new_par.createRun();
                RunUtils.copyPropertiesFromTo(runToCopy, newRun);
            }

            textByRunsList.add(new TextByRuns(new_par, texts.get(i)));
        }

        return textByRunsList;
    }

    public void transform() {
        for (TextByRuns textsByRun : textsByRuns) {
            textsByRun.transform();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParagraphForTransform that = (ParagraphForTransform) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
