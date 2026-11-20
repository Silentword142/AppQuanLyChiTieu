#!/usr/bin/env python3
"""Bộ icon Spendy: vẽ VECTOR 2D trước → chuyển pixel 32×32 → tô khối.

  python3 tools/icons32/vector.py --sheet /tmp/out [tên…]   # out_vector.png (bản vector) + out_pixel.png
  python3 tools/icons32/vector.py --inject                   # nhúng PNG vào web/icons.js (IMG32)

Hệ lưới chung (viewBox 0 0 32 32, đơn vị = 1 điểm ảnh cuối):
  * vùng vẽ 3..29 (viền 1px tự thêm ra ngoài) · tròn Ø25 · vuông 24 (bo 3) · ngang 26×20 · đứng 20×26
  * nét chi tiết dày 1 (đặt ở toạ độ .5 cho sắc) hoặc 2 · góc nhìn chính diện · ánh sáng trên-trái
  * màu tô phẳng bằng ký hiệu bảng màu (fill="r"…); bóng/sáng do bước tô khối tự làm cho đồng đều
Chuyển pixel: dựng SVG ở 256×256 (×8, không khử răng cưa) → mỗi ô 8×8 lấy màu chiếm đa số
(nét chi tiết được ưu tiên) → dọn điểm lẻ → viền ngoài 1px → tô khối từng mảng (from16.shade).
"""
import base64
import io
import json
import os
import re
import subprocess
import sys

from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, HERE)
import from16 as F  # noqa: E402  (dùng chung dải màu, tô khối, viền)

N, SS = 32, 8
# Bảng màu chung (ký hiệu -> màu gốc). "o" = mực cho nét chi tiết.
PAL = {
    "o": "#3B2A40", "h": "#FFFFFF",
    "r": "#F0545E", "P": "#E25F8E", "p": "#FF9EBB", "O": "#FF9443", "y": "#FFD34E", "Y": "#F4AE2F",
    "g": "#5DC86B", "G": "#3A9F5B", "M": "#8FE3BE", "t": "#33BFAE", "b": "#6CC5F4", "B": "#4A83E6",
    "L": "#9466E0", "l": "#C6AEF5", "c": "#EDBB80", "C": "#AF6E3F", "D": "#6D4431", "w": "#FFF3DE",
    "W": "#F4F1F7", "s": "#C7CCD9", "S": "#8B94AA", "n": "#4C4762", "k": "#FFCFAA", "u": "#2F3440",
}
LINE = {"o", "h", "n", "u", "D"}           # màu nét chi tiết được ưu tiên khi lấy đa số
HEX2CH = {v.lower(): k for k, v in PAL.items()}

V = {}


def icon(name, body):
    V[name] = body.strip()


def load_defs():
    for f in sorted(os.listdir(HERE)):
        if f.startswith("vec_") and f.endswith(".py"):
            code = open(os.path.join(HERE, f), encoding="utf8").read()
            exec(compile(code, f, "exec"), {"icon": icon, "__name__": f[:-3]})


def colorize(body):
    def sub(m):
        attr, val = m.group(1), m.group(2)
        return '%s="%s"' % (attr, PAL.get(val, val))
    return re.sub(r'\b(fill|stroke)="([A-Za-z])"', sub, body)


def svg_doc(body, size, crisp):
    sr = ' shape-rendering="crispEdges"' if crisp else ""
    return ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="%d" height="%d"%s '
            'stroke-linecap="round" stroke-linejoin="round">%s</svg>') % (size, size, sr, colorize(body))


