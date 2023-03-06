package com.web.flower.domain.social_login.service;

import com.web.flower.domain.social_login.dto.SocialLoginReqDto;
import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.domain.social_login.dto.LoginResponse;
import com.web.flower.domain.social_login.dto.OAuthTokenResponse;
import com.web.flower.domain.social_login.provider.KakaoUserInfo;
import com.web.flower.domain.social_login.provider.OAuth2UserInfo;
import com.web.flower.domain.refresh_token.entity.RefreshToken;
import com.web.flower.domain.refresh_token.repository.RefreshTokenRepository;
import com.web.flower.utils.JwtUtils;
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

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialLoginService {
    private static final String BEARER_TYPE = "Bearer";

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final UserRepository userRepository;
    
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtils jwtUtils;

    @Transactional
    public LoginResponse socialLogin(SocialLoginReqDto req){
        String providerName = req.getProvider();
        String code = req.getCode();

        ClientRegistration provider = clientRegistrationRepository.findByRegistrationId(providerName);
        // code -> access token 으로 인가서버에 교환요청
        OAuthTokenResponse tokenResponse = getToken(code, provider);

        // access token을 인가서버 userInfo 엔드포인트에 넘겨서 유저 정보를 가져옴
        Map<String, Object> userAttributes = WebClient.create()
                .get()
                .uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(header-> header.setBearerAuth(tokenResponse.getAccess_token()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        OAuth2UserInfo oauth2UserInfo = null;

        if(providerName.equals("kakao")){
            oauth2UserInfo = new KakaoUserInfo(userAttributes);
        }else{
            log.info("허용되지 않은 접근입니다.");
        }

        providerName = oauth2UserInfo.getProvider();

        // oauth로 로그인하면 사실 username, password는 의미없음. 그냥 만들어주는것
        String username = providerName +"_"+oauth2UserInfo.getProviderId(); // kakao_123452134qwewe123123
        String password = UUID.randomUUID().toString();

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
        String accessToken = jwtUtils.createAccessToken(userEntity);
        String refreshToken = jwtUtils.createRefreshToken(userEntity);

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

        return LoginResponse.builder()
                .id(userEntity.getId())
                .name(userEntity.getUsername())
                .imageUrl(userEntity.getProfileImagePath())
                .role(userEntity.getRole())
                .tokenType(BEARER_TYPE)
                .accessToken(accessToken)
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
