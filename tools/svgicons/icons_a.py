from svgkit import SVG, rect, circ, ell, poly, path, OUT
I = {}
def icon(f): I[f.__name__.replace('_', '-')] = f; return f
STEAM = '#C9C0DA'

@icon
def home():
    s = SVG()
    s.shape(rect(40, 8, 8, 18), 'wood', band=2)
    s.shape(rect(12, 30, 40, 26), 'cream')
    s.shape(poly([(6, 32), (32, 6), (58, 32)]), 'red', hl=(24, 16, 2, 4))
    s.shape(rect(26, 38, 12, 18), 'wood', band=3)
    s.raw(f'<circle cx="35" cy="47" r="1.5" fill="#F5C451"/>')
    for x in (15, 41):
        s.shape(rect(x, 37, 9, 9), 'sky', band=0); s.line(f'M{x+4.5} 37V46M{x} 41.5H{x+9}')
    s.shape(rect(6, 54, 52, 6, 3), 'leaf', band=2)
    s.shape(circ(12, 53, 5), 'green', band=2); s.shape(circ(52, 53, 5), 'green', band=2)
    return s

@icon
def coffee():
    s = SVG()
    for x, y in ((20, 4), (30, 2), (40, 4)):
        s.line(f'M{x} {y}q-3 4 0 7t0 7', STEAM, 2.5)
    s.shape(path('M48 26a10 10 0 1 1 0 20a10 10 0 1 1 0-20zM48 31a5 5 0 1 0 0 10a5 5 0 1 0 0-10z', 'evenodd'), 'white', band=2)
    s.shape(rect(10, 20, 38, 32, 6), 'white', hl=(14, 26, 2, 8))
    s.shape(ell(29, 21, 18, 4), 'coffee', band=0)
    s.shape(rect(10, 32, 38, 8), 'pink', band=2)
    s.raw('<path d="M26 34h2v1h2v-1h2v3h-1v1h-1v1h-2v-1h-1v-1h-1z" fill="#FFFFFF"/>')
    s.shape(rect(4, 52, 54, 7, 3), 'pink', band=2)
    return s

@icon
def pho():
    s = SVG()
    s.tube('M36 4L30 26', 'wood', 3); s.tube('M45 6L37 27', 'wood', 3)
    s.shape(ell(32, 30, 27, 6), 'broth', band=0)
    s.shape(ell(18, 28, 6, 3), 'meat', band=1); s.shape(ell(44, 28, 6, 3), 'meat', band=1)
    s.line('M24 30q3-3 6 0t6 0', '#FFFFFF', 2.5)
    s.raw('<circle cx="27" cy="27" r="2" fill="#8FD06A"/><circle cx="38" cy="32" r="2" fill="#6FCF9F"/>')
    s.shape(path('M4 32H60Q58 50 44 52H20Q6 50 4 32Z'), 'sky', hl=(10, 36, 2, 6))
    s.line('M12 40H52', '#FFFFFF', 2, 'butt')
    s.shape(rect(20, 51, 24, 6, 2), 'blue', band=2)
    for x in (13, 21):
        s.line(f'M{x} 22q-3-4 0-8t0-8', STEAM, 2.5)
    return s

@icon
def wallet():
    s = SVG()
    s.shape(rect(14, 8, 32, 16, 2), 'mint', band=2)
    s.shape(circ(30, 15, 4), 'green', band=0)
    s.shape(rect(6, 18, 52, 38, 6), 'wood', hl=(10, 24, 2, 8))
    s.line('M10 24H34', '#F2D2B0', 2, 'butt'); s.raw('<path d="M10 24H34" stroke="#D9A47A" stroke-width="2" stroke-dasharray="3 3"/>')
    s.shape(rect(36, 28, 24, 18, 5), 'tan', band=3)
    s.shape(circ(46, 37, 3.5), 'gold', band=1)
    return s

@icon
def piggy():
    s = SVG()
    s.shape(circ(28, 9, 6), 'gold', band=2); s.line('M28 6v6', '#E0A23C', 2, 'butt')
    s.shape(poly([(13, 22), (17, 10), (26, 19)]), 'rose', band=2)
    s.shape(rect(10, 44, 9, 14, 2), 'pink', band=2); s.shape(rect(37, 44, 9, 14, 2), 'pink', band=2)
    s.line('M8 32q-5-1-4-5t5 0', OUT, 2)
    s.shape(ell(29, 35, 22, 17), 'pink', hl=(14, 25, 3, 3))
    s.shape(ell(52, 35, 6, 7), 'rose', band=2)
    s.raw(f'<rect x="49.5" y="32" width="2" height="4" fill="{OUT}"/><rect x="53.5" y="32" width="2" height="4" fill="{OUT}"/>')
    s.raw(f'<rect x="38" y="26" width="3" height="4" fill="{OUT}"/><rect x="20" y="19" width="14" height="3" rx="1.5" fill="{OUT}"/>')
    s.raw('<ellipse cx="41" cy="37" rx="3" ry="1.6" fill="#FF8FAB"/>')
    return s

