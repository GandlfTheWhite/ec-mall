-- 商品テーブル
CREATE TABLE IF NOT EXISTS ec_mall.products (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
                                                name VARCHAR(100) NOT NULL COMMENT '商品名',
    description TEXT COMMENT '商品説明',
    price DECIMAL(10, 2) NOT NULL COMMENT '価格（円）',
    stock INT NOT NULL DEFAULT 0 COMMENT '在庫数',
    category VARCHAR(50) COMMENT 'カテゴリ',
    image_url VARCHAR(255) COMMENT '画像URL',
    status TINYINT DEFAULT 1 COMMENT 'ステータス（1:公開、0:非公開）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品マスタ';

-- カート主テーブル（会員1人につき1レコード）
CREATE TABLE IF NOT EXISTS ec_mall.cart (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'カートID',
                                            member_id BIGINT NOT NULL UNIQUE COMMENT '会員ID（一意制約）',
                                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
                                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='カートマスタ';

-- カート明細テーブル（実際の商品を保持）
CREATE TABLE IF NOT EXISTS ec_mall.cart_item (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明細ID',
                                                 cart_id BIGINT NOT NULL COMMENT 'カートID',
                                                 product_id BIGINT NOT NULL COMMENT '商品ID',
                                                 quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
                                                 price_at_add DECIMAL(10, 2) NOT NULL COMMENT '追加時点の価格（スナップショット）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    FOREIGN KEY (cart_id) REFERENCES ec_mall.cart(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ec_mall.products(id),
    UNIQUE KEY uk_cart_product (cart_id, product_id) -- 同一商品は1レコードに制限
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='カート明細';


-- 注文主テーブル
CREATE TABLE IF NOT EXISTS ec_mall.orders (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '注文ID',
                                              order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '注文番号（例：ORD20260828123456）',
                                              member_id BIGINT NOT NULL COMMENT '会員ID',
                                              order_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注文日時',
                                              total_amount DECIMAL(10, 2) NOT NULL COMMENT '合計金額',
                                              status TINYINT NOT NULL DEFAULT 0 COMMENT 'ステータス（0:未払い、1:支払済み、2:発送済み、3:完了、4:キャンセル）',
                                              shipping_address VARCHAR(255) COMMENT '配送先住所',
                                              receiver_name VARCHAR(50) COMMENT '受取人氏名',
                                              receiver_phone VARCHAR(20) COMMENT '受取人電話番号',
                                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
                                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注文マスタ';

-- 注文明細テーブル（注文時点の商品名と価格をスナップショットとして保持）
CREATE TABLE IF NOT EXISTS ec_mall.order_item (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '注文明細ID',
                                                  order_id BIGINT NOT NULL COMMENT '注文ID',
                                                  product_id BIGINT NOT NULL COMMENT '商品ID',
                                                  product_name VARCHAR(100) NOT NULL COMMENT '商品名（注文時点のスナップショット）',
                                                  price DECIMAL(10, 2) NOT NULL COMMENT '単価（注文時点のスナップショット）',
                                                  quantity INT NOT NULL COMMENT '数量',
                                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
                                                  FOREIGN KEY (order_id) REFERENCES ec_mall.orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注文明細';