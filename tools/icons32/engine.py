"""Bộ dựng icon pixel 32×32 cho Spendy.

Mỗi icon là danh sách khối hình (ellipse / hộp / đa giác / nét / điểm) gắn chất liệu.
Bộ dựng tự:
  * tạo dải 6 tông cho mỗi chất liệu (bóng ngả tím, sáng ngả vàng – hue shifting)
  * tô khối: cầu (sphere), trụ đứng (cylx), trụ nằm (cyly), vát cạnh (bevel), phẳng (flat)
  * kẻ viền trong giữa các khối bằng tông tối của khối phía trên (selective outline)
  * kẻ viền ngoài bằng tông tối nhất của chất liệu kề bên
  * thêm điểm bóng loáng (gloss) cho vật liệu bóng
Kết quả: ảnh PNG 32×32 RGBA.
"""
import colorsys
import math
from PIL import Image

N = 32
LIGHT = (-0.55, -0.65, 0.52)  # hướng tới nguồn sáng (trên-trái, hơi phía trước)
_l = math.sqrt(sum(v * v for v in LIGHT))
LIGHT = tuple(v / _l for v in LIGHT)


def _hex(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) / 255 for i in (0, 2, 4))


def _to_hex(rgb):
    return "#" + "".join("%02x" % max(0, min(255, round(v * 255))) for v in rgb)


def _shift_hue(h, target, amt):
    d = ((target - h + 0.5) % 1.0) - 0.5
    return (h + d * amt) % 1.0


def ramp(base):
    """6 tông: 0 viền, 1 tối sâu, 2 bóng, 3 gốc, 4 sáng, 5 bóng loáng"""
    r, g, b = _hex(base)
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    out = []
    spec = [(-0.50, 0.34, 0.10), (-0.30, 0.22, 0.04), (-0.15, 0.11, 0.0), (0, 0, 0), (0.12, -0.06, -0.06), (0.26, -0.12, -0.18)]
    for dl, hue_amt, ds in spec:
        hh = _shift_hue(h, 0.75 if dl < 0 else 0.14, abs(hue_amt)) if dl != 0 else h
        ll = max(0.05, min(0.97, l + dl if dl < 0 else l + (1 - l) * dl * 1.6))
        ss = max(0, min(1, s + ds))
        out.append(colorsys.hls_to_rgb(hh, ll, ss))
    out[0] = tuple(v * 0.78 for v in out[0])
    return [_to_hex(c) for c in out]


# Chất liệu (màu gốc) – càng nhiều màu càng sinh động
M = {
    "red": "#E8434F", "tomato": "#F25C3C", "orange": "#F5913B", "peach": "#FFB38A", "yellow": "#FFD447",
    "butter": "#FFE08A", "gold": "#F2B430", "lime": "#A6D84A", "green": "#4CB05E", "leaf": "#5DBB4A",
    "mint": "#7FDDB0", "teal": "#2FB6A8", "sky": "#6CC4F2", "blue": "#3F84E5", "navy": "#34508E",
    "lav": "#B79CEB", "purple": "#8C5BD6", "pink": "#FF8FB1", "rose": "#E35D8C", "magenta": "#D44BB8",
    "cream": "#FFF1D6", "white": "#F4F1F6", "paper": "#FBF4E6", "skin": "#FFCFA8", "bread": "#E9A04F",
    "crust": "#C77A35", "choc": "#7A4632", "coffee": "#8A5534", "wood": "#C98A4F", "darkwood": "#8F5A34",
    "steel": "#A7B2C4", "silver": "#D3D9E3", "iron": "#6E7487", "dark": "#4A4458", "black": "#2F2A3A",
    "glass": "#BFE8FF", "water": "#58B8F0", "noodle": "#FFE7A6", "rice": "#FBFAF2", "meat": "#C8574B",
    "egg": "#FFC93C", "grape": "#7E4FC2", "berry": "#D33F6A", "sand": "#EFD39A", "grass": "#6FC255",
    "brick": "#D2694B", "slate": "#7B8BA6", "cocoa": "#5A3A2E", "milk": "#F7EBDD", "tea": "#C9935A",
}
RAMPS = {k: ramp(v) for k, v in M.items()}


