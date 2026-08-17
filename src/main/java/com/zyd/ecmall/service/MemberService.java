package com.zyd.ecmall.service;
import com.zyd.ecmall.dto.MemberCreateRequest;
import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.exception.DuplicateEmailException;
import com.zyd.ecmall.exception.LoginFailedException;
import com.zyd.ecmall.exception.MemberNotFoundException;
import com.zyd.ecmall.mapper.MemberMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.zyd.ecmall.dto.MemberUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberMapper memberMapper,
                         PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }
    //追加機能
    public Member createMember(MemberCreateRequest request) {
        Member existingMember =
                memberMapper.selectByEmail(request.getEmail());

        if (existingMember != null) {
            throw new DuplicateEmailException(request.getEmail());
        }
        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setAge(request.getAge());
        String passwordHash =
                passwordEncoder.encode(request.getPassword());
        member.setPasswordHash(passwordHash);
        memberMapper.insert(member);
        return memberMapper.selectById(member.getId());
    }

    //全体検索機能
    public List<Member> getAllMembers() {
        return memberMapper.selectAll();
    }
    //個別検索機能
    public Member getMemberById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new MemberNotFoundException(id);
        }
        return member;
    }
    //削除機能
    public boolean deleteMember(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new MemberNotFoundException(id);
        }
        return memberMapper.deleteById(id) > 0;
    }
    // 更新機能
    public Member updateMember(Long id, MemberUpdateRequest request) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new MemberNotFoundException(id);
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_FOUND,
//                    "会員が見つかりません。id=" + id
//            );
        }
        // 名前、メール、年齢入力ないと既存のものを使います
        if (request.getName() != null) {
            member.setName(request.getName());
        }
        if (request.getEmail() != null) {
            member.setEmail(request.getEmail());
        }
        if (request.getAge() != null) {
            member.setAge(request.getAge());
        }
        //パスワード入力ないと更新できない
        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            member.setPasswordHash(
                    passwordEncoder.encode(request.getPassword())
            );
        }
        memberMapper.update(member);
        return memberMapper.selectById(id);
    }
    //登録機能
        public Member login(String email, String password) {

            Member member = memberMapper.selectByEmail(email);
            if (member == null) {
                throw new LoginFailedException();
            }
            boolean matched = passwordEncoder.matches(
                    password,
                    member.getPasswordHash()
            );
            if (!matched) {
                throw new LoginFailedException();
            }
            return member;
        }
//    public Member login(String email, String password) {
//
//        Member member = memberMapper.selectByEmail(email);
//
//        System.out.println("收到的邮箱 = [" + email + "]");
//        System.out.println("是否查到会员 = " + (member != null));
//
//        if (member == null) {
//            return null;
//        }
//
//        boolean passwordMatched = passwordEncoder.matches(
//                password,
//                member.getPasswordHash()
//        );
//
//        System.out.println("密码是否一致 = " + passwordMatched);
//
//        if (!passwordMatched) {
//            return null;
//        }
//
//        return member;
//    }



}