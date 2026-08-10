package com.zyd.ecmall.exception;

public class LoginFailedException extends RuntimeException {

    public LoginFailedException() {
        super("メールアドレスまたはパスワードが正しくありません");
    }
}