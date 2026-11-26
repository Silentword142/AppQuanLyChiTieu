"""Đợt 3: phone .. zap"""
import math
from svgkit import SVG, rect, circ, ell, poly, path, OUT
I = {}
def icon(f): I[f.__name__.replace('_', '-')] = f; return f
W = '#FFFFFF'; G = '#A79FC0'
def star_(cx, cy, R, r, n=5, a0=-90):
    return [(round(cx + (R if i % 2 == 0 else r) * math.cos(math.radians(a0 + i * 180 / n)), 1), round(cy + (R if i % 2 == 0 else r) * math.sin(math.radians(a0 + i * 180 / n)), 1)) for i in range(2 * n)]

@icon
def phone():
    s = SVG(); s.shape(rect(16, 4, 32, 56, 6), 'lilac', hl=(19, 8, 2, 10))
    s.shape(rect(20, 12, 24, 36, 1), 'sky', hl=(22, 14, 6, 2)); s.shape(rect(28, 52, 8, 3, 1), 'dark', band=0); return s

@icon
def phone_call():
    s = SVG(); s.shape(path('M12 6L22 4L28 18L21 24C24 32 32 40 40 43L46 36L60 42L58 52C56 58 48 60 40 56C24 48 16 40 8 24C4 16 6 8 12 6Z'), 'mint', hl=(10, 12, 2, 6))
    for d in ('M36 8a20 20 0 0 1 20 20', 'M36 16a12 12 0 0 1 12 12'): s.line(d, OUT, 3)
    return s

@icon
def pie():
    s = SVG(); s.shape(circ(30, 34, 24), 'pink', hl=(14, 20, 3, 5))
    s.shape(path('M34 6A24 24 0 0 1 58 30H34Z'), 'yellow', band=2); s.line('M30 34L30 10M30 34L50 46', OUT, 2.5); return s

@icon
def pill():
    s = SVG(); s.shape(path('M24 52a10 10 0 0 1-14-14L22 26L38 42Z'), 'white', band=2)
    s.shape(path('M22 26L36 12a10 10 0 0 1 14 14L38 42Z'), 'red', hl=(38, 16, 2, 4))
    s.shape(circ(48, 48, 10), 'sky', band=2); s.line('M42 54L54 42', '#6AA8F0', 2.5); return s

@icon
def pizza():
    s = SVG(); s.shape(path('M6 12Q32 0 58 12L32 60Z'), 'yellow', hl=(16, 14, 2, 6))
    s.shape(path('M6 12Q32 0 58 12L55 18Q32 8 9 18Z'), 'bread', band=2)
    for x, y in ((24, 22), (40, 24), (32, 38)): s.shape(circ(x, y, 4.5), 'red', band=1)
    s.shape(circ(30, 28, 2), 'leaf', band=0); return s

@icon
def plane():
    s = SVG(); s.shape(path('M28 60L32 50L36 60Z'), 'blue', band=0)
    s.shape(path('M32 4C36 4 37 10 37 16V24L60 38V44L37 36V50L44 56V60L32 56L20 60V56L27 50V36L4 44V38L27 24V16C27 10 28 4 32 4Z'), 'sky', hl=(29, 10, 2, 8)); return s

@icon
def plug():
    s = SVG(); s.line('M32 46V52Q32 60 24 60H18', OUT, 6); s.line('M32 46V52Q32 60 24 60H18', '#8C83A6', 2.5)
    for x in (22, 38): s.shape(rect(x, 4, 5, 14, 1), 'gold', band=1)
    s.shape(path('M12 16H52V28C52 40 44 46 32 46C20 46 12 40 12 28Z'), 'yellow', hl=(16, 20, 2, 8)); return s

@icon
def printer():
    s = SVG(); s.shape(rect(16, 4, 32, 18, 1), 'white', band=2)
    s.shape(rect(4, 20, 56, 26, 5), 'lilac', hl=(8, 24, 2, 8)); s.shape(circ(50, 28, 2.5), 'mint', band=0)
    s.shape(rect(16, 36, 32, 22, 1), 'white', band=2)
    for y in (44, 50): s.line(f'M22 {y}H42', G, 2.5, 'butt')
    return s

