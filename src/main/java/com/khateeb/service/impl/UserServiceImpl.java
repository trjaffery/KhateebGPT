/*
 * This service class contains the business logic for managing users. 
 * It interacts with the UserEntityRepository to find, save, and update 
 * user information. It is responsible for the core logic in handling 
 * user-related operations.
 */

package com.khateeb.service.impl;

import com.khateeb.entity.UserEntity;
import com.khateeb.repository.UserEntityRepository;
import com.khateeb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserEntityRepository userEntityRepository;

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userEntityRepository.findByEmail(email);
    }

    @Override
    public void save(UserEntity user) {
        userEntityRepository.save(user);
    }
}