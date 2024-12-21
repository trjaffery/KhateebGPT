/*
 * This interface extends JpaRepository, providing CRUD operations for UserEntity. 
 * It is used by UserServiceImpl to query or update user data in the database.
 */

package com.khateeb.repository;

import com.khateeb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
}