from engine import Icon
from reg import icon


def house(i, roof="red", wall="cream"):
    i.R(5, 14, 26, 28, wall)
    i.P([(1, 16), (16, 3), (31, 16), (27, 16), (16, 7), (5, 16)], roof)
    i.P([(5, 16), (16, 7), (27, 16)], "brick", mode="flat", tone=2)
    i.R(21, 4, 24, 11, "brick")
    i.R(3, 28, 28, 29, "grass")


@icon("home")
def home():
    i = Icon(); house(i)
    i.R(13, 19, 19, 28, "wood"); i.D([(17, 23)], "gold", 4)
    i.R(7, 18, 11, 22, "glass"); i.R(21, 18, 25, 22, "glass")
    return i


@icon("home-heart")
def homeheart():
    i = Icon(); house(i, roof="rose", wall="paper")
    i.E(13, 20, 3, 3, "red", c=(16, 22, 6, 6)); i.E(19, 20, 3, 3, "red", c=(16, 22, 6, 6)); i.P([(10, 21), (22, 21), (16, 27)], "red", mode="sphere", c=(16, 22, 6, 6))
    return i


@icon("building")
def building():
    i = Icon()
    i.R(3, 12, 13, 29, "sky", mode="cylx")
    i.R(11, 2, 27, 29, "slate", mode="cylx")
    for y in range(5, 26, 4):
        for x in (14, 18, 22):
            i.R(x, y, x + 2, y + 2, "butter" if (x + y) % 3 else "glass", mode="flat", tone=4 if (x + y) % 3 else 3)
    for y in (15, 20, 25):
        i.R(5, y, 7, y + 2, "butter", mode="flat", tone=4)
    i.R(17, 26, 21, 29, "darkwood", mode="flat")
    return i


@icon("droplet")
def droplet():
    i = Icon()
    i.P([(16, 1), (24, 14), (16, 18), (8, 14)], "water", mode="sphere", c=(16, 19, 10, 11))
    i.E(16, 19, 9.5, 10, "water")
    i.G([(11, 17), (11, 18), (12, 16), (11, 19)])
    i.D([(13, 24), (14, 25)], "glass", 4)
    return i


@icon("wifi")
def wifi():
    i = Icon()
    for r, m in ((14, "teal"), (10, "mint"), (6, "sky")):
        i.ring(16, 25, r, 2.6, m, mode="flat", tone=3)
    i._erase([(x, y) for y in range(32) for x in range(32) if y > 25 - abs(x - 16) * 0.35 or abs(x - 16) > (25 - y) * 1.25])
    i.E(16, 25.5, 2.6, 2.6, "teal")
    return i


@icon("tv")
def tv():
    i = Icon()
    i.L(15, 9, 9, 2, "dark", w=1.5); i.L(17, 9, 23, 2, "dark", w=1.5)
    i.E(9, 2, 1.5, 1.5, "butter"); i.E(23, 2, 1.5, 1.5, "butter")
    i.R(2, 9, 29, 27, "pink", r=3)
    i.R(5, 12, 21, 24, "teal", mode="sphere", r=2)
    i.D([(7, 14), (8, 14), (7, 15)], "#FFFFFF")
    i.E(25, 15, 2, 2, "cream"); i.E(25, 21, 2, 2, "cream")
    i.R(6, 27, 9, 29, "dark", mode="flat"); i.R(22, 27, 25, 29, "dark", mode="flat")
    return i


@icon("bulb")
def bulb():
    i = Icon()
    i.E(16, 12, 10, 10, "yellow")
    i.P([(11, 19), (21, 19), (19, 24), (13, 24)], "yellow", mode="cylx")
    i.L(13, 13, 15, 17, "orange", w=1, mode="flat", tone=2, noline=True); i.L(19, 13, 17, 17, "orange", w=1, mode="flat", tone=2, noline=True)
    i.R(12, 23, 20, 28, "steel", mode="cylx")
    i.L(12, 25, 20, 25, "iron", w=1, mode="flat", noline=True)
    i.R(14, 28, 18, 30, "dark", mode="flat")
    i.G([(10, 7), (11, 6), (9, 9)])
    return i


