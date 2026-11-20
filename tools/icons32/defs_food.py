from engine import Icon
from reg import icon


@icon("pho")
def pho():
    i = Icon()
    i.L(19, 3, 27, 11, "wood", w=2); i.L(22, 2, 29, 9, "darkwood", w=2)
    i.E(16, 16, 13, 4, "noodle", mode="cyly")
    i.P([(3, 16), (29, 16), (26, 24), (21, 27), (11, 27), (6, 24)], "white", mode="cylx")
    i.R(5, 19, 26, 20, "sky", mode="flat")
    i.E(10, 15, 3, 1.6, "meat"); i.E(17, 14.5, 3, 1.5, "meat"); i.E(23, 15.5, 2.4, 1.4, "leaf"); i.E(13, 16.2, 1.6, 1, "lime")
    i.D([(20, 14), (21, 16), (14, 14)], "red", 3)
    i.R(12, 27, 20, 28, "white", mode="flat", tone=2)
    i.L(8, 5, 8, 10, "silver", w=1, mode="flat", tone=4, noline=True); i.L(12, 3, 12, 9, "silver", w=1, mode="flat", tone=4, noline=True)
    return i


@icon("rice")
def rice():
    i = Icon()
    i.E(16, 14, 11, 7, "rice")
    i.P([(3, 15), (29, 15), (26, 23), (21, 26), (11, 26), (6, 23)], "white", mode="cylx")
    i.R(5, 18, 26, 19, "red", mode="flat")
    i.D([(9, 21), (13, 21), (17, 21), (21, 21)], "red", 2)
    i.R(12, 26, 20, 27, "white", mode="flat", tone=2)
    i.D([(12, 9), (15, 8), (18, 10), (10, 12), (21, 12), (14, 11)], "cream", 2)
    i.L(20, 2, 28, 12, "darkwood", w=2); i.L(17, 3, 27, 13, "wood", w=2)
    return i


@icon("banh-mi")
def banhmi():
    i = Icon()
    i.P([(2, 20), (6, 12), (16, 7), (27, 7), (30, 11), (26, 19), (16, 24), (6, 24)], "bread", mode="sphere", c=(16, 15, 14, 9))
    i.P([(5, 19), (13, 13), (24, 10), (28, 12), (22, 17), (12, 21)], "lime", mode="flat")
    i.L(8, 18, 24, 12, "meat", w=2.2, mode="flat"); i.L(9, 20, 23, 15, "tomato", w=1.2, mode="flat", tone=4)
    i.L(10, 17, 22, 12.5, "orange", w=1, mode="flat", tone=4, noline=True)
    i.D([(12, 11), (17, 9), (22, 8), (14, 10), (25, 9)], "bread", 5)
    return i


@icon("coffee")
def coffee():
    i = Icon()
    i.E(25, 18, 5, 5.5, "white", mode="cylx"); i.HE(25, 18, 2.2, 2.8)
    i.R(5, 11, 22, 27, "white", mode="cylx", r=2)
    i.E(13.5, 12, 8.5, 2, "coffee", mode="flat", tone=2)
    i.D([(10, 12), (11, 12), (15, 11)], "tea", 3)
    i.R(4, 27, 27, 29, "steel", r=1)
    i.E(13.5, 20, 4, 3.5, "pink", mode="sphere")
    i.L(10, 3, 11, 8, "silver", w=1.5, mode="flat", tone=4, noline=True); i.L(16, 2, 15, 8, "silver", w=1.5, mode="flat", tone=4, noline=True)
    return i


@icon("phin")
def phin():
    i = Icon()
    i.R(8, 15, 23, 29, "glass", mode="cylx", r=1)
    i.R(9, 22, 22, 28, "coffee", mode="cylx"); i.R(9, 25, 22, 28, "milk", mode="cylx")
    i.R(5, 12, 26, 14, "steel", mode="cyly")
    i.R(9, 5, 22, 12, "silver", mode="cylx")
    i.R(11, 2, 20, 5, "steel", mode="cylx", r=1); i.R(14, 1, 17, 2, "iron", mode="flat")
    i.D([(15, 16), (15, 17), (16, 19)], "coffee", 2)
    i.D([(10, 17), (10, 18), (10, 19)], "#FFFFFF")
    return i


@icon("milk-tea")
def milktea():
    i = Icon()
    i.L(20, 1, 17, 12, "pink", w=2.4, mode="cylx")
    i.P([(7, 11), (25, 11), (22, 30), (10, 30)], "tea", mode="cylx")
    i.E(16, 10, 10, 3.5, "glass", mode="sphere")
    i.R(6, 11, 26, 12, "glass", mode="flat", tone=4)
    for x, y in ((11, 25), (14, 27), (17, 26), (20, 28), (13, 23), (19, 23), (16, 29)):
        i.E(x + 0.5, y + 0.5, 1.6, 1.6, "cocoa")
    i.R(9, 16, 23, 19, "cream", mode="flat")
    i.D([(14, 17), (15, 17), (16, 17), (17, 17)], "rose", 3)
    i.D([(9, 13), (9, 14)], "#FFFFFF")
    return i


