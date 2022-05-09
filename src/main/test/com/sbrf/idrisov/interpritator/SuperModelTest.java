package com.sbrf.idrisov.interpritator;

import com.sbrf.idrisov.interpritator.models.Model;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SuperModelTest {

    @Test
    void getValueFromPath() {
        Model myModel = Model.getModelFromFile(new File("src/main/resources/source.xml"));

        Object valueFromPath = myModel.getValueFromPath("model.Product.Requisites.Number");

        assertEquals((int)valueFromPath, 112233);
    }
}