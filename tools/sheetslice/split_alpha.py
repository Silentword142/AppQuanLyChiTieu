"""Tách lẻ icon từ sheet ĐÃ tách nền (PNG trong suốt, lưới 6 cột), giữ nguyên độ phân giải.
python3 split_alpha.py SHEET.png "name1 name2 ..." OUTDIR"""
import sys, os, numpy as np
from PIL import Image
from scipy import ndimage as ndi
sheet, names, out = sys.argv[1], sys.argv[2].split(), sys.argv[3]
im = Image.open(sheet).convert('RGBA'); a = np.array(im); H, W = a.shape[:2]
fg = a[..., 3] > 8
lab, n = ndi.label(ndi.binary_dilation(fg, iterations=4))
cols = 6; rows = (len(names) + cols - 1) // cols; cw, ch = W / cols, H / rows
cells = {}
for i, sl in enumerate(ndi.find_objects(lab), 1):
    comp = (lab[sl] == i) & fg[sl]
    if comp.sum() < 30: continue
    ys, xs = np.nonzero(comp); cy = ys.mean() + sl[0].start; cx = xs.mean() + sl[1].start
    k = min(int(cy // ch), rows - 1) * cols + min(int(cx // cw), cols - 1)
    cells.setdefault(k, np.zeros((H, W), bool))[sl] |= comp
os.makedirs(out, exist_ok=True)
for k, nm in enumerate(names):
    if k not in cells: print('MISSING', nm); continue
    ys, xs = np.nonzero(cells[k]); y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
    crop = a[y0:y1, x0:x1].copy(); crop[~cells[k][y0:y1, x0:x1]] = 0
    h, w = crop.shape[:2]; S = max(h, w) + max(h, w) // 16
    sq = Image.new('RGBA', (S, S), (0, 0, 0, 0)); sq.paste(Image.fromarray(crop), ((S - w) // 2, (S - h) // 2))
    sq.save(f'{out}/{nm}.png', optimize=True)
print(sheet, 'ok', len(cells))
