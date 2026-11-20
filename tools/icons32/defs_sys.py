import math

from engine import Icon
from reg import icon


@icon("settings")
def settings():
    i = Icon()
    for k in range(8):
        a = math.radians(k * 45)
        i.L(16, 16, 16 + 13 * math.cos(a), 16 + 13 * math.sin(a), "steel", w=5, mode="sphere", c=(16, 16, 14, 14))
    i.E(16, 16, 10, 10, "steel")
    i.E(16, 16, 4.5, 4.5, "iron", mode="flat", tone=2); i.HE(16, 16, 3, 3)
    return i


@icon("sun")
def sun():
    i = Icon()
    for k in range(8):
        a = math.radians(k * 45 + 22.5)
        i.L(16 + 10 * math.cos(a), 16 + 10 * math.sin(a), 16 + 14.5 * math.cos(a), 16 + 14.5 * math.sin(a), "orange", w=3)
    i.E(16, 16, 9, 9, "yellow")
    i.D([(12, 17), (20, 17)], "cocoa", 2); i.D([(14, 20), (15, 21), (16, 21), (17, 21), (18, 20)], "cocoa", 2)
    i.E(10.5, 19, 1.4, 1, "pink", mode="flat"); i.E(21.5, 19, 1.4, 1, "pink", mode="flat")
    return i


@icon("moon")
def moon():
    i = Icon()
    i.E(14, 16, 12, 12, "butter"); i.HE(21, 11, 10, 10)
    i.E(9, 15, 1.6, 1.6, "gold", mode="flat", tone=2); i.E(12, 22, 1.2, 1.2, "gold", mode="flat", tone=2)
    for (x, y) in ((25, 21), (21, 27)):
        i.D([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], "sky", 4)
    return i


def cloud(i, mat="white", y=0):
    c = (16, 17 + y, 14, 9)
    i.E(10, 18 + y, 7, 6, mat, c=c); i.E(18, 13 + y, 8, 8, mat, c=c); i.E(24, 18 + y, 6, 5.5, mat, c=c)
    i.R(5, 18 + y, 28, 24 + y, mat, mode="sphere", c=c)


@icon("cloud")
def cloud_icon():
    i = Icon(); cloud(i); return i


@icon("cloud-up")
def cloudup():
    i = Icon(); cloud(i, "white", -2)
    i.R(14, 15, 18, 29, "blue", mode="cylx"); i.P([(9, 17), (16, 9), (23, 17)], "blue")
    return i


@icon("cloud-off")
def cloudoff():
    i = Icon(); cloud(i, "silver")
    i.L(4, 4, 28, 28, "red", w=3)
    return i


@icon("zap")
def zap():
    i = Icon()
    i.P([(19, 1), (6, 18), (14, 18), (11, 31), (26, 12), (17, 12), (21, 1)], "yellow", mode="sphere", c=(16, 15, 10, 14))
    return i


def bell(i, mat="gold"):
    i.E(16, 4, 2.2, 2.2, mat)
    i.P([(9, 10), (16, 5), (23, 10), (25, 22), (28, 25), (4, 25), (7, 22)], mat, mode="sphere", c=(16, 15, 11, 11))
    i.R(3, 24, 29, 26, mat, mode="cyly", tone=2)
    i.E(16, 28, 3, 2.5, "orange")


@icon("bell")
def bell_icon():
    i = Icon(); bell(i); i.G([(11, 11), (10, 13)]); return i


@icon("bell-ring")
def bellring():
    i = Icon(); bell(i)
    for s in (-1, 1):
        i.L(16 + s * 12, 6, 16 + s * 14, 10, "pink", w=1.5, mode="flat", noline=True)
        i.L(16 + s * 11, 12, 16 + s * 14, 15, "pink", w=1.5, mode="flat", noline=True)
    return i


@icon("bell-off")
def belloff():
    i = Icon(); bell(i, "silver"); i.L(3, 3, 29, 29, "red", w=3); return i


