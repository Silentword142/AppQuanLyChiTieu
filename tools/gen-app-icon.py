#!/usr/bin/env python3
"""Sinh icon ứng dụng Spendy: đồng xu vàng pixel khắc chữ "S" trên ô nền hồng bo góc pixel.
Chạy: python3 tools/gen-app-icon.py  -> web/icon-512.png + android/res/drawable-*/ic_launcher.png
Lưới 32×32 điểm ảnh, phóng to nguyên lần (512 = 16×) rồi thu nhỏ cho các mật độ Android.
"""
import math
import os
from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
N = 32
INK = "#4A3350"
PAL = {
    "o": INK,
    "1": "#F2A3A4", "2": "#F5B3B0", "3": "#F8C4BC", "4": "#FAD3C8",   # nền trời hồng (trên -> dưới)
    "h": "#FFE3E0",                                                   # viền sáng ô nền
    "d": "#E48E96",                                                   # bóng dưới ô nền
    "w": "#FFF8EE",                                                   # lấp lánh
    "y": "#FFD87E", "Y": "#F2B24C", "A": "#CF7F2A", "H": "#FFF3C4",   # đồng xu: mặt, vành, khắc tối, sáng
    "s": "#D97C88",                                                   # bóng đổ của đồng xu
}
g = [["."] * N for _ in range(N)]


def put(x, y, ch):
    if 0 <= x < N and 0 <= y < N:
        g[y][x] = ch


# ---------- ô nền bo góc pixel (bán kính 3 bậc) ----------
CUT = {0: 3, 1: 1, 2: 1}   # số điểm cắt ở mỗi hàng góc: hàng 0 cắt 3, hàng 1-2 cắt 1 -> bo tròn kiểu pixel


def inside_tile(x, y):
    for yy, xx in ((y, x), (N - 1 - y, x), (y, N - 1 - x), (N - 1 - y, N - 1 - x)):
        if yy in CUT and xx < CUT[yy]:
            return False
    return True


for y in range(N):
    band = "1" if y < 8 else "2" if y < 16 else "3" if y < 24 else "4"
    for x in range(N):
        if inside_tile(x, y):
            put(x, y, band)
# viền mực theo biên
edge = []
for y in range(N):
    for x in range(N):
        if g[y][x] == ".":
            continue
        if any(not (0 <= x + dx < N and 0 <= y + dy < N) or g[y + dy][x + dx] == "." for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            edge.append((x, y))
for x, y in edge:
    put(x, y, "o")
for x in range(3, N - 3):
    put(x, 1, "h")
for y in range(3, N - 3):
    put(1, y, "h")
for x in range(3, N - 3):
    put(x, N - 2, "d"); put(x, N - 3, "d")
for y in range(3, N - 3):
    put(N - 2, y, "d")

# ---------- bóng đổ dưới đồng xu ----------
for x in range(9, 24):
    put(x, 27, "s")
for x in range(11, 22):
    put(x, 28, "s")

# ---------- đồng xu ----------
CX, CY, R = 15.5, 15.0, 10.6
coin = {}
for y in range(N):
    for x in range(N):
        d = math.hypot(x + 0.5 - CX - 0.5, y + 0.5 - CY - 0.5)
        if d <= R:
            coin[(x, y)] = d
for (x, y), d in coin.items():
    dx, dy = x + 1 - CX - 0.5, y + 1 - CY - 0.5
    if d > R - 1.0:
        ch = "o"
    elif d > R - 2.6:
        ch = "H" if (dx + dy) < -R * 0.55 else ("A" if (dx + dy) > R * 0.75 else "Y")
    else:
        ch = "H" if (dx + dy) < -R * 0.95 else "y"
    put(x, y, ch)
# đảm bảo viền liền nét (điểm nào giáp ngoài đồng xu thì là viền)
for (x, y) in list(coin):
    if any((x + dx, y + dy) not in coin for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
        put(x, y, "o")

# ---------- chữ S khắc nổi (kiểu ký hiệu tiền: có vạch trên/dưới) ----------
S = [
    "...AA...",
    ".AAAAAA.",
    "AA....AA",
    "AA......",
    ".AAAAA..",
    "...AAAA.",
    "......AA",
    "AA....AA",
    ".AAAAAA.",
    "...AA...",
]
ox, oy = 12, 11
for j, row in enumerate(S):
    for i, ch in enumerate(row):
        if ch != ".":
            put(ox + i, oy + j, ch)
# vệt sáng nổi bên dưới-phải nét chữ (hiệu ứng dập nổi)
for j, row in enumerate(S):
    for i, ch in enumerate(row):
        if ch == "A":
            nx, ny = ox + i + 1, oy + j + 1
            if g[ny][nx] == "y" and (j + 1 >= len(S) or i + 1 >= len(row) or S[j + 1][i + 1] == "."):
                put(nx, ny, "H")

# ---------- lấp lánh ----------
for (sx, sy) in ((26, 5), (5, 24)):
    put(sx, sy, "w")
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        put(sx + dx, sy + dy, "w")
put(24, 9, "w")
put(7, 5, "w")


def render(scale):
    im = Image.new("RGBA", (N * scale, N * scale), (0, 0, 0, 0))
    px = im.load()
    for y in range(N):
        for x in range(N):
            ch = g[y][x]
            if ch in PAL:
                c = PAL[ch].lstrip("#")
                rgb = tuple(int(c[i:i + 2], 16) for i in (0, 2, 4)) + (255,)
                for yy in range(y * scale, (y + 1) * scale):
                    for xx in range(x * scale, (x + 1) * scale):
                        px[xx, yy] = rgb
    return im


big = render(16)
big.save(os.path.join(ROOT, "web", "icon-512.png"))
for name, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
    p = os.path.join(ROOT, "android", "res", "drawable-" + name, "ic_launcher.png")
    if os.path.isdir(os.path.dirname(p)):
        (render(size // N) if size % N == 0 else big.resize((size, size), Image.LANCZOS)).save(p)
print("\n".join("".join(r) for r in g))
