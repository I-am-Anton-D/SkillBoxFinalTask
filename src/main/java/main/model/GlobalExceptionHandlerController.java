package main.model;

import java.io.IOException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;


@RestControllerAdvice
public class GlobalExceptionHandlerController {
//    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    @ExceptionHandler(value = NullPointerException.class)
//    @ResponseBody
//    public String handleNullPointerException(Exception e) {
//        System.out.println("A null pointer exception ocurred " + e);
//        return "123445";
//    }
//
//
//    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(value = Exception.class)
//    @ResponseBody
//    public String handleAllException(Exception e) {
//        System.out.println("A unknow Exception Ocurred: " + e);
//        return "123";
//    }


    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(HttpServletRequest httpServletRequest, HttpServletResponse response)
        throws IOException {
        return "forward:/404/";
    }
}