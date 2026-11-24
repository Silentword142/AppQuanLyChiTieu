"""Bộ vẽ pixel tay 32x32 cho Spendy (không dùng AI).
Mỗi icon vẽ bằng hình khối (chữ nhật bo góc, elip, đa giác, nét) gán "chất liệu".
Sau đó tự động: tô 3 sắc độ (sáng trên-trái, tối dưới-phải), viền mực 1px bên ngoài,
viền trong tối giữa các khối khác chất liệu, đốm bóng trắng. Giống cách vẽ của pixel-art-xl:
nét viền đậm, màu phẳng, khối rõ, căn giữa.
"""
import math
from PIL import Image

INK = (59, 42, 63, 255)

def hx(h):
    h = h.lstrip('#'); return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (255,)

def mix(a, b, k):
    return tuple(round(a[i] + (b[i] - a[i]) * k) for i in range(3)) + (255,)

# chất liệu: (sáng, gốc, tối)
def ramp(base, hi=None, lo=None):
    b = hx(base)
    return (hx(hi) if hi else mix(b, (255, 255, 255), .45), b, hx(lo) if lo else mix(b, INK, .35))

M = {
 'pink': ramp('#F9A8BD', '#FFD3DE', '#D9728F'), 'rose': ramp('#F07A93', '#FFB3C3', '#C24D6C'),
 'red': ramp('#EE5D6C', '#FF9AA3', '#B83A52'), 'coral': ramp('#FF8C75', '#FFC0B0', '#D2604E'),
 'orange': ramp('#FFA654', '#FFD29A', '#D9762E'), 'gold': ramp('#FFD15C', '#FFF0B0', '#E09A2E'),
 'yellow': ramp('#FFE27A', '#FFF6C8', '#E8B440'), 'cream': ramp('#FFF3DC', '#FFFFFF', '#EBCBA6'),
 'paper': ramp('#FFFDF7', '#FFFFFF', '#D9CFC8'), 'mint': ramp('#8FE3B9', '#D2F7E3', '#4FB389'),
 'green': ramp('#5CC77E', '#A6EDB8', '#2F9257'), 'leaf': ramp('#7ACB5A', '#BDEB98', '#3E9441'),
 'teal': ramp('#5ED0C4', '#B2F0E8', '#2E9C98'), 'sky': ramp('#8CCBF5', '#CDEBFF', '#5592D6'),
 'blue': ramp('#5E9DF0', '#A8D0FF', '#3A6BC4'), 'navy': ramp('#4E5BA8', '#8692D8', '#2F3878'),
 'lilac': ramp('#C4A8F2', '#E8DCFF', '#9273D4'), 'purple': ramp('#9B6BDB', '#CDB0F5', '#6A44A8'),
 'brown': ramp('#B5764A', '#DDA77A', '#80502F'), 'tan': ramp('#E8B37E', '#FFDDB5', '#BD8450'),
 'wood': ramp('#D18C55', '#F2B886', '#9C5F35'), 'bread': ramp('#F0B060', '#FFDA9A', '#C47A34'),
 'grey': ramp('#B8B0C4', '#E6E1EE', '#867E96'), 'steel': ramp('#9AA6BA', '#D6DEEA', '#66708A'),
 'dark': ramp('#5A4A6E', '#7E6C94', '#3B2F4C'), 'white': ramp('#FFFFFF', '#FFFFFF', '#DCD4E4'),
 'skin': ramp('#FFD2B0', '#FFE8D6', '#E8A982'), 'coffee': ramp('#8A5A3C', '#B07A55', '#5E3A26'),
 'broth': ramp('#F5C46A', '#FFE3A0', '#D69A3C'), 'noodle': ramp('#FFF4D6', '#FFFFFF', '#EBD3A0'),
 'glass': ramp('#BFE8FA', '#EAF8FF', '#86C2E0'), 'black': ramp('#3B2F4C', '#5A4A6E', '#2A2036'),
}
FLAT = {}  # màu phẳng không tô bóng
def flat(name, col): FLAT[name] = hx(col) if isinstance(col, str) else col
flat('ink', INK); flat('w', '#FFFFFF'); flat('shine', '#FFFFFF'); flat('cheek', '#FF9DB0')

