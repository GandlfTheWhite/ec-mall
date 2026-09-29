package com.zyd.ecmall;

import com.zyd.ecmall.controller.MemberController;
import com.zyd.ecmall.controller.ProductImageController;
import com.zyd.ecmall.service.ProductImageService;
import org.springframework.mock.web.MockMultipartFile;
import com.zyd.ecmall.controller.ProductController;
import com.zyd.ecmall.controller.AdminController;
import com.zyd.ecmall.controller.AuthController;
import com.zyd.ecmall.controller.CartController;
import com.zyd.ecmall.controller.OrderController;
import com.zyd.ecmall.config.WebConfig;
import com.zyd.ecmall.entity.Member;
import com.zyd.ecmall.exception.GlobalExceptionHandler;
import com.zyd.ecmall.exception.MemberNotFoundException;
import com.zyd.ecmall.interceptor.AdminAuthInterceptor;
import com.zyd.ecmall.interceptor.JwtAuthInterceptor;
import com.zyd.ecmall.interceptor.ResourceAccessInterceptor;
import com.zyd.ecmall.security.JwtTokenProvider;
import com.zyd.ecmall.service.MemberService;
import com.zyd.ecmall.service.ProductService;
import com.zyd.ecmall.service.OrderService;
import com.zyd.ecmall.service.CartService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiAccessTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private MemberService members;
    private ProductService products;

    @Configuration
    @EnableWebMvc
    @Import({ WebConfig.class, JwtAuthInterceptor.class, ResourceAccessInterceptor.class,
            AdminAuthInterceptor.class, MemberController.class, ProductController.class,
            AdminController.class, AuthController.class, CartController.class,
            OrderController.class, ProductImageController.class, GlobalExceptionHandler.class })
    static class TestConfig {
        @Bean ProductImageService images() { return mock(ProductImageService.class); }
        @Bean MemberService members() { return mock(MemberService.class); }
        @Bean ProductService products() { return mock(ProductService.class); }
        @Bean OrderService orders() { return mock(OrderService.class); }
        @Bean CartService cart() { return mock(CartService.class); }
        @Bean JwtTokenProvider tokens() { return mock(JwtTokenProvider.class); }
    }

    @BeforeEach
    void setup() {
        // 実際の WebConfig を使うが、DB・スケジューラー・外部サーバーは起動しない。
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestConfig.class);
        context.refresh();
        members = context.getBean(MemberService.class);
        products = context.getBean(ProductService.class);
        JwtTokenProvider tokens = context.getBean(JwtTokenProvider.class);
        when(tokens.getMemberId("user-token")).thenReturn(1L);
        when(tokens.getMemberId("admin-token")).thenReturn(2L);
        when(tokens.getMemberId("expired-token")).thenThrow(new JwtException("expired"));
        when(tokens.getMemberId("")).thenThrow(new IllegalArgumentException("empty"));
        when(tokens.getMemberId("deleted-token")).thenReturn(99L);
        when(members.getMemberById(99L)).thenThrow(new MemberNotFoundException(99L));
        Member user = new Member();
        user.setId(1L);
        user.setRole("USER");
        Member admin = new Member();
        admin.setId(2L);
        admin.setRole("ADMIN");
        when(members.getMemberById(1L)).thenReturn(user);
        when(members.getMemberById(2L)).thenReturn(admin);
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void closeContext() { context.close(); }

    @Test
    void anonymousCanRegisterButCannotListMembers() throws Exception {
        mvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"山田\",\"email\":\"new@example.com\",\"age\":20,\"password\":\"pass1234\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/members")).andExpect(status().isUnauthorized());
        verify(members).createMember(any());
        verify(members, never()).getAllMembers();
    }

    @Test
    void registrationWithoutAgeIsRejectedBeforeTheService() throws Exception {
        mvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"山田\",\"email\":\"new@example.com\",\"password\":\"pass1234\"}"))
                .andExpect(status().isBadRequest());
        verify(members, never()).createMember(any());
    }

    @Test
    void userCanReadAndEditOnlyOwnProfile() throws Exception {
        mvc.perform(get("/api/members/1").header("Authorization", "Bearer user-token")).andExpect(status().isOk());
        mvc.perform(put("/api/members/1").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"変更後\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/members/2").header("Authorization", "Bearer user-token")).andExpect(status().isForbidden());
        mvc.perform(put("/api/members/2").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"変更後\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/members/1").header("Authorization", "Bearer user-token")).andExpect(status().isForbidden());
        verify(members, never()).updateMember(eq(2L), any());
    }

    @Test
    void productWriteOperationsRequireAdmin() throws Exception {
        mvc.perform(delete("/api/products/3").header("Authorization", "Bearer user-token")).andExpect(status().isForbidden());
        mvc.perform(put("/api/products/3").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"stock\":100}")).andExpect(status().isForbidden());
        when(products.deleteProduct(3L)).thenReturn(true);
        mvc.perform(delete("/api/products/3").header("Authorization", "Bearer admin-token")).andExpect(status().isNoContent());
        verify(products).deleteProduct(3L);
    }

    @Test
    void adminCanEditAndDeleteMember() throws Exception {
        mvc.perform(put("/api/members/1").header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"更新\"}"))
                .andExpect(status().isOk());
        when(members.deleteMember(1L)).thenReturn(true);
        mvc.perform(delete("/api/members/1").header("Authorization", "Bearer admin-token")).andExpect(status().isNoContent());
    }

    @Test
    void configuredProtectedPathsRejectAnonymousRequests() throws Exception {
        for (String path : new String[] { "/api/products", "/api/products/search",
                "/api/auth/me", "/api/cart", "/api/orders", "/api/admin/members",
                "/api/admin/orders" }) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/products/search").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpointsDistinguishInvalidLoginFromInsufficientRole() throws Exception {
        for (String token : new String[] { "expired-token", "", "deleted-token" }) {
            mvc.perform(get("/api/admin/members").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/admin/members").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/members").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void adminCannotAssignUnknownRole() throws Exception {
        mvc.perform(put("/api/admin/members/1/role").param("role", "OWNER")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest());
        verify(members, never()).updateRole(anyLong(), anyString());
    }

    @Test
    void imageUploadRequiresValidAdministratorBeforeInvokingService() throws Exception {
        var imageService = context.getBean(ProductImageService.class);
        var file = new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1});
        mvc.perform(multipart("/api/admin/product-images").file(file)).andExpect(status().isUnauthorized());
        for (String token : new String[]{"expired-token", "deleted-token"}) {
            mvc.perform(multipart("/api/admin/product-images").file(file).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(multipart("/api/admin/product-images").file(file).header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(imageService);
        when(imageService.upload(any())).thenReturn(new ProductImageService.UploadResult("https://images.example.com/products/a.png", "products/a.png"));
        mvc.perform(multipart("/api/admin/product-images").file(file).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.imageUrl").value("https://images.example.com/products/a.png"));
        verify(imageService).upload(any());
    }

    @Test
    void missingImageAndContainerSizeErrorsHaveJapaneseResponses() throws Exception {
        mvc.perform(multipart("/api/admin/product-images").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("画像ファイルを選択してください。"));
        when(context.getBean(ProductImageService.class).upload(any()))
                .thenThrow(new org.springframework.web.multipart.MaxUploadSizeExceededException(5 * 1024 * 1024));
        mvc.perform(multipart("/api/admin/product-images").file(new MockMultipartFile("file", new byte[]{1}))
                .header("Authorization", "Bearer admin-token")).andExpect(status().isPayloadTooLarge());
    }
}
