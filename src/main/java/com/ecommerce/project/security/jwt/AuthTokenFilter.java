package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger= LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("AuthenticationFilter called for URI: {}",request.getRequestURI());
        try{
            String jwt=parseJwt(request);
            if(jwt!=null && jwtUtils.validateJwtToken(jwt)){
                String username=jwtUtils.getUsernameFromJwtToken(jwt);
                UserDetails userDetails=userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                        userDetails,null,userDetails.getAuthorities()
                );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("Roles from JWT:{}",userDetails.getAuthorities());
            }
        }catch (Exception e){
                logger.error("cannot set user authentication: {}",e.getMessage());
        }
        filterChain.doFilter(request,response);

    }

//    public String parseJwt(HttpServletRequest request){
//        String jwt= jwtUtils.getJwtFromCookies(request);
//        logger.debug("AuthTokenFilter.java: {}",jwt);
//        return jwt;
//    }

    public String parseJwt(HttpServletRequest request){
        String jwtFromCookie= jwtUtils.getJwtFromCookies(request);
        if(jwtFromCookie!=null)
            return jwtFromCookie;
        String jwtFromHeader=jwtUtils.getJwtFromHeader(request);
        if(jwtFromHeader!=null)
            return jwtFromHeader;
        return null;
    }
}
