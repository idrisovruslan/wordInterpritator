package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Product")
@Getter
public class Product extends SuperModel {
    @XmlElement(name = "Requisites")
    Requisites requisites;

    @XmlElement()
    String agreementLocation;
}
