package com.zyd.ecmall.service;

import com.zyd.ecmall.dto.CartItemRequest;
import com.zyd.ecmall.dto.CartResponse;
import com.zyd.ecmall.entity.Cart;
import com.zyd.ecmall.entity.CartItem;
import com.zyd.ecmall.entity.Product;
import com.zyd.ecmall.exception.ProductNotFoundException;
import com.zyd.ecmall.mapper.CartItemMapper;
import com.zyd.ecmall.mapper.CartMapper;
import com.zyd.ecmall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartService(CartMapper cartMapper, CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    /**
     * カート情報を取得（会員IDから）
     */
    public CartResponse getCart(Long memberId) {
        // 1. カート主テーブルを取得（なければ新規作成）
        Cart cart = getOrCreateCart(memberId);

        // 2. 明細＋商品情報をJOINで取得
        List<CartResponse.CartItemDetail> items = cartItemMapper.selectCartDetailsByCartId(cart.getId());

        // 3. 合計金額を計算（BigDecimal）
        BigDecimal total = items.stream()
                .map(item -> item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. レスポンス作成
        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setItems(items);
        response.setTotalPrice(total);
        return response;
    }

    /**
     * カートに商品を追加 or 数量更新
     */
    @Transactional
    public void addItemToCart(Long memberId, CartItemRequest request) {
        // 1. 商品存在チェック
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException(request.getProductId());
        }

        // 2. カート取得（なければ作成）
        Cart cart = getOrCreateCart(memberId);

        // 3. 既に同じ商品がカートにあるかチェック
        CartItem existingItem = cartItemMapper.selectByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem != null) {
            // あるなら数量を加算（更新）
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            cartItemMapper.updateQuantity(existingItem.getId(), newQuantity);
        } else {
            // なければ新規挿入（その時点の商品価格をスナップショットとして保存）
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPriceAtAdd(product.getPrice()); // 今の価格で固定保存！
            cartItemMapper.insert(newItem);
        }
    }

    /**
     * カート内の商品数量を直接更新（置き換え）
     */
    @Transactional
    public void updateItemQuantity(Long memberId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(memberId);
        CartItem item = cartItemMapper.selectByCartIdAndProductId(cart.getId(), productId);
        if (item == null) {
            throw new RuntimeException("カートに該当商品が存在しません。ID：" + productId);
        }
        cartItemMapper.updateQuantity(item.getId(), quantity);
    }

    /**
     * カートから特定商品を削除
     */
    @Transactional
    public void removeItemFromCart(Long memberId, Long productId) {
        Cart cart = getOrCreateCart(memberId);
        int deleted = cartItemMapper.deleteByCartIdAndProductId(cart.getId(), productId);
        if (deleted == 0) {
            throw new RuntimeException("削除対象の商品がカートに見つかりません。");
        }
    }

    /**
     * カートを空にする（全削除）
     */
    @Transactional
    public void clearCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        cartItemMapper.deleteAllByCartId(cart.getId());
    }

    // ---- プライベートヘルパーメソッド ----
    private Cart getOrCreateCart(Long memberId) {
        Cart cart = cartMapper.selectByMemberId(memberId);
        if (cart == null) {
            cart = new Cart();
            cart.setMemberId(memberId);
            cartMapper.insert(cart);
        }
        return cart;
    }
}