package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@XmlRootElement(name = "model")
@Getter
public class Model extends SuperModel {
    @XmlElement
    Product product;
    @XmlElement
    Participant participant;
    @XmlElement
    ClientSigner clientSigner;
    @XmlElement()
    Redakciya redakciya;

    @XmlElement()
    Integer documentCode;
    @XmlElement()
    String ipFullName;
    @XmlElement()
    String shortCompanyName;
    @XmlElement()
    String variantUplaty;

    //for creation control
    private Model() {
    }

    @SneakyThrows
    public static Model getModelFromFile(File modelFileInXmlFormat) {
        JAXBContext jaxbContext = JAXBContext.newInstance(Model.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Model) jaxbUnmarshaller.unmarshal(modelFileInXmlFormat);
    }
}
