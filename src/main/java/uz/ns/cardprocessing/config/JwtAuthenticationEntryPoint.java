package uz.ns.cardprocessing.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.ErrorData;
import uz.ns.cardprocessing.utils.AppConstants;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("Responding with unauthorized error. URL -  {}, Message - {}", request.getRequestURI(), authException.getMessage());
        ApiResult<ErrorData> errorDataApiResult = ApiResult.errorResponse(authException.getMessage(), "Forbidden", 403);
        response.getWriter().write(AppConstants.objectMapper.writeValueAsString(errorDataApiResult));
        response.setStatus(403);
        response.setContentType("application/json");
    }
}
