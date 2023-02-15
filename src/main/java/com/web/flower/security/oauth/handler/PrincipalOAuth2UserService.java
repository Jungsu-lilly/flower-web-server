//package com.web.flower.security.config.oauth;
//
//import com.web.flower.domain.user.entity.User;
//import com.web.flower.domain.user.repository.UserRepository;
//import com.web.flower.security.config.auth.PrincipalDetails;
//import com.web.flower.security.config.oauth.provider.KakaoUserInfo;
//import com.web.flower.security.config.oauth.provider.NaverUserInfo;
//import com.web.flower.security.config.oauth.provider.OAuth2UserInfo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class PrincipalOAuth2UserService extends DefaultOAuth2UserService {
//
//    @Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    public PrincipalOAuth2UserService(BCryptPasswordEncoder bCryptPasswordEncoder) {
//        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
//    }
//
//    // userRequest 는 code를 받아서 accessToken을 응답 받은 객체
//    // 구글로부터 받은 userRequest 데이터에 대한 후처리되는 함수
//    // 코드를 통해서 엑세스 토큰을 요청받아서 토큰을 통해 사용자프로필 정보까지 받은 정보가 userRequest 에 리턴됨.
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//
//        System.out.println("=== PrincipalOAuth2UserService:loadUser() 실행 =======");
//        // code를 통해 구성한 정보
//        System.out.println("userRequest clientRegistration : " + userRequest.getClientRegistration());
//        // 구글로그인 버튼 클릭 -> 구글 로그인창 -> 로그인 완료 -> code리턴(OAuth-Client라이브러리가 받음) -> AccessToken 요청
//        // userRequest 정보 -> loadUser 함수호출 -> 구글로부터 회원프로필 받아준다.
//
//        OAuth2User oAuth2User = super.loadUser(userRequest); // 구글의 회원프로필 조회
//        // Access_Token 을 통해 응답받은 회원정보
//        System.out.println("getAttributes : " + oAuth2User.getAttributes());
//
//        // 회원가입을 강제로 진행
//        OAuth2UserInfo oAuth2UserInfo= null;
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//
//        if(registrationId.equals("naver")){
//            System.out.println("네이버 로그인 요청");
//            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
//        }else if(registrationId.equals("kakao")){
//            System.out.println("카카오 로그인 요청");
//            Map<String, Object> attributes = oAuth2User.getAttributes();
//            oAuth2UserInfo = new KakaoUserInfo(attributes);
//            System.out.println("EMAIL : " + oAuth2UserInfo.getEmail());
//            System.out.println("NAME : " + oAuth2UserInfo.getUsername());
//        }
//
//        String provider = oAuth2UserInfo.getProvider();
//        String providerId = oAuth2UserInfo.getProviderId();
//
//        // oauth로 로그인하면 사실 username, password는 의미없음. 그냥 만들어주는것
//        String username = registrationId +"_"+providerId; // google_109832018212431240
//        String password = bCryptPasswordEncoder.encode("꽃물");
//
//        // 이미 존재하는 회원인지 체크
//        User userEntity = userRepository.findByUsername(username);
//        if(userEntity == null){
//            userEntity = User.builder()
//                    .id(UUID.randomUUID())
//                    .username(username)
//                    .password(password)
//                    .email(oAuth2UserInfo.getEmail())
//                    .role("ROLE_USER")
//                    .provider(registrationId)
//                    .providerId(oAuth2UserInfo.getProviderId())
//                    .profileImagePath(oAuth2UserInfo.getProfileImagePath())
//                    .createdAt(LocalDateTime.now())
//                    .build();
//            userRepository.save(userEntity);
//        }
//
//        // Authentication 객체 안에 들어가 있음
//        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
//    }
//}
