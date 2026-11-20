from engine import Icon
from reg import icon


# ===================== MUA SẮM =====================
@icon("bag")
def bag():
    i = Icon()
    i.ring(16, 10, 6, 2, "rose"); i.HR(0, 10, 31, 31)
    i.P([(5, 10), (27, 10), (29, 29), (3, 29)], "pink", mode="cylx")
    i.R(5, 10, 27, 12, "rose", mode="flat", tone=2)
    i.E(16, 20, 4, 4, "butter", mode="flat"); i.D([(15, 19), (16, 19), (15, 20), (17, 21)], "gold", 2)
    return i


@icon("shirt")
def shirt():
    i = Icon()
    i.P([(10, 3), (13, 5), (19, 5), (22, 3), (30, 9), (26, 15), (24, 13), (24, 29), (8, 29), (8, 13), (6, 15), (2, 9)], "sky", mode="sphere", c=(16, 15, 13, 13))
    i.P([(13, 4), (16, 8), (19, 4)], "blue", mode="flat")
    i.R(11, 16, 15, 19, "white", mode="flat", tone=4)
    return i


@icon("ao-dai")
def aodai():
    i = Icon()
    i.R(10, 24, 13, 30, "white", mode="cylx"); i.R(19, 24, 22, 30, "white", mode="cylx")
    i.P([(12, 2), (20, 2), (21, 5), (27, 8), (29, 20), (26, 20), (24, 11), (23, 22), (25, 30), (19, 30), (16, 22), (13, 30), (7, 30), (9, 22), (8, 11), (6, 20), (3, 20), (5, 8), (11, 5)], "lav", mode="sphere", c=(16, 14, 12, 14))
    i.R(13, 2, 19, 4, "purple", mode="flat")
    for p in ((16, 10), (14, 13), (18, 13), (16, 16)):
        i.D([p], "pink", 4)
    i.D([(16, 13)], "yellow", 4)
    return i


@icon("shoe")
def shoe():
    i = Icon()
    i.P([(2, 13), (11, 11), (14, 6), (19, 7), (22, 15), (29, 19), (30, 24), (2, 24)], "red", mode="sphere", c=(15, 17, 14, 9))
    i.R(1, 23, 30, 27, "white", mode="cyly", r=1)
    for x in (13, 15, 17):
        i.L(x, 10, x + 2, 12, "white", w=1, mode="flat", tone=5, noline=True)
    i.D([(5, 19), (6, 19), (7, 19)], "white", 4)
    return i


@icon("non-la")
def nonla():
    i = Icon()
    i.P([(16, 3), (31, 22), (1, 22)], "sand", mode="sphere", c=(16, 18, 14, 10))
    for k in range(1, 4):
        i.L(16 - k * 3.6, 7 + k * 4, 16 + k * 3.6, 7 + k * 4, "bread", w=1, mode="flat", tone=2, noline=True)
    i.R(4, 21, 28, 23, "crust", mode="cyly")
    i.L(9, 23, 14, 29, "rose", w=1.5); i.L(23, 23, 18, 29, "rose", w=1.5)
    return i


@icon("watch")
def watch():
    i = Icon()
    i.R(11, 1, 21, 9, "coffee", mode="cylx"); i.R(11, 23, 21, 31, "coffee", mode="cylx")
    i.E(16, 16, 9, 9, "gold"); i.E(16, 16, 6.8, 6.8, "white")
    i.L(16, 16, 16, 11, "dark", w=1.4, mode="flat", noline=True); i.L(16, 16, 19, 17, "red", w=1.2, mode="flat", noline=True)
    i.R(25, 14, 26, 17, "gold", mode="flat")
    return i


@icon("glasses")
def glasses():
    i = Icon()
    i.L(2, 11, 8, 11, "red", w=2); i.L(24, 11, 30, 11, "red", w=2)
    i.R(3, 11, 14, 21, "red", r=3); i.R(18, 11, 29, 21, "red", r=3)
    i.R(5, 13, 12, 19, "navy", mode="sphere", r=2); i.R(20, 13, 27, 19, "navy", mode="sphere", r=2)
    i.L(14, 13, 18, 13, "red", w=2, mode="flat")
    i.D([(6, 14), (7, 14), (6, 15), (21, 14), (22, 14), (21, 15)], "#FFFFFF")
    return i


