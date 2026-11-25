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
}
FAM = {k: tuple(h(c) for c in v) for k, v in FAM.items()}
CH = {}  # ký tự -> (họ, tông) ; tông 1 = gốc, 2 = tối
for k in 'ywmplbocr': CH[k] = (k, 1); CH[k.upper()] = (k, 2)
CH['g'] = ('w', 2); CH['G'] = ('G', 1)
DIG = {'1': ('p', 2), '2': ('w', 0), '3': ('y', 2), '4': ('b', 2), '5': ('m', 2), '6': ('G', 1), '7': ('l', 2), '8': ('r', 2), '9': ('c', 2), '0': ('G', 3)}

def parse(art):
    rows = [r.rstrip() for r in art.strip('\n').split('\n')]
    w = max(len(r) for r in rows); return [r.ljust(w, '.') for r in rows]

def render(art, name='?'):
    rows = parse(art); h_, w_ = len(rows), len(rows[0]); H, W = h_ + 2, w_ + 2
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
            if kind[y][x]: px[x - x0 + ox, y - y0 + oy] = (*FAM[fam[y][x]][tone[y][x]], 255)
    return im
