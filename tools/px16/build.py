"""Xuất toàn bộ icon 16px (và bản phóng 64, 128) + bảng tổng có đánh số.
Dùng: python build.py OUT_DIR"""
import sys, os, json
from PIL import Image
from px16 import render, sheet
from icons_a import A; from icons_b import B; from icons_c import C; from icons_d import D
ART = {**A, **B, **C, **D}
ORDER = json.load(open(os.path.join(os.path.dirname(__file__), 'order.json')))
out = sys.argv[1]
for s in ('16', '64', '128'): os.makedirs(f'{out}/{s}', exist_ok=True)
items = []; missing = [n for n in ORDER if n not in ART]
if missing: print('THIẾU', missing)
for i, n in enumerate(ORDER, 1):
    if n not in ART: continue
    im = render(ART[n], n); items.append((f'#{i:03d} {n}', im))
    im.save(f'{out}/16/{n}.png')
    for s in (64, 128): im.resize((s, s), Image.NEAREST).save(f'{out}/{s}/{n}.png')
sheet(items, f'{out}/_sheet.png', scale=5, cols=12)
print(len(items), 'icons')
