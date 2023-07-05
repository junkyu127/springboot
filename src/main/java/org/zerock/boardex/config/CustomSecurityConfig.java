package org.zerock.boardex.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.zerock.boardex.security.CustomUserDetailsService;
import org.zerock.boardex.security.handler.Custom403Handler;
import org.zerock.boardex.security.handler.CustomSocialLoginSuccessHandler;

import javax.sql.DataSource;

@Log4j2
@Configuration // 환경설정파일을 만듦
@RequiredArgsConstructor // final이 붙거나 @NotNull 이 붙은 필드의 생성자를 자동 생성해주는 lombok annotation
@EnableGlobalMethodSecurity(prePostEnabled = true) // 권한 설정
public class CustomSecurityConfig {
    private final DataSource dataSource;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomSocialLoginSuccessHandler(passwordEncoder());
    }
    @Bean // 수동으로 Bean 등록
    // SecurityFilterChain : 스프링 부트에서 지원하는 기본 로그인 화면으로 접속하지 못하게 함.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("****** configure ******");
        http.formLogin().loginPage("/member/login"); // 사용자 인증 처리
        http.csrf().disable(); // csrf 토큰 비활성화 (아이디와 비밀번호 사용하여 로그인)
        // rememberMe() : 자동로그인
        // key() : 인증받은 사용자의 정보로 token 생성, key 값은 임의로 설정함.
        // token : 서버가 각각의 사용자를 구별하도록 고유한 정보를 담은 암호화 데이터
        // tokenRepository() : rememberMe 의 토큰 저장소
        // userDetailsService() : 유저의 정보를 가져오는 인터페이스
        // tokenValiditySeconds() : 생성된 token 의 만료시간(30일)
        http.rememberMe()
                .key("12345678")
                .tokenRepository(persistentTokenRepository())
                .userDetailsService(customUserDetailsService)
                .tokenValiditySeconds(60*60*24*30);
        // exceptioonHandling() : Error 발생 시 후 처리 설정
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        // 카카오 로그인하기
        http.oauth2Login().loginPage("/member/login").successHandler(authenticationSuccessHandler());
        return http.build();
    }

    @Bean
    // WebSecurityCustomizer : 적용하지 않을 리소스를 설정
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("****** web configure ******");
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        // 쿠키 정보를 테이블로 설정
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    // 정식 사용자가 아닌데 게시글 수정하려고 접근할 때 서버에서 접근 거부하면 예외처리함
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new Custom403Handler();
    }
}
