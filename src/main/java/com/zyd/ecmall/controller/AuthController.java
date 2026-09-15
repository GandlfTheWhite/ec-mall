package com.zyd.ecmall.controller;

import com.zyd.ecmall.dto.LoginRequest;
import com.zyd.ecmall.dto.LoginResponse;
import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.security.JwtTokenProvider;
import com.zyd.ecmall.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(
            MemberService memberService,
            JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        Member member = memberService.login(
                request.getEmail(),
                request.getPassword()
        );
        // JWT作成
        String token =
                jwtTokenProvider.generateToken(member.getId());
        LoginResponse response =
                new LoginResponse(token, "Bearer");
        return ResponseEntity.ok(response);
    }

        @GetMapping("/me")
        public ResponseEntity<Member> getCurrentMember(
                HttpServletRequest request) {
            Long memberId =
                    (Long) request.getAttribute("memberId");
            Member member =
                    memberService.getMemberById(memberId);
            return ResponseEntity.ok(member);
        }


}
