"""18 icon vẽ lại bằng tay (đợt anh Hoàng chọn): số theo bảng 173 icon của bộ R."""
import math
from px64 import Icon
I = {}
def icon(num):
    def deco(f): I[f"{num:03d} {f.__name__.replace('_', '-')}"] = f; return f
    return deco

def rot(pts, cx, cy, a):
    ca, sa = math.cos(a), math.sin(a)
    return [(cx + (x - cx) * ca - (y - cy) * sa, cy + (x - cx) * sa + (y - cy) * ca) for x, y in pts]

def star_pts(cx, cy, R, r, n=5, a0=-math.pi / 2):
    return [(cx + (R if i % 2 == 0 else r) * math.cos(a0 + i * math.pi / n),
             cy + (R if i % 2 == 0 else r) * math.sin(a0 + i * math.pi / n)) for i in range(2 * n)]

@icon(8)
def alert_circle():
    c = Icon()
    c.ell(32, 32, 27, 27, 'red', kind='round')
    c.rect(28, 13, 36, 38, 'cream', r=4, kind='soft')
    c.ell(32, 47, 4.5, 4.5, 'cream', kind='round')
    return c

@icon(20)
def bell_off():
    c = Icon()
    c.ell(32, 9, 4, 4, 'gold', kind='round')
    c.poly([(14, 46), (17, 26), (22, 15), (32, 11), (42, 15), (47, 26), (50, 46)], 'gold', kind='round')
    c.rect(9, 44, 55, 51, 'orange', r=3, kind='soft')
    c.ell(32, 55, 5, 4, 'orange', kind='round')
    c.line([(10, 10), (54, 56)], 'red', w=6, kind='soft')
    return c

@icon(106)
def non_la():
    c = Icon()
    c.line([(16, 45), (32, 58), (48, 45)], 'rose', w=2, kind='flat')          # quai
    c.poly([(32, 7), (36, 12), (46, 26), (56, 38), (61, 44), (32, 49), (3, 44), (8, 38), (18, 26), (28, 12)], 'bread', kind='soft')
    for y, half in ((20, 8), (29, 15), (38, 22)):                              # vành lá
        c.line([(32 - half, y + 3), (32, y), (32 + half, y + 3)], 'wood', w=1.6, kind='flat', ol=False)
    c.ell(32, 45, 29, 4.5, 'tan', kind='soft')                                # mép nón
    return c

@icon(116)
def percent():
    c = Icon()
    c.line([(47, 9), (17, 55)], 'rose', w=8, kind='soft')
    c.ell(18, 18, 10, 10, 'rose', kind='round'); c.hole_ell(18, 18, 4, 4)
    c.ell(46, 46, 10, 10, 'rose', kind='round'); c.hole_ell(46, 46, 4, 4)
    return c

@icon(124)
def plug():
    c = Icon()
    c.line([(32, 44), (32, 52), (26, 58), (18, 58)], 'dark', w=5, kind='round')   # dây
    c.rect(22, 4, 27, 20, 'gold', r=1, kind='soft'); c.rect(37, 4, 42, 20, 'gold', r=1, kind='soft')  # chân
    c.rect(12, 16, 52, 40, 'steel', r=6, kind='soft')                            # thân
    c.rect(24, 38, 40, 47, 'grey', r=3, kind='soft')                             # cổ
    c.rect(18, 26, 46, 29, 'navy', r=1, kind='flat', ol=False)
    c.ell(32, 33, 2.5, 2.5, 'mint', kind='round', ol=False)
    return c

@icon(129)
def road():
    c = Icon()
    c.ell(32, 36, 30, 25, 'leaf', kind='round')
    c.poly([(28, 12), (36, 12), (56, 58), (8, 58)], 'dark', kind='flat')
    c.rect(6, 56, 58, 61, 'green', r=2, kind='soft')
    for y0, y1, w in ((14, 19, 1.4), (24, 31, 2), (37, 47, 2.6)):
        c.rect(32 - w, y0, 32 + w, y1, 'yellow', kind='flat', ol=False)
    c.line([(29, 13), (12, 56)], 'white', w=1, kind='flat', ol=False)
    c.line([(35, 13), (52, 56)], 'white', w=1, kind='flat', ol=False)
    return c

