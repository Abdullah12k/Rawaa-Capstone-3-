package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.UserPreferenceDTOIn;
import com.example.capstone3.DTO.In.WorkplaceDTOIn;
import com.example.capstone3.DTO.Out.NominatimLocationDTOOut;
import com.example.capstone3.DTO.Out.UserPreferenceDTOOut;
import com.example.capstone3.Enums.PreferenceLevel;
import com.example.capstone3.Models.User;
import com.example.capstone3.Models.UserPreference;
import com.example.capstone3.Repository.UserPreferenceRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    private final NominatimService nominatimService;

    public List<UserPreferenceDTOOut> getAll() {
        List<UserPreferenceDTOOut> userPreferenceDTOOuts = new ArrayList<>();
        for (UserPreference preference : userPreferenceRepository.findAll()) {
            userPreferenceDTOOuts.add(convertToDTO(preference));
        }
        return userPreferenceDTOOuts;
    }

    public UserPreferenceDTOOut getUserPreference(Integer id) {
        UserPreference preference = userPreferenceRepository.findUserPreferenceById(id);
        if (preference == null) {
            throw new ApiException("User preference not found");
        }
        return convertToDTO(preference);
    }

    public void addUserPreference(Integer userId, UserPreferenceDTOIn userPreferenceDTOIn) {
        if ((userPreferenceDTOIn.getWorkLatitude() == null)
                != (userPreferenceDTOIn.getWorkLongitude() == null)) {
            throw new ApiException(
                    "Work latitude and longitude must be provided together");
        }
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (userPreferenceRepository.findUserPreferenceByUserId(user.getId()) != null) {
            throw new ApiException("User preferences already exist");
        }
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setWorkLatitude(userPreferenceDTOIn.getWorkLatitude());
        preference.setWorkLongitude(userPreferenceDTOIn.getWorkLongitude());
        preference.setMaxBudget(userPreferenceDTOIn.getMaxBudget());
        preference.setRequiresParking(userPreferenceDTOIn.getRequiresParking());
        preference.setRequiresElevator(userPreferenceDTOIn.getRequiresElevator());
        preference.setRequiresFurnished(userPreferenceDTOIn.getRequiresFurnished());
        preference.setGymPreference(userPreferenceDTOIn.getGymPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getGymPreference());
        preference.setCafesPreference(userPreferenceDTOIn.getCafesPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getCafesPreference());
        preference.setHospitalPreference(userPreferenceDTOIn.getHospitalPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getHospitalPreference());
        preference.setSchoolPreference(userPreferenceDTOIn.getSchoolPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getSchoolPreference());
        preference.setPublicTransportPreference(userPreferenceDTOIn.getPublicTransportPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getPublicTransportPreference());
        preference.setLookingForRoommate(userPreferenceDTOIn.getLookingForRoommate());
        preference.setRoommateBudget(userPreferenceDTOIn.getRoommateBudget());
        preference.setPreferredBedrooms(userPreferenceDTOIn.getPreferredBedrooms());
        preference.setPreferredBathrooms(userPreferenceDTOIn.getPreferredBathrooms());
        preference.setPreferredDistrict(userPreferenceDTOIn.getPreferredDistrict());
        userPreferenceRepository.save(preference);
    }

    public void updateUserPreference(Integer id, UserPreferenceDTOIn userPreferenceDTOIn) {
        if ((userPreferenceDTOIn.getWorkLatitude() == null)
                != (userPreferenceDTOIn.getWorkLongitude() == null)) {
            throw new ApiException(
                    "Work latitude and longitude must be provided together");
        }
        UserPreference preference = userPreferenceRepository.findUserPreferenceById(id);
        if (preference == null) {
            throw new ApiException("User preference not found");
        }
        if (userPreferenceDTOIn.getWorkLatitude() != null) {
            preference.setWorkLatitude(userPreferenceDTOIn.getWorkLatitude());
            preference.setWorkLongitude(userPreferenceDTOIn.getWorkLongitude());
        }
        preference.setMaxBudget(userPreferenceDTOIn.getMaxBudget());
        preference.setRequiresParking(userPreferenceDTOIn.getRequiresParking());
        preference.setRequiresElevator(userPreferenceDTOIn.getRequiresElevator());
        preference.setRequiresFurnished(userPreferenceDTOIn.getRequiresFurnished());
        preference.setGymPreference(userPreferenceDTOIn.getGymPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getGymPreference());
        preference.setCafesPreference(userPreferenceDTOIn.getCafesPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getCafesPreference());
        preference.setHospitalPreference(userPreferenceDTOIn.getHospitalPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getHospitalPreference());
        preference.setSchoolPreference(userPreferenceDTOIn.getSchoolPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getSchoolPreference());
        preference.setPublicTransportPreference(userPreferenceDTOIn.getPublicTransportPreference() == null
                ? PreferenceLevel.NOT_IMPORTANT : userPreferenceDTOIn.getPublicTransportPreference());
        preference.setLookingForRoommate(userPreferenceDTOIn.getLookingForRoommate());
        preference.setRoommateBudget(userPreferenceDTOIn.getRoommateBudget());
        preference.setPreferredBedrooms(userPreferenceDTOIn.getPreferredBedrooms());
        preference.setPreferredBathrooms(userPreferenceDTOIn.getPreferredBathrooms());
        preference.setPreferredDistrict(userPreferenceDTOIn.getPreferredDistrict());
        userPreferenceRepository.save(preference);
    }

    public void deleteUserPreference(Integer id) {
        UserPreference preference = userPreferenceRepository.findUserPreferenceById(id);
        if (preference == null) {
            throw new ApiException("User preference not found");
        }
        userPreferenceRepository.deleteById(id);
    }

    public UserPreferenceDTOOut getUserPreferenceByUserId(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        UserPreference preference = userPreferenceRepository.findUserPreferenceByUserId(userId);
        if (preference == null) {
            throw new ApiException("User preference not found");
        }
        return convertToDTO(preference);
    }

    public UserPreferenceDTOOut updateWorkplace(Integer userId, WorkplaceDTOIn workplaceDTOIn) {
        UserPreference preference = userPreferenceRepository.findUserPreferenceByUserId(userId);
        if (preference == null) {
            throw new ApiException("User preferences not found. Please create preferences first.");
        }

        // Convert the workplace name to coordinates before saving the preference.
        NominatimLocationDTOOut location =
                nominatimService.resolveWorkplaceCoordinates(workplaceDTOIn.getWorkplaceName());

        preference.setWorkplaceName(workplaceDTOIn.getWorkplaceName().trim());
        preference.setWorkLatitude(location.getLatitude());
        preference.setWorkLongitude(location.getLongitude());
        userPreferenceRepository.save(preference);

        return convertToDTO(preference);
    }

    public UserPreferenceDTOOut convertToDTO(UserPreference preference) {
        UserPreferenceDTOOut userPreferenceDTOOut = new UserPreferenceDTOOut();
        userPreferenceDTOOut.setId(preference.getId());
        userPreferenceDTOOut.setUserId(preference.getUser().getId());
        userPreferenceDTOOut.setWorkLatitude(preference.getWorkLatitude());
        userPreferenceDTOOut.setWorkLongitude(preference.getWorkLongitude());
        userPreferenceDTOOut.setWorkplaceName(preference.getWorkplaceName());
        userPreferenceDTOOut.setMaxBudget(preference.getMaxBudget());
        userPreferenceDTOOut.setRequiresParking(preference.getRequiresParking());
        userPreferenceDTOOut.setRequiresElevator(preference.getRequiresElevator());
        userPreferenceDTOOut.setRequiresFurnished(preference.getRequiresFurnished());
        userPreferenceDTOOut.setGymPreference(preference.getGymPreference());
        userPreferenceDTOOut.setCafesPreference(preference.getCafesPreference());
        userPreferenceDTOOut.setHospitalPreference(preference.getHospitalPreference());
        userPreferenceDTOOut.setSchoolPreference(preference.getSchoolPreference());
        userPreferenceDTOOut.setPublicTransportPreference(preference.getPublicTransportPreference());
        userPreferenceDTOOut.setPreferredBedrooms(preference.getPreferredBedrooms());
        userPreferenceDTOOut.setPreferredBathrooms(preference.getPreferredBathrooms());
        userPreferenceDTOOut.setPreferredDistrict(preference.getPreferredDistrict());
        userPreferenceDTOOut.setLookingForRoommate(preference.getLookingForRoommate());
        userPreferenceDTOOut.setRoommateBudget(preference.getRoommateBudget());
        return userPreferenceDTOOut;
    }

    //^^^^^^^CRUD^^^^^^^^


}
