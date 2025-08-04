package com.ecommerce.project.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5,message = "street name must be atleast 5 characters")
    private  String street;

    @NotBlank
    @Size(min = 5,message = "building name must be atleast 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 4,message = "city name must be atleast 4 characters")
    private String city;

    @NotBlank
    @Size(min = 2,message = "state name must be atleast 2 characters")
    private String state;

    @NotBlank
    @Size(min = 2,message = "country name must be atleast 2 characters")
    private String country;

    @NotBlank
    @Size(min = 6,message = "pincode name must be atleast 6 characters")
    private String pincode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
