"""4 mẫu icon app Spendy pixel 32x32 (A TV, B đồng xu có cánh, C ví, D chữ S). Chạy trực tiếp để xuất ảnh mẫu + sheet.png."""
import math, sys
from PIL import Image
N=32
INK=(0x3B,0x2A,0x45)
def hexc(h): h=h.lstrip('#'); return tuple(int(h[i:i+2],16) for i in (0,2,4))
class C:
    def __init__(s): s.g=[[None]*N for _ in range(N)]
    def p(s,x,y,c):
        if 0<=x<N and 0<=y<N: s.g[y][x]=hexc(c) if isinstance(c,str) else c
    def rect(s,x0,y0,x1,y1,c):
        for y in range(y0,y1+1):
            for x in range(x0,x1+1): s.p(x,y,c)
    def rrect(s,x0,y0,x1,y1,c,r=2):
        for y in range(y0,y1+1):
            for x in range(x0,x1+1):
                dx=max(x0+r-x,0,x-(x1-r)); dy=max(y0+r-y,0,y-(y1-r))
                if dx*dx+dy*dy<=r*r+0.5: s.p(x,y,c)
    def disc(s,cx,cy,r,c):
        for y in range(N):
            for x in range(N):
                if (x-cx)**2+(y-cy)**2<=r*r: s.p(x,y,c)
    def bitmap(s,x0,y0,rows,pal):
        for j,row in enumerate(rows):
            for i,ch in enumerate(row):
                if ch in pal: s.p(x0+i,y0+j,pal[ch])
    def outline(s,col=INK):
        add=[]
        for y in range(N):
            for x in range(N):
                if s.g[y][x] is None:
                    for dx,dy in((1,0),(-1,0),(0,1),(0,-1)):
                        xx,yy=x+dx,y+dy
                        if 0<=xx<N and 0<=yy<N and s.g[yy][xx] is not None and s.g[yy][xx]!=col: add.append((x,y));break
        for x,y in add: s.g[y][x]=col
    def over(s,o):
        for y in range(N):
            for x in range(N):
                if o.g[y][x] is not None: s.g[y][x]=o.g[y][x]
    def shadow(s,o,col,dx=1,dy=1):
        for y in range(N):
            for x in range(N):
                if o.g[y][x] is not None and 0<=x+dx<N and 0<=y+dy<N and s.g[y+dy][x+dx] is not None: s.g[y+dy][x+dx]=hexc(col)
    def img(s,scale=16):
        im=Image.new('RGBA',(N,N),(0,0,0,0))
        for y in range(N):
            for x in range(N):
                if s.g[y][x]: im.putpixel((x,y),s.g[y][x]+(255,))
        return im.resize((N*scale,N*scale),Image.NEAREST)

def tile(top,bot,hi,lo):
    t=C()
    for y in range(1,31):
        k=(y-1)/29; c=tuple(round(a+(b-a)*k) for a,b in zip(hexc(top),hexc(bot)))
        # banded gradient (4 bands)
        k=int(k*4)/3; c=tuple(round(a+(b-a)*k) for a,b in zip(hexc(top),hexc(bot)))
        for x in range(1,31): t.p(x,y,c)
    m=C(); m.rrect(1,1,30,30,'#000000',3)
    for y in range(N):
        for x in range(N):
            if m.g[y][x] is None: t.g[y][x]=None
    for x in range(4,28): t.p(x,2,hi)
    for y in range(4,28): t.p(2,y,hi)
    for x in range(4,28): t.p(x,29,lo)
    for y in range(4,28): t.p(29,y,lo)
    t.outline(); return t
def sparkle(c,x,y,col='#FFF8EE'):
    for dx,dy in((0,0),(1,0),(-1,0),(0,1),(0,-1)): c.p(x+dx,y+dy,col)

S_BIG=["  ####  "," ##  ## "," ##     ","  ####  ","     ## "," ##  ## ","  ####  "]
S7=[" ##### ","##   ##","##     "," ##### ","     ##","##   ##"," ##### "]
S5=[" ### ","#    "," ### ","    #"," ### "]

def coin(o,cx,cy,r,S=S7):
    o.disc(cx,cy,r,'#F2B24C'); o.disc(cx-0.6,cy-0.6,r-1.2,'#FFD87E')
    # rim highlight
    for a in range(200,290,6):
        x=round(cx+(r-1.5)*math.cos(math.radians(a))); y=round(cy+(r-1.5)*math.sin(math.radians(a))); o.p(x,y,'#FFF3C4')
    w=len(S[0]); h=len(S)
    o.bitmap(round(cx-w/2+0.01),round(cy-h/2+0.01),S,{'#':'#C9781F'})

