#!/usr/bin/env python3
"""
Áp dụng gói hình vẽ lại từ Procreate (file .zip / thư mục PNG) hoặc spendy-elements-patch.json vào web/icons.js.
Hỗ trợ cả 173 Icons lẫn 15 UI Elements (ui-dock-*, ui-arcade-*, ui-sky-cloud).

Cách dùng:
  python3 tools/icons32/apply_patch.py path/to/Procreate-Drawings.zip --mode hd
  python3 tools/icons32/apply_patch.py path/to/folder_of_pngs/ --mode hd
  python3 tools/icons32/apply_patch.py path/to/folder_of_pngs/ --mode pixel
  python3 tools/icons32/apply_patch.py path/to/spendy-elements-patch.json

Trong đó:
  --mode hd    : Giữ nét vẽ tay Procreate sắc nét (thu gọn về 128x128 PNG nén tối ưu, giữ anti-aliasing mượt)
  --mode pixel : Chuyển về pixel art 32x32 Nearest-Neighbor
"""
import argparse
import base64
import io
import json
import re
import sys
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ICONS_JS = ROOT / "web" / "icons.js"


def encode_image(img: Image.Image, mode: str, size: int) -> str:
    img = img.convert("RGBA")
    if mode == "pixel":
        target = size or 32
        img = img.resize((target, target), Image.NEAREST)
    else:
        target = size or 128
        if img.width > target or img.height > target:
            img = img.resize((target, target), Image.LANCZOS)
    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True)
    return base64.b64encode(buf.getvalue()).decode("ascii")


def load_patch(source: Path, mode: str, size: int) -> dict[str, str]:
    updates: dict[str, str] = {}
    if source.is_dir():
        for p in sorted(source.rglob("*")):
            if p.is_file() and p.suffix.lower() in {".png", ".webp", ".jpg", ".jpeg"} and not p.name.startswith("."):
                key = p.stem.strip().lower()
                if key.startswith("00_"):
                    continue
                updates[key] = encode_image(Image.open(p), mode, size)
    elif source.suffix.lower() == ".zip":
        with zipfile.ZipFile(source, "r") as zf:
            for info in zf.infolist():
                if info.is_dir():
                    continue
                name = Path(info.filename).name
                if name.startswith(".") or name.startswith("00_") or "__MACOSX" in info.filename:
                    continue
                if Path(name).suffix.lower() in {".png", ".webp", ".jpg", ".jpeg"}:
                    key = Path(name).stem.strip().lower()
                    im = Image.open(io.BytesIO(zf.read(info.filename)))
                    updates[key] = encode_image(im, mode, size)
    elif source.suffix.lower() == ".json":
        data = json.loads(source.read_text(encoding="utf-8"))
        icons = data.get("icons", data)
        for k, v in icons.items():
            if isinstance(v, str) and v.strip():
                updates[k.strip().lower()] = re.sub(r"^data:image/\w+;base64,", "", v.strip())
    elif source.suffix.lower() in {".png", ".webp", ".jpg", ".jpeg"}:
        key = source.stem.strip().lower()
        updates[key] = encode_image(Image.open(source), mode, size)
    else:
        raise ValueError(f"Không hỗ trợ định dạng: {source}")
    return updates


def main():
    ap = argparse.ArgumentParser(description="Nạp hình vẽ Procreate / Patch vào web/icons.js")
    ap.add_argument("source", help="Đường dẫn file .zip, .json, .png hoặc thư mục chứa ảnh PNG")
    ap.add_argument("--mode", choices=["hd", "pixel"], default="hd", help="hd (128x128 mượt cho Procreate) hoặc pixel (32x32)")
    ap.add_argument("--size", type=int, default=0, help="Kích thước tuỳ chọn (mặc định 128 cho hd, 32 cho pixel)")
    args = ap.parse_args()

    src = Path(args.source)
    if not src.exists():
        print(f"Không tìm thấy: {src}")
        sys.exit(1)

    updates = load_patch(src, args.mode, args.size)
    if not updates:
        print("Không tìm thấy element hợp lệ nào trong gói đầu vào.")
        sys.exit(1)

    text = ICONS_JS.read_text(encoding="utf-8")
    m = re.search(r"(var IMG32\s*=\s*\{)([\s\S]*?)(\};)", text)
    if not m:
        print("Không tìm thấy bảng IMG32 trong web/icons.js")
        sys.exit(1)

    entries = dict(re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(2)))
    applied = []
    for k, b64 in sorted(updates.items()):
        entries[k] = b64
        applied.append(k)

    lines = [f'    "{k}": "{v}"' for k, v in sorted(entries.items())]
    new_block = m.group(1) + "\n" + ",\n".join(lines) + "\n  " + m.group(3)
    new_text = text[:m.start()] + new_block + text[m.end():]
    ICONS_JS.write_text(new_text, encoding="utf-8")
    print(f"Đã cập nhật {len(applied)} elements ({args.mode}) vào web/icons.js: {', '.join(applied[:12])}{'...' if len(applied) > 12 else ''}")


if __name__ == "__main__":
    main()
