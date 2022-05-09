package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Product")
@Getter
public class Requisites extends SuperModel {
    @XmlElement(name = "Number")
    Integer number;
}
