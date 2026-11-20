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
INK_HEX = "#22172B"
PAL = {
    "o": "#3B2A40", "h": "#FFFFFF",
    "r": "#FF5765", "P": "#EC5F93", "p": "#FFA3C2", "O": "#FF9638", "y": "#FFDA47", "Y": "#F5AA27",
    "g": "#52CF68", "G": "#2FA355", "M": "#82E8BE", "t": "#2AC4B3", "b": "#63C7FF", "B": "#4582EC",
    "L": "#9665E6", "l": "#C9B0F9", "c": "#F2BF82", "C": "#B5703D", "D": "#704430", "w": "#FFF4E0",
    "W": "#F6F4FA", "s": "#CBD1E0", "S": "#8C96AD", "n": "#4D4666", "k": "#FFD1AD", "u": "#2D3240",
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


def _path_bbox_size(d):
    tokens = re.findall(r'[A-Za-z]|[-+]?(?:\d+\.?\d*|\.\d+)', d)
    cx = cy = sx = sy = 0.0
    xs, ys = [], []
    cmd = ""
    i = 0
    n = len(tokens)
    def rec(x, y):
        xs.append(x); ys.append(y)
    while i < n:
        if tokens[i].isalpha():
            cmd = tokens[i]; i += 1
            if cmd in ("Z", "z"):
                cx, cy = sx, sy
            continue
        try:
            if cmd in ("M", "L", "T"):
                cx, cy = float(tokens[i]), float(tokens[i + 1]); i += 2; rec(cx, cy)
                if cmd == "M": sx, sy = cx, cy; cmd = "L"
            elif cmd in ("m", "l", "t"):
                cx += float(tokens[i]); cy += float(tokens[i + 1]); i += 2; rec(cx, cy)
                if cmd == "m": sx, sy = cx, cy; cmd = "l"
            elif cmd == "H":
                cx = float(tokens[i]); i += 1; rec(cx, cy)
            elif cmd == "h":
                cx += float(tokens[i]); i += 1; rec(cx, cy)
            elif cmd == "V":
                cy = float(tokens[i]); i += 1; rec(cx, cy)
            elif cmd == "v":
                cy += float(tokens[i]); i += 1; rec(cx, cy)
            elif cmd == "C":
                rec(float(tokens[i]), float(tokens[i + 1]))
                rec(float(tokens[i + 2]), float(tokens[i + 3]))
                cx, cy = float(tokens[i + 4]), float(tokens[i + 5]); i += 6; rec(cx, cy)
            elif cmd == "c":
                rec(cx + float(tokens[i]), cy + float(tokens[i + 1]))
                rec(cx + float(tokens[i + 2]), cy + float(tokens[i + 3]))
                cx += float(tokens[i + 4]); cy += float(tokens[i + 5]); i += 6; rec(cx, cy)
            elif cmd in ("S", "Q"):
                rec(float(tokens[i]), float(tokens[i + 1]))
                cx, cy = float(tokens[i + 2]), float(tokens[i + 3]); i += 4; rec(cx, cy)
            elif cmd in ("s", "q"):
                rec(cx + float(tokens[i]), cy + float(tokens[i + 1]))
                cx += float(tokens[i + 2]); cy += float(tokens[i + 3]); i += 4; rec(cx, cy)
            elif cmd == "A":
                rx, ry = float(tokens[i]), float(tokens[i + 1])
                nx, ny = float(tokens[i + 5]), float(tokens[i + 6]); i += 7
                rec(cx - rx, cy - ry); rec(nx + rx, ny + ry); cx, cy = nx, ny; rec(cx, cy)
            elif cmd == "a":
                rx, ry = float(tokens[i]), float(tokens[i + 1])
                nx, ny = cx + float(tokens[i + 5]), cy + float(tokens[i + 6]); i += 7
                rec(cx - rx, cy - ry); rec(nx + rx, ny + ry); cx, cy = nx, ny; rec(cx, cy)
            else:
                i += 1
        except (IndexError, ValueError):
            break
    if not xs:
        return 0.0, 0.0
    return max(xs) - min(xs), max(ys) - min(ys)


def _add_unblast_outlines(body):
    """Thêm viền mực mảnh (paint-order: stroke fill) quanh các mảng hình lớn kiểu Unblast Colored Icons."""
    def repl_tag(m):
        tag, attrs, close = m.group(1), m.group(2), m.group(3)
        if 'stroke=' in attrs:
            return m.group(0)
        fm = re.search(r'\bfill="([^"]+)"', attrs)
        if not fm or fm.group(1) in ("none", "o", "h", "u", "n"):
            return m.group(0)
        should_outline = False
        if tag == "circle":
            rm = re.search(r'\br="([0-9.]+)"', attrs)
            should_outline = bool(rm and float(rm.group(1)) >= 3.0)
        elif tag == "ellipse":
            rxm = re.search(r'\brx="([0-9.]+)"', attrs)
            rym = re.search(r'\bry="([0-9.]+)"', attrs)
            should_outline = bool(rxm and rym and float(rxm.group(1)) >= 3.0 and float(rym.group(1)) >= 2.2)
        elif tag == "rect":
            wm = re.search(r'\bwidth="([0-9.]+)"', attrs)
            hm = re.search(r'\bheight="([0-9.]+)"', attrs)
            if wm and hm:
                w, h = float(wm.group(1)), float(hm.group(1))
                should_outline = (w >= 5.0 and h >= 4.0) or (w * h >= 28 and min(w, h) >= 2.5)
        elif tag == "path":
            dm = re.search(r'\bd="([^"]+)"', attrs)
            if dm:
                w, h = _path_bbox_size(dm.group(1))
                should_outline = (w * h >= 44 and min(w, h) >= 3.6)
        if should_outline:
            attrs += ' stroke="o" stroke-width="1.5" paint-order="stroke fill"'
        return f"<{tag}{attrs}{close}>"
    return re.sub(r'<(rect|circle|ellipse|path)\b([^>]*?)(/?)>', repl_tag, body)


def colorize(body):
    body = _add_unblast_outlines(body)
    def sub(m):
        attr, val = m.group(1), m.group(2)
        return '%s="%s"' % (attr, PAL.get(val, val))
    return re.sub(r'\b(fill|stroke)="([A-Za-z])"', sub, body)


def svg_doc(body, size, crisp):
    sr = ' shape-rendering="crispEdges"' if crisp else ""
    return ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="%d" height="%d"%s '
            'stroke-linecap="round" stroke-linejoin="round">%s</svg>') % (size, size, sr, colorize(body))


