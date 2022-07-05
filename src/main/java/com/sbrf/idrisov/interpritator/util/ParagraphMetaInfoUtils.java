package com.sbrf.idrisov.interpritator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParagraphMetaInfoUtils {

    public static String createParagraphMetaInfoText(int serialNumberOfParagraph) {
        return String.format("{MetaInfoParagraph: serialNumberOfParagraph = %d}", serialNumberOfParagraph);
    }

    public static boolean containsParagraphMetaInfo(String paragraphText) {
        Pattern pattern = Pattern.compile("\\{MetaInfoParagraph: .*?}$");
        Matcher matcher = pattern.matcher(paragraphText);
        return matcher.find();
    }

    public static String removeParagraphMetaInfo(String paragraphText) {
        Pattern pattern = Pattern.compile("\\{MetaInfoParagraph: .*?}$");
        Matcher matcher = pattern.matcher(paragraphText);
        if (matcher.find()) {
            return matcher.replaceFirst("");
        }
        throw new RuntimeException();
    }

    public static int getSerialNumberOfParagraphFromMeta(String meta) {
        Pattern pattern = Pattern.compile("(?<=\\{MetaInfoParagraph: serialNumberOfParagraph = )(.*?)(?=\\})");
        Matcher matcher = pattern.matcher(meta);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        throw new RuntimeException();
    }

    public static int getSerialNumberOfParagraphFromParagraphText(String paragraphText) {
        return getSerialNumberOfParagraphFromMeta(getParagraphMetaInfo(paragraphText));
    }

    private static String getParagraphMetaInfo(String paragraphText) {
        Pattern pattern = Pattern.compile("\\{MetaInfoParagraph: .*?}$");
        Matcher matcher = pattern.matcher(paragraphText);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new RuntimeException();
    }
}
