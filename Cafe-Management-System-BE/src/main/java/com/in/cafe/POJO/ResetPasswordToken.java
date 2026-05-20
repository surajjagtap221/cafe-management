package com.in.cafe.POJO;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RedisHash(value = "reset-password", timeToLive = 900)
public class ResetPasswordToken {

    @Id
    private String token;     // Redis primary key

    private String email;

    public ResetPasswordToken() {}

    public ResetPasswordToken(String token, String email) {
        this.token = token;
        this.email = email;
    }
}