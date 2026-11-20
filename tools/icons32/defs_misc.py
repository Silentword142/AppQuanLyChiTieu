import math

from engine import Icon
from reg import icon


# ===================== HỌC TẬP =====================
@icon("book")
def book():
    i = Icon()
    i.R(6, 3, 27, 28, "paper", mode="cylx")
    i.R(4, 2, 24, 27, "blue", r=1)
    i.R(4, 2, 7, 27, "navy", mode="cylx")
    i.R(11, 7, 21, 11, "butter", mode="flat", tone=4)
    i.P([(18, 2), (22, 2), (22, 9), (20, 7), (18, 9)], "red", mode="flat")
    i.R(5, 27, 25, 29, "paper", mode="cyly")
    return i


@icon("book-open")
def bookopen():
    i = Icon()
    i.P([(1, 8), (8, 6), (16, 9), (16, 27), (8, 25), (1, 27)], "paper", mode="cylx", c=(9, 17, 8, 10))
    i.P([(31, 8), (24, 6), (16, 9), (16, 27), (24, 25), (31, 27)], "paper", mode="cylx", c=(23, 17, 8, 10))
    for y in (12, 15, 18, 21):
        i.L(4, y, 13, y + 1, "slate", w=1, mode="flat", tone=4, noline=True)
        i.L(19, y + 1, 28, y, "slate", w=1, mode="flat", tone=4, noline=True)
    i.L(16, 9, 16, 27, "crust", w=1, mode="flat", noline=True)
    i.P([(0, 27), (16, 29), (32, 27), (32, 29), (16, 31), (0, 29)], "rose", mode="flat")
    return i


@icon("file")
def file():
    i = Icon()
    i.P([(5, 2), (21, 2), (27, 8), (27, 29), (5, 29)], "paper", mode="bevel")
    i.P([(21, 2), (27, 8), (21, 8)], "sand", mode="flat", tone=2)
    i.R(8, 5, 17, 8, "sky", mode="flat")
    for y in (12, 16, 20, 24):
        i.L(9, y, 23 if y != 24 else 17, y, "slate", w=1, mode="flat", tone=4, noline=True)
    return i


@icon("graduation")
def graduation():
    i = Icon()
    i.R(9, 13, 23, 21, "dark", mode="cylx", r=2)
    i.P([(16, 4), (31, 11), (16, 17), (1, 11)], "dark", mode="sphere", c=(16, 10, 15, 6))
    i.E(16, 10.5, 2, 1.4, "gold")
    i.L(16, 10.5, 25, 14, "gold", w=1.2, mode="flat", noline=True); i.L(25, 14, 25, 22, "gold", w=1.4, mode="flat", noline=True)
    i.R(24, 22, 26, 25, "gold")
    return i


@icon("school")
def school():
    i = Icon()
    i.R(13, 2, 14, 7, "darkwood", mode="flat"); i.P([(14, 2), (20, 3.5), (14, 5)], "red", mode="flat")
    i.P([(9, 12), (16, 6), (23, 12)], "brick")
    i.R(10, 12, 22, 29, "cream")
    i.R(2, 15, 10, 29, "brick"); i.R(22, 15, 30, 29, "brick")
    i.E(16, 14, 2.2, 2.2, "gold")
    i.R(14, 22, 18, 29, "wood")
    for x in (4, 7, 24, 27):
        i.R(x, 18, x + 1, 21, "glass", mode="flat", tone=4)
    return i


@icon("lamp")
def lamp():
    i = Icon()
    i.E(16, 28.5, 8, 2, "teal", mode="cyly")
    i.L(16, 28, 10, 17, "steel", w=2); i.L(10, 17, 17, 9, "steel", w=2)
    i.P([(13, 4), (24, 9), (22, 15), (11, 10)], "teal", mode="sphere", c=(18, 9, 7, 6))
    i.P([(22, 15), (29, 25), (11, 10)], "yellow", mode="flat", tone=5, noline=True)
    i._erase([(x, y) for y in range(32) for x in range(32) if i.obj[y][x] >= 0 and i.objs[i.obj[y][x]]["mat"] == "yellow" and (x + y) % 2])
    return i


@icon("pen")
def pen():
    i = Icon()
    i.L(9, 23, 25, 7, "navy", w=6)
    i.L(21, 11, 25, 7, "gold", w=6, mode="flat", tone=3)
    i.P([(4, 28), (7, 21), (11, 25)], "gold")
    i.D([(6, 25), (7, 24)], "dark", 3)
    i.L(13, 15, 20, 8, "silver", w=1.2, mode="flat", noline=True)
    return i


