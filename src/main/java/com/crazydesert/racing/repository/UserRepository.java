package com.crazydesert.racing.repository;

import com.crazydesert.racing.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
