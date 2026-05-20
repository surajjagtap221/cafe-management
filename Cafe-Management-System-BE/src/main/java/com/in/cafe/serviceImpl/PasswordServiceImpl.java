package com.in.cafe.serviceImpl;


import com.in.cafe.JWT.CustomerUsersDetailsService;
import com.in.cafe.JWT.JwtFilter;
import com.in.cafe.JWT.JwtUtil;
import com.in.cafe.POJO.ResetPasswordToken;
import com.in.cafe.POJO.User;
import com.in.cafe.constants.CafeConstants;
import com.in.cafe.dao.ResetPasswordDao;
import com.in.cafe.dao.UserDao;
import com.in.cafe.service.PasswordService;
import com.in.cafe.utils.CafeUtils;
import com.in.cafe.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
public class PasswordServiceImpl implements PasswordService {


    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Autowired
    ResetPasswordDao resetPasswordDao;


    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            System.out.println(".....................................................................");
            User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
            System.out.println(userObj);
            if (userObj != null) {
                if (passwordEncoder.matches(requestMap.get("oldPassword"), userObj.getPassword())) {
                    userObj.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
                    userDao.save(userObj);
                    return CafeUtils.getResponseEntity("Password Updated Successfully.", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Incorrect Old Password.", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {

        try {
            User user = userDao.findByEmail(requestMap.get("email"));

            if (user != null) {

                String token = UUID.randomUUID().toString();

                ResetPasswordToken resetToken =
                        new ResetPasswordToken(token, user.getEmail());

                resetPasswordDao.save(resetToken);

                emailUtils.forgetPasswordMail(
                        user.getEmail(),
                        "Reset Password",
                        token
                );
            }

            return CafeUtils.getResponseEntity(
                    "Reset link sent to your email.",
                    HttpStatus.OK
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return CafeUtils.getResponseEntity(
                CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {

        try {
            Optional<ResetPasswordToken> tokenObj =
                    resetPasswordDao.findById(requestMap.get("token"));

            if (tokenObj.isEmpty()) {
                return CafeUtils.getResponseEntity(
                        "Invalid or expired token",
                        HttpStatus.BAD_REQUEST
                );
            }

            ResetPasswordToken token = tokenObj.get();

            User user = userDao.findByEmail(token.getEmail());

            if (user == null) {
                return CafeUtils.getResponseEntity(
                        "User not found",
                        HttpStatus.NOT_FOUND
                );
            }

            user.setPassword(
                    passwordEncoder.encode(requestMap.get("newPassword"))
            );

            userDao.save(user);

            resetPasswordDao.deleteById(token.getToken()); // one-time use

            emailUtils.passwordUpdatedEmail(
                    user.getEmail(),
                    "Password Updated",
                    "Your password has been updated successfully."
            );

            return CafeUtils.getResponseEntity(
                    "Password changed successfully",
                    HttpStatus.OK
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return CafeUtils.getResponseEntity(
                CafeConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


}
