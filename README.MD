以下のリンクで開きます
d3pjmlbpe1u7rp.cloudfront.net

以下のユーザーで登録
メールアドレス　user@example.com
パスワード　user123

EC-Mall API ドキュメント（日本語）

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

# ローカル開発と AWS 本番環境の分離

以前の設定 `RDS_HOSTNAME:localhost` は「環境変数がなければ localhost を使う」という意味。
AWS の接続先を必ず上書きする設定ではないが、設定漏れがあったときにローカル接続へ戻る危険がある。
今回、本番設定からホスト・ユーザー・パスワードの既定値をなくした。

## 設定ファイル

- `src/main/resources/application.yml`：共通設定。プロファイル未指定時は `prod`。
- `src/main/resources/application-prod.yml`：既存の `RDS_*` 環境変数から接続情報を取得。実際のパスワードや接続先をコミットしない。
- `src/main/resources/application-local.yml`：`local` を明示した場合のみ localhost に接続。開発用ポートは8888。
- 本番では `local` プロファイルを指定しない。本番の接続設定を手元のローカル実行環境へコピーしない。
- `SPRING_DATASOURCE_*` や `SPRING_APPLICATION_JSON` は Spring の設定を上書きできる。今回の事前確認では、予期しない接続先を避けるため、それらがある場合は配信を停止して確認を求める。

## ローカルで起動する場合

IDE の Run Configuration の環境変数に以下を設定する。実際の値は Git に保存しない。

| 変数 | 設定内容 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `local` |
| `LOCAL_DB_USERNAME` | ローカル MySQL のユーザー。省略時は `root` |
| `LOCAL_DB_PASSWORD` | ローカル MySQL のパスワード |
| `LOCAL_JWT_SECRET` | 32バイト以上のランダムな鍵を Base64 化した値 |
| `LOCAL_DB_PORT` | 必要な場合のみ。省略時は3306 |

Maven から起動する場合も、ローカル用の環境変数を設定した上で実行する。

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

フロントエンドは `ec-mall-frontend` フォルダーで `npm.cmd run dev` を実行する。
RDS 接続は行わず、既存の開発プロキシから8888番ポートへ転送する。

## AWS EB で確認する環境変数

EB コンソールで対象の本番環境を選択し、Configuration 内の環境プロパティを確認する。
リポジトリへ値を書き戻したり、チャットへパスワードを貼り付けたりしない。

