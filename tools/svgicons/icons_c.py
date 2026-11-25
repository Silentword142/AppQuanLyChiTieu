"""Đợt 2: dice .. phin"""
from svgkit import SVG, rect, circ, ell, poly, path, OUT
I = {}
def icon(f): I[f.__name__.replace('_', '-')] = f; return f
W = '#FFFFFF'; G = '#A79FC0'
def dots(s, pts, r=3.5, c='dark'):
    for x, y in pts: s.shape(circ(x, y, r), c, band=0)

@icon
def dice():
    s = SVG(); s.shape(rect(8, 8, 48, 48, 8), 'white', hl=(13, 14, 2, 8))
    dots(s, [(20, 20), (44, 20), (32, 32), (20, 44), (44, 44)]); return s

@icon
def disco():
    s = SVG(); s.line('M32 2V12', OUT, 3)
    s.shape(circ(32, 36, 22), 'lilac', hl=(19, 25, 3, 4))
    for d in ('M10 30H54', 'M12 44H52', 'M24 15V57', 'M40 15V57'): s.line(d, '#9F86E0', 2, 'butt')
    for x, y in ((18, 22), (46, 38), (30, 50)): s.shape(rect(x, y, 5, 5), 'white', band=0)
    for x, y in ((4, 8), (58, 14)): s.line(f'M{x} {y - 4}V{y + 4}M{x - 4} {y}H{x + 4}', '#F5C451', 2)
    return s

@icon
def dog():
    s = SVG(); s.shape(ell(32, 34, 20, 19), 'tan', hl=(19, 22, 3, 4))
    s.shape(path('M10 16C4 18 4 34 10 40C14 36 16 26 16 18Z'), 'wood', band=2)
    s.shape(path('M54 16C60 18 60 34 54 40C50 36 48 26 48 18Z'), 'wood', band=2)
    s.raw(f'<rect x="23" y="29" width="4" height="5" fill="{OUT}"/><rect x="37" y="29" width="4" height="5" fill="{OUT}"/>')
    s.shape(ell(32, 42, 8, 6), 'cream', band=0); s.shape(ell(32, 39, 3.5, 2.5), 'dark', band=0)
    s.shape(path('M29 46H35V52a3 3 0 0 1-6 0Z'), 'pink', band=0); return s

@icon
def droplet():
    s = SVG(); s.shape(path('M32 4C32 4 12 28 12 40a20 20 0 0 0 40 0C52 28 32 4 32 4Z'), 'sky', hl=(19, 34, 3, 8)); return s

@icon
def dumbbell():
    s = SVG(); s.shape(rect(18, 28, 28, 8, 2), 'grey', band=2)
    for x in (6, 46): s.shape(rect(x, 14, 12, 36, 3), 'lilac', hl=(x + 3, 18, 2, 8))
    for x in (2, 58): s.shape(rect(x - 2 if x == 2 else x - 2, 22, 6, 20, 2), 'purple', band=1)
    return s

@icon
def egg():
    s = SVG(); s.shape(path('M32 8C14 8 4 22 6 36C8 52 22 58 34 56C50 54 60 44 58 30C56 16 46 8 32 8Z'), 'white', band=3)
    s.shape(circ(30, 34, 11), 'yellow', hl=(24, 28, 3, 3)); return s

@icon
def envelope_red():
    s = SVG(); s.shape(rect(10, 4, 44, 56, 4), 'red', hl=(14, 8, 2, 10))
    s.shape(path('M10 8L32 26L54 8'), 'red', band=0)
    s.line('M12 8L32 24L52 8', '#E8707E', 2.5)
    s.shape(circ(32, 30, 8), 'gold', band=2); s.line('M32 26V34', '#E0A23C', 2.5, 'butt'); return s

@icon
def fan():
    s = SVG(); s.shape(rect(26, 40, 12, 14), 'grey', band=2); s.shape(rect(16, 52, 32, 6, 2), 'steel', band=2)
    s.shape(circ(32, 26, 22), 'sky', band=3); s.shape(circ(32, 26, 18), 'white', band=0)
    for d in ('M32 26C28 14 36 8 40 12C42 18 36 22 32 26Z', 'M32 26C44 24 48 32 44 36C38 38 34 32 32 26Z', 'M32 26C26 36 18 34 18 28C20 22 28 22 32 26Z'): s.shape(path(d), 'blue', band=0)
    s.shape(circ(32, 26, 3.5), 'dark', band=0); return s

