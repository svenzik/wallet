package com.playtech.wallet.spring.controller.errors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class RestExceptionHandler {

    /**
     * If controllers method parameters are validated useing @Valid annotation
     * then MethodArgumentNotValidException is throw on failed validation.
     * This method parses validation error to custom JSON/XML message
     * @param ex MethodArgumentNotValidException produced by javax.validation
     * @return serializable custom error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
    HttpEntity<ResultMessage> handleControllerValidationError(MethodArgumentNotValidException ex) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();

        List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>(fieldErrors.size() + globalErrors.size());
        for (FieldError fieldError : fieldErrors) {
            errorMessages.add(new ErrorMessage(String.format("%s.%s: %s", fieldError.getObjectName(),fieldError.getField(), fieldError.getDefaultMessage())));
        }
        for (ObjectError objectError : globalErrors) {
            errorMessages.add(new ErrorMessage(String.format("%s: %s", objectError.getObjectName(), objectError.getDefaultMessage())));
        }

        return new ResponseEntity<ResultMessage>(new ResultMessage(errorMessages), HttpStatus.BAD_REQUEST);
    }


    /**
     * This catches exceptions for JSON/XML parsing errors and returns custom JSON/XML message
     * @param ex Any Exception
     * @return serializable custom error type
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    HttpEntity<ResultMessage> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException ex) {

//        ex.printStackTrace();

        String message = ex.getCause().getMessage();

        if (ex.getCause() instanceof  com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException) {
            message = ( (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException)ex.getCause() ).getOriginalMessage();
        }

        List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>(1);
        errorMessages.add(new ErrorMessage(message));

        return new ResponseEntity<ResultMessage>(new ResultMessage(errorMessages), HttpStatus.BAD_REQUEST);
    }

    /**
     * This catches exceptions for JSON/XML parsing errors and returns custom JSON/XML message
     * @param ex Any Exception
     * @return serializable custom error type
     */
    @ExceptionHandler(javax.persistence.RollbackException.class)
    HttpEntity<ResultMessage> handleRepositoryRollbackException(javax.persistence.RollbackException ex) {

//        ex.printStackTrace();

        return new ResponseEntity<ResultMessage>(buildResultFromExceptionCause(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockException.class)
    HttpEntity<ResultMessage> handleRepositoryRollbackException(javax.persistence.OptimisticLockException ex) {
        return new ResponseEntity<ResultMessage>(buildResultFromExceptionCause(ex), HttpStatus.CONFLICT);
    }

    /**
     * This catches no cought exceptions and parses custom JSON/XML message
     * @param ex Any Exception
     * @return serializable custom error type
     */
    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
    HttpEntity<ResultMessage> handleGenericException(Exception ex) {

        ex.printStackTrace();

        List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>(1);
        errorMessages.add(new ErrorMessage(ex));

        return new ResponseEntity<ResultMessage>(new ResultMessage(errorMessages), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //internal


    private ResultMessage buildResultFromExceptionCause(Exception ex) {
        String message = ex.getCause().getMessage();

        List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>(1);
        errorMessages.add(new ErrorMessage(message));

        return new ResultMessage(errorMessages);
    }


    //HttpMediaTypeNotAcceptableException - cannot serialize to XML/JSON



}


