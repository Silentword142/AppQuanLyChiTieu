import sys, os, json
from PIL import Image, ImageDraw
import warm16
from warm16 import render
warm16.set_style(sys.argv[2] if len(sys.argv) > 2 else 'warm')
from icons_a import A; from icons_b import B; from icons_c import C; from icons_d import D
from icons_warm_fix import F, SYM
ART = {**A, **B, **C, **D, **F}; ORDER = json.load(open('order.json'))
out = sys.argv[1]
for s in ('16', '64', '128'): os.makedirs(f'{out}/{s}', exist_ok=True)
items = []
for i, n in enumerate(ORDER, 1):
    im = render(ART[n], n, sym=n in SYM); im.save(f'{out}/16/{n}.png')
    for s in (64, 128): im.resize((s, s), Image.NEAREST).save(f'{out}/{s}/{n}.png')
    items.append((f'#{i:03d} {n}', im))
cols, cell, pad = 12, 80, 12
sh = Image.new('RGB', (cols * (cell + pad) + pad, ((len(items) + cols - 1) // cols) * (cell + pad + 14) + pad), (236, 225, 208)); d = ImageDraw.Draw(sh)
for k, (lab, im) in enumerate(items):
    x = pad + (k % cols) * (cell + pad); y = pad + (k // cols) * (cell + pad + 14)
    b = im.resize((cell, cell), Image.NEAREST); sh.paste(b, (x, y), b); d.text((x, y + cell + 1), lab, fill=(90, 70, 60))
sh.save(f'{out}/_sheet.png')
