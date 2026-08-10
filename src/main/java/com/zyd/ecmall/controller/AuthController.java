package com.zyd.ecmall.controller;

import com.zyd.ecmall.constant.SessionConst;
import com.zyd.ecmall.dto.LoginRequest;
import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.service.MemberService;
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
    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }
    @PostMapping("/login")
    public ResponseEntity<Member> login(
            @RequestBody LoginRequest request,
            HttpSession session) {
        // ① メールとパスワードを検証します。
        Member member = memberService.login(
                request.getEmail(),
                request.getPassword()
        );
        // ② 登録失敗かの判断   失敗の場合serviceに異常を出していたのため、ここは不要になっております。
//        if (member == null) {
//            return ResponseEntity
//                    .status(HttpStatus.UNAUTHORIZED)
//                    .build();
//        }
        // ③ 検証成功だけの場合、会員IDをSESSIONに保存します。
        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );
        // ④ 200コードと会員情報を返却します。
        return ResponseEntity.ok(member);
    }
    @GetMapping("/me")
    public ResponseEntity<Member> getCurrentMember(
            HttpSession session) {
        Long memberId = (Long) session.getAttribute(
                SessionConst.LOGIN_MEMBER_ID
        );
        if (memberId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        Member member = memberService.getMemberById(memberId);
        return ResponseEntity.ok(member);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.noContent().build();
    }

}
