package com.yongyang.demo.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object notFoundException(final Exception e) {
        e.printStackTrace();
        return Response.newFailed(Response.Code.INTERNAL_ERROR, e.getMessage());
    }
}
