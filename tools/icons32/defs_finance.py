from engine import Icon
from reg import icon

# chữ số / ký hiệu nhỏ dạng điểm ảnh
DONG = [(1, 0), (2, 0), (3, 0), (2, 1), (0, 2), (1, 2), (2, 2), (0, 3), (2, 3), (0, 4), (1, 4), (2, 4), (0, 6), (1, 6), (2, 6)]
DOLLAR = [(1, 0), (0, 1), (1, 1), (2, 1), (0, 2), (1, 3), (2, 4), (0, 5), (1, 5), (2, 5), (1, 6)]


def glyph(i, pts, ox, oy, mat, tone):
    i.D([(ox + x, oy + y) for x, y in pts], mat, tone)


def coin(i, cx, cy, r, mat="gold"):
    i.E(cx, cy, r, r, mat)
    i.ring(cx, cy, r - 1.2, 1, mat, tone=2, noline=True)


@icon("coins")
def coins():
    i = Icon()
    for y in (27, 23, 19, 15):
        i.R(3, y - 1, 21, y + 1, "gold", mode="cylx", tone=2, sep=True)
        i.E(12, y - 1.5, 9, 2.8, "yellow", mode="cyly", sep=True)
    coin(i, 23, 18, 7.5)
    glyph(i, DONG, 22, 14, "gold", 1)
    i.G([(18, 13), (19, 12), (8, 12)])
    return i


@icon("salary")
def salary():
    i = Icon()
    i.R(5, 6, 29, 20, "green", r=1)
    i.R(2, 10, 26, 25, "green", r=1)
    i.E(14, 17.5, 4.5, 4.5, "mint")
    glyph(i, DONG, 13, 14, "green", 1)
    i.D([(4, 12), (24, 12), (4, 23), (24, 23)], "lime", 4)
    i.R(9, 9, 12, 26, "gold", mode="cylx")
    return i


@icon("hand-coins")
def handcoins():
    i = Icon()
    coin(i, 12, 8, 5.5); coin(i, 21, 11, 4.5, "yellow")
    glyph(i, DONG, 11, 5, "gold", 1)
    i.P([(2, 20), (8, 17), (16, 18), (22, 17), (29, 14), (30, 18), (22, 25), (12, 27), (2, 26)], "skin", mode="sphere", c=(16, 21, 14, 6))
    i.L(9, 21, 17, 21, "skin", w=1, mode="flat", tone=1, noline=True)
    i.R(0, 19, 4, 28, "blue", mode="cylx")
    return i


@icon("safe")
def safe():
    i = Icon()
    i.R(3, 4, 28, 27, "iron", r=2)
    i.R(6, 7, 25, 24, "steel", r=1)
    i.E(15, 15.5, 5.5, 5.5, "silver"); i.E(15, 15.5, 2.4, 2.4, "iron")
    for x, y in ((15, 10), (20, 15), (15, 21), (10, 15)):
        i.D([(x, y)], "dark", 3)
    i.R(22, 12, 24, 19, "gold", mode="cylx")
    i.R(5, 27, 9, 29, "dark", mode="flat"); i.R(22, 27, 26, 29, "dark", mode="flat")
    return i


@icon("gem")
def gem():
    i = Icon()
    i.P([(9, 6), (23, 6), (29, 12), (3, 12)], "sky", mode="bevel")
    i.P([(3, 12), (29, 12), (16, 28)], "blue", mode="cylx")
    i.L(12, 6, 9, 12, "sky", w=1, mode="flat", tone=5, noline=True); i.L(20, 6, 23, 12, "sky", w=1, mode="flat", tone=2, noline=True)
    i.L(9, 12, 16, 27, "blue", w=1, mode="flat", tone=5, noline=True); i.L(23, 12, 16, 27, "blue", w=1, mode="flat", tone=2, noline=True)
    i.G([(10, 8), (11, 8), (10, 9), (6, 13)])
    return i


