package com.TripSync.auth.service;


import com.TripSync.auth.dto.AuthResponse;
import com.TripSync.auth.dto.LoginRequest;
import com.TripSync.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));
        String token = jwtService.generateToken(authentication.getName());
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds());
    }
}