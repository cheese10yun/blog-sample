# Spring Security OAuth2 Social 설정
처음으로 배우는 스프링 부트2 예제 코드


```yml
facebook:
  client:
    clientId: <your-client-id>
    clientSecret: <your-secret>
    accessTokenUri: https://graph.facebook.com/oauth/access_token
    userAuthorizationUri: https://www.facebook.com/dialog/oauth
    tokenName: oauth_token
    authenticationScheme: query
    clientAuthenticationScheme: form
  resource:
    userInfoUri: https://graph.facebook.com/me?fields=id, name, email, picture

google :
  client :
    clientId : <your-client-id>
    clientSecret: <your-secret>
    accessTokenUri: https://www.googleapis.com/oauth2/v4/token
    userAuthorizationUri: https://accounts.google.com/o/oauth2/v2/auth
    clientAuthenticationScheme: form
    scope:
    - email
    - profile
  resource:
    userInfoUri: https://www.googleapis.com/oauth2/v3/userinfo
```


```java
public class ClientResources {
    @NestedConfigurationProperty
    private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

    @NestedConfigurationProperty
    private ResourceServerProperties resource = new ResourceServerProperties();

    public AuthorizationCodeResourceDetails getClient() {
        return client;
    }

    public ResourceServerProperties getResource() {
        return resource;
    }
}
```
* `@NestedConfigurationProperty`는 해당 필드가 단일값이 아닌 중복으로 바인딩된다고 표시하는 애노테이션입니다. 여러 소셜 미디어의 프로퍼티를 각각 바인딩하므로 해당 어노테이션 사용
* `AuthorizationCodeResourceDetails` 객체는 properties.yml 에서 설정한 값중 **`client`를 기준으로 key/value 값을 매핑해주는 대상 객체입니다.**
* `ResourceServerProperties` 객체는 OAuth2 리소스값을 매핑하는데 사용하지만 예제에서는 회우너 정보를 얻는 userInfoUri 값을 받는 데 사용 합니다.
  
```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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
}
```
* 소셜 미디어 리소스 정보는 시큐리티에서 사용하기 때문에 빈등록을 SecurityConfig에서 진행
* `ConfigurationProperties` 설정을 통해서 편하게 값바인딩 

```java
public class SecurityConfig extends WebSecurityConfigurerAdapter{
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

    ...
}
```

```java
....
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
        //(1)
        //인증이 수행될 경로를 넣어 OAuth2 클라이언트용 인증 처리 필터를 생성합니다.
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);
        //(2)
        // 권한 서버와 통신을 위해 OAuth2RestTemplate를 생성합니다. 이를 위해선 client 프로퍼티정보와 OAuth2ClientContext가 필요합니다.
        StringBuilder redirectUrl = new StringBuilder("/");
        redirectUrl.append(socialType.getValue()).append("/complete");
        filter.setRestTemplate(template);
        filter.setTokenServices(new UserTokenService(client, socialType));
        //(3)
        //User 권한을 최적화해서 생성하고자 UserInfoTokenService를 상속받은 UserTokenService를 생성했습니다.
        //OAuth2 AccessToken 검증을 위해 생성한 UserTokenService를 필터의 토큰 서비스로 등록합니다.
        filter.setAuthenticationSuccessHandler((request, response, authentication) -> response.sendRedirect(redirectUrl.toString()));
        //(4)
        // 인증이 성공적으로 이루어지면 필터에 리다이렉트될 URL을 설정합니다.
        filter.setAuthenticationFailureHandler((request, response, exception) -> response.sendRedirect("/error"));
        //(5)
        //인증이 실패하면 필터에 리다이렉트될 URL을 설정합니다.
        return filter;
    }
```
```java
public class UserTokenService extends UserInfoTokenServices {

    public UserTokenService(ClientResources resources, SocialType socialType) {
        super(resources.getResource().getUserInfoUri(), resources.getClient().getClientId());
        setAuthoritiesExtractor(new OAuth2AuthoritiesExtractor(socialType));
    }

    public static class OAuth2AuthoritiesExtractor implements AuthoritiesExtractor {
        private String socialType;

        public OAuth2AuthoritiesExtractor(SocialType socialType) {
            this.socialType = socialType.getRoleType();
        }

        @Override
        public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
            return AuthorityUtils.createAuthorityList(this.socialType);
        }
    }
}

```
* UserInfoTokenServices는 스프링 시큐리티 OAuth2에서 제공하는 클래스이며 User정보를 얻어오기 위해 소셜 서버와 통신하는 역할을 수행합니다.
* UserTokenService super()를 사용해서 각각의 소셜 미디어 정보를 주입할 수 있도록 합니다.
* AuthoritiesExtractor 인터페이스를 구현한 내부 클래스인 OAuth2AuthoritiesExtractor를 생성했습니다.
* extractAuthorities() 메서드를 오버라이드하여 권한을 리스트 형식으로 생성하여 변환하도록합니다.