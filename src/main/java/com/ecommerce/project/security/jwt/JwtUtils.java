package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
private static final Logger logger= LoggerFactory.getLogger(JwtUtils.class);

    //getting JWT from header
    @Value("${spring.app.jwtExpirationMs}")
    private  int jwtExpirationMs;
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookie;
//    public  String getJwtFromHeader(HttpServletRequest request){
//        String bearerToken=request.getHeader("Authorization");
//        logger.debug("Authorization Header: {}",bearerToken);
//        if(bearerToken!=null&&bearerToken.startsWith("Bearer ") ){
//            return bearerToken.substring(7);
//        }
//        return null;
//    }

    public String getJwtFromCookies(HttpServletRequest request){
        Cookie cookie= WebUtils.getCookie(request,jwtCookie);
        if(cookie!=null) return cookie.getValue();
        else{
            return null;
        }
    }

    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken=request.getHeader("Authorization");
        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }


    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrinciple){
        String jwt=generateTokenFromUsername(userPrinciple.getUsername());
        ResponseCookie cookie= ResponseCookie.from(jwtCookie,jwt)
                .path("/api")
                .maxAge(24*60*60)
                .httpOnly(false)
                .build();
        return cookie;
    }


    public ResponseCookie cleanJwtToken(){
        ResponseCookie cookie= ResponseCookie.from(jwtCookie,null)
                .path("/api")
                .build();
        return cookie;
    }

    //generating token from username

    public String generateTokenFromUsername(String uname){
        String username=uname;
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime())+jwtExpirationMs))
                .signWith(key())
                .compact();

    }
    //getting username from JWT Tokens
    public String getUsernameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith(key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    //generate Signing key

    public SecretKey key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)

        );
    }
    //validating JWT Token

    public boolean validateJwtToken(String authToken){
        try{

            System.out.println("validate");
            Jwts.parser().verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}",e.getMessage());
        }
        catch (ExpiredJwtException e) {
            logger.error("expired JWT token: {}",e.getMessage());
        }
        catch (UnsupportedJwtException e){
            logger.error("unsupported JWT token: {}",e.getMessage());
        }
        catch (IllegalArgumentException e){
            logger.error("empty JWT token: {}",e.getMessage());
        }
        return false;
    }

}
