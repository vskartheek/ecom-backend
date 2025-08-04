package com.ecommerce.project.controller;


import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Authentication authentication;
        try{
            authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

        }catch (AuthenticationException exception){
            Map<String,Object> map=new HashMap<>();
            map.put("message","Bad credentials");
            map.put("status",false);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails= (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie=jwtUtils.generateJwtCookie(userDetails);
        List<String> roles=userDetails.getAuthorities().stream()
                .map(item->item.getAuthority()).collect(Collectors.toList());
        UserInfoResponse response=new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),roles);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE
        ,jwtCookie.toString()).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest signupRequest){

        if(userRepository.existsByUsername(signupRequest.getUsername())){

            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if(userRepository.existsByEmail(signupRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email already Exists!"));
        }

        User user=new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        Set<String> strRoles=signupRequest.getRole();
        Set<Role> roles=new HashSet<>();
        if(strRoles==null) {

            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role Not  found/assigned"));
            roles.add(userRole);
        }else{
            //admin --> ROLE_ADMIN
            //seller --> ROLE_SELLER
            strRoles.forEach(role->{
                switch(role){
                    case "admin":
                        Role adminRole=roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(()->new RuntimeException("Error: Role not found"));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole=roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(()->new RuntimeException("Error: Role not found"));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole=roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(()->new RuntimeException("Error: Role not found"));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User Registered Successfully!"));

    }

    @GetMapping("/username")
    public String currentUsername(Authentication authentication){
        if(authentication!=null){
            return authentication.getName();
        }
        else return "NULL";
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication){

        UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();
        List<String> roles=userDetails.getAuthorities().stream()
                .map(item->item.getAuthority()).collect(Collectors.toList());
        UserInfoResponse response=new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),roles);
        return ResponseEntity.ok().body(response);
           }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie cookie=jwtUtils.cleanJwtToken();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE
                ,cookie.toString()).body(new MessageResponse("You have been signed out"));
    }


}