@icon
def radio():
    s = SVG(); s.line('M42 16L54 4', OUT, 3)
    s.shape(rect(4, 16, 56, 40, 6), 'orange', hl=(8, 20, 2, 10))
    s.shape(circ(20, 36, 11), 'cream', band=2)
    for y in (24, 30, 36): s.line(f'M10 {y + 0}H30', '#FFFFFF', 0.01)
    for r in (6, 3): s.raw(f'<circle cx="20" cy="36" r="{r}" fill="none" stroke="#D9A47A" stroke-width="2"/>')
    s.shape(rect(36, 26, 18, 8, 1), 'white', band=0); s.line('M42 26V34', '#FF8FAB', 2, 'butt')
    s.shape(circ(40, 44, 3.5), 'dark', band=0); s.shape(circ(50, 44, 3.5), 'dark', band=0); return s

@icon
def rice():
    s = SVG(); s.shape(path('M10 30C10 14 54 14 54 30Z'), 'white', band=2)
    for x, y in ((22, 22), (30, 18), (38, 22), (44, 26), (16, 27)): s.raw(f'<ellipse cx="{x}" cy="{y}" rx="2.5" ry="1.5" fill="#E3DDF0"/>')
    s.line('M40 4L30 20M48 6L36 22', OUT, 3)
    s.shape(path('M4 30H60C60 46 48 56 32 56C16 56 4 46 4 30Z'), 'sky', hl=(10, 34, 2, 8))
    s.line('M10 38H54', '#FFFFFF', 2, 'butt'); s.shape(rect(22, 54, 20, 5, 2), 'blue', band=1); return s

@icon
def road():
    s = SVG(); s.shape(rect(4, 6, 56, 52, 6), 'green', band=3)
    s.shape(path('M26 6H38L54 58H10Z'), 'grey', band=0); s.shape(path('M26 6H38L54 58H10Z'), 'steel', band=3)
    for y0, y1 in ((10, 18), (24, 34), (40, 54)): s.line(f'M32 {y0}V{y1}', '#FFE08A', 2.5 + (y0 / 20), 'butt')
    return s

@icon
def safe():
    s = SVG(); s.shape(rect(6, 6, 52, 48, 4), 'steel', hl=(10, 10, 2, 10))
    s.shape(rect(12, 12, 40, 36, 2), 'grey', band=2)
    s.shape(circ(30, 30, 10), 'white', band=2); s.line('M30 22V26M30 34V38M22 30H26M34 30H38', OUT, 2)
    s.shape(rect(44, 24, 4, 12, 1), 'dark', band=0)
    s.shape(rect(10, 54, 8, 6, 1), 'dark', band=0); s.shape(rect(46, 54, 8, 6, 1), 'dark', band=0); return s

@icon
def salary():
    s = SVG(); s.shape(rect(8, 4, 48, 56, 4), 'mint', hl=(12, 8, 2, 10))
    s.shape(circ(32, 28, 14), 'green', band=2)
    s.line('M37 21Q32 18 28 21Q25 25 32 28Q39 31 36 35Q32 38 27 35M32 16V40', '#FFFFFF', 3)
    s.line('M18 50H46', '#2F8A5E', 3, 'butt'); return s

@icon
def save():
    s = SVG(); s.shape(path('M6 10a4 4 0 0 1 4-4H48L58 16V54a4 4 0 0 1-4 4H10a4 4 0 0 1-4-4Z'), 'blue', hl=(10, 10, 2, 10))
    s.shape(rect(16, 6, 28, 16, 1), 'white', band=2); s.shape(rect(34, 9, 6, 10), 'dark', band=0)
    s.shape(rect(14, 34, 36, 24, 1), 'white', band=2)
    for y in (42, 50): s.line(f'M20 {y}H44', G, 2.5, 'butt')
    return s

@icon
def school():
    s = SVG(); s.line('M32 4V18', OUT, 3); s.shape(path('M33 4H46L42 8L46 12H33Z'), 'red', band=0)
    s.shape(rect(10, 30, 44, 28), 'cream', hl=(14, 34, 2, 8))
    s.shape(poly([(4, 32), (32, 16), (60, 32)]), 'red', band=2)
    s.shape(circ(32, 28, 4), 'white', band=0)
    s.shape(rect(26, 42, 12, 16), 'wood', band=2)
    for x in (14, 42): s.shape(rect(x, 38, 8, 8), 'sky', band=0)
    return s

