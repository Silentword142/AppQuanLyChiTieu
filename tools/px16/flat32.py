"""Bộ vẽ icon 32x32 kiểu đơn giản (theo mẫu 16px anh Hoàng gửi), vẽ bằng khối hình.
API giống tools/pixeldraw/px64.py (toạ độ theo lưới 64, tự chia đôi), nhưng tô phẳng:
mỗi khối 1 màu sáng + dải đậm ở mép phải-dưới, chấm trắng góc trên-trái cho khối tròn,
viền 1px #3B3355 quanh khối và giữa các khối, bóng đổ lệch (1,1)."""
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

N = 32; S = N / 64
OUT = np.array([59, 51, 85]); SHADOW = np.array([186, 177, 181]); WHITE = np.array([255, 255, 255])
def h(x): return np.array([int(x[i:i+2], 16) for i in (0, 2, 4)])
TONE = {k: (h(a), h(b)) for k, (a, b) in {
 'paper': ('FFFFFF', 'E3DDF0'), 'white': ('FFFFFF', 'E3DDF0'), 'ice': ('FFFFFF', 'E3DDF0'), 'noodle': ('FFF6DE', 'F2DDB0'),
 'cream': ('FFF3D6', 'F2D2B0'), 'milk': ('FFF3D6', 'F2D2B0'),
 'pink': ('FFB7C9', 'FF8FAB'), 'rose': ('FF8FAB', 'E8708E'), 'red': ('FFA3A3', 'E8707E'), 'chili': ('FF8A8A', 'E06070'),
 'coral': ('FFB8A0', 'FF8F76'), 'orange': ('FFCBA4', 'FF9F6B'), 'yellow': ('FFE8A3', 'F5C451'), 'gold': ('F5C451', 'E0A23C'),
 'mint': ('B5EAD7', '6FCF9F'), 'green': ('9FE0B5', '6FCF9F'), 'leaf': ('C2EBA0', '8FD06A'), 'teal': ('A6E6E0', '6CC9C0'),
 'sky': ('A9D8FF', '6AA8F0'), 'glass': ('CDEBFF', 'A9D8FF'), 'blue': ('8CC0FA', '6AA8F0'), 'navy': ('8A8AD0', '6666AA'),
 'lilac': ('CDB8F6', '9F86E0'), 'purple': ('B39AEE', '8C70D6'), 'brown': ('C9926A', 'A06E4C'), 'coffee': ('A87452', '82563A'),
 'tan': ('F2D2B0', 'D9A47A'), 'bread': ('F7CC8E', 'E3A45E'), 'broth': ('FFDB8A', 'F2B860'), 'wood': ('D9A47A', 'B98058'),
 'grey': ('E3DDF0', 'A79FC0'), 'steel': ('CFC8E0', 'A79FC0'), 'dark': ('3B3355', '3B3355'), 'skin': ('FFDCC0', 'F2B896'),
 'meat': ('F2A08A', 'D9826E'),
}.items()}

