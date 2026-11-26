"""Đợt 1: icon 6–60 (trừ các hình đã có ở icons_a)."""
from svgkit import SVG, rect, circ, ell, poly, path, OUT
I = {}
def icon(f): I[f.__name__.replace('_', '-')] = f; return f
W = '#FFFFFF'; G = '#A79FC0'

@icon
def activity():
    s = SVG(); s.shape(rect(4, 8, 56, 48, 6), 'white', band=2)
    s.tube('M10 34H20L25 20L32 46L38 26L42 34H54', 'pink', 2.5); return s

@icon
def alert():
    s = SVG(); s.shape(path('M32 6L60 54H4Z'), 'yellow', hl=(28, 16, 2, 4))
    s.shape(rect(29, 22, 6, 18, 3), 'dark', band=0); s.shape(circ(32, 46, 3.5), 'dark', band=0); return s

@icon
def alert_circle():
    s = SVG(); s.shape(circ(32, 32, 26), 'red', hl=(16, 16, 3, 5))
    s.shape(rect(28, 14, 8, 24, 4), 'white', band=2); s.shape(circ(32, 46, 4.5), 'white', band=1); return s

@icon
def ao_dai():
    s = SVG()
    s.shape(path('M24 8H40L44 14L56 22L52 30L44 26L44 58H20L20 26L12 30L8 22L20 14Z'), 'lilac', hl=(22, 16, 2, 8))
    s.shape(path('M28 8H36L32 18Z'), 'purple', band=0)
    s.line('M32 18V58', '#9F86E0', 2, 'butt'); s.line('M24 30q4 4 8 0q4-4 8 0', '#FFB7C9', 2.5); return s

@icon
def apple():
    s = SVG(); s.line('M32 18Q32 10 36 6', OUT, 3)
    s.shape(path('M44 6Q52 6 50 14Q42 14 44 6Z'), 'leaf', band=1)
    s.shape(path('M32 18C24 12 8 14 8 32C8 48 20 58 28 56C30 55 34 55 36 56C44 58 56 48 56 32C56 14 40 12 32 18Z'), 'red', hl=(14, 24, 3, 7)); return s

@icon
def baby():
    s = SVG(); s.shape(path('M14 58C14 46 22 42 32 42C42 42 50 46 50 58Z'), 'sky', band=3)
    s.shape(circ(32, 26, 16), 'skin', hl=(21, 16, 2, 4))
    s.line('M30 10q4-2 4 2', OUT, 2.5)
    s.raw(f'<rect x="24" y="25" width="3" height="4" fill="{OUT}"/><rect x="37" y="25" width="3" height="4" fill="{OUT}"/>')
    s.raw('<ellipse cx="22" cy="32" rx="3" ry="2" fill="#FF8FAB"/><ellipse cx="42" cy="32" rx="3" ry="2" fill="#FF8FAB"/>')
    s.shape(circ(32, 36, 3), 'pink', band=0); return s

@icon
def bag():
    s = SVG(); s.line('M22 22V16a10 10 0 0 1 20 0v6', OUT, 3)
    s.shape(path('M10 20H54L50 58H14Z'), 'pink', hl=(15, 25, 2, 10))
    s.shape(circ(22, 28, 2.5), 'dark', band=0); s.shape(circ(42, 28, 2.5), 'dark', band=0); return s

@icon
def banknote():
    s = SVG(); s.shape(rect(4, 14, 56, 36, 3), 'mint', band=3)
    s.shape(rect(9, 19, 46, 26, 2), 'green', band=0); s.shape(circ(32, 32, 9), 'mint', band=2)
    s.line('M32 26V38', '#2F8A5E', 3, 'butt'); s.shape(circ(15, 32, 2.5), 'mint', band=0); s.shape(circ(49, 32, 2.5), 'mint', band=0); return s

@icon
def basket():
    s = SVG(); s.line('M14 26Q32 0 50 26', OUT, 3)
    s.shape(ell(22, 24, 7, 6), 'red', band=2); s.shape(ell(36, 22, 7, 6), 'leaf', band=2); s.shape(ell(46, 25, 6, 5), 'orange', band=2)
    s.shape(path('M4 28H60L54 58H10Z'), 'tan', hl=(10, 32, 2, 6))
    for x in (18, 32, 46): s.line(f'M{x} 32V54', '#B98058', 2.5, 'butt')
    s.shape(rect(4, 26, 56, 7, 2), 'wood', band=2); return s

