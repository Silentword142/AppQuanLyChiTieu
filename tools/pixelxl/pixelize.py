#!/usr/bin/env python3
"""
Hậu kỳ "pixel sạch" cho ảnh sinh từ SDXL + LoRA nerijs/pixel-art-xl.

Model card khuyên: "Downscale 8 times to get pixel perfect images (use Nearest Neighbors)".
Ảnh SDXL 1024px vì vậy có lưới ~8px/điểm (128 ô). Để icon đơn giản và đồng đều, ta:
  1. Tách nền (flood fill từ 4 góc, nền trắng/đơn sắc) nếu ảnh chưa trong suốt.
  2. Cắt sát vật thể, chia thành lưới GRID ô (mặc định 32) theo cạnh dài, mỗi ô lấy
     màu chiếm đa số (không lấy trung bình -> không ra màu nhoè).
  3. Ép về bảng màu pastel của app (PAL trong web/icons.js) -> cả bộ icon chung màu.
  4. Bỏ điểm lẻ, thêm viền ngoài 1 ô màu mực mận.
  5. Phóng nguyên lần (x2) bằng Nearest lên 64x64 -> từng điểm vuông vức, không vỡ.

Dùng:
  python3 tools/pixelxl/pixelize.py in_dir out_dir [--grid 32] [--size 64]
"""
import argparse, sys
from collections import Counter
from pathlib import Path
from PIL import Image

# Bảng màu app (khớp PAL trong web/icons.js) + vài tông trung gian để tô khối
PALETTE = [
    "#FFF6E9", "#FFFFFF", "#FBB4BA", "#E7798F", "#EE6B6E", "#C04860",
    "#FFDF85", "#F2B24C", "#FFBB85", "#EC8D5C", "#9BE0AE", "#55B27F",
    "#7DD3C6", "#3E9E98", "#A5D8F7", "#6E9FDE", "#D3B9F2", "#9C7BD3",
    "#EBC49A", "#B98058", "#84553F", "#E3D8E0", "#AE9DB0", "#6F5272",
    "#FFDCC2", "#3B2A3F", "#2F7A5A", "#4A6FB0", "#8E3550", "#C9A07A",
    # hồng pastel thêm để heo đất, son... không bị ép thành trắng/xám
    "#FFD6DC", "#F7B7C4", "#F59AAE", "#FCE3E8",
]
INK = (0x3B, 0x2A, 0x3F)  # viền ngoài – mận đậm


def hex2rgb(h):
    h = h.lstrip("#"); return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))

PAL_RGB = [hex2rgb(h) for h in PALETTE]


def nearest(c):
    r, g, b = c[:3]
    best, bd = None, 1e18
    for p in PAL_RGB:
        # khoảng cách có trọng số theo cảm nhận (redmean)
        rm = (r + p[0]) / 2
        d = (2 + rm / 256) * (r - p[0]) ** 2 + 4 * (g - p[1]) ** 2 + (2 + (255 - rm) / 256) * (b - p[2]) ** 2
        if d < bd: best, bd = p, d
    return best


def remove_bg(im, tol=40):
    """Flood fill từ viền ảnh: điểm gần màu nền -> trong suốt."""
    im = im.convert("RGBA")
    w, h = im.size
    px = im.load()
    if any(px[x, y][3] < 10 for x, y in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1))):
        return im  # đã có nền trong suốt
    edge = Counter(px[x, y][:3] for x in range(0, w, 4) for y in (0, h - 1))
    bg = edge.most_common(1)[0][0]
    seen = bytearray(w * h)
    stack = [(x, y) for x in range(w) for y in (0, h - 1)] + [(x, y) for y in range(h) for x in (0, w - 1)]
    while stack:
        x, y = stack.pop()
        i = y * w + x
        if seen[i]: continue
        seen[i] = 1
        r, g, b, a = px[x, y]
        if abs(r - bg[0]) + abs(g - bg[1]) + abs(b - bg[2]) > tol: continue
        px[x, y] = (0, 0, 0, 0)
        if x > 0: stack.append((x - 1, y))
        if x < w - 1: stack.append((x + 1, y))
        if y > 0: stack.append((x, y - 1))
        if y < h - 1: stack.append((x, y + 1))
    return im