@icon("bed")
def bed():
    i = Icon()
    i.R(1, 7, 5, 28, "darkwood")
    i.R(5, 17, 30, 24, "white")
    i.E(10, 15.5, 4.5, 3, "white")
    i.R(13, 13, 30, 22, "sky", r=1)
    i.L(13, 17, 30, 17, "blue", w=1, mode="flat", tone=4, noline=True)
    i.R(5, 23, 30, 25, "wood", mode="cyly")
    i.R(27, 25, 30, 28, "darkwood")
    return i


@icon("bath")
def bath():
    i = Icon()
    i.L(5, 17, 5, 5, "steel", w=2); i.L(5, 5, 10, 5, "steel", w=2); i.E(10, 7, 2.5, 1.6, "silver")
    for x, y in ((9, 10), (11, 12), (8, 13)):
        i.D([(x, y)], "water", 3)
    i.E(12, 14, 3, 3, "glass"); i.E(18, 13, 3.5, 3.5, "glass"); i.E(23, 14, 2.6, 2.6, "glass")
    i.R(2, 15, 29, 18, "white", mode="cyly", r=1)
    i.R(4, 17, 27, 25, "white", mode="cylx", r=3)
    i.R(4, 17, 27, 18, "water", mode="flat", tone=4)
    i.L(7, 25, 6, 29, "gold", w=2); i.L(24, 25, 25, 29, "gold", w=2)
    return i


@icon("sofa")
def sofa():
    i = Icon()
    i.R(5, 7, 26, 18, "teal", r=2)
    i.L(16, 8, 16, 17, "teal", w=1, mode="flat", tone=2, noline=True)
    i.R(1, 13, 7, 26, "teal", mode="cylx", r=2); i.R(24, 13, 30, 26, "teal", mode="cylx", r=2)
    i.R(6, 18, 25, 24, "mint", r=1)
    i.L(16, 18, 16, 24, "mint", w=1, mode="flat", tone=2, noline=True)
    i.E(10, 13, 3, 3, "pink")
    i.R(3, 26, 5, 29, "darkwood", mode="flat"); i.R(26, 26, 28, 29, "darkwood", mode="flat")
    return i


@icon("fan")
def fan():
    i = Icon()
    i.R(15, 18, 17, 26, "steel", mode="cylx")
    i.E(16, 27.5, 7, 2.4, "white", mode="cyly")
    i.E(16, 11, 10, 10, "glass", mode="flat", tone=4)
    for a, b in ((16, 4), (23, 13), (10, 15)):
        i.E((a + 16) / 2, (b + 11) / 2, 3.6, 2.6, "sky")
    i.E(16, 11, 2.2, 2.2, "blue")
    i.ring(16, 11, 10, 1.2, "steel", noline=True)
    return i


@icon("plug")
def plug():
    i = Icon()
    i.L(16, 22, 14, 30, "dark", w=2.4)
    i.R(10, 3, 12, 10, "silver", mode="cylx"); i.R(19, 3, 21, 10, "silver", mode="cylx")
    i.R(6, 9, 25, 18, "white", r=2)
    i.P([(8, 18), (23, 18), (19, 24), (12, 24)], "white", mode="cylx")
    i.D([(15, 13), (16, 13)], "green", 3)
    return i


@icon("flame")
def flame():
    i = Icon()
    i.P([(16, 1), (24, 11), (27, 19), (23, 28), (16, 30), (9, 28), (5, 19), (8, 12), (11, 15), (13, 7)], "red", mode="sphere", c=(16, 20, 11, 11))
    i.P([(16, 8), (22, 16), (22, 24), (16, 28), (10, 24), (10, 18), (13, 19)], "orange", mode="sphere", c=(16, 22, 7, 7))
    i.P([(16, 15), (19, 21), (18, 26), (14, 26), (13, 22)], "yellow", mode="sphere", c=(16, 23, 4, 4))
    return i


@icon("key")
def key():
    i = Icon()
    i.E(9, 16, 7, 7, "gold"); i.HE(7.5, 16, 2.2, 2.2)
    i.R(14, 14, 29, 18, "gold", mode="cyly")
    i.R(21, 18, 23, 22, "gold"); i.R(26, 18, 28, 23, "gold")
    i.G([(6, 11), (7, 11), (5, 12)])
    return i


@icon("key-house")
def keyhouse():
    i = Icon()
    i.P([(4, 11), (13, 3), (22, 11)], "red")
    i.R(6, 11, 20, 21, "cream")
    i.R(11, 14, 15, 21, "wood")
    i.E(23, 21, 5, 5, "gold"); i.HE(23, 21, 1.8, 1.8)
    i.L(19.5, 24.5, 10, 30, "gold", w=2.6)
    i.R(11, 26, 13, 29, "gold")
    return i


