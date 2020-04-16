package cloud.ren.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * 描述
 */
@Configuration
// 开启 资源服务器(标识他是一个oauth2中的资源服务器)
@EnableResourceServer
//@EnableGlobalMethodSecurity(prePostEnabled = true)//激活方法上的PreAuthorize注解,方法级别的权限控制开启
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    //公钥
    private static final String PUBLIC_KEY = "public.key";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        //这里可以配资自己的资源id，client如果没有这个resourceid的访问权限是不可以访问的
        resources.resourceId("resource1");
    }

    /***
     * 定义JwtTokenStore
     */
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    /***
     * 定义JJwtAccessTokenConverter  用来校验令牌
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setSigningKey("123"); //对称加密
        String publicKey = getPubKey();
        converter.setVerifierKey(publicKey);
        //不设置这个会出现 Cannot convert access token to JSON
        converter.setVerifier(new RsaVerifier(publicKey));
        return converter;
    }

    /**
     * 获取非对称加密公钥 Key
     *
     * @return 公钥 Key
     */
    private String getPubKey() {
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
             BufferedReader br = new BufferedReader(inputStreamReader)) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException ioe) {
            throw new RuntimeException("公钥未找到");
        }
    }

    /***
     * Http安全配置，对每个到达系统的http请求链接进行校验
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        //所有请求必须认证通过
        http.authorizeRequests()
                .anyRequest().
                authenticated();
    }

}
