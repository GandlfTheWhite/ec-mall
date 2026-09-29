# 商品画像アップロード

## 実装範囲

- 商品管理の登録・編集ダイアログで、管理者がローカル画像を選択してアップロードする。
- Vue → 認証付き multipart POST → Spring Boot → AWS SDK v2 の S3 PutObject。
- 成功時に返された `imageUrl` をフォームへ反映し、「保存する」で既存の商品APIへ送信する。
- `products.image_url` をそのまま使う。テーブル追加・画像バイナリのDB保存は行わない。
- 手入力の画像URLも従来どおり使用できる。表示文言は日本語。
- 実装とローカルテストだけでは、本番AWSの設定や実アップロードは完了しない。

## 制限と保存形式

- JPEG・PNG・WebPのみ。1枚5 MiB（5 × 1024 × 1024バイト）以下。
- 各辺8192px以下、幅×高さが1600万画素以下。解凍後の極端なメモリー消費を制限する。
- 拡張子だけを信用せず、ImageIO（WebPはTwelveMonkeys）でヘッダーと画素を読み取り、申告MIMEとも照合する。
- サーバー生成の `products/<UUID>.jpg`、`.png`、`.webp` で保存。利用者のファイル名はS3キーに使用しない。
- 元の画像を保存する。自動縮小、再圧縮、EXIF削除、アニメーションの全フレーム検証は今回の対象外。
- 同じキーを使い回さないため、画像には長期キャッシュを設定する。商品画像変更のたびに新しいURLになる。
- URLはHTTPSの固定配信元＋キー。期限付きの署名URLをDBに保存しない。
- `image_url` の255文字制限に収まるよう、配信元URLの長さもチェックする。

## AWSの準備（手動設定が必要）

### 1. 商品画像専用の非公開S3バケット

フロントエンドの配信先とは**別のバケット**を用意する。
既存のフロントエンド配信は `aws s3 sync --delete` を使うため、同じバケットに商品画像を置かない。
Block Public Accessを有効にし、Object OwnershipはBucket owner enforced、暗号化はSSE-S3を基本とする。
アプリは公開ACLを設定しない。画像用の静的ウェブサイトホスティングは不要。
本番用とローカル検証用も可能なら別バケットにする。

### 2. CloudFrontで画像を配信

