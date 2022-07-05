package com.sbrf.idrisov.interpritator.entitys.paragraph;

import com.sbrf.idrisov.interpritator.utils.ParagraphUtils;
import com.sbrf.idrisov.interpritator.utils.RunUtils;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sbrf.idrisov.interpritator.utils.ParagraphUtils.removeRumMetaInfo;

public class ParagraphForTransform {

    private final int id;

    private final XWPFParagraph paragraph;

    private final List<String> processedTexts;

    public ParagraphForTransform(int id, XWPFParagraph paragraph, List<String> processedTexts) {
        this.id = id;
        this.paragraph = paragraph;
        this.processedTexts = processedTexts;
    }

    public void transform() {
        for (TextByRuns textsByRun : getParagraphsTextsFromRunsMeta()) {
            textsByRun.transform();
        }
    }

    public boolean isEmptyAfterTransform() {
        return !removeRumMetaInfo(paragraph.getText()).isEmpty() && (processedTexts.isEmpty() ||
                processedTexts.stream().map(ParagraphUtils::removeRumMetaInfo).allMatch(newText -> newText.equals("")));
    }
    
    private List<TextByRuns> getParagraphsTextsFromRunsMeta() {
        List<TextByRuns> textByRunsList = new ArrayList<>();
        //TODO баг(не верный порядок параграфов из за цикла)
        // если несколько параграфов в цикле(надо объединять текст не по параграфам, а по блокам)
        //сперва последний ибо XmlCursor(с его помощью инсертим копию параграфа) ставит перед вызывающим параграфом
        textByRunsList.add(new TextByRuns(paragraph, processedTexts.get(processedTexts.size() - 1)));

        for (int i = 0; i < processedTexts.size() - 1; i++) {
            //TODO вынестри объект MetaInfoRun
            if (processedTexts.get(i).replaceAll("\\{MetaInfoRun: .*?}", "").isEmpty()) {
                continue;
            }

            XmlCursor cursor = paragraph.getCTP().newCursor();

            XWPFParagraph new_par = paragraph.getDocument().insertNewParagraph(cursor);
            copyPropertiesFromTo(paragraph, new_par);

            List<XWPFRun> runsToCopy = paragraph.getRuns();

            for (XWPFRun runToCopy : runsToCopy) {
                XWPFRun newRun = new_par.createRun();
                RunUtils.copyPropertiesFromTo(runToCopy, newRun);
            }

            textByRunsList.add(new TextByRuns(new_par, processedTexts.get(i)));
        }

        return textByRunsList;
    }

    public void copyPropertiesFromTo(XWPFParagraph paragraph, XWPFParagraph new_par) {
        CTPPr pPr = new_par.getCTP().isSetPPr() ? new_par.getCTP().getPPr() : new_par.getCTP().addNewPPr();
        pPr.set(paragraph.getCTP().getPPr());
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
