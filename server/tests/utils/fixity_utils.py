import hashlib


def md5_checksum(file_path: str, chunk_size: int = 1024 * 1024) -> str:
    md5 = hashlib.md5()

    with open(file_path, "rb") as f:
        for chunk in iter(lambda: f.read(chunk_size), b""):
            md5.update(chunk)

    return md5.hexdigest()