画像専用のCloudFront配信を作成し、OriginにS3の通常のバケットエンドポイントを指定する。
OACを使い、次のような読み取り許可を画像バケットに設定する。プレースホルダーは自分のリソースに置き換える。

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "AllowImageCloudFrontRead",
    "Effect": "Allow",
    "Principal": { "Service": "cloudfront.amazonaws.com" },
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::YOUR_IMAGE_BUCKET/products/*",
    "Condition": {
      "StringEquals": {
        "AWS:SourceArn": "arn:aws:cloudfront::YOUR_ACCOUNT_ID:distribution/YOUR_IMAGE_DISTRIBUTION_ID"
      }
    }
  }]
}


CloudFrontのOrigin pathは空にし、`https://画像配信ドメイン/products/...` がS3の `products/...` に対応する構成にする。
Viewer protocol policyはHTTPSへ転送し、GET/HEADを許可する。画像はCloudFront経由で一般閲覧可能になる。
ブラウザーからS3へ直接PUTする設計ではないため、アップロード用S3 CORS設定は不要。
設定詳細は [CloudFront OACの公式説明](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html) を参照。

### 3. EBのEC2インスタンスロールに書き込み権限

**GitHub Actionsのデプロイ用IAMユーザーではなく、Javaアプリが動くEBのEC2インスタンスプロファイルのロール**に追加する。
EBサービスロールとも区別する。次はこの機能に必要な追加権限の例であり、既存のEB権限を置き換えるものではない。

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": "s3:PutObject",
    "Resource": "arn:aws:s3:::YOUR_IMAGE_BUCKET/products/*"
  }]
}
```

アプリに画像削除、全バケット参照、公開ACL変更の権限は不要。
独自のSSE-KMSキーを使う場合は別途KMS権限とキーのポリシーも確認する。
SDKは [標準の認証チェーン](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html) を使い、アクセスキーをコードへ記述しない。

### 4. EBの環境プロパティ

以下は **EBの実行時設定**。GitHubのRepository variablesやVueの `VITE_*` 変数ではない。

| 環境変数 | 値 |
| --- | --- |
| `PRODUCT_IMAGES_ENABLED` | 準備完了後に `true`。未設定時は `false` |
| `PRODUCT_IMAGES_BUCKET` | ec-mall-product-images-zyd |
| `PRODUCT_IMAGES_REGION` | そのバケットのAWSリージョン（例：`us-east-1`） |
| `PRODUCT_IMAGES_BASE_URL` | `https://画像用CloudFrontドメイン`。パス・クエリは付けない |
| `FRONTEND_S3_BUCKET` | ec-mall-frontend-zyd |

画像用とフロントエンド用の名前が同じ場合は起動・配信前チェックで拒否する。
`FRONTEND_S3_BUCKET` は実際のフロントエンドworkflowの `S3_BUCKET` と一致させる。
画像用バケットをフロントエンドworkflowの `S3_BUCKET` に設定しない。
未設定時は既存アプリを起動でき、画像アップロードだけが503となる。画像URLの手入力は利用可能。
`PRODUCTION_DEPLOY_ENABLED` は引き続きGitHub上の**配信許可**、`PRODUCT_IMAGES_ENABLED` は**画像機能の有効化**であり、別の設定。

ローカルでは同じ画像設定をIDEのRun Configurationへ追加する。
認証には検証用の一時的な `AWS_ACCESS_KEY_ID`、`AWS_SECRET_ACCESS_KEY`、`AWS_SESSION_TOKEN` を使用できる。
キーをソース、チャット、フロントエンド、コミット対象ファイルへ保存しない。

## 配信経路とEBパッケージの変更

Javaのmultipart上限は1ファイル5MB、リクエスト全体6MB。
`.platform/nginx/conf.d/20_upload_size.conf` でもリクエスト上限6mを指定している。
対象はNginxを使うAmazon Linux 2/2023のJava SEプラットフォーム。
既に独自Nginx設定がある場合は重複・より小さい上限がないことを実環境で確認する。

`.platform` は単体JARの中に入れてもEBが設定として読み込まないため、バックエンドの配信物をZIPへ変更した。
`.github/scripts/package_backend.py` が検証済みJAR、Procfile、Nginx設定だけを次の形で格納する。

```text
ec-mall-eb.zip
├── application.jar
├── Procfile
└── .platform/nginx/conf.d/20_upload_size.conf
```

Procfileは `java -jar application.jar` で起動する。既存のDB接続設定は変更しない。
GitHub Actionsのテスト必須、master限定、配信開閉スイッチ、EB設定事前確認は維持する。
画像機能を有効にした場合は、専用バケット・リージョン・配信元URLも配信前チェックの対象。
このチェックはS3の実在、IAMの実効権限、CloudFront到達性まで保証しない。
参考：[EBのプロキシ拡張](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/platforms-linux-extend.proxy.html)、[Java SEソースバンドル](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-se-platform.html)。

フロントエンドの `/api/*` がCloudFrontを通る場合、EB向けBehaviorでPOSTを許可し、AuthorizationとContent-Typeを転送する。
認証APIはキャッシュしない。追加のプロキシ・WAFがある場合も5MB画像を通せる制限を確認する。
前後のworkflowは独立しているため、初回はバックエンドの配信と設定確認を先に行う。

## API

`POST /api/admin/product-images`、Authorizationは管理者のBearer JWT。
`multipart/form-data` の `file` フィールドに画像を指定する。boundaryはブラウザーが設定する。

成功時（201）の例：

```json
{
  "imageUrl": "https://images.example.com/products/550e8400-e29b-41d4-a716-446655440000.png",
  "key": "products/550e8400-e29b-41d4-a716-446655440000.png"
}
```

| ステータス | 意味 |
| --- | --- |
| 400 | 空、欠落、形式不正、MIME不一致、破損、画素数上限超過 |
| 401 / 403 | 未認証・無効JWT / 管理者以外 |
| 413 | ファイルまたはリクエスト全体が上限超過 |
| 502 | S3保存失敗。権限、リージョン、ネットワークなどを確認 |
| 503 | 画像機能が未有効化 |

S3へ保存しただけでは商品は更新されない。画面の「保存する」操作で初めて商品APIへURLを送信する。
アップロード失敗時は元のURLを保持し、アップロード中の保存・二重実行を防ぐ。
画面遷移時は通信を中断し、古い応答を別の商品フォームへ反映しない。
通信中断や商品保存の取消後にS3オブジェクトが残る場合がある。
旧画像・未使用画像の自動削除は未実装。他の商品が参照する可能性があるため、置換時に削除しない。
`products/*` 全体への一律の有効期限設定は使用中の画像も消すため避ける。将来は参照照合による清掃を検討する。

## 検証

```powershell
# リポジトリ直下
mvn verify
python -B -m unittest discover -s .github/scripts -p 'test_*.py'
python -B .github/scripts/package_backend.py
cd ec-mall-frontend
npm.cmd test
npm.cmd run build
```

自動テストはS3と認証サービスをモック化し、またはインメモリーDBを使用する。AWSへ画像を送信しない。
画像の内容判定・上限、権限制御、S3リクエスト、保存とアップロードの分離、失敗時のURL保持、EB配信物を検証する。

今回のローカル実行結果：Java 35件、フロントエンド17件、配信設定・パッケージ検証11件がすべて成功。
JAR、EB用ZIP、フロントエンドのビルドも成功した。フロントエンドには既存のチャンク容量警告が残る。

実環境での受入確認（別途実施）：

1. 一般会員でアップロードできず、管理者ではJPEG・PNG・WebPが成功する。
2. 1MB超～5MBの画像も成功する。5MB超、SVG、偽装ファイルは拒否される。
3. 登録・編集で画像URLが自動入力され、保存後の一覧・詳細ページで表示される。
4. S3の直接アクセスは拒否され、CloudFrontの画像URLでは表示される。
5. フロントエンド再配信後も商品画像が残る。
6. S3権限不足時は日本語のエラーとなり、元の商品画像が変更されない。

今回、AWSリソースの作成、IAM設定変更、本番配信、実ブラウザーでの受入確認は行わない。
