"""Mẫu 32px bản 3: vẽ thẳng trên lưới 32 (toạ độ pixel), cạnh xiên đúng 45°, thêm chi tiết bằng nét 1px."""
from flat32 import Icon as _I
def Icon(): return _I(scale=1)
I = {}
def icon(f): I[f.__name__.replace('_', '-')] = f; return f
K = '3B3355'; W = 'FFFFFF'; G = 'A79FC0'; GL = 'E3DDF0'

@icon
def home():
    c = Icon()
    c.box(21, 4, 24, 10, 'wood')                                   # ống khói
    c.box(6, 15, 25, 28, 'cream')                                  # tường
    c.poly([(2, 16), (16, 2), (30, 16)], 'red')                    # mái 45°
    c.box(13, 19, 18, 28, 'wood', kind='flat')                     # cửa
    c.box(8, 18, 11, 21, 'sky', kind='flat'); c.box(20, 18, 23, 21, 'sky', kind='flat')
    c.seg(9, 18, 9, 21, W); c.seg(21, 18, 21, 21, W)               # ánh kính
    c.pts([(17, 24)], 'F5C451')                                    # tay nắm
    c.seg(10, 9, 13, 6, 'FFD0D0'); c.seg(14, 5, 15, 4, 'FFD0D0')   # vệt sáng mái
    return c

@icon
def coffee():
    c = Icon()
    c.box(2, 26, 27, 28, 'pink')                                   # đĩa
    c.box(20, 13, 26, 21, 'white', r=2); c.hole_box(22, 15, 24, 19)  # quai
    c.box(5, 9, 21, 26, 'white', r=2)                              # cốc
    c.box(6, 10, 20, 12, 'coffee', kind='flat', ol=False)          # mặt cà phê
    c.seg(8, 11, 11, 11, 'F3DDBF')
    c.box(5, 17, 21, 19, 'pink', kind='flat', ol=False)            # dải hồng
    c.pts([(10, 2), (11, 3), (11, 4), (10, 5), (10, 6), (11, 7)], G)   # khói
    c.pts([(15, 1), (16, 2), (16, 3), (15, 4), (15, 5), (16, 6)], G)
    return c

@icon
def pho():
    c = Icon()
    for x in (0, 5):                                               # đũa nghiêng 1:2
        c.seg(12 + x, 1, 17 + x, 11, 'B98058'); c.seg(13 + x, 1, 18 + x, 11, 'D9A47A')
    c.box(3, 12, 28, 14, 'broth', kind='flat')                     # nước dùng
    c.poly([(2, 15), (30, 15), (22, 23), (10, 23)], 'sky')         # tô 45°
    c.box(11, 23, 20, 26, 'blue')                                  # chân
    c.box(6, 11, 10, 12, 'meat', kind='flat'); c.box(20, 11, 24, 12, 'meat', kind='flat')
    c.pts([(14, 12), (16, 11), (18, 12)], '6FCF9F'); c.pts([(12, 13), (22, 13)], 'E8707E')
    for x in range(6, 26, 4): c.pts([(x, 18), (x + 1, 17), (x + 2, 18)], W)   # hoa văn sóng
    return c

@icon
def wallet():
    c = Icon()
    c.box(7, 4, 22, 10, 'mint', kind='flat')                       # tiền
    c.pts([(13, 6), (14, 6), (15, 6), (13, 7), (14, 8), (15, 8)], '6FCF9F')
    c.box(3, 9, 27, 27, 'wood', r=2)                               # ví
    c.seg(5, 12, 25, 12, 'B98058')
    for x in range(5, 19, 2): c.pts([(x, 25)], 'F2D2B0')           # đường chỉ
    c.box(18, 15, 29, 22, 'tan', r=1)                              # nắp cài
    c.box(22, 17, 24, 19, 'gold', kind='flat')
    return c

@icon
def piggy():
    c = Icon()
    c.box(13, 1, 18, 3, 'gold', kind='flat')                       # đồng xu rơi
    c.poly([(8, 11), (11, 5), (14, 8)], 'rose')                    # tai
    c.box(7, 22, 10, 28, 'pink'); c.box(19, 22, 22, 28, 'pink')    # chân
    c.ell(15, 16, 12.5, 9.5, 'pink', kind='round')                 # thân
    c.box(25, 13, 29, 19, 'rose', r=1)                             # mõm
    c.pts([(26, 15), (26, 16), (28, 15), (28, 16)], K)
    c.box(19, 11, 20, 13, 'dark', kind='flat', ol=False)           # mắt
    c.seg(12, 8, 17, 8, K)                                         # khe bỏ xu
    c.seg(21, 17, 22, 17, 'FF8FAB')                                # má
    c.pts([(2, 13), (1, 12), (2, 11), (3, 12)], 'FF8FAB')          # đuôi xoắn
    return c

