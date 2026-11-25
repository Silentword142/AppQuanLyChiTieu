"""Cắt 5 sheet Banana Pro (art/banana/g1..g5.jpg) -> art/banana/32/*.png, rồi --apply nạp vào web/icons.js IMG32."""
import sys, json, re, base64, subprocess, shutil
from pathlib import Path
HERE = Path(__file__).parent; ROOT = HERE.parent.parent; SRC = ROOT / 'art' / 'banana'; OUT = SRC / '32'
G = json.load(open(HERE / 'groups.json'))
for i, g in enumerate(G, 1):
    subprocess.run([sys.executable, str(HERE / 'slice.py'), str(SRC / f'g{i}.jpg'), g, str(OUT)], check=True)
ALIAS = {'chart-up': 'trend-up', 'key-house': 'key'}
for k, v in ALIAS.items(): shutil.copy(OUT / f'{v}.png', OUT / f'{k}.png')
if '--apply' in sys.argv:
    js = ROOT / 'web' / 'icons.js'; text = js.read_text(encoding='utf-8')
    m = re.search(r'(var IMG32\s*=\s*\{)([\s\S]*?)(\};)', text)
    entries = dict(re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(2))); c = 0
    for f in sorted(OUT.glob('*.png')):
        entries[f.stem] = base64.b64encode(f.read_bytes()).decode('ascii'); c += 1
    body = ',\n'.join(f'    "{k}": "{v}"' for k, v in sorted(entries.items()))
    js.write_text(text[:m.start()] + m.group(1) + '\n' + body + '\n  ' + m.group(3) + text[m.end():], encoding='utf-8')
    print('đã nạp', c, 'icon vào web/icons.js')
