package com.sbrf.idrisov.interpritator;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.*;

@Service
public class FreemarkerService {

    public void transformDocument(Map<String, Object> model, XWPFDocument document) {
        List<IBodyElement> bodyElements = document.getBodyElements();

        for (int i = 0; i < bodyElements.size(); i++) {
            if (bodyElements.get(i) instanceof XWPFParagraph) {
                transformParagraph(model, (XWPFParagraph) bodyElements.get(i));
            } else {
                //TODO РЕАЛИЗУЙ ТАБЛИЦЫ БЛЕАТЬ!
            }
        }
    }

    private void transformParagraph(Map<String, Object> model, XWPFParagraph paragraph) {
        String resultText = getProcessedText(paragraph.getText(), model);
        StringBuilder sb = new StringBuilder(resultText);
        List<XWPFRun> runs = paragraph.getRuns();
        Deque<Integer> toRemove = new LinkedList<>();

        runs.get(0).getColor();
        runs.get(0).getTextHightlightColor();

        for (int i = 0; i < runs.size(); i++) {
            if (sb.indexOf(runs.get(i).text()) == 0) {
                sb.delete(0, runs.get(i).text().length());
            } else {
                toRemove.addFirst(i);
            }
        }

        if (sb.length() != 0) {
            System.out.println("все плохо");
        } else {
            System.out.println("все хорошо");
        }

        toRemove.forEach(paragraph::removeRun);
    }

    @SneakyThrows
    private String getProcessedText(String templateString, Map<String, Object> model) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateString);
        configuration.setTemplateLoader(stringTemplateLoader);
        Template template = configuration.getTemplate("template", "utf-8");

        StringWriter stringWriter = new StringWriter();

        template.process(model, stringWriter);

        return stringWriter.toString();
    }
}
