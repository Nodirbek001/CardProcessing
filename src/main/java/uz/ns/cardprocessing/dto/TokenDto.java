package uz.ns.cardprocessing.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TokenDto {
    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer";
}
