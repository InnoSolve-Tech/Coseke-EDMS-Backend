package com.cosek.edms.authentication;

import com.cosek.edms.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthenticationResponse {
    private String token;
    private String firstname;
    private String lastname;
    private String email;
    private Set<Role> roles;
}