| 変数 | 必要な設定 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` を推奨。省略時も現在の共通設定では `prod` |
| `RDS_HOSTNAME` | RDS エンドポイント。localhost や URL 全体ではない |
| `RDS_PORT` | 通常3306。省略時も3306 |
| `RDS_DB_NAME` | `ec_mall`。現在の Mapper にスキーマ名の明示があるため |
| `RDS_USERNAME` | 本番データベースのユーザー |
| `RDS_PASSWORD` | 本番データベースのパスワード |
| `JWT_SECRET` | 32バイト以上の鍵を Base64 化した値 |

`RDS_PASSWORD` と `JWT_SECRET` は EB の Secrets Manager／SSM 参照方式も事前確認の対象とする。
その場合、事前確認で検証するのは参照の設定有無であり、シークレットの取得権限や実際の値ではない。

既存の環境が EB 管理の RDS 自動注入だけに依存しており、ConfigurationSettings に変数が現れない場合、
今回の事前確認は安全のため停止する。設定方式を確認してから検証方法を調整し、検証を単純に削除しない。

以前コード内の既定 JWT 鍵を使っていた場合は、本番用の秘密鍵を設定し直す必要がある。
鍵を変更すると、以前発行したログイントークンは使えなくなる。

## GitHub Actions の新しい動作

1. `dev` への push：対象ファイルの変更に応じてテスト・ビルドのみ実行する。
2. `master`／`dev` 向け Pull Request：対象ファイルの変更に応じてテスト・ビルドのみ実行する。
3. `master` への push：まずテスト・ビルドする。本番配信は、明示的な有効化と検証成功が必要。
4. 手動実行も、`master` 以外では本番配信しない。

**既定では本番配信を無効にしている。** この変更が GitHub に反映された後、
リポジトリ変数 `PRODUCTION_DEPLOY_ENABLED` が `true` でなければ、配信ジョブはスキップされる。
変数が未設定でもエラーではなく、意図した停止状態となる。
なお、この保護が有効になるのは変更後のワークフローが対象ブランチに反映されてからであり、
現在 GitHub 上で実行中の古いワークフローを取り消すものではない。

本番配信を再開する前の手順：

1. AWS EB の上記変数、DB 接続権限、セキュリティグループを確認する。
2. GitHub の Settings → Environments で `production` を設定し、配信可能なブランチを `master` に制限する。必要に応じて Required reviewers を設定する。
3. 既存の AWS・S3・CloudFront・EB の GitHub Secrets を確認する。AWS 認証を新たにコードへ書かない。
4. AWS のデプロイ用権限に、対象環境の `elasticbeanstalk:DescribeConfigurationSettings` が必要。取得失敗時は本番配信を停止する。
5. リポジトリの Settings → Secrets and variables → Actions → Variables に、`PRODUCTION_DEPLOY_ENABLED=true` を設定する。ジョブ条件に使うため、Environment の変数ではなくリポジトリ変数として設定する。
6. 最初はバックエンドを手動実行し、EB の稼働状態を確認してからフロントエンドを配信する。

バックエンドは配信直前に EB 設定を読み取り、未設定、ローカル接続先、想定外の設定上書きを検出すると停止する。
検証は値をログやファイルへ出力せず、DB 接続や設定変更も行わない。
AWS のネットワーク到達性・パスワードの正しさ・実際の稼働確認まで保証する検証ではない。

フロントエンドとバックエンドのワークフローは別々に実行される。完全な同時切替ではないため、
互換性のない変更はバックエンドの更新と稼働確認を先に行う。
本番配信は GitHub Actions から実行する。誤配信を防ぐため、ローカルの `npm run deploy` スクリプトは削除した。

## 今回の検証範囲と未実施事項

2026年9月24日：Java の23件、EB 設定事前確認の7件が成功し、バックエンドの JAR 生成も成功した。

追加した自動テストは、プロファイル選択、環境変数の解決、本番設定にローカル既定値がないこと、
ワークフローの配信条件、EB 設定事前確認の成功・失敗を扱う。
アプリの DB 接続は起動せず、実際の RDS への操作も行わない。

AWS と GitHub の管理設定は今回変更していない。コミット・push・デプロイも実行していない。
旧設定の SSL 無効化指定は引き継いでいない。実際の RDS の TLS 要件と接続確認は別途必要。
公開済みの設定や認証情報の過去履歴を削除・無効化する作業も別途必要。
`aws/` は誤コミット防止のため除外したが、既に公開してしまった鍵を無効化する効果はない。

## 商品画像アップロード

管理画面の「商品を登録／編集」でJPEG・PNG・WebP画像（5MB以下）を選択し、商品を保存すると、
`POST /api/admin/product-images` に `file`（multipart/form-data）を送る。
このAPIは管理者のJWTが必要。S3へのアップロード後に `{ "imageUrl": "https://..." }` を返し、
既存の `POST/PUT /api/products` がそのURLを `products.image_url` に保存する。
ファイルを選ばず既存の画像URLを入力する操作も引き続き可能。

本番のEB環境プロパティには、画像用の**別のS3バケット**を用意した上で以下を設定する。
フロントエンド配信用バケットは `aws s3 sync --delete` を使用するので画像保存に使わない。

| 変数 | 内容 |
| --- | --- |
| `IMAGE_UPLOAD_ENABLED` | `true` でアップロードを有効化（未指定では無効） |
| `IMAGE_BUCKET` | 画像用S3バケット名 |
| `IMAGE_REGION` | 画像バケットのAWSリージョン（例：`ap-northeast-1`） |
| `IMAGE_PUBLIC_BASE_URL` | 画像バケットをオリジンとするCloudFrontのHTTPS URL（末尾の `/` は省略可） |

画像用バケットは非公開にし、CloudFrontのOACで読み取りを許可する。
EBの**EC2インスタンスプロファイル**には画像用バケットの `products/*` に対する `s3:PutObject` を付与する。
アプリにはAWSアクセスキーを書かず、SDKの標準認証情報チェーンを使用する。
ローカルで実際に画像を送る場合も同じ4変数と、権限を持つローカルAWS認証情報が必要。
設定しない場合は既存の画像URL入力が使え、アップロードAPIは503を返す。

画像は一商品につき一枚。画像を置き換えたり商品を削除したりしても、以前のS3オブジェクトは
自動削除しない。商品保存に失敗した場合もアップロード済み画像が残ることがあるため、運用時には
不要な `products/` オブジェクトを確認・整理する。画像のリサイズも現時点では行わない。
