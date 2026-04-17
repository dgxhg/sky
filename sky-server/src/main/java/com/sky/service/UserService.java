package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

public interface UserService {
    User  wxLogin (UserLoginDTO userLoginDTO);
}
