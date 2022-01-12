package com.code.furry_takeout.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.code.furry_takeout.common.R;
import com.code.furry_takeout.entity.User;
import com.code.furry_takeout.service.UserService;
import com.code.furry_takeout.utils.SMSUtils;
import com.code.furry_takeout.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequestMapping("user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("sendMsg")
    public R<String> SendMessage(@RequestBody User user, HttpServletRequest request) {
        // 1. 生成验证码
        String code = ValidateCodeUtils.generateValidateCode(6).toString();
        // 2. 将验证码存到 session 中
        // 优化后放到 Redis 中设置过期时间
        log.info("生成的验证码{}", code);
        redisTemplate.opsForValue().set(user.getPhone(), code, 5, TimeUnit.MINUTES);
        //request.getSession().setAttribute(user.getPhone(), code);
        // 3. 发送验证码
//        SMSUtils.sendMessage(user.getPhone(), code);
        // 方便调试本处改为直接将 code 填充到前端
        return R.success(code);
    }

    @PostMapping("login")
    public R<User> MobileLogin(@RequestBody Map map, HttpSession session) {
        // 1. 从 请求中获取 phone 和 code
        String phone = map.getOrDefault("phone", "").toString();
        String code = map.getOrDefault("code", "").toString();
        log.info("前端登录请求，手机号码{}, 验证码{}", phone, code);
        // 2. 从 session 中获取存储的验证码，校验和用户提交的是否一致
        // 优化后从 Redis 中获取
        Object sessionCode = redisTemplate.opsForValue().get(phone);
//        Object sessionCode = session.getAttribute(phone);
        if (sessionCode != null && sessionCode.equals(code)) {
            // 验证码匹配正确
            // 获取用户
            User user = null;
            List<User> userList = userService.list(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
            if (userList != null && userList.size() > 0) {
                // 数据库中存在该用户，获取第一条数据
                user = userList.get(0);
            } else {
                // 用户第一次使用系统，默认创建新用户
                user = new User();
                user.setPhone(phone);
                user.setName("user_" + phone);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            // 登录成功之后删除 Redis 中的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("验证码输入错误");
    }

    @PostMapping("loginout")
    public R<String> MobileLogout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("移动端退出登录成功");
    }
}
