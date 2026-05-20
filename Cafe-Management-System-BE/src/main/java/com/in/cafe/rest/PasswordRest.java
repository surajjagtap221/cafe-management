package com.in.cafe.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping(path="/password")
public interface PasswordRest {

    @PostMapping(path="/changePassword")
    ResponseEntity<String> changePassword(@RequestBody Map<String, String> requestMap);

    @PostMapping(path="/forgotPassword")
    ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> requestMap);

    @PostMapping(path="/resetPassword")
    ResponseEntity<String> resetPassword(@RequestBody Map<String, String> requestMap);

}
