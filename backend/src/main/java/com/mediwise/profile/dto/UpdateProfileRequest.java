package com.mediwise.profile.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private LocalDate dob;
    private String bloodType;
    private String gender;
    private JsonNode address;
    private JsonNode emergencyContact;

    public String getAddressAsString() {
        if (address == null || address.isNull()) return null;
        if (address.isTextual()) return address.asText();
        return address.toString();
    }

    public String getEmergencyContactAsString() {
        if (emergencyContact == null || emergencyContact.isNull()) return null;
        if (emergencyContact.isTextual()) return emergencyContact.asText();
        return emergencyContact.toString();
    }
}
