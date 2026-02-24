package com.eazybrew.vend.nomba.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NombaTokenResponse {
    private String code;
    private NombaTokenData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NombaTokenData {
        private String access_token;
        private String refresh_token;
        private String expiresAt;
    }
}
