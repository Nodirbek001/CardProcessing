package uz.ns.cardprocessing.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.UserRegisterDto;
import uz.ns.cardprocessing.service.contract.AuthService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final AuthService authService;

    @PostMapping("/api/register")
    ApiResult<Void> register(@RequestBody UserRegisterDto userRegisterDto) {
        log.info("Registering user {}", userRegisterDto);
        return authService.register(userRegisterDto);
    }
}
