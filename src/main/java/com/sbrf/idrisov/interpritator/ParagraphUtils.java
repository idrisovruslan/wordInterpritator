package com.sbrf.idrisov.interpritator;

import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;

import java.util.List;

//TODO проксю запели или что то типа того
public class ParagraphUtils {
    private ParagraphUtils() {
    }

    public static String removeRumMetaInfo(String paragraphText) {
        return paragraphText.replaceAll("\\{MetaInfoRun: .*?}", "");
    }

    public static int getPosOfBodyElement(IBodyElement needle, List<? extends IBodyElement> bodyElements) {
        BodyElementType type = needle.getElementType();
        IBodyElement current;
        for (int i = 0; i < bodyElements.size(); i++) {
            current = bodyElements.get(i);
            if (current.getElementType() == type) {
                if (current.equals(needle)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
