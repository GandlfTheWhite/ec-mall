"""EB 設定を標準入力で受け取り、値を表示せずに本番配信の可否を確認する。"""

import base64
import binascii
import ipaddress
import json
import re
import sys
from urllib.parse import urlsplit


ENV_NAMESPACE = "aws:elasticbeanstalk:application:environment"
SECRET_NAMESPACE = "aws:elasticbeanstalk:application:environmentsecrets"


def validate(options):
    if not isinstance(options, list):
        return ["EB の環境設定を取得できませんでした。配信を中止します。"]
    values = {}
    secret_names = set()
    for item in options:
        if not isinstance(item, dict):
            return ["EB の環境設定の形式が正しくありません。"]
        name, value = item.get("OptionName"), item.get("Value")
        if item.get("Namespace") == ENV_NAMESPACE:
            values[name] = value if isinstance(value, str) else ""
        elif item.get("Namespace") == SECRET_NAMESPACE and isinstance(value, str) and value.strip():
            secret_names.add(name)

    errors = []
    # 独自の設定上書きを見逃さない。使用中の場合は設定方式を先に確認する。
    if any(name in values or name in secret_names for name in (
        "SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD",
        "SPRING_APPLICATION_JSON", "SPRING_CONFIG_LOCATION",
        "SPRING_CONFIG_ADDITIONAL_LOCATION", "SPRING_CONFIG_IMPORT", "SPRING_CONFIG_NAME",
        "SPRING_PROFILES_DEFAULT", "SPRING_PROFILES_INCLUDE",
    )):
        errors.append("標準の RDS_* 設定以外の上書きがあります。設定を確認するまで配信できません。")
    if values.get("SPRING_PROFILES_ACTIVE", "prod").strip() != "prod":
        errors.append("SPRING_PROFILES_ACTIVE は prod にしてください。")
    if {"SPRING_PROFILES_ACTIVE", "RDS_PORT"} & secret_names:
        errors.append("プロファイルとポートは確認可能な通常の環境プロパティで設定してください。")

    for name in ("RDS_HOSTNAME", "RDS_DB_NAME", "RDS_USERNAME", "RDS_PASSWORD", "JWT_SECRET"):
        if name in ("RDS_PASSWORD", "JWT_SECRET") and name in secret_names:
            continue
        if not values.get(name, "").strip():
            errors.append(name + " が未設定です。")

    host = values.get("RDS_HOSTNAME", "").strip().lower().rstrip(".")
    if host:
        invalid = host == "localhost" or host.endswith(".localhost")
        try:
            address = ipaddress.ip_address(host.strip("[]"))
            invalid = invalid or address.is_loopback or address.is_unspecified
        except ValueError:
            # エンドポイントにはプロトコル・ポート・パス・認証情報を含めない。
            invalid = invalid or any(c in host for c in "/:@?# \\")
        if invalid:
            errors.append("RDS_HOSTNAME にローカルアドレスや URL は指定できません。")

    port = values.get("RDS_PORT", "3306")
    if not port.isdigit() or not 1 <= int(port) <= 65535:
        errors.append("RDS_PORT は有効なポート番号にしてください。")
    # 既存の Mapper は ec_mall スキーマを明示している。
    if values.get("RDS_DB_NAME") and values["RDS_DB_NAME"] != "ec_mall":
        errors.append("現在の Mapper に合わせて RDS_DB_NAME は ec_mall にしてください。")

    if "JWT_SECRET" not in secret_names and values.get("JWT_SECRET"):
        try:
            if len(base64.b64decode(values["JWT_SECRET"], validate=True)) < 32:
                errors.append("JWT_SECRET は32バイト以上の鍵を Base64 にした値が必要です。")
        except (ValueError, binascii.Error):
            errors.append("JWT_SECRET は正しい Base64 形式で設定してください。")
    # 画像機能を有効にした場合だけ、配信前に専用バケットと公開URLを確認する。
    enabled = values.get("PRODUCT_IMAGES_ENABLED", "false").strip().lower()
    image_names = ("PRODUCT_IMAGES_ENABLED", "PRODUCT_IMAGES_BUCKET", "PRODUCT_IMAGES_REGION",
                   "PRODUCT_IMAGES_BASE_URL", "FRONTEND_S3_BUCKET")
    if secret_names.intersection(image_names):
        errors.append("画像配信設定は秘密値ではなく通常の環境プロパティで設定してください。")
    if enabled not in ("true", "false"):
        errors.append("PRODUCT_IMAGES_ENABLED は true または false にしてください。")
    if enabled == "true":
        for name in image_names[1:]:
            if not values.get(name, "").strip():
                errors.append(name + " が未設定です。")
        bucket = values.get("PRODUCT_IMAGES_BUCKET", "")
        frontend = values.get("FRONTEND_S3_BUCKET", "")
        if not re.fullmatch(r"[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]", bucket) or not re.fullmatch(r"[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]", frontend):
            errors.append("画像用とフロントエンド用のバケット名を確認してください。")
        if bucket == frontend:
            errors.append("商品画像にはフロントエンドと異なるS3バケットが必要です。")
        try:
            base = values.get("PRODUCT_IMAGES_BASE_URL", "")
            url = urlsplit(base)
            if (url.scheme != "https" or not url.hostname or url.username is not None
                    or url.password is not None or url.port not in (None, 443)
                    or url.path not in ("", "/") or "?" in base or "#" in base
                    or len(base.rstrip("/")) + 51 > 255):
                raise ValueError()
        except ValueError:
            errors.append("PRODUCT_IMAGES_BASE_URL はパス・クエリのないHTTPSのURLにしてください。")
    return errors


def main():
    try:
        options = json.load(sys.stdin)
        errors = validate(options)
    except (ValueError, TypeError):
        errors = ["EB 設定の読み取りに失敗しました。配信を中止します。"]
    if errors:
        for message in errors:
            print("::error::" + message)
        return 1
    print("本番設定の事前確認に成功しました。設定値は表示していません。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