@icon
def file():
    s = SVG(); s.shape(path('M12 4H40L52 16V60H12Z'), 'white', hl=(16, 10, 2, 10))
    s.shape(path('M40 4V16H52Z'), 'grey', band=0)
    for y, x1 in ((26, 44), (34, 40), (42, 44), (50, 34)): s.line(f'M20 {y}H{x1}', G, 3, 'butt')
    return s

@icon
def film():
    s = SVG(); s.shape(rect(4, 10, 56, 44, 3), 'dark', band=0)
    for x in range(9, 58, 9):
        s.raw(f'<rect x="{x}" y="14" width="4" height="4" fill="#FFFFFF"/><rect x="{x}" y="46" width="4" height="4" fill="#FFFFFF"/>')
    s.shape(rect(10, 22, 20, 20, 1), 'sky', band=2); s.shape(rect(34, 22, 20, 20, 1), 'pink', band=2); return s

@icon
def fish():
    s = SVG(); s.shape(path('M44 32L60 18V46Z'), 'blue', band=2)
    s.shape(path('M4 32C12 16 30 12 46 32C30 52 12 48 4 32Z'), 'sky', hl=(14, 26, 3, 3))
    s.shape(circ(14, 30, 3), 'dark', band=0); s.line('M24 24q4 8 0 16M32 24q4 8 0 16', '#6AA8F0', 2); return s

@icon
def flag():
    s = SVG(); s.shape(rect(8, 4, 5, 56, 2), 'grey', band=1)
    s.shape(path('M13 8C24 2 34 14 46 8C50 6 54 8 56 10V36C46 42 34 30 24 36C20 38 16 38 13 36Z'), 'red', hl=(17, 12, 2, 6))
    s.shape(poly([(32, 14), (34, 20), (40, 20), (35, 24), (37, 30), (32, 26), (27, 30), (29, 24), (24, 20), (30, 20)]), 'yellow', band=0); return s

@icon
def flame():
    s = SVG(); s.shape(path('M32 4C34 16 50 22 50 40C50 52 42 60 32 60C22 60 14 52 14 40C14 30 20 24 22 18C26 24 28 26 30 28C28 18 30 10 32 4Z'), 'orange', hl=(20, 38, 2, 6))
    s.shape(path('M32 30C34 38 42 40 42 48C42 54 38 58 32 58C26 58 22 54 22 48C22 42 30 38 32 30Z'), 'yellow', band=2); return s

@icon
def flower():
    s = SVG(); s.line('M32 36V60', OUT, 6); s.line('M32 36V60', '#8FD06A', 2.5)
    s.shape(path('M32 50C28 40 18 42 16 46C22 52 28 52 32 50Z'), 'leaf', band=1)
    for a in range(5):
        import math
        x = 32 + 13 * math.cos(math.radians(a * 72 - 90)); y = 22 + 13 * math.sin(math.radians(a * 72 - 90))
        s.shape(circ(round(x, 1), round(y, 1), 9), 'pink', band=2)
    s.shape(circ(32, 22, 7), 'yellow', band=2); return s

@icon
def fuel():
    s = SVG(); s.line('M44 16L52 24V48a4 4 0 0 1-8 0V36H40', OUT, 3)
    s.shape(rect(10, 6, 30, 52, 4), 'red', hl=(14, 10, 2, 10))
    s.shape(rect(15, 12, 20, 14, 2), 'white', band=0); s.shape(rect(6, 54, 38, 6, 2), 'grey', band=0); return s

@icon
def gamepad():
    s = SVG(); s.shape(path('M16 16H48C56 16 60 24 60 34C60 46 56 52 50 52C44 52 42 44 36 44H28C22 44 20 52 14 52C8 52 4 46 4 34C4 24 8 16 16 16Z'), 'lilac', hl=(10, 22, 3, 6))
    s.line('M18 26V38M12 32H24', OUT, 4)
    s.shape(circ(44, 28, 3.5), 'pink', band=0); s.shape(circ(50, 35, 3.5), 'mint', band=0); return s