@icon("lock")
def lock():
    i = Icon()
    i.ring(16, 13, 8, 3, "silver"); i.HR(0, 14, 31, 31)
    i.R(5, 13, 26, 29, "gold", r=2)
    i.E(16, 19.5, 2.4, 2.4, "cocoa", mode="flat"); i.R(15, 20, 17, 25, "cocoa", mode="flat")
    i.G([(8, 15), (8, 16), (9, 15)])
    return i


@icon("tree")
def tree():
    i = Icon()
    i.R(14, 20, 18, 29, "darkwood", mode="cylx")
    i.E(16, 12, 11, 9.5, "leaf"); i.E(10, 16, 6, 5, "green"); i.E(22, 16, 6, 5, "green"); i.E(16, 8, 7, 6, "lime")
    i.E(9, 11, 1.5, 1.5, "red"); i.E(22, 9, 1.5, 1.5, "red"); i.E(19, 17, 1.5, 1.5, "red")
    i.E(16, 29.5, 8, 1.5, "grass", mode="flat")
    return i


@icon("sprout")
def sprout():
    i = Icon()
    i.L(16, 18, 16, 9, "green", w=2)
    i.E(10, 9, 6, 3.5, "leaf"); i.E(22, 7, 6, 3.5, "lime")
    i.P([(6, 17), (26, 17), (23, 30), (9, 30)], "brick", mode="cylx")
    i.R(5, 16, 27, 20, "brick", mode="cyly", tone=4)
    i.E(16, 16.5, 9, 1.5, "cocoa", mode="flat")
    return i


@icon("flower")
def flower():
    i = Icon()
    i.L(16, 16, 16, 30, "green", w=2)
    i.E(11, 24, 4, 2, "leaf"); i.E(21, 21, 4, 2, "leaf")
    for dx, dy in ((0, -6), (6, -1), (4, 5), (-4, 5), (-6, -1)):
        i.E(16 + dx, 11 + dy, 4.5, 4.5, "pink")
    i.E(16, 11, 3.6, 3.6, "yellow")
    return i


@icon("palm")
def palm():
    i = Icon()
    i.E(16, 27, 13, 4, "sand", mode="cyly")
    i.E(16, 30, 15, 2, "water", mode="flat", tone=3)
    i.L(17, 26, 14, 9, "wood", w=3)
    for k in range(3):
        i.L(16, 9 + k * 5, 14, 9 + k * 5, "darkwood", w=1, mode="flat", noline=True)
    i.E(9, 8, 7, 2.5, "leaf"); i.E(23, 8, 7, 2.5, "green"); i.E(13, 5, 5, 2.5, "lime"); i.E(20, 4, 5, 2.4, "leaf")
    i.E(15, 11, 2, 2, "cocoa"); i.E(18, 11, 2, 2, "cocoa")
    return i


# ===================== DI CHUYỂN =====================
def wheel(i, cx, cy, r=4):
    i.E(cx, cy, r, r, "dark"); i.E(cx, cy, r * 0.45, r * 0.45, "silver")


@icon("scooter")
def scooter():
    i = Icon()
    i.L(23, 4, 25, 18, "steel", w=2)
    i.L(20, 4, 27, 4, "dark", w=2); i.E(27, 9, 1.8, 1.8, "butter")
    i.P([(3, 15), (10, 14), (13, 18), (21, 18), (22, 10), (27, 12), (28, 22), (4, 22)], "mint", mode="sphere", c=(16, 18, 13, 6))
    i.R(4, 11, 13, 14, "cocoa", r=1)
    wheel(i, 8, 24); wheel(i, 24, 24)
    return i


@icon("bike")
def bike():
    i = Icon()
    for cx in (8, 24):
        i.ring(cx, 21, 6.5, 2, "dark"); i.E(cx, 21, 1.4, 1.4, "silver")
    i.L(8, 21, 14, 21, "red", w=2); i.L(14, 21, 20, 12, "red", w=2); i.L(11, 12, 20, 12, "red", w=2); i.L(11, 12, 8, 21, "red", w=2)
    i.L(20, 12, 24, 21, "red", w=2); i.L(11, 12, 14, 21, "red", w=2)
    i.R(8, 9, 13, 10, "cocoa", r=0); i.L(20, 12, 19, 7, "steel", w=1.5); i.L(17, 7, 22, 7, "dark", w=2)
    return i