@icon("pencil")
def pencil():
    i = Icon()
    i.L(9, 23, 23, 9, "yellow", w=7)
    i.L(22, 10, 25, 7, "steel", w=7)
    i.L(25, 7, 27, 5, "pink", w=7)
    i.P([(3, 29), (6, 21), (11, 26)], "bread")
    i.P([(3, 29), (4.5, 25), (7, 27.5)], "dark", mode="flat")
    i.L(10, 20, 20, 10, "gold", w=1, mode="flat", tone=2, noline=True)
    return i


@icon("globe")
def globe():
    i = Icon()
    i.L(26, 6, 26, 27, "darkwood", w=2); i.E(16, 29, 7, 1.8, "wood", mode="cyly"); i.R(15, 27, 17, 29, "wood")
    i.E(15, 15, 11, 11, "water")
    i.P([(8, 8), (14, 6), (16, 11), (12, 15), (13, 20), (9, 18), (6, 13)], "leaf", mode="sphere", c=(15, 15, 11, 11))
    i.P([(18, 13), (24, 11), (25, 18), (21, 24), (18, 19)], "green", mode="sphere", c=(15, 15, 11, 11))
    i.G([(9, 7), (8, 8)])
    return i


# ===================== GIẢI TRÍ =====================
@icon("guitar")
def guitar():
    i = Icon()
    i.L(16, 15, 27, 3, "darkwood", w=3)
    i.R(25, 1, 30, 6, "cocoa", r=1)
    i.E(10, 23, 7, 7, "orange"); i.E(15, 17, 5, 5, "orange")
    i.E(12, 20, 2.4, 2.4, "cocoa", mode="flat")
    i.L(8, 26, 12, 22, "dark", w=2, mode="flat")
    for k in range(3):
        i.L(9 + k, 24 + k * 0, 26 + k * 0.3, 4 + k, "silver", w=0.6, mode="flat", noline=True) if False else None
    i.L(12, 20, 27, 4, "silver", w=0.8, mode="flat", tone=4, noline=True)
    return i


@icon("dice")
def dice():
    i = Icon()
    i.R(13, 2, 29, 18, "white", r=3)
    for p in ((17, 6), (24, 13), (20, 9)):
        i.E(p[0] + 0.5, p[1] + 0.5, 1.6, 1.6, "red", mode="flat")
    i.R(3, 13, 19, 29, "white", r=3)
    for p in ((6, 16), (15, 16), (6, 25), (15, 25), (10.5, 20.5)):
        i.E(p[0] + 0.5, p[1] + 0.5, 1.6, 1.6, "navy", mode="flat")
    return i


@icon("party")
def party():
    i = Icon()
    i.P([(2, 29), (10, 12), (19, 21)], "purple", mode="cylx")
    i.L(6, 22, 12, 26, "yellow", w=2, mode="flat", noline=True); i.L(8, 16, 15, 22, "pink", w=2, mode="flat", noline=True)
    for (x, y, m) in ((18, 6, "yellow"), (24, 10, "sky"), (22, 3, "pink"), (28, 16, "lime"), (14, 4, "orange"), (26, 22, "red"), (20, 13, "mint")):
        i.R(x, y, x + 1, y + 1, m, mode="flat", tone=4)
    i.L(13, 14, 18, 8, "rose", w=1.2, mode="flat", noline=True); i.L(17, 19, 26, 15, "sky", w=1.2, mode="flat", noline=True)
    return i


@icon("palette")
def palette():
    i = Icon()
    i.E(15, 16, 13.5, 11.5, "wood"); i.HE(21, 22, 3, 2.6)
    for (x, y, m) in ((9, 11, "red"), (15, 8, "yellow"), (21, 10, "lime"), (8, 18, "sky"), (13, 22, "purple")):
        i.E(x, y, 2.6, 2.4, m)
    i.L(30, 2, 21, 15, "darkwood", w=2); i.E(20, 16, 1.8, 1.8, "pink")
    return i


@icon("film")
def film():
    i = Icon()
    i.P([(3, 8), (27, 3), (28, 7), (4, 12)], "white", mode="flat", tone=4)
    for k in range(4):
        x = 6 + k * 6
        i.P([(x, 7.5 - k * 1.2), (x + 3, 7 - k * 1.2), (x + 5, 10.5 - k * 1.2), (x + 2, 11 - k * 1.2)], "black", mode="flat")
    i.R(3, 13, 28, 28, "dark", r=1)
    for k in range(4):
        i.R(5 + k * 6, 14, 7 + k * 6, 16, "white", mode="flat", tone=4)
    i.R(6, 19, 25, 25, "teal", mode="flat", tone=2)
    return i


@icon("music")
def music():
    i = Icon()
    i.R(11, 4, 13, 22, "purple", mode="cylx"); i.R(23, 2, 25, 19, "purple", mode="cylx")
    i.P([(11, 4), (25, 1), (25, 7), (11, 10)], "purple")
    i.E(9, 23, 4.5, 3.6, "lav"); i.E(21, 20, 4.5, 3.6, "lav")
    return i


