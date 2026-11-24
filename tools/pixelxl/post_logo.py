"""Hậu kỳ ảnh gốc SDXL (nền trắng trơn) -> icon pixel 64x64, 3 sắc độ, đối xứng kiểu logo.

Chạy trên máy cloud (không cần GPU). Các bước:
 1. Tách nền: lấy màu nền từ viền ảnh, loang từ mép vào qua các điểm gần màu nền
    (giữ vùng trắng bên trong vật), bỏ đốm lẻ, giữ khối chính.
 2. Cắt sát vật, đặt giữa khung vuông có lề.
 3. Thu về lưới 64: mỗi ô lấy màu chiếm đa số (sau khi gom màu bằng k-means) -> nét sạch.
 4. Ép 3 sắc độ: gom màu theo họ (đỏ, hồng, cam, vàng, xanh lá, xanh dương, tím, nâu, trung tính);
    mỗi họ chỉ còn sáng / vừa / tối. Đỏ và hồng tách riêng.
 5. Đối xứng (tuỳ chọn --mirror): soi gương nửa trái sang phải.
 6. Viền ngoài 1px màu tối cùng tông.

python tools/pixelxl/post_logo.py RAW_DIR OUT_DIR [--size 64] [--mirror auto|on|off] [--sheet sheet.png]
"""
import argparse, colorsys
from pathlib import Path
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

def load(p):
    im = Image.open(p).convert('RGB')
    if max(im.size) > 512: im = im.resize((512, 512), Image.LANCZOS)
    return np.asarray(im).astype(float)

def remove_bg(a, tol=26):
    H, W, _ = a.shape
    border = np.concatenate([a[0], a[-1], a[:, 0], a[:, -1]])
    bg = np.median(border, 0)
    dist = np.sqrt(((a - bg) ** 2).sum(2))
    near = dist < tol
    lab, _ = ndi.label(near)
    edge_labels = np.unique(np.concatenate([lab[0], lab[-1], lab[:, 0], lab[:, -1]]))
    bgmask = np.isin(lab, edge_labels[edge_labels > 0])
    # bóng mờ xám nhạt sát nền (bóng đổ) cũng coi là nền
    soft = (dist < tol * 2.2) & (a.std(2) < 10)
    lab2, _ = ndi.label(soft | bgmask)
    bgmask |= np.isin(lab2, np.unique(lab2[bgmask]))
    fg = ~bgmask
    fg = ndi.binary_opening(fg, iterations=2)
    lab, n = ndi.label(fg)
    if n:
        sizes = ndi.sum(fg, lab, range(1, n + 1)); keep = sizes >= max(sizes.max() * 0.06, 60)
        fg = np.isin(lab, np.nonzero(keep)[0] + 1)
    # lấp lỗ nhỏ (đốm nền lọt vào giữa vật)
    holes = ndi.binary_fill_holes(fg) & ~fg
    hl, hn = ndi.label(holes)
    if hn:
        hs = ndi.sum(holes, hl, range(1, hn + 1))
        fg |= np.isin(hl, np.nonzero(hs < fg.sum() * 0.004)[0] + 1)
    return fg

def kmeans(X, k, it=12, seed=0):
    rng = np.random.default_rng(seed); C = X[rng.choice(len(X), min(k, len(X)), replace=False)]
    for _ in range(it):
        d = ((X[:, None] - C[None]) ** 2).sum(2); l = d.argmin(1)
        for j in range(len(C)):
            if (l == j).any(): C[j] = X[l == j].mean(0)
    return C, l

FAMILIES = [  # (tên, khoảng hue độ)
    ('red', 350, 12), ('orange', 12, 38), ('yellow', 38, 70), ('green', 70, 165),
    ('blue', 165, 255), ('purple', 255, 300), ('pink', 300, 350)]

def family(rgb):
    h, l, s = colorsys.rgb_to_hls(*(rgb / 255)); hd = h * 360
    if s < 0.16 or l > 0.93 or l < 0.08: return 'neutral'
    if 12 <= hd < 40 and l < 0.45: return 'brown'
    if (hd >= 330 or hd < 12) and l > 0.68: return 'pink'
    for name, a, b in FAMILIES:
        if (a <= hd < b) if a < b else (hd >= a or hd < b): return name
    return 'neutral'

def three_tone(cols):
    """cols: Nx3 các màu của một họ -> map mỗi màu về 1 trong 3 tông (theo độ sáng)."""
    L = np.array([colorsys.rgb_to_hls(*(c / 255))[1] for c in cols])
    if len(cols) <= 3:
        order = np.argsort(L); out = {}
        for i, j in enumerate(order): out[j] = cols[j]
        return [out[i] for i in range(len(cols))]
    q = np.quantile(L, [1/3, 2/3]); bins = np.digitize(L, q)
    tones = [cols[bins == b].mean(0) if (bins == b).any() else None for b in range(3)]
    return [tones[b] for b in bins]