def car_body(i, mat, glass="glass"):
    i.P([(8, 9), (23, 9), (27, 16), (5, 16)], glass)
    i.L(16, 9, 16, 16, mat, w=2, mode="flat")
    i.R(2, 15, 29, 24, mat, r=3)
    i.R(2, 18, 5, 20, "butter", mode="flat", tone=4); i.R(26, 18, 29, 20, "orange", mode="flat")
    wheel(i, 8.5, 24); wheel(i, 23.5, 24)


@icon("car")
def car():
    i = Icon(); car_body(i, "red"); return i


@icon("taxi")
def taxi():
    i = Icon()
    i.R(12, 4, 20, 8, "dark", r=1); i.D([(14, 6), (15, 6), (17, 6), (18, 6)], "yellow", 4)
    car_body(i, "yellow")
    for x in range(4, 28, 4):
        i.R(x, 19, x + 1, 20, "black", mode="flat")
    return i


@icon("bus")
def bus():
    i = Icon()
    i.R(3, 3, 28, 26, "lime", r=2)
    i.R(5, 6, 26, 14, "glass", r=1); i.L(16, 6, 16, 14, "lime", w=1, mode="flat", noline=True)
    i.R(3, 16, 28, 17, "green", mode="flat")
    i.E(7, 21, 2, 2, "butter"); i.E(24, 21, 2, 2, "butter")
    i.R(11, 20, 20, 22, "dark", mode="flat")
    i.R(5, 26, 9, 29, "dark", mode="flat"); i.R(22, 26, 26, 29, "dark", mode="flat")
    return i


@icon("train")
def train():
    i = Icon()
    i.R(5, 2, 26, 25, "lav", r=4)
    i.R(8, 6, 23, 14, "glass", r=1)
    i.R(5, 16, 26, 18, "purple", mode="flat")
    i.E(10, 21, 2, 2, "butter"); i.E(21, 21, 2, 2, "butter")
    i.L(9, 25, 5, 30, "dark", w=2); i.L(22, 25, 26, 30, "dark", w=2)
    i.L(3, 30, 28, 30, "steel", w=1.5, mode="flat")
    return i


@icon("plane")
def plane():
    i = Icon()
    i.P([(14, 14), (22, 14), (12, 28), (8, 28)], "sky")
    i.P([(15, 15), (21, 15), (13, 3), (10, 3)], "blue")
    i.R(2, 13, 30, 18, "white", mode="cyly", r=2)
    i.P([(2, 13), (6, 13), (3, 6), (1, 6)], "red")
    i.R(27, 14, 30, 15, "glass", mode="flat")
    for x in (10, 14, 18, 22):
        i.D([(x, 15)], "blue", 3)
    return i


@icon("ship")
def ship():
    i = Icon()
    i.R(12, 5, 15, 15, "red", mode="cylx"); i.R(12, 5, 15, 7, "dark", mode="flat")
    i.R(8, 11, 24, 17, "white"); i.D([(11, 13), (15, 13), (19, 13)], "glass", 3)
    i.P([(2, 17), (30, 17), (26, 26), (6, 26)], "navy", mode="cylx")
    i.R(3, 17, 29, 19, "red", mode="flat")
    i.P([(0, 27), (8, 25), (16, 27), (24, 25), (31, 27), (31, 30), (0, 30)], "water", mode="flat")
    return i


@icon("fuel")
def fuel():
    i = Icon()
    i.R(4, 3, 19, 28, "red", r=2)
    i.R(7, 6, 16, 12, "glass", mode="flat", tone=4)
    i.D([(9, 9), (10, 9), (12, 9), (13, 9)], "dark", 3)
    i.R(2, 27, 21, 29, "dark", mode="flat")
    i.L(19, 9, 24, 9, "dark", w=1.8); i.L(24, 9, 26, 22, "dark", w=1.8); i.L(26, 22, 23, 26, "dark", w=1.8)
    i.R(22, 5, 27, 9, "steel", r=1)
    return i


