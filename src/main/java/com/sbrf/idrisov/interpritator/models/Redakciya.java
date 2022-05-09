package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "redakciya")
@Getter
public class Redakciya extends SuperModel {
    @XmlElement()
    String date;
    @XmlElement()
    String num;

    //for creation control
    private Redakciya() {
    }
}
