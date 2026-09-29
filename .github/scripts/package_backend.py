"""検証済みJARと必要なEB設定のみを、ルート階層を保ってZIPに格納する。"""

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


def package(root=Path(".")):
    entries = {
        "application.jar": root / "target/ec-mall-1.0-SNAPSHOT.jar",
        "Procfile": root / "Procfile",
        ".platform/nginx/conf.d/20_upload_size.conf": root / ".platform/nginx/conf.d/20_upload_size.conf",
    }
    for source in entries.values():
        if not source.is_file():
            raise FileNotFoundError(source)
    output = root / "target/ec-mall-eb.zip"
    with ZipFile(output, "w", compression=ZIP_DEFLATED) as bundle:
        for name, source in entries.items():
            bundle.write(source, name)
    return output


if __name__ == "__main__":
    print(package())
