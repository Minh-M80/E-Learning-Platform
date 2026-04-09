package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.response.GoogleTokenResponse;
import com.example.E_Learning_Platform.dto.response.GoogleUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    @Value("${outbound.identity.client-id}")
    private String clientId;

    @Value("${outbound.identity.client-secret}")
    private String clientSecret;

    @Value("${outbound.identity.redirect-uri}")
    private String redirectUri;

    @Value("${outbound.identity.token-uri}")
    private String tokenUri;

    @Value("${outbound.identity.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateGoogleLoginUrl() {
        return UriComponentsBuilder
                .fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent select_account")
                .build()
                .toUriString();
    }

    public String getGoogleAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    tokenUri,
                    request,
                    GoogleTokenResponse.class
            );

            GoogleTokenResponse responseBody = response.getBody();
            if (responseBody == null || responseBody.getAccessToken() == null) {
                log.error("Google token response is empty. status={}", response.getStatusCode());
                throw new RuntimeException("Khong nhan duoc Access Token tu Google");
            }

            return responseBody.getAccessToken();
        } catch (RestClientResponseException e) {
            log.error(
                    "Google token exchange failed. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            throw new RuntimeException(
                    "Google Authorization Code khong hop le, da het han hoac da bi su dung",
                    e
            );
        } catch (Exception e) {
            log.error("Loi khi lay Access Token tu Google", e);
            throw new RuntimeException("Khong the lay Access Token tu Google", e);
        }
    }

    public GoogleUserResponse getGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserResponse> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    entity,
                    GoogleUserResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Loi khi lay UserInfo tu Google", e);
            throw new RuntimeException("Khong the lay thong tin nguoi dung tu Google", e);
        }
    }
}
