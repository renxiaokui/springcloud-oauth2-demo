package cloud.ren.authserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author RenYinKui
 * @Description: 用户测试类
 * @date 2020/4/13 17:23
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/get")
    public String getUser() {
        return "SUCCESS";
    }
}
