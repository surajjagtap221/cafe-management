package com.in.cafe.restImpl;

import com.in.cafe.constants.CafeConstants;
import com.in.cafe.rest.PasswordRest;
import com.in.cafe.service.PasswordService;
import com.in.cafe.utils.CafeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PasswordResImpl implements PasswordRest {

    @Autowired
    PasswordService passwordService;


    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try{
            return passwordService.changePassword(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try{
            return passwordService.forgotPassword(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
        try{
            return passwordService.resetPassword(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
