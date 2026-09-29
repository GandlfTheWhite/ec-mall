import base64
import unittest
from check_eb_environment import validate, ENV_NAMESPACE, SECRET_NAMESPACE


class EnvironmentCheckTest(unittest.TestCase):
    def options(self, **overrides):
        values = {
            "RDS_HOSTNAME": "database.example.invalid",
            "RDS_DB_NAME": "ec_mall",
            "RDS_USERNAME": "test-user",
            "RDS_PASSWORD": "test-password-not-a-real-secret",
            "JWT_SECRET": base64.b64encode(b"x" * 32).decode(),
        }
        values.update(overrides)
        return [{"Namespace": ENV_NAMESPACE, "OptionName": key, "Value": value}
                for key, value in values.items()]

    def test_rds_configuration_is_accepted(self):
        self.assertEqual([], validate(self.options()))

    def test_missing_credentials_are_rejected_without_echoing_values(self):
        errors = validate(self.options(RDS_PASSWORD="", JWT_SECRET=""))
        self.assertTrue(any("RDS_PASSWORD" in error for error in errors))
        self.assertTrue(any("JWT_SECRET" in error for error in errors))
        self.assertNotIn("test-user", str(errors))

    def test_loopback_and_malformed_hosts_are_rejected(self):
        for host in ("localhost", "localhost.", "127.0.0.1", "::1", "[::1]",
                     "0.0.0.0", "foo.localhost", "https://db.example.invalid"):
            with self.subTest(host=host):
                self.assertTrue(validate(self.options(RDS_HOSTNAME=host)))

    def test_local_profile_and_database_overrides_are_rejected(self):
        self.assertTrue(validate(self.options(SPRING_PROFILES_ACTIVE="local")))
        self.assertTrue(validate(self.options(SPRING_DATASOURCE_URL="jdbc:mysql://localhost/ec_mall")))

    def test_invalid_port_schema_and_signing_key_are_rejected(self):
        for override in ({"RDS_PORT": "70000"}, {"RDS_DB_NAME": "other"},
                         {"JWT_SECRET": "short"}, {"JWT_SECRET": base64.b64encode(b"short").decode()}):
            with self.subTest(override=list(override)):
                self.assertTrue(validate(self.options(**override)))

    def test_secret_references_are_checked_without_reading_secret_values(self):
        options = [item for item in self.options() if item["OptionName"] not in ("RDS_PASSWORD", "JWT_SECRET")]
        options += [{"Namespace": SECRET_NAMESPACE, "OptionName": name, "Value": "arn:aws:secretsmanager:example"}
                    for name in ("RDS_PASSWORD", "JWT_SECRET")]
        self.assertEqual([], validate(options))

    def test_unavailable_or_malformed_settings_are_rejected(self):
        self.assertTrue(validate(None))
        self.assertTrue(validate([]))
        self.assertTrue(validate([None]))

    def test_image_upload_configuration_requires_separate_bucket_and_https_origin(self):
        settings = {
            "PRODUCT_IMAGES_ENABLED": "true", "PRODUCT_IMAGES_BUCKET": "product-images-test",
            "PRODUCT_IMAGES_REGION": "us-east-1", "PRODUCT_IMAGES_BASE_URL": "https://images.example.com",
            "FRONTEND_S3_BUCKET": "frontend-test",
        }
        self.assertEqual([], validate(self.options(**settings)))
        for override in ({"PRODUCT_IMAGES_BUCKET": "frontend-test"}, {"PRODUCT_IMAGES_REGION": ""},
                         {"FRONTEND_S3_BUCKET": ""}, {"PRODUCT_IMAGES_BASE_URL": "http://images.example.com"},
                         {"PRODUCT_IMAGES_BASE_URL": "https://images.example.com?secret=test"},
                         {"PRODUCT_IMAGES_BASE_URL": "https://user:password@images.example.com"},
                         {"PRODUCT_IMAGES_BASE_URL": "https://images.example.com/path"}):
            with self.subTest(override=list(override)):
                self.assertTrue(validate(self.options(**(settings | override))))

    def test_image_upload_is_disabled_by_default_but_missing_enabled_settings_fail(self):
        self.assertEqual([], validate(self.options(PRODUCT_IMAGES_ENABLED="false")))
        self.assertTrue(validate(self.options(PRODUCT_IMAGES_ENABLED="true")))
        self.assertTrue(validate(self.options(PRODUCT_IMAGES_ENABLED="not-a-boolean")))


if __name__ == "__main__":
    unittest.main()
