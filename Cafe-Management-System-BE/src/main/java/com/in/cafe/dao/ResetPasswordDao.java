package com.in.cafe.dao;


import com.in.cafe.POJO.ResetPasswordToken;
import org.springframework.data.repository.CrudRepository;

public interface ResetPasswordDao extends CrudRepository<ResetPasswordToken, String> {
}
