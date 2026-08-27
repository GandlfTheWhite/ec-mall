package com.zyd.ecmall.dto;

import jakarta.validation.constraints.*;

public class MemberCreateRequest {

    @NotBlank(message = "名前は必須です")
    private String name;
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;
    @Min(value = 0, message = "年齢は0以上を入力してください")
    @Max(value = 150, message = "年齢は150以下で入力してください")
    private Integer age;
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 6, max = 20,
            message = "パスワードは6文字以上20文字以内で入力してください")
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