@icon(136)
def settings():
    c = Icon()
    teeth = []
    for k in range(8):
        teeth += [rot([(27, 2), (37, 2), (38, 14), (26, 14)], 32, 32, k * math.pi / 4)]
    for t in teeth: c.poly(t, 'steel', kind='soft')
    c.ell(32, 32, 22, 22, 'steel', kind='round')
    c.ell(32, 32, 11, 11, 'grey', kind='soft'); c.hole_ell(32, 32, 6, 6)
    return c

@icon(139)
def ship():
    c = Icon()
    c.rect(36, 10, 43, 24, 'red', kind='soft'); c.rect(36, 13, 43, 16, 'white', kind='flat', ol=False)   # ống khói
    c.rect(18, 22, 46, 36, 'white', r=2, kind='soft')                         # cabin
    for x in (23, 31, 39): c.ell(x + 1, 29, 2.6, 2.6, 'sky', kind='round', ol=False)
    c.poly([(4, 35), (60, 35), (52, 50), (12, 50)], 'navy', kind='soft')      # thân tàu
    c.rect(7, 35, 57, 39, 'red', kind='flat', ol=False)
    c.line([(4, 54), (12, 51), (20, 54), (28, 51), (36, 54), (44, 51), (52, 54), (60, 51)], 'sky', w=3, kind='soft')
    return c

@icon(142)
def sofa():
    c = Icon()
    c.rect(11, 50, 16, 58, 'brown', kind='soft'); c.rect(48, 50, 53, 58, 'brown', kind='soft')
    c.rect(10, 14, 54, 38, 'teal', r=6, kind='soft')                          # lưng
    c.rect(12, 34, 52, 52, 'teal', r=3, kind='soft')                          # đệm ngồi
    c.rect(14, 36, 31, 44, 'mint', r=3, kind='soft'); c.rect(33, 36, 50, 44, 'mint', r=3, kind='soft')
    c.rect(3, 28, 15, 53, 'teal', r=5, kind='soft'); c.rect(49, 28, 61, 53, 'teal', r=5, kind='soft')  # tay vịn
    c.rect(20, 19, 30, 29, 'pink', r=3, kind='soft')                          # gối
    return c

@icon(143)
def sparkles():
    c = Icon()
    c.poly(star_pts(26, 36, 23, 6, 4), 'gold', kind='round')
    c.poly(star_pts(49, 14, 11, 3, 4), 'yellow', kind='round')
    c.poly(star_pts(51, 50, 8, 2.4, 4), 'pink', kind='round')
    return c

@icon(145)
def star():
    c = Icon()
    c.poly(star_pts(32, 34, 30, 14), 'gold', kind='round')
    c.ell(26, 34, 2, 3, 'dark', kind='flat', ol=False); c.ell(38, 34, 2, 3, 'dark', kind='flat', ol=False)
    c.dot(25, 32, '#FFFFFF'); c.dot(37, 32, '#FFFFFF')
    c.ell(21, 40, 2.5, 1.5, 'coral', kind='flat', ol=False); c.ell(43, 40, 2.5, 1.5, 'coral', kind='flat', ol=False)
    return c

@icon(146)
def stethoscope():
    c = Icon()
    c.line([(16, 8), (16, 26), (20, 34), (26, 38), (32, 38), (38, 38), (44, 34), (48, 26), (48, 8)], 'navy', w=4, kind='round')
    c.ell(16, 7, 3.5, 3, 'grey', kind='round'); c.ell(48, 7, 3.5, 3, 'grey', kind='round')
    c.line([(32, 38), (32, 46), (36, 52), (44, 54)], 'navy', w=4, kind='round')
    c.ell(48, 52, 9, 9, 'steel', kind='round'); c.ell(48, 52, 5, 5, 'sky', kind='round')
    return c

