#!/usr/bin/env python3
"""Sinh icon ứng dụng VinaSpends (TV pixel "Mai" trên nền trời hoàng hôn pastel).
Chạy: python3 tools/gen-app-icon.py  -> web/icon-512.png + android/res/drawable-*/ic_launcher.png
"""
import os
from PIL import Image, ImageDraw

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
N = 32
PAL = {
    "o": "#4A3350", "P": "#EE9AA3", "p": "#F7BDC3", "R": "#D17E8B", "c": "#FCE6D2", "C": "#EBCDB6",
    "n": "#2A2140", "m": "#41325C", "g": "#9DF5C2", "k": "#FFA5BC", "y": "#FFD87E", "Y": "#F2B24C", "h": "#FFF6D8",
}
g = [["."] * N for _ in range(N)]

def rect(x0, y0, x1, y1, ch):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            g[y][x] = ch

def rbox(x0, y0, x1, y1, fill, outline="o"):
    """hộp bo góc 1px: viền + nền"""
    rect(x0 + 1, y0, x1 - 1, y0, outline); rect(x0 + 1, y1, x1 - 1, y1, outline)
    rect(x0, y0 + 1, x0, y1 - 1, outline); rect(x1, y0 + 1, x1, y1 - 1, outline)
    rect(x0 + 1, y0 + 1, x1 - 1, y1 - 1, fill)

# ăng-ten
for i in range(5):
    g[6 + i][10 + i] = "o"; g[6 + i][21 - i] = "o"
rect(13, 10, 18, 10, "o")
for (cx, cy) in ((8, 3), (22, 3)):
    rect(cx, cy, cx + 1, cy + 1, "y"); g[cy][cx] = "h"; rect(cx, cy + 2, cx + 1, cy + 2, "Y")
    for x in (cx - 1, cx + 2):
        rect(x, cy, x, cy + 2, "o")
    rect(cx, cy - 1, cx + 1, cy - 1, "o"); rect(cx, cy + 3, cx + 1, cy + 3, "o")

# thân TV
rbox(2, 11, 29, 27, "P")
rect(3, 12, 28, 12, "p"); rect(3, 26, 28, 26, "R")
# khung kem quanh màn hình
rbox(4, 13, 21, 25, "c")
rect(5, 24, 20, 24, "C")
# màn hình
rbox(6, 15, 19, 23, "n")
rect(8, 17, 17, 21, "m")
# mặt cười
rect(10, 18, 10, 19, "g"); rect(15, 18, 15, 19, "g")
g[20][9] = "k"; g[20][16] = "k"
g[20][11] = "g"; g[20][14] = "g"; rect(12, 21, 13, 21, "g")
g[16][7] = "C"; g[16][8] = "C"  # vệt sáng kính
# bảng điều khiển bên phải: núm, nút, loa
rbox(23, 14, 27, 18, "c"); g[15][25] = "o"; g[16][25] = "o"
rect(23, 20, 27, 20, "o"); g[21][23] = "o"; g[21][24] = "y"; g[21][25] = "o"; g[21][26] = "g"; g[21][27] = "o"; rect(23, 22, 27, 22, "o")
for y in (23, 25):
    rect(23, y, 27, y, "R")
# chân
for x0 in (5, 22):
    rect(x0, 28, x0 + 4, 28, "o"); rect(x0 + 1, 28, x0 + 3, 28, "n"); rect(x0, 29, x0 + 4, 29, "o")

def tv_image(scale):
    im = Image.new("RGBA", (N * scale, N * scale), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    for y in range(N):
        for x in range(N):
            ch = g[y][x]
            if ch in PAL:
                d.rectangle([x * scale, y * scale, (x + 1) * scale - 1, (y + 1) * scale - 1], fill=PAL[ch])
    return im

def background(size):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    top, bot = (0xF0, 0xA3, 0xA2), (0xF8, 0xD3, 0xC6)
    grad = Image.new("RGBA", (size, size))
    gd = ImageDraw.Draw(grad)
    for y in range(size):
        t = y / (size - 1)
        gd.line([(0, y), (size, y)], fill=tuple(int(top[i] + (bot[i] - top[i]) * t) for i in range(3)) + (255,))
    # mây pixel
    u = size // 32
    for (cx, cy, w) in ((3, 6, 7), (23, 4, 6), (25, 27, 5)):
        gd.rectangle([cx * u, cy * u, (cx + w) * u, (cy + 2) * u], fill="#FFF1E8")
        gd.rectangle([(cx + 2) * u, (cy - 1) * u, (cx + w - 2) * u, cy * u], fill="#FFF1E8")
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size - 1, size - 1], radius=int(size * 0.22), fill=255)
    im.paste(grad, (0, 0), mask)
    # viền mận
    ImageDraw.Draw(im).rounded_rectangle([2, 2, size - 3, size - 3], radius=int(size * 0.22), outline="#4A3350", width=max(2, size // 64))
    return im

def icon(size):
    big = 512
    bg = background(big)
    tv = tv_image(14)  # 448px
    bg.alpha_composite(tv, ((big - tv.width) // 2, (big - tv.height) // 2 + 6))
    return bg if size == big else bg.resize((size, size), Image.LANCZOS)

icon(512).save(os.path.join(ROOT, "web", "icon-512.png"))
for name, px in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
    p = os.path.join(ROOT, "android", "res", "drawable-" + name, "ic_launcher.png")
    if os.path.isdir(os.path.dirname(p)):
        icon(px).save(p)
print("\n".join("".join(r) for r in g))