@icon
def scissors():
    s = SVG()
    for d in ('M24 36L52 6', 'M40 36L12 6'): s.line(d, OUT, 8); s.line(d, '#E3DDF0', 3.5)
    for x in (18, 46):
        s.shape(path(f'M{x} 36a10 10 0 1 1 0 20a10 10 0 1 1 0-20zM{x} 41a5 5 0 1 0 0 10a5 5 0 1 0 0-10z', 'evenodd'), 'pink', band=2)
    s.shape(circ(32, 26, 3), 'dark', band=0); return s

@icon
def scooter():
    s = SVG(); s.line('M46 30L42 8H34', OUT, 6); s.line('M46 30L42 8H34', '#E3DDF0', 2.5)
    for x in (14, 50): s.shape(circ(x, 50, 8), 'dark', band=0); s.shape(circ(x, 50, 3), 'grey', band=0)
    s.shape(path('M4 46C4 34 12 28 22 28H30C32 34 34 40 40 40L44 26H50L54 46Z'), 'pink', hl=(9, 34, 3, 4))
    s.shape(rect(12, 22, 18, 7, 3), 'dark', band=0)
    s.shape(circ(44, 10, 3.5), 'yellow', band=0); return s

@icon
def settings():
    s = SVG(); pts = []
    for i in range(8):
        a0 = math.radians(i * 45)
        for da, r in ((-14, 21), (-9, 28), (9, 28), (14, 21)):
            a = a0 + math.radians(da); pts.append((round(32 + r * math.cos(a), 1), round(32 + r * math.sin(a), 1)))
    s.shape(poly(pts), 'lilac', hl=(16, 18, 3, 4))
    s.shape(path('M32 18a14 14 0 1 1 0 28a14 14 0 1 1 0-28zM32 25a7 7 0 1 0 0 14a7 7 0 1 0 0-14z', 'evenodd'), 'purple', band=2); return s

@icon
def shield():
    s = SVG(); s.shape(path('M32 4L56 12V30C56 46 44 56 32 60C20 56 8 46 8 30V12Z'), 'sky', hl=(14, 16, 3, 10))
    s.shape(path('M32 10L50 16V30C50 42 42 50 32 54Z'), 'blue', band=0); return s

@icon
def shield_check():
    s = SVG(); s.shape(path('M32 4L56 12V30C56 46 44 56 32 60C20 56 8 46 8 30V12Z'), 'mint', hl=(14, 16, 3, 10))
    s.tube('M20 32L28 40L44 24', 'white', 4); return s

@icon
def ship():
    s = SVG(); s.shape(rect(36, 6, 8, 16), 'red', band=2); s.shape(rect(36, 10, 8, 4), 'white', band=0)
    s.shape(rect(16, 20, 32, 16, 2), 'white', hl=(19, 23, 2, 4))
    for x in (24, 32, 40): s.shape(circ(x, 28, 2.5), 'sky', band=0)
    s.shape(path('M4 34H60L52 52H12Z'), 'navy', hl=(14, 38, 3, 2)); s.shape(rect(6, 34, 52, 5), 'red', band=0)
    s.line('M4 58q4-4 8 0t8 0t8 0t8 0t8 0t8 0t8 0', '#6AA8F0', 3); return s

@icon
def shirt():
    s = SVG(); s.shape(path('M22 6L32 12L42 6L60 16L54 30L46 26V58H18V26L10 30L4 16Z'), 'sky', hl=(22, 18, 2, 10))
    s.shape(path('M22 6L32 16L42 6L38 4H26Z'), 'blue', band=0)
    for y in (26, 36, 46): s.shape(circ(32, y, 2), 'white', band=0)
    return s

@icon
def shoe():
    s = SVG(); s.shape(path('M6 46V20C6 16 10 14 14 16C18 18 24 24 30 26L48 32C56 34 60 38 60 46Z'), 'red', hl=(10, 22, 2, 8))
    for x in (18, 24, 30): s.line(f'M{x} {20 + (x - 18) * 0.6}l6 -4', '#FFFFFF', 2.5)
    s.shape(rect(4, 46, 58, 10, 3), 'white', band=2); return s

