#!/usr/bin/env python3
"""Nâng bộ icon 16×16 (web/icons.js) lên 32×32 có bóng đổ và màu sắc chi tiết.

  python3 tools/icons32/from16.py --sheet /tmp/xem.png [tên…]   # bảng xem trước
  python3 tools/icons32/from16.py --inject                      # nhúng PNG vào web/icons.js (IMG32)

Quy trình cho mỗi icon màu (icon nét đơn sắc "#" giữ nguyên SVG 16×16):
  1. Scale2x (EPX): phóng 2× mà nét chéo vẫn mượt
  2. Làm mảnh viền: viền 2px -> 1px; nửa trong của viền thành mép của mảng màu kề bên
  3. Tô khối từng mảng màu bằng dải 6 tông lệch sắc (tối ngả tím, sáng ngả vàng):
     ước lượng pháp tuyến từ khoảng cách tới mép theo 8 hướng, ánh sáng trên-trái,
     dither bàn cờ ở ranh giới dải tông, điểm bóng loáng ở mảng lớn
  4. Viền ngoài = tông tối nhất của mảng màu kề bên (selective outline)
"""
import base64
import colorsys
import io
import json
import math
import os
import subprocess
import sys

from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
N = 32
INK = "#3A2840"
SAT_BOOST = 0.32          # tăng độ rực của màu gốc (anh muốn nhiều màu sắc hơn)
LIGHT = (-0.62, -0.78)    # hướng tới nguồn sáng (trên-trái)
GLOSSY = set("prRPyYOagGtTbBlL")
# Bộ 16×16 tô bóng bằng cặp ký tự sáng/tối: gộp thành một khối, ký tự tối = tông thấp hơn 1 bậc
PAIR = {"P": "p", "R": "r", "Y": "y", "a": "O", "G": "g", "T": "t", "B": "b", "L": "l", "C": "c", "S": "s"}
grp = lambda ch: PAIR.get(ch, ch)


def load_grids():
    js = r"""
global.window = {}; require(process.argv[1]); const V = window.VinaIcons; const out = {};
for (const n of V.names) { const g = V.grid(n); if (!g || g.join("").includes("#")) continue; out[n] = g; }
out.__pal = V.PAL; process.stdout.write(JSON.stringify(out));
"""
    raw = subprocess.check_output(["node", "-e", js, os.path.join(ROOT, "web", "icons.js")])
    data = json.loads(raw)
    pal = data.pop("__pal")
    return data, pal


# ---------------- màu ----------------
def _hex(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) / 255 for i in (0, 2, 4))


def _to_hex(c):
    return "#" + "".join("%02x" % max(0, min(255, round(v * 255))) for v in c)


def _shift(h, target, amt):
    d = ((target - h + 0.5) % 1.0) - 0.5
    return (h + d * amt) % 1.0


_RAMPS = {}


def ramp(base):
    """0 viền · 1 tối sâu · 2 bóng · 3 gốc · 4 sáng · 5 bóng loáng (tính trên HSV, lệch sắc)"""
    if base in _RAMPS:
        return _RAMPS[base]
    h, s, v = colorsys.rgb_to_hsv(*_hex(base))
    s = min(1, s * (1 + SAT_BOOST)) if s > 0.15 else s
    spec = [(0.40, 0.26, 0.22), (0.62, 0.18, 0.14), (0.81, 0.09, 0.08), (1, 0, 0), (1.5, 0.06, -0.12), (1.8, 0.12, -0.42)]
    out = []
    for k, (vm, amt, ds) in enumerate(spec):
        if vm < 1:
            hh, vv, ss = _shift(h, 0.76, amt), v * vm, min(1, s + ds * (0.4 + s))
        elif vm > 1:
            hh, vv, ss = _shift(h, 0.14, amt), v + (1 - v) * (0.55 if vm < 1.7 else 0.85), max(0, s * (1 + ds))
        else:
            hh, vv, ss = h, v, s
        out.append(_to_hex(colorsys.hsv_to_rgb(hh, ss, vv)))
    _RAMPS[base] = out
    return out


# ---------------- 1. Scale2x ----------------
def scale2x(g):
    H, W = len(g), len(g[0])
    at = lambda x, y: g[y][x] if 0 <= x < W and 0 <= y < H else "."
    out = [["."] * (W * 2) for _ in range(H * 2)]
    for y in range(H):
        for x in range(W):
            P, A, B, C, D = g[y][x], at(x, y - 1), at(x + 1, y), at(x - 1, y), at(x, y + 1)
            e0 = A if (C == A and C != D and A != B) else P
            e1 = B if (A == B and A != C and B != D) else P
            e2 = C if (D == C and D != B and C != A) else P
            e3 = D if (B == D and B != A and D != C) else P
            out[2 * y][2 * x], out[2 * y][2 * x + 1], out[2 * y + 1][2 * x], out[2 * y + 1][2 * x + 1] = e0, e1, e2, e3
    return out


