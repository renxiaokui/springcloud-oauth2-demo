//package cloud.ren.authserver.controller;
//
//import cloud.ren.authserver.entity.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
//import org.springframework.web.HttpRequestMethodNotSupportedException;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * @author RenYinKui
// * @Description:自定义返回数据 注释掉此类就是默认的jwt数据
// * @date 2020/4/15 18:35
// */
//@RestController
//@RequestMapping("/oauth")
//public class OauthController {
//
//    @Autowired
//    private TokenEndpoint tokenEndpoint;
//
//    @GetMapping("/token")
//    public Result getAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
//        return custom(tokenEndpoint.getAccessToken(principal, parameters).getBody());
//    }
//
//    @PostMapping("/token")
//    public Result postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
//        return custom(tokenEndpoint.postAccessToken(principal, parameters).getBody());
//    }
//
//    //自定义返回格式
//    private Result custom(OAuth2AccessToken accessToken) {
//        DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;
//        Map<String, Object> data = new LinkedHashMap<>(token.getAdditionalInformation());
//        data.put("access-token", token.getValue());
//        if (token.getRefreshToken() != null) {
//            data.put("refresh-token", token.getRefreshToken().getValue());
//        }
//        return Result.build(data);
//    }
//
//}