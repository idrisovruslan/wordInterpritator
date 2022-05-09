package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ClientSigner")
@Getter
public class ClientSigner extends SuperModel {
    @XmlElement()
    String eioType;
    @XmlElement()
    String position;
    @XmlElement()
    String lastName;
    @XmlElement()
    String firstName;
    @XmlElement()
    String middleName;
}