def render_svgs(names, size, crisp):
    """Dựng các SVG bằng Google Chrome Headless -> {tên: ảnh RGBA}"""
    cols = 12
    cells = "".join('<div class="c">%s</div>' % svg_doc(V[n], size, crisp) for n in names)
    html = ('<html><head><style>html,body{margin:0;padding:0;background:transparent;overflow:hidden}.g{display:grid;grid-template-columns:repeat(%d,%dpx);}'
            '.c{width:%dpx;height:%dpx}svg{display:block}</style></head><body><div class="g">%s</div></body></html>') % (cols, size, size, size, cells)
    tmp = os.path.join(HERE, ".render")
    os.makedirs(tmp, exist_ok=True)
    hp, op = os.path.join(tmp, "page.html"), os.path.join(tmp, "page.png")
    open(hp, "w", encoding="utf8").write(html)
    rows = (len(names) + cols - 1) // cols
    w, h = cols * size, max(1, rows) * size
    subprocess.check_call(
        [
            "google-chrome", "--headless=new", "--no-sandbox", "--disable-gpu",
            "--hide-scrollbars", "--default-background-color=00000000",
            "--force-device-scale-factor=1", f"--window-size={w},{h}",
            f"--screenshot={op}", f"file://{hp}",
        ],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
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
            best = max(cnt, key=lambda k: cnt[k] * (1.85 if k in LINE else 1.0))
            g[cy][cx] = best
    # dọn điểm lẻ: ô mà cả 4 hàng xóm cùng một màu khác -> theo hàng xóm
    for y in range(N):
        for x in range(N):
            nb = [g[y + dy][x + dx] if 0 <= x + dx < N and 0 <= y + dy < N else "." for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
            if len(set(nb)) == 1 and nb[0] != g[y][x] and g[y][x] not in LINE:
                g[y][x] = nb[0]
    return g


def _unblast_tones(hex_col):
    """Trả về (shadow_hex, base_hex, highlight_hex) phẳng, tươi sáng kiểu Unblast Colored Icons."""
    r, g, b = F._hex(hex_col)
    import colorsys
    h, s, v = colorsys.rgb_to_hsv(r, g, b)
    # Base: tươi sáng
    s_base = min(1.0, s * 1.12) if s > 0.12 else s
    base = F._to_hex(colorsys.hsv_to_rgb(h, s_base, v))
    # Shadow: đậm hơn ~20%, ngả nhẹ về tím mận để tách khối phẳng rõ nét
    h_sh = F._shift(h, 0.76, 0.08)
    s_sh = min(1.0, s_base + 0.10) if s > 0.08 else s_base + 0.05
    v_sh = max(0.18, v * 0.78)
    shade = F._to_hex(colorsys.hsv_to_rgb(h_sh, s_sh, v_sh))
    # Highlight: sáng hơn ở mép trên-trái
    h_hi = F._shift(h, 0.14, 0.05)
    s_hi = max(0.0, s_base * 0.72)
    v_hi = min(1.0, v + (1.0 - v) * 0.55)
    hi = F._to_hex(colorsys.hsv_to_rgb(h_hi, s_hi, v_hi))
    return shade, base, hi


def _shade_flat_unblast(region, ch, col):
    """Tô màu phẳng 2 sắc độ (+ viền sáng nhẹ trên-trái cho mảng lớn) kiểu Unblast Colored Icons."""
    shade, base, hi = _unblast_tones(PAL[ch])
    S = set(region)
    area = len(region)
    xs = [p[0] for p in region]
    ys = [p[1] for p in region]
    w = max(xs) - min(xs) + 1
    h = max(ys) - min(ys) + 1
    big = area >= 48 and min(w, h) >= 6
    med = area >= 14 and min(w, h) >= 3

    for (x, y) in region:
        col[y][x] = base

    if not med:
        # Mảng nhỏ: chỉ 2 sắc độ (mép dưới/phải đậm hơn)
        for (x, y) in region:
            if (x + 1, y) not in S or (x, y + 1) not in S:
                if (x - 1, y) in S or (x, y - 1) in S:
                    col[y][x] = shade
        return

    # Mảng vừa & lớn: dải bóng phẳng 1-2px ở đáy/phải + vệt sáng 1px ở đỉnh/trái
    for (x, y) in region:
        is_bot = (x, y + 1) not in S
        is_rgt = (x + 1, y) not in S
        is_top = (x, y - 1) not in S
        is_lft = (x - 1, y) not in S
        if is_bot or is_rgt:
            col[y][x] = shade
        elif big and ((x, y + 2) not in S and (x + 1, y + 1) not in S or (x + 2, y) not in S and (x + 1, y + 1) not in S):
            col[y][x] = shade
        elif (is_top and not is_rgt) or (is_lft and not is_bot and y <= min(ys) + h * 0.55):
            col[y][x] = hi


def to_pixel(g):
    """lưới ký hiệu -> màu cuối: tô màu phẳng 2 sắc độ kiểu Unblast + viền mực đậm rõ nét"""
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
            if c == "o":
                for x, y in region:
                    col[y][x] = INK_HEX
            elif c in ("u", "n"):
                shade, base, _ = _unblast_tones(PAL[c])
                for x, y in region:
                    col[y][x] = base
            elif c == "h":
                for x, y in region:
                    col[y][x] = "#FFFFFF"
            else:
                _shade_flat_unblast(region, c, col)
    # viền ngoài 1px: viền mực đậm đồng nhất kiểu Unblast
    out = [r[:] for r in col]
    for y in range(N):
        for x in range(N):
            if col[y][x] is not None:
                continue
            nb = [at(x + dx, y + dy) for dx, dy in F.N4 if at(x + dx, y + dy) != "."]
            if nb:
                out[y][x] = INK_HEX
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
