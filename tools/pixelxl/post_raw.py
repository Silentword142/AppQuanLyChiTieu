"""Giữ nguyên ảnh gốc pixel-art-xl: chỉ tách nền + thu về đúng lưới pixel gốc rồi về cỡ đích.
Không ép màu, không làm phẳng, không soi gương.

python tools/pixelxl/post_raw.py RAW_DIR OUT_DIR [--size 64] [--sheet s.png]
"""
import argparse
from pathlib import Path
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

def remove_bg(a, tol=26):
    border = np.concatenate([a[0], a[-1], a[:, 0], a[:, -1]]); bg = np.median(border, 0)
    dist = np.sqrt(((a - bg) ** 2).sum(2))
    near = (dist < tol) | ((a.max(2) - a.min(2) < 14) & (a.mean(2) > 125))   # nền + bóng xám nhạt
    lab, _ = ndi.label(near)
    edge = np.unique(np.concatenate([lab[0], lab[-1], lab[:, 0], lab[:, -1]]))
    fg = ~np.isin(lab, edge[edge > 0])
    lab, n = ndi.label(fg)
    if n:
        s = ndi.sum(fg, lab, range(1, n + 1)); fg = np.isin(lab, np.nonzero(s >= s.max() * 0.04)[0] + 1)
    return fg

def mode_down(a, fg, k):
    """Thu nhỏ k lần: mỗi ô kxk lấy màu xuất hiện nhiều nhất (giữ nét pixel art)."""
    H, W = fg.shape; h, w = H // k, W // k
    out = np.zeros((h, w, 3), np.uint8); al = np.zeros((h, w), bool)
    q = (a // 8).astype(int); key = (q[..., 0] << 10) | (q[..., 1] << 5) | q[..., 2]
    for y in range(h):
        for x in range(w):
            m = fg[y*k:(y+1)*k, x*k:(x+1)*k]
            if m.mean() < 0.5: continue
            kk = key[y*k:(y+1)*k, x*k:(x+1)*k][m]; v = np.bincount(kk).argmax()
            px = a[y*k:(y+1)*k, x*k:(x+1)*k][m][kk == v].mean(0)
            out[y, x] = px; al[y, x] = True
    return out, al

def native_scale(a):
    """Ước lượng cỡ ô pixel gốc (thường 8 ở ảnh 1024)."""
    g = np.abs(np.diff(a.mean(2), axis=1)).sum(0); best, bs = 8, -1
    for k in range(4, 17):
        sc = g[k-1::k].mean() / (g.mean() + 1e-6)
        if sc > bs + 0.15: best, bs = k, sc
    return best

def process(p, size):
    a = np.asarray(Image.open(p).convert('RGB')).astype(float)
    fg = remove_bg(a); k = 8   # pixel-art-xl ở 1024px: mỗi pixel gốc = 8x8
    small, al = mode_down(a, fg, k)
    ys, xs = np.nonzero(al); small = small[ys.min():ys.max()+1, xs.min():xs.max()+1]; al = al[ys.min():ys.max()+1, xs.min():xs.max()+1]
    rgba = np.dstack([small, al.astype(np.uint8) * 255]); im = Image.fromarray(rgba, 'RGBA')
    inner = size - 4
    if max(im.size) > inner:   # lớn hơn đích: thu tiếp bằng nearest để không nhoè
        r = inner / max(im.size); im = im.resize((max(1, round(im.width * r)), max(1, round(im.height * r))), Image.NEAREST)
    out = Image.new('RGBA', (size, size)); out.paste(im, ((size - im.width) // 2, (size - im.height) // 2)); return out, k

def main():
    ap = argparse.ArgumentParser(); ap.add_argument('raw'); ap.add_argument('out')
    ap.add_argument('--size', type=int, default=64); ap.add_argument('--sheet'); a = ap.parse_args()
    out = Path(a.out); out.mkdir(parents=True, exist_ok=True); res = {}
    for p in sorted(Path(a.raw).glob('*.png')):
        ic, k = process(p, a.size); ic.save(out / p.name); res[p.stem] = ic; print(p.name, 'cell', k)
    if a.sheet:
        from PIL import ImageDraw
        n = a.size * 4; W = len(res) * (n + 20) + 20
        sh = Image.new('RGB', (W, n + 40 + 2 * (a.size + 12)), (255, 246, 233)); d = ImageDraw.Draw(sh)
        sh.paste((42, 33, 64), (0, n + 40 + a.size + 12, W, sh.height))
        for i, (k, ic) in enumerate(res.items()):
            x = 20 + i * (n + 20); b = ic.resize((n, n), Image.NEAREST); sh.paste(b, (x, 16), b)
            d.text((x, n + 20), f'#{i+1} {k}', fill=(59, 42, 63))
            sh.paste(ic, (x, n + 40), ic); sh.paste(ic, (x, n + 40 + a.size + 12 + 6), ic)
        sh.save(a.sheet)

if __name__ == '__main__': main()