@icon("gift")
def gift():
    i = Icon()
    i.E(11, 6, 5, 3.4, "gold"); i.E(21, 6, 5, 3.4, "gold"); i.HE(11, 6, 2, 1.2); i.HE(21, 6, 2, 1.2)
    i.R(3, 10, 28, 15, "red", r=1); i.R(5, 15, 26, 29, "red")
    i.R(14, 10, 17, 29, "gold", mode="cylx")
    i.E(16, 9, 2.4, 2, "gold")
    return i


@icon("package")
def package():
    i = Icon()
    i.P([(2, 10), (16, 4), (30, 10), (16, 16)], "bread", mode="flat", tone=4)
    i.P([(2, 10), (16, 16), (16, 30), (2, 24)], "bread", mode="flat", tone=3)
    i.P([(16, 16), (30, 10), (30, 24), (16, 30)], "crust", mode="flat", tone=3)
    i.P([(7, 8), (11, 6), (25, 12), (21, 14)], "paper", mode="flat", tone=3)
    i.D([(5, 19), (6, 19), (7, 20), (5, 21)], "cocoa", 3)
    return i


@icon("lipstick")
def lipstick():
    i = Icon()
    i.P([(12, 4), (19, 1), (19, 12), (12, 12)], "red", mode="cylx")
    i.R(11, 12, 20, 16, "gold", mode="cylx")
    i.R(9, 16, 22, 30, "magenta", mode="cylx", r=1)
    i.R(9, 18, 22, 19, "gold", mode="flat", tone=4)
    return i


@icon("store")
def store():
    i = Icon()
    i.R(4, 12, 27, 29, "cream")
    for k in range(7):
        i.P([(1 + k * 4.3, 4), (5.3 + k * 4.3, 4), (5.3 + k * 4.3, 12), (1 + k * 4.3, 12)], "red" if k % 2 == 0 else "white", mode="cylx")
    for k in range(7):
        i.E(3.2 + k * 4.3, 12.5, 2.2, 1.8, "red" if k % 2 == 0 else "white", mode="cyly")
    i.R(7, 17, 14, 29, "wood"); i.R(17, 17, 25, 24, "glass")
    i.D([(12, 23)], "gold", 4)
    return i


@icon("scissors")
def scissors():
    i = Icon()
    i.L(7, 3, 21, 20, "silver", w=3); i.L(25, 3, 11, 20, "steel", w=3)
    i.E(16, 13, 1.5, 1.5, "dark")
    i.ring(9, 24, 5, 2.2, "pink"); i.ring(23, 24, 5, 2.2, "pink")
    return i


@icon("umbrella")
def umbrella():
    i = Icon()
    i.E(16, 15, 14, 12, "pink")
    i.W(16, 15, 14, 180, 216, "butter", c=(16, 15, 14, 12), sep=True); i.W(16, 15, 14, 252, 288, "mint", c=(16, 15, 14, 12), sep=True); i.W(16, 15, 14, 324, 360, "sky", c=(16, 15, 14, 12), sep=True)
    i.HR(0, 15, 31, 31)
    for x in (4, 10, 16, 22, 28):
        i.E(x, 15, 3, 1.6, "rose", mode="cyly")
    i.E(16, 3, 1.4, 1.6, "dark")
    i.ring(13, 26, 3.4, 1.8, "dark"); i.HR(8, 20, 18, 25)
    i.L(16, 16, 16, 26, "dark", w=2, mode="cylx")
    return i