@icon
def sofa():
    s = SVG(); s.shape(rect(10, 52, 6, 6), 'wood', band=0); s.shape(rect(48, 52, 6, 6), 'wood', band=0)
    s.shape(rect(12, 12, 40, 26, 6), 'teal', hl=(16, 16, 2, 8))
    s.shape(rect(12, 34, 40, 18, 2), 'teal', band=3)
    s.shape(rect(14, 34, 17, 9, 3), 'mint', band=2); s.shape(rect(33, 34, 17, 9, 3), 'mint', band=2)
    s.shape(rect(4, 26, 12, 28, 5), 'teal', band=2); s.shape(rect(48, 26, 12, 28, 5), 'teal', band=2)
    s.shape(rect(20, 18, 10, 10, 3), 'pink', band=2); return s

@icon
def sparkles():
    s = SVG(); s.shape(poly(star_(26, 36, 24, 6, 4)), 'yellow', hl=(24, 20, 2, 4))
    s.shape(poly(star_(48, 14, 11, 3, 4)), 'gold', band=1); s.shape(poly(star_(50, 50, 8, 2.5, 4)), 'pink', band=1); return s

@icon
def sprout():
    s = SVG(); s.line('M32 44V26', OUT, 6); s.line('M32 44V26', '#8FD06A', 2.5)
    s.shape(path('M32 28C30 16 18 10 8 14C10 26 22 30 32 28Z'), 'leaf', hl=(13, 16, 2, 3))
    s.shape(path('M32 24C34 12 46 6 56 10C54 22 42 26 32 24Z'), 'green', band=2)
    s.shape(path('M14 42H50L46 60H18Z'), 'wood', hl=(18, 46, 2, 6)); s.shape(rect(12, 40, 40, 7, 2), 'tan', band=2); return s

@icon
def star():
    s = SVG(); s.shape(poly(star_(32, 34, 29, 13)), 'yellow', hl=(22, 28, 2, 4))
    s.raw(f'<rect x="25" y="32" width="3" height="5" fill="{OUT}"/><rect x="36" y="32" width="3" height="5" fill="{OUT}"/>')
    s.raw('<ellipse cx="22" cy="41" rx="3" ry="2" fill="#FF8FAB"/><ellipse cx="42" cy="41" rx="3" ry="2" fill="#FF8FAB"/>'); return s

@icon
def stethoscope():
    s = SVG(); s.line('M14 6V26a14 14 0 0 0 28 0V6M28 40V48a10 10 0 0 0 20 0V44', OUT, 7)
    s.line('M14 6V26a14 14 0 0 0 28 0V6M28 40V48a10 10 0 0 0 20 0V44', '#8A8AD0', 3)
    for x in (14, 42): s.shape(rect(x - 4, 3, 8, 5, 2), 'grey', band=0)
    s.shape(circ(48, 38, 9), 'steel', band=2); s.shape(circ(48, 38, 4.5), 'sky', band=0); return s

@icon
def store():
    s = SVG(); s.shape(rect(8, 26, 48, 32), 'cream', hl=(12, 30, 2, 8))
    s.shape(rect(14, 34, 14, 12, 1), 'sky', band=0); s.shape(rect(34, 34, 14, 24), 'wood', band=2)
    s.shape(path('M4 12H60L58 28H6Z'), 'white', band=0)
    for i in range(0, 7, 2): s.shape(path(f'M{4 + i * 8} 12H{12 + i * 8}L{12 + i * 7.7} 28H{6 + i * 7.7}Z'), 'red', band=0, ol=False)
    s.shape(path('M4 12H60L58 28H6Z').replace('/>', '/>'), 'white', band=0)
    for i in range(0, 7, 2): s.raw(f'<path d="M{4 + i * 8} 12H{12 + i * 8}V28H{4 + i * 8}Z" fill="#FF9C9C"/>')
    s.raw(f'<path d="M4 12H60L58 28H6Z" fill="none" stroke="{OUT}" stroke-width="2"/>'); s.shape(rect(6, 6, 52, 7, 2), 'red', band=2); return s

@icon
def sun():
    s = SVG()
    for i in range(8):
        a = math.radians(i * 45); x1, y1 = 32 + 22 * math.cos(a), 32 + 22 * math.sin(a); x2, y2 = 32 + 29 * math.cos(a), 32 + 29 * math.sin(a)
        s.line(f'M{x1:.1f} {y1:.1f}L{x2:.1f} {y2:.1f}', OUT, 7); s.line(f'M{x1:.1f} {y1:.1f}L{x2:.1f} {y2:.1f}', '#F5C451', 3)
    s.shape(circ(32, 32, 16), 'yellow', hl=(22, 22, 3, 3)); return s