@icon
def gem():
    s = SVG(); s.shape(path('M16 10H48L60 24L32 56L4 24Z'), 'sky', band=3)
    s.shape(path('M16 10L24 24L32 10L40 24L48 10'), 'glass', band=0, ol=False)
    s.line('M4 24H60M24 24L32 56L40 24', '#6AA8F0', 2); s.shape(path('M16 10H48L60 24H4Z'), 'glass', band=0)
    s.line('M24 24L32 10L40 24M16 10L24 24M48 10L40 24', OUT, 2); return s

@icon
def glasses():
    s = SVG(); s.line('M4 26L10 20M60 26L54 20M26 34Q32 28 38 34', OUT, 3)
    for x in (16, 48): s.shape(circ(x, 36, 12), 'dark', band=0); s.shape(circ(x, 36, 9), 'glass', hl=(x - 6, 30, 3, 3))
    return s

@icon
def globe():
    s = SVG(); s.shape(circ(32, 32, 26), 'sky', hl=(16, 16, 3, 5))
    s.shape(path('M18 14C24 16 26 22 22 26C18 30 22 36 16 40C10 38 8 28 10 22Z'), 'leaf', band=2)
    s.shape(path('M38 10C46 12 52 18 54 26C48 28 44 24 40 26C36 22 40 16 38 10Z'), 'leaf', band=2)
    s.shape(path('M34 40C42 38 48 42 46 50C42 54 36 54 32 52C30 48 30 42 34 40Z'), 'leaf', band=2); return s

@icon
def google():
    s = SVG(); s.shape(circ(32, 32, 26), 'white', band=3)
    s.line('M43 22A14 14 0 1 0 46 34H32', OUT, 9); s.line('M43 22A14 14 0 1 0 46 34H32', '#6AA8F0', 5); return s

@icon
def graduation():
    s = SVG(); s.shape(path('M16 30V44C16 50 48 50 48 44V30Z'), 'purple', band=3)
    s.shape(poly([(32, 10), (62, 24), (32, 38), (2, 24)]), 'lilac', hl=(22, 20, 4, 2))
    s.line('M54 28V44', OUT, 3); s.shape(rect(50, 44, 8, 10, 2), 'gold', band=1); return s

@icon
def guitar():
    s = SVG(); s.shape(rect(44, 4, 12, 10, 2), 'wood', band=1); s.line('M48 12L32 30', OUT, 8); s.line('M48 12L32 30', '#D9A47A', 4)
    s.shape(path('M30 24C22 20 14 24 16 32C8 32 4 40 8 48C12 58 24 60 30 52C36 48 36 42 36 38C42 36 42 28 30 24Z'), 'orange', hl=(12, 40, 2, 6))
    s.shape(circ(24, 40, 4.5), 'dark', band=0); s.line('M18 50L28 40', '#E3884A', 3, 'butt'); return s

@icon
def hand_coins():
    s = SVG(); s.shape(circ(40, 14, 9), 'gold', band=2); s.line('M40 9V19', '#E0A23C', 2.5, 'butt')
    s.shape(circ(24, 22, 7), 'yellow', band=2)
    s.shape(path('M4 38H18C22 38 26 40 30 42H40C44 42 44 48 40 48H28H42L54 40C58 38 62 42 58 46L44 56H4Z'), 'skin', hl=(8, 42, 2, 4))
    s.shape(rect(2, 36, 8, 22, 1), 'sky', band=2); return s

@icon
def handshake():
    s = SVG(); s.shape(rect(2, 18, 10, 26, 2), 'sky', band=2); s.shape(rect(52, 18, 10, 26, 2), 'pink', band=2)
    s.shape(path('M12 22L24 18L36 24L44 22L52 24V42L44 50L36 52L22 46L12 40Z'), 'skin', hl=(16, 26, 3, 2))
    for d in ('M26 30L36 40', 'M30 26L42 36', 'M22 36L30 44'): s.line(d, '#D9A47A', 2.5)
    return s

@icon
def headphones():
    s = SVG(); s.line('M10 38V30a22 22 0 0 1 44 0v8', OUT, 8); s.line('M10 38V30a22 22 0 0 1 44 0v8', '#CDB8F6', 4)
    for x in (6, 44): s.shape(rect(x, 32, 14, 24, 5), 'purple', hl=(x + 3, 36, 2, 6))
    return s

