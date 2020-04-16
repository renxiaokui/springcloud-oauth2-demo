package cloud.ren.authserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    //数据源，用于从数据库获取数据进行认证操作，测试可以从内存中获取
    @Resource
    private DataSource dataSource;

    @Autowired
    AuthenticationManager authenticationManager;

    //自定义用户验证
    @Resource
    private UserDetailsService userDetailsService;

    //oauth2四种认证方式
//    authorization_code（授权码模式）第三方Web服务器端应用与第三方原生App (正宗方式)(支持refresh token)
//
//    implicit(简化模式) 第三方单页面应用 (不支持refresh token)
//
//    password（密码认证）有自己的用户系统，应用直接都是受信任的(都是由一家公司开发的)(支持refresh token)
//
//    client_credentials（客户端模式）没有用户参 完全信任的服务器端服务(不支持refresh token)

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 通过数据库控制clients
//        clients.jdbc(dataSource).clients(clientDetails());

        //authorizedGrantTypes参数是认证方式 password用户名密码方式  authorization_code授权码方式 client_credentials客户端方式

        clients.inMemory()
                //password模式，自己本身有一套用户体系，在认证时需要带上自己的用户名和密码，以及客户端的client_id,client_secret
                // 此时，accessToken所包含的权限是用户本身的权限，而不是客户端的权限
                .withClient("client_1")
//                .resourceIds("res_1") //可以访问的资源id，不配置是所有的
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("all")
                .authorities("ROLE_ADMIN")
                .secret(new BCryptPasswordEncoder().encode("secret"))

                //client_credentials模式，没有用户的概念，直接与认证服务器交互，用配置中的客户端信息去申请accessToken，
                // 客户端有自己的client_id,client_secret对应于用户的username,password，而客户端也拥有自己的authorities，
                // 当采取client模式认证时，对应的权限也就是客户端自己的authorities
                //(为后台api服务消费者设计)(不支持refresh token)
                .and()
                .withClient("client_2")
                .scopes("read")
                .secret(new BCryptPasswordEncoder().encode("secret"))
                .authorizedGrantTypes("client_credentials")

                //authorization_code（授权码模式）:用在服务端应用之间 (正宗方式)(支持refresh token)
                .and()
                .withClient("client_3")
                .scopes("read")
                .secret(new BCryptPasswordEncoder().encode("secret"))
                .authorizedGrantTypes("authorization_code", "refresh_token")

                //隐式授权模式
                .and()
                .withClient("client_4")
                .scopes("read")
                .secret(new BCryptPasswordEncoder().encode("secret"))
                .authorizedGrantTypes("implicit");

    }

    //数据库客户端配置
//    @Bean
//    public ClientDetailsService clientDetails() {
//        // 操作oauth_client_details表
//        return new JdbcClientDetailsService(dataSource);
//    }

    @Bean
    public TokenStore tokenStore() {
//        return new JdbcTokenStore(dataSource);
        return new JwtTokenStore(accessTokenConverter());
    }

    //读取密钥的配置
    @Bean("keyProp")
    public KeyProperties keyProperties() {
        return new KeyProperties();
    }

    @Resource(name = "keyProp")
    private KeyProperties keyProperties;

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setSigningKey("123");           //对称加密算法
        KeyPair keyPair = new KeyStoreKeyFactory(
                keyProperties.getKeyStore().getLocation(),         //证书路径
                keyProperties.getKeyStore().getSecret().toCharArray()) //证书秘钥
                .getKeyPair(
                        keyProperties.getKeyStore().getAlias(), //证书别名
                        keyProperties.getKeyStore().getPassword().toCharArray()); //证书密码
        converter.setKeyPair(keyPair);
        return converter;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        // 允许客户表单认证,不加的话/oauth/token无法访问
        oauthServer.allowFormAuthenticationForClients()
                .passwordEncoder(new BCryptPasswordEncoder())
                // 开启/oauth/token_key验证端口无权限访问
                .tokenKeyAccess("permitAll()")
                // /oauth/check_token验证端口认证权限访问
                .checkTokenAccess("isAuthenticated()");
    }

    //定义授权和令牌端点以及令牌服务
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // redisTokenStore
//        endpoints.tokenStore(new RedisTokenStore(redisConnectionFactory))
//                .authenticationManager(authenticationManager)
//                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);

        endpoints
                //验证用户
                .userDetailsService(userDetailsService)
                //指定token存储位置
                .tokenStore(tokenStore())
                // 配置JwtAccessToken转换器
                .tokenEnhancer(accessTokenConverter())
                //指定认证管理器
                .authenticationManager(authenticationManager)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);

        // 配置tokenServices参数
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(endpoints.getTokenStore());
        // 是否支持刷新
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setClientDetailsService(endpoints.getClientDetailsService());
        tokenServices.setTokenEnhancer(endpoints.getTokenEnhancer());
        // 20分钟
        tokenServices.setAccessTokenValiditySeconds((int) TimeUnit.MINUTES.toSeconds(20));
        endpoints.tokenServices(tokenServices);
    }

}