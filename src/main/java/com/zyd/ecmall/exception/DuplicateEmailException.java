package com.zyd.ecmall.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("このメールアドレスは既に使用されています：" + email);
    }
}