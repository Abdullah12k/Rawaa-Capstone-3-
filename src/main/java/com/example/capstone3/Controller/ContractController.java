package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.Config.AuthPrincipal;
import com.example.capstone3.DTO.In.ContractDTOIn;
import com.example.capstone3.Service.ContractService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/get")
    public ResponseEntity<?> getContracts() {
        return ResponseEntity.status(200).body(contractService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getContract(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(contractService.getContract(id));
    }

    @GetMapping("/get/{id}/pdf")
    public ResponseEntity<byte[]> getContractPdf(@PathVariable Integer id) throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Rawaa-Contract-" + id + ".pdf")
                .body(contractService.generateContractPdf(id));
    }

    @PostMapping("/add/{ownerId}/{reservation_id}")
    public ResponseEntity<?> addContract(@RequestBody @Valid ContractDTOIn contractDTOIn,
                                         @PathVariable Integer ownerId,
                                         @PathVariable Integer reservation_id,
                                         @AuthenticationPrincipal AuthPrincipal owner) {
        contractService.addContract(contractDTOIn, owner.requireOwner(ownerId), reservation_id);
        return ResponseEntity.status(200).body(
                new ApiResponse("Contract created successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateContract(@PathVariable Integer id, @RequestBody @Valid ContractDTOIn contractDTOIn) {
        contractService.updateContract(id, contractDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Contract updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteContract(@PathVariable Integer id) {
        contractService.deleteContract(id);
        return ResponseEntity.status(200).body(new ApiResponse("Contract deleted successfully"));
    }

    @PutMapping("/accept/{contractId}/{userId}")
    public ResponseEntity<?> acceptContract(@PathVariable Integer userId, @PathVariable Integer contractId, @AuthenticationPrincipal AuthPrincipal renter) {
        contractService.acceptContract(renter.requireUser(userId), contractId);
        return ResponseEntity.status(200).body(new ApiResponse("Contract accepted and activated successfully."));
    }

    @PutMapping("/reject/{contractId}/{userId}")
    public ResponseEntity<?> rejectContract(@PathVariable Integer userId, @PathVariable Integer contractId, @AuthenticationPrincipal AuthPrincipal renter) {
        contractService.rejectContract(renter.requireUser(userId), contractId);
        return ResponseEntity.status(200).body(new ApiResponse("Contract rejected and cancelled successfully."));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getContractsByUserId(@PathVariable Integer userId, @AuthenticationPrincipal AuthPrincipal renter) {
        return ResponseEntity.status(200).body(contractService.getContractsByUserId(renter.requireUser(userId)));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getContractsByOwnerId(@PathVariable Integer ownerId, @AuthenticationPrincipal AuthPrincipal owner) {
        return ResponseEntity.status(200).body(contractService.getContractsByOwnerId(owner.requireOwner(ownerId)));
    }

    @PutMapping("/end/{contractId}/{ownerId}")
    public ResponseEntity<?> endContract(@PathVariable Integer ownerId, @PathVariable Integer contractId, @AuthenticationPrincipal AuthPrincipal owner) {
        contractService.endContract(owner.requireOwner(ownerId), contractId);
        return ResponseEntity.status(200).body(new ApiResponse("Contract officially ended successfully."));
    }

    @PutMapping("/terminate/{contractId}/{ownerId}")
    public ResponseEntity<?> terminateContract(@PathVariable Integer ownerId, @PathVariable Integer contractId,
                                               @RequestParam String reason, @AuthenticationPrincipal AuthPrincipal owner) {
        contractService.terminateContract(owner.requireOwner(ownerId), contractId, reason);
        return ResponseEntity.status(200).body(new ApiResponse("Contract terminated successfully."));
    }

    @PutMapping("/renew/{contractId}/{userId}")
    public ResponseEntity<?> renewContract(@PathVariable Integer userId, @PathVariable Integer contractId, @RequestParam Integer extraMonths, @AuthenticationPrincipal AuthPrincipal renter) {
        contractService.requestRenewal(renter.requireUser(userId), contractId, extraMonths);
        return ResponseEntity.status(200).body(new ApiResponse("Renewal request submitted successfully."));
    }

    @PutMapping("/renew/approve/{contractId}/{ownerId}")
    public ResponseEntity<?> approveRenewal(@PathVariable Integer ownerId,
                                            @PathVariable Integer contractId, @AuthenticationPrincipal AuthPrincipal owner) {
        contractService.approveRenewal(owner.requireOwner(ownerId), contractId);
        return ResponseEntity.status(200).body(new ApiResponse("Contract renewal approved."));
    }

    @PutMapping("/renew/reject/{contractId}/{ownerId}")
    public ResponseEntity<?> rejectRenewal(@PathVariable Integer ownerId,
                                           @PathVariable Integer contractId, @AuthenticationPrincipal AuthPrincipal owner) {
        contractService.rejectRenewal(owner.requireOwner(ownerId), contractId);
        return ResponseEntity.status(200).body(new ApiResponse("Contract renewal rejected."));
    }

    @GetMapping("/analyze/{contractId}/{userId}")
    public ResponseEntity<?> getContractAnalysis(@PathVariable Integer userId, @PathVariable Integer contractId, @RequestParam(defaultValue = "EN") String language, @AuthenticationPrincipal AuthPrincipal renter) {
        return ResponseEntity.status(200).body(contractService.getContractAnalysis(renter.requireUser(userId), contractId, language));
    }

    @PostMapping("/{contractId}/send-pdf")
    public ResponseEntity<?> sendContractPdfEmail(@PathVariable Integer contractId) throws IOException, MessagingException {
        contractService.generateAndEmailContractPdf(contractId);
        return ResponseEntity.status(200).body(new ApiResponse("Contract PDF has been successfully generated and sent via email."));
    }


}
