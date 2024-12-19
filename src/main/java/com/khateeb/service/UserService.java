// Defines the contract (method signatures) for user-related operations.

package com.khateeb.service;

import com.khateeb.entity.UserEntity;

import java.util.Optional;

public interface UserService {

    Optional<UserEntity> findByEmail(String email);

    void save(UserEntity user);
}