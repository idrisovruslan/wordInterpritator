package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.models.Model;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sbrf.idrisov.interpritator.ParagraphUtils.replaceText;

@Service
public class FreemarkerService {

    private void replaceVariablesWithValues(Map<String, Object> model, XWPFParagraph paragraph) {
        Model currentModel = (Model) model.get("model");

        String paragraphText = paragraph.getText();

        Pattern pattern = Pattern.compile("\\$\\{\\w+(\\.\\w+)+\\}?");
        Matcher matcher = pattern.matcher(paragraphText);

        while (matcher.find()) {
            String foundVariable = matcher.group(0);
            String variablePath = foundVariable.substring(2, foundVariable.length() - 1);
            String variableValue;

            if (currentModel.getPathToValue().containsKey(variablePath)) {
                variableValue = currentModel.getPathToValue().get(variablePath);
            } else {
                variableValue = currentModel.getValueFromPath(variablePath).toString();
                currentModel.getPathToValue().put(variablePath, variableValue);
            }
            replaceText(paragraph, foundVariable, variableValue);
        }
    }

    //TODO первое что пришло в голову, не факт, что работает правильно
    private void transformParagraph(XWPFParagraph paragraph, Map<String, Object> model) {
        String resultText = getProcessedText(paragraph.getText(), model);
        StringBuilder sb = new StringBuilder(resultText);
        List<XWPFRun> runs = paragraph.getRuns();
        Deque<Integer> runsToRemove = new LinkedList<>();

        String[] resultTextByParagraph = resultText.split("\\n");

        for (int i = 0; i < runs.size(); i++) {
            if (sb.indexOf(runs.get(i).text()) == 0) {
                sb.delete(0, runs.get(i).text().length());
            } else {
                runsToRemove.addFirst(i);
            }
        }

        if (sb.length() != 0) {
            System.out.println("все плохо");
        } else {
            System.out.println("все хорошо");
        }

        runsToRemove.forEach(paragraph::removeRun);
    }

    @SneakyThrows
    public String getProcessedText(String templateString, Map<String, Object> model) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateString);
        configuration.setTemplateLoader(stringTemplateLoader);

        StringWriter stringWriter = new StringWriter();

        Template template;
        try {
            template = configuration.getTemplate("template", "utf-8");

            template.process(model, stringWriter);
        } catch (Exception e) {
            System.out.println(templateString);
            throw e;
        }

        return stringWriter.toString();
    }
}
