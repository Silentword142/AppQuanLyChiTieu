"""python build.py OUT_DIR [module]  -> OUT/svg, OUT/64, OUT/128, OUT/_sheet.png"""
import sys, os, subprocess, importlib
from PIL import Image, ImageDraw
from topx import topx
out = sys.argv[1]; mod = importlib.import_module(sys.argv[2] if len(sys.argv) > 2 else 'icons_a')
for d in ('svg', 'r512', '64', '128'): os.makedirs(f'{out}/{d}', exist_ok=True)
for k, f in mod.I.items(): open(f'{out}/svg/{k}.svg', 'w').write(f().svg())
subprocess.run(['node', os.path.join(os.path.dirname(os.path.abspath(__file__)), 'render.cjs'), f'{out}/svg', f'{out}/r512'], check=True)
items = []
for k in mod.I:
    im = topx(f'{out}/r512/{k}.png'); im.save(f'{out}/64/{k}.png'); im.resize((128, 128), Image.NEAREST).save(f'{out}/128/{k}.png')
    items.append((k, im))
cols = 6; cell = 128; pad = 16
sh = Image.new('RGB', (cols * (cell + pad) + pad, ((len(items) + cols - 1) // cols) * (cell + pad + 16) + pad), (255, 246, 233)); d = ImageDraw.Draw(sh)
for i, (k, im) in enumerate(items):
    x = pad + (i % cols) * (cell + pad); y = pad + (i // cols) * (cell + pad + 16)
    b = im.resize((cell, cell), Image.NEAREST); sh.paste(b, (x, y), b); d.text((x, y + cell + 2), k, fill=(59, 51, 85))
sh.save(f'{out}/_sheet.png')