def to_icon(a, fg, size=64, margin=3, mirror='auto'):
    ys, xs = np.nonzero(fg); y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
    side = max(y1 - y0, x1 - x0); cy, cx = (y0 + y1) / 2, (x0 + x1) / 2
    inner = size - 2 * margin; cell = side / inner
    out = np.zeros((size, size, 3)); alpha = np.zeros((size, size), bool)
    # gom màu toàn ảnh trước
    X = a[fg].reshape(-1, 3); sub = X[:: max(1, len(X) // 6000)]
    C, _ = kmeans(sub, 18)
    lab_full = np.full(fg.shape, -1)
    d = ((a[fg][:, None] - C[None]) ** 2).sum(2); lab_full[fg] = d.argmin(1)
    for gy in range(inner):
        for gx in range(inner):
            sy0 = int(cy - side / 2 + gy * cell); sx0 = int(cx - side / 2 + gx * cell)
            sy1 = max(sy0 + 1, int(cy - side / 2 + (gy + 1) * cell)); sx1 = max(sx0 + 1, int(cx - side / 2 + (gx + 1) * cell))
            sy0, sx0 = max(sy0, 0), max(sx0, 0); blk = lab_full[sy0:sy1, sx0:sx1]
            if blk.size == 0: continue
            if (blk >= 0).mean() < 0.5: continue
            v = blk[blk >= 0]; c = np.bincount(v).argmax()
            out[gy + margin, gx + margin] = C[c]; alpha[gy + margin, gx + margin] = True
    # 3 sắc độ theo họ màu
    used = np.unique(out[alpha].round().astype(int), axis=0).astype(float)
    fams = {}
    for c in used: fams.setdefault(family(c), []).append(c)
    remap = {}
    for f, cols in fams.items():
        cols = np.array(cols)
        if f == 'neutral':   # trung tính: tách trắng / xám / tối riêng
            L = cols.mean(1); groups = [cols[L > 225], cols[(L > 90) & (L <= 225)], cols[L <= 90]]
            for g in groups:
                if len(g):
                    t = three_tone(g) if len(g) > 3 else list(g)
                    for c, tc in zip(g, t): remap[tuple(c.astype(int))] = tc
        else:
            for c, tc in zip(cols, three_tone(cols)): remap[tuple(c.astype(int))] = tc
    for y, x in zip(*np.nonzero(alpha)):
        out[y, x] = remap.get(tuple(out[y, x].round().astype(int)), out[y, x])
    # đối xứng
    if mirror != 'off':
        L = alpha[:, : size // 2]; R = alpha[:, size - size // 2:][:, ::-1]
        sym = (L == R).mean()
        both = L & R
        cdiff = np.abs(out[:, : size // 2][both] - out[:, size - size // 2:][:, ::-1][both]).mean() if both.any() else 99
        if mirror == 'on' or (sym > 0.94 and cdiff < 14):
            alpha[:, size - size // 2:] = alpha[:, : size // 2][:, ::-1]
            out[:, size - size // 2:] = out[:, : size // 2][:, ::-1]
    # dọn pixel lẻ
    lab, n = ndi.label(alpha)
    if n > 1:
        s = ndi.sum(alpha, lab, range(1, n + 1)); alpha = np.isin(lab, np.nonzero(s >= 4)[0] + 1)
    # viền ngoài cùng tông
    ring = ndi.binary_dilation(alpha) & ~alpha
    img = np.zeros((size, size, 4), np.uint8)
    img[alpha, :3] = out[alpha].clip(0, 255); img[alpha, 3] = 255
    near = ndi.grey_dilation(np.where(alpha[..., None], out, 0), size=(3, 3, 1))
    for y, x in zip(*np.nonzero(ring)):
        c = near[y, x] / 255; h, l, s = colorsys.rgb_to_hls(*c)
        dh = ((0.78 - h + .5) % 1) - .5          # ngả về tím mận
        r, g, b = colorsys.hls_to_rgb((h + dh * .4) % 1, max(.14, min(.26, l * .3)), min(.6, s * .5 + .2))
        img[y, x] = (int(r * 255), int(g * 255), int(b * 255), 255)
    return Image.fromarray(img, 'RGBA')

def main():
    ap = argparse.ArgumentParser(); ap.add_argument('raw'); ap.add_argument('out')
    ap.add_argument('--size', type=int, default=64); ap.add_argument('--mirror', default='auto')
    ap.add_argument('--tol', type=float, default=26); ap.add_argument('--sheet')
    a = ap.parse_args(); out = Path(a.out); out.mkdir(parents=True, exist_ok=True); res = {}
    for p in sorted(Path(a.raw).glob('*.png')):
        arr = load(p); fg = remove_bg(arr, a.tol)
        ic = to_icon(arr, fg, a.size, mirror=a.mirror); ic.save(out / p.name); res[p.stem] = ic
        print('ok', p.name)
    if a.sheet and res:
        from PIL import ImageDraw
        n = a.size * 4; W = len(res) * (n + 20) + 20
        sh = Image.new('RGB', (W, n + 50 + a.size + 20), (255, 246, 233)); d = ImageDraw.Draw(sh)
        for i, (k, ic) in enumerate(res.items()):
            x = 20 + i * (n + 20); b = ic.resize((n, n), Image.NEAREST); sh.paste(b, (x, 20), b)
            d.text((x, n + 26), f'#{i+1} {k}', fill=(59, 42, 63)); sh.paste(ic, (x, n + 44), ic)
        sh.save(a.sheet)

if __name__ == '__main__': main()
