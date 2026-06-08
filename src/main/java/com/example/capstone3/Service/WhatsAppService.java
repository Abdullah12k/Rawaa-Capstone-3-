package com.example.capstone3.Service;

import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Contract;
import com.example.capstone3.Models.MaintenanceRequest;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.Reservation;
import com.example.capstone3.Models.Review;
import com.example.capstone3.Models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final RestTemplate restTemplate;

    @Value("${ultramsg.token}")
    private String token;

    @Value("${ultramsg.instance.id}")
    private String instanceId;

    private void sendMessage(String phoneNumber, String message) {
        try {
            String url = "https://api.ultramsg.com/" + instanceId + "/messages/chat";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("token", token);
            params.add("to", phoneNumber);
            params.add("body", message);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            restTemplate.postForEntity(url, request, String.class);

        } catch (Exception e) {
            System.err.println("WhatsApp notification failed for " + phoneNumber + ": " + e.getMessage());
        }
    }


    public void notifyTenantReservationAccepted(User tenant, Apartment apartment) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Your reservation for apartment *" + apartment.getApartmentNumber() + "* has been *ACCEPTED*.\n" +
                "District: " + apartment.getBuilding().getDistrict() + "\n" +
                "Monthly Rent: " + apartment.getMonthlyRent() + " SAR\n\n" +
                "Please wait for your contract to be generated.\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantReservationRejected(User tenant, Apartment apartment) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Unfortunately, your reservation for apartment *" + apartment.getApartmentNumber() + "* has been *REJECTED*.\n\n" +
                "You can browse other available apartments on the platform.\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantReservationExpired(User tenant, Apartment apartment) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Your reservation for apartment *" + apartment.getApartmentNumber() + "* has *EXPIRED*.\n\n" +
                "You can submit a new reservation if the apartment is still available.\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantContractCreated(User tenant, Contract contract) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "A new contract is ready for apartment *" + contract.getReservation().getApartment().getApartmentNumber() + "*.\n" +
                "Contract Number: " + contract.getContractNumber() + "\n" +
                "Start Date: " + contract.getStartDate() + "\n" +
                "End Date: " + contract.getEndDate() + "\n\n" +
                "Please log in to review and accept or reject the contract.\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantContractEnded(User tenant, Apartment apartment) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Your rental contract for apartment *" + apartment.getApartmentNumber() + "* has officially *ENDED*.\n\n" +
                "Please make sure to hand over the apartment keys and submit your final review.\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantContractTerminated(User tenant, Apartment apartment) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Your rental contract for apartment *" + apartment.getApartmentNumber() + "* has been *TERMINATED*.\n" +
                "The rental is now completed and the apartment is under maintenance.\n\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantContractRenewed(User tenant, Contract contract) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Your rental contract for apartment *" + contract.getReservation().getApartment().getApartmentNumber() + "* has been *RENEWED*.\n" +
                "New End Date: " + contract.getEndDate() + "\n\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantContractEndingSoon(User tenant, Contract contract) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Your rental contract for apartment *" + contract.getReservation().getApartment().getApartmentNumber() + "* will end in 30 days.\n" +
                "End Date: " + contract.getEndDate() + "\n\n" +
                "Please review your renewal or move-out plans.\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyTenantMaintenanceUpdated(User tenant, MaintenanceRequest maintenanceRequest) {
        String message = "Hello " + tenant.getFullName() + ",\n\n" +
                "Your maintenance request has been updated.\n" +
                "Issue: " + maintenanceRequest.getDescription() + "\n" +
                "Status: *" + maintenanceRequest.getStatus() + "*\n\n" +
                "Smart Rental Platform";
        sendMessage(tenant.getPhoneNumber(), message);
    }

    public void notifyOwnerContractAccepted(Owner owner, Apartment apartment, User tenant) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "The contract for apartment *" + apartment.getApartmentNumber() + "* has been *ACCEPTED* by the tenant.\n" +
                "Tenant: " + tenant.getFullName() + "\n" +
                "Phone: " + tenant.getPhoneNumber() + "\n\n" +
                "The apartment status has been updated to RENTED.\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }

    public void notifyOwnerContractRejected(Owner owner, Apartment apartment, User tenant) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "The contract for apartment *" + apartment.getApartmentNumber() + "* has been *REJECTED* by the tenant.\n" +
                "Tenant: " + tenant.getFullName() + "\n" +
                "Phone: " + tenant.getPhoneNumber() + "\n\n" +
                "The apartment status has been updated back to AVAILABLE.\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }

    public void notifyOwnerNewReservation(Apartment apartment, Reservation reservation) {
        Owner owner = apartment.getOwner();

        StringBuilder message = new StringBuilder();
        message.append("Hello ").append(owner.getFullName()).append(",\n\n")
                .append("You have a *NEW RESERVATION* request.\n")
                .append("Apartment number: ").append(reservation.getApartment().getApartmentNumber()).append("\n")
                .append("Tenant: ").append(reservation.getUser().getFullName()).append("\n")
                .append("Requested Start Date: ").append(reservation.getRequestedStartDate()).append("\n")
                .append("Rental Duration: ").append(reservation.getRentalMonths()).append(" months\n");

        message.append("Monthly Rent: ").append(apartment.getMonthlyRent()).append(" SAR\n");

        if (Boolean.TRUE.equals(apartment.getNegotiable()) && apartment.getDesiredMonthlyRent() != null) {
            message.append("Desired Monthly Rent: ").append(apartment.getDesiredMonthlyRent()).append(" SAR\n");
        }

        message.append("\nPlease log in to accept or reject the reservation.\n")
                .append("Smart Rental Platform");

        sendMessage(owner.getPhoneNumber(), message.toString());
    }

    public void notifyOwnerReservationCancelled(Owner owner, Reservation reservation) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "The reservation for apartment *" + reservation.getApartment().getApartmentNumber() + "* has been *CANCELLED* by the tenant.\n" +
                "Tenant: " + reservation.getUser().getFullName() + "\n\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }

    public void notifyOwnerContractEnded(Owner owner, Apartment apartment) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "The contract for apartment *" + apartment.getApartmentNumber() + "* has *ENDED*.\n" +
                "The apartment has been placed *UNDER MAINTENANCE* for your inspection.\n\n" +
                "Please log in to review and update the apartment status.\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }

    public void notifyOwnerContractRenewed(Owner owner, Contract contract) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "The contract for apartment *" + contract.getReservation().getApartment().getApartmentNumber() + "* has been *RENEWED*.\n" +
                "New End Date: " + contract.getEndDate() + "\n\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }

    public void notifyOwnerContractEndingSoon(Owner owner, Contract contract) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "The contract for apartment *" + contract.getReservation().getApartment().getApartmentNumber() + "* will end in 30 days.\n" +
                "End Date: " + contract.getEndDate() + "\n" +
                "Tenant: " + contract.getReservation().getUser().getFullName() + "\n\n" +
                "Please review the renewal or move-out arrangements.\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }

    public void notifyOwnerNewMaintenanceRequest(Owner owner, MaintenanceRequest maintenanceRequest) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "A new maintenance request was submitted for apartment *" + maintenanceRequest.getApartment().getApartmentNumber() + "*.\n" +
                "Issue: " + maintenanceRequest.getDescription() + "\n" +
                "Priority: *" + maintenanceRequest.getPriority() + "*\n\n" +
                "Please log in to review the request.\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }

    public void notifyRoommateRequestReceived(User receiver, User sender) {
        String message = "Hello " + receiver.getFullName() + ",\n\n" +
                "You received a new roommate request from *" + sender.getFullName() + "*.\n\n" +
                "Please log in to accept or reject the request.\n" +
                "Smart Rental Platform";
        sendMessage(receiver.getPhoneNumber(), message);
    }

    public void notifyRoommateRequestAccepted(User sender, User receiver) {
        String message = "Hello " + sender.getFullName() + ",\n\n" +
                "Your roommate request was *ACCEPTED* by " + receiver.getFullName() + ".\n" +
                "You are now linked as roommates.\n\n" +
                "Smart Rental Platform";
        sendMessage(sender.getPhoneNumber(), message);
    }

    public void notifyRoommateRequestRejected(User sender, User receiver) {
        String message = "Hello " + sender.getFullName() + ",\n\n" +
                "Your roommate request was *REJECTED* by " + receiver.getFullName() + ".\n\n" +
                "You can continue searching for other roommate matches.\n" +
                "Smart Rental Platform";
        sendMessage(sender.getPhoneNumber(), message);
    }

    public void notifyOwnerNewReview(Owner owner, Apartment apartment, Review review) {
        String message = "Hello " + owner.getFullName() + ",\n\n" +
                "A new review has been posted on apartment *" + apartment.getApartmentNumber() + "*.\n" +
                "Rating: " + review.getRating() + "/5\n" +
                "Comment: " + review.getComment() + "\n\n" +
                "Smart Rental Platform";
        sendMessage(owner.getPhoneNumber(), message);
    }
}
