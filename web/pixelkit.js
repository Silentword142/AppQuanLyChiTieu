/* PixelKit – bộ vẽ pixel dùng chung cho giao diện TV mới (dock, keycap, icon màu, đồng xu, bầu trời) */
(function(root){"use strict";var C={};
C.homec={g:[".....kk.....", "....krrk....", "...krrrrk...", "..krrrrrrk..", ".krrrrrrrrk.", "kkkkkkkkkkkk", ".kwwwwwwwwk.", ".kwbbwwddwk.", ".kwbbwwddwk.", ".kwwwwwddwk.", ".kkkkkkkkkk.", "............"],m:{"k": "#3B2A3F", "r": "#EE6B6E", "w": "#FFF6E9", "b": "#A5D8F7", "d": "#B98058"}};
C.chartc={g:[".........kkk", ".........kyk", "......kkkkyk", "......kgkkyk", "...kkkkgkkyk", "...kpkkgkkyk", "kkkkpkkgkkyk", "kbkkpkkgkkyk", "kbkkpkkgkkyk", "kbkkpkkgkkyk", "kkkkkkkkkkkk", "............"],m:{"k": "#3B2A3F", "b": "#6E9FDE", "p": "#F49CA6", "g": "#55B27F", "y": "#FFD36E"}};
C.maic={g:["..k......k..", "...k....k...", "kkkkkkkkkkkk", "klllllllllLk", "kldddddddlLk", "kldgdddgdlLk", "kldddddddlLk", "kldgggggdlLk", "kldddddddlLk", "klllllllllLk", "kkkkkkkkkkkk", ".kk......kk."],m:{"k": "#3B2A3F", "l": "#D3B9F2", "L": "#9C7BD3", "d": "#2A2140", "g": "#9DF5C2"}};
C.walletc={g:["............", "..kkkkkkkk..", ".kggggggggk.", "kkkkkkkkkkkk", "kccccccccCCk", "kcccccckkkkk", "kcccccckyyYk", "kcccccckkkkk", "kccccccccCCk", "kCCCCCCCCCCk", "kkkkkkkkkkkk", "............"],m:{"k": "#3B2A3F", "g": "#9BE0AE", "c": "#C98A5E", "C": "#84553F", "y": "#FFD36E", "Y": "#E0A23C"}};
C.upc={g:[".....kk.....", "....kppk....", "...kpppPk...", "..kpppppPk..", ".kppppppPPk.", "kkkkppPPkkkk", "...kppPPk...", "...kppPPk...", "...kppPPk...", "...kppPPk...", "...kkkkkk...", "............"],m:{"k": "#3B2A3F", "p": "#F49CA6", "P": "#D4506A"}};
C.camc={g:["............", "..kkkk......", "kkkddkkkkkkk", "kbbbbbbbbrbk", "kbbbkkkkbbbk", "kbbkwwwwkbbk", "kbbkwddwkbbk", "kbbkwddwkbbk", "kbbkwwwwkbbk", "kBBBkkkkBBBk", "kkkkkkkkkkkk", "............"],m:{"k": "#3B2A3F", "b": "#A5D8F7", "B": "#6E9FDE", "d": "#4A6FB0", "w": "#FFFFFF", "r": "#EE6B6E"}};
C.targetc={g:["...kkkkkk...", "..krrrrrrk..", ".krrwwwwrrk.", "krrwwrrwwrrk", "krwwrrrrwwrk", "krwrryyrrwrk", "krwrryyrrwrk", "krwwrrrrwwrk", "krrwwrrwwrrk", ".krrwwwwrrk.", "..krrrrrrk..", "...kkkkkk..."],m:{"k": "#3B2A3F", "r": "#EE6B6E", "w": "#FFF6E9", "y": "#FFD36E"}};
C.gearc={g:["....kkkk....", ".kk.kssk.kk.", "kssksssskssk", "ksssssssssSk", ".ksskkkksSk.", "kksskwwksSkk", "kksskwwksSkk", ".ksskkkksSk.", "kssssssssSSk", "ksSksSSkSSSk", ".kk.kSSk.kk.", "....kkkk...."],m:{"k": "#3B2A3F", "s": "#E3D8E0", "S": "#AE9DB0", "w": "#FFD36E"}};
C.downc={g:["............", "...kkkkkk...", "...kppPPk...", "...kppPPk...", "...kppPPk...", "...kppPPk...", "kkkkppPPkkkk", ".kppppppPPk.", "..kpppppPk..", "...kpppPk...", "....kppk....", ".....kk....."],m:{"k": "#3B2A3F", "p": "#9BE0AE", "P": "#55B27F"}};
function centerGrid(g){var H=g.length,W=0,i;for(i=0;i<H;i++)W=Math.max(W,g[i].length);var x0=W,x1=-1,y0=H,y1=-1;
 g.forEach(function(r,y){for(var x=0;x<r.length;x++){var c=r[x];if(c!=="."&&c!==" "){x0=Math.min(x0,x);x1=Math.max(x1,x);y0=Math.min(y0,y);y1=Math.max(y1,y)}}});
 if(x1<0)return g;var w=x1-x0+1,h=y1-y0+1,S=Math.max(w,h)+2,px=Math.floor((S-w)/2),py=Math.floor((S-h)/2),out=[];
 for(var y=0;y<S;y++){var r="";for(var x=0;x<S;x++){var gy=y-py+y0,gx=x-px+x0;r+=(gy>=y0&&gy<=y1&&gx>=x0&&gx<=x1)?(g[gy][gx]||"."):"."}out.push(r)}return out}
function gridSvg(rows,cmap){var H=rows.length,W=0,s="";rows.forEach(function(r){W=Math.max(W,r.length)});
 rows.forEach(function(r,y){for(var x=0;x<r.length;x++){var c=cmap[r[x]];if(c)s+='<rect x="'+x+'" y="'+y+'" width="1" height="1" fill="'+c+'"/>'}});
 return "data:image/svg+xml,"+encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="'+W+'" height="'+H+'" viewBox="0 0 '+W+' '+H+'" shape-rendering="crispEdges">'+s+'</svg>')}
function icon(name){var k=C[name+"c"];return k?gridSvg(centerGrid(k.g),k.m):null}
function mix(a,b,k){var p=function(h){return [1,3,5].map(function(i){return parseInt(h.slice(i,i+2),16)})},A=p(a),B=p(b);
 return "#"+A.map(function(v,i){return Math.round(v+(B[i]-v)*k).toString(16).padStart(2,"0")}).join("")}
/* Phím bấm pixel 32x30: mặt bóng loáng, thành dày, bóng đổ */
function keycap(face,side){var W=32,H=30,rows=[];
 function inR(x,y,x0,y0,x1,y1,r){if(x<x0||x>x1||y<y0||y>y1)return false;var cx=x<x0+r?x0+r:x>x1-r?x1-r:x,cy=y<y0+r?y0+r:y>y1-r?y1-r:y;return Math.hypot(x-cx,y-cy)<=r+.3}
 var m={k:"#3B2A3F",d:"rgba(59,42,63,.35)",w:"#FFFFFF",f:face,s:side,g:"rgba(255,255,255,.35)",l:mix(face,"#FFFFFF",.35)};
 for(var y=0;y<H;y++){var r="";for(var x=0;x<W;x++){var c=".";
  if(!inR(x,y,0,0,29,26,4)){if(inR(x,y,2,2,31,29,4))c="d"}
  else if(!inR(x,y,1,1,28,25,3))c="k";
  else if(y>=20)c=(y==20?"k":(y==23&&x%3==1&&x>3&&x<26)?"g":"s");
  else if(!inR(x,y,2,2,27,19,2))c=(x<6||y<6)?"w":"s";
  else{c="f";if(y<=4&&x>=4&&x<=24)c="w";else if((x+y)%13==0&&y>5&&y<18)c="l"}
  r+=c}rows.push(r)}
 return gridSvg(rows,m)}
/* Đồng xu GHI có vệt sáng quét */
function drawCoin(ctx,f){ctx.clearRect(0,0,28,28);var sh=(f%24)-6;
 for(var y=0;y<28;y++)for(var x=0;x<28;x++){var dx=x+.5-14,dy=y+.5-13,d=Math.hypot(dx,dy),col=null;
  if(d<=13){col="#FFD36E";if(d>11.8)col="#3B2A3F";else if(d>9.8)col=(dx+dy<-4)?"#FFF3CC":(dx+dy>4?"#E0A23C":"#FFD36E");else if(d>8.7)col="#E9B04E";
   var band=(x+y)/2-sh;if(d<=11.8&&band>=0&&band<2.2)col="#FFFBEA"}
  if(!col&&y>=26&&Math.abs(dx)<9)col="rgba(59,42,63,.35)";
  if(col){ctx.fillStyle=col;ctx.fillRect(x,y,1,1)}}
 ctx.fillStyle="#5A3D62";ctx.fillRect(12,7,3,13);ctx.fillRect(7,12,13,3);ctx.fillStyle="#3B2A3F";ctx.fillRect(12,19,3,1);ctx.fillRect(19,14,1,1)}
function drawSpark(ctx,f){ctx.clearRect(0,0,28,28);var pts=[[3,4],[24,6],[25,20],[2,18],[14,0]],k=Math.floor(f/3)%pts.length,ph=f%3,p=pts[k];
 ctx.fillStyle="#FFFFFF";ctx.fillRect(p[0],p[1],1,1);if(ph<2){ctx.fillStyle="#FFF3CC";ctx.fillRect(p[0]-1,p[1],1,1);ctx.fillRect(p[0]+1,p[1],1,1);ctx.fillRect(p[0],p[1]-1,1,1);ctx.fillRect(p[0],p[1]+1,1,1)}}
/* Bầu trời pixel động: dải màu dithering, mặt trời, đồi, mây 3 tầng, sao, sao băng, chim */
function sky(cv,opt){opt=opt||{};var g=cv.getContext("2d"),W,H,base,b,clouds,stars,birds,tick=0,shoot=null;
 var P=function(c,x,y,col){c.fillStyle=col;c.fillRect(x,y,1,1)};
 var CL={big:["......wwww..............","....wwhhhhww....wwww....","...whhhhhhhhw..whhhhw...",".wwhhhhhhhhhhwwhhhhhhww.","whhhhhhhhhhhhhhhhhhhhhhw","whhhhhhhhhhhhhhhhhhhhhhw","wsssssssssssssssssssssss",".ssssssssssssssssssssss.","...ssssss......ssssss..."],
  mid:["....wwww........","..wwhhhhww.www..",".whhhhhhhhwhhhw.","whhhhhhhhhhhhhhw","wsssssssssssssss",".ssssssssssssss."],sm:["..www....",".whhhwww.","whhhhhhhw","sssssssss"]};
 function build(){W=Math.ceil(innerWidth/4);H=Math.ceil(innerHeight/4);cv.width=W;cv.height=H;
  var cs=getComputedStyle(document.documentElement),s1=(cs.getPropertyValue("--sky1")||"#F0A3A2").trim(),s2=(cs.getPropertyValue("--sky2")||"#F8D3C6").trim(),gr=(cs.getPropertyValue("--ground")||"#E9C3B6").trim();
  var dark=document.documentElement.classList.contains("dark");
  var bands=[];for(var i=0;i<7;i++)bands.push(mix(mix(s1,"#C77C9E",dark?0:.18),s2,i/6));
  base=document.createElement("canvas");base.width=W;base.height=H;b=base.getContext("2d");var bh=Math.ceil(H/7);
  for(var y=0;y<H;y++){var bi=Math.min(6,Math.floor(y/bh)),t=y%bh;for(var x=0;x<W;x++){var c=bands[bi];
   if(bi<6&&t>=bh-5){var k=bh-t;if((k<=2&&(x+y)%2===0)||(k>2&&x%2===0&&y%2===0))c=bands[bi+1]}P(b,x,y,c)}}
  var sx=Math.round(W*.72),sy=H-30;for(y=-14;y<=14;y++)for(x=-14;x<=14;x++){var d=Math.hypot(x,y);
   if(d<=11)P(b,sx+x,sy+y,(y>3&&y%3===0)?s2:(dark?"#FFE9B8":"#FFE7B0"));else if(d<=13&&(x+y)%2===0)P(b,sx+x,sy+y,dark?"rgba(255,233,184,.5)":"#FFE2C2")}
  [[26,mix(gr,"#D98FA0",.4),mix(gr,"#C07890",.5),5,2,.07],[19,mix(gr,"#CE8390",.5),mix(gr,"#B87080",.6),4,1,.11],[11,mix(gr,"#BE7380",.6),mix(gr,"#A0607A",.7),3,1,.15]].forEach(function(L){
   for(var x=0;x<W;x++){var h=Math.round(L[0]+L[3]*Math.sin(x*L[5]+L[0])+L[4]*Math.sin(x*L[5]*2.7));for(var y=H-h;y<H;y++)P(b,x,y,y===H-h?L[2]:L[1])}});
  clouds=[];[["big",.04,14,.05],["mid",.6,34,.09],["sm",.3,6,.14],["big",.7,74,.05],["sm",.12,58,.14],["mid",.26,104,.09],["sm",.8,128,.14],["mid",0,150,.09],["big",.5,180,.05]].forEach(function(c){if(c[2]<H-40)clouds.push({t:c[0],x:c[1]*W,y:c[2],sp:c[3]})});
  stars=[];for(i=0;i<60;i++)stars.push({x:(i*37+11)%W,y:(i*53+7)%Math.floor(H*.7),p:i%7,big:i%3===0});
  birds=[{x:20,y:46,sp:.25},{x:28,y:50,sp:.25},{x:24,y:43,sp:.25}]}
 var CC={w:"#FFE9E3",h:"#FFF8F3",s:"#F6CFC8"};
 function draw(){g.drawImage(base,0,0);
  stars.forEach(function(s){var ph=(tick+s.p*3)%21;if(ph>15)return;P(g,s.x,s.y,ph<5?"#FFFFFF":"#FFEFD8");
   if(s.big&&ph<8){[[1,0],[-1,0],[0,1],[0,-1]].forEach(function(o){P(g,s.x+o[0],s.y+o[1],"#FFE9C8")});if(ph<3)[[2,0],[-2,0],[0,2],[0,-2]].forEach(function(o){P(g,s.x+o[0],s.y+o[1],"rgba(255,255,255,.6)")})}});
  if(!shoot&&tick%90===40)shoot={x:W-10,y:8};
  if(shoot){for(var i=0;i<6;i++)P(g,shoot.x+i,shoot.y-Math.floor(i/2),i===0?"#FFFFFF":"rgba(255,246,233,"+(1-i/6)+")");shoot.x-=2;shoot.y+=1;if(shoot.x<-6)shoot=null}
  clouds.forEach(function(c){var s=CL[c.t],w=s[0].length,x=Math.round(c.x)%(W+w);if(x<-w)x+=W+w;x=x>W?x-(W+w):x;
   s.forEach(function(r,y){for(var i=0;i<r.length;i++){var col=CC[r[i]];if(col)P(g,x+i,c.y+y,col)}});c.x+=c.sp});
  birds.forEach(function(bd){var f=Math.floor(tick/3)%2,x=Math.round(bd.x)%(W+10)-5,c="#8C5A70";
   if(f){P(g,x,bd.y,c);P(g,x+1,bd.y+1,c);P(g,x+2,bd.y,c)}else{P(g,x,bd.y+1,c);P(g,x+1,bd.y,c);P(g,x+2,bd.y+1,c)}bd.x+=bd.sp});
  tick++}
 build();draw();var reduce=matchMedia("(prefers-reduced-motion: reduce)").matches,iv=reduce?0:setInterval(function(){if(!document.hidden)draw()},180);
 var skyKey=function(){var cs=getComputedStyle(document.documentElement);return [innerWidth,innerHeight,cs.getPropertyValue("--sky1"),cs.getPropertyValue("--sky2"),cs.getPropertyValue("--ground"),document.documentElement.classList.contains("dark")].join("|")},lastKey=skyKey(),rt=0;
 var onR=function(){clearTimeout(rt);rt=setTimeout(function(){var k=skyKey();if(k===lastKey)return;lastKey=k;build();draw()},120)};addEventListener("resize",onR);
 var mo=new MutationObserver(onR);mo.observe(document.documentElement,{attributes:true,attributeFilter:["class","data-theme","style"]});
 return function(){clearInterval(iv);removeEventListener("resize",onR);mo.disconnect()}}

/* Khung pixel 9-slice nhiều lớp bóng (viền sáng, thân, mép tối, 2 lớp bóng đổ) */
var FRAME=["..kkkkkk....",".kHHHHHHk...","kHHHHHHHhk..","kHhbbbbbskd.","kHhbbbbbskde","kHhbbbbbskde","kHhbbbbbskde","kmmmmmmmmkde","kSSSSSSSSkde",".kSSSSSSkdde","..kkkkkkddee","...ddddddee.","....eeeeee.."];
var FR={tvRose:{b:"#F7A8BC",h:"#FFD4DF",s:"#DC7F98"},tvTeal:{b:"#8EDBC9",h:"#C6F2E7",s:"#5DB7A3"},tvWood:{b:"#FFC796",h:"#FFE5C9",s:"#E69F66"},
 tvIndigo:{b:"#BBA7F2",h:"#E0D5FF",s:"#927BD6"},tvBlue:{b:"#9BCDF5",h:"#D0E9FF",s:"#6AA6DB"},tvWine:{b:"#FF9FAE",h:"#FFD0D8",s:"#E3707F"}};
var frameCache={};
function frame(name){if(frameCache[name])return frameCache[name];var f=FR[name]||FR.tvRose;
 var m={k:"#3B2A3F",d:"rgba(59,42,63,.42)",e:"rgba(59,42,63,.18)",H:mix(f.b,"#FFFFFF",.55),h:f.h,b:f.b,s:f.s,m:mix(f.b,"#3B2A3F",.55),S:mix(f.b,"#3B2A3F",.28)};
 return frameCache[name]=gridSvg(FRAME,m)}
// Khung 9-slice từ 1 mã màu bất kỳ (ô phím, nút nhỏ trên thân TV)
function framec(hex){var key="#"+hex;if(frameCache[key])return frameCache[key];var f={b:hex,h:mix(hex,"#FFFFFF",.5),s:mix(hex,"#3B2A3F",.2)};
 var m={k:"#3B2A3F",d:"rgba(59,42,63,.42)",e:"rgba(59,42,63,.18)",H:mix(f.b,"#FFFFFF",.6),h:f.h,b:f.b,s:f.s,m:mix(f.b,"#3B2A3F",.5),S:mix(f.b,"#3B2A3F",.25)};
 return frameCache[key]=gridSvg(FRAME,m)}
// Ăng-ten TV pixel 40x14
function antenna(body){var r=[];for(var y=0;y<14;y++)r.push(Array(41).join("."));var set=function(x,y,c){r[y]=r[y].slice(0,x)+c+r[y].slice(x+1)};
 for(var i=0;i<11;i++){set(8+i,2+i,"k");set(31-i,2+i,"k")}
 [[7,1],[32,1]].forEach(function(p){for(var dy=-1;dy<=1;dy++)for(var dx=-1;dx<=1;dx++)set(p[0]+dx,p[1]+dy,"k");set(p[0],p[1],"y")});
 for(var x=16;x<24;x++){set(x,11,"k");set(x,12,x>16&&x<23?"b":"k");set(x,13,x>16&&x<23?"b":"k")}
 return gridSvg(r,{k:"#3B2A3F",y:"#FFD36E",b:body||"#F7A8BC"})}
function frameColor(name){return (FR[name]||FR.tvRose)}

root.PixelKit={frame:frame,framec:framec,antenna:antenna,frameColor:frameColor,icon:icon,keycap:keycap,drawCoin:drawCoin,drawSpark:drawSpark,sky:sky,gridSvg:gridSvg,mix:mix};
})(window);
