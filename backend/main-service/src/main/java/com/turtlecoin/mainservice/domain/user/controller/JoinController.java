package com.turtlecoin.mainservice.domain.user.controller;

import com.turtlecoin.mainservice.domain.user.dto.UserRequestDto;
import com.turtlecoin.mainservice.domain.user.entity.User;
import com.turtlecoin.mainservice.domain.user.service.JoinService;
import com.turtlecoin.mainservice.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@ResponseBody
public class JoinController {

    private final JoinService joinService;
    public JoinController(JoinService joinService, UserService userService) {
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinProcess(@RequestBody UserRequestDto userDto) {
        //비밀번호 유효성 먼저 검사하는 로직 구현
        return joinService.joinProcess(userDto);
    }
}
