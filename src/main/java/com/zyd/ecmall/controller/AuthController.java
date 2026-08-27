package com.zyd.ecmall.controller;

import com.zyd.ecmall.constant.SessionConst;
import com.zyd.ecmall.dto.LoginRequest;
import com.zyd.ecmall.dto.LoginResponse;
import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.security.JwtTokenProvider;
import com.zyd.ecmall.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
//    private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    public AuthController(
            MemberService memberService,
            JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;

//        System.out.println("/api/auth");
    }

//    @PostMapping("/login")
//    public ResponseEntity<Member> login(
//            @RequestBody LoginRequest request,
//            HttpSession session) {
//        // ① メールとパスワードを検証します。
//        Member member = memberService.login(
//                request.getEmail(),
//                request.getPassword()
//        );
//        // ② 登録失敗かの判断   失敗の場合serviceに異常を出していたのため、ここは不要になっております。
////        if (member == null) {
////            return ResponseEntity
////                    .status(HttpStatus.UNAUTHORIZED)
////                    .build();
////        }
//        // ③ 検証成功だけの場合、会員IDをSESSIONに保存します。
//        session.setAttribute(
//                SessionConst.LOGIN_MEMBER_ID,
//                member.getId()
//        );
//        // ④ 200コードと会員情報を返却します。
//        return ResponseEntity.ok(member);
//    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        Member member = memberService.login(
                request.getEmail(),
                request.getPassword()
        );
//        // Session保留
//        session.setAttribute(
//                SessionConst.LOGIN_MEMBER_ID,
//                member.getId()
//        );
        // JWT作成
        String token =
                jwtTokenProvider.generateToken(member.getId());

        LoginResponse response =
                new LoginResponse(token, "Bearer");

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/me")
//    public ResponseEntity<Member> getCurrentMember(
//            HttpSession session) {
//        Long memberId = (Long) session.getAttribute(
//                SessionConst.LOGIN_MEMBER_ID
//        );
//        if (memberId == null) {
//            return ResponseEntity
//                    .status(HttpStatus.UNAUTHORIZED)
//                    .build();
//        }
//        Member member = memberService.getMemberById(memberId);
//        return ResponseEntity.ok(member);
//    }
        @GetMapping("/me")
        public ResponseEntity<Member> getCurrentMember(
                HttpServletRequest request) {

            Long memberId =
                    (Long) request.getAttribute("memberId");

            Member member =
                    memberService.getMemberById(memberId);
//            System.out.println("me");
            return ResponseEntity.ok(member);
        }


//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(HttpSession session) {
//
//        session.invalidate();
//
//        return ResponseEntity.noContent().build();
//    }

}
