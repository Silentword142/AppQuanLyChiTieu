#!/usr/bin/env python3
"""Spendy · quy trình vẽ icon + hoạt ảnh trên Procreate (xem docs/ART-WORKFLOW.md)

Nguồn gốc của toàn bộ hình vẽ là 2 ảnh trong thư mục art/:
  art/icons.png  + art/icons.txt   – mọi icon 32×32 trên 1 ảnh, 16 ô / hàng
  art/anims.png  + art/anims.txt   – mọi hoạt ảnh trên 1 ảnh, mỗi hàng 1 hoạt ảnh, tối đa 12 khung
Ảnh xuất từ Procreate ở cỡ gốc (1×) hoặc phóng to số nguyên (2×, 4×, 8×…) đều được.
Một hoạt ảnh cũng có thể nằm riêng: art/anims/<tên>.gif hoặc .png động (Procreate Animation Assist),
khi đó file riêng được ưu tiên hơn hàng trong anims.png.

  python3 tools/art.py                 build: cắt ảnh → kiểm tra → nhúng vào web/icons.js → art/out/ (ảnh xem trước)
  python3 tools/art.py check           chỉ kiểm tra, không ghi gì
  python3 tools/art.py export          (khởi tạo/làm mới) xuất icons.png từ icons.js + guide + swatches
  python3 tools/art.py starter [tên…]  vẽ lại hoạt ảnh mẫu (tự động) vào anims.png cho các hàng đã liệt kê
  python3 tools/art.py vector tên…     render icon từ bản vector (tools/icons32/vec_*.py) vào ô của nó trên icons.png
  python3 tools/art.py guides          chỉ vẽ lại art/guide/* (sau khi thêm hàng/ô mới)
"""
import base64, io, json, math, os, re, subprocess, sys, zipfile, colorsys
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
ART = os.path.join(ROOT, "art")
JS = os.path.join(ROOT, "web", "icons.js")
CELL, COLS, MAXF = 32, 16, 12
INK = (59, 42, 64, 255)
PAL = {  # bảng màu gốc – trùng tools/icons32/vector.py
    "Viền mận": "#3B2A40", "Trắng": "#FFFFFF", "Đỏ": "#F0545E", "Hồng đậm": "#E25F8E", "Hồng": "#FF9EBB",
    "Cam": "#FF9443", "Vàng": "#FFD34E", "Vàng đậm": "#F4AE2F", "Lá": "#5DC86B", "Lá đậm": "#3A9F5B",
    "Bạc hà": "#8FE3BE", "Ngọc": "#33BFAE", "Trời": "#6CC5F4", "Biển": "#4A83E6", "Tím": "#9466E0",
    "Oải hương": "#C6AEF5", "Bánh quy": "#EDBB80", "Gỗ": "#AF6E3F", "Gỗ đậm": "#6D4431", "Kem": "#FFF3DE",
    "Mây": "#F4F1F7", "Bạc": "#C7CCD9", "Thép": "#8B94AA", "Đêm": "#4C4762", "Da": "#FFCFAA", "Mực": "#2F3440",
}


# ───────────────────────── đọc / ghi manifest ─────────────────────────
def read_lines(path):
    if not os.path.exists(path):
        return []
    out = []
    for ln in open(path, encoding="utf8"):
        ln = ln.split("#", 1)[0].strip()
        if ln:
            out.append(ln.split())
    return out


def icon_rows():
    return [r[:COLS] for r in read_lines(os.path.join(ART, "icons.txt"))]


def anim_rows():
    """tên  fps  loop|once|pingpong  [giữ khung…]"""
    out = []
    for r in read_lines(os.path.join(ART, "anims.txt")):
        name, fps = r[0], float(r[1]) if len(r) > 1 else 12
        mode = r[2] if len(r) > 2 else "loop"
        hold = [max(1, int(x)) for x in r[3:]]
        out.append({"name": name, "fps": fps, "mode": mode, "hold": hold, "_row": len(out)})
    return out


