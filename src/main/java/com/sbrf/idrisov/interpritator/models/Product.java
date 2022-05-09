package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "product")
@Getter
public class Product extends SuperModel {
    @XmlElement
    Requisites requisites;

    @XmlElement()
    String agreementLocation;
}
