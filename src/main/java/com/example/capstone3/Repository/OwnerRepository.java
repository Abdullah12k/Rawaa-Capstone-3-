package com.example.capstone3.Repository;

import com.example.capstone3.Models.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Integer> {

    Owner findOwnerById(Integer id);
}
