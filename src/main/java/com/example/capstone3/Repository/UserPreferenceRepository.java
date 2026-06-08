package com.example.capstone3.Repository;

import com.example.capstone3.Models.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Integer> {

    UserPreference findUserPreferenceById(Integer id);

    UserPreference findUserPreferenceByUserId(Integer userId);

    @Query("SELECT up FROM UserPreference up WHERE up.lookingForRoommate = true " +
            "AND up.user.gender = :gender " +
            "AND up.user.id != :userId " +
            "AND up.user.currentRoommateId IS NULL " +
            "AND up.roommateBudget BETWEEN :minBudget AND :maxBudget")
    List<UserPreference> findPotentialRoommates(String gender, Integer userId, Double minBudget, Double maxBudget);
}
