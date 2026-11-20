#!/usr/bin/env python3
"""Áp dụng gói chỉnh sửa icon/element (spendy-elements-patch.json hoặc thư mục PNG) vào web/icons.js.

Cách dùng:
  python3 tools/icons32/apply_patch.py spendy-elements-patch.json
  python3 tools/icons32/apply_patch.py /đường/dẫn/thư_mục_png/
"""
import base64
import io
import json
import os
import re
import sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
ICONS_JS = os.path.join(ROOT, "web", "icons.js")


def load_existing_img32():
    src = open(ICONS_JS, encoding="utf8").read()
    m = re.search(r"/\* ICON32-START \*/\s*var IMG32 = (\{.*?\});\s*/\* ICON32-END \*/", src, re.S)
    if not m:
        raise RuntimeError("Không tìm thấy khối IMG32 trong web/icons.js")
    return src, json.loads(m.group(1))


def normalize_png32(b64_or_bytes):
    if isinstance(b64_or_bytes, str):
        b64_or_bytes = re.sub(r"^data:image/\w+;base64,", "", b64_or_bytes.strip())
        raw = base64.b64decode(b64_or_bytes)
    else:
        raw = b64_or_bytes
    im = Image.open(io.BytesIO(raw)).convert("RGBA")
    if im.size != (32, 32):
        im = im.resize((32, 32), Image.NEAREST)
    buf = io.BytesIO()
    im.save(buf, format="PNG", optimize=True)
    return base64.b64encode(buf.getvalue()).decode("ascii")


def apply_patch(target_path):
    src, img32 = load_existing_img32()
    updated = []

    if os.path.isdir(target_path):
        for fn in sorted(os.listdir(target_path)):
            if fn.lower().endswith((".png", ".webp", ".gif", ".jpg", ".jpeg")):
                key = os.path.splitext(fn)[0]
                raw = open(os.path.join(target_path, fn), "rb").read()
                img32[key] = normalize_png32(raw)
                updated.append(key)
    else:
        data = json.loads(open(target_path, encoding="utf8").read())
        icons_map = data.get("icons", data)
        for key, val in icons_map.items():
            if isinstance(val, str) and len(val) > 20:
                img32[key] = normalize_png32(val)
                updated.append(key)

    body = "/* ICON32-START */\n  var IMG32 = {\n" + ",\n".join(
        f'    {json.dumps(k)}: {json.dumps(v)}' for k, v in sorted(img32.items())
    ) + "\n  };\n  /* ICON32-END */"
    new_src = re.sub(r"/\* ICON32-START \*/.*?/\* ICON32-END \*/", body, src, flags=re.S)
    open(ICONS_JS, "w", encoding="utf8").write(new_src)
    print(f"Đã cập nhật {len(updated)} icon vào web/icons.js:", ", ".join(updated))


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    apply_patch(sys.argv[1])