N4 = ((1, 0), (-1, 0), (0, 1), (0, -1))


def upgrade(g16, pal):
    g = scale2x([list(r) for r in g16])
    at = lambda x, y: g[y][x] if 0 <= x < N and 0 <= y < N else "."
    is_fill = lambda c: c not in (".", "o", "h")
    # ---------- 2. làm mảnh viền ----------
    edge_dark = set()      # điểm viền cũ chuyển thành mép tối của mảng màu
    touch_out = [[g[y][x] == "o" and any(at(x + dx, y + dy) == "." for dx, dy in N4) for x in range(N)] for y in range(N)]
    newg = [row[:] for row in g]
    for y in range(N):
        for x in range(N):
            if g[y][x] != "o" or touch_out[y][x]:
                continue
            fills = [at(x + dx, y + dy) for dx, dy in N4 if is_fill(at(x + dx, y + dy))]
            if not fills:
                continue
            outer_nb = any(0 <= x + dx < N and 0 <= y + dy < N and touch_out[y + dy][x + dx] for dx, dy in N4)
            if outer_nb:
                newg[y][x] = max(set(fills), key=fills.count); edge_dark.add((x, y))
    g = newg
    # ---------- 3. tô khối từng mảng ----------
    col = [[None] * N for _ in range(N)]
    seen = [[False] * N for _ in range(N)]
    for sy in range(N):
        for sx in range(N):
            c = g[sy][sx]
            if seen[sy][sx] or not is_fill(c):
                continue
            region, stack = [], [(sx, sy)]
            seen[sy][sx] = True
            while stack:
                x, y = stack.pop(); region.append((x, y))
                for dx, dy in N4:
                    xx, yy = x + dx, y + dy
                    if 0 <= xx < N and 0 <= yy < N and not seen[yy][xx] and is_fill(g[yy][xx]) and grp(g[yy][xx]) == grp(c):
                        seen[yy][xx] = True; stack.append((xx, yy))
            shade_region(region, grp(c), pal, g, col, edge_dark)
    # điểm sáng 'h': điểm đầu cụm trắng tinh, phần còn lại là tông sáng của màu bên cạnh
    for y in range(N):
        for x in range(N):
            if g[y][x] != "h":
                continue
            lead = at(x - 1, y) != "h" and at(x, y - 1) != "h"
            if lead:
                col[y][x] = "#FFFFFF"
            else:
                nb = [col[y + dy][x + dx] for dx, dy in N4 if 0 <= x + dx < N and 0 <= y + dy < N and col[y + dy][x + dx] and g[y + dy][x + dx] not in ("h", "o")]
                col[y][x] = _to_hex(tuple(0.55 + 0.45 * v for v in _hex(nb[0]))) if nb else "#FFF8F0"
    # ---------- 4. viền ----------
    for y in range(N):
        for x in range(N):
            if g[y][x] != "o":
                continue
            nb = [g[y + dy][x + dx] for dx, dy in N4 if 0 <= x + dx < N and 0 <= y + dy < N and is_fill(g[y + dy][x + dx])]
            if nb:
                base = pal[max(set(nb), key=nb.count)]
                r0 = ramp(base)[0]
                col[y][x] = _to_hex(tuple(a * 0.55 + b * 0.45 for a, b in zip(_hex(r0), _hex(INK))))
            else:
                col[y][x] = INK
    return col


