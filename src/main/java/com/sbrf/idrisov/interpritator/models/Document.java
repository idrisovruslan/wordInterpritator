package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "document")
@Getter
public class Document {
    @XmlElement()
    String id;

    @XmlElement()
    String name;
}
