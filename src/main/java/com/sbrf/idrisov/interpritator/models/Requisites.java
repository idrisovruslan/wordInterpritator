package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "requisites")
@Getter
public class Requisites extends SuperModel {
    @XmlElement
    Integer number;
}
