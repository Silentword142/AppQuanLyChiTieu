"""Vẽ icon bằng SVG (lưới 64), mỗi khối: màu sáng + dải đậm phải-dưới tự sinh + viền 2px #3B3355.
Khối vẽ sau đè lên khối trước và có viền riêng nên các hình tách bạch."""
OUT = '#3B3355'
TONE = {
 'white': ('#FFFFFF', '#E3DDF0'), 'cream': ('#FFF3D6', '#F2D2B0'), 'noodle': ('#FFF6DE', '#F2DDB0'),
 'pink': ('#FFB7C9', '#FF8FAB'), 'rose': ('#FF8FAB', '#E8708E'), 'red': ('#FF9C9C', '#E8707E'),
 'orange': ('#FFCBA4', '#FF9F6B'), 'yellow': ('#FFE8A3', '#F5C451'), 'gold': ('#F5C451', '#E0A23C'),
 'mint': ('#B5EAD7', '#6FCF9F'), 'green': ('#9FE0B5', '#6FCF9F'), 'leaf': ('#C2EBA0', '#8FD06A'),
 'sky': ('#A9D8FF', '#6AA8F0'), 'glass': ('#D6EEFF', '#A9D8FF'), 'blue': ('#8CC0FA', '#6AA8F0'),
 'lilac': ('#CDB8F6', '#9F86E0'), 'purple': ('#B39AEE', '#8C70D6'), 'coffee': ('#A87452', '#82563A'),
 'tan': ('#F2D2B0', '#D9A47A'), 'bread': ('#F7CC8E', '#E3A45E'), 'broth': ('#FFDB8A', '#F2B860'),
 'wood': ('#D9A47A', '#B98058'), 'grey': ('#E3DDF0', '#A79FC0'), 'steel': ('#CFC8E0', '#A79FC0'), 'teal': ('#A6E6E0', '#6CC9C0'), 'navy': ('#8A8AD0', '#6666AA'), 'chili': ('#FF8A8A', '#E06070'), 'dark': ('#3B3355', '#3B3355'),
 'meat': ('#F2A08A', '#D9826E'), 'skin': ('#FFDCC0', '#F2B896'),
}
class SVG:
    def __init__(s): s.parts = []; s.n = 0
    def shape(s, el, m, band=4, ol=True, hl=None):
        """el: phần tử SVG không có fill (vd '<rect x=.. />'); band: độ dày dải đậm; hl: (x,y,w,h) chấm trắng"""
        lt, dk = TONE[m]; s.n += 1; cid = f'c{s.n}'
        body = el.replace('/>', f' fill="{lt}"/>')
        g = [f'<clipPath id="{cid}">{el}</clipPath>', body]
        if band:
            g.append(f'<g clip-path="url(#{cid})"><g fill="{dk}">{el}</g>'
                     f'<g fill="{lt}" transform="translate({-band},{-band})">{el}</g></g>')
        if hl:
            x, y, w, h = hl; g.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="#FFFFFF" clip-path="url(#{cid})"/>')
        if ol: g.append(el.replace('/>', f' fill="none" stroke="{OUT}" stroke-width="2" stroke-linejoin="round"/>'))
        s.parts.append(''.join(g)); return s
    def line(s, d, color=OUT, w=2, cap='round'):
        s.parts.append(f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{w}" stroke-linecap="{cap}" stroke-linejoin="round"/>'); return s
    def tube(s, d, m, w=4):
        """nét có viền: viền đậm rộng w+4, lõi màu rộng w"""
        s.line(d, OUT, w + 4); s.line(d, TONE[m][0], w); return s
    def raw(s, x): s.parts.append(x); return s
    def svg(s): return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="512" height="512">' + ''.join(s.parts) + '</svg>'

def rect(x, y, w, h, r=0): return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}"/>'
def circ(cx, cy, r): return f'<circle cx="{cx}" cy="{cy}" r="{r}"/>'
def ell(cx, cy, rx, ry): return f'<ellipse cx="{cx}" cy="{cy}" rx="{rx}" ry="{ry}"/>'
def poly(pts): return '<polygon points="' + ' '.join(f'{x},{y}' for x, y in pts) + '"/>'
def path(d, rule='nonzero'): return f'<path d="{d}" fill-rule="{rule}"/>'
