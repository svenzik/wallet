package com.playtech.wallet.spring.controller.errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class ErrorMessage {

    //for serialization
    public ErrorMessage() {
    }

    public ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorMessage(Exception e) {
//        String newline = System.getProperty("line.separator");
//
//        StringBuffer sb = new StringBuffer();
//        sb.append(e.getMessage());
//        sb.append(newline);
//        sb.append("Cause: ");
//
//        for(StackTraceElement stackTraceElement:e.getStackTrace()) {
//            sb.append(newline);
//            sb.append(stackTraceElement);
//        }
//
//        this.errorMessage = sb.toString();
        this.errorMessage = e.getMessage();
    }

    @JsonProperty("error_message")
    @XmlElement(name="error_message")
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

//    public void setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//    }
}
