package com.morenod.basket.controller;

import com.morenod.basket.repository.UserRepository;
import com.morenod.basket.security.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils; 
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    // passwordencoder is needed
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String username = HtmlUtils.htmlEscape(credentials.get("username")); 
        String password = credentials.get("password");

        return userRepository.findByUsername(username)
            .map(user -> { // encoding password logic here
                if (!user.getPassword().startsWith("$2a$")) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Security update: Password migrated. Please log in again.");
                }

                if (passwordEncoder.matches(password, user.getPassword())) {
                    String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
                    String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

                    // refresh token logic 
                    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/api/auth/refresh")
                        .maxAge(7 * 24 * 60 * 60)
                        .build();

                    return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(accessToken);
                }
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials for user: " + username);
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found: " + username));
    }
    // mapping /refresh url
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (jwtUtil.validateToken(refreshToken)) {
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(jwtUtil.generateToken(user.getUsername(), user.getRole())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    // making the logout logic + url mapping
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0); // this tells the browser to delete the cookie immediately
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return ResponseEntity.ok("Logged out successfully");
    }
}