@icon
def heart_pulse():
    s = SVG(); s.shape(path('M32 56L8 32A13 13 0 0 1 32 15A13 13 0 0 1 56 32Z'), 'pink', band=5, hl=(12, 22, 3, 6))
    s.tube('M8 34H20L25 24L32 44L38 30L42 34H56', 'white', 2.5); return s

@icon
def history():
    s = SVG(); s.line('M10 32A22 22 0 1 0 16 16', OUT, 8); s.line('M10 32A22 22 0 1 0 16 16', '#A9D8FF', 4)
    s.shape(poly([(8, 8), (22, 12), (12, 22)]), 'sky', band=0)
    s.shape(circ(32, 32, 15), 'white', band=2); s.line('M32 32V23M32 32L38 36', OUT, 3); return s

@icon
def home_heart():
    s = SVG(); s.shape(rect(12, 30, 40, 26), 'cream')
    s.shape(poly([(6, 32), (32, 6), (58, 32)]), 'red', hl=(24, 16, 2, 4))
    s.shape(path('M32 52L22 42A6 6 0 0 1 32 35A6 6 0 0 1 42 42Z'), 'pink', band=2); return s

@icon
def ice_cream():
    s = SVG(); s.shape(path('M18 30H46L32 60Z'), 'tan', band=3)
    s.line('M22 36L40 36M24 42H38M28 50H36', '#D9A47A', 2, 'butt')
    s.shape(circ(32, 18, 13), 'pink', hl=(24, 10, 3, 4)); s.shape(path('M16 30C16 24 22 24 24 26C26 22 38 22 40 26C42 24 48 24 48 30Z'), 'pink', band=2)
    s.shape(circ(34, 4, 3.5), 'red', band=0); return s

@icon
def image():
    s = SVG(); s.shape(rect(4, 8, 56, 48, 4), 'sky', band=3)
    s.shape(circ(44, 20, 5), 'yellow', band=1)
    s.shape(path('M4 48L20 28L32 42L40 34L60 50V52a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4Z'), 'mint', band=2); return s

@icon
def inbox():
    s = SVG(); s.shape(rect(14, 6, 36, 30, 2), 'white', band=2)
    for y in (14, 22): s.line(f'M20 {y}H44', G, 3, 'butt')
    s.shape(path('M4 34H20L24 42H40L44 34H60V54a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4Z'), 'lilac', hl=(8, 38, 2, 8)); return s

@icon
def info():
    s = SVG(); s.shape(circ(32, 32, 26), 'sky', hl=(16, 16, 3, 5))
    s.shape(circ(32, 18, 4.5), 'white', band=1); s.shape(rect(28, 26, 8, 22, 3), 'white', band=2); return s

@icon
def key():
    s = SVG(); s.shape(path('M28 28H58V36H52V44H46V36H28Z'), 'gold', band=2)
    s.shape(circ(18, 32, 14), 'yellow', hl=(9, 23, 3, 3)); s.shape(circ(18, 32, 5), 'cream', band=0); return s

@icon
def key_house():
    s = SVG(); s.shape(rect(12, 30, 40, 26), 'cream')
    s.shape(poly([(6, 32), (32, 6), (58, 32)]), 'red', hl=(24, 16, 2, 4))
    s.shape(path('M30 42H48V48H44V52H40V48H30Z'), 'gold', band=1); s.shape(circ(24, 45, 7), 'yellow', band=2); s.shape(circ(24, 45, 2.5), 'cream', band=0); return s

@icon
def lamp():
    s = SVG(); s.shape(rect(29, 30, 6, 22), 'grey', band=1); s.shape(rect(16, 52, 32, 6, 2), 'wood', band=2)
    s.shape(path('M20 6H44L54 30H10Z'), 'yellow', hl=(20, 12, 2, 10)); return s

@icon
def landmark():
    s = SVG(); s.shape(poly([(4, 20), (32, 4), (60, 20)]), 'lilac', hl=(28, 10, 3, 3))
    s.shape(rect(6, 20, 52, 6), 'purple', band=2)
    for x in (10, 22, 36, 48): s.shape(rect(x, 26, 6, 24), 'white', band=2)
    s.shape(rect(6, 50, 52, 8, 1), 'purple', band=2); return s

