"""Cắt sheet icon (ảnh AI, lưới 6 cột) -> từng icon pixel nhỏ, nền trong suốt.
python3 slice.py SHEET.jpg "name1 name2 ..." OUTDIR [--size 32]
- tách nền: loang từ mép ảnh qua các ô gần màu nền (be), phần kín bên trong viền giữ nguyên
- dò kích thước + pha lưới pixel của ảnh AI, lấy trung vị mỗi khối -> pixel sạch
- lượng tử màu mỗi icon, căn giữa vào khung SIZE x SIZE"""
import sys, os, numpy as np
from PIL import Image, ImageFilter
from scipy import ndimage as ndi

def bg_mask(a, tol=34, holes=True):
    H, W, _ = a.shape
    border = np.concatenate([a[0], a[-1], a[:, 0], a[:, -1]])
    bg = np.median(border, 0)
    # nền có vignette nhẹ: so với màu nền mờ cục bộ
    near = np.sqrt(((a - bg) ** 2).sum(2)) < tol
    lab, _ = ndi.label(near)
    edge = set(np.unique(np.concatenate([lab[0], lab[-1], lab[:, 0], lab[:, -1]]))) - {0}
    m = np.isin(lab, list(edge))
    # lỗ kín bên trong (bánh xe, vòng kéo, sóng wifi) nếu rất sát màu nền và đủ to thì cũng là nền
    if not holes: return m, bg
    tight = np.sqrt(((a - bg) ** 2).sum(2)) < 22
    lab2, n2 = ndi.label(tight & ~m)
    if n2:
        sz = ndi.sum(np.ones_like(lab2), lab2, range(1, n2 + 1))
        big = [i + 1 for i, v in enumerate(sz) if v >= 120]
        m |= np.isin(lab2, big)
    return m, bg

def best_grid(rgb, mask, sizes=(4, 5, 6)):
    best = None
    for s in sizes:
        for oy in range(s):
            for ox in range(s):
                sub = rgb[oy:, ox:]; h, w = (sub.shape[0] // s) * s, (sub.shape[1] // s) * s
                b = sub[:h, :w].reshape(h // s, s, w // s, s, 3)
                v = b.var(axis=(1, 3)).sum(-1).mean()
                sc = v + 25 * s  # phạt khối nhỏ để không chọn s quá bé vô cớ
                if best is None or sc < best[0]: best = (sc, s, oy, ox)
    return best[1:]

def blockify(rgb, alpha, s, oy, ox):
    sub = rgb[oy:, ox:]; al = alpha[oy:, ox:]
    h, w = (sub.shape[0] // s) * s, (sub.shape[1] // s) * s
    b = sub[:h, :w].reshape(h // s, s, w // s, s, 3).transpose(0, 2, 1, 3, 4).reshape(h // s, w // s, s * s, 3)
    ab = al[:h, :w].reshape(h // s, s, w // s, s).transpose(0, 2, 1, 3).reshape(h // s, w // s, s * s)
    col = np.median(b, 2)
    A = (ab.mean(2) > 0.5)
    return col, A

HIRES = int(sys.argv[sys.argv.index('--hires') + 1]) if '--hires' in sys.argv else 0
HOLES = set('basket bag beer coffee lock luggage briefcase guitar bike scissors wifi headphones cart glasses utensils stethoscope lamp plug key compass umbrella wine cup-soda mic trophy sparkles party mai flag syringe tags tag activity heart-pulse more sun bell-ring phone-call trend-up trend-down chart dumbbell'.split())

def main(sheet, names, out, size=32):
    names = names.split()
    im = Image.open(sheet).convert('RGB'); a = np.array(im).astype(float)
    H, W, _ = a.shape
    bgm0, bg = bg_mask(a, holes=False); bgm1, _ = bg_mask(a)
    fg = ~bgm0
    fg = ndi.binary_opening(fg, iterations=1)
    lab, n = ndi.label(ndi.binary_dilation(fg, iterations=6))
    cols = 6; rows = (len(names) + cols - 1) // cols
    cw, ch = W / cols, H / rows
    cells = {}
    for i, sl in enumerate(ndi.find_objects(lab), 1):
        comp = (lab[sl] == i) & fg[sl]
        if comp.sum() < 40: continue
        ys, xs = np.nonzero(comp); cy = ys.mean() + sl[0].start; cx = xs.mean() + sl[1].start
        k = int(cy // ch) * cols + int(cx // cw)
        cells.setdefault(k, np.zeros((H, W), bool))[sl] |= comp
    os.makedirs(out, exist_ok=True); res = {}
    for k, nm in enumerate(names):
        if k not in cells: print('MISSING', nm); continue
        m = cells[k] & (~bgm1 if nm in HOLES else True); ys, xs = np.nonzero(m)
        y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
        rgb = a[y0:y1, x0:x1]; al = m[y0:y1, x0:x1].astype(float)
        if HIRES:  # giữ nguyên nét ảnh gốc: chỉ tách nền, căn vuông, thu về HIRES px
            mk = ndi.binary_fill_holes(m[y0:y1, x0:x1]) if nm not in HOLES else m[y0:y1, x0:x1]
            mk = ndi.binary_erosion(mk, iterations=1) | (mk & (np.sqrt(((rgb - bg) ** 2).sum(2)) > 60))
            soft = np.array(Image.fromarray((mk * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(0.6)))
            rgba = np.dstack([rgb.astype(np.uint8), soft]); h, w = mk.shape; S = max(h, w) + 8
            sq = Image.new('RGBA', (S, S), (0, 0, 0, 0)); sq.paste(Image.fromarray(rgba, 'RGBA'), ((S - w) // 2, (S - h) // 2))
            sq = sq.resize((HIRES, HIRES), Image.LANCZOS)
            sq.quantize(256, method=Image.Quantize.FASTOCTREE).save(f'{out}/{nm}.png', optimize=True); res[nm] = S; continue
        s, oy, ox = best_grid(rgb, al)
        col, A = blockify(rgb, al, s, oy, ox)
        h, w = A.shape
        if max(h, w) > size - 2:  # quá to thì thu nhỏ bằng nearest
            f = (size - 2) / max(h, w); nh, nw = max(1, round(h * f)), max(1, round(w * f))
            col = np.array(Image.fromarray(col.astype(np.uint8)).resize((nw, nh), Image.NEAREST)).astype(float)
            A = np.array(Image.fromarray((A * 255).astype(np.uint8)).resize((nw, nh), Image.NEAREST)) > 127
            h, w = A.shape
        # bỏ viền be lem ở mép (pixel rìa sát màu nền)
        for _ in range(2):
            edge = A & ~ndi.binary_erosion(A, border_value=0)
            fr = edge & (np.sqrt(((col - bg) ** 2).sum(2)) < 48)
            if not fr.any(): break
            A = A & ~fr
        # lượng tử màu
        pim = Image.fromarray(col.astype(np.uint8)).quantize(14, method=Image.Quantize.MEDIANCUT).convert('RGB')
        q = np.array(pim)
        canvas = np.zeros((size, size, 4), np.uint8)
        py, px = (size - h) // 2, (size - w) // 2
        canvas[py:py + h, px:px + w, :3] = q; canvas[py:py + h, px:px + w, 3] = A * 255
        Image.fromarray(canvas).save(f'{out}/{nm}.png'); res[nm] = (s, w, h)
    print(res)

if __name__ == '__main__':
    sz = int(sys.argv[sys.argv.index('--size') + 1]) if '--size' in sys.argv else 32
    main(sys.argv[1], sys.argv[2], sys.argv[3], sz)