@icon
def syringe():
    s = SVG(); s.raw('<g transform="rotate(45 32 32)">')
    s.shape(rect(30, 0, 4, 10), 'grey', band=0); s.shape(rect(26, 8, 12, 5, 1), 'steel', band=0)
    s.shape(rect(24, 12, 16, 32, 2), 'glass', hl=(26, 14, 2, 8)); s.shape(rect(25, 26, 14, 17), 'mint', band=2, ol=False)
    s.shape(rect(18, 42, 28, 5, 2), 'steel', band=1); s.shape(rect(29, 46, 6, 10), 'grey', band=0); s.shape(rect(22, 55, 20, 5, 2), 'steel', band=1)
    s.raw('</g>'); return s

@icon
def tag():
    s = SVG(); s.shape(path('M34 4H56a4 4 0 0 1 4 4V30L30 60L4 34Z'), 'pink', hl=(36, 8, 4, 2))
    s.shape(circ(48, 16, 5), 'white', band=0); return s

@icon
def tags():
    s = SVG(); s.shape(path('M38 2H58a4 4 0 0 1 4 4V26L36 52L12 28Z'), 'lilac', band=3)
    s.shape(path('M28 10H48a4 4 0 0 1 4 4V34L26 60L2 36Z'), 'pink', hl=(30, 14, 4, 2)); s.shape(circ(42, 20, 4.5), 'white', band=0); return s

@icon
def taxi():
    s = SVG(); s.shape(rect(24, 4, 16, 7, 1), 'dark', band=0)
    s.shape(poly([(14, 28), (20, 12), (44, 12), (50, 28)]), 'yellow', band=2)
    s.shape(poly([(18, 27), (22, 16), (30, 16), (30, 27)]), 'glass', band=0); s.shape(poly([(34, 16), (42, 16), (46, 27), (34, 27)]), 'glass', band=0)
    s.shape(rect(4, 26, 56, 20, 7), 'yellow', hl=(9, 30, 2, 5))
    for x in range(14, 50, 8): s.raw(f'<rect x="{x}" y="34" width="4" height="4" fill="{OUT}"/>')
    for x in (17, 47): s.shape(circ(x, 47, 8), 'dark', band=0); s.shape(circ(x, 47, 3.5), 'grey', band=1)
    return s

@icon
def tent():
    s = SVG(); s.line('M32 4V10', OUT, 3); s.shape(path('M33 4H42L39 7L42 10H33Z'), 'red', band=0)
    s.shape(poly([(2, 56), (32, 8), (62, 56)]), 'orange', hl=(24, 22, 2, 6))
    s.shape(poly([(22, 56), (32, 30), (42, 56)]), 'dark', band=0); s.line('M2 58H62', OUT, 3); return s

@icon
def ticket():
    s = SVG(); s.shape(path('M4 16H60V26a6 6 0 0 0 0 12V48H4V38a6 6 0 0 0 0-12Z'), 'yellow', hl=(8, 20, 2, 4))
    s.line('M42 18V46', OUT, 2.5); s.raw(f'<path d="M42 18V46" stroke="#FFE08A" stroke-width="2" stroke-dasharray="3 3"/>')
    for y in (26, 34): s.line(f'M12 {y}H34', '#F5C451', 3, 'butt')
    return s

@icon
def tooth():
    s = SVG(); s.shape(path('M18 6C10 6 6 14 8 22C10 30 12 34 14 46C15 54 18 58 22 58C26 58 26 50 28 44C29 40 35 40 36 44C38 50 38 58 42 58C46 58 49 54 50 46C52 34 54 30 56 22C58 14 54 6 46 6C40 6 38 10 32 10C26 10 24 6 18 6Z'), 'white', hl=(14, 14, 3, 8))
    s.line('M24 16q8 4 16 0', G, 2); return s

@icon
def train():
    s = SVG(); s.shape(rect(10, 4, 44, 46, 8), 'red', hl=(14, 10, 2, 10))
    s.shape(rect(16, 12, 32, 16, 2), 'glass', band=0); s.line('M32 12V28', OUT, 2)
    s.shape(circ(20, 38, 3.5), 'yellow', band=0); s.shape(circ(44, 38, 3.5), 'yellow', band=0)
    s.line('M18 50L10 60M46 50L54 60M14 56H50', OUT, 3); return s

