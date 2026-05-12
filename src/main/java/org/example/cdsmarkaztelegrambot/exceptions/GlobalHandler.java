package org.example.cdsmarkaztelegrambot.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler
    public void exceptionHandler(Exception e){
        System.err.println(e.getMessage());
    }
}