@icon
def chart():
    c = Icon()
    c.box(2, 2, 29, 28, 'white', r=1)
    c.seg(5, 5, 5, 25, G); c.seg(5, 25, 26, 25, G)
    c.box(8, 18, 11, 24, 'mint', kind='flat'); c.box(14, 14, 17, 24, 'sky', kind='flat')
    c.box(20, 9, 23, 24, 'pink', kind='flat')
    c.seg(7, 14, 13, 8, 'F5C451'); c.seg(7, 15, 13, 9, 'F5C451')   # đường tăng 45°
    c.seg(13, 8, 16, 11, 'F5C451'); c.seg(16, 11, 22, 5, 'F5C451'); c.seg(16, 12, 22, 6, 'F5C451')
    c.pts([(20, 4), (21, 4), (22, 4), (23, 4), (23, 5), (23, 6), (23, 7)], 'E0A23C')
    return c

@icon
def car():
    c = Icon()
    c.poly([(6, 14), (12, 8), (21, 8), (27, 14)], 'red')           # mui 45°
    c.poly([(9, 13), (13, 9), (15, 9), (15, 13)], 'glass', kind='flat', ol=False)
    c.poly([(17, 9), (20, 9), (24, 13), (17, 13)], 'glass', kind='flat', ol=False)
    c.box(2, 13, 29, 23, 'red', r=2)                               # thân
    c.seg(16, 14, 16, 21, 'E8707E'); c.seg(13, 16, 14, 16, K); c.seg(18, 16, 19, 16, K)
    c.box(2, 16, 3, 18, 'yellow', kind='flat', ol=False); c.box(28, 16, 29, 18, 'orange', kind='flat', ol=False)
    for x in (8, 23):
        c.ell(x, 24, 4, 4, 'dark', kind='flat'); c.box(x - 1, 23, x, 24, 'grey', kind='flat', ol=False)
    return c

@icon
def gift():
    c = Icon()
    c.poly([(16, 10), (9, 3), (6, 6), (9, 10)], 'gold')           # nơ 45°
    c.poly([(16, 10), (23, 3), (26, 6), (23, 10)], 'gold')
    c.box(4, 14, 27, 28, 'lilac')                                  # hộp
    c.box(2, 9, 29, 14, 'purple')                                  # nắp
    c.box(14, 9, 17, 28, 'yellow', kind='flat')                    # ruy băng
    c.seg(4, 15, 27, 15, '9F86E0')
    return c

@icon
def heart():
    c = Icon()
    c.mask('''
....######.....######...
..##########.##########.
.#######################
########################
########################
########################
########################
.######################.
..####################..
...##################...
....################....
.....##############.....
......############......
.......##########.......
........########........
.........######.........
..........####..........
...........##...........
''', 4, 6, 'pink', kind='round')
    c.pts([(7, 9), (8, 9), (6, 10), (6, 11)], W)
    return c

@icon
def receipt():
    c = Icon()
    teeth = [(26, 29)]
    for i, x in enumerate(range(23, 5, -3)): teeth.append((x, 26 if i % 2 == 0 else 29))
    c.poly([(5, 2), (26, 2)] + teeth + [(5, 29)], 'white', kind='flat')
    c.seg(9, 6, 16, 6, K)
    for y, x1 in ((10, 21), (13, 18), (16, 21)): c.seg(9, y, x1, y, G)
    c.seg(9, 20, 13, 20, K); c.seg(17, 20, 21, 20, 'E8707E'); c.seg(17, 21, 21, 21, 'E8707E')
    return c

@icon
def banh_mi():
    c = Icon()
    c.poly([(10, 30), (2, 22), (2, 19), (19, 2), (22, 2), (30, 10), (30, 13), (13, 30)], 'bread', kind='round')  # ổ bánh xiên 45°
    c.poly([(7, 20), (20, 7), (23, 10), (10, 23)], 'leaf', kind='flat')      # rau
    c.poly([(9, 21), (21, 9), (22, 10), (10, 22)], 'meat', kind='flat', ol=False)  # chả
    for k in range(4):                                                        # vết rạch
        x, y = 6 + 5 * k, 25 - 5 * k
        c.seg(x + 3, y - 1, x + 5, y + 1, 'E3A45E')
    c.pts([(15, 14), (17, 12)], 'E8707E')
    return c

@icon
def coins():
    c = Icon()
    for y in (24, 19, 14):
        c.box(3, y, 19, y + 4, 'yellow', r=1); c.seg(5, y + 1, 17, y + 1, 'FFF3D6')
    c.ell(22, 10, 7.5, 7.5, 'gold', kind='flat')
    c.ell(22, 10, 5, 5, 'yellow', kind='flat', ol=False)
    c.seg(22, 6, 22, 14, 'E0A23C'); c.seg(20, 7, 21, 7, 'E0A23C'); c.seg(23, 13, 24, 13, 'E0A23C')
    return c