@icon
def bath():
    s = SVG(); s.line('M12 30V12a6 6 0 0 1 12 0', OUT, 3); s.shape(rect(20, 12, 8, 4, 1), 'grey', band=0)
    for x, y in ((40, 14), (48, 10), (52, 20)): s.shape(circ(x, y, 4), 'glass', band=1)
    s.shape(path('M4 30H60V40C60 50 52 54 44 54H20C12 54 4 50 4 40Z'), 'white', hl=(10, 34, 2, 8))
    s.shape(rect(4, 28, 56, 6, 2), 'sky', band=2)
    s.shape(rect(12, 52, 6, 6, 1), 'grey', band=0); s.shape(rect(46, 52, 6, 6, 1), 'grey', band=0); return s

@icon
def bed():
    s = SVG(); s.shape(rect(4, 14, 8, 44, 2), 'wood', band=2)
    s.shape(rect(10, 26, 16, 10, 4), 'white', band=2)
    s.shape(path('M10 36H56a4 4 0 0 1 4 4V50H10Z'), 'lilac', hl=(14, 39, 6, 2))
    s.shape(rect(24, 30, 36, 10, 4), 'purple', band=2)
    s.shape(rect(52, 48, 8, 10, 2), 'wood', band=2); return s

@icon
def beer():
    s = SVG(); s.shape(path('M44 24h6a6 6 0 0 1 6 6v8a6 6 0 0 1-6 6h-6v-6h5v-8h-5z'), 'yellow', band=2)
    s.shape(rect(10, 20, 36, 38, 4), 'yellow', hl=(15, 26, 3, 20))
    for x in (22, 34): s.line(f'M{x} 28V50', '#F5C451', 2.5, 'butt')
    s.shape(path('M8 22C6 14 14 8 20 12C24 6 34 6 38 12C44 8 52 14 48 22Z'), 'white', band=2); return s

@icon
def bell():
    s = SVG(); s.shape(circ(32, 8, 4), 'gold', band=1)
    s.shape(path('M32 10C20 10 16 20 16 32V44L10 50H54L48 44V32C48 20 44 10 32 10Z'), 'yellow', hl=(21, 18, 3, 10))
    s.shape(circ(32, 54, 5), 'gold', band=2); return s

@icon
def bell_off():
    s = bell(); s.line('M8 8L56 58', OUT, 9); s.line('M8 8L56 58', '#FF8FAB', 4); return s

@icon
def bell_ring():
    s = bell()
    for d in ('M8 22q-4 10 0 20', 'M56 22q4 10 0 20', 'M2 18q-4 14 0 28', 'M62 18q4 14 0 28'): s.line(d, OUT, 3)
    return s

@icon
def bike():
    s = SVG()
    for x in (14, 50): s.shape(circ(x, 42, 11), 'dark', band=0); s.shape(circ(x, 42, 7), 'white', band=0); s.shape(circ(x, 42, 2), 'grey', band=0)
    s.line('M14 42L24 24H44L50 42M24 24L32 42L44 24M30 18H38M44 24L42 16H48', OUT, 6)
    s.line('M14 42L24 24H44L50 42M24 24L32 42L44 24', '#6AA8F0', 2.5); return s

@icon
def book():
    s = SVG(); s.shape(rect(12, 50, 44, 8, 2), 'white', band=2)
    s.shape(rect(10, 6, 44, 48, 3), 'lilac', hl=(16, 10, 2, 16))
    s.shape(rect(10, 6, 8, 48), 'purple', band=0)
    s.shape(rect(24, 14, 24, 10, 1), 'white', band=0); s.line('M28 19H44', G, 2, 'butt'); return s

@icon
def book_open():
    s = SVG(); s.shape(path('M4 14C12 10 24 10 32 16C40 10 52 10 60 14V54C52 50 40 50 32 56C24 50 12 50 4 54Z'), 'white', band=3)
    s.line('M32 16V56', OUT, 2)
    for y in (22, 30, 38): s.line(f'M10 {y}Q18 {y-3} 26 {y}M38 {y}Q46 {y-3} 54 {y}', G, 2)
    s.shape(rect(4, 54, 56, 4, 1), 'lilac', band=0); return s

