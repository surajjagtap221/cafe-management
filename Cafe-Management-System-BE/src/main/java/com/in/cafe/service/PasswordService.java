package com.in.cafe.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PasswordService {

    ResponseEntity<String> changePassword(Map<String, String> requestMap);

    ResponseEntity<String> forgotPassword(Map<String, String> requestMap);

    ResponseEntity<String> resetPassword(Map<String, String> requestMap);
}
