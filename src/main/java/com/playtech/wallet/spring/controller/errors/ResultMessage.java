package com.playtech.wallet.spring.controller.errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement(name = "result")
public class ResultMessage {

    public ResultMessage() {
    }

    public ResultMessage(Collection<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @JsonProperty("errors")
    @XmlElementWrapper(name="errors")
    @XmlElement(name="error")
    private Collection<ErrorMessage> errorMessages;

    public Collection<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

//    public void setErrors(List<ErrorMessage> errorMessages) {
//        this.errorMessages = errorMessages;
//    }

}
