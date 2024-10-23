package uz.ns.cardprocessing.service.imp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.TokenDto;
import uz.ns.cardprocessing.dto.UserRegisterDto;
import uz.ns.cardprocessing.entity.User;
import uz.ns.cardprocessing.repository.UserRepository;
import uz.ns.cardprocessing.service.contract.AuthService;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, @Lazy AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ApiResult<Void> register(UserRegisterDto dto) {
        log.info("User registration with " + dto);
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber()))
            return ApiResult.errorResponse("PHONE_NUMBER_ALREADY EXISTS","PHONE_NUMBER_ALREADY EXISTS", 404);
        User user = new User();
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setFullName(dto.getFullName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        User save = userRepository.save(user);
        System.out.println(save);
        return ApiResult.successResponse();
    }

    @Override
    public ApiResult<TokenDto> refreshToken(String accessToken, String refreshToken) {
        return null;
    }

    @Override
    public ApiResult<Void> active(String activationCode) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByPhoneNumber(username).orElseThrow(() -> new UsernameNotFoundException("USER_NOT_FOUND_WITH_USERNAME"));

    }
}