@icon("chat")
def chat():
    i = Icon()
    i.P([(6, 22), (4, 29), (13, 23)], "lav")
    i.R(2, 4, 29, 23, "lav", mode="sphere", r=5)
    for x in (10, 16, 22):
        i.E(x, 13.5, 1.8, 1.8, "white", mode="flat", tone=5)
    return i


def spark(i, cx, cy, r, mat):
    i.P([(cx, cy - r), (cx + r * 0.3, cy - r * 0.3), (cx + r, cy), (cx + r * 0.3, cy + r * 0.3), (cx, cy + r), (cx - r * 0.3, cy + r * 0.3), (cx - r, cy), (cx - r * 0.3, cy - r * 0.3)], mat, mode="sphere", c=(cx, cy, r, r))


@icon("sparkles")
def sparkles():
    i = Icon(); spark(i, 12, 17, 11, "yellow"); spark(i, 25, 7, 5.5, "pink"); spark(i, 25, 25, 4.5, "sky"); return i


@icon("trash")
def trash():
    i = Icon()
    i.R(12, 2, 19, 5, "iron", r=1); i.HR(14, 4, 17, 5)
    i.R(3, 5, 28, 9, "steel", mode="cyly", r=1)
    i.P([(5, 9), (26, 9), (24, 30), (7, 30)], "silver", mode="cylx")
    for x in (11, 16, 21):
        i.L(x, 12, x - (x - 16) * 0.1, 27, "steel", w=1.5, mode="flat", tone=2, noline=True)
    return i


@icon("calendar")
def calendar():
    i = Icon()
    i.R(2, 5, 29, 29, "white", r=2)
    i.R(2, 5, 29, 11, "red", r=2)
    i.R(8, 2, 10, 8, "steel", mode="cylx"); i.R(21, 2, 23, 8, "steel", mode="cylx")
    for r in range(3):
        for c in range(5):
            x, y = 5 + c * 5, 14 + r * 5
            m = "red" if (r, c) == (1, 3) else "silver"
            i.R(x, y, x + 2, y + 2, m, mode="flat", tone=3 if m == "red" else 2)
    return i


@icon("clock")
def clock():
    i = Icon()
    i.E(7, 6, 4.5, 4, "red"); i.E(25, 6, 4.5, 4, "red")
    i.L(8, 25, 5, 30, "dark", w=2); i.L(24, 25, 27, 30, "dark", w=2)
    i.E(16, 17, 12, 12, "red"); i.E(16, 17, 9.4, 9.4, "white")
    for k in range(12):
        a = math.radians(k * 30)
        i.D([(int(16 + 7.8 * math.cos(a)), int(17 + 7.8 * math.sin(a)))], "slate", 2)
    i.L(16, 17, 16, 11, "dark", w=1.6, mode="flat", noline=True); i.L(16, 17, 20, 19, "dark", w=1.6, mode="flat", noline=True)
    i.E(16, 17, 1.2, 1.2, "red", mode="flat")
    return i


def badge(i, mat):
    i.E(16, 16, 14, 14, mat)


@icon("info")
def info():
    i = Icon(); badge(i, "blue")
    i.E(16, 9, 2.2, 2.2, "white", mode="flat", tone=5); i.R(14, 13, 18, 24, "white", mode="flat", tone=5)
    return i


@icon("alert-circle")
def alertcircle():
    i = Icon(); badge(i, "red")
    i.R(14, 7, 18, 19, "white", mode="flat", tone=5); i.E(16, 23.5, 2.2, 2.2, "white", mode="flat", tone=5)
    return i


@icon("check-circle")
def checkcircle():
    i = Icon(); badge(i, "green")
    i.L(9, 16, 14, 21, "white", w=3, mode="flat", tone=5, noline=True); i.L(14, 21, 23, 11, "white", w=3, mode="flat", tone=5, noline=True)
    return i


@icon("alert")
def alert():
    i = Icon()
    i.P([(16, 2), (30, 28), (2, 28)], "yellow", mode="sphere", c=(16, 18, 13, 12))
    i.R(14, 10, 18, 20, "dark", mode="flat"); i.E(16, 24, 2, 2, "dark", mode="flat")
    return i