# ===================== CÔNG NGHỆ =====================
@icon("phone")
def phone():
    i = Icon()
    i.R(8, 1, 24, 30, "black", r=3)
    i.R(10, 4, 22, 25, "sky", mode="sphere")
    for x, y, m in ((12, 7, "red"), (17, 7, "lime"), (12, 12, "butter"), (17, 12, "lav"), (12, 17, "pink"), (17, 17, "mint")):
        i.R(x, y, x + 3, y + 3, m)
    i.R(14, 27, 18, 28, "steel", mode="flat")
    return i


@icon("laptop")
def laptop():
    i = Icon()
    i.R(5, 5, 26, 21, "silver", r=1); i.R(7, 7, 24, 19, "blue", mode="sphere")
    i.L(9, 10, 18, 10, "sky", w=1, mode="flat", tone=5, noline=True); i.L(9, 13, 21, 13, "sky", w=1, mode="flat", tone=4, noline=True); i.L(9, 16, 15, 16, "pink", w=1, mode="flat", tone=4, noline=True)
    i.P([(2, 22), (29, 22), (31, 27), (0, 27)], "steel", mode="bevel")
    i.R(13, 24, 18, 25, "iron", mode="flat")
    return i


@icon("monitor")
def monitor():
    i = Icon()
    i.R(13, 21, 18, 27, "steel", mode="cylx"); i.E(16, 28, 8, 2, "iron", mode="cyly")
    i.R(2, 3, 29, 22, "dark", r=2); i.R(4, 5, 27, 19, "teal", mode="sphere")
    i.P([(5, 19), (12, 11), (17, 16), (21, 12), (27, 19)], "green", mode="flat")
    i.E(22, 9, 2, 2, "yellow")
    return i


@icon("headphones")
def headphones():
    i = Icon()
    i.ring(16, 17, 12, 2.6, "purple"); i.HR(0, 17, 31, 31)
    i.R(2, 15, 9, 27, "lav", r=2); i.R(23, 15, 30, 27, "lav", r=2)
    i.R(5, 17, 9, 25, "pink", mode="flat", tone=3); i.R(23, 17, 27, 25, "pink", mode="flat", tone=3)
    return i


@icon("gamepad")
def gamepad():
    i = Icon()
    i.E(9, 17, 8, 8, "lav"); i.E(23, 17, 8, 8, "lav"); i.R(9, 10, 23, 22, "lav", mode="sphere", c=(16, 16, 14, 8))
    i.R(6, 15, 12, 17, "dark", mode="flat"); i.R(8, 13, 10, 19, "dark", mode="flat")
    i.E(23, 14, 1.6, 1.6, "red"); i.E(26, 17, 1.6, 1.6, "yellow"); i.E(20, 17, 1.6, 1.6, "sky"); i.E(23, 20, 1.6, 1.6, "lime")
    return i


@icon("printer")
def printer():
    i = Icon()
    i.R(8, 2, 23, 12, "paper")
    i.R(2, 10, 29, 23, "silver", r=2)
    i.R(6, 19, 25, 29, "paper")
    for y in (22, 25):
        i.L(9, y, 22, y, "slate", w=1, mode="flat", tone=4, noline=True)
    i.E(25, 14, 1.4, 1.4, "lime")
    return i


@icon("radio")
def radio():
    i = Icon()
    i.L(8, 9, 4, 2, "steel", w=1.5)
    i.R(2, 9, 29, 27, "orange", r=2)
    i.E(10, 18, 5.5, 5.5, "cocoa")
    for y in range(14, 23, 2):
        i.L(6, y, 14, y, "choc", w=1, mode="flat", tone=2, noline=True)
    i.R(18, 12, 27, 16, "paper", mode="flat"); i.L(21, 12, 21, 16, "red", w=1, mode="flat", noline=True)
    i.E(20, 22, 2, 2, "cream"); i.E(25, 22, 2, 2, "cream")
    return i


@icon("cassette")
def cassette():
    i = Icon()
    i.R(1, 6, 30, 26, "pink", r=2)
    i.R(5, 9, 26, 19, "paper", mode="flat")
    i.R(9, 12, 22, 17, "dark", mode="flat")
    i.ring(12, 14.5, 2.4, 1.2, "white", noline=True); i.ring(19, 14.5, 2.4, 1.2, "white", noline=True)
    i.P([(8, 26), (10, 21), (21, 21), (23, 26)], "rose", mode="flat")
    return i