@icon("crown")
def crown():
    i = Icon()
    i.P([(3, 10), (10, 17), (16, 6), (22, 17), (29, 10), (27, 25), (5, 25)], "gold", mode="sphere", c=(16, 17, 13, 10))
    i.R(5, 22, 27, 26, "gold", mode="cyly", tone=2)
    for x, m in ((10, "red"), (16, "blue"), (22, "red")):
        i.E(x, 23.5, 1.8, 1.8, m)
    i.E(3, 9, 2, 2, "yellow"); i.E(16, 5, 2, 2, "yellow"); i.E(29, 9, 2, 2, "yellow")
    return i


@icon("piggy")
def piggy():
    i = Icon()
    coin(i, 15, 5, 3.6)
    i.E(16, 18, 12, 9, "pink")
    i.P([(9, 11), (11, 5), (15, 10)], "pink")
    i.E(27, 18, 3, 3.6, "rose", mode="sphere")
    i.D([(26, 17), (26, 19)], "cocoa", 2)
    i.D([(21, 14)], "black", 3)
    i.R(13, 10, 19, 11, "cocoa", mode="flat")
    i.R(8, 25, 11, 29, "pink", mode="cylx"); i.R(19, 25, 22, 29, "pink", mode="cylx")
    i.L(3, 16, 5, 14, "pink", w=1.5)
    i.G([(10, 13), (11, 12)])
    return i


@icon("wallet")
def wallet():
    i = Icon()
    i.R(4, 7, 26, 13, "green", r=1); i.R(6, 5, 24, 10, "mint")
    i.R(2, 10, 28, 27, "rose", r=2)
    i.L(4, 13, 18, 13, "pink", w=1, mode="flat", tone=4, noline=True)
    i.R(18, 15, 30, 22, "pink", r=1)
    i.E(23, 18.5, 2, 2, "gold")
    i.D([(5, 24), (8, 24), (11, 24), (14, 24)], "rose", 2)
    return i


@icon("credit-card")
def creditcard():
    i = Icon()
    i.R(2, 7, 29, 25, "purple", r=2)
    i.R(2, 10, 29, 13, "black", mode="flat")
    i.R(5, 15, 10, 19, "gold", r=1)
    i.D([(6, 17), (7, 17), (8, 17), (7, 16), (7, 18)], "gold", 2)
    for x in (5, 6, 8, 9, 11, 12, 14, 15, 17, 18):
        i.D([(x, 22)], "lav", 5)
    i.E(23, 20, 2.5, 2.5, "red"); i.E(26, 20, 2.5, 2.5, "orange")
    return i


@icon("banknote")
def banknote():
    i = Icon()
    i.R(1, 8, 30, 24, "green", r=1)
    i.R(3, 10, 28, 22, "lime", mode="flat", tone=2)
    i.R(4, 11, 27, 21, "green", mode="flat", tone=3)
    i.E(16, 16, 5, 5, "mint")
    glyph(i, DONG, 15, 13, "green", 1)
    for x, y in ((6, 13), (25, 13), (6, 19), (25, 19)):
        i.E(x + 0.5, y + 0.5, 1.4, 1.4, "mint", mode="flat", tone=4)
    return i


@icon("landmark")
def landmark():
    i = Icon()
    i.P([(2, 11), (16, 3), (30, 11)], "sand", mode="bevel")
    i.R(2, 11, 29, 13, "sand", mode="cyly", tone=2)
    for x in (5, 11, 18, 24):
        i.R(x, 14, x + 2, 24, "white", mode="cylx")
    i.R(3, 24, 28, 26, "sand", mode="cyly"); i.R(1, 27, 30, 29, "sand", mode="cyly", tone=2)
    i.E(16, 8, 2, 2, "gold")
    return i


@icon("receipt")
def receipt():
    i = Icon()
    pts = [(5, 2), (27, 2), (27, 28)]
    for k in range(11):
        x = 27 - k * 2
        pts.append((x - 1, 30 if k % 2 == 0 else 28))
    pts.append((5, 28))
    i.P(pts, "paper", mode="bevel")
    for y in (7, 11, 15):
        i.L(9, y, 22, y, "slate", w=1, mode="flat", tone=4, noline=True)
    i.L(9, 20, 15, 20, "slate", w=1, mode="flat", tone=4, noline=True)
    i.L(9, 24, 23, 24, "red", w=2, mode="flat", noline=True)
    i.D([(21, 20), (22, 20), (23, 20)], "green", 2)
    return i


