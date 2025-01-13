package com.melli.hub.exception;


import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Setter
@Getter
public class InternalServiceException extends Exception {

    private int  status;
    private HttpStatus httpStatus;
    private Map<String, String> parameters;

    public InternalServiceException(){
        super();
    }

    public InternalServiceException(int status) {
        this.status = status;
    }


    public InternalServiceException(String message, int status, HttpStatus httpStatus) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }

    /**
     * Map.ofEntries(
     *                     entry("1", fundEntity.getName() + "-" + fundEntity.getDetailName()),
     *                     entry("2", String.valueOf(fundEntity.getMaxAmount()))
     * )
     * @param message
     * @param status
     * @param httpStatus
     * @param parameters (Map.ofEntries(
     *                         entry("1", fundEntity.getName() + "-" + fundEntity.getDetailName()),
     *                          entry("2", String.valueOf(fundEntity.getMaxAmount())))
     */
    public InternalServiceException(String message, int status, HttpStatus httpStatus,  Map<String, String> parameters) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
        this.parameters = parameters;
    }

    public InternalServiceException(String message, Throwable cause, int status) {
        super(message, cause);
        this.status = status;
    }
}
