package com.web.flower.security.oauth.service;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.oauth.dto.LoginResponse;
import com.web.flower.security.oauth.dto.OAuthTokenResponse;
import com.web.flower.security.oauth.provider.KakaoUserInfo;
import com.web.flower.security.oauth.provider.NaverUserInfo;
import com.web.flower.security.oauth.provider.OAuth2UserInfo;
import com.web.flower.domain.jwt.entity.RefreshToken;
import com.web.flower.domain.jwt.repository.RefreshTokenRepository;
import com.web.flower.domain.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.NonUniqueResultException;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OauthService {
    private static final String BEARER_TYPE = "Bearer";

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final UserRepository userRepository;
    
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtService jwtService;

    @Transactional
    public LoginResponse login(String providerName, String code){
        System.out.println("--- OauthService login 실행 -----");
        ClientRegistration provider = clientRegistrationRepository.findByRegistrationId(providerName);
        // code -> access token 으로 인가서버에 교환요청
        OAuthTokenResponse tokenResponse = getToken(code, provider);
        System.out.println("--- 인가서버 accessToken : " + tokenResponse.getAccess_token());

        // access token을 인가서버 userInfo 엔드포인트에 넘겨서 유저 정보를 가져옴
        Map<String, Object> userAttributes = WebClient.create()
                .get()
                .uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(header-> header.setBearerAuth(tokenResponse.getAccess_token()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        System.out.println("userAttributes = " + userAttributes);

        OAuth2UserInfo oauth2UserInfo = null;

        if(providerName.equals("kakao")){
            oauth2UserInfo = new KakaoUserInfo(userAttributes);
        }else if(providerName.equals("naver")){
            oauth2UserInfo = new NaverUserInfo(userAttributes);
        }else{
            log.info("허용되지 않은 접근입니다.");
        }

        providerName = oauth2UserInfo.getProvider();
        String providerId = oauth2UserInfo.getProviderId();

        // oauth로 로그인하면 사실 username, password는 의미없음. 그냥 만들어주는것
        String username = providerName +"_"+oauth2UserInfo.getEmail(); // kakao_wjdtb1235@naver.com
        String password = bCryptPasswordEncoder.encode("꽃물");

        // 이미 존재하는 회원인지 체크
        Optional<User> byUsername = userRepository.findByUsername(username);
        User userEntity = null;

        if(!byUsername.isPresent()){ // 회원이 가입되어 있지 않으면, 강제로 회원가입 진행
            userEntity = User.builder()
                    .id(UUID.randomUUID())
                    .username(username)
                    .password(password)
                    .role("ROLE_USER")
                    .provider(providerName)
                    .providerId(oauth2UserInfo.getProviderId())
                    .profileImagePath(oauth2UserInfo.getProfileImagePath())
                    .profileName(oauth2UserInfo.getProfileName())
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(userEntity);
        }else{ // 이미 존재한다면
            userEntity = byUsername.get();
        }

        // 새로운 access, refresh token 생성
        String accessToken1 = jwtService.createAccessToken(userEntity);
        String refreshToken = jwtService.createRefreshToken(userEntity);

        Optional<RefreshToken> byUserId = refreshTokenRepository.findByUserId(userEntity.getId());
        if(byUserId.isPresent()){ // 해당 사용자의 리프레쉬 토큰이 이미 있다면 삭제한다.
            refreshTokenRepository.delete(byUserId.get());
        }
        RefreshToken buildRefreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .value(refreshToken)
                .userId(userEntity.getId())
                .build();
        refreshTokenRepository.save(buildRefreshToken);

        RefreshToken save = refreshTokenRepository.save(buildRefreshToken);

        return LoginResponse.builder()
                .id(userEntity.getId())
                .name(userEntity.getUsername())
                .imageUrl(userEntity.getProfileImagePath())
                .role(userEntity.getRole())
                .tokenType(BEARER_TYPE)
                .accessToken(accessToken1)
                .build();
    }

    public OAuthTokenResponse getToken(String code, ClientRegistration provider){

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", provider.getClientId());
        formData.add("client_secret", provider.getClientSecret());
        formData.add("redirect_uri", provider.getRedirectUri());
        formData.add("code", code);

        String tokenUri = provider.getProviderDetails().getTokenUri();
        System.out.println("tokenUri = " + tokenUri);

        return WebClient.create()
                .post()
                .uri(provider.getProviderDetails().getTokenUri())
                .headers(header -> {
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(OAuthTokenResponse.class)
                .block();
    }
}
