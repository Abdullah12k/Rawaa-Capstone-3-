package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.UserDTOIn;
import com.example.capstone3.DTO.Out.UserDTOOut;
import com.example.capstone3.Models.User;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTOOut> getAll() {
        List<UserDTOOut> userDTOOuts = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            userDTOOuts.add(convertToDTO(user));
        }
        return userDTOOuts;
    }

    public UserDTOOut getUser(Integer id) {
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new ApiException("User not found");
        }
        return convertToDTO(user);
    }

    public void addUser(UserDTOIn userDTOIn) {
        User user = new User();
        user.setFullName(userDTOIn.getFullName());
        user.setEmail(userDTOIn.getEmail());
        user.setPhoneNumber(userDTOIn.getPhoneNumber());
        user.setDateOfBirth(userDTOIn.getDateOfBirth());
        user.setFamilyCount(userDTOIn.getFamilyCount());
        user.setGender(userDTOIn.getGender());
        user.setChildrenCount(userDTOIn.getChildrenCount());
        userRepository.save(user);
    }

    public void updateUser(Integer id, UserDTOIn userDTOIn) {
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new ApiException("User not found");
        }
        user.setFullName(userDTOIn.getFullName());
        user.setEmail(userDTOIn.getEmail());
        user.setPhoneNumber(userDTOIn.getPhoneNumber());
        user.setDateOfBirth(userDTOIn.getDateOfBirth());
        user.setFamilyCount(userDTOIn.getFamilyCount());
        user.setGender(userDTOIn.getGender());
        user.setChildrenCount(userDTOIn.getChildrenCount());
        userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new ApiException("User not found");
        }
        userRepository.deleteById(id);
    }

    public UserDTOOut convertToDTO(User user) {
        UserDTOOut userDTOOut = new UserDTOOut();
        userDTOOut.setId(user.getId());
        userDTOOut.setFullName(user.getFullName());
        userDTOOut.setEmail(user.getEmail());
        userDTOOut.setPhoneNumber(user.getPhoneNumber());
        userDTOOut.setDateOfBirth(user.getDateOfBirth());
        userDTOOut.setFamilyCount(user.getFamilyCount());
        userDTOOut.setChildrenCount(user.getChildrenCount());
        userDTOOut.setCreatedAt(user.getCreatedAt());
        return userDTOOut;
    }


    //^^^^^^^CRUD^^^^^^^^


}
