package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;


    @Override
    public AddressDTO createAddress(AddressDTO addressDTO,User user) {
        Address address=modelMapper.map(addressDTO,Address.class);
        List<Address> addressList=user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);
        address.setUser(user);
        Address savedAddress=addressRepository.save(address);
        AddressDTO addressDTO1=modelMapper.map(savedAddress,AddressDTO.class);
        return addressDTO1;

    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses=addressRepository.findAll();
        List<AddressDTO> addressDTOS=addresses.stream().map(
                address -> modelMapper.map(address,AddressDTO.class)
        ).toList();
        return addressDTOS;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","Address Id",addressId));
        return modelMapper.map(address,AddressDTO.class);

    }

    @Override
    public List<AddressDTO> getAddressesByUser(User user) {
        List<Address> addresses=user.getAddresses();
        if(addresses.isEmpty()){
            throw new APIException("No Addresses exist for the user");
        }
        List<AddressDTO> addressDTOS=addresses.stream().map(address -> modelMapper.map(address,AddressDTO.class)).toList();
        return addressDTOS;
    }

    @Override
    public AddressDTO updateAddressOfUser(User user, Long addressId, AddressDTO addressDTO) {
        Address addressFromDB=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Addresss","address Id",addressId));
        addressFromDB.setCity(addressDTO.getCity());
        addressFromDB.setState(addressDTO.getState());
        addressFromDB.setBuildingName(addressDTO.getBuildingName());
        addressFromDB.setStreet(addressDTO.getStreet());
        addressFromDB.setPincode(addressDTO.getPincode());
        addressFromDB.setCountry(addressDTO.getCountry());
        Address updatedAddress=addressRepository.save(addressFromDB);
        user.getAddresses().removeIf(address -> address.getAddressId()==addressId);
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);
        return modelMapper.map(updatedAddress,AddressDTO.class);

    }

    @Override
    public String deleteAddressById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("address","address id",addressId));
        User user=address.getUser();
        user.getAddresses().removeIf(add -> add.getAddressId()==addressId);
        userRepository.save(user);
        addressRepository.deleteById(addressId);
        return "address deleted successfully with addressId "+addressId;
    }
}
