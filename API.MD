EC-Mall API ドキュメント（日本語）
ベースURL: http://localhost:8888
文字コード: UTF-8
レスポンス形式: JSON

🔐 認証について
ログインAPI（/api/auth/login）を除くすべてのAPIは、JWTトークンが必要です。

トークンはリクエストヘッダー Authorization: Bearer {token} に設定してください。

トークンの有効期限は application.yml の jwt.expiration で設定（デフォルト10分）。

📦 共通レスポンス形式
成功時
ステータスコード 200 または 204 で、必要に応じてJSONデータを返します。

エラー時
json
{
  "status": 400,
  "message": "エラーメッセージ",
  "errors": [
    {
      "field": "フィールド名",
      "message": "バリデーションエラーメッセージ"
    }
  ]
}
ステータスコード	意味
400	バリデーションエラー / 不正なリクエスト
401	JWTがない、または無効（期限切れ含む）
404	リソースが見つからない
409	重複（例：メールアドレス既存）
500	サーバー内部エラー
👤 会員認証（Auth）
1. ログイン
POST /api/auth/login

リクエストボディ:

json
{
  "email": "ichiro.suzuki@example.com",
  "password": "test1234"
}
レスポンス:

json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
備考: メールまたはパスワードが正しくない場合は 401 を返します。

2. ログイン会員情報取得
GET /api/auth/me
認証: 必須（Bearer Token）

レスポンス（会員情報）:

json
{
  "id": 1,
  "name": "鈴木一郎",
  "email": "ichiro.suzuki@example.com",
  "age": 30,
  "createdAt": "2026-08-28T10:00:00"
}
👥 会員管理（Member）
メソッド	エンドポイント	認証	説明
POST	/api/members	❌	新規会員登録
GET	/api/members	✅	全会員一覧取得（管理者用）
GET	/api/members/{id}	✅	会員詳細取得
PUT	/api/members/{id}	✅	会員情報更新
DELETE	/api/members/{id}	✅	会員削除
POST /api/members（新規登録）
リクエストボディ:

json
{
  "name": "山田花子",
  "email": "hanako@example.com",
  "age": 25,
  "password": "password123"
}
バリデーション:

name: 必須

email: 必須、メール形式

age: 0〜150

password: 6〜20文字

レスポンス: 登録された会員情報（passwordHashは含まれません）

重複メールの場合: 409 Conflict

PUT /api/members/{id}
リクエストボディ（全フィールド任意）:

json
{
  "name": "山田花子改",
  "email": "new@example.com",
  "age": 26,
  "password": "newpass456"
}
レスポンス: 更新後の会員情報

DELETE /api/members/{id}
レスポンス: 204 No Content

📦 商品管理（Product）
メソッド	エンドポイント	認証	説明
GET	/api/products	❌	全商品一覧
GET	/api/products/{id}	❌	商品詳細
GET	/api/products/search	❌	商品検索（分頁）
POST	/api/products	✅	新規商品登録
PUT	/api/products/{id}	✅	商品更新
DELETE	/api/products/{id}	✅	商品削除
GET /api/products/search（検索・分頁）
クエリパラメータ（すべて任意）:

パラメータ	型	説明
keyword	string	商品名に部分一致
category	string	カテゴリ完全一致
minPrice	double	下限価格
maxPrice	double	上限価格
page	int	ページ番号（デフォルト1）
size	int	1ページあたり件数（デフォルト10、最大100）
レスポンス:

json
{
  "content": [
    {
      "id": 1,
      "name": "Apple iPhone 15 Pro",
      "description": "...",
      "price": 8999.00,
      "stock": 50,
      "category": "スマートフォン",
      "imageUrl": "https://...",
      "status": 1,
      "createdAt": "...",
      "updatedAt": "..."
    }
  ],
  "totalElements": 22,
  "totalPages": 3,
  "currentPage": 1,
  "pageSize": 10
}
POST /api/products（新規追加）
認証必須
リクエストボディ:

json
{
  "name": "商品名",
  "description": "説明",
  "price": 1000.00,
  "stock": 10,
  "category": "カテゴリ",
  "imageUrl": "https://..."
}
バリデーション:

name: 必須

price: 必須、0より大きい

stock: 必須、0以上

レスポンス: 作成された商品（id 自動生成）

