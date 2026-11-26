"""Ảnh 512 -> pixel 64: mỗi ô 8x8 lấy màu chiếm nhiều nhất (ưu tiên viền nếu >= 30%),
gắn về bảng màu, rồi thêm bóng đổ lệch 2px."""
import sys, os, numpy as np
from PIL import Image
from svgkit import TONE, OUT
def hx(c): return tuple(int(c[i:i+2], 16) for i in (1, 3, 5))
PAL = sorted({hx(OUT), (255, 255, 255)} | {hx(c) for v in TONE.values() for c in v} | {hx('#C9C0DA')})
PAL = np.array(PAL); OUTC = np.array(hx(OUT)); SHADOW = np.array([186, 177, 181])
def topx(src, k=8, sh=2):
    a = np.array(Image.open(src).convert('RGBA')).astype(int); n = a.shape[0] // k
    rgb = a[..., :3]; al = a[..., 3]
    d = ((rgb[..., None, :] - PAL[None, None]) ** 2).sum(-1); idx = d.argmin(-1)
    idx = np.where(al < 128, -1, idx)
    oi = int(np.where((PAL == OUTC).all(1))[0][0])
    out = np.zeros((n, n, 4), np.uint8)
    for y in range(n):
        for x in range(n):
            c = idx[y*k:(y+1)*k, x*k:(x+1)*k].ravel()
            vals, cnt = np.unique(c, return_counts=True)
            if (c == oi).sum() >= .3 * c.size: v = oi
            else: v = vals[cnt.argmax()]
            if v >= 0: out[y, x, :3] = PAL[v]; out[y, x, 3] = 255
    alpha = out[..., 3] > 0
    shd = np.zeros_like(alpha); shd[sh:, sh:] = alpha[:-sh, :-sh]
    for i in range(1, sh):
        t = np.zeros_like(alpha); t[i:, i:] = alpha[:-i, :-i]; shd |= t
    shd &= ~alpha; out[shd, :3] = SHADOW; out[shd, 3] = 255
    return Image.fromarray(out, 'RGBA')
