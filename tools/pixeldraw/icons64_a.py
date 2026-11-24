from px64 import Icon
I = {}
def icon(f): I[f.__name__.replace('_', '-')] = f; return f
STEAM = '#E4DAF0'
def steam(c, x, y, h=10):
    for i in range(h):
        c.dot(x + (1 if (i // 3) % 2 else 0), y + i, STEAM); c.dot(x + 1 + (1 if (i // 3) % 2 else 0), y + i, STEAM)

@icon
def home():
    c = Icon()
    c.rect(42, 10, 49, 26, 'brown', kind='flat')                     # ống khói
    c.rect(13, 30, 51, 57, 'cream', kind='flat')                      # tường
    c.poly([(5, 33), (32, 7), (59, 33), (54, 37), (32, 16), (10, 37)], 'red', kind='soft')  # mái
    c.rect(27, 39, 38, 57, 'wood', r=1, kind='flat')                  # cửa
    c.ell(35, 49, 1.3, 1.3, 'gold')
    c.rect(16, 38, 23, 45, 'sky', kind='flat'); c.rect(42, 38, 49, 45, 'sky', kind='flat')
    c.rect(8, 56, 56, 60, 'leaf', r=2, kind='soft')                   # cỏ
    c.ell(14, 54, 4, 3.5, 'green'); c.ell(51, 54, 4, 3.5, 'green')
    return c

@icon
def coffee():
    c = Icon()
    c.ell(48, 36, 9, 9, 'paper', kind='soft'); c.hole_ell(48, 36, 4.5, 5)   # quai
    c.rect(11, 22, 49, 52, 'paper', r=6, kind='soft')                # cốc
    c.ell(30, 23, 19, 4, 'coffee', kind='flat', ol=False)            # mặt cà phê
    c.ell(28, 23, 6, 1.5, 'milk', kind='flat', ol=False)
    c.rect(11, 33, 49, 40, 'rose', kind='flat', ol=False)            # dải màu trên cốc
    c.ell(30, 36.5, 3.5, 3.5, 'paper', kind='round', ol=False)
    c.rect(4, 52, 57, 58, 'pink', r=3, kind='soft')                   # đĩa
    steam(c, 22, 5); steam(c, 31, 3, 12); steam(c, 40, 6)
    return c

@icon
def pho():
    c = Icon()
    c.line([(36, 4), (30, 26)], 'wood', w=3); c.line([(44, 6), (34, 27)], 'wood', w=3)   # đũa
    c.ell(32, 30, 27, 7, 'broth', kind='flat')                        # nước dùng
    c.poly([(4, 30), (60, 30), (52, 50), (12, 50)], 'sky', kind='soft')  # tô
    c.rect(20, 49, 44, 55, 'blue', r=2, kind='soft')                  # chân tô
    c.rect(9, 36, 55, 38, 'white', kind='flat', ol=False)             # vạch hoa văn
    c.ell(18, 28, 6, 3, 'meat', kind='round'); c.ell(44, 28, 6, 3, 'meat', kind='round')  # thịt bò
    c.ell(31, 27, 6, 2.5, 'noodle', kind='soft')                      # bánh phở
    c.ell(25, 31, 3, 1.6, 'green', kind='round'); c.ell(38, 31, 3, 1.6, 'leaf', kind='round')
    c.ell(52, 30, 2, 1.5, 'chili', kind='round')
    steam(c, 14, 10, 12); steam(c, 22, 8, 12)
    return c

@icon
def wallet():
    c = Icon()
    c.rect(14, 10, 46, 26, 'mint', r=2, kind='flat')                  # tiền
    c.ell(30, 18, 4, 4, 'green', kind='flat', ol=False)
    c.rect(5, 20, 57, 55, 'wood', r=7, kind='soft')                   # ví
    c.rect(36, 30, 60, 45, 'tan', r=5, kind='soft')                   # nắp cài
    c.ell(45, 37.5, 3.5, 3.5, 'gold', kind='round')
    c.line([(10, 25), (32, 25)], 'tan', w=2, kind='flat', ol=False)   # đường chỉ
    return c

@icon
def piggy():
    c = Icon()
    c.ell(28, 9, 6.5, 6.5, 'gold', kind='round')                      # đồng xu
    c.line([(28, 5.5), (28, 12.5)], 'orange', w=3, kind='flat', ol=False)
    c.poly([(14, 22), (18, 10), (26, 20)], 'rose', kind='soft')       # tai
    c.rect(9, 46, 17, 58, 'pink', r=2, kind='soft'); c.rect(38, 46, 46, 58, 'pink', r=2, kind='soft')  # chân
    c.ell(29, 36, 24, 18, 'pink', kind='round')                       # thân
    c.ell(53, 35, 6, 7, 'rose', kind='soft')                          # mõm
    c.rect(19, 18, 36, 21, 'dark', r=1, kind='flat', ol=False)        # khe
    c.ell(40, 28, 2.2, 2.6, 'dark', kind='flat', ol=False)            # mắt
    c.dot(39, 27, '#FFFFFF')
    c.ell(42, 38, 3, 1.8, 'red', kind='flat', ol=False)               # má hồng
    c.ell(51.5, 34, 1, 1.6, 'dark', kind='flat', ol=False); c.ell(55, 34, 1, 1.6, 'dark', kind='flat', ol=False)
    c.line([(5, 32), (3, 28), (6, 26)], 'rose', w=2, kind='flat')     # đuôi
    return c

@icon
def chart():
    c = Icon()
    c.rect(4, 6, 59, 57, 'paper', r=5, kind='soft')
    c.rect(12, 38, 19, 50, 'mint', r=1, kind='soft'); c.rect(23, 30, 30, 50, 'sky', r=1, kind='soft')
    c.rect(34, 22, 41, 50, 'lilac', r=1, kind='soft'); c.rect(45, 14, 52, 50, 'rose', r=1, kind='soft')
    c.line([(9, 51), (55, 51)], 'grey', w=2, kind='flat', ol=False)
    c.line([(14, 33), (26, 25), (37, 17), (49, 9)], 'gold', w=3, kind='round')
    return c

@icon
def car():
    c = Icon()
    c.poly([(14, 28), (20, 13), (44, 13), (52, 28)], 'glass', kind='flat')   # kính
    c.rect(4, 26, 60, 46, 'red', r=7, kind='soft')                    # thân
    c.rect(31, 13, 33, 28, 'red', kind='flat')                        # trụ giữa
    c.ell(9, 33, 3, 2.5, 'yellow', kind='round'); c.ell(55, 33, 3, 2.5, 'orange', kind='round')
    c.rect(24, 36, 40, 38, 'dark', r=1, kind='flat', ol=False)
    for x in (17, 47):
        c.ell(x, 47, 8, 8, 'dark', kind='round'); c.ell(x, 47, 3.5, 3.5, 'steel', kind='round')
    return c

@icon
def gift():
    c = Icon()
    c.ell(21, 13, 9, 7, 'gold', kind='round'); c.hole_ell(21, 13, 3.5, 2.8)
    c.ell(43, 13, 9, 7, 'gold', kind='round'); c.hole_ell(43, 13, 3.5, 2.8)
    c.rect(9, 28, 55, 58, 'lilac', kind='soft')
    c.rect(5, 20, 59, 30, 'purple', r=2, kind='soft')
    c.rect(28, 20, 36, 58, 'gold', kind='flat')
    c.ell(32, 18, 5, 4.5, 'orange', kind='round')
    return c

@icon
def heart():
    c = Icon()
    c.ell(20.5, 22, 14, 14, 'red', kind='round'); c.ell(43.5, 22, 14, 14, 'red', kind='round')
    c.poly([(7, 27), (57, 27), (32, 56)], 'red', kind='round')
    # gộp 3 khối thành một khối để bóng liền mạch
    m = (c.mat >= 0); c.shapes = []; c.mat[:] = -1; c._add(m, 'red', 'round', True)
    return c

@icon
def receipt():
    c = Icon()
    pts = [(12, 4), (52, 4), (52, 58)]
    for i, x in enumerate(range(52, 11, -5)):
        pts.append((x, 58 if i % 2 == 0 else 54))
    pts += [(12, 58)]
    c.poly(pts, 'paper', kind='flat')
    for y, x1 in ((14, 44), (22, 38), (30, 44)): c.line([(19, y), (x1, y)], 'grey', w=2, kind='flat', ol=False)
    c.line([(19, 40), (30, 40)], 'rose', w=2, kind='flat', ol=False); c.line([(36, 40), (45, 40)], 'rose', w=2, kind='flat', ol=False)
    c.ell(44, 49, 6, 6, 'mint', kind='round'); c.line([(41, 49), (43, 51), (47, 46)], 'white', w=2, kind='flat', ol=False)
    return c

@icon
def banh_mi():
    c = Icon()
    c.poly([(4, 42), (8, 30), (18, 22), (36, 18), (52, 19), (60, 26), (57, 36), (42, 44), (20, 49)], 'bread', kind='round')
    c.poly([(12, 36), (22, 28), (40, 24), (54, 26), (49, 33), (30, 39)], 'leaf', kind='soft')
    c.poly([(16, 36), (26, 31), (46, 29), (42, 33), (26, 37)], 'meat', kind='soft')
    c.ell(36, 31, 4, 1.8, 'red', kind='round', ol=False)
    for x in (16, 28, 40): c.line([(x, 24 - (x - 16) // 6), (x + 6, 21 - (x - 16) // 6)], 'gold', w=2, kind='flat', ol=False)
    return c

@icon
def coins():
    c = Icon()
    for y in (49, 40, 31):
        c.rect(5, y, 39, y + 7, 'gold', kind='soft'); c.ell(22, y, 17, 6, 'yellow', kind='flat')
    c.ell(45, 20, 14, 14, 'gold', kind='round'); c.ell(45, 20, 9, 9, 'yellow', kind='soft')
    c.rect(43, 13, 47, 27, 'orange', r=1, kind='flat', ol=False)
    return c