@icon(149)
def syringe():
    c = Icon()
    a = -math.pi / 4
    def P(pts): return rot(pts, 32, 32, a)
    c.line(P([(32, 2), (32, 12)]), 'steel', w=2, kind='flat')                  # kim
    c.poly(P([(28, 50), (36, 50), (36, 56), (28, 56)]), 'grey', kind='soft')  # cần
    c.poly(P([(22, 56), (42, 56), (42, 61), (22, 61)]), 'grey', kind='soft')  # nút đẩy
    c.poly(P([(20, 46), (44, 46), (44, 51), (20, 51)]), 'steel', kind='soft') # tai cầm
    c.poly(P([(29, 10), (35, 10), (35, 15), (29, 15)]), 'steel', kind='soft')
    c.poly(P([(24, 14), (40, 14), (40, 47), (24, 47)]), 'glass', kind='soft') # ống
    c.poly(P([(25, 28), (39, 28), (39, 46), (25, 46)]), 'mint', kind='soft', ol=False)  # thuốc
    for y in (20, 26, 32, 38): c.line(P([(25, y), (30, y)]), 'navy', w=1, kind='flat', ol=False)
    return c

@icon(159)
def trend_down():
    c = Icon()
    c.rect(4, 6, 60, 58, 'paper', r=4, kind='flat')
    c.line([(10, 52), (56, 52)], 'grey', w=2, kind='flat', ol=False)
    c.line([(10, 12), (10, 52)], 'grey', w=2, kind='flat', ol=False)
    c.line([(12, 14), (24, 26), (32, 20), (46, 40)], 'red', w=5, kind='soft')
    c.poly([(50, 49), (37, 44), (51, 34)], 'red', kind='soft')
    return c

@icon(163)
def upload():
    c = Icon()
    c.poly([(6, 34), (14, 34), (14, 50), (50, 50), (50, 34), (58, 34), (58, 58), (6, 58)], 'navy', kind='soft')
    c.poly([(32, 4), (52, 24), (40, 24), (40, 44), (24, 44), (24, 24), (12, 24)], 'mint', kind='soft')
    return c

@icon(164)
def user():
    c = Icon()
    c.ell(32, 62, 24, 17, 'blue', kind='round')                                 # vai
    c.rect(28, 36, 36, 46, 'skin', kind='soft')                                 # cổ
    c.ell(32, 26, 15, 16, 'skin', kind='round')                                 # đầu
    c.poly([(16, 26), (17, 15), (24, 9), (32, 8), (40, 9), (47, 15), (48, 26), (44, 18), (34, 16), (24, 19), (19, 22)], 'coffee', kind='soft')  # tóc
    c.ell(26, 29, 1.8, 2.5, 'dark', kind='flat', ol=False); c.ell(38, 29, 1.8, 2.5, 'dark', kind='flat', ol=False)
    c.ell(22, 34, 2.5, 1.4, 'pink', kind='flat', ol=False); c.ell(42, 34, 2.5, 1.4, 'pink', kind='flat', ol=False)
    c.line([(29, 36), (32, 38), (35, 36)], 'red', w=1.6, kind='flat', ol=False)
    return c

@icon(172)
def yoga():
    c = Icon()
    c.rect(2, 54, 62, 60, 'lilac', r=3, kind='soft')                            # thảm
    c.ell(32, 49, 22, 7, 'purple', kind='round')                                # chân xếp bằng
    c.ell(20, 50, 4, 3, 'skin', kind='round'); c.ell(44, 50, 4, 3, 'skin', kind='round')
    c.poly([(23, 46), (25, 24), (39, 24), (41, 46)], 'pink', kind='soft')       # thân
    c.line([(25, 26), (16, 36), (21, 46)], 'skin', w=4, kind='round')           # tay
    c.line([(39, 26), (48, 36), (43, 46)], 'skin', w=4, kind='round')
    c.ell(32, 15, 9, 9, 'skin', kind='round')                                   # đầu
    c.ell(32, 6, 4, 3.5, 'coffee', kind='round')                                # búi tóc
    c.poly([(23, 15), (25, 8), (32, 6), (39, 8), (41, 15), (37, 11), (27, 11)], 'coffee', kind='soft')
    c.line([(27, 17), (29, 18)], 'dark', w=1, kind='flat', ol=False); c.line([(35, 18), (37, 17)], 'dark', w=1, kind='flat', ol=False)
    return c

@icon(173)
def zap():
    c = Icon()
    c.poly([(38, 2), (12, 36), (30, 36), (24, 62), (52, 26), (34, 26)], 'yellow', kind='soft')
    return c
