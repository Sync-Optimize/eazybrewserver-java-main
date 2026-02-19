package com.eazybrew.vend.config;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.exceptions.CustomMessageException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.DateTimeException;
import java.util.Objects;


@Slf4j
@ControllerAdvice
public class RequestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ce) {
        ApiResponse<?> ar = new ApiResponse<>(ce.getStatus());
        if(ce.getStatus().is2xxSuccessful()) {
            ar.setMessage(ce.getMessage());
        } else {
            ar.setError(ce.getMessage());
        }
        return buildResponseEntity(ar);
    }

    @ExceptionHandler(CustomMessageException.class)
    public ResponseEntity<Object> handleCustomMessageException(CustomMessageException ce) {
        ApiResponse<?> ar = new ApiResponse<>(ce.getStatus());
        ar.setMessage(ce.getMessage());
        return buildResponseEntity(ar);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleException(DataIntegrityViolationException ce) {
//        logger.info("Error Message " + ce.getMessage());
        ApiResponse<?> ar = new ApiResponse<>(HttpStatus.CONFLICT);
        ar.setError("Duplicate key value violation");
        return buildResponseEntity(ar);
    }



    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ce,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
//        logger.info("Error Message " + ce.getMessage());
        ApiResponse<?> ar = new ApiResponse<>(HttpStatus.BAD_REQUEST);
        ar.setError("Pass Integer for Integer field(s), not a string. " + Objects.requireNonNull(ce.getRootCause()));
        return buildResponseEntity(ar);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<Object> handleDateTimeException(DateTimeException ce) {
//        logger.info("Error Message "+ce.getMessage());
        ApiResponse<?> ar = new ApiResponse<>(HttpStatus.BAD_REQUEST);
        ar.setError("Invalid date");
        return buildResponseEntity(ar);
    }


//  public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException e) {
//    ApiResponse<?> ar = new ApiResponse<>(HttpStatus.UNPROCESSABLE_ENTITY);
//    ar.setError(e.getMessage());
//    return buildResponseEntity(ar);
//  }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException mx,
                                                               HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest request) {
        ApiResponse<?> ar = new ApiResponse<>(HttpStatus.BAD_REQUEST);
        ar.addValidationError(mx.getBindingResult().getAllErrors());
        ar.setError("Validation Error");
        return buildResponseEntity(ar);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiResponse<?> apiResponse) {
        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    protected ResponseEntity<?> handleAuthenticationException(RuntimeException ex, WebRequest request) {
        log.error("Authentication exception occurred: {}", ex.getMessage(), ex);
        ApiResponse<?> ar = new ApiResponse<>(HttpStatus.UNAUTHORIZED);
        ar.setError("Authentication failed. Please check your credentials and try again.");
        return buildResponseEntity(ar);
    }

    // Remove the ambiguous exception handler by implementing the correct method from ResponseEntityExceptionHandler
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        ApiResponse<?> ar = new ApiResponse<>(HttpStatus.BAD_REQUEST);
        ar.setError("Document size must not exceed 20MB");
        return buildResponseEntity(ar);
    }

}
