package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.OwnerDTOIn;
import com.example.capstone3.DTO.Out.OwnerDTOOut;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public List<OwnerDTOOut> getAll() {
        List<OwnerDTOOut> ownerDTOOuts = new ArrayList<>();
        for (Owner owner : ownerRepository.findAll()) {
            ownerDTOOuts.add(convertToDTO(owner));
        }
        return ownerDTOOuts;
    }

    public OwnerDTOOut getOwner(Integer id) {
        Owner owner = ownerRepository.findOwnerById(id);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        return convertToDTO(owner);
    }

    public void addOwner(OwnerDTOIn ownerDTOIn) {
        Owner owner = new Owner();
        owner.setFullName(ownerDTOIn.getFullName());
        owner.setEmail(ownerDTOIn.getEmail());
        owner.setPhoneNumber(ownerDTOIn.getPhoneNumber());
        owner.setCommercialRegistrationNumber(ownerDTOIn.getCommercialRegistrationNumber());
        ownerRepository.save(owner);
    }

    public void updateOwner(Integer id, OwnerDTOIn ownerDTOIn) {
        Owner owner = ownerRepository.findOwnerById(id);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        owner.setFullName(ownerDTOIn.getFullName());
        owner.setEmail(ownerDTOIn.getEmail());
        owner.setPhoneNumber(ownerDTOIn.getPhoneNumber());
        owner.setCommercialRegistrationNumber(ownerDTOIn.getCommercialRegistrationNumber());
        ownerRepository.save(owner);
    }

    public void deleteOwner(Integer id) {
        Owner owner = ownerRepository.findOwnerById(id);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        ownerRepository.deleteById(id);
    }

    public OwnerDTOOut convertToDTO(Owner owner) {
        OwnerDTOOut ownerDTOOut = new OwnerDTOOut();
        ownerDTOOut.setId(owner.getId());
        ownerDTOOut.setFullName(owner.getFullName());
        ownerDTOOut.setEmail(owner.getEmail());
        ownerDTOOut.setPhoneNumber(owner.getPhoneNumber());
        ownerDTOOut.setCommercialRegistrationNumber(owner.getCommercialRegistrationNumber());
        return ownerDTOOut;
    }


    //^^^^^^^CRUD^^^^^^^^


}
