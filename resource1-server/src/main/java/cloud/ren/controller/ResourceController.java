package cloud.ren.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author RenYinKui
 * @Description:
 * @date 2020/4/13 17:23
 */
@RestController
@RequestMapping("/resource")
public class ResourceController {
    @GetMapping("/get")
    public String getUser() {
        return "SUCCESS";
    }
}
