package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "listTest")
@Getter
public class ListTest {
    @XmlElement(name = "document")
    List<Document> documents;
}
