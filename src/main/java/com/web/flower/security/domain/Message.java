package com.web.flower.security.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class Message {

    private Object data;
    private HttpStatus status;
    private String message;
    private String memo;

}
