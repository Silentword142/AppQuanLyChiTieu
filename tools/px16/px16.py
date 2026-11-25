"""Bộ vẽ icon pixel 16x16 kiểu đơn giản (theo mẫu anh Hoàng gửi 2026-11-25).

Mỗi icon là một bản vẽ ASCII. Chữ cái = màu (thường = sáng, HOA = đậm), '#' = nét viền đậm,
chữ số = màu KHÔNG tự sinh viền (nét mảnh, gạch chéo...). '.' = trống.
Bộ vẽ tự thêm viền ngoài 1px (4 hướng) quanh mọi ô chữ cái, rồi thêm bóng đổ lệch (1,1).
Kết quả căn giữa trong khung 16x16.
"""
from PIL import Image, ImageDraw

OUTLINE = (59, 51, 85); SHADOW = (186, 177, 181); BG = (255, 246, 233)
COL = {
 'y': 'FFE8A3', 'Y': 'F5C451', 'w': 'FFFFFF', 'g': 'E3DDF0', 'G': 'A79FC0',
 'm': 'B5EAD7', 'M': '6FCF9F', 'p': 'FFB7C9', 'P': 'FF8FAB', 'l': 'CDB8F6', 'L': '9F86E0',
 'b': 'A9D8FF', 'B': '6AA8F0', 'o': 'FFCBA4', 'O': 'FF9F6B', 'c': 'F2D2B0', 'C': 'D9A47A',
 'r': 'FFA3A3', 'R': 'E8707E',
 '1': 'FF8FAB', '2': 'FFFFFF', '3': 'F5C451', '4': '6AA8F0', '5': '6FCF9F', '6': 'A79FC0',
 '7': '9F86E0', '8': 'E8707E', '9': 'D9A47A', '0': '3B3355',
}
RGB = {k: tuple(int(v[i:i+2], 16) for i in (0, 2, 4)) for k, v in COL.items()}
N = 16

def parse(art):
    rows = [r.rstrip() for r in art.strip('\n').split('\n')]
    rows = [r for r in rows]
    w = max(len(r) for r in rows)
    return [r.ljust(w, '.') for r in rows]

def render(art, name='?'):
    rows = parse(art); h, w = len(rows), len(rows[0])
    H, W = h + 3, w + 3                      # viền 2 phía + bóng
    g = [[None] * W for _ in range(H)]
    for y, r in enumerate(rows):
        for x, ch in enumerate(r):
            if ch == '.' or ch == ' ': continue
            g[y + 1][x + 1] = OUTLINE if ch == '#' else RGB[ch]
    # viền ngoài
    src = [(y + 1, x + 1) for y, r in enumerate(rows) for x, ch in enumerate(r) if ch.isalpha()]
    for y, x in src:
        for dy, dx in ((0, 1), (0, -1), (1, 0), (-1, 0)):
            if g[y + dy][x + dx] is None: g[y + dy][x + dx] = OUTLINE
    # bóng đổ
    filled = [(y, x) for y in range(H) for x in range(W) if g[y][x] is not None]
    for y, x in filled:
        if y + 1 < H and x + 1 < W and g[y + 1][x + 1] is None: g[y + 1][x + 1] = 'S'
    # cắt khung chứa, căn giữa trong 16x16
    ys = [y for y in range(H) for x in range(W) if g[y][x]]; xs = [x for y in range(H) for x in range(W) if g[y][x]]
    y0, y1, x0, x1 = min(ys), max(ys), min(xs), max(xs)
    bh, bw = y1 - y0 + 1, x1 - x0 + 1
    if bh > N or bw > N: raise ValueError(f'{name}: {bw}x{bh} > 16')
    oy, ox = (N - bh) // 2, (N - bw) // 2
    im = Image.new('RGBA', (N, N), (0, 0, 0, 0)); px = im.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            c = g[y][x]
            if c is None: continue
            px[x - x0 + ox, y - y0 + oy] = (*(SHADOW if c == 'S' else c), 255)
    return im

def sheet(items, path, scale=6, cols=8):
    pad = 14; lh = 16; cell = N * scale
    rows = (len(items) + cols - 1) // cols
    im = Image.new('RGB', (cols * (cell + pad) + pad, rows * (cell + pad + lh) + pad), BG); d = ImageDraw.Draw(im)
    for i, (label, ic) in enumerate(items):
        x = pad + (i % cols) * (cell + pad); y = pad + (i // cols) * (cell + pad + lh)
        big = ic.resize((cell, cell), Image.NEAREST); im.paste(big, (x, y), big)
        d.text((x, y + cell + 3), label, fill=OUTLINE)
    im.save(path)
