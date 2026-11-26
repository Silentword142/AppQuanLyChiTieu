"""Kiểu "pixel ấm" (theo mẫu đồ ăn anh Hoàng gửi 2026-11-25 09:47):
không viền mực, viền là tông tối nhất của chính màu vật (selout), 3 tông + viền, có điểm sáng, không bóng đổ.
Dùng lại bản vẽ ASCII của px16 (icons_a..d): chữ thường = tông gốc, chữ HOA = tông tối,
'#' = nét chi tiết (lấy màu viền của vùng xung quanh), chữ số = màu không viền."""
from PIL import Image
N = 16
def h(x): return tuple(int(x[i:i+2], 16) for i in (0, 2, 4))
FAM = {  # họ màu: sáng, gốc, tối, viền
 'y': ('FFF3B0', 'FFD84D', 'F2A93B', 'B8651F'), 'w': ('FFFFFF', 'F4F0E8', 'D3CCDD', '8A8199'),
 'G': ('D3CCDD', 'A79FC0', '857DA0', '5A5275'), 'm': ('C4F2A8', '6CCF78', '35A05C', '1E6B4A'),
 'p': ('FFD3DD', 'FF8FAB', 'E0607F', 'A33A5C'), 'l': ('DCC8FF', 'AE90EC', '7E60CB', '4E3A8F'),
 'b': ('CDEBFF', '72BAF6', '4287DA', '284E9E'), 'o': ('FFDDB0', 'FFA552', 'E5702E', 'A3421E'),
 'c': ('F8DDB4', 'DCA46A', 'AD6E42', '6E3F26'), 'r': ('FFB0A8', 'EE5A52', 'C0343F', '7A1F2E'),
 'k': ('FFEBD6', 'FCCFA6', 'E5A178', '9E5F3F'), 'd': ('8F86A6', '5E5675', '453E5C', '2B2640'),
}
FAM = {k: tuple(h(c) for c in v) for k, v in FAM.items()}
# kiểu "retro trầm" (mẫu tạp hoá anh Hoàng gửi 09:55): màu trầm, viền ngoài nâu đậm
RETRO = {
 'y': ('F6E3A8', 'E3B65E', 'B98A3E', '7A5528'), 'w': ('FBF3E2', 'EEDDBC', 'C9B38E', '8A765B'),
 'G': ('D4D6CC', '9FAAA6', '6E7D7C', '45524F'), 'm': ('D3DCA2', '98AA62', '667A42', '3E4D2A'),
 'p': ('F4C8B4', 'DA907E', 'B06456', '7A3E36'), 'l': ('D6C2CF', 'A68AA4', '76607A', '4C3A50'),
 'b': ('C4D4D2', '83A0AA', '5A7682', '394C5A'), 'o': ('F4C68E', 'DC8C4E', 'AA5E34', '6E3A20'),
 'c': ('ECC896', 'C88E5A', '8E5A38', '5A3622'), 'r': ('EBA690', 'C65E46', '8E3C2E', '5C2620'),
 'k': ('F9E0C4', 'EBBE94', 'C78E68', '8A5A40'), 'd': ('8E7A60', '5E4A34', '45341F', '2D2210'),
}
RETRO = {k: tuple(h(c) for c in v) for k, v in RETRO.items()}
WARM = FAM; INK = None
def set_style(name):
    global FAM, INK
    if name == 'retro': FAM, INK = RETRO, h('2D2210')
    else: FAM, INK = WARM, None
CH = {}  # ký tự -> (họ, tông) ; tông 1 = gốc, 2 = tối
for k in 'ywmplbocrkd': CH[k] = (k, 1); CH[k.upper()] = (k, 2)
CH['g'] = ('w', 2); CH['G'] = ('G', 1); CH['a'] = ('m', 0); CH['h'] = ('y', 0); CH['n'] = ('b', 0); CH['e'] = ('p', 0)
DIG = {'1': ('p', 2), '2': ('w', 0), '3': ('y', 2), '4': ('b', 2), '5': ('m', 2), '6': ('G', 1), '7': ('l', 2), '8': ('r', 2), '9': ('c', 2), '0': ('G', 3)}

def parse(art):
    rows = [r.rstrip() for r in art.strip('\n').split('\n')]
    w = max(len(r) for r in rows); return [r.ljust(w, '.') for r in rows]

def symmetry(rows):
    """tỉ lệ ô khớp khi lật ngang (chỉ xét có/không có màu)"""
    tot = same = 0
    xs = [x for r in rows for x, c in enumerate(r) if c != '.']; x0, x1 = min(xs), max(xs)
    for r in rows:
        for x in range(x0, x1 + 1):
            a, b = r[x] != '.', r[x0 + x1 - x] != '.'
            if a or b: tot += 1; same += a == b
    return same / max(tot, 1)