@icon
def trash():
    s = SVG(); s.shape(rect(24, 4, 16, 6, 2), 'grey', band=0); s.shape(rect(6, 10, 52, 8, 2), 'lilac', band=2)
    s.shape(path('M10 18H54L50 58H14Z'), 'lilac', hl=(14, 22, 2, 10))
    for x in (22, 32, 42): s.line(f'M{x} 24V52', '#9F86E0', 3, 'butt')
    return s

@icon
def tree():
    s = SVG(); s.shape(rect(28, 40, 8, 18), 'wood', band=2); s.shape(ell(32, 58, 16, 3), 'green', band=0)
    s.shape(path('M32 4C46 4 52 14 50 22C58 26 58 40 46 42H18C6 40 6 26 14 22C12 14 18 4 32 4Z'), 'green', hl=(18, 14, 3, 4))
    for x, y in ((22, 30), (40, 22), (36, 34)): s.shape(circ(x, y, 2.5), 'red', band=0)
    return s

@icon
def trend_down():
    s = SVG(); s.shape(rect(4, 6, 56, 52, 4), 'white', band=2); s.line('M10 12V50H54', G, 2)
    s.tube('M14 18L26 30L34 24L48 40', 'pink', 3); s.shape(poly([(52, 46), (40, 44), (50, 34)]), 'pink', band=0); return s

@icon
def trend_up():
    s = SVG(); s.shape(rect(4, 6, 56, 52, 4), 'white', band=2); s.line('M10 12V50H54', G, 2)
    s.tube('M14 44L26 32L34 38L48 22', 'mint', 3); s.shape(poly([(52, 16), (40, 18), (50, 28)]), 'mint', band=0); return s

@icon
def tv():
    s = SVG(); s.line('M22 4L32 14L42 4', OUT, 3)
    s.shape(rect(4, 14, 56, 40, 6), 'lilac', hl=(8, 18, 2, 10))
    s.shape(rect(9, 19, 36, 30, 4), 'sky', hl=(12, 22, 6, 2))
    s.shape(circ(52, 26, 3.5), 'dark', band=0); s.shape(circ(52, 36, 3.5), 'dark', band=0)
    s.line('M12 54V60M52 54V60', OUT, 4); return s

@icon
def umbrella():
    s = SVG(); s.line('M32 28V52a6 6 0 0 1-12 0', OUT, 7); s.line('M32 28V52a6 6 0 0 1-12 0', '#E3DDF0', 3)
    s.shape(path('M4 30C4 14 18 4 32 4C46 4 60 14 60 30C56 26 50 26 46 30C42 26 36 26 32 30C28 26 22 26 18 30C14 26 8 26 4 30Z'), 'pink', hl=(12, 16, 3, 5))
    s.line('M32 4Q24 14 18 30M32 4Q40 14 46 30', '#FF8FAB', 2); return s

@icon
def upload():
    s = SVG(); s.shape(path('M4 36H14V48H50V36H60V58H4Z'), 'navy', band=2)
    s.shape(path('M32 4L52 24H40V42H24V24H12Z'), 'mint', hl=(28, 12, 2, 4)); return s

@icon
def user():
    s = SVG(); s.shape(path('M8 60C8 46 18 40 32 40C46 40 56 46 56 60Z'), 'sky', hl=(14, 48, 3, 4))
    s.shape(circ(32, 22, 15), 'skin', hl=(22, 14, 2, 4))
    s.shape(path('M17 20C17 8 28 4 34 6C42 6 48 12 47 20C42 16 32 14 26 14C22 16 20 18 17 20Z'), 'coffee', band=2)
    s.raw(f'<rect x="25" y="22" width="3" height="4" fill="{OUT}"/><rect x="36" y="22" width="3" height="4" fill="{OUT}"/>')
    s.line('M28 30q4 3 8 0', OUT, 2); return s

@icon
def users():
    s = SVG()
    s.shape(path('M30 56C30 44 38 40 46 40C54 40 62 44 62 56Z'), 'pink', band=3); s.shape(circ(46, 26, 11), 'skin', band=2)
    s.shape(path('M35 24C35 16 44 12 50 16C54 18 57 22 57 26C52 22 44 20 35 24Z'), 'wood', band=0)
    s.shape(path('M2 60C2 46 12 40 24 40C36 40 46 46 46 60Z'), 'sky', hl=(8, 48, 3, 4)); s.shape(circ(24, 24, 13), 'skin', hl=(16, 16, 2, 3))
    s.shape(path('M11 22C11 12 22 8 28 10C34 12 37 16 37 22C32 18 22 16 11 22Z'), 'coffee', band=0)
    s.raw(f'<rect x="18" y="24" width="3" height="4" fill="{OUT}"/><rect x="28" y="24" width="3" height="4" fill="{OUT}"/>'); return s

