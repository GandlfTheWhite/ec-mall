package com.zyd.ecmall.dto;

import jakarta.validation.constraints.*;

public class MemberCreateRequest {

    @NotBlank(message = "名前をブラックに設定できない")
    private String name;
    @NotBlank(message = "メールはブラックに設定できない")
    @Email(message = "メール格式間違えた")
    private String email;
    @Min(value = 0, message = "年齢０以下はだめです")
    @Max(value = 150, message = "年齢は150を超えてはいけない")
    private Integer age;
    @NotBlank(message = "パスワードをブラックに設定できない")
    @Size(min = 6, max = 20,
            message = "パスワード長さは6から20以内必要")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}