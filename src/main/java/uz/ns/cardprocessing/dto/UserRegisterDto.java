package uz.ns.cardprocessing.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDto {
    private String phoneNumber;
    private String password;
    private String fullName;
}