@icon("camera")
def camera():
    i = Icon()
    i.R(9, 4, 17, 8, "dark", r=1)
    i.R(2, 7, 29, 27, "dark", r=3)
    i.R(2, 10, 29, 13, "steel", mode="flat")
    i.E(16, 17, 8, 8, "silver"); i.E(16, 17, 5.6, 5.6, "blue"); i.E(16, 17, 2.8, 2.8, "navy")
    i.D([(14, 15), (13, 16), (14, 14)], "#FFFFFF")
    i.R(24, 9, 27, 11, "red", mode="flat")
    return i


@icon("image")
def image():
    i = Icon()
    i.R(1, 4, 30, 27, "wood", r=1)
    i.R(4, 7, 27, 24, "sky", mode="cyly")
    i.E(21, 11, 2.6, 2.6, "yellow")
    i.P([(4, 24), (12, 13), (19, 24)], "green", mode="cylx"); i.P([(13, 24), (20, 16), (27, 24)], "leaf", mode="cylx")
    return i


@icon("phone-call")
def phonecall():
    i = Icon()
    i.R(3, 4, 28, 9, "red", mode="cyly", r=2)
    i.R(3, 9, 8, 12, "red", r=1); i.R(23, 9, 28, 12, "red", r=1)
    i.P([(9, 12), (23, 12), (27, 28), (5, 28)], "red", mode="cylx")
    i.E(16, 20, 5.5, 5.5, "cream")
    for a in range(0, 360, 60):
        import math
        x = 16 + 3.6 * math.cos(math.radians(a)); y = 20 + 3.6 * math.sin(math.radians(a))
        i.D([(int(x), int(y))], "cocoa", 2)
    return i


# ===================== SỨC KHOẺ =====================
def heart_shape(i, mat, cx=16, cy=15, s=1.0):
    c = (cx, cy, 13 * s, 13 * s)
    i.E(cx - 5.5 * s, cy - 3 * s, 7.5 * s, 7 * s, mat, c=c); i.E(cx + 5.5 * s, cy - 3 * s, 7.5 * s, 7 * s, mat, c=c)
    i.P([(cx - 12.8 * s, cy - 1 * s), (cx + 12.8 * s, cy - 1 * s), (cx, cy + 13 * s)], mat, mode="sphere", c=c)


@icon("heart")
def heart():
    i = Icon(); heart_shape(i, "red"); i.G([(8, 8), (9, 8), (7, 9), (7, 10)]); return i


@icon("heart-pulse")
def heartpulse():
    i = Icon(); heart_shape(i, "rose")
    pts = [(1, 16), (9, 16), (11, 12), (14, 21), (17, 9), (20, 18), (22, 16), (30, 16)]
    for a, b in zip(pts, pts[1:]):
        i.L(a[0], a[1], b[0], b[1], "white", w=2, mode="flat", tone=5, noline=True)
    return i


@icon("pill")
def pill():
    i = Icon()
    i.L(7, 24, 15, 16, "white", w=8); i.L(15, 16, 23, 8, "red", w=8)
    i.E(24, 25, 5, 5, "sky"); i.L(21, 28, 27, 22, "blue", w=1, mode="flat", tone=2, noline=True)
    i.G([(18, 8), (19, 7)])
    return i


@icon("stethoscope")
def stethoscope():
    i = Icon()
    i.ring(12, 10, 7, 2, "dark"); i.HR(0, 0, 31, 9)
    i.L(5.5, 10, 6, 3, "dark", w=2); i.L(18.5, 10, 18, 3, "dark", w=2)
    i.E(6, 2.5, 1.8, 1.8, "silver"); i.E(18, 2.5, 1.8, 1.8, "silver")
    i.L(12, 17, 12, 23, "dark", w=2); i.L(12, 23, 16, 27, "dark", w=2); i.L(16, 27, 21, 25, "dark", w=2)
    i.E(24, 22, 5.5, 5.5, "silver"); i.E(24, 22, 3, 3, "sky")
    return i


