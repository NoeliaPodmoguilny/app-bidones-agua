package com.agua.bidones.dto;

import com.agua.bidones.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    private UserType userType;
    private String name;
    private String identifier;
    private String password;

}
