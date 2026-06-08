package com.example.capstone3.Repository;

import com.example.capstone3.Models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Review findReviewById(Integer id);

    List<Review> findReviewByApartmentId(Integer id);

    List<Review> findReviewsByUserId(Integer userId);

    List<Review> findByApartment_Building_Owner_Id(Integer ownerId);

    boolean existsByReservation_Id(Integer reservationId);
}
