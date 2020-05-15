package com.cisco.ec2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
@Builder
public class ErrorResponse {
    private String errorMessage;
    private String errorType;
    private int status;
    private String requestId;
}
