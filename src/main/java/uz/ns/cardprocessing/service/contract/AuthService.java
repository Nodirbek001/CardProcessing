package uz.ns.cardprocessing.service.contract;
import uz.ns.cardprocessing.dto.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.entity.User;

public interface AuthService extends UserDetailsService {
    ApiResult<Void> register(UserRegisterDto dto);

  //  ApiResult<TokenDto> login(UserLoginDto dto);

    ApiResult<TokenDto> refreshToken(String accessToken, String refreshToken);

    ApiResult<Void> active(String activationCode);
}