# ───────────────────────── đọc ảnh Procreate ─────────────────────────
def load_sheet(path, cols, rows):
    """Mở ảnh, tự nhận hệ số phóng (1×, 2×, …), trả về ảnh 1× RGBA với alpha nhị phân."""
    im = Image.open(path).convert("RGBA")
    W, H = cols * CELL, rows * CELL
    k = im.width / W
    if k < 1 or abs(k - round(k)) > 1e-6:
        sys.exit("✗ %s rộng %dpx – cần đúng %dpx (hoặc bội số nguyên: %d, %d…)" % (os.path.relpath(path, ROOT), im.width, W, W * 2, W * 4))
    k = int(round(k))
    if im.height < H * k:  # thiếu hàng → nới thêm vùng trống
        pad = Image.new("RGBA", (im.width, H * k))
        pad.paste(im, (0, 0))
        im = pad
    if k > 1:  # lấy điểm giữa mỗi khối k×k (ảnh Procreate xuất kiểu nearest)
        im = im.resize((im.width // k, im.height // k), Image.NEAREST, box=(k // 2 - .5 * (k % 2 == 0), k // 2 - .5 * (k % 2 == 0), im.width, im.height))
    return binarize(im.crop((0, 0, W, H))), k


def binarize(im):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 255) if a >= 128 else (0, 0, 0, 0)
    return im


def cell(im, c, r):
    return im.crop((c * CELL, r * CELL, (c + 1) * CELL, (r + 1) * CELL))


def empty(im):
    return im.getbbox() is None


def frames_from_file(path):
    im = Image.open(path)
    out = []
    try:
        while True:
            f = im.convert("RGBA")
            if f.width != CELL:
                k = f.width // CELL
                f = f.resize((CELL, CELL), Image.NEAREST) if k >= 1 else f
            out.append((binarize(f.copy()), im.info.get("duration")))
            im.seek(im.tell() + 1)
    except EOFError:
        pass
    return out


def b64(im):
    buf = io.BytesIO()
    im.save(buf, "PNG", optimize=True)
    return base64.b64encode(buf.getvalue()).decode()


# ───────────────────────── kiểm tra ─────────────────────────
def lint_icon(name, im, warn):
    box = im.getbbox()
    if not box:
        return
    x0, y0, x1, y1 = box
    w, h = x1 - x0, y1 - y0
    if max(w, h) < 20:
        warn("%s: hình nhỏ %d×%d – icon khác thường cỡ 24–27px" % (name, w, h))
    cols = {p for p in (im.get_flattened_data() if hasattr(im, "get_flattened_data") else im.getdata()) if p[3]}
    if len(cols) > 48:
        warn("%s: %d màu – nhiều khả năng bị mờ viền (antialias); dùng cọ cứng 1px" % (name, len(cols)))


# ───────────────────────── build ─────────────────────────
def build(write=True):
    warns = []
    warn = warns.append
    rows = icon_rows()
    sheet, k = load_sheet(os.path.join(ART, "icons.png"), COLS, len(rows))
    icons, seen = {}, set()
    for r, names in enumerate(rows):
        for c, n in enumerate(names):
            if n == "-":
                continue
            if n in seen:
                warn("%s: tên bị trùng trong icons.txt" % n)
            seen.add(n)
            im = cell(sheet, c, r)
            if empty(im):
                warn("%s: ô trống (hàng %d, cột %d) → app dùng icon 16×16 dự phòng" % (n, r + 1, c + 1))
                continue
            lint_icon(n, im, warn)
            icons[n] = im
    # hoạt ảnh
    arows = anim_rows()
    anims = {}
    if arows:
        asheet, _ = load_sheet(os.path.join(ART, "anims.png"), MAXF, len(arows))
        for r, a in enumerate(arows):
            n = a["name"]
            ext = [p for p in (os.path.join(ART, "anims", n + e) for e in (".gif", ".png")) if os.path.exists(p)]
            if ext:
                fr = frames_from_file(ext[0])
                frames = [f for f, _ in fr]
                if not a["hold"] and fr and fr[0][1]:  # lấy nhịp từ file GIF/APNG
                    base = 1000 / a["fps"]
                    a["hold"] = [max(1, round((d or base) / base)) for _, d in fr]
            else:
                frames = [cell(asheet, c, r) for c in range(MAXF)]
                while frames and empty(frames[-1]):
                    frames.pop()
            if not frames:
                warn("%s: hoạt ảnh chưa có khung nào" % n)
                continue
            hold = a["hold"] or [1] * len(frames)
            if len(hold) != len(frames):
                warn("%s: %d khung nhưng có %d số giữ khung – phần thiếu tính là 1" % (n, len(frames), len(hold)))
                hold = (hold + [1] * len(frames))[:len(frames)]
            seq = [i for i, h in enumerate(hold) for _ in range(h)]
            if a["mode"] == "pingpong":
                back = list(range(len(frames) - 2, 0, -1))
                seq += [i for i in back for _ in range(hold[i])]
            strip = Image.new("RGBA", (CELL * len(seq), CELL))
            for j, i in enumerate(seq):
                strip.paste(frames[i], (j * CELL, 0))
            ms = round(len(seq) * 1000 / a["fps"])
            anims[n] = dict(strip=strip, n=len(seq), ms=ms, loop=a["mode"] != "once", frames=frames, seq=seq, fps=a["fps"])
            if n not in icons:
                warn("%s: hoạt ảnh không trùng tên icon nào – gọi bằng VinaIcons.anim(\"%s\")" % (n, n))
    for w in warns:
        print("  ⚠", w)
    print("✓ %d icon (ảnh %d×), %d hoạt ảnh, %d cảnh báo" % (len(icons), k, len(anims), len(warns)))
    if write:
        inject(icons, anims)
        preview(icons, rows, anims)
    return icons, anims


def inject(icons, anims):
    s = open(JS, encoding="utf8").read()
    body = "  var IMG32 = {\n" + ",\n".join('    "%s": "%s"' % (n, b64(im)) for n, im in sorted(icons.items())) + "\n  };\n"
    s = _between(s, "  /* ICON32-START */\n", "  /* ICON32-END */\n", body)
    body = "  var ANIM32 = {\n" + ",\n".join('    "%s": ["%s", %d, %d, %d]' % (n, b64(a["strip"]), a["n"], a["ms"], a["loop"]) for n, a in sorted(anims.items())) + "\n  };\n"
    s = _between(s, "  /* ANIM32-START */\n", "  /* ANIM32-END */\n", body)
    open(JS, "w", encoding="utf8").write(s)
    print("✓ đã nhúng vào web/icons.js (%.0f KB)" % (len(s) / 1024))


def _between(s, a, b, body):
    return s[:s.index(a) + len(a)] + body + s[s.index(b):]


# ───────────────────────── xem trước ─────────────────────────
def font(sz):
    for p in ("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "/usr/share/fonts/dejavu/DejaVuSans.ttf"):
        if os.path.exists(p):
            return ImageFont.truetype(p, sz)
    return ImageFont.load_default()


def labeled_grid(cells, cols, sc, path, bg=(255, 236, 242, 255), sub=None):
    """cells: list hàng, mỗi hàng list (ảnh|None, nhãn)"""
    f = font(max(10, sc * 3))
    cw, ch = CELL * sc, CELL * sc + sc * 5
    out = Image.new("RGBA", (cols * cw, len(cells) * ch), bg)
    d = ImageDraw.Draw(out)
    for r, row in enumerate(cells):
        for c, (im, label) in enumerate(row):
            x, y = c * cw, r * ch
            d.rectangle((x, y, x + cw - 1, y + ch - 1), outline=(240, 200, 214, 255))
            if im is not None:
                out.alpha_composite(im.resize((CELL * sc, CELL * sc), Image.NEAREST), (x, y))
            if label:
                d.text((x + sc, y + CELL * sc + sc), label, fill=(76, 51, 80, 255), font=f)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    out.save(path)


def preview(icons, rows, anims):
    out = os.path.join(ART, "out")
    labeled_grid([[(icons.get(n), n if n != "-" else "") for n in r] for r in rows], COLS, 4, os.path.join(out, "icons_preview.png"))
    if not anims:
        return
    # GIF: mọi hoạt ảnh chạy cùng lúc, đúng nhịp, phóng 4×
    names = sorted(anims)
    cols = min(4, len(names))
    sc, tick = 4, 40
    cw, ch = CELL * sc + 24, CELL * sc + 34
    rowsn = math.ceil(len(names) / cols)
    total = max(a["ms"] for a in anims.values())
    total = min(6000, max(2000, total * 2))
    f = font(13)
    frames = []
    for t in range(0, total, tick):
        im = Image.new("RGBA", (cols * cw, rowsn * ch), (255, 236, 242, 255))
        d = ImageDraw.Draw(im)
        for i, n in enumerate(names):
            a = anims[n]
            x, y = (i % cols) * cw + 12, (i // cols) * ch + 6
            j = int(t / 1000 * a["fps"])
            j = j % a["n"] if a["loop"] else min(j, a["n"] - 1)
            fr = a["frames"][a["seq"][j]]
            im.alpha_composite(fr.resize((CELL * sc, CELL * sc), Image.NEAREST), (x, y))
            d.text((x, y + CELL * sc + 4), "%s %gfps" % (n, a["fps"]), fill=(76, 51, 80, 255), font=f)
        frames.append(im.convert("RGB"))
    frames[0].save(os.path.join(out, "anims_preview.gif"), save_all=True, append_images=frames[1:], duration=tick, loop=0)
    print("✓ xem trước: art/out/icons_preview.png, art/out/anims_preview.gif")


# ───────────────────────── guide cho Procreate ─────────────────────────
def guides():
    g = os.path.join(ART, "guide")
    os.makedirs(g, exist_ok=True)
    rows, arows = icon_rows(), anim_rows()
    # 1) lớp lưới 1× – nhập vào Procreate làm layer trên cùng, chế độ Multiply ~30%
    for name, cols, nrows in (("icons_guide.png", COLS, len(rows) + 2), ("anims_guide.png", MAXF, len(arows) + 2)):
        im = Image.new("RGBA", (cols * CELL, nrows * CELL))
        px = im.load()
        for r in range(nrows):
            for c in range(cols):
                ox, oy = c * CELL, r * CELL
                for i in range(CELL):
                    for x, y in ((i, 0), (0, i)):
                        px[ox + x, oy + y] = (120, 90, 130, 150)  # khung ô
                    for x, y in ((i, 3), (i, 28), (3, i), (28, i)):  # vùng vẽ 3…28
                        if 3 <= i <= 28:
                            px[ox + x, oy + y] = (255, 120, 170, 110)
                for t in range(360):  # vòng tròn Ø25
                    x = round(ox + 15.5 + 12.5 * math.cos(math.radians(t)))
                    y = round(oy + 15.5 + 12.5 * math.sin(math.radians(t)))
                    px[x, y] = (90, 160, 240, 110)
                px[ox + 16, oy + 29] = (60, 180, 120, 200)  # điểm chân (đường đất)
        im.save(os.path.join(g, name))
    # 2) bản đồ tên ô (4×) – mở cạnh bên khi vẽ để biết ô nào là icon nào
    cur = {}
    try:
        cur = {n: Image.open(io.BytesIO(base64.b64decode(v))).convert("RGBA") for n, v in read_js_dict("IMG32").items()}
    except Exception:
        pass
    labeled_grid([[(cur.get(n), n if n != "-" else "") for n in r] for r in rows] + [[(None, "+ thêm")] * COLS], COLS, 4, os.path.join(g, "icons_map.png"))
    am = []
    for a in arows:
        h = a["hold"]
        am.append([(None, ("%s %gfps" % (a["name"], a["fps"]) if c == 0 else "#%d%s" % (c + 1, (" ×%d" % h[c]) if c < len(h) and h[c] > 1 else ""))) for c in range(MAXF)])
    if am:
        labeled_grid(am, MAXF, 4, os.path.join(g, "anims_map.png"))
    swatches(g)
    print("✓ art/guide/: icons_guide.png, anims_guide.png, icons_map.png, anims_map.png, Spendy-*.swatches")


def swatches(g):
    sys.path.insert(0, os.path.join(ROOT, "tools", "icons32"))
    import from16 as F
    keys = [k for k in PAL if k not in ("Viền mận", "Trắng")]
    groups = [("Spendy 1 – ấm", keys[0:10]), ("Spendy 2 – mát", keys[10:20]), ("Spendy 3 – trung tính", keys[20:] + ["Viền mận", "Trắng"])]
    for title, ks in groups:
        sw = []
        for tone in (2, 3, 4):  # hàng 1 bóng · hàng 2 gốc · hàng 3 sáng (Procreate hiển thị 10 ô / hàng)
            for k in ks:
                h = F.ramp(PAL[k])[tone] if k not in ("Viền mận", "Trắng") else PAL[k]
                r, gg, b = (int(h[i:i + 2], 16) / 255 for i in (1, 3, 5))
                hh, ss, vv = colorsys.rgb_to_hsv(r, gg, b)
                sw.append({"hue": hh, "saturation": ss, "brightness": vv, "alpha": 1, "colorSpace": 0})
            sw += [None] * (10 - len(ks))
        path = os.path.join(g, title.split(" –")[0].replace(" ", "-") + ".swatches")
        with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as z:
            z.writestr("Swatches.json", json.dumps([{"name": title, "swatches": sw}], ensure_ascii=False))


# ───────────────────────── export từ icons.js hiện tại ─────────────────────────
def read_js_dict(var):
    s = open(JS, encoding="utf8").read()
    m = re.search(r"var %s = \{([\s\S]*?)\n  \};" % var, s)
    return dict(re.findall(r'"([a-z0-9-]+)": "([A-Za-z0-9+/=]+)"', m.group(1))) if m else {}


def js_groups():
    code = "global.window={};require(%r);const V=window.VinaIcons;console.log(JSON.stringify(V.GROUPS.slice(1).map(g=>g.icons)))" % JS
    return json.loads(subprocess.check_output(["node", "-e", code]))


def export():
    imgs = {n: Image.open(io.BytesIO(base64.b64decode(v))).convert("RGBA") for n, v in read_js_dict("IMG32").items()}
    order = []
    for grp in js_groups():
        for n in grp:
            if n in imgs and n not in order:
                order.append(n)
    order += sorted(n for n in imgs if n not in order)
    rows = [order[i:i + COLS] for i in range(0, len(order), COLS)]
    sheet = Image.new("RGBA", (COLS * CELL, len(rows) * CELL))
    for r, row in enumerate(rows):
        for c, n in enumerate(row):
            sheet.paste(imgs[n], (c * CELL, r * CELL))
    os.makedirs(ART, exist_ok=True)
    sheet.save(os.path.join(ART, "icons.png"))
    with open(os.path.join(ART, "icons.txt"), "w", encoding="utf8") as f:
        f.write("# Mỗi dòng = 1 hàng ô 32×32 trên art/icons.png (16 ô/hàng), đọc trái → phải.\n"
                "# Tên phải trùng tên icon trong app. \"-\" = ô bỏ trống. Thêm icon: thêm tên vào ô trống/dòng mới.\n")
        for r in rows:
            f.write(" ".join(r) + "\n")
    print("✓ art/icons.png (%d icon) + art/icons.txt" % len(order))


# ───────────────────────── hoạt ảnh mẫu (tự sinh để có điểm bắt đầu) ─────────────────────────
def warp(im, sx=1, sy=1, ang=0, dx=0, dy=0, pivot=(16, 29)):
    """biến đổi nearest-neighbour quanh điểm neo – giữ điểm ảnh sắc nét"""
    src, out = im.load(), Image.new("RGBA", im.size)
    dst = out.load()
    px, py = pivot
    ca, sa = math.cos(math.radians(-ang)), math.sin(math.radians(-ang))
    for y in range(CELL):
        for x in range(CELL):
            u, v = x + .5 - px - dx, y + .5 - py - dy
            u, v = u * ca - v * sa, u * sa + v * ca
            u, v = u / sx + px, v / sy + py
            iu, iv = math.floor(u), math.floor(v)
            if 0 <= iu < CELL and 0 <= iv < CELL:
                dst[x, y] = src[iu, iv]
    return out


def sheen(im, pos, width=3, amt=.65):
    out = im.copy()
    px = out.load()
    for y in range(CELL):
        for x in range(CELL):
            r, g, b, a = px[x, y]
            if a and 0 <= (x + y) - pos < width and (r + g + b) > 200:
                px[x, y] = (round(r + (255 - r) * amt), round(g + (255 - g) * amt), round(b + (255 - b) * amt), 255)
    return out


def bright(im, amt):
    out = im.copy()
    px = out.load()
    for y in range(CELL):
        for x in range(CELL):
            r, g, b, a = px[x, y]
            if a and r + g + b > 200:
                px[x, y] = tuple(round(v + (255 - v) * amt) for v in (r, g, b)) + (255,)
    return out


def blink(im):
    """nhắm mắt: tô các điểm sáng trong 2 hốc mắt bằng màu màn hình, kẻ 1 vạch mi"""
    out = im.copy()
    px = out.load()
    scr = px[13, 13]
    for (x0, x1) in ((8, 11), (16, 19)):
        for y in range(14, 19):
            for x in range(x0, x1):
                r, g, b, a = px[x, y]
                if a and r + g + b > 600:
                    px[x, y] = scr
        for x in range(x0, x1):
            px[x, 17] = (59, 42, 64, 255)
    return out


STARTERS = {
    # tên: (icon gốc, fps, mode, [(hàm khung, giữ)…])
    "home": ("home", 12, "loop", [(dict(), 6), (dict(sx=1.08, sy=.9), 1), (dict(sx=.94, sy=1.08, dy=-2), 1), (dict(dy=-4), 2), (dict(dy=-2), 1), (dict(sx=1.08, sy=.92), 1), (dict(sx=.97, sy=1.03), 1), (dict(), 8)]),
    "wallet": ("wallet", 12, "loop", [(dict(), 6), (dict(sx=1.08, sy=.9), 1), (dict(sx=.94, sy=1.08, dy=-2), 1), (dict(dy=-4), 2), (dict(dy=-2), 1), (dict(sx=1.08, sy=.92), 1), (dict(sx=.97, sy=1.03), 1), (dict(), 8)]),
    "chart": ("chart", 12, "loop", [(dict(), 6), (dict(sx=1.06, sy=.88), 1), (dict(sx=.96, sy=1.06), 1), (dict(sx=1.06, sy=1.1), 1), (dict(sx=1, sy=.96), 1), (dict(), 10)]),
    "bell": ("bell", 14, "loop", [(dict(ang=0, pivot=(16, 3)), 6), (dict(ang=-10, pivot=(16, 3)), 1), (dict(ang=8, pivot=(16, 3)), 1), (dict(ang=-6, pivot=(16, 3)), 1), (dict(ang=4, pivot=(16, 3)), 1), (dict(ang=-4, pivot=(16, 3)), 1), (dict(ang=2, pivot=(16, 3)), 1), (dict(pivot=(16, 3)), 10)]),
    "heart": ("heart", 12, "loop", [(dict(pivot=(16, 16)), 3), (dict(sx=1.14, sy=1.14, pivot=(16, 16)), 1), (dict(sx=.96, sy=.96, pivot=(16, 16)), 1), (dict(sx=1.08, sy=1.08, pivot=(16, 16)), 1), (dict(pivot=(16, 16)), 6)]),
    "sparkles": ("sparkles", 10, "loop", [(dict(pivot=(16, 16)), 2), ("b.25", 1), ("b.5", 1), (dict(sx=1.1, sy=1.1, pivot=(16, 16)), 1), ("b.25", 1), (dict(sx=.92, sy=.92, pivot=(16, 16)), 1), (dict(pivot=(16, 16)), 4)]),
    "coins": ("coins", 12, "loop", [("s-6", 4), ("s2", 1), ("s7", 1), ("s12", 1), ("s17", 1), ("s22", 1), ("s27", 1), ("s32", 1), ("s37", 1), ("s-6", 10)]),
    "mai": ("mai", 8, "loop", [(dict(), 8), ("blink", 1), (dict(), 6), (dict(dy=-1), 2), (dict(), 2), (dict(dy=-1), 2), (dict(), 6)]),
}


def make_frame(base, spec):
    if isinstance(spec, dict):
        return warp(base, **spec) if spec else base.copy()
    if spec == "blink":
        return blink(base)
    if spec.startswith("s"):
        return sheen(base, int(spec[1:]))
    if spec.startswith("b"):
        return bright(base, float(spec[1:]))
    raise ValueError(spec)


def starter(only):
    imgs = {n: Image.open(io.BytesIO(base64.b64decode(v))).convert("RGBA") for n, v in read_js_dict("IMG32").items()}
    arows = anim_rows()
    names = [a["name"] for a in arows]
    for n in STARTERS:
        if n not in names:
            names.append(n)
    old_path = os.path.join(ART, "anims.png")
    old = load_sheet(old_path, MAXF, len(arows))[0] if arows and os.path.exists(old_path) else None
    sheet = Image.new("RGBA", (MAXF * CELL, len(names) * CELL))
    lines = []
    meta = {a["name"]: a for a in arows}
    for r, n in enumerate(names):
        if n in STARTERS and (not only or n in only):
            src, fps, mode, seq = STARTERS[n]
            for c, (spec, _) in enumerate(seq):
                sheet.paste(make_frame(imgs[src], spec), (c * CELL, r * CELL))
            hold = [h for _, h in seq]
        else:
            a = meta[n]
            if old is not None:
                sheet.paste(old.crop((0, a["_row"] * CELL, MAXF * CELL, (a["_row"] + 1) * CELL)), (0, r * CELL))
            fps, mode, hold = a["fps"], a["mode"], a["hold"]
        lines.append("%-10s %-4g %-8s %s" % (n, fps, mode, " ".join(map(str, hold))))
    sheet.save(old_path)
    with open(os.path.join(ART, "anims.txt"), "w", encoding="utf8") as f:
        f.write("# Mỗi dòng = 1 hàng trên art/anims.png (tối đa 12 khung 32×32, đọc trái → phải).\n"
                "# tên      fps  kiểu     giữ khung (khung 1, 2, 3… đứng bao nhiêu nhịp; bỏ trống = 1)\n"
                "# kiểu: loop (lặp) · once (chạy 1 lần) · pingpong (đi rồi về)\n"
                "# Tên trùng tên icon → icon đó tự chạy hoạt ảnh khi được kích hoạt trong app.\n")
        f.write("\n".join(lines) + "\n")
    print("✓ art/anims.png + art/anims.txt (%d hoạt ảnh)" % len(names))


def from_vector(names):
    sys.path.insert(0, os.path.join(ROOT, "tools", "icons32"))
    import vector as VEC
    rows = icon_rows()
    path = os.path.join(ART, "icons.png")
    sheet, _ = load_sheet(path, COLS, len(rows))
    imgs = VEC.build(names)
    for n, im in imgs.items():
        pos = [(c, r) for r, row in enumerate(rows) for c, x in enumerate(row) if x == n]
        if not pos:
            print("  ⚠ %s chưa có trong icons.txt – thêm tên vào một ô trống trước" % n)
            continue
        c, r = pos[0]
        sheet.paste(Image.new("RGBA", (CELL, CELL)), (c * CELL, r * CELL))
        sheet.paste(im.convert("RGBA"), (c * CELL, r * CELL))
    sheet.save(path)
    print("✓ đã vẽ %d icon từ vector vào art/icons.png" % len(imgs))


if __name__ == "__main__":
    a = sys.argv[1:]
    cmd = a[0] if a else "build"
    if cmd == "build":
        build()
    elif cmd == "check":
        build(write=False)
    elif cmd == "export":
        export()
        guides()
    elif cmd == "starter":
        starter(a[1:])
        guides()
    elif cmd == "vector":
        from_vector(a[1:])
    elif cmd == "guides":
        guides()
    else:
        print(__doc__)
