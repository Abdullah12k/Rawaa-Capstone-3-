package com.example.capstone3.Repository;

import com.example.capstone3.Enums.RoommateStatus;
import com.example.capstone3.Models.RoommateRequest;
import com.example.capstone3.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoommateRequestRepository extends JpaRepository<RoommateRequest, Integer> {

    RoommateRequest findRoommateRequestById(Integer id);

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, RoommateStatus status);

    List<RoommateRequest> findBySenderOrReceiverOrderByCreatedAtDesc(
            User sender, User receiver);

    List<RoommateRequest> findBySenderAndStatusOrReceiverAndStatus(
            User sender, RoommateStatus status1,
            User receiver, RoommateStatus status2
    );
}