class Icon:
    def __init__(s, n=32):
        s.n = n; s.g = [[None]*n for _ in range(n)]; s.L = [[0]*n for _ in range(n)]; s.over = {}; s.layer = 0; s.ol = {}
    def px(s, x, y, m):
        if 0 <= x < s.n and 0 <= y < s.n: s.g[y][x] = m; s.L[y][x] = s.layer
    def _new(s, ol):
        s.layer += 1; s.ol[s.layer] = ol
    def rect(s, x0, y0, x1, y1, m, r=0, ol=True):
        s._new(ol)
        for y in range(y0, y1+1):
            for x in range(x0, x1+1):
                if r:
                    cx = min(max(x, x0+r), x1-r); cy = min(max(y, y0+r), y1-r)
                    if (x-cx)**2 + (y-cy)**2 > r*r + r*.6: continue
                s.px(x, y, m)
    def ell(s, cx, cy, rx, ry, m, ol=True):
        s._new(ol)
        for y in range(s.n):
            for x in range(s.n):
                if ((x+.5-cx)/rx)**2 + ((y+.5-cy)/ry)**2 <= 1: s.px(x, y, m)
    def poly(s, pts, m, ol=True):
        s._new(ol)
        for y in range(s.n):
            for x in range(s.n):
                px, py = x+.5, y+.5; ins = False; j = len(pts)-1
                for i in range(len(pts)):
                    xi, yi = pts[i]; xj, yj = pts[j]
                    if (yi > py) != (yj > py) and px < (xj-xi)*(py-yi)/(yj-yi)+xi: ins = not ins
                    j = i
                if ins: s.px(x, y, m)
    def line(s, x0, y0, x1, y1, m, w=1, ol=False):
        s._new(ol)
        n = max(abs(x1-x0), abs(y1-y0), 1)
        for i in range(n+1):
            x = round(x0+(x1-x0)*i/n); y = round(y0+(y1-y0)*i/n)
            for dy in range(w):
                for dx in range(w): s.px(x+dx-(w-1)//2, y+dy-(w-1)//2, m)
    def dot(s, x, y, col):  # pixel phẳng vẽ đè sau cùng
        s.over[(x, y)] = col
    def clear(s, x0, y0, x1, y1):
        for y in range(y0, y1+1):
            for x in range(x0, x1+1): s.px(x, y, None)

    def render(s, shade=True):
        n = s.n; g = s.g; out = Image.new('RGBA', (n, n), (0, 0, 0, 0)); P = out.load()
        at = lambda x, y: g[y][x] if 0 <= x < n and 0 <= y < n else None
        for y in range(n):
            for x in range(n):
                m = g[y][x]
                if m is None:
                    if any(at(x+dx, y+dy) is not None for dx, dy in ((1,0),(-1,0),(0,1),(0,-1))): P[x, y] = INK
                    continue
                if m in FLAT: P[x, y] = FLAT[m]; continue
                hi, base, lo = M[m]
                up, lf, dn, rt = at(x, y-1), at(x-1, y), at(x, y+1), at(x+1, y)
                c = base
                if shade:
                    if dn != m or rt != m or at(x+1, y+1) != m: c = lo
                    elif up != m or lf != m: c = hi
                # ranh giới với chất liệu khác (không phải nền) -> nét mực trong ở cạnh trên/trái của khối nằm dưới
                # nét mực trong: khối nằm dưới nhận viền quanh khối vẽ sau (nếu khối đó bật ol)
                L = s.L[y][x]
                for dx, dy in ((1,0),(-1,0),(0,1),(0,-1)):
                    xx, yy = x+dx, y+dy
                    if 0 <= xx < n and 0 <= yy < n and g[yy][xx] is not None and g[yy][xx] != m and s.L[yy][xx] > L and s.ol.get(s.L[yy][xx]):
                        c = INK; break
                P[x, y] = c
        for (x, y), col in s.over.items():
            if 0 <= x < n and 0 <= y < n: P[x, y] = hx(col) if isinstance(col, str) else col
        return out
    _order = None
    def _behind(s, m, other):
        return False

def sheet(icons, path, scale=6, cols=6, label=True):
    from PIL import ImageDraw
    names = list(icons); n = 32*scale; pad = 16; lh = 22 if label else 0
    rows = (len(names)+cols-1)//cols
    W = cols*(n+pad)+pad; H = rows*(n+pad+lh)+pad
    im = Image.new('RGB', (W, H), (255, 246, 233)); d = ImageDraw.Draw(im)
    for i, k in enumerate(names):
        x = pad + (i % cols)*(n+pad); y = pad + (i//cols)*(n+pad+lh)
        d.rectangle([x-2, y-2, x+n+1, y+n+1], fill=(255, 238, 226))
        ic = icons[k].resize((n, n), Image.NEAREST); im.paste(ic, (x, y), ic)
        if label: d.text((x+4, y+n+4), f"#{i+1:03d} {k}", fill=(59, 42, 63))
    im.save(path)
