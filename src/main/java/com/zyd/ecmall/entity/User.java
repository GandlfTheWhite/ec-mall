package com.zyd.gulimail.entity;

import java.time.LocalDateTime;

public class User {

    private Long id;

    private String name;

    private Integer age;

    private String email;

    private LocalDateTime createTine;

    public User(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreateTine() {
        return createTine;
    }

    public void setCreateTine(LocalDateTime createTine) {
        this.createTine = createTine;
    }
}