def drop_shadow(im):
    """Xoá bóng đổ xám mềm dưới chân vật thể (SDXL hay vẽ dù đã cấm trong prompt)."""
    a = im.getchannel("A").point(lambda v: 255 if v > 128 else 0)
    bbox = a.getbbox()
    if not bbox:
        return im
    x0, y0, x1, y1 = bbox
    cut = y0 + int((y1 - y0) * 0.70)
    px = im.load()
    w, h = im.size
    for _ in range(3):  # bóc dần từ ngoài vào
        kill = []
        for y in range(cut, y1):
            for x in range(x0, x1):
                r, g, b, al = px[x, y]
                if al < 10:
                    continue
                mx, mn = max(r, g, b), min(r, g, b)
                grey = mx - mn < 28 and 90 < mx < 235
                if not grey:
                    continue
                edge = any(not (0 <= x + dx < w and 0 <= y + dy < h) or px[x + dx, y + dy][3] < 10
                           for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if edge:
                    kill.append((x, y))
        for x, y in kill:
            px[x, y] = (0, 0, 0, 0)
    return im


def to_grid(im, grid, fill=0.82):
    """Cắt sát vật thể, lấy màu đa số cho từng ô của lưới grid x grid."""
    bbox = im.getchannel("A").point(lambda a: 255 if a > 128 else 0).getbbox()
    if not bbox:
        return Image.new("RGBA", (grid, grid))
    im = im.crop(bbox)
    w, h = im.size
    cells = max(1, round(grid * fill))       # vật thể chiếm ~82% khung, chừa chỗ viền
    step = max(w, h) / cells
    gw, gh = max(1, round(w / step)), max(1, round(h / step))
    px = im.load()
    out = Image.new("RGBA", (grid, grid))
    op = out.load()
    ox, oy = (grid - gw) // 2, (grid - gh) // 2
    for gy in range(gh):
        for gx in range(gw):
            x0, x1 = int(gx * step), min(w, max(int(gx * step) + 1, int((gx + 1) * step)))
            y0, y1 = int(gy * step), min(h, max(int(gy * step) + 1, int((gy + 1) * step)))
            cnt, opaque, total = Counter(), 0, 0
            for y in range(y0, y1):
                for x in range(x0, x1):
                    total += 1
                    c = px[x, y]
                    if c[3] > 128:
                        opaque += 1
                        cnt[nearest(c)] += 1
            if total and opaque * 2 >= total:
                op[ox + gx, oy + gy] = cnt.most_common(1)[0][0] + (255,)
    return out


def tidy(g):
    """Bỏ điểm lẻ (khác hết 4 hàng xóm) rồi thêm viền ngoài màu mực."""
    w, h = g.size
    src = g.copy(); s = src.load(); p = g.load()
    for y in range(1, h - 1):
        for x in range(1, w - 1):
            c = s[x, y]
            if c[3] == 0: continue
            nb = [s[x + 1, y], s[x - 1, y], s[x, y + 1], s[x, y - 1]]
            if all(n[3] == 0 for n in nb):
                p[x, y] = (0, 0, 0, 0)
            elif all(n != c for n in nb):
                p[x, y] = Counter(n for n in nb if n[3]).most_common(1)[0][0]
    src = g.copy(); s = src.load()
    for y in range(h):
        for x in range(w):
            if s[x, y][3]: continue
            if any(0 <= x + dx < w and 0 <= y + dy < h and s[x + dx, y + dy][3]
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                p[x, y] = INK + (255,)
    return g


def pixelize(im, grid=32, size=64, bg_tol=40):
    g = tidy(to_grid(drop_shadow(remove_bg(im, bg_tol)), grid))
    return g.resize((size, size), Image.NEAREST)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("inp"); ap.add_argument("out")
    ap.add_argument("--grid", type=int, default=32)
    ap.add_argument("--size", type=int, default=64)
    ap.add_argument("--bg-tol", type=int, default=40)
    a = ap.parse_args()
    if a.size % a.grid:
        sys.exit("--size phải chia hết cho --grid để điểm ảnh vuông vức")
    out = Path(a.out); out.mkdir(parents=True, exist_ok=True)
    for f in sorted(Path(a.inp).glob("*.png")):
        pixelize(Image.open(f), a.grid, a.size, a.bg_tol).save(out / f.name, optimize=True)
        print("ok", f.name)


if __name__ == "__main__":
    main()