@icon("calculator")
def calculator():
    i = Icon()
    i.R(5, 2, 26, 29, "lav", r=2)
    i.R(8, 5, 23, 10, "mint", mode="flat", tone=2)
    i.D([(19, 7), (20, 7), (21, 7), (21, 8), (19, 8)], "green", 0)
    cols = ["white", "white", "white", "orange"]
    for row in range(4):
        for col in range(4):
            x, y = 8 + col * 4, 13 + row * 4
            m = cols[col] if row < 3 else ("red" if col == 3 else "white")
            i.R(x, y, x + 2, y + 2, m, r=0)
    return i


@icon("percent")
def percent():
    i = Icon()
    i.P([(16, 2), (20, 6), (26, 6), (26, 12), (30, 16), (26, 20), (26, 26), (20, 26), (16, 30), (12, 26), (6, 26), (6, 20), (2, 16), (6, 12), (6, 6), (12, 6)], "orange", mode="sphere", c=(16, 16, 13, 13))
    i.E(11.5, 11.5, 2.6, 2.6, "white", mode="flat", tone=5); i.E(20.5, 20.5, 2.6, 2.6, "white", mode="flat", tone=5)
    i.L(21, 9, 11, 23, "white", w=2.4, mode="flat", tone=5, noline=True)
    return i


@icon("chart")
def chart():
    i = Icon()
    i.R(2, 4, 29, 28, "paper", r=1)
    for y in (10, 16, 22):
        i.L(4, y, 27, y, "lav", w=1, mode="flat", tone=4, noline=True)
    i.R(6, 17, 10, 26, "pink", mode="cylx"); i.R(13, 11, 17, 26, "mint", mode="cylx"); i.R(20, 7, 24, 26, "sky", mode="cylx")
    i.R(4, 26, 27, 27, "dark", mode="flat")
    return i


@icon("pie")
def pie():
    i = Icon()
    i.W(14, 18, 12, 90, 200, "butter", c=(14, 18, 12, 12))
    i.W(14, 18, 12, 200, 270, "lav", c=(14, 18, 12, 12), sep=True)
    i.W(14, 18, 12, 0, 90, "pink", c=(14, 18, 12, 12), sep=True)
    i.W(17, 15, 12, 270, 360, "mint", c=(17, 15, 12, 12))
    i.G([(7, 12), (8, 11)])
    return i


@icon("trend-up")
def trendup():
    i = Icon()
    i.R(2, 4, 29, 28, "paper", r=1)
    for x in (9, 16, 23):
        i.L(x, 6, x, 26, "lav", w=1, mode="flat", tone=4, noline=True)
    i.L(5, 23, 12, 16, "green", w=3); i.L(12, 16, 16, 19, "green", w=3); i.L(16, 19, 24, 10, "green", w=3)
    i.P([(19, 8), (27, 6), (26, 14)], "green")
    return i


@icon("chart-up")
def chartup():
    i = Icon()
    i.R(4, 19, 9, 28, "lav", mode="cylx"); i.R(12, 14, 17, 28, "sky", mode="cylx"); i.R(20, 9, 25, 28, "mint", mode="cylx")
    i.R(2, 28, 29, 29, "dark", mode="flat")
    i.L(4, 15, 13, 9, "orange", w=2.4); i.L(13, 9, 17, 11, "orange", w=2.4); i.L(17, 11, 25, 4, "orange", w=2.4)
    i.P([(21, 3), (28, 1), (27, 8)], "orange")
    return i


