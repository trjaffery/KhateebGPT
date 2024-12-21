/*
 * This is the service interface that defines the methods (findByEmail, save, etc.) 
 * for managing users. UserServiceImpl implements this interface to provide the 
 * actual business logic for user-related operations.

 */

package com.khateeb.service;

import com.khateeb.entity.UserEntity;

import java.util.Optional;

public interface UserService {

    Optional<UserEntity> findByEmail(String email);

    void save(UserEntity user);
}