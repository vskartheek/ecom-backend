package com.ecommerce.project.controller;


import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;
    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO){

        User user=authUtil.loggedInUser();
        AddressDTO addedAddress=addressService.createAddress(addressDTO,user);
        return new ResponseEntity<>(addedAddress, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(){
        List<AddressDTO> allAddresses=addressService.getAllAddresses();
        return new ResponseEntity<>(allAddresses,HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId){
        AddressDTO addressById=addressService.getAddressById(addressId);
        return new ResponseEntity<>(addressById,HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){
        User user=authUtil.loggedInUser();
        List<AddressDTO> allAddresses=addressService.getAddressesByUser(user);
        return new ResponseEntity<>(allAddresses,HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId,@RequestBody AddressDTO addressDTO){
        User user=authUtil.loggedInUser();
        AddressDTO addressDTO1=addressService.updateAddressOfUser(user,addressId,addressDTO);
        return new ResponseEntity<>(addressDTO1,HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddressById(@PathVariable Long addressId){
        String response=addressService.deleteAddressById(addressId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