@icon
def laptop():
    s = SVG(); s.shape(rect(10, 10, 44, 32, 3), 'lilac', band=2); s.shape(rect(14, 14, 36, 24, 1), 'sky', hl=(17, 17, 6, 2))
    s.shape(path('M2 44H62L58 54H6Z'), 'purple', band=2); s.line('M26 48H38', OUT, 2.5); return s

@icon
def lipstick():
    s = SVG(); s.shape(path('M24 22V12L40 6V22Z'), 'red', hl=(27, 12, 2, 6))
    s.shape(rect(22, 22, 20, 8, 1), 'gold', band=2); s.shape(rect(20, 30, 24, 28, 3), 'lilac', hl=(24, 34, 2, 10)); return s

@icon
def lock():
    s = SVG(); s.line('M20 28V20a12 12 0 0 1 24 0v8', OUT, 9); s.line('M20 28V20a12 12 0 0 1 24 0v8', '#E3DDF0', 4)
    s.shape(rect(10, 26, 44, 32, 5), 'yellow', hl=(14, 30, 2, 10))
    s.shape(circ(32, 39, 4.5), 'dark', band=0); s.shape(rect(30, 40, 4, 10, 1), 'dark', band=0); return s

@icon
def luggage():
    s = SVG(); s.line('M24 12V6H40V12', OUT, 3)
    s.shape(rect(12, 12, 40, 42, 5), 'pink', hl=(16, 16, 2, 10))
    for x in (24, 40): s.line(f'M{x} 14V52', '#FF8FAB', 3, 'butt')
    s.shape(circ(20, 57, 3.5), 'dark', band=0); s.shape(circ(44, 57, 3.5), 'dark', band=0); return s

@icon
def mai():
    s = SVG(); s.shape(path('M10 30C10 14 20 6 32 6C44 6 54 14 54 30V40H10Z'), 'coffee', band=3)
    s.shape(ell(32, 34, 18, 18), 'skin', hl=(19, 24, 3, 4))
    s.shape(path('M14 26C18 14 30 12 32 16C34 12 46 14 50 26C42 22 36 22 32 18C28 22 22 22 14 26Z'), 'coffee', band=0)
    s.raw(f'<rect x="23" y="32" width="4" height="5" fill="{OUT}"/><rect x="37" y="32" width="4" height="5" fill="{OUT}"/>')
    s.raw('<ellipse cx="20" cy="41" rx="3" ry="2" fill="#FF8FAB"/><ellipse cx="44" cy="41" rx="3" ry="2" fill="#FF8FAB"/>')
    s.line('M28 44q4 3 8 0', OUT, 2)
    s.shape(path('M14 54C14 50 22 50 32 50C42 50 50 50 50 54V60H14Z'), 'lilac', band=2)
    s.shape(circ(50, 12, 5), 'pink', band=1); return s

@icon
def mail():
    s = SVG(); s.shape(rect(4, 12, 56, 40, 4), 'white', hl=(8, 30, 2, 8))
    s.line('M6 14L32 34L58 14', OUT, 3); s.shape(rect(44, 18, 10, 10, 1), 'pink', band=0); return s

@icon
def map_pin():
    s = SVG(); s.shape(ell(32, 56, 14, 4), 'grey', band=0)
    s.shape(path('M32 58C32 58 10 36 10 24a22 22 0 0 1 44 0C54 36 32 58 32 58Z'), 'red', hl=(17, 16, 3, 6))
    s.shape(circ(32, 24, 8), 'white', band=2); return s

@icon
def mic():
    s = SVG(); s.line('M14 30a18 18 0 0 0 36 0M32 48V56M22 58H42', OUT, 4)
    s.shape(rect(22, 4, 20, 36, 10), 'pink', hl=(26, 10, 2, 10))
    for y in (14, 20, 26): s.line(f'M24 {y}H40', '#FF8FAB', 2, 'butt')
    return s

@icon
def milk_tea():
    s = SVG(); s.line('M36 12L42 2', OUT, 6); s.line('M36 12L42 2', '#FFB7C9', 2.5)
    s.shape(path('M14 18H50L46 58H18Z'), 'tan', hl=(18, 22, 2, 12))
    for x, y in ((24, 50), (32, 52), (40, 50), (28, 44), (36, 45)): s.shape(circ(x, y, 3), 'coffee', band=0)
    s.shape(path('M12 18C12 8 52 8 52 18Z'), 'white', band=2); s.shape(rect(10, 16, 44, 5, 2), 'white', band=2); return s

