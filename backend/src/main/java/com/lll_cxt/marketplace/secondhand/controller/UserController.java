package com.lll_cxt.marketplace.secondhand.controller;

import com.lll_cxt.marketplace.secondhand.dto.Result;
import com.lll_cxt.marketplace.secondhand.dto.UserDTO;
import com.lll_cxt.marketplace.secondhand.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserDTO user = userService.getCurrentUser(userId);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<UserDTO> updateUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserDTO user = userService.updateUser(userId, userDTO);
        return Result.success("更新成功", user);
    }
}
