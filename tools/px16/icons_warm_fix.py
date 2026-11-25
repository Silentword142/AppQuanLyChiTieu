"""Bản vẽ lại cho kiểu pixel ấm (góp ý anh Hoàng 2026-11-25 09:52): xe cộ, quần áo, tiền giấy, người."""
F = {}
# ---------- xe cộ (nhìn chính diện, đối xứng như logo) ----------
F['car'] = """
...rrrrrrrr...
..rnbbbbbbbr..
..rbbbbbbbbr..
.rrrrrrrrrrrr.
rrrrrrrrrrrrrr
rhhrrrrrrrrhhr
rrrrGGGGGGrrrr
RRRRRRRRRRRRRR
.dd........dd.
"""
F['taxi'] = """
.....oooo.....
.....oooo.....
...yyyyyyyy...
..ynbbbbbbby..
..ybbbbbbbby..
.yyyyyyyyyyyy.
yyddyyddyyddyy
yhhyyyyyyyyhhy
yyyyGGGGGGyyyy
YYYYYYYYYYYYYY
.dd........dd.
"""
F['bus'] = """
.oooooooooooo.
.oyyyyyyyyyyo.
.oooooooooooo.
.onbbbbbbbbbo.
.obbbbbbbbbbo.
.obbbbbbbbbbo.
.oooooooooooo.
.ohhoooooohho.
.oooooooooooo.
.OOOOOOOOOOOO.
.dd........dd.
"""
F['train'] = """
....llllll....
..llllllllll..
.llnbbbbbbbll.
.llbbbbbbbbll.
.llbbbbbbbbll.
.llllllllllll.
.lhhllllllhhl.
.llllllllllll.
..LLLLLLLLLL..
...dd....dd...
..dd......dd..
"""
F['plane'] = """
......bb......
.....bnbb.....
.....bbbb.....
.....bbbb.....
..bbbbbbbbbb..
bbbbbbbbbbbbbb
bbbbbbbbbbbbbb
.....bbbb.....
.....bbbb.....
.....bbbb.....
...bbbbbbbb...
....bbbbbb....
"""
F['scooter'] = """
..........ddd.
...........p..
..ddddd....p..
.ddddddd...pp.
.ppppppp...pp.
pppppppp..ppp.
ppppppppppppp.
.PPPPPPPPPPPP.
..ddd....ddd..
..ddd....ddd..
"""
F['bike'] = """
...........00.
...000......0.
....8......80.
....8888888...
...88.8..8.8..
.0008..8.80000
0..80..8.8..80
0.8888888...0.
0...0....0...0
.000......000.
"""
F['ship'] = """
......rr......
......rr......
....wwwwww....
....wbwbbw....
..wwwwwwwwww..
bbbbbbbbbbbbbb
.bbnbbbnbbbnb.
..BBBBBBBBBB..
.4..4..4..4..4
"""
# ---------- quần áo ----------
F['shirt'] = """
..bbbb.bbbb..
.bbbbbwbbbbb.
bbbbbbbbbbbbb
bbbbbbbbbbbbb
.bb.bbbbb.bb.
....bbbbb....
....bbbbb....
....bbbbb....
....BBBBB....
"""
F['ao-dai'] = """
....lll....
..lllllll..
.lllllllll.
lllllllllll
ll.lllll.ll
ll.lllll.ll
kk.lllll.kk
...lllll...
...llLll...
..lllLlll..
..lllLlll..
.llllLllll.
.llllLllll.
"""
F['shoe'] = """
.rrrrr........
.rwrwr........
.rrwrwrr......
.rrrwrwrrr....
.rrrrrrrrrrrr.
.rrrrrrrrrrrrr
wwwwwwwwwwwwww
GGGGGGGGGGGGGG
"""
# ---------- tiền ----------
F['banknote'] = """
mmmmmmmmmmmmmm
m5mmmmmmmmmm5m
mmmmmaaaammmmm
mmmmaaMMaammmm
mm5maMMMMam5mm
mmmmaaMMaammmm
mmmmmaaaammmmm
m5mmmmmmmmmm5m
MMMMMMMMMMMMMM
"""
F['salary'] = """
..mmmmmmmmmmm.
..maaaaaaaaam.
.mmmmmmmmmmmm.
.m5mmmaammm5m.
.mmmmaMMammmm.
.mmmmaMMam.yy.
.mmmmmaam.yyyy
.MMMMMMMM.yhyy
..........yyyy
...........YY.
"""
# ---------- người ----------
F['user'] = """
....dddddd....
...dddddddd...
..dddkkkkddd..
..ddkkkkkkdd..
..dk0kkkk0kd..
..dkkkkkkkkd..
...kekkkkek...
....kkkkkk....
.....kkkk.....
...bbbbbbbb...
..bbbbnbbbbb..
.bbbbbbbbbbbb.
.bbbbbbbbbbbb.
"""
F['users'] = """
.........ddd..
..ddd...ddddd.
.ddddd..dkkkd.
.dkkkd..k0k0k.
.k0k0k..kkkkk.
.kkkkk...kkk..
..kkk...ppppp.
.bbbbb.ppppppp
bbbbbbbppppppp
bbbbbbbppppppp
bbbbbbbppppppp
"""
F['baby'] = """
.....d.....
......d....
..kkkkkkk..
.kkkkkkkkk.
kkkkkkkkkkk
kk0kkkkk0kk
kkkkkkkkkkk
kekkpppkkek
kkkkkpkkkkk
.kkkkkkkkk.
..kkkkkkk..
"""
F['chef'] = """
..www.www..
.wwwwwwwww.
.wwwwwwwww.
..wwwwwww..
..ggggggg..
..kkkkkkk..
.kk0kkk0kk.
.kkkkkkkkk.
.kekcccekk.
..kkkkkkk..
...kkkkk...
"""
F['yoga'] = """
.....ddd.....
....ddddd....
....dkkkd....
....kkkkk....
.....kkk.....
...lllllll...
..lllllllll..
.kk.lllll.kk.
.kk.lllll.kk.
.LLLLLLLLLLL.
ppppppppppppp
"""
F['hand-coins'] = """
....yyyyy.....
...yyhyyyy....
...yyyyyyy....
...yyyyyyy....
....yyyyy.....
..............
k.........kk..
kk.......kkk..
kkkkkkkkkkkk..
.kkkkkkkkkk...
..bbbbbbbb....
"""
F['contact'] = None  # giữ bản cũ
F = {k: v for k, v in F.items() if v}

# icon nên đối xứng (theo góp ý 09:53): lật nửa trái sang phải
SYM = {'lamp', 'umbrella', 'wine', 'paw', 'phin', 'graduation', 'ice-cream', 'tooth', 'mountain', 'cup-soda',
       'gift', 'heart', 'train', 'cloud-up', 'radio', 'sprout', 'camera', 'school', 'printer', 'milk-tea',
       'basket', 'calculator', 'chef', 'settings', 'dog', 'user', 'ao-dai', 'car', 'bus', 'taxi', 'plane',
       'shirt', 'banknote', 'baby', 'yoga'}