@icon
def monitor():
    s = SVG(); s.shape(rect(26, 44, 12, 8), 'grey', band=2); s.shape(rect(16, 52, 32, 6, 2), 'lilac', band=2)
    s.shape(rect(4, 6, 56, 40, 4), 'lilac', band=2); s.shape(rect(9, 11, 46, 30, 1), 'sky', hl=(12, 14, 8, 2)); return s

@icon
def moon():
    s = SVG(); s.shape(path('M38 4C24 6 12 18 12 32C12 48 26 60 42 58C50 57 56 52 60 46C44 50 30 40 30 24C30 16 34 8 38 4Z'), 'yellow', hl=(18, 26, 2, 8))
    for x, y in ((46, 14), (54, 26)): s.line(f'M{x} {y - 4}V{y + 4}M{x - 4} {y}H{x + 4}', '#F5C451', 2.5)
    return s

@icon
def more():
    s = SVG()
    for x in (12, 32, 52): s.shape(circ(x, 32, 7), 'lilac', hl=(x - 4, 28, 2, 2))
    return s

@icon
def mountain():
    s = SVG(); s.shape(poly([(36, 58), (48, 24), (62, 58)]), 'green', band=3)
    s.shape(poly([(2, 58), (24, 10), (46, 58)]), 'mint', hl=(20, 22, 2, 8))
    s.shape(path('M16 26L24 10L32 26L28 24L24 28L20 24Z'), 'white', band=0); return s

@icon
def music():
    s = SVG(); s.shape(path('M22 14L54 6V16L22 24Z'), 'purple', band=2)
    s.line('M24 20V46M52 12V40', OUT, 5); s.line('M24 20V46M52 12V40', '#CDB8F6', 1.5)
    s.shape(ell(16, 48, 9, 7), 'lilac', hl=(10, 44, 2, 2)); s.shape(ell(44, 42, 9, 7), 'lilac', hl=(38, 38, 2, 2)); return s

@icon
def non_la():
    s = SVG(); s.line('M18 44Q32 62 46 44', '#FF8FAB', 3)
    s.shape(path('M32 6L60 46H4Z'), 'bread', hl=(28, 14, 2, 6))
    for y in (18, 28, 38): s.line(f'M{32 - (y - 6) * 0.7} {y}H{32 + (y - 6) * 0.7}', '#E3A45E', 2, 'butt')
    s.shape(ell(32, 46, 28, 4), 'tan', band=2); return s

@icon
def package():
    s = SVG(); s.shape(rect(6, 14, 52, 44, 3), 'tan', hl=(10, 18, 2, 10))
    s.shape(path('M6 14L14 4H50L58 14Z'), 'bread', band=2)
    s.shape(rect(26, 4, 12, 22), 'cream', band=2); s.line('M40 48H52', '#B98058', 3, 'butt'); return s

@icon
def pagoda():
    s = SVG(); s.shape(rect(18, 44, 28, 14), 'cream', band=2); s.shape(rect(28, 48, 8, 10), 'wood', band=0)
    s.shape(path('M4 46Q32 36 60 46L56 40H8Z'), 'red', band=2)
    s.shape(rect(22, 28, 20, 12), 'cream', band=2)
    s.shape(path('M10 30Q32 20 54 30L50 24H14Z'), 'red', band=2)
    s.shape(path('M18 24Q32 4 46 24Z'), 'red', band=2); s.line('M32 2V8', OUT, 3); return s

@icon
def palette():
    s = SVG(); s.shape(path('M32 4C14 4 4 18 4 32C4 48 18 60 32 60C38 60 38 54 36 50C34 46 36 42 42 42H50C56 42 60 38 60 30C60 14 46 4 32 4Z'), 'cream', hl=(12, 20, 2, 8))
    for x, y, c in ((18, 22, 'red'), (30, 14, 'yellow'), (44, 18, 'mint'), (48, 30, 'sky'), (16, 38, 'lilac')): s.shape(circ(x, y, 5), c, band=1)
    return s

