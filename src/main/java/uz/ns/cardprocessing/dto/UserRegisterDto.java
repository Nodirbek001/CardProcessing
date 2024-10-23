package uz.ns.cardprocessing.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserRegisterDto {
    private String phoneNumber;
    private String password;
    private String fullName;
}