@icon
def briefcase():
    s = SVG(); s.line('M24 16V12a4 4 0 0 1 4-4h8a4 4 0 0 1 4 4v4', OUT, 3)
    s.shape(rect(4, 16, 56, 40, 5), 'wood', hl=(9, 21, 2, 10))
    s.line('M4 32H60', '#B98058', 2.5, 'butt'); s.shape(rect(27, 28, 10, 9, 2), 'gold', band=2); return s

@icon
def building():
    s = SVG(); s.shape(rect(12, 6, 40, 52, 2), 'sky', hl=(16, 10, 2, 10))
    for y in (12, 22, 32):
        for x in (18, 29, 40): s.shape(rect(x, y, 6, 6), 'glass', band=0)
    s.shape(rect(26, 44, 12, 14), 'blue', band=2); s.shape(rect(6, 56, 52, 4, 1), 'grey', band=0); return s

@icon
def bulb():
    s = SVG()
    for d in ('M32 2V6', 'M8 22H12', 'M52 22H56', 'M14 8L17 11', 'M50 8L47 11'): s.line(d, '#F5C451', 3)
    s.shape(path('M32 10C20 10 14 18 14 26C14 34 22 38 22 46H42C42 38 50 34 50 26C50 18 44 10 32 10Z'), 'yellow', hl=(21, 17, 3, 9))
    s.line('M28 40V32L32 36L36 32V40', '#F5C451', 2)
    s.shape(rect(22, 46, 20, 6, 2), 'grey', band=2); s.shape(rect(25, 52, 14, 6, 3), 'steel', band=2); return s

@icon
def bus():
    s = SVG(); s.shape(rect(8, 6, 48, 46, 6), 'yellow', hl=(13, 11, 2, 10))
    s.shape(rect(13, 13, 38, 16, 2), 'glass', band=0); s.line('M32 13V29', OUT, 2)
    s.shape(rect(12, 38, 8, 5, 2), 'white', band=0); s.shape(rect(44, 38, 8, 5, 2), 'white', band=0)
    s.shape(rect(26, 38, 12, 4, 1), 'gold', band=0)
    s.shape(rect(12, 52, 10, 6, 2), 'dark', band=0); s.shape(rect(42, 52, 10, 6, 2), 'dark', band=0); return s

@icon
def cake():
    s = SVG()
    for x in (20, 32, 44): s.shape(rect(x - 2, 8, 4, 12, 1), 'sky', band=0); s.shape(ell(x, 6, 2.5, 3.5), 'orange', band=0)
    s.shape(rect(8, 20, 48, 16, 4), 'pink', band=3)
    s.shape(path('M8 26q6 6 12 0t12 0t12 0t12 0V22H8Z'), 'white', band=0)
    s.shape(rect(4, 36, 56, 20, 4), 'cream', hl=(9, 40, 2, 6))
    s.line('M4 46H60', '#FF8FAB', 3, 'butt'); return s

@icon
def calculator():
    s = SVG(); s.shape(rect(10, 4, 44, 56, 5), 'lilac', hl=(14, 8, 2, 10))
    s.shape(rect(16, 10, 32, 12, 2), 'mint', band=0); s.line('M36 16H44', '#2F8A5E', 3, 'butt')
    for y in (28, 38, 48):
        for x in (16, 28, 40): s.shape(rect(x, y, 8, 7, 1), 'white' if (x, y) != (40, 48) else 'pink', band=2)
    return s

@icon
def calendar():
    s = SVG(); s.shape(rect(6, 10, 52, 48, 4), 'white', hl=(10, 24, 2, 8))
    s.shape(rect(6, 10, 52, 12, 2), 'red', band=2)
    s.shape(rect(16, 4, 6, 12, 3), 'grey', band=1); s.shape(rect(42, 4, 6, 12, 3), 'grey', band=1)
    for y in (28, 38, 48):
        for x in (13, 24, 35, 46): s.raw(f'<rect x="{x}" y="{y}" width="6" height="5" fill="{"#FF8FAB" if (x, y) == (35, 38) else G}"/>')
    return s

@icon
def camera():
    s = SVG(); s.shape(path('M20 16L24 10H40L44 16Z'), 'grey', band=1)
    s.shape(rect(4, 16, 56, 40, 6), 'lilac', hl=(9, 21, 2, 10))
    s.shape(circ(32, 36, 14), 'dark', band=0); s.shape(circ(32, 36, 10), 'sky', hl=(26, 30, 3, 3))
    s.shape(rect(46, 21, 8, 5, 1), 'yellow', band=0); return s