PUT /api/products/{id}
認証必須
リクエストボディ（全フィールド任意）:

json
{
  "name": "新商品名",
  "price": 1200.00,
  "stock": 5,
  "status": 0   // 0:非公開, 1:公開
}
レスポンス: 更新後の商品

DELETE /api/products/{id}
認証必須
レスポンス: 204 No Content

🛒 カート管理（Cart）
メソッド	エンドポイント	認証	説明
GET	/api/cart	✅	カート詳細取得
POST	/api/cart/items	✅	商品をカートに追加
PUT	/api/cart/items/{productId}	✅	数量更新
DELETE	/api/cart/items/{productId}	✅	特定商品をカートから削除
DELETE	/api/cart	✅	カートを空にする
GET /api/cart
レスポンス:

json
{
  "cartId": 1,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "imageUrl": "https://...",
      "quantity": 2,
      "priceAtAdd": 8999.00,
      "currentPrice": 8999.00
    }
  ],
  "totalPrice": 17998.00
}
POST /api/cart/items
リクエストボディ:

json
{
  "productId": 1,
  "quantity": 2
}
動作:

同一商品が既に存在する場合は数量を加算

存在しない場合は新規追加（その時点の商品価格をスナップショット保存）

レスポンス: 200 OK（ボディなし）

PUT /api/cart/items/{productId}?quantity=5
クエリパラメータ:

quantity: 新しい数量（必須、1以上）

レスポンス: 200 OK

DELETE /api/cart/items/{productId}
レスポンス: 204 No Content

DELETE /api/cart
レスポンス: 204 No Content

📝 注文管理（Order）
メソッド	エンドポイント	認証	説明
POST	/api/orders	✅	注文作成（カート全商品を対象）
POST	/api/orders/{id}/pay	✅	支払い処理（モック）
GET	/api/orders	✅	自分の注文一覧
GET	/api/orders/{id}	✅	注文詳細（明細付き）
POST /api/orders（注文作成）
リクエストボディ:

json
{
  "shippingAddress": "東京都新宿区...",
  "receiverName": "山田太郎",
  "receiverPhone": "090-1234-5678"
}
処理フロー:

カートの全商品を取得

各商品の在庫をチェックし、不足があればロールバック

在庫を減算（UPDATE ... WHERE stock >= quantity で楽観的ロック）

注文番号生成（例: ORD202608311234561234）

注文主テーブルに挿入（ステータス 0 = 未払い）

注文明細を挿入（商品名・価格をスナップショット保存）

カートを空にする

レスポンス: 作成された注文（id, orderNo, totalAmount, status 等）

POST /api/orders/{id}/pay（支払いモック）
認証必須
パス: id = 注文ID

動作:

注文が自分のものであることをチェック

ステータスが 0（未払い）の場合のみ、1（支払済み）に更新

既に支払済みまたはキャンセルの場合はエラー

レスポンス: 更新後の注文

GET /api/orders（注文一覧）
レスポンス: 自分の注文リスト（作成日降順）

GET /api/orders/{id}（詳細）
レスポンス:

json
{
  "id": 1,
  "orderNo": "ORD202608311234561234",
  "memberId": 1,
  "orderDate": "2026-08-31T12:34:56",
  "totalAmount": 17998.00,
  "status": 1,
  "shippingAddress": "東京都新宿区...",
  "receiverName": "山田太郎",
  "receiverPhone": "090-1234-5678",
  "createdAt": "...",
  "updatedAt": "...",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "price": 8999.00,
      "quantity": 2
    }
  ]
}
⏰ バッチ処理（非API）
タイムアウト注文の自動キャンセル: 5分ごとにバッチが実行され、作成から15分以上経過した未払い注文をキャンセル（ステータス 4）し、在庫を戻します。

実行間隔: fixedDelay = 300000（5分）

📊 データベースER図（簡易）
text
members (会員)
  └─┬─ cart (カート)
    └─ cart_item (カート明細) ──┬─ products (商品)
                                │
orders (注文) ── order_item (注文明細) ─┘
cart は member_id で1対1

cart_item は product_id で商品を参照

order_item は product_id と order_id を保持（商品名・価格はスナップショット）

🔧 セットアップメモ
ポート: 8888

データベース: MySQL（ec_mall）

JWTシークレット: application.yml の jwt.secret

パスワードエンコード: BCrypt

