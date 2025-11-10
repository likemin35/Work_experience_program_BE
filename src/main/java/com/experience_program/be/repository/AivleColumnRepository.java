package com.experience_program.be.repository;

import com.experience_program.be.entity.AivleColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AivleColumnRepository extends JpaRepository<AivleColumn, String> {
}
