CREATE TABLE IF NOT EXISTS ec_mall.products (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
                                                name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品价格（单位：元）',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    category VARCHAR(50) COMMENT '商品分类',
    image_url VARCHAR(255) COMMENT '商品图片URL',
    status TINYINT DEFAULT 1 COMMENT '状态：1-上架，0-下架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
