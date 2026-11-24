"""Bộ vẽ pixel tay 64x64 cho Spendy, học theo cách pixel-art-xl vẽ (không dùng AI).

Cách làm:
- Mỗi icon ghép từ các khối (elip, chữ nhật bo góc, đa giác, nét). Mỗi khối có chất liệu (màu gốc)
  và kiểu khối: 'round' (căng tròn như quả cầu), 'soft' (bo mép dày), 'flat' (mặt phẳng, chỉ vát mép).
- Tô bóng tự động: dựng "độ cao" từ khoảng cách tới mép khối, tính pháp tuyến, chiếu ánh sáng từ trên-trái,
  lượng tử hoá thành 5 sắc độ. Sắc sáng ngả vàng, sắc tối ngả tím (hue shift) nên màu tươi, không xỉn.
- Viền ngoài 1px màu tối cùng tông với vật (không đen tuyền), mép giữa các khối chồng nhau có nét tối.
- Đốm sáng trắng ở vùng sáng nhất, dọn pixel lẻ để mảng màu sạch.
"""
import colorsys, math
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

N = 64
LIGHT = np.array([-0.55, -0.65, 0.53]); LIGHT /= np.linalg.norm(LIGHT)

def hx(h):
    h = h.lstrip('#'); return np.array([int(h[i:i+2], 16) for i in (0, 2, 4)], float) / 255

def shift(rgb, dh, ds, dv):
    h, s, v = colorsys.rgb_to_hsv(*rgb)
    return np.clip(colorsys.hsv_to_rgb((h + dh) % 1, min(1, max(0, s + ds)), min(1, max(0, v + dv))), 0, 1)

def toward(h, target, k):
    d = ((target - h + .5) % 1) - .5; return h + d * k

def ramp(base):
    """5 sắc độ + viền, hue shift: sáng -> vàng (60°), tối -> tím (275°)."""
    rgb = hx(base); h, s, v = colorsys.rgb_to_hsv(*rgb); out = []
    for k in (-2, -1, 0, 1, 2):
        if k < 0:
            hh = toward(h, 275/360, .10 * -k); ss = min(1, s + .08 * -k + (.06 if s < .15 else 0)); vv = v - .13 * -k
        elif k > 0:
            hh = toward(h, 55/360, .07 * k); ss = max(0, s - .10 * k); vv = min(1, v + .07 * k + (.04 if v > .9 else 0))
        else:
            hh, ss, vv = h, s, v
        out.append(np.array(colorsys.hsv_to_rgb(hh % 1, max(0, min(1, ss)), max(0, min(1, vv)))))
    hh = toward(h, 280/360, .35); line = np.array(colorsys.hsv_to_rgb(hh % 1, min(1, s * .55 + .35), max(.16, v * .32)))
    return out, line

PAL = {k: v for k, v in {
 'pink': '#F7A3BA', 'rose': '#EE7090', 'red': '#EA4F5E', 'coral': '#FF8466', 'orange': '#FF9F45',
 'gold': '#FFC93C', 'yellow': '#FFE070', 'cream': '#FFF0D6', 'paper': '#FAF4EC', 'white': '#FFFFFF',
 'mint': '#7EDDB2', 'green': '#4FBF73', 'leaf': '#79C850', 'teal': '#4CC6BA', 'sky': '#86C7F2',
 'blue': '#5A92EA', 'navy': '#4A55A0', 'lilac': '#BBA0EE', 'purple': '#9566D6', 'brown': '#A86C45',
 'tan': '#E3AA74', 'wood': '#CC8550', 'bread': '#EDA85A', 'grey': '#B5AEC2', 'steel': '#98A3B8',
 'dark': '#5A4A6E', 'skin': '#FFCFAA', 'coffee': '#7E4F35', 'broth': '#F2BE5E', 'noodle': '#FFF2D2',
 'glass': '#B9E5F8', 'chili': '#E8423C', 'meat': '#D9826E', 'ice': '#E6F6FF', 'milk': '#F3DDBF',
}.items()}
RAMPS = {k: ramp(v) for k, v in PAL.items()}

