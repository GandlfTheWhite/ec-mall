-- 如果之前有测试数据，可以先清空表（谨慎操作！）
--

TRUNCATE TABLE ec_mall.products;
INSERT INTO ec_mall.products (name, description, price, stock, category, image_url, status) VALUES
                                                                                                ('Apple iPhone 15 Pro', 'A17 Proチップ、チタニウムフレーム、4800万画素カメラ', 8999.00, 50, 'スマートフォン', 'https://example.com/iphone15pro.jpg', 1),
                                                                                                ('Apple iPhone 15', 'A16 Bionicチップ、ダイナミックアイランド、USB-C', 6999.00, 80, 'スマートフォン', 'https://example.com/iphone15.jpg', 1),
                                                                                                ('Samsung Galaxy S24 Ultra', 'AIスマートフォン、Sペン内蔵、2億画素カメラ', 7999.00, 30, 'スマートフォン', 'https://example.com/galaxys24.jpg', 1),
                                                                                                ('Sony WH-1000XM5', '業界最高峰のノイズキャンセリング、30時間再生', 4599.00, 120, 'イヤホン・ヘッドホン', 'https://example.com/sonywh.jpg', 1),
                                                                                                ('Apple AirPods Pro 2', 'アクティブノイズキャンセリング、空間オーディオ', 3499.00, 200, 'イヤホン・ヘッドホン', 'https://example.com/airpodspro.jpg', 1),
                                                                                                ('MacBook Pro 16インチ', 'M3 Maxチップ、36GBメモリ、1TB SSD', 34999.00, 15, 'パソコン', 'https://example.com/macbookpro.jpg', 1),
                                                                                                ('MacBook Air 13インチ', 'M3チップ、軽量設計、最大18時間駆動', 15999.00, 25, 'パソコン', 'https://example.com/macbookair.jpg', 1),
                                                                                                ('Lenovo ThinkPad X1 Carbon', 'ビジネス向け軽量ノートPC、4Kディスプレイ', 24999.00, 10, 'パソコン', 'https://example.com/thinkpad.jpg', 1),
                                                                                                ('Dell XPS 15', 'インテルCore Ultra、RTX 4060、クリエイター向け', 22999.00, 8, 'パソコン', 'https://example.com/dellxps.jpg', 1),
                                                                                                ('Anker 100W USB-C 充電器', '小型軽量、急速充電、GaN技術搭載', 999.00, 500, 'アクセサリー', 'https://example.com/anker.jpg', 1),
                                                                                                ('Belkin ガラスフィルム iPhone用', '強化ガラス、指紋防止、衝撃吸収', 499.00, 1000, 'アクセサリー', 'https://example.com/belkin.jpg', 1),
                                                                                                ('Samsung 45W 急速充電器', '超急速充電、USB-C対応', 799.00, 300, 'アクセサリー', 'https://example.com/samsungcharger.jpg', 1),
                                                                                                ('LG 27インチ 4K モニター', 'IPSパネル、HDR10対応、USB-C給電', 15999.00, 20, '周辺機器', 'https://example.com/lgmonitor.jpg', 1),
                                                                                                ('Keychron K3 ワイヤレスキーボード', '薄型メカニカル、マルチデバイス対応', 1299.00, 60, '周辺機器', 'https://example.com/keychron.jpg', 1),
                                                                                                ('Logitech MX Master 3S', '静音クリック、8K DPI、MagSpeedホイール', 1499.00, 45, '周辺機器', 'https://example.com/logitechmx.jpg', 1),
                                                                                                ('iPad Pro 12.9インチ', 'M2チップ、XDRディスプレイ、Apple Pencil対応', 16999.00, 40, 'タブレット', 'https://example.com/ipadpro.jpg', 1),
                                                                                                ('iPad 10.9インチ', 'A14 Bionic、USB-C、カラフルデザイン', 7999.00, 55, 'タブレット', 'https://example.com/ipad.jpg', 1),
                                                                                                ('Apple Watch Series 9', '常時表示ディスプレイ、S9チップ、ダブルタップ', 4599.00, 70, 'ウェアラブル', 'https://example.com/applewatch.jpg', 1),
                                                                                                ('Garmin Forerunner 265', 'GPS搭載ランニングウォッチ、AMOLEDディスプレイ', 5999.00, 25, 'ウェアラブル', 'https://example.com/garmin.jpg', 1),
                                                                                                ('Nintendo Switch OLED', '有機ELディスプレイ、TVモード・携帯モード対応', 3999.00, 100, 'ゲーム', 'https://example.com/switch.jpg', 1),
                                                                                                ('PlayStation 5', 'Ultra HD Blu-ray、DualSenseコントローラー', 7999.00, 10, 'ゲーム', 'https://example.com/ps5.jpg', 1),
                                                                                                ('Xbox Series X', '12 Teraflops、4K 120FPS', 7499.00, 8, 'ゲーム', 'https://example.com/xbox.jpg', 1);

INSERT INTO ec_mall.members (name, email, age, password_hash, role)
VALUES (
    'システム管理者',
    'admin@example.com',
    30,
    '$2a$10$...',  -- BCrypt ハッシュ化した「admin123」を入れる
    'ADMIN'
);

