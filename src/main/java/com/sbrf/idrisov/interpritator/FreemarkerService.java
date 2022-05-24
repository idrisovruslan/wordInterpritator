package com.sbrf.idrisov.interpritator;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Map;

@Service
public class FreemarkerService {

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
