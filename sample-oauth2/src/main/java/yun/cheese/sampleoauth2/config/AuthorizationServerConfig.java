package yun.cheese.sampleoauth2.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
@AllArgsConstructor
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final TokenStore tokenStore;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {

        // 해당 정보가 인증에 필요한 정보다
        // 해당정보를 보고 authenticationManager가 인증을 처리해준다.
        configurer
                .inMemory()
                .withClient("ynu-client")
                .secret(passwordEncoder.encode("yun-password"))
                .authorizedGrantTypes(
                        "password",
                        "authorization_code",
                        "refresh_token",
                        "implicit"
                )
                .scopes("read", "write", "trust")
                .accessTokenValiditySeconds(1 * 60 * 60)
                .refreshTokenValiditySeconds(6 * 60 * 60);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore)
                .authenticationManager(authenticationManager);
    }
}
