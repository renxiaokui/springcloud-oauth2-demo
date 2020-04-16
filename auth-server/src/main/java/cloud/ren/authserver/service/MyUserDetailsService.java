package cloud.ren.authserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // TODO 根据用户名查询数据库，这里测试先写死
        // 封装用户信息，并返回。参数分别是：用户名，密码，用户权限
        String encode = new BCryptPasswordEncoder().encode("123456");
        logger.info("用户的用户名: {} 密码: {}", username,encode);
        User user = new User(username, encode,
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN"));
        user.isEnabled();
        return user;
  }
}