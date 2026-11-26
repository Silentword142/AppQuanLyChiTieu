"""Dựng cả bộ 173 icon theo thứ tự order.json, xuất OUT/{svg,64,128}, bảng tổng đánh số, và (tuỳ chọn) nạp vào web/icons.js.
python buildall.py OUT [--apply]"""
import sys, os, json, re, base64, subprocess, importlib
from pathlib import Path
from PIL import Image, ImageDraw
from topx import topx
HERE = Path(__file__).resolve().parent; ROOT = HERE.parents[1]
out = Path(sys.argv[1]); ART = {}
for m in ('icons_a', 'icons_b', 'icons_c', 'icons_d'): ART.update(importlib.import_module(m).I)
ORDER = json.load(open(HERE.parent / 'px16' / 'order.json')) + ['google']
miss = [n for n in ORDER if n not in ART]; print('thiếu:', miss)
for d in ('svg', 'r512', '64', '128'): (out / d).mkdir(parents=True, exist_ok=True)
for n in ORDER:
    if n in ART: (out / 'svg' / f'{n}.svg').write_text(ART[n]().svg())
subprocess.run(['node', str(HERE / 'render.cjs'), str(out / 'svg'), str(out / 'r512')], check=True)
items = []
for i, n in enumerate(ORDER, 1):
    if n not in ART: continue
    im = topx(out / 'r512' / f'{n}.png'); im.save(out / '64' / f'{n}.png'); im.resize((128, 128), Image.NEAREST).save(out / '128' / f'{n}.png')
    items.append((f'#{i:03d} {n}', im))
cols, cell, pad = 12, 96, 12
sh = Image.new('RGB', (cols * (cell + pad) + pad, ((len(items) + cols - 1) // cols) * (cell + pad + 14) + pad), (255, 246, 233)); d = ImageDraw.Draw(sh)
for k, (lab, im) in enumerate(items):
    x = pad + (k % cols) * (cell + pad); y = pad + (k // cols) * (cell + pad + 14)
    b = im.resize((cell, cell), Image.NEAREST); sh.paste(b, (x, y), b); d.text((x, y + cell + 1), lab, fill=(59, 51, 85))
sh.save(out / '_sheet.png'); print(len(items), 'icon')
if '--apply' in sys.argv:
    js = ROOT / 'web' / 'icons.js'; text = js.read_text(encoding='utf-8')
    m = re.search(r'(var IMG32\s*=\s*\{)([\s\S]*?)(\};)', text)
    entries = dict(re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(2))); c = 0
    for k in entries:
        f = out / '64' / f'{k}.png'
        if f.exists(): entries[k] = base64.b64encode(f.read_bytes()).decode('ascii'); c += 1
    body = ',\n'.join(f'    "{k}": "{v}"' for k, v in sorted(entries.items()))
    js.write_text(text[:m.start()] + m.group(1) + '\n' + body + '\n  ' + m.group(3) + text[m.end():], encoding='utf-8')
    print('đã nạp', c, 'icon vào web/icons.js')