@icon
def candy():
    s = SVG(); s.shape(poly([(4, 18), (18, 26), (18, 38), (4, 46)]), 'pink', band=2)
    s.shape(poly([(60, 18), (46, 26), (46, 38), (60, 46)]), 'pink', band=2)
    s.shape(ell(32, 32, 16, 12), 'rose', hl=(22, 24, 3, 3))
    for x in (26, 34): s.line(f'M{x} 21L{x + 6} 43', '#FFFFFF', 3, 'butt')
    return s

@icon
def carrot():
    s = SVG()
    for d in ('M40 20Q40 6 46 4Q46 14 42 20Z', 'M44 22Q54 12 60 14Q54 22 46 24Z', 'M40 20Q30 10 34 6Q42 12 42 20Z'): s.shape(path(d), 'leaf', band=1)
    s.shape(path('M38 18C44 20 46 26 42 30L10 58C6 60 4 58 6 54L34 22C35 20 36 18 38 18Z'), 'orange', hl=(30, 26, 2, 3))
    for d in ('M28 34l4 4', 'M20 42l4 4', 'M34 26l3 3'): s.line(d, '#E3884A', 2.5)
    return s

@icon
def cart():
    s = SVG(); s.line('M4 10H12L18 44H50', OUT, 4)
    s.shape(path('M14 16H58L52 38H18Z'), 'sky', hl=(20, 20, 2, 6))
    for x in (28, 40): s.line(f'M{x} 18V36', '#6AA8F0', 2.5, 'butt')
    s.shape(circ(22, 52, 5), 'dark', band=0); s.shape(circ(46, 52, 5), 'dark', band=0); return s

@icon
def cassette():
    s = SVG(); s.shape(rect(4, 12, 56, 40, 4), 'lilac', hl=(8, 16, 2, 8))
    s.shape(rect(12, 18, 40, 18, 2), 'white', band=0)
    for x in (22, 42): s.shape(circ(x, 27, 5), 'dark', band=0); s.shape(circ(x, 27, 2), 'white', band=0)
    s.shape(rect(26, 24, 12, 6), 'coffee', band=0); s.shape(path('M16 52L20 42H44L48 52Z'), 'purple', band=2); return s

@icon
def cat():
    s = SVG(); s.line('M50 50Q60 46 58 36', OUT, 6); s.line('M50 50Q60 46 58 36', '#FFCBA4', 2.5)
    s.shape(path('M12 26L14 8L24 18H40L50 8L52 26Z'), 'orange', band=2)
    s.shape(ell(32, 34, 21, 18), 'orange', hl=(17, 24, 3, 4))
    s.shape(poly([(17, 12), (19, 20), (22, 18)]), 'pink', band=0); s.shape(poly([(47, 12), (45, 20), (42, 18)]), 'pink', band=0)
    s.raw(f'<rect x="22" y="30" width="4" height="5" fill="{OUT}"/><rect x="38" y="30" width="4" height="5" fill="{OUT}"/>')
    s.shape(poly([(29, 38), (35, 38), (32, 41)]), 'rose', band=0); s.line('M32 41q-3 4-6 2M32 41q3 4 6 2', OUT, 2); return s

@icon
def chart_up():
    s = SVG(); s.shape(rect(4, 6, 56, 52, 4), 'white', band=2); s.line('M10 12V50H54', G, 2)
    s.tube('M14 44L26 32L34 38L50 18', 'mint', 3); s.shape(poly([(42, 16), (54, 12), (52, 24)]), 'mint', band=0); return s

@icon
def chat():
    s = SVG(); s.shape(path('M10 6H54a6 6 0 0 1 6 6V40a6 6 0 0 1-6 6H26L14 58V46H10a6 6 0 0 1-6-6V12a6 6 0 0 1 6-6Z'), 'sky', hl=(10, 12, 2, 8))
    for x in (20, 32, 44): s.shape(circ(x, 26, 3.5), 'white', band=0)
    return s

@icon
def check_circle():
    s = SVG(); s.shape(circ(32, 32, 26), 'mint', hl=(16, 16, 3, 5)); s.tube('M18 32L28 42L46 22', 'white', 4); return s

