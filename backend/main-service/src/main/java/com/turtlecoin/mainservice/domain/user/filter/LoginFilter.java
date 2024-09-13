package com.turtlecoin.mainservice.domain.user.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turtlecoin.mainservice.domain.user.dto.LoginUserDto;
import com.turtlecoin.mainservice.domain.user.util.JWTUtil;
import com.turtlecoin.mainservice.domain.user.util.ResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RedisTemplate<String, String> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        LoginUserDto user = objectMapper.readValue(req.getInputStream(), LoginUserDto.class);
        System.out.println(user.getEmail());
        // 스프링 시큐리티에서 email과 password를 검증하기 위해 token에 담아야 함.
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword(), null);

        // token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName(); // "username"을 "email"로 변경

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String access = jwtUtil.createToken("access", email, role, 600000L);
        String refresh = jwtUtil.createToken("refresh", email, role, 86400000L);

        // Redis에 refresh token 저장 (email, 토큰, 만료시간을 함께 저장)
        redisTemplate.opsForValue().set(email, refresh, 86400000L, TimeUnit.SECONDS);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 응답을 위한 JSON 데이터를 구성
        String jsonResponse = String.format("{\"status\": 200, \"message\": \"요청이 정상적으로 수행되었습니다.\", \"data\": {\"accessToken\": \"%s\", \"refreshToken\": \"%s\", \"role\": \"%s\"}}", access, refresh, role);

        // 응답 본문에 JSON 데이터 작성
        response.getWriter().write(jsonResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        ResponseUtil.sendResponse(response, 401, "로그인에 실패하였습니다.");
    }
}