@icon("cup-soda")
def cupsoda():
    i = Icon()
    i.L(20, 1, 18, 10, "sky", w=2.4, mode="cylx")
    i.P([(7, 10), (25, 10), (22, 30), (10, 30)], "red", mode="cylx")
    i.P([(8, 16), (24, 16), (23.2, 21), (8.8, 21)], "white", mode="cylx")
    i.D([(13, 18), (14, 18), (15, 18), (16, 18), (17, 18), (18, 18)], "red", 2)
    i.E(16, 9.5, 10.5, 2.8, "white", mode="cyly")
    i.D([(9, 12), (9, 13), (10, 23), (10, 24)], "#FFFFFF")
    return i


@icon("beer")
def beer():
    i = Icon()
    i.E(24, 18, 5, 6, "glass", mode="cylx"); i.HE(24, 18, 2.3, 3.4)
    i.R(5, 10, 21, 29, "gold", mode="cylx", r=1)
    i.E(8, 9, 4, 3.5, "cream"); i.E(13, 8, 4.5, 4, "cream"); i.E(18, 9, 4, 3.5, "cream"); i.E(10, 4.5, 3, 2.5, "cream")
    for x, y in ((9, 16), (14, 20), (11, 24), (17, 14), (16, 25)):
        i.D([(x, y)], "butter", 5)
    i.L(8, 13, 8, 26, "#FFFFFF" if False else "butter", w=1, mode="flat", tone=5, noline=True)
    return i


@icon("wine")
def wine():
    i = Icon()
    i.E(16, 10, 9, 8.5, "glass", mode="sphere"); i.HR(5, 0, 26, 3)
    i.E(16, 12, 8, 6, "berry", mode="sphere"); i.HR(6, 0, 26, 9)
    i.R(15, 18, 17, 26, "glass", mode="cylx")
    i.E(16, 27.5, 7, 2, "glass", mode="cyly")
    i.G([(10, 6), (10, 7), (9, 8)])
    return i


@icon("cake")
def cake():
    i = Icon()
    i.R(4, 15, 27, 28, "pink", mode="cylx", r=1)
    i.R(4, 20, 27, 22, "cream", mode="cylx")
    i.E(15.5, 15, 12, 3, "cream", mode="cyly")
    for x in (7, 12, 18, 23):
        i.E(x, 17.5, 1.6, 2.4, "cream", mode="cylx")
    i.E(11, 12, 2.6, 2.6, "red"); i.E(20, 12, 2.6, 2.6, "red")
    i.R(15, 4, 16, 12, "sky", mode="cylx"); i.E(15.5, 3, 1.4, 2, "orange", noline=True)
    i.D([(8, 25), (13, 24), (19, 26), (24, 24)], "white", 5)
    i.R(2, 28, 29, 30, "steel", r=1)
    return i


@icon("candy")
def candy():
    i = Icon()
    i.P([(2, 9), (11, 14), (11, 18), (2, 23), (4, 16)], "lav")
    i.P([(30, 9), (21, 14), (21, 18), (30, 23), (28, 16)], "lav")
    i.E(16, 16, 7.5, 7, "pink")
    for k in range(-1, 2):
        i.L(13 + k * 4, 10, 19 + k * 4, 22, "rose", w=1.4, mode="flat", tone=3, noline=True)
    i.HE(16, 16, 9, 9) if False else None
    i.G([(12, 11), (11, 12)])
    return i


@icon("apple")
def apple():
    i = Icon()
    i.E(16, 18, 11, 10.5, "red")
    i.L(16, 9, 17, 4, "darkwood", w=2); i.E(21, 6, 4.5, 2.2, "leaf")
    i.G([(10, 13), (11, 12), (10, 14)])
    return i


@icon("egg")
def egg():
    i = Icon()
    i.P([(4, 16), (7, 8), (14, 5), (22, 6), (28, 11), (27, 20), (21, 26), (11, 26), (5, 22)], "white", mode="sphere", c=(16, 15, 13, 11))
    i.E(16, 15, 6, 5.5, "egg")
    i.G([(13, 12), (14, 11), (12, 13)])
    return i


@icon("carrot")
def carrot():
    i = Icon()
    i.E(23, 6, 2.5, 5, "leaf"); i.E(27, 9, 5, 2.4, "green"); i.E(25, 4, 2, 4, "lime")
    i.P([(17, 9), (25, 15), (6, 29), (3, 28)], "orange", mode="sphere", c=(15, 18, 8, 8))
    for a, b in ((12, 16), (9, 21), (15, 13)):
        i.L(a, b, a + 3, b + 2, "orange", w=1, mode="flat", tone=1, noline=True)
    return i