@icon("trend-down")
def trenddown():
    i = Icon()
    i.R(2, 4, 29, 28, "paper", r=1)
    for x in (9, 16, 23):
        i.L(x, 6, x, 26, "lav", w=1, mode="flat", tone=4, noline=True)
    i.L(5, 8, 12, 15, "red", w=3); i.L(12, 15, 16, 12, "red", w=3); i.L(16, 12, 23, 20, "red", w=3)
    i.P([(18, 22), (27, 25), (25, 16)], "red")
    return i


@icon("tag")
def tag():
    i = Icon()
    i.P([(17, 2), (29, 2), (29, 14), (14, 29), (2, 17)], "butter", mode="sphere", c=(16, 15, 13, 13))
    i.HE(23.5, 7.5, 2.2, 2.2)
    i.ring(23.5, 7.5, 3.4, 1.2, "gold", noline=True)
    i.L(23.5, 7.5, 30, 1, "rose", w=1.2, mode="flat", noline=True)
    i.L(10, 16, 16, 10, "gold", w=1, mode="flat", tone=2, noline=True); i.L(12, 19, 19, 12, "gold", w=1, mode="flat", tone=2, noline=True)
    return i


@icon("tags")
def tags():
    i = Icon()
    i.P([(13, 1), (24, 1), (24, 12), (11, 25), (1, 15)], "pink", mode="sphere", c=(12, 13, 11, 11))
    i.P([(19, 6), (30, 6), (30, 17), (17, 30), (7, 20)], "butter", mode="sphere", c=(18, 18, 12, 12))
    i.HE(25.5, 11, 2, 2)
    i.ring(25.5, 11, 3.2, 1.1, "gold", noline=True)
    i.L(14, 21, 21, 14, "gold", w=1, mode="flat", tone=2, noline=True)
    return i


@icon("briefcase")
def briefcase():
    i = Icon()
    i.R(11, 4, 20, 9, "cocoa", r=1); i.HR(13, 6, 18, 9)
    i.R(2, 8, 29, 27, "coffee", r=2)
    i.R(2, 15, 29, 17, "darkwood", mode="flat")
    i.R(13, 13, 18, 19, "gold", r=1)
    i.D([(15, 16), (16, 16)], "cocoa", 2)
    return i


@icon("handshake")
def handshake():
    i = Icon()
    i.R(0, 11, 6, 22, "blue", mode="cylx"); i.R(25, 11, 31, 22, "orange", mode="cylx")
    i.P([(6, 12), (14, 9), (20, 13), (26, 13), (26, 20), (17, 25), (12, 24), (6, 21)], "skin", mode="sphere", c=(16, 17, 10, 7))
    i.P([(12, 15), (19, 12), (24, 14), (22, 17), (16, 18)], "peach", mode="sphere", c=(18, 15, 6, 3))
    for x in (14, 17, 20):
        i.L(x, 19, x + 2, 22, "skin", w=1, mode="flat", tone=1, noline=True)
    return i


@icon("shield")
def shield():
    i = Icon()
    i.P([(16, 2), (28, 6), (27, 18), (16, 30), (5, 18), (4, 6)], "silver", mode="sphere", c=(16, 15, 12, 14))
    i.P([(16, 5), (25, 8), (24.5, 17), (16, 26.5), (7.5, 17), (7, 8)], "blue", mode="sphere", c=(16, 15, 10, 12))
    i.P([(16, 9), (18, 14), (23, 14), (19, 17), (21, 22), (16, 19), (11, 22), (13, 17), (9, 14), (14, 14)], "butter", mode="flat", tone=4)
    return i


@icon("shield-check")
def shieldcheck():
    i = Icon()
    i.P([(16, 2), (28, 6), (27, 18), (16, 30), (5, 18), (4, 6)], "silver", mode="sphere", c=(16, 15, 12, 14))
    i.P([(16, 5), (25, 8), (24.5, 17), (16, 26.5), (7.5, 17), (7, 8)], "green", mode="sphere", c=(16, 15, 10, 12))
    i.L(10, 15, 14, 20, "white", w=2.6, mode="flat", tone=5, noline=True); i.L(14, 20, 22, 10, "white", w=2.6, mode="flat", tone=5, noline=True)
    return i
