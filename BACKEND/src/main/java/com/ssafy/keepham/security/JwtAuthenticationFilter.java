package com.ssafy.keepham.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = parseBearerToken(request);
            User user = parseUserSpecification(token);
            AbstractAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(user,token,user.getAuthorities());
            authenticated.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticated);
        }catch (Exception e){
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request,response);
    }

    private User parseUserSpecification(String token) {
        String split[] = Optional.ofNullable(token)
            .filter(subject -> 10 <= subject.length())
            .map(tokenProvider::validateTokenAndGetSubJect)
            .orElse("ssafy:ssafy")
            .split(":");
        return new User(split[0],"", List.of(new SimpleGrantedAuthority(split[1])));
    }


    //토큰을 파싱 후 payload 부분만 남(접두사 제거)
    private String parseBearerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
            .filter(parseBearerToken -> parseBearerToken.substring(0,7).equalsIgnoreCase("Bearer "))
            .map(parseBearerToken -> parseBearerToken.substring(7))
            .orElse(null);
    }
}