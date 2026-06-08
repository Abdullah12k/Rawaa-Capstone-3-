package com.example.capstone3.Controller;

import com.example.capstone3.DTO.Out.ApartmentServicesDTOOut;
import com.example.capstone3.Service.OverpassLocationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationAnalysisController {

    private final OverpassLocationService overpassLocationService;

    // Uses Overpass to count nearby services around the supplied coordinates.
    @GetMapping("/analyze")
    public ApartmentServicesDTOOut analyzeArea(@RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @RequestParam(defaultValue = "3000") @Min(100) @Max(10000) Integer radiusMetres) {
        return overpassLocationService.analyzeApartmentLocation(latitude, longitude, radiusMetres);
    }
}
