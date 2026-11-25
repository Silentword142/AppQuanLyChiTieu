import sys, importlib, types
sys.path.insert(0, '../pixeldraw')
import flat32
sys.modules['px64'] = flat32          # dùng lại bản vẽ khối của bộ 64 cũ, render kiểu phẳng 32px
src = importlib.import_module('icons64_a')
from px16 import sheet, BG
items = [(k, f().render()) for k, f in src.I.items()]
import px16; px16.N = 32
sheet(items, sys.argv[1], scale=4, cols=6)
