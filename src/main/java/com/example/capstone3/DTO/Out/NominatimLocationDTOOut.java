package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"displayName", "latitude", "longitude"})
@Data
public class NominatimLocationDTOOut {

    private String displayName;
    private Double latitude;
    private Double longitude;
}
