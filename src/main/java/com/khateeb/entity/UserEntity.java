/*
 * This entity class represents a user in the database. It holds information 
 * about the user, like their email, name, and possibly the access token. 
 * It is saved and retrieved via UserEntityRepository.
 */

package com.khateeb.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_entity")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @Column(length = 500)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;

}