class Icon:
    def __init__(s, scale=S):
        s.sc = scale
        s.mat = np.full((N, N), -1, int); s.shapes = []; s.dots = {}
        yy, xx = np.mgrid[0:N, 0:N]; s.X = (xx + .5) / scale; s.Y = (yy + .5) / scale
    def _add(s, mask, m, kind, ol):
        sid = len(s.shapes); s.shapes.append((m, kind, ol, mask)); s.mat[mask] = sid; return sid
    def ell(s, cx, cy, rx, ry, m, kind='round', ol=True):
        return s._add(((s.X - cx) / max(rx, 1)) ** 2 + ((s.Y - cy) / max(ry, 1)) ** 2 <= 1, m, kind, ol)
    def rect(s, x0, y0, x1, y1, m, r=0, kind='soft', ol=True):
        cx = np.clip(s.X, x0 + r, x1 - r); cy = np.clip(s.Y, y0 + r, y1 - r)
        mask = (s.X >= x0) & (s.X <= x1) & (s.Y >= y0) & (s.Y <= y1) & (((s.X - cx) ** 2 + (s.Y - cy) ** 2) <= r * r + .01)
        return s._add(mask, m, kind, ol)
    def poly(s, pts, m, kind='soft', ol=True):
        mask = np.zeros((N, N), bool); j = len(pts) - 1
        for i in range(len(pts)):
            (xi, yi), (xj, yj) = pts[i], pts[j]
            cond = (yi > s.Y) != (yj > s.Y)
            with np.errstate(divide='ignore', invalid='ignore'):
                xint = (xj - xi) * (s.Y - yi) / (yj - yi) + xi
            mask ^= cond & (s.X < xint); j = i
        return s._add(mask, m, kind, ol)
    def line(s, pts, m, w=2, kind='round', ol=True):
        mask = np.zeros((N, N), bool); w = max(w, 2.2)
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            dx, dy = x1 - x0, y1 - y0; L = dx * dx + dy * dy or 1
            t = np.clip(((s.X - x0) * dx + (s.Y - y0) * dy) / L, 0, 1)
            mask |= (s.X - x0 - t * dx) ** 2 + (s.Y - y0 - t * dy) ** 2 <= (w / 2) ** 2
        return s._add(mask, m, kind, ol)
    def hole_ell(s, cx, cy, rx, ry):
        m = ((s.X - cx) / rx) ** 2 + ((s.Y - cy) / ry) ** 2 <= 1; s.mat[m] = -1
    def dot(s, x, y, col): s.dots[(int(x * s.sc), int(y * s.sc))] = col
    # ---- công cụ lưới 32 nguyên (sc=1): toạ độ là chỉ số pixel, nét sắc ----
    def box(s, x0, y0, x1, y1, m, kind='soft', ol=True, r=0):
        """chữ nhật phủ pixel x0..x1, y0..y1 (gồm cả hai đầu); r = số pixel vạt góc (bậc thang 45°)"""
        px = s.X - .5; py = s.Y - .5
        mask = (px >= x0) & (px <= x1) & (py >= y0) & (py <= y1)
        if r:
            for cx, cy, sx, sy in ((x0, y0, 1, 1), (x1, y0, -1, 1), (x0, y1, 1, -1), (x1, y1, -1, -1)):
                mask &= ~(((px - cx) * sx + (py - cy) * sy) < r)
        return s._add(mask, m, kind, ol)
    def hole_box(s, x0, y0, x1, y1):
        px = s.X - .5; py = s.Y - .5; s.mat[(px >= x0) & (px <= x1) & (py >= y0) & (py <= y1)] = -1
    def mask(s, art, x0, y0, m, kind='soft', ol=True):
        """khối vẽ bằng ASCII: ký tự khác '.' là có"""
        mk = np.zeros((N, N), bool)
        for j, row in enumerate(art.strip('\n').split('\n')):
            for i, ch in enumerate(row):
                if ch not in '. ' and 0 <= y0 + j < N and 0 <= x0 + i < N: mk[y0 + j, x0 + i] = True
        return s._add(mk, m, kind, ol)
    def seg(s, x0, y0, x1, y1, col):
        """nét 1px Bresenham, tô màu cố định (không viền), vẽ sau cùng"""
        dx, dy = abs(x1 - x0), -abs(y1 - y0); sx = 1 if x0 < x1 else -1; sy = 1 if y0 < y1 else -1; e = dx + dy
        while True:
            s.dots[(x0, y0)] = col
            if x0 == x1 and y0 == y1: break
            e2 = 2 * e
            if e2 >= dy: e += dy; x0 += sx
            if e2 <= dx: e += dx; y0 += sy
    def pts(s, lst, col):
        for x, y in lst: s.dots[(x, y)] = col
    def dots(s, pts, col):
        for x, y in pts: s.dot(x, y, col)

    def render(s):
        mat = s.mat; img = np.zeros((N, N, 4), int); filled = mat >= 0
        def shifted(m, dy, dx):
            o = np.zeros_like(m); ys = slice(max(dy, 0), N + min(dy, 0)); yd = slice(max(-dy, 0), N + min(-dy, 0))
            xs = slice(max(dx, 0), N + min(dx, 0)); xd = slice(max(-dx, 0), N + min(-dx, 0))
            o[yd, xd] = m[ys, xs]; return o
        for sid, (m, kind, ol, full) in enumerate(s.shapes):
            reg = mat == sid
            if not reg.any(): continue
            lt, dk = TONE[m]
            vis = reg
            dark = vis & ~shifted(vis | (mat > sid), 2, 2) if kind != 'flat' else vis & ~shifted(vis | (mat > sid), 1, 1)
            col = np.where(dark[..., None], dk, lt)
            if kind == 'round' and vis.sum() > 60:
                # chấm sáng nhỏ gọn (2-3 px) ở góc trên-trái, cách mép 2px
                inner = ndi.binary_erosion(vis, iterations=2)
                ys, xs = np.nonzero(inner)
                if len(ys):
                    k = np.argmin(ys + xs); y, x = ys[k], xs[k]
                    for dy, dx in ((0, 0), (1, 0), (0, 1)):
                        if inner[min(y + dy, N - 1), min(x + dx, N - 1)]: col[y + dy, x + dx] = WHITE
            img[vis, :3] = col[vis]; img[vis, 3] = 255
            # nét viền giữa khối này và khối đè lên
            over = np.isin(mat, [k for k in range(sid + 1, len(s.shapes)) if s.shapes[k][2] and s.shapes[k][0] != m])
            edge = ndi.binary_dilation(over, structure=[[0, 1, 0], [1, 1, 1], [0, 1, 0]]) & vis
            img[edge, :3] = OUT
        for (x, y), c in s.dots.items():
            if 0 <= x < N and 0 <= y < N: img[y, x, :3] = h(c.lstrip('#')); img[y, x, 3] = 255
        outer = ndi.binary_dilation(filled, structure=[[0, 1, 0], [1, 1, 1], [0, 1, 0]]) & ~filled
        img[outer, :3] = OUT; img[outer, 3] = 255
        sh = shifted(img[..., 3] > 0, -1, -1) & (img[..., 3] == 0)
        img[sh, :3] = SHADOW; img[sh, 3] = 255
        return Image.fromarray(img.astype(np.uint8), 'RGBA')
