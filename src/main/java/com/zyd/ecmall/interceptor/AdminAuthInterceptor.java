package com.zyd.ecmall.interceptor;

import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.exception.MemberNotFoundException;
import com.zyd.ecmall.security.JwtTokenProvider;
import com.zyd.ecmall.service.MemberService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    public AdminAuthInterceptor(JwtTokenProvider jwtTokenProvider, MemberService memberService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. JWT を取得
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "認証が必要です");
            return false;
        }

        String token = authorizationHeader.substring(7);

        try {
            // 2. JWT から会員IDを取得
            Long memberId = jwtTokenProvider.getMemberId(token);
            request.setAttribute("memberId", memberId);

            // 3. 会員情報を取得してロールをチェック
            Member member = memberService.getMemberById(memberId);

            if (!"ADMIN".equals(member.getRole())) {
                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "管理者権限がありません");
                return false;
            }

            // 4. 管理者情報もリクエストに保存（必要に応じて）
            request.setAttribute("adminId", memberId);
            return true;

        } catch (JwtException | IllegalArgumentException | MemberNotFoundException e) {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "ログイン情報が無効です。再度ログインしてください。");
            return false;
        }

    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
