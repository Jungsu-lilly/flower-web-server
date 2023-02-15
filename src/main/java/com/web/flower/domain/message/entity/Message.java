package com.web.flower.domain.message.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Object data;
    private HttpStatus status;
    private String message;
    private String memo;

}
