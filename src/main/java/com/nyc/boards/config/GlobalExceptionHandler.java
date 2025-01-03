package com.nyc.boards.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;

@ControllerAdvice
@Slf4j // Add this for logging
public class GlobalExceptionHandler {

    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException e, Model model) {
        log.error("File upload failed: ", e); // Log the exception
        model.addAttribute("error", "File upload failed: " + e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        log.error("An error occurred: ", e); // Log the exception
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e, Model model) {
        log.error("Argument type mismatch: ", e);
        model.addAttribute("error", "Invalid argument: " + e.getMessage());

        return "error";
    }


    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormatException(NumberFormatException e, Model model) {
        log.error("Number format exception: ", e);
        model.addAttribute("error", "Invalid number format: " + e.getMessage());
        return "error";
    }

}
