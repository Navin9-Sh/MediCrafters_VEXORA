package com.mediwise.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigResponse {
    private String appName;
    private String version;
    private String status;
    private String environment;
    private List<String> features;
}