@icon("parking")
def parking():
    i = Icon()
    i.R(14, 18, 17, 30, "steel", mode="cylx")
    i.R(3, 2, 28, 21, "blue", r=2)
    i.L(11, 6, 11, 18, "white", w=2.6, mode="flat", tone=5, noline=True)
    i.R(10, 5, 17, 7, "white", mode="flat", tone=5); i.R(10, 11, 17, 13, "white", mode="flat", tone=5)
    i.L(18, 7, 18, 11, "white", w=2.6, mode="flat", tone=5, noline=True)
    return i


@icon("road")
def road():
    i = Icon()
    i.R(0, 4, 31, 29, "grass", mode="flat")
    i.P([(11, 4), (21, 4), (30, 29), (2, 29)], "iron", mode="cylx")
    for y0, y1 in ((5, 8), (12, 16), (20, 26)):
        i.P([(15.3, y0), (16.7, y0), (16.7 + (y1 - y0) * 0.12, y1), (15.3 - (y1 - y0) * 0.12, y1)], "yellow", mode="flat", tone=4)
    i.E(5, 10, 2.5, 2.5, "leaf"); i.E(27, 14, 3, 3, "leaf")
    return i


@icon("map-pin")
def mappin():
    i = Icon()
    i.E(16, 28, 7, 2, "dark", mode="flat", tone=2)
    i.P([(9, 15), (23, 15), (16, 28)], "red", mode="sphere", c=(16, 12, 9, 9))
    i.E(16, 12, 9, 9, "red")
    i.E(16, 12, 3.4, 3.4, "white", mode="flat", tone=5)
    return i


@icon("compass")
def compass():
    i = Icon()
    i.E(16, 16, 13, 13, "gold"); i.E(16, 16, 10.5, 10.5, "paper")
    i.P([(16, 6), (19, 16), (13, 16)], "red", mode="flat"); i.P([(16, 26), (19, 16), (13, 16)], "navy", mode="flat")
    i.E(16, 16, 1.5, 1.5, "gold")
    for x, y in ((16, 4), (28, 16), (16, 28), (4, 16)):
        i.D([(x, y)], "cocoa", 2)
    return i


@icon("luggage")
def luggage():
    i = Icon()
    i.R(11, 2, 20, 7, "dark", r=1); i.HR(13, 4, 18, 7)
    i.R(5, 6, 26, 27, "orange", r=2)
    for x in (10, 21):
        i.R(x, 6, x + 1, 27, "tomato", mode="flat", tone=2)
    i.E(14, 12, 2.5, 2, "sky", mode="flat"); i.R(15, 18, 19, 21, "lime", mode="flat")
    i.E(9, 29, 1.6, 1.6, "dark"); i.E(22, 29, 1.6, 1.6, "dark")
    return i


@icon("ticket")
def ticket():
    i = Icon()
    i.P([(2, 8), (30, 8), (30, 13), (28, 16), (30, 19), (30, 24), (2, 24), (2, 19), (4, 16), (2, 13)], "yellow")
    i.R(2, 8, 9, 24, "red", mode="flat"); i.HE(9, 8, 1.5, 1.5); i.HE(9, 24, 1.5, 1.5)
    for y in range(11, 23, 3):
        i.D([(10, y)], "cocoa", 2)
    i.P([(20, 11), (21.5, 14.5), (25, 15), (22, 17.5), (23, 21), (20, 19), (17, 21), (18, 17.5), (15, 15), (18.5, 14.5)], "red", mode="flat")
    return i


@icon("tent")
def tent():
    i = Icon()
    i.E(16, 28.5, 14, 2, "grass", mode="flat")
    i.P([(16, 4), (30, 27), (2, 27)], "orange", mode="cylx")
    i.P([(16, 4), (30, 27), (16, 27)], "tomato", mode="flat", tone=2)
    i.P([(16, 13), (21, 27), (11, 27)], "cocoa", mode="flat")
    i.L(16, 4, 16, 1, "darkwood", w=1.5); i.P([(16, 1), (21, 2.5), (16, 4)], "butter", mode="flat")
    return i


@icon("mountain")
def mountain():
    i = Icon()
    i.E(25, 7, 4, 4, "yellow")
    i.P([(19, 11), (31, 28), (8, 28)], "lav", mode="cylx")
    i.P([(11, 5), (24, 28), (0, 28)], "slate", mode="cylx")
    i.P([(11, 5), (15, 11), (13, 10), (11, 12), (8, 10), (7, 11)], "white", mode="flat", tone=4)
    i.R(0, 28, 31, 30, "grass", mode="flat")
    return i
