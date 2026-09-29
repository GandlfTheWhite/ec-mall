"""EB配信パッケージにプロキシ設定が含まれることを検証する。"""

from pathlib import Path
from tempfile import TemporaryDirectory
import unittest
from zipfile import ZipFile
from package_backend import package


class PackagingTest(unittest.TestCase):
    def test_only_required_files_are_packaged_at_the_root(self):
        with TemporaryDirectory(prefix="ec-mall-package-test-") as folder:
            root = Path(folder)
            files = {
                "target/ec-mall-1.0-SNAPSHOT.jar": b"test-jar",
                "Procfile": b"web: java -jar application.jar\n",
                ".platform/nginx/conf.d/20_upload_size.conf": b"client_max_body_size 6m;\n",
                "unrelated-secret.txt": b"not-in-bundle",
            }
            for name, content in files.items():
                target = root / name
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(content)
            with ZipFile(package(root)) as bundle:
                self.assertEqual(set(bundle.namelist()), {
                    "application.jar", "Procfile", ".platform/nginx/conf.d/20_upload_size.conf",
                })
                self.assertEqual(bundle.read("application.jar"), b"test-jar")

    def test_missing_files_fail_before_creating_bundle(self):
        with TemporaryDirectory(prefix="ec-mall-package-test-") as folder:
            with self.assertRaises(FileNotFoundError):
                package(Path(folder))
            self.assertFalse((Path(folder) / "target/ec-mall-eb.zip").exists())
