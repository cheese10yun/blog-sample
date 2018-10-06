package blog.oauth2.socialsample.security;

import blog.oauth2.socialsample.social.ClientResources;
import blog.oauth2.socialsample.user.SocialType;

import blog.oauth2.socialsample.user.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableOAuth2Client
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private OAuth2ClientContext oAuth2ClientContext;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();

        //@formatter:off
            http
                .authorizeRequests()
                    .antMatchers("/", "/login/**",  "/css/**", "/images/**", "/js/**", "/console/**").permitAll()
                    .anyRequest().authenticated()
                .and()
                    // headers 응답에 해당하는 header를 지정합니다. disable를 적용하면 XFrameOptionsHeaderWriter의 최적화 설정을 허용허지 않습니다.
                    .headers().frameOptions().disable()
                .and()
                    .exceptionHandling()
                    // 인증 진입 지정입니다. 인증되지 않은 사용자가 허용되지 않으 경로로 리퀘스트 요청을 할경우 /login 으로 이동 됩니다.
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .and()
                    // 로그인에 성공하면 설정된 경로로 포워딩됩니다.
                    .formLogin()
                    .successForwardUrl("/board/list")
                .and()
                    // 로그아웃에 성공했을 경우 포워딩될 URL, 로그아웃 수행때 삭제될 쿠키 값, 설정된 세션 무효화를 수행하게 설정
                    .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .deleteCookies("SESSION")
                    .invalidateHttpSession(true)
                .and()
                    //첫번째 인자보다 먼저 시작될 필터를 등록합니다.
                    .addFilterBefore(filter, CsrfFilter.class)
                    .addFilterBefore(oauth2Filter(), BasicAuthenticationFilter.class)
                    .csrf().disable();
        //@formatter:on
    }


    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("google")
    public ClientResources google() {
        return new ClientResources();
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    private Filter oauth2Filter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(oauth2Filter(facebook(), "/login/facebook", SocialType.FACEBOOK));
        filters.add(oauth2Filter(google(), "/login/google", SocialType.GOOGLE));
        filter.setFilters(filters);
        return filter;
    }

    private Filter oauth2Filter(ClientResources client, String path, SocialType socialType) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);
        StringBuilder redirectUrl = new StringBuilder("/");
        redirectUrl.append(socialType.getValue()).append("/complete");

        filter.setRestTemplate(template);
        filter.setTokenServices(new UserTokenService(client, socialType));
        filter.setAuthenticationSuccessHandler((request, response, authentication) -> response.sendRedirect(redirectUrl.toString()));
        filter.setAuthenticationFailureHandler((request, response, exception) -> response.sendRedirect("/error"));
        return filter;
    }
}