def symmetrize(rows):
    """lật nửa trái sang phải cho ĐẦY ĐỦ hình (giữ chữ hoa/thường của ô bên phải nếu cùng họ)"""
    W = max(len(r) for r in rows); xs = [x for r in rows for x, c in enumerate(r) if c != '.']
    x0, x1 = min(xs), max(xs); out = []
    for r in rows:
        r = list(r.ljust(W, '.'))
        for x in range(x0, x1 + 1):
            m = x0 + x1 - x
            if m <= x: break
            L, R = r[x], r[m]
            if (L == '.') != (R == '.') or (L.lower() != R.lower()):
                r[m] = L.upper() if (R.isupper() and L.isalpha()) else L
        out.append(''.join(r))
    return out

SEAM = True
def render(art, name='?', sym=False):
    rows = parse(art)
    if sym: rows = symmetrize(rows)
    h_, w_ = len(rows), len(rows[0]); H, W = h_ + 2, w_ + 2
    fam = [[None] * W for _ in range(H)]; tone = [[None] * W for _ in range(H)]; kind = [[None] * W for _ in range(H)]
    for y, r in enumerate(rows):
        for x, ch in enumerate(r):
            Y, X = y + 1, x + 1
            if ch in CH: fam[Y][X], tone[Y][X] = CH[ch]; kind[Y][X] = 'f'
            elif ch in DIG: fam[Y][X], tone[Y][X] = DIG[ch]; kind[Y][X] = 'd'
            elif ch == '#': kind[Y][X] = '#'
    N4 = ((-1, 0), (0, -1), (1, 0), (0, 1))
    # '#' nét chi tiết: lấy họ màu của ô kề
    for y in range(H):
        for x in range(W):
            if kind[y][x] == '#':
                for dy, dx in N4 + ((1, 1), (-1, -1), (1, -1), (-1, 1)):
                    yy, xx = y + dy, x + dx
                    if 0 <= yy < H and 0 <= xx < W and kind[yy][xx] == 'f': fam[y][x] = fam[yy][xx]; break
                tone[y][x] = 3; fam[y][x] = fam[y][x] or 'G'
    # điểm sáng: ô tông gốc có ô trên hoặc trái trống, nằm nửa trên-trái
    hl = []
    for y in range(H):
        for x in range(W):
            if kind[y][x] == 'f' and tone[y][x] == 1:
                up = kind[y - 1][x]; lf = kind[y][x - 1]
                if (up is None or lf is None) and (y - 1) < h_ * .55 and (x - 1) < w_ * .6: hl.append((y, x))
    for y, x in hl: tone[y][x] = 0
    # đường tách khối: ô màu nằm trên/trái một mảng KHÁC họ thì lấy tông viền -> hai khối không dính
    if SEAM:
        seam = []
        for y in range(H - 1):
            for x in range(W - 1):
                if kind[y][x] != 'f': continue
                for yy, xx in ((y + 1, x), (y, x + 1)):
                    if kind[yy][xx] == 'f' and fam[yy][xx] != fam[y][x] and not (fam[y][x] in 'wG' and fam[yy][xx] in 'wG'):
                        seam.append((y, x)); break
        for y, x in seam: tone[y][x] = 3
    # viền ngoài = tông viền của họ màu ô kề (ưu tiên ô dưới/phải để viền đậm đúng mặt tối)
    out = {}
    for y in range(H):
        for x in range(W):
            if kind[y][x] is None:
                for dy, dx in ((0, -1), (-1, 0), (0, 1), (1, 0)):
                    yy, xx = y + dy, x + dx
                    if 0 <= yy < H and 0 <= xx < W and kind[yy][xx] == 'f': out[(y, x)] = fam[yy][xx]
    for (y, x), f in out.items(): fam[y][x] = f; tone[y][x] = 3; kind[y][x] = 'o'
    ys = [y for y in range(H) for x in range(W) if kind[y][x]]; xs = [x for y in range(H) for x in range(W) if kind[y][x]]
    y0, y1, x0, x1 = min(ys), max(ys), min(xs), max(xs); bh, bw = y1 - y0 + 1, x1 - x0 + 1
    if bh > N or bw > N: raise ValueError(f'{name}: {bw}x{bh}')
    oy, ox = (N - bh) // 2, (N - bw) // 2
    im = Image.new('RGBA', (N, N), (0, 0, 0, 0)); px = im.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if kind[y][x]:
                c = INK if (INK and kind[y][x] == 'o') else FAM[fam[y][x]][tone[y][x]]
                px[x - x0 + ox, y - y0 + oy] = (*c, 255)
    return im