@icon("inbox")
def inbox():
    i = Icon()
    i.R(8, 3, 23, 16, "paper"); i.L(11, 7, 20, 7, "slate", w=1, mode="flat", tone=4, noline=True); i.L(11, 10, 18, 10, "slate", w=1, mode="flat", tone=4, noline=True)
    i.P([(2, 15), (9, 15), (12, 19), (19, 19), (22, 15), (29, 15), (29, 28), (2, 28)], "lav", mode="sphere", c=(16, 21, 14, 7))
    i.P([(2, 15), (6, 9), (8, 9), (8, 15)], "lav"); i.P([(29, 15), (25, 9), (23, 9), (23, 15)], "lav")
    return i


@icon("save")
def save():
    i = Icon()
    i.R(2, 2, 29, 29, "blue", r=2)
    i.R(8, 2, 23, 11, "silver"); i.R(18, 4, 21, 9, "iron", mode="flat")
    i.R(6, 16, 25, 29, "paper")
    for y in (20, 24):
        i.L(9, y, 22, y, "slate", w=1, mode="flat", tone=4, noline=True)
    return i


@icon("upload")
def upload():
    i = Icon()
    i.P([(2, 18), (8, 18), (8, 24), (23, 24), (23, 18), (29, 18), (29, 29), (2, 29)], "lav", mode="sphere", c=(16, 24, 14, 6))
    i.R(13, 9, 18, 21, "green", mode="cylx"); i.P([(7, 11), (15.5, 2), (24, 11)], "green")
    return i


@icon("history")
def history():
    i = Icon()
    i.R(5, 1, 26, 4, "darkwood", mode="cyly"); i.R(5, 27, 26, 30, "darkwood", mode="cyly")
    i.P([(7, 4), (24, 4), (17, 15.5), (24, 27), (7, 27), (14, 15.5)], "glass", mode="cylx")
    i.P([(10, 8), (21, 8), (16, 14)], "sand", mode="flat", tone=4)
    i.P([(16, 19), (22, 26), (9, 26)], "sand", mode="flat", tone=3)
    i.L(15.5, 15, 15.5, 22, "sand", w=1, mode="flat", tone=4, noline=True)
    return i


@icon("mic")
def mic():
    i = Icon()
    i.ring(16, 14, 9, 2, "iron"); i.HR(0, 0, 31, 13)
    i.R(15, 22, 17, 28, "iron", mode="cylx"); i.R(10, 27, 22, 29, "iron", mode="cyly")
    i.R(10, 2, 21, 19, "pink", mode="cylx", r=5)
    for y in (6, 9, 12):
        i.L(12, y, 19, y, "rose", w=1, mode="flat", tone=2, noline=True)
    return i


@icon("wrench")
def wrench():
    i = Icon()
    i.L(6, 26, 19, 13, "steel", w=5)
    i.E(23, 9, 7.5, 7.5, "steel")
    i._erase([(x, y) for y in range(32) for x in range(32) if abs((x - 23) - (9 - y)) <= 2.2 and (x - 23) + (9 - y) >= 2])
    i.E(5.5, 26.5, 1.4, 1.4, "iron", mode="flat")
    return i


@icon("mai")
def mai():
    i = Icon()
    i.L(16, 8, 9, 2, "dark", w=1.5); i.L(16, 8, 23, 2, "dark", w=1.5)
    i.E(9, 2, 1.8, 1.8, "yellow"); i.E(23, 2, 1.8, 1.8, "yellow")
    i.R(2, 8, 29, 27, "pink", r=3)
    i.R(5, 11, 22, 24, "teal", mode="sphere", r=2)
    i.R(9, 15, 10, 17, "white", mode="flat", tone=5); i.R(17, 15, 18, 17, "white", mode="flat", tone=5)
    i.D([(12, 20), (13, 21), (14, 21), (15, 20)], "white", 5)
    i.D([(7, 19), (8, 19), (19, 19), (20, 19)], "pink", 4)
    i.E(26, 14, 2, 2, "yellow"); i.E(26, 20, 2, 2, "mint")
    i.R(6, 27, 9, 30, "dark", mode="flat"); i.R(22, 27, 25, 30, "dark", mode="flat")
    return i
