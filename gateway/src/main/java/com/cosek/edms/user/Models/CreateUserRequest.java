package com.cosek.edms.user.Models;

import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserRequest {
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;
    private String address;
    private String password;
    private List<Long> roles;
}