@icon
def utensils():
    s = SVG(); s.shape(path('M10 4V22C10 28 14 30 16 30V60H24V30C26 30 30 28 30 22V4H26V20H22V4H18V20H14V4Z'), 'steel', hl=(16, 32, 2, 10))
    s.shape(path('M44 4C52 4 56 14 56 26C56 32 52 34 50 34V60H42V4Z'), 'grey', hl=(44, 8, 2, 10)); return s

@icon
def vinyl():
    s = SVG(); s.shape(circ(32, 32, 28), 'dark', band=0)
    for r in (22, 16): s.raw(f'<circle cx="32" cy="32" r="{r}" fill="none" stroke="#5A4E78" stroke-width="2"/>')
    s.shape(circ(32, 32, 10), 'pink', band=2); s.shape(circ(32, 32, 2.5), 'white', band=0)
    s.line('M14 18A24 24 0 0 1 22 10', '#8C83A6', 2.5); return s

@icon
def watch():
    s = SVG(); s.shape(rect(20, 2, 24, 14, 2), 'lilac', band=2); s.shape(rect(20, 48, 24, 14, 2), 'lilac', band=2)
    s.shape(circ(32, 32, 19), 'gold', band=3); s.shape(circ(32, 32, 14), 'white', hl=(22, 24, 2, 3))
    s.line('M32 32V23M32 32L38 36', OUT, 3); s.shape(rect(51, 29, 5, 6, 1), 'gold', band=0); return s

@icon
def wifi():
    s = SVG()
    for r, w in ((26, 7), (17, 7), (8, 7)):
        d = f'M{32 - r * 0.86:.1f} {44 - r * 0.5:.1f}A{r} {r} 0 0 1 {32 + r * 0.86:.1f} {44 - r * 0.5:.1f}'
        s.line(d, OUT, w + 4); s.line(d, '#6AA8F0', w - 1)
    s.shape(circ(32, 50, 5), 'blue', band=1); return s

@icon
def wine():
    s = SVG(); s.shape(rect(29, 34, 6, 18), 'glass', band=0); s.shape(rect(18, 52, 28, 6, 3), 'glass', band=2)
    s.shape(path('M16 4H48C48 22 42 34 32 34C22 34 16 22 16 4Z'), 'glass', band=0)
    s.shape(path('M17 16H47C46 28 40 34 32 34C24 34 18 28 17 16Z'), 'red', band=2); s.line('M22 8V14', '#FFFFFF', 2.5, 'butt'); return s

@icon
def wrench():
    s = SVG(); s.shape(path('M44 4C36 4 30 12 32 20L6 46C2 50 6 58 10 58C12 58 14 57 16 55L42 30C50 32 58 26 58 18L50 24L44 22L42 16L48 8C46 6 46 4 44 4Z'), 'steel', hl=(36, 10, 2, 4))
    s.shape(circ(12, 52, 3), 'grey', band=0); return s

@icon
def yoga():
    s = SVG(); s.shape(rect(2, 52, 60, 8, 3), 'lilac', band=2)
    s.shape(ell(32, 48, 22, 6), 'purple', band=2)
    s.line('M22 28L12 40L20 48M42 28L52 40L44 48', OUT, 7); s.line('M22 28L12 40L20 48M42 28L52 40L44 48', '#FFDCC0', 3)
    s.shape(path('M22 26H42L40 46H24Z'), 'pink', hl=(25, 29, 2, 6))
    s.shape(circ(32, 14, 9), 'skin', hl=(26, 8, 2, 3)); s.shape(circ(32, 4, 4), 'coffee', band=0)
    s.shape(path('M23 14C23 6 41 6 41 14C38 10 26 10 23 14Z'), 'coffee', band=0)
    s.line('M27 15h3M34 15h3', OUT, 2); return s

@icon
def zap():
    s = SVG(); s.shape(path('M38 2L10 36H28L22 62L54 24H36Z'), 'yellow', hl=(26, 16, 2, 6)); return s