@icon("vinyl")
def vinyl():
    i = Icon()
    i.E(16, 16, 14, 14, "black")
    for r in (11, 8):
        i.ring(16, 16, r, 0.7, "dark", noline=True)
    i.E(16, 16, 5, 5, "red"); i.E(16, 16, 1.2, 1.2, "black", mode="flat")
    i.D([(9, 7), (8, 8), (7, 9), (10, 6)], "slate", 5)
    return i


@icon("disco")
def disco():
    i = Icon()
    i.L(16, 0, 16, 4, "dark", w=1.2)
    i.E(16, 16, 12, 12, "silver")
    for y in range(5, 28, 3):
        i.L(4, y, 28, y, "steel", w=1, mode="flat", tone=2, noline=True)
    for x in range(6, 28, 3):
        i.L(x, 4, x, 28, "steel", w=1, mode="flat", tone=2, noline=True)
    i._erase([(x, y) for y in range(32) for x in range(32) if math.hypot(x + 0.5 - 16, y + 0.5 - 16) > 12])
    for (x, y, m) in ((10, 9, "pink"), (19, 12, "sky"), (13, 18, "butter"), (22, 20, "lav"), (9, 21, "mint")):
        i.R(x, y, x + 1, y + 1, m, mode="flat", tone=4)
    i.D([(27, 4), (28, 3), (29, 4), (28, 5), (4, 27), (3, 28), (5, 28), (4, 29)], "yellow", 4)
    return i


@icon("paw")
def paw():
    i = Icon()
    i.E(16, 21, 8, 7, "cocoa")
    for (x, y) in ((7, 12), (12, 6), (20, 6), (25, 12)):
        i.E(x, y, 3.2, 3.8, "cocoa")
    i.E(16, 22, 4, 3, "pink", mode="flat", tone=3)
    return i


@icon("cat")
def cat():
    i = Icon()
    i.P([(4, 3), (13, 9), (5, 15)], "orange"); i.P([(28, 3), (19, 9), (27, 15)], "orange")
    i.D([(6, 7), (7, 8), (6, 9), (26, 7), (25, 8), (26, 9)], "pink", 3)
    i.E(16, 18, 13, 11, "orange")
    for y in (11, 14):
        i.L(13, y, 19, y, "tomato", w=1, mode="flat", tone=2, noline=True)
    i.E(11, 18, 2.6, 3, "black", mode="flat", tone=3); i.E(21, 18, 2.6, 3, "black", mode="flat", tone=3)
    i.D([(10, 17), (20, 17)], "#FFFFFF")
    i.D([(15, 22), (16, 22), (17, 22), (16, 23)], "rose", 3)
    i.L(2, 22, 9, 22, "white", w=0.8, mode="flat", tone=4, noline=True); i.L(23, 22, 30, 22, "white", w=0.8, mode="flat", tone=4, noline=True)
    return i


@icon("dog")
def dog():
    i = Icon()
    i.E(16, 17, 11, 11, "bread")
    i.E(5, 14, 4, 8, "crust"); i.E(27, 14, 4, 8, "crust")
    i.E(16, 22, 6, 5, "cream")
    i.E(16, 19.5, 2.4, 1.8, "black")
    i.D([(11, 14), (11, 15), (21, 14), (21, 15)], "black", 3)
    i.D([(16, 25), (16, 26), (17, 26)], "rose", 3)
    i.E(22, 10, 3, 2.4, "crust", mode="flat")
    return i


@icon("star")
def star():
    i = Icon()
    pts = []
    for k in range(10):
        r = 14 if k % 2 == 0 else 6
        a = math.radians(-90 + k * 36)
        pts.append((16 + r * math.cos(a), 17 + r * math.sin(a)))
    i.P(pts, "yellow", mode="sphere", c=(16, 16, 12, 12))
    i.G([(12, 12), (13, 11)])
    return i


# ===================== GIA ĐÌNH & KHÁC =====================
def person(i, cx, cy, shirt, hair="cocoa", s=1.0):
    i.E(cx, cy + 10 * s, 8 * s, 6 * s, shirt)
    i.HR(0, int(cy + 13 * s), 31, 31)
    i.E(cx, cy, 5.5 * s, 5.5 * s, "skin", tone=4)
    i.E(cx, cy - 3 * s, 5.8 * s, 3.2 * s, hair, mode="flat", tone=3)