@icon
def chef():
    s = SVG(); s.shape(path('M16 30C6 30 6 14 16 16C16 6 30 4 32 12C36 4 48 6 48 16C58 14 58 30 48 30Z'), 'white', band=3)
    s.shape(rect(16, 28, 32, 8, 1), 'grey', band=2)
    s.shape(path('M16 36H48V44C48 54 40 58 32 58C24 58 16 54 16 44Z'), 'skin', band=3)
    s.raw(f'<rect x="24" y="42" width="3" height="4" fill="{OUT}"/><rect x="37" y="42" width="3" height="4" fill="{OUT}"/>')
    s.line('M28 51q4 3 8 0', OUT, 2); return s

@icon
def clock():
    s = SVG(); s.shape(circ(32, 32, 26), 'sky', band=3); s.shape(circ(32, 32, 20), 'white', hl=(19, 21, 3, 4))
    for d in ('M32 14V18', 'M32 46V50', 'M14 32H18', 'M46 32H50'): s.line(d, G, 2.5)
    s.line('M32 32V20M32 32L42 38', OUT, 3.5); s.shape(circ(32, 32, 2.5), 'pink', band=0); return s

@icon
def cloud():
    s = SVG(); s.shape(path('M16 50C6 50 4 36 14 34C12 22 26 16 32 24C36 12 54 14 52 30C62 30 62 50 50 50Z'), 'white', hl=(30, 22, 3, 3)); return s

@icon
def cloud_off():
    s = cloud(); s.line('M8 6L56 58', OUT, 9); s.line('M8 6L56 58', '#FF8FAB', 4); return s

@icon
def cloud_up():
    s = cloud(); s.shape(path('M32 26L42 36H36V46H28V36H22Z'), 'mint', band=2); return s

@icon
def compass():
    s = SVG(); s.shape(circ(32, 32, 26), 'gold', band=3); s.shape(circ(32, 32, 20), 'white', band=2)
    s.shape(poly([(32, 14), (37, 32), (27, 32)]), 'red', band=0); s.shape(poly([(32, 50), (37, 32), (27, 32)]), 'grey', band=0)
    s.shape(circ(32, 32, 3), 'dark', band=0); return s

@icon
def contact():
    s = SVG(); s.shape(rect(4, 10, 56, 44, 4), 'sky', hl=(8, 14, 2, 6))
    s.shape(rect(9, 16, 20, 30, 2), 'white', band=0); s.shape(circ(19, 26, 5), 'skin', band=0)
    s.shape(path('M11 44C11 36 27 36 27 44Z'), 'pink', band=0)
    for y, x1 in ((22, 52), (30, 48), (38, 52)): s.line(f'M34 {y}H{x1}', '#FFFFFF', 3, 'butt')
    return s

@icon
def credit_card():
    s = SVG(); s.shape(rect(4, 12, 56, 40, 5), 'lilac', hl=(8, 30, 2, 6))
    s.shape(rect(4, 20, 56, 7), 'dark', band=0); s.shape(rect(10, 34, 12, 9, 2), 'gold', band=2)
    s.line('M34 44H52', '#FFFFFF', 3, 'butt'); return s

@icon
def cross():
    s = SVG(); s.shape(path('M24 6H40V24H58V40H40V58H24V40H6V24H24Z'), 'white', band=3)
    s.shape(path('M28 10H36V28H54V36H36V54H28V36H10V28H28Z'), 'red', band=2); return s

@icon
def crown():
    s = SVG(); s.shape(path('M6 20L18 34L32 12L46 34L58 20L54 50H10Z'), 'yellow', hl=(12, 30, 2, 8))
    s.shape(rect(10, 46, 44, 8, 2), 'gold', band=2)
    for x, c in ((20, 'red'), (32, 'sky'), (44, 'mint')): s.shape(circ(x, 40, 3), c, band=0)
    for x, y in ((6, 18), (32, 10), (58, 18)): s.shape(circ(x, y, 3.5), 'gold', band=0)
    return s

@icon
def cup_soda():
    s = SVG(); s.line('M34 18L40 2H48', OUT, 6); s.line('M34 18L40 2H48', '#FF8FAB', 2.5)
    s.shape(path('M14 18H50L46 58H18Z'), 'white', band=3)
    for y in (26, 38, 50): s.shape(path(f'M{15 + (y - 18) * 0.1} {y}H{49 - (y - 18) * 0.1}L{49 - (y - 14) * 0.1} {y + 5}H{15 + (y - 14) * 0.1}Z'), 'pink', band=0, ol=False)
    s.shape(rect(10, 14, 44, 6, 2), 'red', band=2); return s
