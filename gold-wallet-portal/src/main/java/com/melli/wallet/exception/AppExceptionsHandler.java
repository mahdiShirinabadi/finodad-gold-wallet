package com.melli.wallet.exception;

import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.base.ErrorDetail;
import com.melli.wallet.service.operation.AlertService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.utils.Helper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.auth.AuthenticationException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Log4j2
public class AppExceptionsHandler extends ResponseEntityExceptionHandler {

    private final Helper responseHelper;
    private final StatusRepositoryService statusRepositoryService;
    private final AlertService alertService;


    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<PanelBaseResponse<ObjectUtils.Null>> handleAnyException(Exception exception, WebRequest request) {
        ExceptionUtils.getStackTrace(exception);
        log.error("exception in request ==> {} with message {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getStackTrace());
        log.error("error in request " + exception.getCause());
        try{
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusRepositoryService.GENERAL_ERROR);
            errorDetail.setMessage(statusRepositoryService.findByCode(String.valueOf(StatusRepositoryService.GENERAL_ERROR)).getPersianDescription());
//            alertService.send("Exception in " + ((ServletWebRequest) request).getRequest().getRequestURI() + " and error message is "  + exception,"HUB-Portal-Exception", "");
            return ResponseEntity.status(HttpStatus.OK).body(new PanelBaseResponse<>(false, errorDetail));
        }catch (Exception ex){
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusRepositoryService.GENERAL_ERROR);
            errorDetail.setMessage("Exception: error read description");
            return ResponseEntity.status(HttpStatus.OK).body(new PanelBaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    public ResponseEntity<PanelBaseResponse<ObjectUtils.Null>> handleAuthenticationException(AuthenticationException exception, WebRequest request) {
        log.error("AuthenticationException in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try{
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusRepositoryService.TOKEN_NOT_VALID);
            errorDetail.setMessage(statusRepositoryService.findByCode(String.valueOf(StatusRepositoryService.TOKEN_NOT_VALID)).getPersianDescription());
//            alertService.send("AuthenticationException in " + ((ServletWebRequest) request).getRequest().getRequestURI() + " and error is "  + exception,"HUB-Portal-AuthenticationException", "");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PanelBaseResponse<>(false, errorDetail));
        }catch (Exception ex){
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusRepositoryService.TOKEN_NOT_VALID);
            errorDetail.setMessage("AuthenticationException: error read description");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new PanelBaseResponse<>(false, errorDetail));
        }

    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<PanelBaseResponse<ObjectUtils.Null>> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        log.error("AccessDeniedException in request ==> {} message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try{
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusRepositoryService.USER_NOT_PERMISSION);
            errorDetail.setMessage(statusRepositoryService.findByCode(String.valueOf(StatusRepositoryService.USER_NOT_PERMISSION)).getPersianDescription());
//            alertService.send("AccessDeniedException in " + ((ServletWebRequest) request).getRequest().getRequestURI() + " and error is "  + exception,"HUB-Portal-AccessDeniedException", "");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PanelBaseResponse<>(false, errorDetail));
        }catch (Exception ex){
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusRepositoryService.USER_NOT_PERMISSION);
            errorDetail.setMessage("AccessDeniedException: error read description");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new PanelBaseResponse<>(false, errorDetail));
        }

    }

    @ExceptionHandler(value = {InternalServiceException.class})
    public ResponseEntity<PanelBaseResponse<ObjectUtils.Null>> handleServiceException(InternalServiceException exception, WebRequest request) {
        log.error("service exception in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try{
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(exception.getStatus());
            String message = statusRepositoryService.findByCode(String.valueOf(exception.getStatus())).getPersianDescription();
            if(exception.getParameters() != null){
                message =  StringSubstitutor.replace(message, exception.getParameters(), "${", "}");
            }
            errorDetail.setMessage(message);
//            alertService.send("InternalServiceException in " + ((ServletWebRequest) request).getRequest().getRequestURI() + " and error is "  + exception,"HUB-Portal-InternalServiceException", "");
            return ResponseEntity.status(exception.getHttpStatus()).body(new PanelBaseResponse<>(false, errorDetail));
        }catch (Exception ex){
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(exception.getStatus());
            errorDetail.setMessage("InternalServiceException: error read description");
//            alertService.send("InternalServiceException in " + ((ServletWebRequest) request).getRequest().getRequestURI() + " and error is "  + exception,"HUB-Portal-InternalServiceException", "");
            return ResponseEntity.status(exception.getHttpStatus()).body(new PanelBaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<PanelBaseResponse<ObjectUtils.Null>> handleConstraintViolationExceptions(ConstraintViolationException exception, WebRequest request) {
        log.error("exception in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        ConstraintViolation<?> violation = exception.getConstraintViolations().iterator().next();
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setCode(StatusRepositoryService.INPUT_PARAMETER_NOT_VALID);
        errorDetail.setMessage(violation.getMessage());
//        alertService.send("ConstraintViolationException in " + ((ServletWebRequest) request).getRequest().getRequestURI() + " and error is "  + errorDetail.getCode() + "|" + errorDetail.getMessage(),"HUB-Portal-ConstraintViolationException", "");
        return ResponseEntity.status(HttpStatus.OK).body(new PanelBaseResponse<>(false, errorDetail));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            log.error("error in parameter ({}) , and error is ({}) and input value is ({})", fieldName, errorMessage, ((FieldError) error).getRejectedValue());
            errors.put(fieldName, errorMessage);
        });
        ErrorDetail errorDetail = new ErrorDetail(ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage(), StatusRepositoryService.INPUT_PARAMETER_NOT_VALID);
//        alertService.send("handleMethodArgumentNotValid in " + ((ServletWebRequest) request).getRequest().getRequestURI() + " and error is "  + errorDetail.getCode() + "|" + errorDetail.getMessage(),"HUB-Portal-handleMethodArgumentNotValid", "");
        return new ResponseEntity<>(new BaseResponse(false, errorDetail), HttpStatus.OK);
    }

    @Profile("dev")
    public static void main(String[] args) {


        Map<Integer, String> map = new HashMap<>();

        String TEMPLATE = "حداقل تعداد واحد ابطال برای صندوق [${1}] برابر با [${2}]";
        Map<String, String> params = new HashMap<>();
        params.put("1", "اعتماد");
        params.put("2", "42");
        params.put("3", "42");
        params.put("4", "42");
        String result = StringSubstitutor.replace(TEMPLATE, params, "${", "}");
        log.debug("Template result: {}", result);
    }


}
