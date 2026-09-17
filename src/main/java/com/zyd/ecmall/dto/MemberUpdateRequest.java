package com.zyd.ecmall.dto;

import jakarta.validation.constraints.*;

public class MemberUpdateRequest {

    @Pattern(regexp = "(?s).*\\S.*", message = "氏名を入力してください")
    private String name;
    @Email(message = "メールアドレスの形式が正しくありません")
    @Pattern(regexp = "(?s).*\\S.*", message = "メールアドレスを入力してください")
    private String email;
    @Min(value = 0, message = "年齢は0以上で入力してください")
    @Max(value = 150, message = "年齢は150以下で入力してください")
    private Integer age;
    @Size(min = 6, max = 20, message = "パスワードは6〜20文字で入力してください")
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
