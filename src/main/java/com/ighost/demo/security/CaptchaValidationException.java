package com.ighost.demo.security;

import org.springframework.security.core.AuthenticationException;

public class CaptchaValidationException extends AuthenticationException {

    public CaptchaValidationException(String msg) {
        super(msg);
    }
}
