import sys, os, importlib
from px16 import render, sheet
mods = sys.argv[2:] or ['icons_a']
items = []
for m in mods:
    D = getattr(importlib.import_module(m), m.split('_')[1].upper())
    for k, art in D.items():
        try: items.append((k, render(art, k)))
        except Exception as e: print('ERR', e)
sheet(items, sys.argv[1])
print(len(items))
