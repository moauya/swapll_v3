package com.swapll.gradu.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetRequest {
    private String email;
    private String code;
    private String newPassword;


}
