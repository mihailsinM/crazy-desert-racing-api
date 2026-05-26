package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.Role;

public class UserResponse {

    public Long id;

    public String name;

    public int age;

    public String email;

    public String licenseCategory;

    public boolean licenseVerified;

    public Role role;
}