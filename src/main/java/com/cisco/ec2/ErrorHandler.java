package com.cisco.ec2;

import com.amazonaws.AmazonServiceException;
import com.cisco.ec2.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);
    @ResponseBody
    @ExceptionHandler(Exception.class)
    ErrorResponse globalExceptionHandler(HttpServletResponse response, Exception ex) {
        LOGGER.error(ex.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return ErrorResponse.builder().errorMessage("Couldn't process request").status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
    }

    @ResponseBody
    @ExceptionHandler(AmazonServiceException.class)
    ErrorResponse amazonServiceExceptionHandler(HttpServletResponse response, AmazonServiceException ex) {
        LOGGER.error(ex.getMessage());
        response.setStatus(ex.getStatusCode());
        return ErrorResponse.builder().requestId(ex.getRequestId()).errorMessage("Downstream failure").errorType(ex.getErrorType().name()).status(ex.getStatusCode()).build();
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    ErrorResponse requestParamsExceptionHandler(HttpServletResponse response, ConstraintViolationException ex) {
        LOGGER.error(ex.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return ErrorResponse.builder().errorMessage(ex.getMessage()).status(HttpServletResponse.SC_BAD_REQUEST).build();
    }

}
