#!/usr/bin/env python3
"""Dựng bộ icon 32×32 của Spendy.

  python3 tools/icons32/build.py --sheet out.png [tên|tiền tố…]   # bảng xem trước
  python3 tools/icons32/build.py --inject                          # nhúng PNG vào web/icons.js
"""
import base64
import importlib
import io
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from PIL import Image, ImageDraw  # noqa: E402
from reg import REG  # noqa: E402

for mod in sorted(f[:-3] for f in os.listdir(HERE) if f.startswith("defs_") and f.endswith(".py")):
    importlib.import_module(mod)

ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))


def render_all(names=None):
    out = {}
    for n, f in REG.items():
        if names and not any(n == k or n.startswith(k.rstrip("*")) and k.endswith("*") for k in names):
            continue
        out[n] = f().render()
    return out


def png_b64(img):
    q = img.convert("RGBA")
    buf = io.BytesIO()
    q.save(buf, format="PNG", optimize=True)
    return base64.b64encode(buf.getvalue()).decode("ascii")


def sheet(imgs, path, cols=8, scale=3, bg=(251, 235, 216, 255)):
    cell = 32 * scale + 40
    rows = (len(imgs) + cols - 1) // cols
    W, H = cols * cell + 10, rows * (32 * scale + 34) + 10
    sh = Image.new("RGBA", (W, H), bg)
    d = ImageDraw.Draw(sh)
    for k, (n, im) in enumerate(imgs.items()):
        x = 10 + (k % cols) * cell
        y = 10 + (k // cols) * (32 * scale + 34)
        sh.alpha_composite(im.resize((32 * scale, 32 * scale), Image.NEAREST), (x, y))
        sh.alpha_composite(im, (x + 32 * scale + 4, y + 32 * scale - 32))
        d.text((x, y + 32 * scale + 4), n, fill=(74, 51, 80, 255))
    sh.save(path)


def inject(imgs):
    p = os.path.join(ROOT, "web", "icons.js")
    s = open(p, encoding="utf8").read()
    body = "  var IMG32 = {\n" + ",\n".join('    "%s": "%s"' % (n, png_b64(im)) for n, im in sorted(imgs.items())) + "\n  };\n"
    a, b = "  /* ICON32-START */\n", "  /* ICON32-END */\n"
    if a in s:
        s = s[:s.index(a) + len(a)] + body + s[s.index(b):]
    else:
        raise SystemExit("Thiếu mốc ICON32-START trong web/icons.js")
    open(p, "w", encoding="utf8").write(s)
    print("Đã nhúng %d icon 32×32 (%.0f KB)" % (len(imgs), len(body) / 1024))


if __name__ == "__main__":
    args = sys.argv[1:]
    if args and args[0] == "--inject":
        inject(render_all())
    elif args and args[0] == "--sheet":
        imgs = render_all(args[2:] or None)
        sheet(imgs, args[1])
        print(len(imgs), "icon")
    else:
        print(__doc__)
