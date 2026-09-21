package com.ipagency.service;

import com.ipagency.dto.LoginRequest;
import com.ipagency.dto.PasswordChangeRequest;
import com.ipagency.vo.LoginVO;
import com.ipagency.vo.UserProfileVO;

public interface AuthService {
    LoginVO login(LoginRequest request);
    UserProfileVO profile();
    void changePassword(PasswordChangeRequest request);
}
