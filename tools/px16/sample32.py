import sys, importlib
import px16
mod = importlib.import_module(sys.argv[2] if len(sys.argv) > 2 else 'icons32_a')
items = [(k, f().render()) for k, f in mod.I.items()]
px16.N = 32; px16.sheet(items, sys.argv[1], scale=4, cols=6)