def render_svgs(names, size, crisp):
    """Dựng các SVG bằng Chromium (Playwright) -> {tên: ảnh RGBA}"""
    cols = 12
    cells = "".join('<div class="c">%s</div>' % svg_doc(V[n], size, crisp) for n in names)
    html = ('<html><head><style>body{margin:0;background:transparent}.g{display:grid;grid-template-columns:repeat(%d,%dpx);}'
            '.c{width:%dpx;height:%dpx}svg{display:block}</style></head><body><div class="g">%s</div></body></html>') % (cols, size, size, size, cells)
    tmp = os.path.join(HERE, ".render")
    os.makedirs(tmp, exist_ok=True)
    hp, op = os.path.join(tmp, "page.html"), os.path.join(tmp, "page.png")
    open(hp, "w", encoding="utf8").write(html)
    rows = (len(names) + cols - 1) // cols
    js = r"""
const { chromium } = require('playwright');
(async () => {
  const [hp, op, w, h] = process.argv.slice(1);
  const b = await chromium.launch(); const p = await b.newPage({ viewport: { width: +w, height: +h }, deviceScaleFactor: 1 });
  await p.goto('file://' + hp); await p.waitForTimeout(150);
  await p.screenshot({ path: op, omitBackground: true, fullPage: true }); await b.close();
})();
"""
    subprocess.check_call(["node", "-e", js, hp, op, str(cols * size), str(max(1, rows) * size)])
    sheet = Image.open(op).convert("RGBA")
    out = {}
    for k, n in enumerate(names):
        x, y = (k % cols) * size, (k // cols) * size
        out[n] = sheet.crop((x, y, x + size, y + size))
    return out


def downsample(img):
    """ảnh 256×256 -> lưới ký hiệu 32×32 theo đa số (nét chi tiết được ưu tiên)"""
    px = img.load()
    g = [["."] * N for _ in range(N)]
    for cy in range(N):
        for cx in range(N):
            cnt = {}
            for yy in range(cy * SS, cy * SS + SS):
                for xx in range(cx * SS, cx * SS + SS):
                    r, gg, b, a = px[xx, yy]
                    if a < 128:
                        ch = "."
                    else:
                        ch = HEX2CH.get("#%02x%02x%02x" % (r, gg, b))
                        if ch is None:   # màu lạ (hiếm) -> gần nhất
                            ch = min(PAL, key=lambda k: sum((int(PAL[k][i:i + 2], 16) - v) ** 2 for i, v in zip((1, 3, 5), (r, gg, b))))
                    cnt[ch] = cnt.get(ch, 0) + 1
            tr = cnt.pop(".", 0)
            if not cnt or tr > SS * SS * 0.55:
                continue
            best = max(cnt, key=lambda k: cnt[k] * (1.7 if k in LINE else 1.0))
            g[cy][cx] = best
    # dọn điểm lẻ: ô mà cả 4 hàng xóm cùng một màu khác -> theo hàng xóm
    for y in range(N):
        for x in range(N):
            nb = [g[y + dy][x + dx] if 0 <= x + dx < N and 0 <= y + dy < N else "." for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
            if len(set(nb)) == 1 and nb[0] != g[y][x] and g[y][x] not in LINE:
                g[y][x] = nb[0]
    return g


def to_pixel(g):
    """lưới ký hiệu -> màu cuối: tô khối từng mảng + viền ngoài 1px"""
    col = [[None] * N for _ in range(N)]
    at = lambda x, y: g[y][x] if 0 <= x < N and 0 <= y < N else "."
    seen = [[False] * N for _ in range(N)]
    for sy in range(N):
        for sx in range(N):
            c = g[sy][sx]
            if seen[sy][sx] or c in (".",):
                continue
            region, st = [], [(sx, sy)]
            seen[sy][sx] = True
            while st:
                x, y = st.pop(); region.append((x, y))
                for dx, dy in F.N4:
                    xx, yy = x + dx, y + dy
                    if 0 <= xx < N and 0 <= yy < N and not seen[yy][xx] and g[yy][xx] == c:
                        seen[yy][xx] = True; st.append((xx, yy))
            if c in ("o", "u", "n"):          # nét mực: phẳng
                for x, y in region:
                    col[y][x] = F.ramp(PAL[c])[3] if c != "o" else PAL["o"]
            elif c == "h":
                for x, y in region:
                    col[y][x] = "#FFFFFF"
            else:
                F.shade_region(region, c, PAL, g, col, set(), pairs=False)
    # viền ngoài 1px: màu tối nhất của mảng kề bên pha mực
    out = [r[:] for r in col]
    for y in range(N):
        for x in range(N):
            if col[y][x] is not None:
                continue
            nb = [at(x + dx, y + dy) for dx, dy in F.N4 if at(x + dx, y + dy) != "."]
            if nb:
                c = max(set(nb), key=nb.count)
                base = PAL["o"] if c in ("o", "u", "n", "h") else PAL[c]
                r0 = F.ramp(base)[0]
                out[y][x] = F._to_hex(tuple(a * 0.5 + b * 0.5 for a, b in zip(F._hex(r0), F._hex(PAL["o"]))))
    return out


def build(names=None):
    load_defs()
    names = [n for n in V if not names or n in names]
    hi = render_all_hi(names)
    return {n: F.to_image(to_pixel(downsample(hi[n]))) for n in names}


def render_all_hi(names):
    return render_svgs(names, N * SS, True)


def sheet_vector(names, path, size=96, cols=12):
    imgs = render_svgs(names, size, False)
    _grid(imgs, path, size, cols, scale=1)


def _grid(imgs, path, size, cols, scale):
    cell = size * scale + 10
    rows = (len(imgs) + cols - 1) // cols
    sh = Image.new("RGBA", (cols * cell + 10, rows * (cell + 12) + 10), (251, 235, 216, 255))
    d = ImageDraw.Draw(sh)
    for k, (n, im) in enumerate(imgs.items()):
        x, y = 10 + (k % cols) * cell, 10 + (k // cols) * (cell + 12)
        sh.alpha_composite(im.resize((size * scale, size * scale), Image.NEAREST) if scale != 1 else im, (x, y))
        d.text((x, y + size * scale + 1), n[:14], fill=(74, 51, 80, 255))
    sh.save(path)


def inject(imgs):
    F.inject(imgs)


if __name__ == "__main__":
    a = sys.argv[1:]
    if a[:1] == ["--inject"]:
        inject(build())
    elif a[:1] == ["--sheet"]:
        load_defs()
        names = [n for n in V if not a[2:] or n in a[2:]]
        sheet_vector(names, a[1] + "_vector.png")
        imgs = {n: im for n, im in build(names).items()}
        _grid(imgs, a[1] + "_pixel.png", 32, 12, 3)
        print(len(names), "icon")
    else:
        print(__doc__)
