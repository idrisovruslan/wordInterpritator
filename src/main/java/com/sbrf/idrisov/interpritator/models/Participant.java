package com.sbrf.idrisov.interpritator.models;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Participant")
@Getter
public class Participant extends SuperModel {
    @XmlElement()
    String participantType;
    @XmlElement()
    String fullName;
}