out={}
# ---- A: CRT TV showing coin S ----
t=tile('#9FD3F5','#CDEBFF','#E6F6FF','#7FB6DE')
for x,y in((6,6),(25,9),(8,25)): sparkle(t,x,y)
o=C()
# antennas
for i in range(5): o.p(11+i//1-i,4+i,'#6E5A7A') if False else None
for i in range(4): o.p(12-i,6-i if False else 7-i,'#6E5A7A'); o.p(19+i,7-i,'#6E5A7A')
o.p(8,3,'#FF8FA3'); o.p(23,3,'#FF8FA3')
o.rrect(5,9,26,26,'#FFB7C5',2)          # body
o.rect(6,10,25,10,'#FFD6DE')            # top highlight
o.rect(6,25,25,25,'#E58A9E')
o.rrect(7,11,20,24,'#2F2440',2)         # screen bezel
o.rrect(8,12,19,23,'#4FD6C2',1)         # screen
for y in range(12,24,2): o.rect(8,y,19,y,'#43C1AE')
o.p(9,13,'#B8FFF3'); o.p(10,13,'#B8FFF3'); o.p(9,14,'#B8FFF3')
o.bitmap(11,14,S5,{'#':'#FFF8D6'})
o.disc(23,14,1.2,'#FFD87E'); o.disc(23,18,1.2,'#7FD1A0')
o.rect(22,21,24,21,'#C9708A'); o.rect(22,23,24,23,'#C9708A')
o.rect(8,27,9,28,'#6E5A7A'); o.rect(22,27,23,28,'#6E5A7A')
o.outline(); t.shadow(o,'#8EBFE3',1,1); t.over(o); out['A_tv']=t
# ---- B: winged coin ----
t=tile('#FFB0C0','#FFD9C9','#FFE6EC','#E68FA3')
for x,y in((6,7),(26,24),(24,6)): sparkle(t,x,y)
o=C()
wing=["#       ","###     ","#####   "," ###### "," #######","  ######","   ## ##"]
def wingdraw(o,x0,y0,flip):
    for j,row in enumerate(wing):
        for i,ch in enumerate(row):
            if ch=='#':
                xx=x0+(7-i if flip else i); col='#FFFFFF' if j<4 else '#E6E9FF'
                if j>=6: col='#C9CDF2'
                o.p(xx,y0+j,col)
wingdraw(o,3,9,False); wingdraw(o,21,9,True)
coin(o,15.5,17,7.6)
o.outline(); t.shadow(o,'#E3909F',1,1); t.over(o); out['B_wings']=t
# ---- C: wallet with coin popping ----
t=tile('#B9EBC9','#E6F8D8','#EFFFF2','#86C99C')
for x,y in((6,6),(26,26)): sparkle(t,x,y)
o=C()
coin(o,16,9,5.2,S5)
o.rrect(5,13,26,26,'#FF8FA3',2); o.rect(6,14,25,14,'#FFB7C5'); o.rect(6,25,25,25,'#D9667F')
o.rrect(17,17,27,22,'#E5607C',1); o.disc(21,19.5,1.3,'#FFD87E')
for x in range(7,16,2): o.p(x,16,'#FFC9D3')
o.outline(); t.shadow(o,'#7DB892',1,1); t.over(o); out['C_wallet']=t
# ---- D: bold S lettermark with coin dot (arcade) ----
t=tile('#FFD36E','#FFE9A8','#FFF4CE','#E0A93C')
o=C()
SB=["  ########  "," ########## ","####    ####","####        ","####        "," #########  ","  ######### ","        ####","        ####","####    ####"," ########## ","  ########  "]
for j,row in enumerate(SB):
    for i,ch in enumerate(row):
        if ch=='#': o.p(9+i,10+j,'#FF7A93' if j<6 else '#F25F7E')
[o.p(11+i,10,'#FFC2CD') for i in range(8)]; [o.p(10+i,11,'#FFB3C1') for i in range(2)]
coin(o,24.5,7.5,3.8,["#"])
o.outline(); t.shadow(o,'#D9932B',1,1); t.over(o); out['D_letter']=t
if __name__=='__main__':
  for k,v in out.items(): v.img().save(k+'.png')
  sheet=Image.new('RGBA',(4*256+5*24,256+80),(250,246,240,255))
  from PIL import ImageDraw
  d=ImageDraw.Draw(sheet)
  for i,(k,v) in enumerate(out.items()):
      sheet.paste(v.img(8),(24+i*280,24),v.img(8)); d.text((24+i*280+100,296),k[0],fill=(60,40,70))
  sheet.save('sheet.png')
