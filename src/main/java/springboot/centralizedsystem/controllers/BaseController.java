package springboot.centralizedsystem.controllers;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;

public class BaseController {

    @ExceptionHandler(NullPointerException.class)
    public String handlerNullEx(RedirectAttributes redirect) {
        return unauthorized(redirect);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handlerHttpClientEx(HttpClientErrorException httpException) {
        System.err.println("[ERROR] HttpClientErrorException: " + httpException.getMessage());
        String error = httpException.getResponseBodyAsString();
        return new ResponseEntity<>(new JSONObject(error).getString("message"), httpException.getStatusCode());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> handlerHttpEx(HttpServerErrorException httpException) {
        System.err.println("[ERROR] HttpServerErrorException: " + httpException.getMessage());
        String error = httpException.getResponseBodyAsString();
        return new ResponseEntity<>(new JSONObject(error).getString("message"), httpException.getStatusCode());
    }

    @ExceptionHandler({ Exception.class })
    public String handlerEx(Exception e) {
        System.err.println("[ERROR]: " + e.getMessage());
        return Views.ERROR_UNKNOWN;
    }

    public String unauthorized(RedirectAttributes redirect) {
        redirect.addFlashAttribute(Keys.LOGIN, Messages.TOKEN_EXPIRED_ERROR);
        return "redirect:" + RequestsPath.LOGIN;
    }

    public String roleForbidden(RedirectAttributes redirect) {
        return Views.ERROR_403;
    }
}
