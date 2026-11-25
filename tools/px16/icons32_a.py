"""Mẫu 32px bản 2: ít khối, khối to, không chồng chéo (toạ độ theo lưới 64)."""
from flat32 import Icon
I = {}
def icon(f): I[f.__name__.replace('_', '-')] = f; return f

@icon
def home():
    c = Icon()
    c.rect(14, 30, 50, 58, 'cream', kind='soft')
    c.poly([(4, 32), (32, 6), (60, 32)], 'red', kind='soft')
    c.rect(26, 40, 38, 58, 'wood', kind='flat')
    c.rect(41, 38, 47, 44, 'sky', kind='flat')
    return c

@icon
def coffee():
    c = Icon()
    c.ell(48, 36, 9, 9, 'white', kind='soft'); c.hole_ell(48, 36, 4, 4)
    c.rect(10, 20, 48, 54, 'white', r=6, kind='soft')
    c.rect(10, 20, 48, 27, 'coffee', kind='flat')
    c.rect(10, 34, 48, 40, 'pink', kind='flat', ol=False)
    return c

@icon
def pho():
    c = Icon()
    c.line([(30, 4), (36, 26)], 'wood', w=4, kind='flat'); c.line([(40, 4), (42, 26)], 'wood', w=4, kind='flat')
    c.ell(32, 28, 28, 6, 'broth', kind='flat')
    c.poly([(4, 30), (60, 30), (50, 52), (14, 52)], 'sky', kind='soft')
    c.rect(22, 52, 42, 58, 'blue', kind='soft')
    return c

@icon
def wallet():
    c = Icon()
    c.rect(14, 10, 46, 24, 'mint', kind='flat')
    c.rect(6, 20, 58, 56, 'wood', r=6, kind='soft')
    c.rect(38, 30, 60, 46, 'tan', r=4, kind='soft')
    c.ell(47, 38, 3.5, 3.5, 'gold', kind='round')
    return c

@icon
def piggy():
    c = Icon()
    c.poly([(16, 22), (20, 8), (30, 18)], 'rose', kind='soft')
    c.rect(12, 46, 22, 58, 'pink', kind='soft'); c.rect(38, 46, 48, 58, 'pink', kind='soft')
    c.ell(30, 34, 24, 18, 'pink', kind='round')
    c.ell(54, 34, 6, 8, 'rose', kind='soft')
    c.rect(38, 24, 42, 30, 'dark', kind='flat', ol=False)
    c.rect(22, 18, 34, 21, 'dark', kind='flat', ol=False)
    return c

@icon
def chart():
    c = Icon()
    c.rect(6, 50, 58, 56, 'lilac', kind='soft')
    c.rect(10, 34, 20, 50, 'mint', kind='soft'); c.rect(27, 24, 37, 50, 'sky', kind='soft'); c.rect(44, 12, 54, 50, 'pink', kind='soft')
    return c

@icon
def car():
    c = Icon()
    c.poly([(14, 28), (20, 12), (44, 12), (52, 28)], 'glass', kind='soft')
    c.rect(4, 26, 60, 46, 'red', r=6, kind='soft')
    c.rect(30, 12, 34, 28, 'red', kind='flat')
    for x in (17, 47): c.ell(x, 48, 8, 8, 'dark', kind='flat'); c.ell(x, 48, 3, 3, 'grey', kind='flat', ol=False)
    return c

@icon
def gift():
    c = Icon()
    c.ell(22, 14, 9, 7, 'gold', kind='soft'); c.ell(42, 14, 9, 7, 'gold', kind='soft')
    c.rect(9, 30, 55, 58, 'lilac', kind='soft')
    c.rect(5, 20, 59, 30, 'purple', kind='soft')
    c.rect(28, 20, 36, 58, 'yellow', kind='flat')
    return c

@icon
def heart():
    c = Icon()
    c.ell(20.5, 22, 14, 14, 'pink', kind='round'); c.ell(43.5, 22, 14, 14, 'pink', kind='round')
    c.poly([(7, 27), (57, 27), (32, 56)], 'pink', kind='round')
    m = (c.mat >= 0); c.shapes = []; c.mat[:] = -1; c._add(m, 'pink', 'round', True)
    return c

@icon
def receipt():
    c = Icon()
    pts = [(12, 4), (52, 4), (52, 58)]
    for i, x in enumerate(range(52, 11, -8)): pts.append((x, 58 if i % 2 == 0 else 53))
    pts += [(12, 58)]
    c.poly(pts, 'white', kind='soft')
    for y, x1 in ((14, 44), (24, 36), (34, 44)): c.rect(20, y, x1, y + 3, 'dark', kind='flat', ol=False)
    c.rect(20, 44, 44, 47, 'rose', kind='flat', ol=False)
    return c

@icon
def banh_mi():
    c = Icon()
    c.poly([(4, 44), (10, 28), (24, 18), (46, 16), (60, 22), (56, 36), (36, 46), (16, 50)], 'bread', kind='round')
    c.poly([(14, 38), (26, 28), (48, 24), (52, 28), (38, 36), (20, 42)], 'leaf', kind='flat')
    return c

@icon
def coins():
    c = Icon()
    for y in (50, 40, 30): c.rect(6, y - 2, 38, y + 7, 'yellow', r=3, kind='soft')
    c.ell(45, 20, 14, 14, 'gold', kind='soft')
    c.rect(42, 12, 48, 28, 'yellow', kind='flat', ol=False)
    return c
