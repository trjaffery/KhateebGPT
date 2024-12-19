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

}