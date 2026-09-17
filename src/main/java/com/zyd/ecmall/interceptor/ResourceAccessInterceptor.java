package com.zyd.ecmall.interceptor;

import com.zyd.ecmall.controller.MemberController;
import com.zyd.ecmall.controller.ProductController;
import com.zyd.ecmall.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import java.util.Map;

/** 会員本人の操作と管理者の操作を区別する。 */
@Component
public class ResourceAccessInterceptor implements HandlerInterceptor {
    private final MemberService members;

    public ResourceAccessInterceptor(MemberService members) {
        this.members = members;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) return true;
        boolean memberResource = MemberController.class.isAssignableFrom(method.getBeanType());
        boolean productResource = ProductController.class.isAssignableFrom(method.getBeanType());
        if (!memberResource && !productResource) return true;
        if (memberResource && method.getMethod().getName().equals("create")) return true;
        if (productResource && (request.getMethod().equals("GET") || request.getMethod().equals("HEAD"))) return true;

        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId != null && "ADMIN".equals(members.getMemberById(memberId).getRole())) return true;
        if (memberResource && (request.getMethod().equals("GET") || request.getMethod().equals("PUT"))) {
            Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (variables instanceof Map<?, ?> paths && memberId != null
                    && memberId.toString().equals(paths.get("id"))) return true;
        }
        response.setStatus(memberId == null ? 401 : 403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"この操作を行う権限がありません\"}");
        return false;
    }
}