def shade_region(region, ch, pal, g, col, edge_dark):
    rp = ramp(pal[ch])
    S = set(region)
    xs = [p[0] for p in region]; ys = [p[1] for p in region]
    w, h = max(xs) - min(xs) + 1, max(ys) - min(ys) + 1
    cy = (min(ys) + max(ys)) / 2
    area = len(region)
    dirs = [(1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, -1), (1, -1), (-1, 1)]

    def dist(x, y, dx, dy):
        k = 0
        while (x + dx * (k + 1), y + dy * (k + 1)) in S and k < 12:
            k += 1
        return k + 1

    tiny = area < 10 or min(w, h) <= 3
    lits = {}
    for (x, y) in region:
        if tiny:
            lits[(x, y)] = 0.6 if ((x, y - 1) not in S or (x - 1, y) not in S) else 0.0
            continue
        nx = ny = 0.0
        for dx, dy in dirs:
            d = dist(x, y, dx, dy)
            wgt = 1.0 if dx == 0 or dy == 0 else 0.7
            nx -= dx * wgt / d
            ny -= dy * wgt / d
        mag = math.hypot(nx, ny)
        lit = (nx * LIGHT[0] + ny * LIGHT[1]) / mag * min(1.0, mag * 1.1) if mag > 1e-6 else 0.0
        lit -= (y - cy) / max(h / 2, 1) * 0.12
        lits[(x, y)] = lit
    tone = {}
    for (x, y) in region:
        v = lits[(x, y)]
        tone[(x, y)] = 4 if v > 0.52 else 3 if v > -0.28 else 2 if v > -0.7 else 1
    if not tiny:   # lọc đa số 3×3 trong khối: bỏ điểm tông lẻ cho mảng màu sạch
        sm = {}
        for (x, y) in region:
            nb = [tone[(x + dx, y + dy)] for dx in (-1, 0, 1) for dy in (-1, 0, 1) if (x + dx, y + dy) in S]
            cnt = {}
            for t in nb:
                cnt[t] = cnt.get(t, 0) + 1
            best = max(cnt, key=lambda t: (cnt[t], t == tone[(x, y)]))
            sm[(x, y)] = best if cnt[best] >= 5 else tone[(x, y)]
        tone = sm
    for (x, y) in region:
        t = tone[(x, y)]
        if g[y][x] in PAIR:          # ô tô bóng vẽ tay trong bản 16×16
            t = max(1, t - 1)
        if (x, y) in edge_dark:      # nửa trong của viền cũ -> mép tối
            t = min(t, 3 if lits[(x, y)] > 0.25 else 2)
        col[y][x] = rp[t]
    # điểm bóng loáng cho mảng lớn, vật liệu bóng
    has_h = any(g[y][x] == "h" for y in range(max(0, min(ys) - 1), min(N, max(ys) + 2)) for x in range(max(0, min(xs) - 1), min(N, max(xs) + 2)))
    if ch in GLOSSY and area >= 40 and min(w, h) >= 7 and not has_h:
        cand = [p for p in region if lits[p] > 0.3 and p not in edge_dark and all((p[0] + dx, p[1] + dy) in S for dx, dy in N4)]
        if cand:
            x, y = min(cand, key=lambda p: (p[0] - min(xs)) + (p[1] - min(ys)))
            col[y][x] = "#FFFFFF"
            for dx, dy in ((1, 0), (0, 1)):
                if (x + dx, y + dy) in S:
                    col[y + dy][x + dx] = rp[5]


def to_image(col):
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    px = img.load()
    for y in range(N):
        for x in range(N):
            c = col[y][x]
            if c:
                r, gg, b = (int(c[i:i + 2], 16) for i in (1, 3, 5))
                px[x, y] = (r, gg, b, 255)
    return img


def png_b64(img):
    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True)
    return base64.b64encode(buf.getvalue()).decode("ascii")


def render_all(names=None):
    grids, pal = load_grids()
    return {n: to_image(upgrade(g, pal)) for n, g in grids.items() if not names or n in names}


def sheet(imgs, path, cols=16, sc=2):
    cell = 32 * sc + 8
    rows = (len(imgs) + cols - 1) // cols
    sh = Image.new("RGBA", (cols * cell + 8, rows * (cell + 12) + 8), (251, 235, 216, 255))
    d = ImageDraw.Draw(sh)
    for k, (n, im) in enumerate(imgs.items()):
        x, y = 8 + (k % cols) * cell, 8 + (k // cols) * (cell + 12)
        sh.alpha_composite(im.resize((32 * sc, 32 * sc), Image.NEAREST), (x, y))
        d.text((x, y + 32 * sc + 1), n[:11], fill=(74, 51, 80, 255))
    sh.save(path)


def inject(imgs):
    p = os.path.join(ROOT, "web", "icons.js")
    s = open(p, encoding="utf8").read()
    body = "  var IMG32 = {\n" + ",\n".join('    "%s": "%s"' % (n, png_b64(im)) for n, im in sorted(imgs.items())) + "\n  };\n"
    a, b = "  /* ICON32-START */\n", "  /* ICON32-END */\n"
    s = s[:s.index(a) + len(a)] + body + s[s.index(b):]
    open(p, "w", encoding="utf8").write(s)
    print("Đã nhúng %d icon 32×32 (%.0f KB)" % (len(imgs), len(body) / 1024))


if __name__ == "__main__":
    args = sys.argv[1:]
    if args[:1] == ["--inject"]:
        inject(render_all())
    elif args[:1] == ["--sheet"]:
        imgs = render_all(args[2:] or None)
        sheet(imgs, args[1])
        print(len(imgs), "icon")
    else:
        print(__doc__)