@icon
def chart():
    s = SVG()
    s.shape(rect(4, 6, 56, 52, 4), 'white', band=2)
    s.line('M10 12V50H54', '#A79FC0', 2)
    for x, y, m in ((14, 38, 'mint'), (25, 30, 'sky'), (36, 22, 'lilac'), (47, 14, 'pink')):
        s.shape(rect(x, y, 8, 50 - y), m, band=2)
    s.tube('M12 34L24 26L34 20L46 10', 'yellow', 2.5)
    s.shape(poly([(41, 8), (50, 6), (48, 15)]), 'yellow', band=0)
    return s

@icon
def car():
    s = SVG()
    s.shape(poly([(14, 28), (20, 12), (44, 12), (52, 28)]), 'red', band=2)
    s.shape(poly([(18, 27), (22, 16), (30, 16), (30, 27)]), 'glass', band=0)
    s.shape(poly([(34, 16), (42, 16), (47, 27), (34, 27)]), 'glass', band=0)
    s.shape(rect(4, 26, 56, 20, 7), 'red', hl=(9, 30, 2, 5))
    s.line('M32 28V42'); s.line('M24 34h4M36 34h4', OUT, 2)
    s.shape(rect(4, 31, 6, 5, 2), 'yellow', band=0); s.shape(rect(54, 31, 6, 5, 2), 'orange', band=0)
    for x in (17, 47):
        s.shape(circ(x, 47, 8), 'dark', band=0); s.shape(circ(x, 47, 3.5), 'grey', band=1)
    return s

@icon
def gift():
    s = SVG()
    for x in (20, 44):
        s.shape(path(f'M{x} 13m-9 0a9 7 0 1 0 18 0a9 7 0 1 0-18 0zM{x} 13m-4 0a4 3 0 1 0 8 0a4 3 0 1 0-8 0z', 'evenodd'), 'gold', band=2)
    s.shape(rect(9, 28, 46, 30), 'lilac', hl=(13, 32, 2, 8))
    s.shape(rect(5, 19, 54, 11, 2), 'purple', band=2)
    s.shape(rect(28, 19, 8, 39), 'yellow', band=2)
    s.shape(circ(32, 18, 5), 'orange', band=2)
    return s

@icon
def heart():
    s = SVG()
    s.shape(path('M32 56L8 32A13 13 0 0 1 32 15A13 13 0 0 1 56 32Z'), 'pink', band=5, hl=(12, 22, 3, 6))
    return s

@icon
def receipt():
    s = SVG()
    d = 'M12 4H52V58' + ''.join(f'L{52 - 5 * (i + 1)} {54 if i % 2 == 0 else 58}' for i in range(8)) + 'Z'
    s.shape(path(d), 'white', band=2)
    for y, x1 in ((14, 44), (22, 38), (30, 44)): s.line(f'M19 {y}H{x1}', '#A79FC0', 3, 'butt')
    s.line('M19 40H30M35 40H45', '#E8708E', 3, 'butt')
    s.shape(circ(44, 49, 6), 'mint', band=2); s.line('M41 49l2 2l4-4', '#FFFFFF', 2)
    return s

@icon
def banh_mi():
    s = SVG()
    s.shape(path('M6 44C2 36 10 24 22 19L42 12C54 8 62 16 58 26C56 32 48 38 38 42L20 48C14 50 8 48 6 44Z'), 'bread', hl=(18, 24, 3, 3))
    s.shape(path('M12 38C18 30 30 26 42 22C48 20 54 20 54 24C50 30 40 32 30 36L16 41Z'), 'leaf', band=2)
    s.shape(path('M16 40C22 35 32 32 44 28C47 27 48 29 46 30C38 34 28 37 18 42Z'), 'meat', band=0)
    s.raw('<circle cx="34" cy="31" r="2" fill="#E8707E"/>')
    for x in (16, 28, 40): s.line(f'M{x} {24 - (x - 16) // 4}l6-3', '#E3A45E', 2)
    return s

@icon
def coins():
    s = SVG()
    for y in (48, 39, 30):
        s.shape(path(f'M5 {y}h34v7a17 5 0 0 1-34 0z'), 'gold', band=2)
        s.shape(ell(22, y, 17, 5), 'yellow', band=1)
    s.shape(circ(45, 20, 14), 'gold', band=3)
    s.shape(circ(45, 20, 9), 'yellow', band=2)
    s.line('M45 14V26', '#E0A23C', 3, 'butt')
    return s
