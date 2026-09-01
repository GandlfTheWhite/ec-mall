package com.zyd.ecmall.service;

import com.zyd.ecmall.dto.CartResponse;
import com.zyd.ecmall.dto.OrderCreateRequest;
import com.zyd.ecmall.entity.Order;
import com.zyd.ecmall.entity.OrderItem;
import com.zyd.ecmall.entity.Product;
import com.zyd.ecmall.exception.ProductNotFoundException;
import com.zyd.ecmall.mapper.OrderMapper;
import com.zyd.ecmall.mapper.OrderItemMapper;
import com.zyd.ecmall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    public OrderService(CartService cartService, OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper, ProductMapper productMapper) {
        this.cartService = cartService;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    /**
     * 注文を作成する（全カート商品を対象） / 创建订单（下单全部购物车商品）
     */
    @Transactional(rollbackFor = Exception.class) // 任何异常都回滚
    public Order createOrderFromCart(Long memberId, OrderCreateRequest request) {

        // 1. 現在のカート情報を取得
        CartResponse cart = cartService.getCart(memberId);
        List<CartResponse.CartItemDetail> items = cart.getItems();

        if (items.isEmpty()) {
            throw new RuntimeException("カートが空です。注文できません。");
        }

        // 2. 在庫チェックと仮更新（ここで在庫をロックする）
        for (CartResponse.CartItemDetail item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new ProductNotFoundException(item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException(
                        "商品「" + product.getName() + "」の在庫が不足しています。"
                                + " (在庫:" + product.getStock() + ", 要求:" + item.getQuantity() + ")"
                );
            }
            // **本当に超重要なポイント：ここで在庫を減らす！**
            // UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}
            // このSQLは「在庫が足りない場合は0件更新（更新失敗）」になるので、安全です。
            int updated = productMapper.deductStock(item.getProductId(), item.getQuantity());
            if (updated == 0) {
                // もし上記のif文をすり抜けてここに来たとしたら、他のスレッドが先に在庫を奪った証拠
                throw new RuntimeException("在庫更新に失敗しました。再度お試しください。");
            }
        }

        // 3. 注文番号を生成（例：ORD20260828123456）
        String orderNo = generateOrderNo();

        // 4. 注文主テーブルに挿入
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setTotalAmount(cart.getTotalPrice());
        order.setStatus(0); // 0: 未払い
        order.setShippingAddress(request.getShippingAddress());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setOrderDate(LocalDateTime.now());

        orderMapper.insert(order); // ここで order.getId() が自動生成される

        // 5. 注文明細を一括挿入（ループ）
        for (CartResponse.CartItemDetail item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName()); // 商品名をスナップショット保存！
            orderItem.setPrice(item.getPriceAtAdd());        // カート追加時の価格をそのまま使う
            orderItem.setQuantity(item.getQuantity());
            orderItemMapper.insert(orderItem);
        }

        // 6. 注文が完了したので、カートを空にする
        cartService.clearCart(memberId);

        // 7. 生成した注文を返す（フロントに表示させるため）
        return order;
    }

    // 注文番号生成器（シンプル版）
    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + datePart + randomNum;
    }

    /**
     * 支払い処理（モック） / 支付处理（模拟）
     * 注文ステータスを「支払済み」(1) に更新する
     */
    @Transactional
    public Order processPayment(Long orderId, Long memberId) {
        // 1. 注文を取得
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("注文が見つかりません。ID: " + orderId);
        }
    
        // 2. 権限チェック：この注文は自分（memberId）のものか？
        if (!order.getMemberId().equals(memberId)) {
            throw new RuntimeException("この注文を操作する権限がありません。");
        }
    
        // 3. 既に支払済み or キャンセル済みの場合はエラー
        if (order.getStatus() == 1) {
            throw new RuntimeException("この注文は既に支払済みです。");
        }
        if (order.getStatus() == 4) {
            throw new RuntimeException("この注文はキャンセル済みです。");
        }
    
        // 4. ステータスを「支払済み」(1) に更新
        order.setStatus(1);
        orderMapper.updateStatus(orderId, 1);
    
        // 5. 更新後の注文を返す
        return order;
    }

    /**
     * タイムアウトした注文をキャンセルし、在庫を戻す / 取消超时订单并恢复库存
     */
    @Transactional
    public void cancelTimeoutOrders() {
        // 1. 15分前を閾値とする
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
    
        // 2. 超えた未払い注文を取得
        List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(threshold);
    
        for (Order order : timeoutOrders) {
            // 3. 注文ステータスを「キャンセル」(4) に更新
            orderMapper.cancelOrder(order.getId());
    
            // 4. 注文明細を取得（在庫を戻すために必要）
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
    
            // 5. 各商品の在庫を戻す
            for (OrderItem item : items) {
                productMapper.addStock(item.getProductId(), item.getQuantity());
            }
        }
    
        System.out.println("【定期タスク】" + timeoutOrders.size() + "件の注文をキャンセルし、在庫を戻しました。");
    }
    
    /**
     * 会員の全注文を取得 / 获取会员的所有订单
     */
    public List<Order> getOrdersByMember(Long memberId) {
        return orderMapper.selectByMemberId(memberId);
    }
    
    /**
     * 注文詳細を取得（明細付き） / 获取订单详情（含明细）
     */
    public Order getOrderDetail(Long orderId, Long memberId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("注文が見つかりません。");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new RuntimeException("権限がありません。");
        }
        // 明細をセット（別途 Order クラスに List<OrderItem> フィールドを追加推奨）
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        order.setItems(items); // Order クラスに List<OrderItem> items を追加しておく必要あり
        return order;
    }

    public List<Order> getAllOrders() {
        return orderMapper.selectAll();
}

    @Transactional
    public Order updateStatus(Long id, Integer status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("注文が見つかりません。ID: " + id);
        }
        orderMapper.updateStatus(id, status);
        return orderMapper.selectById(id);
    }
    

}
