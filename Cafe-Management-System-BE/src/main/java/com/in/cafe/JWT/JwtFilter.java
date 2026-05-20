package com.in.cafe.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerUsersDetailsService service;

    Claims claims = null;

    private String userName = null;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.contains("/password/forgotPassword")
                || path.contains("/password/resetPassword")
                || path.contains("/user/login")
                || path.contains("/user/signup")) {
            filterChain.doFilter(request, response);
        }else{
            String authorizationHeader = request.getHeader("Authorization");
            String token = null;



            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

                token = authorizationHeader.substring(7);

                if (token != null && !token.trim().isEmpty()) {
                    try {
                        userName = jwtUtil.extractUsername(token);
                        claims = jwtUtil.extractAllClaims(token);
                    } catch (Exception e) {
                        System.out.println("Invalid JWT Token: " + e.getMessage());
                    }

                }

            }

            if(userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = service.loadUserByUsername(userName);

                if(jwtUtil.validateToken(token, userDetails)){
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }

            filterChain.doFilter(request, response);

        }
    }

    public boolean isAdmin(){
        return "admin".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isUser(){
        return "user".equalsIgnoreCase((String) claims.get("role"));
    }

    public String getCurrentUser(){
        return userName;
    }
}