@icon("chef")
def chef():
    i = Icon()
    i.R(8, 18, 24, 27, "white", mode="cylx", r=1)
    i.E(10, 13, 6, 6, "white"); i.E(22, 13, 6, 6, "white"); i.E(16, 9, 7.5, 7, "white")
    i.R(8, 22, 24, 24, "red", mode="flat")
    i.D([(10, 23), (14, 23), (18, 23), (22, 23)], "white", 4)
    return i


@icon("pizza")
def pizza():
    i = Icon()
    i.P([(3, 6), (29, 6), (16, 30)], "butter", mode="sphere", c=(16, 14, 13, 12))
    i.R(2, 3, 29, 7, "bread", mode="cyly", r=2)
    for x, y in ((10, 11), (19, 10), (15, 17), (21, 15)):
        i.E(x, y, 2.6, 2.4, "meat")
    i.E(12, 21, 1.5, 1.2, "leaf"); i.E(17, 23, 1.3, 1, "leaf")
    i.D([(14, 12), (8, 9), (23, 9), (17, 20)], "yellow", 5)
    return i


@icon("ice-cream")
def icecream():
    i = Icon()
    i.P([(9, 15), (23, 15), (16, 30)], "bread", mode="cylx")
    for k in range(4):
        i.L(10 + k * 4, 15, 16, 29, "crust", w=1, mode="flat", tone=2, noline=True)
    i.E(16, 12, 8, 6.5, "pink"); i.E(12, 7.5, 4.5, 4, "mint"); i.E(20, 7.5, 4.5, 4, "butter"); i.E(16, 3.5, 2, 2, "red")
    i.D([(13, 13), (19, 12), (16, 14), (11, 11)], "choc", 2)
    return i


@icon("fish")
def fish():
    i = Icon()
    i.P([(5, 9), (9, 16), (5, 23), (2, 16)], "teal")
    i.E(17, 16, 12, 8, "sky")
    i.P([(14, 8), (20, 4), (22, 9)], "teal")
    i.E(24, 14, 2, 2, "white", mode="flat", tone=4); i.D([(24, 14)], "black", 3)
    i.L(19, 11, 19, 21, "blue", w=1, mode="flat", tone=2, noline=True)
    for x, y in ((12, 13), (15, 18), (11, 19), (15, 12)):
        i.D([(x, y), (x + 1, y)], "sky", 5)
    i.D([(27, 18), (26, 18)], "rose", 2)
    return i


@icon("utensils")
def utensils():
    i = Icon()
    i.L(10, 14, 10, 30, "wood", w=3)
    for x in (6, 9, 12):
        i.L(x + 0.5, 2, x + 0.5, 10, "silver", w=1.6)
    i.R(6, 9, 13, 13, "silver", r=2)
    i.L(22, 15, 22, 30, "wood", w=3)
    i.E(22, 8, 5, 7, "silver")
    i.G([(20, 4), (19, 5), (19, 6)])
    return i


@icon("cart")
def cart():
    i = Icon()
    i.E(16, 7, 4, 4, "red"); i.E(10, 9, 3, 3, "lime"); i.E(21, 9, 3.5, 3, "orange")
    i.P([(5, 10), (28, 10), (25, 21), (8, 21)], "sky", mode="bevel")
    for x in (11, 16, 21):
        i.L(x, 11, x - 0.5, 20, "blue", w=1, mode="flat", tone=2, noline=True)
    i.L(8, 15.5, 26, 15.5, "blue", w=1, mode="flat", tone=2, noline=True)
    i.L(1, 5, 5, 5, "iron", w=2, mode="cyly"); i.L(5, 5, 9, 24, "steel", w=2)
    i.L(9, 24, 26, 24, "steel", w=2, mode="cyly")
    i.E(11, 27.5, 2.4, 2.4, "dark"); i.E(23, 27.5, 2.4, 2.4, "dark")
    return i


@icon("basket")
def basket():
    i = Icon()
    i.ring(16, 15, 11, 2.2, "darkwood"); i.HR(0, 14, 31, 31)
    i.E(11, 14, 4.5, 4.5, "red"); i.E(19, 13, 4, 5, "bread"); i.E(23, 15, 3.5, 3.5, "lime"); i.E(15, 11, 2.5, 4, "leaf")
    i.P([(3, 15), (29, 15), (26, 28), (6, 28)], "wood", mode="cylx")
    for y in (19, 23):
        i.L(4, y, 28, y, "darkwood", w=1, mode="flat", tone=2, noline=True)
    for x in (9, 14, 19, 24):
        i.L(x, 16, x, 27, "darkwood", w=1, mode="flat", tone=2, noline=True)
    i.R(2, 14, 29, 16, "darkwood", mode="cyly", r=1)
    return i
