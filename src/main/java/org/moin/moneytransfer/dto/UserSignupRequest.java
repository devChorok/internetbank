package org.moin.moneytransfer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSignupRequest {

    private String userId;
    private String password;
    private String name;
    private String idType;
    private String idValue;
}