class Icon:
    def __init__(s):
        s.mat = np.full((N, N), -1, int)      # id khối trên cùng ở mỗi pixel
        s.shapes = []                          # (chất liệu, kiểu, viền?, mask)
        s.dots = {}; s.holes = np.zeros((N, N), bool)
        yy, xx = np.mgrid[0:N, 0:N]; s.X = xx + .5; s.Y = yy + .5
    def _add(s, mask, m, kind, ol):
        sid = len(s.shapes); s.shapes.append((m, kind, ol, mask)); s.mat[mask] = sid; return sid
    def ell(s, cx, cy, rx, ry, m, kind='round', ol=True):
        return s._add(((s.X - cx) / rx) ** 2 + ((s.Y - cy) / ry) ** 2 <= 1, m, kind, ol)
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
        mask = np.zeros((N, N), bool)
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            dx, dy = x1 - x0, y1 - y0; L = dx * dx + dy * dy or 1
            t = np.clip(((s.X - x0) * dx + (s.Y - y0) * dy) / L, 0, 1)
            mask |= (s.X - x0 - t * dx) ** 2 + (s.Y - y0 - t * dy) ** 2 <= (w / 2) ** 2
        return s._add(mask, m, kind, ol)
    def cut(s, mask_fn):
        """khoét lỗ (trong suốt) theo hàm mask(X,Y)."""
        m = mask_fn(s.X, s.Y); s.mat[m] = -1; s.holes |= m
    def hole_ell(s, cx, cy, rx, ry):
        s.cut(lambda X, Y: ((X - cx) / rx) ** 2 + ((Y - cy) / ry) ** 2 <= 1)
    def dot(s, x, y, col):
        s.dots[(x, y)] = col
    def dots(s, pts, col):
        for x, y in pts: s.dots[(x, y)] = col

    def render(s):
        img = np.zeros((N, N, 4)); mat = s.mat
        filled = mat >= 0
        for sid, (m, kind, ol, full) in enumerate(s.shapes):
            reg = mat == sid
            if not reg.any(): continue
            tones, line = RAMPS[m]
            # độ cao theo khoảng cách tới mép vùng nhìn thấy
            body = (full & ~s.holes) | reg
            d = ndi.distance_transform_edt(np.pad(body, 1))[1:-1, 1:-1]
            R = {'round': max(2.5, d.max()), 'soft': 5.0, 'flat': 1.6}[kind]
            hgt = np.sqrt(np.clip(1 - (1 - np.clip(d / R, 0, 1)) ** 2, 0, 1)) * R
            hgt = ndi.gaussian_filter(hgt, max(.8, R / 4.5) if kind == 'round' else .7)
            gy, gx = np.gradient(hgt)
            nrm = np.dstack([-gx, -gy, np.ones_like(gx) * .9]); nrm /= np.linalg.norm(nrm, axis=2, keepdims=True)
            lum = nrm @ LIGHT
            if kind == 'flat': lum = lum * .85 + .1
            # bóng đổ của khối nằm trên (khối vẽ sau) lên khối này
            above = (mat > sid)
            occ = ndi.binary_dilation(above, structure=np.ones((3, 3)))
            above_ol = np.isin(mat, [k for k in range(sid + 1, len(s.shapes)) if s.shapes[k][2]])
            occ_shift = np.roll(np.roll(above_ol, 1, 0), 1, 1) & reg
            idx = np.digitize(lum, [.30, .52, .72, .86])   # 0..4
            idx = np.where(occ_shift, np.maximum(idx - 1, 0), idx)
            if kind == 'round': idx = ndi.median_filter(idx, size=3)
            if d.max() <= 1.6: idx = np.full_like(idx, 2)        # nét mảnh: một màu
            col = np.array(tones)[idx]
            # nét tối giữa khối này và khối vẽ sau đè lên (nếu khối đó bật viền)
            over_ol = np.zeros((N, N), bool)
            for sid2 in range(sid + 1, len(s.shapes)):
                if s.shapes[sid2][2] and s.shapes[sid2][0] != m:
                    over_ol |= (mat == sid2)
            edge = ndi.binary_dilation(over_ol, structure=[[0,1,0],[1,1,1],[0,1,0]]) & reg
            col[edge] = line
            img[reg, :3] = col[reg]; img[reg, 3] = 1
            # đốm sáng trắng ở vùng sáng nhất của khối tròn
            if kind == 'round' and reg.sum() > 40:
                sp = (lum > np.percentile(lum[reg], 96)) & reg & ~edge
                img[sp, :3] = np.minimum(1, tones[4] * .3 + .75)
        # viền ngoài cùng tông: lấy màu viền của khối kề bên
        outer = ndi.binary_dilation(filled, structure=[[0,1,0],[1,1,1],[0,1,0]]) & ~filled
        ys, xs = np.nonzero(outer)
        for y, x in zip(ys, xs):
            best = None
            for dy, dx in ((0,1),(0,-1),(1,0),(-1,0),(1,1),(-1,-1),(1,-1),(-1,1)):
                yy, xx = y + dy, x + dx
                if 0 <= yy < N and 0 <= xx < N and mat[yy, xx] >= 0:
                    best = mat[yy, xx] if best is None else max(best, mat[yy, xx])
            img[y, x, :3] = RAMPS[s.shapes[best][0]][1]; img[y, x, 3] = 1
        for (x, y), c in s.dots.items():
            if 0 <= x < N and 0 <= y < N:
                img[y, x, :3] = hx(c) if isinstance(c, str) else c; img[y, x, 3] = 1
        return Image.fromarray((img * 255).round().astype(np.uint8), 'RGBA')

def sheet(icons, path, scale=4, cols=4, bg=(255, 246, 233)):
    from PIL import ImageDraw
    names = list(icons); n = N * scale; pad = 20; lh = 22
    rows = (len(names) + cols - 1) // cols
    im = Image.new('RGB', (cols * (n + pad) + pad, rows * (n + pad + lh) + pad), bg); d = ImageDraw.Draw(im)
    for i, k in enumerate(names):
        x = pad + (i % cols) * (n + pad); y = pad + (i // cols) * (n + pad + lh)
        ic = icons[k].resize((n, n), Image.NEAREST); im.paste(ic, (x, y), ic)
        d.text((x + 4, y + n + 5), f"#{i+1:03d} {k}", fill=(59, 42, 63))
    im.save(path)
