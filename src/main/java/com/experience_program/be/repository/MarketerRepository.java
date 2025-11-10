package com.experience_program.be.repository;

import com.experience_program.be.entity.Marketer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketerRepository extends JpaRepository<Marketer, String> {
}