class Icon:
    def __init__(self):
        self.obj = [[-1] * N for _ in range(N)]   # chỉ số khối
        self.objs = []   # (mat, mode, opts)
        self.fixed = {}  # (x,y) -> màu cố định (chi tiết)

    # ---------- nguyên hàm ----------
    def _add(self, cells, mat, mode, **o):
        idx = len(self.objs)
        self.objs.append({"mat": mat, "mode": mode, "cells": set(), **o})
        for x, y in cells:
            if 0 <= x < N and 0 <= y < N:
                prev = self.obj[y][x]
                if prev >= 0:
                    self.objs[prev]["cells"].discard((x, y))
                self.obj[y][x] = idx
                self.objs[idx]["cells"].add((x, y))
        return self

    def E(self, cx, cy, rx, ry, mat, mode="sphere", **o):
        cells = [(x, y) for y in range(N) for x in range(N) if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1.0]
        o.setdefault("c", (cx, cy, rx, ry))
        return self._add(cells, mat, mode, **o)

    def R(self, x0, y0, x1, y1, mat, mode="bevel", r=0, **o):
        cells = []
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                if r:
                    dx = max(x0 + r - x, 0, x - (x1 - r)); dy = max(y0 + r - y, 0, y - (y1 - r))
                    if dx and dy and (dx - 0.5) ** 2 + (dy - 0.5) ** 2 > r * r:
                        continue
                cells.append((x, y))
        o.setdefault("c", ((x0 + x1 + 1) / 2, (y0 + y1 + 1) / 2, (x1 - x0 + 1) / 2, (y1 - y0 + 1) / 2))
        return self._add(cells, mat, mode, **o)

    def P(self, pts, mat, mode="bevel", **o):
        cells = []
        for y in range(N):
            for x in range(N):
                px, py = x + 0.5, y + 0.5
                inside = False
                j = len(pts) - 1
                for i in range(len(pts)):
                    xi, yi = pts[i]; xj, yj = pts[j]
                    if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi + 1e-9) + xi:
                        inside = not inside
                    j = i
                if inside:
                    cells.append((x, y))
        xs = [p[0] for p in pts]; ys = [p[1] for p in pts]
        o.setdefault("c", ((min(xs) + max(xs)) / 2, (min(ys) + max(ys)) / 2, (max(xs) - min(xs)) / 2 or 1, (max(ys) - min(ys)) / 2 or 1))
        return self._add(cells, mat, mode, **o)

    def L(self, x0, y0, x1, y1, mat, w=2, mode="cylx", **o):
        cells = []
        dx, dy = x1 - x0, y1 - y0
        ln2 = dx * dx + dy * dy or 1
        for y in range(N):
            for x in range(N):
                px, py = x + 0.5, y + 0.5
                t = max(0, min(1, ((px - x0) * dx + (py - y0) * dy) / ln2))
                if math.hypot(px - (x0 + t * dx), py - (y0 + t * dy)) <= w / 2:
                    cells.append((x, y))
        o.setdefault("c", ((x0 + x1) / 2, (y0 + y1) / 2, max(1, abs(dx) / 2 + w / 2), max(1, abs(dy) / 2 + w / 2)))
        return self._add(cells, mat, mode, **o)

    # ---------- khoét lỗ (trong suốt) ----------
    def _erase(self, cells):
        for x, y in cells:
            if 0 <= x < N and 0 <= y < N:
                j = self.obj[y][x]
                if j >= 0:
                    self.objs[j]["cells"].discard((x, y))
                self.obj[y][x] = -1
                self.fixed.pop((x, y), None)
        return self

    def HE(self, cx, cy, rx, ry):
        return self._erase([(x, y) for y in range(N) for x in range(N) if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1.0])

    def HR(self, x0, y0, x1, y1):
        return self._erase([(x, y) for y in range(y0, y1 + 1) for x in range(x0, x1 + 1)])

    def W(self, cx, cy, r, a0, a1, mat, mode="sphere", **o):
        """hình quạt từ góc a0 đến a1 (độ, 0 = phải, chiều kim đồng hồ)"""
        cells = []
        for y in range(N):
            for x in range(N):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                if math.hypot(dx, dy) > r:
                    continue
                a = math.degrees(math.atan2(dy, dx)) % 360
                if (a0 <= a <= a1) if a0 <= a1 else (a >= a0 or a <= a1):
                    cells.append((x, y))
        o.setdefault("c", (cx, cy, r, r))
        return self._add(cells, mat, mode, **o)

    def ring(self, cx, cy, r, w, mat, mode="sphere", **o):
        """vòng tròn dày w (khối độc lập)"""
        cells = [(x, y) for y in range(N) for x in range(N) if r - w <= math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= r]
        o.setdefault("c", (cx, cy, r, r))
        return self._add(cells, mat, mode, **o)

    def D(self, pts, mat, tone=3):
        """điểm chi tiết tô màu cố định (mắt, hạt, chữ…)"""
        col = RAMPS[mat][tone] if mat in RAMPS else mat
        for x, y in pts:
            if 0 <= x < N and 0 <= y < N:
                self.fixed[(x, y)] = col
        return self

    def G(self, pts):
        """điểm bóng loáng trắng"""
        return self.D(pts, "#FFFFFF")

    # ---------- dựng ----------
    def render(self):
        col = [[None] * N for _ in range(N)]
        for i, o in enumerate(self.objs):
            rp = RAMPS[o["mat"]]
            cx, cy, rx, ry = o.get("c", (16, 16, 8, 8))
            cells = o["cells"]
            mode = o["mode"]
            for (x, y) in cells:
                if mode == "flat":
                    t = o.get("tone", 3)
                elif mode == "bevel":
                    up = (x, y - 1) not in cells; lf = (x - 1, y) not in cells
                    dn = (x, y + 1) not in cells; rt = (x + 1, y) not in cells
                    dn2 = (x, y + 2) not in cells
                    t = 3
                    if dn or rt: t = 2
                    elif dn2 and o.get("deep", True): t = 2 if (x, y + 2) not in cells and rx > 3 and ry > 3 else 3
                    if up or lf: t = 4 if not (dn or rt) else 3
                    if up and lf: t = 5
                    t += o.get("tone", 3) - 3
                else:
                    nx = (x + 0.5 - cx) / max(rx, 0.5); ny = (y + 0.5 - cy) / max(ry, 0.5)
                    if mode == "cylx": ny = 0
                    if mode == "cyly": nx = 0
                    d2 = min(1, nx * nx + ny * ny)
                    nz = math.sqrt(1 - d2)
                    inten = nx * LIGHT[0] + ny * LIGHT[1] + nz * LIGHT[2]
                    # dither nhẹ tại ranh giới dải màu
                    bias = 0.035 if (x + y) % 2 else -0.035
                    v = inten + bias
                    t = 5 if v > 0.86 else 4 if v > 0.62 else 3 if v > 0.30 else 2 if v > -0.02 else 1
                    t += o.get("tone", 3) - 3
                t = max(1, min(5, t))
                col[y][x] = rp[t]
            # viền trong: điểm của khối này giáp khối nằm dưới (vẽ trước) khác chất liệu
            if not o.get("noline"):
                for (x, y) in cells:
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        j = self.obj[y + dy][x + dx] if 0 <= x + dx < N and 0 <= y + dy < N else -1
                        if 0 <= j < i and (self.objs[j]["mat"] != o["mat"] or o.get("sep")) and not self.objs[j].get("noline"):
                            col[y][x] = rp[0] if o.get("hardline") else rp[1]
                            break
        for (x, y), c in self.fixed.items():
            col[y][x] = c
        # viền ngoài: màu tối nhất của chất liệu kề bên
        out = [row[:] for row in col]
        for y in range(N):
            for x in range(N):
                if col[y][x] is not None:
                    continue
                best = None
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    xx, yy = x + dx, y + dy
                    if 0 <= xx < N and 0 <= yy < N and col[yy][xx] is not None:
                        j = self.obj[yy][xx]
                        best = RAMPS[self.objs[j]["mat"]][0] if j >= 0 else "#2E1F30"
                        break
                if best:
                    out[y][x] = best
        img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
        px = img.load()
        for y in range(N):
            for x in range(N):
                c = out[y][x]
                if c:
                    h = c.lstrip("#")
                    px[x, y] = (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)
        return img