@icon
def palm():
    s = SVG(); s.shape(ell(32, 56, 26, 6), 'bread', band=2)
    s.line('M30 52Q34 36 30 18', OUT, 8); s.line('M30 52Q34 36 30 18', '#D9A47A', 4)
    for d in ('M30 18C22 8 8 12 6 22C14 16 22 16 30 18Z', 'M30 18C38 6 54 8 58 18C50 14 40 14 30 18Z', 'M30 18C26 22 18 30 18 38C24 30 28 24 30 18Z', 'M30 18C36 22 44 28 46 36C40 30 34 24 30 18Z'): s.shape(path(d), 'green', band=2)
    s.shape(circ(28, 22, 3.5), 'coffee', band=0); s.shape(circ(34, 22, 3.5), 'coffee', band=0); return s

@icon
def parking():
    s = SVG(); s.shape(rect(6, 6, 52, 52, 6), 'blue', hl=(10, 10, 2, 10))
    s.shape(path('M22 16H36C44 16 48 20 48 27C48 34 44 38 36 38H30V48H22ZM30 23V31H35C38 31 40 30 40 27C40 24 38 23 35 23Z', 'evenodd'), 'white', band=2); return s

@icon
def party():
    s = SVG()
    s.shape(path('M6 58L18 18L46 46Z'), 'yellow', hl=(14, 36, 2, 6))
    s.line('M12 40L26 48M16 28L36 40', '#FF8FAB', 3, 'butt')
    for x, y, c in ((40, 10, '#FF8FAB'), (54, 22, '#6FCF9F'), (30, 6, '#6AA8F0'), (50, 36, '#F5C451')): s.raw(f'<rect x="{x}" y="{y}" width="4" height="4" fill="{c}"/>')
    s.line('M28 26Q36 14 30 6M38 36Q50 30 58 34', OUT, 2.5); return s

@icon
def paw():
    s = SVG(); s.shape(path('M32 30C22 30 12 42 14 50C16 58 24 56 32 54C40 56 48 58 50 50C52 42 42 30 32 30Z'), 'pink', hl=(20, 38, 3, 3))
    for x, y in ((14, 26), (24, 14), (40, 14), (50, 26)): s.shape(ell(x, y, 6, 7), 'pink', band=2)
    return s

@icon
def pen():
    s = SVG(); s.shape(path('M44 6L58 20L24 54L10 40Z'), 'sky', hl=(30, 22, 2, 2))
    s.shape(path('M10 40L24 54L6 58Z'), 'cream', band=0); s.shape(path('M7 52L12 57L5 59Z'), 'dark', band=0)
    s.line('M38 12L52 26', '#6AA8F0', 3, 'butt'); return s

@icon
def pencil():
    s = SVG(); s.shape(path('M42 10L54 22L22 54L10 42Z'), 'yellow', hl=(28, 26, 2, 2))
    s.shape(path('M42 10L48 4C50 2 52 2 54 4L60 10C62 12 62 14 60 16L54 22Z'), 'pink', band=2)
    s.shape(path('M10 42L22 54L4 60Z'), 'tan', band=0); s.shape(path('M5 55L9 59L3 61Z'), 'dark', band=0)
    s.line('M16 48L48 16', '#F5C451', 2.5, 'butt'); return s

@icon
def percent():
    s = SVG(); s.line('M48 8L16 56', OUT, 12); s.line('M48 8L16 56', '#FF8FAB', 7)
    for x, y in ((16, 16), (48, 48)):
        s.shape(path(f'M{x} {y - 11}a11 11 0 1 1 0 22a11 11 0 1 1 0-22zM{x} {y - 4}a4 4 0 1 0 0 8a4 4 0 1 0 0-8z', 'evenodd'), 'pink', band=2)
    return s

@icon
def phin():
    s = SVG(); s.shape(path('M18 4H46V12H18Z'), 'grey', band=2)
    s.shape(path('M16 12H48L44 30H20Z'), 'steel', hl=(21, 15, 2, 8)); s.shape(rect(10, 28, 44, 5, 2), 'grey', band=2)
    s.shape(path('M18 34H46L42 58H22Z'), 'glass', band=0)
    s.shape(path('M19 42H45L42 58H22Z'), 'coffee', band=2); s.shape(path('M19 40H45V44H19Z'), 'cream', band=0, ol=False)
    s.line('M32 33V40', '#82563A', 2, 'butt'); return s
