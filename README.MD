# ECサイト管理システム

├── 商品管理

├── カテゴリ管理

├── ブランド管理

├── 会員管理

├── 注文管理 

├── 在庫管理


## 1. プロジェクト概要

本プロジェクトは、ECサイトにおける商品・会員・注文・在庫などの管理機能を想定した学習用Webアプリケーションです。

Spring Bootを中心に、MySQL、Redis、Nginx、Dockerなどを利用し、バックエンド開発およびインフラ環境構築について実践的に学習することを目的としています。

現在は、Spring BootによるREST APIの作成、MySQLとの接続、MyBatisを利用したデータ取得処理の実装を進めています。

## 2. 開発目的

* JavaおよびSpring Bootを用いたWeb API開発スキルの向上
* Controller、Service、Mapperによるレイヤー構成の理解
* MySQLを利用したデータベース操作の習得
* Dockerを利用した開発環境構築の習得
* Nginxによる静的コンテンツ配信およびリバースプロキシの理解
* Redisを利用したキャッシュ処理の学習
* 日本語による設計内容・開発内容の説明力向上

## 3. 使用技術

### バックエンド

* Java 17
* Spring Boot
* Spring MVC
* Maven
* MyBatis

### データベース・ミドルウェア

* MySQL 5.7
* Redis 6.2.6
* Nginx 1.28.0

### インフラ・開発環境

* Docker
* CentOS 7
* Vagrant
* VirtualBox
* IntelliJ IDEA

## 4. システム構成

```text
ブラウザ
   ↓
Nginx
   ↓
Spring Boot
   ↓
Service
   ↓
Mapper
   ↓
MySQL / Redis
```

Nginxは、静的コンテンツの配信およびバックエンドへのリバースプロキシとして利用します。

Spring BootはREST APIを提供し、MyBatisを通じてMySQLへアクセスします。

## 5. 現在実装済みの内容

* Spring Bootプロジェクトの作成
* REST APIの基本実装
* Javaオブジェクト、List、MapのJSONレスポンス確認
* ControllerとServiceの基本的なレイヤー分離
* DockerによるMySQL、Redis、Nginx環境の構築
* Nginxのlocation設定
* Nginxによるリバースプロキシの動作確認
* MySQL接続設定
* MyBatis導入準備

## 6. 今後の実装予定

* MyBatisを利用したユーザー情報取得
* 登録・更新・削除を含むCRUD処理
* 商品管理機能
* カテゴリ管理機能
* ブランド管理機能
* 会員管理機能
* 注文管理機能
* 在庫管理機能
* Redisを利用したキャッシュ処理
* 統一レスポンス形式の導入
* 入力値チェックおよび例外処理
* NginxとSpring Bootの連携
* Docker Composeによる環境統合

## 7. 学習・工夫した点

Dockerコンテナを単に起動するだけではなく、設定ファイル、データ、ログをホスト側へマウントし、コンテナを再作成してもデータを保持できる構成としました。

また、Nginxについては、locationのマッチングルールやリバースプロキシの動作を、複数のNginxコンテナを利用して確認しました。

Spring Bootでは、REST APIの基本的な処理フローを理解し、Controller、Service、Mapperの役割を意識した構成で開発を進めています。

## 8. 今後の目標

最終的には、商品検索、会員管理、注文処理、在庫管理などを備えたECサイト管理システムとして完成させる予定です。

また、実装内容を日本語で説明できるよう、README、設計メモ、API仕様書もあわせて整備します。
