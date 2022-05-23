package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static com.sbrf.idrisov.interpritator.RunUtils.isEquals;

//TODO проксю запели или что то типа того
public class ParagraphUtils {
    private ParagraphUtils() {
    }

    //TODO не работает
    public static void copyPropertiesFromTo(XWPFParagraph paragraph, XWPFParagraph new_par) {
        CTPPr pPr = new_par.getCTP().isSetPPr() ? new_par.getCTP().getPPr() : new_par.getCTP().addNewPPr();
        pPr.set(paragraph.getCTP().getPPr());
    }

    public static void squashRuns(XWPFParagraph paragraph) {
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

    public static boolean isEmptyAfterTransform(XWPFParagraph paragraph, List<String> newTexts) {
        return !removeRumMetaInfo(paragraph.getText()).isEmpty() && (newTexts.isEmpty() || newTexts.stream().map(ParagraphUtils::removeRumMetaInfo).allMatch(newText -> newText.equals("")));
    }

    public static String removeRumMetaInfo(String paragraphText) {
        return paragraphText.replaceAll("\\{MetaInfoRun: .*?}", "");
    }

    public static void removeParagraphOnDocument(XWPFParagraph paragraph) {
        XWPFDocument document = paragraph.getDocument();
        document.removeBodyElement(document.getPosOfParagraph(paragraph));
    }

    //TODO это копипаста с стаковерфлоу(изучить как работает)
    public static void replaceText(XWPFParagraph paragraph, String textToFind, String replacement) {
        TextSegment foundTextSegment = null;
        PositionInParagraph startPos = new PositionInParagraph(0, 0, 0);
        while((foundTextSegment = searchText(paragraph, textToFind, startPos)) != null) { // search all text segments having text to find

            // maybe there is text before textToFind in begin run
            XWPFRun beginRun = paragraph.getRuns().get(foundTextSegment.getBeginRun());
            String textInBeginRun = beginRun.getText(foundTextSegment.getBeginText());
            String textBefore = textInBeginRun.substring(0, foundTextSegment.getBeginChar()); // we only need the text before

            // maybe there is text after textToFind in end run
            XWPFRun endRun = paragraph.getRuns().get(foundTextSegment.getEndRun());
            String textInEndRun = endRun.getText(foundTextSegment.getEndText());
            String textAfter = textInEndRun.substring(foundTextSegment.getEndChar() + 1); // we only need the text after

            if (foundTextSegment.getEndRun() == foundTextSegment.getBeginRun()) {
                textInBeginRun = textBefore + replacement + textAfter; // if we have only one run, we need the text before, then the replacement, then the text after in that run
            } else {
                textInBeginRun = textBefore + replacement; // else we need the text before followed by the replacement in begin run
                endRun.setText(textAfter, foundTextSegment.getEndText()); // and the text after in end run
            }

            beginRun.setText(textInBeginRun, foundTextSegment.getBeginText());
            beginRun.setTextHighlightColor("none");

            // runs between begin run and end run needs to be removed
            for (int runBetween = foundTextSegment.getEndRun() - 1; runBetween > foundTextSegment.getBeginRun(); runBetween--) {
                paragraph.removeRun(runBetween); // remove not needed runs
            }

        }
    }

    //rewrote default method
    //  https://stackoverflow.com/questions/65275097/apache-poi-my-placeholder-is-treated-as-three-different-runs/65289246#65289246
    public static TextSegment searchText(XWPFParagraph paragraph, String searched, PositionInParagraph startPos) {
        int startRun = startPos.getRun(),
            startText = startPos.getText(),
            startChar = startPos.getChar();
        int beginRunPos = 0, candCharPos = 0;
        boolean newList = false;

        java.util.List<XWPFRun> runs = paragraph.getRuns();

        int beginTextPos = 0, beginCharPos = 0;

        for (int runPos = startRun; runPos < runs.size(); runPos++) {
            int textPos = 0, charPos;
            CTR ctRun = runs.get(runPos).getCTR();
            XmlCursor c = ctRun.newCursor();
            c.selectPath("./*");
            try {
                while (c.toNextSelection()) {
                    XmlObject o = c.getObject();
                    if (o instanceof CTText) {
                        if (textPos >= startText) {
                            String candidate = ((CTText) o).getStringValue();
                            if (runPos == startRun) {
                                charPos = startChar;
                            } else {
                                charPos = 0;
                            }

                            for (; charPos < candidate.length(); charPos++) {
                                if ((candidate.charAt(charPos) == searched.charAt(0)) && (candCharPos == 0)) {
                                    beginTextPos = textPos;
                                    beginCharPos = charPos;
                                    beginRunPos = runPos;
                                    newList = true;
                                }
                                if (candidate.charAt(charPos) == searched.charAt(candCharPos)) {
                                    if (candCharPos + 1 < searched.length()) {
                                        candCharPos++;
                                    } else if (newList) {
                                        TextSegment segment = new TextSegment();
                                        segment.setBeginRun(beginRunPos);
                                        segment.setBeginText(beginTextPos);
                                        segment.setBeginChar(beginCharPos);
                                        segment.setEndRun(runPos);
                                        segment.setEndText(textPos);
                                        segment.setEndChar(charPos);
                                        return segment;
                                    }
                                } else {
                                    candCharPos = 0;
                                }
                            }
                        }
                        textPos++;
                    } else if (o instanceof CTProofErr) {
                        c.removeXml();
                    } else if (o instanceof CTRPr) {
                        //do nothing
                    } else {
                        candCharPos = 0;
                    }
                }
            } finally {
                c.dispose();
            }
        }
        return null;
    }
}
