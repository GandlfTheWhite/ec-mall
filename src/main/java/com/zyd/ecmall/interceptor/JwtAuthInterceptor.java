package com.zyd.ecmall.interceptor;

import com.zyd.ecmall.security.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthInterceptor(
            JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String authorizationHeader =
                request.getHeader("Authorization");
        // Authorization請求ヘーダ存在しないまたは格式間違えた
        if (authorizationHeader == null) {
            writeUnauthorizedResponse(
                    response,
                    "JWTが必要です"
            );
            return false;
        }

        // Authorization請求ヘーダ格式間違えた
        if (!authorizationHeader.startsWith("Bearer ")) {
            writeUnauthorizedResponse(
                    response,
                    "Authorizationヘッダーの形式が正しくありません"
            );
            return false;
        }

        // 頭からの"Bearer "を取り抜けて
        String token =
                authorizationHeader.substring(7);

        try {
            // JWTを認証して、会員IDを取得します
            Long memberId =
                    jwtTokenProvider.getMemberId(token);

            // 会員ID情報を今回の請求に保存します
            request.setAttribute("memberId", memberId);

            return true;

        } catch (ExpiredJwtException e) {

            writeUnauthorizedResponse(
                    response,
                    "JWTの有効期限が切れています"
            );
            return false;

        } catch (JwtException | IllegalArgumentException e) {

            writeUnauthorizedResponse(
                    response,
                    "JWTが正しくありません"
            );
            return false;
        }
    }

    private void writeUnauthorizedResponse(
            HttpServletResponse response,
            String message) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"status\":401,"
                        + "\"message\":\""
                        + message
                        + "\"}"
        );
    }
}