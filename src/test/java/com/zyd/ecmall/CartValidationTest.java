package com.zyd.ecmall;

import com.zyd.ecmall.dto.CartItemRequest;
import com.zyd.ecmall.entity.Cart;
import com.zyd.ecmall.entity.CartItem;
import com.zyd.ecmall.entity.Product;
import com.zyd.ecmall.mapper.CartItemMapper;
import com.zyd.ecmall.mapper.CartMapper;
import com.zyd.ecmall.mapper.ProductMapper;
import com.zyd.ecmall.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartValidationTest {
    private CartItemMapper items;
    private CartService service;
    private Product product;

    @BeforeEach
    void setup() {
        CartMapper carts = mock(CartMapper.class);
        items = mock(CartItemMapper.class);
        ProductMapper products = mock(ProductMapper.class);
        service = new CartService(carts, items, products);
        Cart cart = new Cart();
        cart.setId(2L);
        when(carts.selectByMemberId(1L)).thenReturn(cart);
        product = new Product();
        product.setId(3L);
        product.setStatus(1);
        product.setStock(5);
        product.setPrice(new BigDecimal("100.00"));
        when(products.selectById(3L)).thenReturn(product);
    }

    private CartItemRequest request(int quantity) {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(3L);
        request.setQuantity(quantity);
        return request;
    }

    @Test
    void validAdditionStoresQuantityAndPriceSnapshot() {
        service.addItemToCart(1L, request(2));
        verify(items).insert(argThat(item -> item.getProductId().equals(3L)
                && item.getQuantity() == 2 && item.getPriceAtAdd().equals(product.getPrice())));
    }

    @Test
    void unavailableProductsAndInsufficientStockAreRejected() {
        product.setStatus(0);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.addItemToCart(1L, request(1))).getStatusCode().value());
        product.setStatus(1);
        assertThrows(ResponseStatusException.class, () -> service.addItemToCart(1L, request(6)));
        verifyNoInteractions(items);
    }

    @Test
    void repeatedAdditionChecksCombinedQuantity() {
        CartItem existing = new CartItem();
        existing.setId(4L);
        existing.setQuantity(4);
        when(items.selectByCartIdAndProductId(2L, 3L)).thenReturn(existing);
        assertThrows(ResponseStatusException.class, () -> service.addItemToCart(1L, request(2)));
        verify(items, never()).updateQuantity(anyLong(), anyInt());
        service.addItemToCart(1L, request(1));
        verify(items).updateQuantity(4L, 5);
    }

    @Test
    void quantityUpdatesRejectZeroNegativeAndMissingValues() {
        for (Integer quantity : new Integer[] {0, -1, null}) {
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> service.updateItemQuantity(1L, 3L, quantity)).getStatusCode().value());
        }
        verifyNoInteractions(items);
    }
}