@icon("dumbbell")
def dumbbell():
    i = Icon()
    i.R(8, 14, 23, 17, "steel", mode="cyly")
    for x in (3, 7, 21, 25):
        i.R(x, 8 if x in (7, 21) else 10, x + 3, 23 if x in (7, 21) else 21, "iron", mode="cylx", r=1)
    i.D([(9, 10), (22, 10)], "silver", 5)
    return i


@icon("activity")
def activity():
    i = Icon()
    i.R(2, 5, 29, 26, "dark", r=2); i.R(4, 7, 27, 24, "black", mode="flat")
    pts = [(5, 17), (10, 17), (12, 11), (15, 22), (18, 9), (21, 17), (26, 17)]
    for a, b in zip(pts, pts[1:]):
        i.L(a[0], a[1], b[0], b[1], "lime", w=1.6, mode="flat", tone=4, noline=True)
    i.D([(24, 9), (25, 9)], "red", 4)
    return i


@icon("syringe")
def syringe():
    i = Icon()
    i.L(23, 9, 29, 3, "steel", w=2.4); i.L(24, 4, 28, 8, "steel", w=2.4)
    i.L(5, 27, 9, 23, "silver", w=1.2)
    i.L(9, 23, 21, 11, "glass", w=7)
    i.L(9, 23, 15, 17, "red", w=5, mode="cylx")
    for k in range(3):
        i.D([(13 + k * 2, 17 - k * 2)], "dark", 3)
    return i


@icon("tooth")
def tooth():
    i = Icon()
    i.P([(5, 6), (11, 4), (16, 6), (21, 4), (27, 6), (27, 15), (23, 28), (19, 28), (16, 19), (13, 28), (9, 28), (5, 15)], "white", mode="sphere", c=(16, 13, 12, 12))
    i.P([(25, 2), (26, 5), (29, 6), (26, 7), (25, 10), (24, 7), (21, 6), (24, 5)], "butter", mode="flat", tone=4)
    i.G([(9, 8), (10, 8), (8, 9)])
    return i


@icon("baby")
def baby():
    i = Icon()
    i.E(16, 17, 12, 12, "skin", tone=4)
    i.P([(5, 13), (8, 5), (16, 2), (24, 5), (27, 13), (22, 10), (10, 10)], "sky", mode="sphere", c=(16, 9, 11, 7))
    i.E(16, 2, 2.4, 2.4, "white")
    i.D([(11, 16), (11, 17), (20, 16), (20, 17)], "black", 3)
    i.E(8.5, 20, 2, 1.4, "pink", mode="flat"); i.E(23.5, 20, 2, 1.4, "pink", mode="flat")
    i.E(16, 23, 3, 2, "rose"); i.E(16, 23, 1.2, 1.2, "white", mode="flat")
    return i


@icon("cross")
def cross():
    i = Icon()
    i.R(2, 2, 29, 29, "white", r=4)
    i.R(12, 6, 19, 25, "red"); i.R(6, 12, 25, 19, "red", sep=False)
    return i


@icon("yoga")
def yoga():
    i = Icon()
    i.E(16, 28, 14, 2.5, "lav", mode="cyly")
    i.E(16, 6, 3.6, 3.6, "skin"); i.E(16, 3.5, 3.6, 2, "cocoa", mode="flat", tone=2)
    i.P([(11, 11), (21, 11), (22, 21), (10, 21)], "teal", mode="sphere", c=(16, 16, 6, 6))
    i.L(11, 12, 6, 19, "skin", w=2.4); i.L(21, 12, 26, 19, "skin", w=2.4)
    i.P([(4, 26), (12, 20), (20, 20), (28, 26), (16, 27)], "purple", mode="sphere", c=(16, 24, 12, 4))
    return i
