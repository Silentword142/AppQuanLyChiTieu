from engine import Icon
I = {}
def icon(f): I[f.__name__.replace('_','-')] = f; return f

@icon
def home():
    c = Icon()
    c.rect(7, 15, 24, 27, 'cream')               # tường
    c.poly([(3,16),(16,4),(29,16),(26,18),(16,8.5),(6,18)], 'red')  # mái
    c.rect(21, 6, 24, 12, 'brown')               # ống khói
    c.rect(13, 19, 18, 27, 'wood', r=0)          # cửa
    c.rect(9, 18, 11, 21, 'sky'); c.rect(20, 18, 22, 21, 'sky')
    c.dot(17, 23, '#FFE27A'); c.dot(9, 18, '#FFFFFF'); c.dot(20, 18, '#FFFFFF')
    c.rect(5, 27, 26, 28, 'leaf')
    return c

@icon
def coffee():
    c = Icon()
    c.ell(25, 18, 5, 5, 'paper'); c.ell(25, 18, 2.2, 2.4, None)  # quai
    c.rect(6, 11, 24, 26, 'paper', r=3)
    c.rect(8, 11, 22, 13, 'coffee', ol=False)
    c.rect(4, 26, 27, 28, 'pink', r=1)           # đĩa
    c.rect(11, 17, 19, 20, 'rose', r=1)          # tim/nhãn
    for x, y in ((11,4),(12,5),(12,6),(11,7),(16,3),(17,4),(17,5),(16,6),(16,7)): c.dot(x, y, '#D9CFE6')
    c.dot(8, 15, '#FFFFFF'); c.dot(8, 16, '#FFFFFF')
    return c

@icon
def pho():
    c = Icon()
    for i, (x0, x1) in enumerate(((19, 26), (21, 28))): c.line(x0, 3+i, x1-6, 14, 'wood', 1)  # đũa
    c.ell(16, 15, 13, 4, 'broth')
    c.poly([(3,15),(29,15),(25,25),(7,25)], 'sky')        # tô
    c.rect(10, 25, 22, 27, 'blue')
    c.line(6, 19, 26, 19, 'white', 1)
    for x in range(7, 26, 4): c.dot(x, 21, '#5592D6')
    c.rect(8, 13, 12, 15, 'green', ol=False); c.rect(18, 13, 22, 14, 'pink', ol=False)    # hành, thịt
    c.rect(13, 12, 16, 14, 'noodle', ol=False)
    for x, y in ((9,5),(10,6),(10,7),(9,8),(14,4),(15,5),(15,6),(14,7)): c.dot(x, y, '#D9CFE6')
    return c

@icon
def wallet():
    c = Icon()
    c.rect(7, 6, 22, 13, 'mint', r=1)             # tờ tiền ló ra
    c.dot(14, 9, '#2F9257'); c.dot(15, 9, '#2F9257')
    c.rect(3, 10, 28, 27, 'wood', r=3)
    c.rect(18, 15, 29, 22, 'tan', r=2)            # nắp cài
    c.rect(22, 17, 24, 19, 'gold', ol=False)
    c.line(5, 13, 16, 13, 'tan')
    return c

@icon
def piggy():
    c = Icon()
    c.ell(15, 18, 12, 9, 'pink')
    c.poly([(8,10),(10,5),(13,10)], 'pink')       # tai
    c.rect(4, 25, 7, 28, 'pink'); c.rect(20, 25, 23, 28, 'pink')
    c.rect(25, 15, 29, 20, 'rose', r=1)           # mõm
    c.dot(26, 17, '#3B2A3F'); c.dot(28, 17, '#3B2A3F')
    c.dot(20, 14, '#3B2A3F'); c.dot(20, 13, '#3B2A3F')
    c.rect(11, 9, 17, 10, 'dark', ol=False)                 # khe xu
    c.dot(22, 18, '#FF9DB0'); c.dot(23, 18, '#FF9DB0')
    c.ell(14, 4, 3, 3, 'gold')                    # đồng xu rơi
    return c

@icon
def chart():
    c = Icon()
    c.rect(3, 4, 28, 27, 'paper', r=2)
    c.rect(7, 19, 10, 24, 'mint'); c.rect(12, 15, 15, 24, 'sky'); c.rect(17, 11, 20, 24, 'lilac'); c.rect(22, 7, 25, 24, 'rose')
    c.line(5, 25, 26, 25, 'grey')
    return c

@icon
def car():
    c = Icon()
    c.poly([(7,14),(10,7),(22,7),(26,14)], 'sky')
    c.rect(3, 13, 29, 23, 'red', r=3)
    c.line(16, 8, 16, 13, 'red')
    c.rect(4, 15, 6, 17, 'yellow', ol=False); c.rect(26, 15, 28, 17, 'gold', ol=False)
    c.ell(9, 24, 4, 4, 'dark'); c.ell(23, 24, 4, 4, 'dark')
    c.ell(9, 24, 1.6, 1.6, 'grey'); c.ell(23, 24, 1.6, 1.6, 'grey')
    c.dot(11, 9, '#FFFFFF'); c.dot(12, 9, '#FFFFFF')
    return c

@icon
def gift():
    c = Icon()
    c.rect(5, 14, 26, 28, 'lilac')
    c.rect(3, 10, 28, 15, 'purple')
    c.rect(14, 10, 17, 28, 'gold')
    c.ell(11, 7, 4.5, 3.5, 'gold'); c.ell(20, 7, 4.5, 3.5, 'gold')
    c.ell(11, 7, 1.6, 1.4, None); c.ell(20, 7, 1.6, 1.4, None)
    c.rect(14, 5, 17, 9, 'orange')
    return c

@icon
def heart():
    c = Icon()
    c.ell(10.5, 11.5, 7, 7, 'red'); c.ell(21.5, 11.5, 7, 7, 'red')
    c.poly([(3.6,13),(28.4,13),(16,27)], 'red')
    c.dot(7, 10, '#FFFFFF'); c.dot(7, 9, '#FFFFFF'); c.dot(8, 8, '#FFFFFF')
    return c

@icon
def receipt():
    c = Icon()
    c.poly([(6,3),(26,3),(26,28),(23,26),(20,28),(17,26),(14,28),(11,26),(8,28),(6,26)], 'paper')
    for y in (8, 12, 16): c.line(9, y, 22 if y != 12 else 18, y, 'grey')
    c.line(9, 21, 15, 21, 'rose'); c.line(18, 21, 22, 21, 'rose')
    return c

@icon
def banh_mi():
    c = Icon()
    c.poly([(3,22),(5,15),(10,11),(20,9),(27,10),(30,14),(28,19),(20,23),(9,25)], 'bread')
    c.poly([(7,18),(12,14),(22,12),(27,13),(24,17),(14,20)], 'leaf')
    c.poly([(9,18),(14,16),(24,15),(22,17),(13,19)], 'pink', ol=False)
    c.line(11, 17, 23, 14, 'red')
    for x in (9, 15, 21): c.line(x, 12+(1 if x==9 else 0), x+3, 11, 'gold')
    return c

@icon
def coins():
    c = Icon()
    for y in (24, 19, 14):
        c.rect(3, y, 21, y+3, 'gold', ol=True); c.ell(12, y, 9.5, 3.2, 'yellow')
    c.ell(23, 10, 7, 7, 'gold'); c.ell(23, 10, 4.5, 4.5, 'yellow')
    c.rect(22, 7, 23, 13, 'orange', ol=False)
    c.dot(19, 6, '#FFFFFF'); c.dot(20, 5, '#FFFFFF')
    return c
