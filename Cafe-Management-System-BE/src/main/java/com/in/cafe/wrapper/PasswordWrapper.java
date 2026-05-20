package com.in.cafe.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordWrapper {
    private String token;
    private String newPassword;

    public PasswordWrapper(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }
}
