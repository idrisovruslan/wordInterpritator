package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;

@Getter
public class SuperModel {

    Map<String, String> pathToValue = new HashMap<>();

    @SneakyThrows
    public Object getValueFromPath(String path) {
        String[] values = path.split("\\.");
        List<String> valuesList = new LinkedList<>(Arrays.asList(values));

        valuesList.remove(0);

        return getValueFromPath(valuesList);
    }

    @SneakyThrows
    private Object getValueFromPath(List<String> variablesNamesList) {
        Class<? extends SuperModel> thisClass = this.getClass();
        Field[] declaredFields = thisClass.getDeclaredFields();

        for (Field field:declaredFields) {
            if (field.getName().equalsIgnoreCase(variablesNamesList.get(0))) {
                if (variablesNamesList.size() == 1) {
                    return field.get(this);
                } else {
                    variablesNamesList.remove(0);
                    return ((SuperModel) field.get(this)).getValueFromPath(variablesNamesList);
                }
            }
        }

        throw new RuntimeException("неверный путь " + variablesNamesList.get(0));
    }
}
