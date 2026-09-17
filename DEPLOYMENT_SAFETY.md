# ローカル開発と AWS 本番環境の分離

更新日：2026年9月24日

## 今回の問題

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

## 参考資料

- [Spring Boot の外部設定](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Boot のプロファイル](https://docs.spring.io/spring-boot/reference/features/profiles.html)
- [AWS EB の環境プロパティ](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/environments-cfg-softwaresettings.html)
