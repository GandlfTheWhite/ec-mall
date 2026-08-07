package com.zyd.ecmall.controller;

import com.zyd.ecmall.dto.MemberCreateRequest;
import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.service.MemberService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.zyd.ecmall.dto.MemberUpdateRequest;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    @PostMapping
    public Member create(@RequestBody MemberCreateRequest request) {
        return memberService.createMember(request);
    }

    @GetMapping("/{id}")
    public Member getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        boolean deleted = memberService.deleteMember(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public Member updateMember(
            @PathVariable Long id,
            @RequestBody MemberUpdateRequest request) {

        return memberService.updateMember(id, request);
    }



}