@icon("user")
def user():
    i = Icon()
    i.P([(5, 30), (8, 22), (16, 19), (24, 22), (27, 30)], "blue", mode="sphere", c=(16, 26, 11, 6))
    i.E(16, 12, 7, 7.5, "skin", tone=4)
    i.P([(9, 11), (10, 5), (16, 3), (22, 5), (23, 11), (20, 8), (13, 8)], "cocoa", mode="sphere", c=(16, 7, 7, 5))
    i.D([(13, 13), (19, 13)], "black", 3); i.D([(15, 16), (16, 16), (17, 16)], "rose", 3)
    return i


@icon("users")
def users():
    i = Icon()
    i.P([(14, 30), (16, 22), (23, 19), (30, 22), (31, 30)], "mint", mode="sphere", c=(23, 26, 9, 6))
    i.E(23, 13, 5.5, 6, "skin", tone=4); i.E(23, 9, 6, 3, "choc", mode="flat")
    i.P([(1, 30), (3, 23), (10, 20), (17, 23), (19, 30)], "pink", mode="sphere", c=(10, 26, 9, 6))
    i.E(10, 14, 5.5, 6, "skin", tone=4); i.P([(4, 16), (5, 9), (10, 7), (15, 9), (16, 16), (14, 12), (6, 12)], "darkwood", mode="sphere", c=(10, 11, 6, 5))
    i.D([(8, 15), (12, 15), (21, 14), (25, 14)], "black", 3)
    return i


@icon("mail")
def mail():
    i = Icon()
    i.R(2, 7, 29, 25, "paper")
    i.P([(2, 7), (29, 7), (16, 18)], "sand", mode="flat", tone=4)
    i.L(2, 25, 12, 16, "sand", w=1, mode="flat", tone=2, noline=True); i.L(29, 25, 20, 16, "sand", w=1, mode="flat", tone=2, noline=True)
    i.E(16, 17, 3.2, 3, "red")
    i.R(23, 9, 27, 13, "sky", mode="flat")
    return i


@icon("envelope-red")
def envelopered():
    i = Icon()
    i.R(5, 2, 26, 29, "red", r=1)
    i.P([(5, 2), (26, 2), (26, 11), (16, 16), (5, 11)], "red", mode="flat", tone=4)
    i.E(16, 15, 4.5, 4.5, "gold"); i.R(14, 13, 17, 16, "red", mode="flat", tone=2)
    for y in (21, 25):
        for x in (8, 12, 16, 20, 24):
            i.D([(x, y)], "gold", 4)
    return i


@icon("pagoda")
def pagoda():
    i = Icon()
    i.R(15, 0, 16, 4, "gold", mode="flat")
    for (y, w) in ((4, 7), (11, 10), (18, 13)):
        i.R(16 - w + 3, y + 3, 16 + w - 4, y + 7, "cream")
        i.P([(16 - w - 2, y + 4), (16 - w, y + 2), (16, y - 1), (16 + w, y + 2), (16 + w + 2, y + 4), (16 + w - 2, y + 4.5), (16 - w + 2, y + 4.5)], "red", mode="sphere", c=(16, y + 2, w, 3))
    i.R(4, 26, 27, 29, "sand")
    i.R(13, 23, 18, 29, "darkwood")
    return i


@icon("flag")
def flag():
    i = Icon()
    i.R(4, 2, 6, 30, "steel", mode="cylx"); i.E(5, 2, 1.8, 1.8, "gold")
    i.P([(6, 4), (16, 6), (28, 4), (28, 18), (16, 20), (6, 18)], "red", mode="sphere", c=(17, 11, 11, 8))
    pts = []
    for k in range(10):
        r = 4.5 if k % 2 == 0 else 1.9
        a = math.radians(-90 + k * 36)
        pts.append((17 + r * math.cos(a), 12 + r * math.sin(a)))
    i.P(pts, "yellow", mode="flat", tone=4)
    return i


@icon("contact")
def contact():
    i = Icon()
    i.R(2, 5, 29, 27, "paper", r=2)
    i.R(2, 5, 29, 9, "sky", mode="flat")
    i.R(5, 12, 13, 23, "lav", r=1)
    i.E(9, 15.5, 2.4, 2.4, "skin", tone=4); i.E(9, 21, 3.6, 2.4, "blue")
    for y in (13, 17, 21):
        i.L(16, y, 26, y, "slate", w=1.4, mode="flat", tone=4, noline=True)
    return i


@icon("google")
def google():
    i = Icon()
    i.ring(16, 16, 12, 4.4, "red", mode="flat")
    i.W(16, 16, 12, 135, 225, "yellow", mode="flat"); i.W(16, 16, 12, 45, 135, "green", mode="flat"); i.W(16, 16, 12, 345, 45, "blue", mode="flat")
    i.HE(16, 16, 7.6, 7.6)
    i.HR(19, 8, 31, 14)
    i.R(16, 14, 28, 18, "blue", mode="flat")
    return i
