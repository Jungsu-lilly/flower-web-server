package com.web.flower.security.oauth.controller;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import com.web.flower.security.auth.PrincipalDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/test/login")
	public @ResponseBody String testLogin(Authentication authentication,
										  @AuthenticationPrincipal PrincipalDetails userDetails){
		System.out.println("test/login =================");
		System.out.println("authentication : " + authentication.getPrincipal());
		PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
		System.out.println("authentication : " + principalDetails.getUser());
		System.out.println("userDetails = " + userDetails.getUser());

		return "세션 정보 확인하기";
	}

	// 일반적으로 로그인하면 Authentication(UserDetails)
	// oauth2 로그인하면 Authentication(OAuth2User)
	@GetMapping("/test/oauth/login")
	public @ResponseBody String testOAuthLogin(Authentication authentication,
											   @AuthenticationPrincipal OAuth2User oauth){
		System.out.println("test/oauth/login =================");
		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
		System.out.println("authentication : " + oauth2User.getAttributes());
		System.out.println("oauth2User = " + oauth.getAttributes());

		return "OAuth 세션 정보 확인하기";
	}

	@GetMapping({ "", "/" })
	public @ResponseBody String index() {
		return "인덱스 페이지입니다.";
	}

	// oAuth 로그인을 해도 PrincipalDetails, 일반 로그인을 해도 PrincipalDetails
	// 어노테이션은 언제 활성화되는가?
	@GetMapping("/user")
	public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails) {
		System.out.println("/user =================");
		System.out.println("PrincipalDetails : " + principalDetails);
		System.out.println("provider : " + principalDetails.getUser().getProvider());

		return "유저 페이지입니다.";
	}

	@GetMapping("/admin")
	public @ResponseBody String admin() {
		return "어드민 페이지입니다.";
	}

	//@PostAuthorize("hasRole('ROLE_MANAGER')")
	//@PreAuthorize("hasRole('ROLE_MANAGER')")
	@Secured("ROLE_MANAGER")
	@GetMapping("/manager")
	public @ResponseBody String manager() {
		return "매니저 페이지입니다.";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/join")
	public String join() {
		return "join";
	}

	@PostMapping("/joinProc")
	public String joinProc(User user) {
		System.out.println("회원가입 진행 : " + user);
		String rawPassword = user.getPassword();
		String encPassword = bCryptPasswordEncoder.encode(rawPassword);
		user.setPassword(encPassword);
		user.setRole("ROLE_USER");
		userRepository.save(user);
		return "redirect:/";
	}
}
