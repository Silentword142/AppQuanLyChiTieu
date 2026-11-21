#!/usr/bin/env python3
"""Spendy · đưa hình vẽ Procreate vào app (xem docs/ART-WORKFLOW.md)

Mỗi phần tử (icon / UI element) là 1 file PNG trong art/procreate/<tên>.png
  - 1 khung = 64×64 (icon) hoặc 2× cỡ gốc (UI element: xem art/elements.txt)
  - hoạt ảnh: các khung đặt CẠNH NHAU theo chiều ngang  → rộng = số khung × 64
  - xuất từ Procreate ở bất kỳ bội số nguyên nào (64, 128, 256, 512…) hoặc vẽ 32×32 đều được:
    ảnh luôn được đưa về đúng cỡ khung bằng nearest-neighbour → không vỡ nét pixel.

  python3 tools/art.py [file.zip | thư-mục | file.png …]   nhập (nếu có) rồi build vào web/icons.js
  python3 tools/art.py check                              chỉ kiểm tra, không ghi
  python3 tools/art.py template [--force]                 tạo art/procreate/*.png từ hình đang có trong app (phóng 2×)
  python3 tools/art.py starter [tên…]                     vẽ hoạt ảnh mẫu (nhảy, lắc, đập…) cho các tên có sẵn
  python3 tools/art.py pack                               đóng gói art/Spendy-Procreate-64.zip để chuyển sang iPad
"""
import base64, colorsys, io, json, math, os, re, shutil, sys, zipfile
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
ART = os.path.join(ROOT, "art")
SRC = os.path.join(ART, "procreate")
MANIFEST = os.path.join(ART, "elements.txt")
JS = os.path.join(ROOT, "web", "icons.js")
SCALE = 2                     # khung vẽ = 2× cỡ hiển thị gốc (icon 32 → 64)
DEF_FPS, DEF_MODE = 10, "loop"
PAL = {
    "Viền mận": "#3B2A40", "Trắng": "#FFFFFF", "Đỏ": "#F0545E", "Hồng đậm": "#E25F8E", "Hồng": "#FF9EBB",
    "Cam": "#FF9443", "Vàng": "#FFD34E", "Vàng đậm": "#F4AE2F", "Lá": "#5DC86B", "Lá đậm": "#3A9F5B",
    "Bạc hà": "#8FE3BE", "Ngọc": "#33BFAE", "Trời": "#6CC5F4", "Biển": "#4A83E6", "Tím": "#9466E0",
    "Oải hương": "#C6AEF5", "Bánh quy": "#EDBB80", "Gỗ": "#AF6E3F", "Gỗ đậm": "#6D4431", "Kem": "#FFF3DE",
    "Mây": "#F4F1F7", "Bạc": "#C7CCD9", "Thép": "#8B94AA", "Đêm": "#4C4762", "Da": "#FFCFAA", "Mực": "#2F3440",
}


# ───────────────────────── manifest art/elements.txt ─────────────────────────
HEADER = """# Spendy · danh sách phần tử vẽ trên Procreate – mỗi dòng 1 file art/procreate/<tên>.png
# tên                     rộng  cao   [fps  kiểu  giữ-khung…]
#   rộng × cao = cỡ 1 KHUNG (icon 64×64). Các khung hoạt ảnh đặt cạnh nhau theo chiều ngang.
#   fps   : nhịp mỗi giây (mặc định 10) · kiểu: loop | once | pingpong
#   giữ-khung: khung 1, 2, 3… đứng bao nhiêu nhịp (bỏ trống = 1) → tạo easing mà không phải vẽ khung trùng
# Thêm icon mới: thêm 1 dòng "tên 64 64" rồi vẽ file art/procreate/tên.png
"""


def read_manifest():
    out = {}
    if os.path.exists(MANIFEST):
        for ln in open(MANIFEST, encoding="utf8"):
            p = ln.split("#", 1)[0].split()
            if len(p) < 3:
                continue
            out[p[0]] = dict(w=int(p[1]), h=int(p[2]), fps=float(p[3]) if len(p) > 3 else DEF_FPS,
                             mode=p[4] if len(p) > 4 else DEF_MODE, hold=[max(1, int(x)) for x in p[5:]], timed=len(p) > 3)
    return out


def write_manifest(m):
    with open(MANIFEST, "w", encoding="utf8") as f:
        f.write(HEADER)
        for n in sorted(m, key=lambda k: (k.startswith("ui-"), k)):
            e = m[n]
            line = "%-26s %-5d %-5d" % (n, e["w"], e["h"])
            if e.get("timed"):
                line += " %-4g %-8s %s" % (e["fps"], e["mode"], " ".join(map(str, e["hold"])))
            f.write(line.rstrip() + "\n")


# ───────────────────────── icons.js ─────────────────────────
def js_dict(var):
    s = open(JS, encoding="utf8").read()
    m = re.search(r"var %s\s*=\s*\{([\s\S]*?)\n  \};" % var, s)
    return s, m


def img32():
    _, m = js_dict("IMG32")
    return {k: Image.open(io.BytesIO(base64.b64decode(v))).convert("RGBA") for k, v in re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(1))}


def b64(im):
    buf = io.BytesIO()
    im.save(buf, "PNG", optimize=True)
    return base64.b64encode(buf.getvalue()).decode()


def write_js(var, entries):
    s, m = js_dict(var)
    body = "\n" + ",\n".join("    %s" % e for e in entries)
    s = s[:m.start(1)] + body + s[m.end(1):]
    open(JS, "w", encoding="utf8").write(s)


# ───────────────────────── chuẩn hoá ảnh: không bao giờ nội suy mượt ─────────────────────────
def binarize(im):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 255) if a >= 128 else (0, 0, 0, 0)
    return im


def to_cell_grid(im, cw, ch, name, warn):
    """Đưa ảnh về đúng (n·cw) × ch. Cao gấp k lần → lấy mẫu điểm giữa khối k×k; cao bằng 1/k → phóng nearest ×k."""
    im = im.convert("RGBA")
    if im.height % ch == 0:
        k = im.height // ch
        if k > 1:
            # NEAREST lấy đúng điểm giữa mỗi khối k×k (tránh viền nhoè của cọ Procreate)
            im = im.resize((im.width // k, ch), Image.NEAREST, box=(0, 0, (im.width // k) * k, ch * k))
    elif ch % im.height == 0:
        k = ch // im.height
        im = im.resize((im.width * k, ch), Image.NEAREST)
    else:
        warn("%s: cao %dpx không phải bội số của %d → co giãn nearest (có thể lệch pixel)" % (name, im.height, ch))
        im = im.resize((max(1, round(im.width * ch / im.height)), ch), Image.NEAREST)
    n = max(1, round(im.width / cw))
    if im.width != n * cw:
        warn("%s: rộng %dpx không chia hết cho khung %dpx → cắt/bù về %d khung" % (name, im.width, cw, n))
        pad = Image.new("RGBA", (n * cw, ch))
        pad.paste(im, (0, 0))
        im = pad
    return binarize(im), n


def is_pure_up(im, s=SCALE):
    """ảnh chỉ là bản phóng ×s của ảnh nhỏ? → lưu bản nhỏ cho nhẹ (hiển thị y hệt)."""
    small = im.resize((im.width // s, im.height // s), Image.NEAREST, box=(0, 0, im.width // s * s, im.height // s * s))
    return small if small.resize((small.width * s, small.height * s), Image.NEAREST).tobytes() == im.tobytes() else None


# ───────────────────────── nhập file từ Procreate ─────────────────────────
def import_sources(paths):
    os.makedirs(SRC, exist_ok=True)
    got = []
    for p in paths:
        if os.path.isdir(p):
            files = [os.path.join(d, f) for d, _, fs in os.walk(p) for f in fs]
        elif p.lower().endswith(".zip"):
            tmp = os.path.join(ART, ".import")
            shutil.rmtree(tmp, ignore_errors=True)
            with zipfile.ZipFile(p) as z:
                for info in z.infolist():
                    base = os.path.basename(info.filename)
                    if info.is_dir() or "__MACOSX" in info.filename or base.startswith("."):
                        continue
                    os.makedirs(tmp, exist_ok=True)
                    with open(os.path.join(tmp, base), "wb") as f:
                        f.write(z.read(info))
            files = [os.path.join(tmp, f) for f in os.listdir(tmp)] if os.path.isdir(tmp) else []
        else:
            files = [p]
        for f in files:
            base = os.path.basename(f)
            name, ext = os.path.splitext(base)
            if ext.lower() not in (".png", ".gif", ".webp") or name.startswith(("00_", ".")):
                continue
            name = re.sub(r"\s+", "-", name.strip().lower())
            dst = os.path.join(SRC, name + ".png")
            im = Image.open(f)
            if getattr(im, "n_frames", 1) > 1:  # GIF / PNG động từ Animation Assist → ghép khung cạnh nhau
                frames = []
                for i in range(im.n_frames):
                    im.seek(i)
                    frames.append(im.convert("RGBA"))
                strip = Image.new("RGBA", (frames[0].width * len(frames), frames[0].height))
                for i, fr in enumerate(frames):
                    strip.paste(fr, (i * fr.width, 0))
                strip.save(dst)
            else:
                im.convert("RGBA").save(dst)
            got.append(name)
    shutil.rmtree(os.path.join(ART, ".import"), ignore_errors=True)
    if got:
        print("✓ nhập %d file: %s%s" % (len(got), ", ".join(got[:10]), "…" if len(got) > 10 else ""))
    return got


# ───────────────────────── build ─────────────────────────
def build(write=True):
    warns = []
    warn = warns.append
    man = read_manifest()
    current = img32()
    files = sorted(f[:-4] for f in os.listdir(SRC) if f.endswith(".png")) if os.path.isdir(SRC) else []
    statics, anims, changed = {}, {}, 0
    for n in files:
        if n not in man:
            man[n] = dict(w=64, h=64, fps=DEF_FPS, mode=DEF_MODE, hold=[], timed=False)
            warn("%s: tên mới → thêm vào elements.txt với khung 64×64" % n)
        e = man[n]
        strip, nf = to_cell_grid(Image.open(os.path.join(SRC, n + ".png")), e["w"], e["h"], n, warn)
        frames = [strip.crop((i * e["w"], 0, (i + 1) * e["w"], e["h"])) for i in range(nf)]
        while len(frames) > 1 and frames[-1].getbbox() is None:
            frames.pop()
        if frames[0].getbbox() is None:
            warn("%s: khung 1 trống → bỏ qua (app giữ hình cũ)" % n)
            continue
        first = frames[0]
        store = is_pure_up(first) or first
        statics[n] = store
        old = current.get(n)
        if old is None or old.tobytes() != store.tobytes() or old.size != store.size:
            changed += 1
        if len(frames) > 1:
            hold = e["hold"] or [1] * len(frames)
            if len(hold) != len(frames):
                warn("%s: %d khung nhưng ghi %d số giữ khung → phần thiếu tính 1" % (n, len(frames), len(hold)))
                hold = (hold + [1] * len(frames))[:len(frames)]
            seq = [i for i, h in enumerate(hold) for _ in range(h)]
            if e["mode"] == "pingpong":
                seq += [i for i in range(len(frames) - 2, 0, -1) for _ in range(hold[i])]
            st = Image.new("RGBA", (e["w"] * len(seq), e["h"]))
            for j, i in enumerate(seq):
                st.paste(frames[i], (j * e["w"], 0))
            anims[n] = dict(strip=is_pure_up(st) or st, n=len(seq), ms=round(len(seq) * 1000 / e["fps"]),
                            loop=e["mode"] != "once", frames=frames, seq=seq, fps=e["fps"], size=(e["w"], e["h"]))
    for w in warns:
        print("  ⚠", w)
    print("✓ %d file · %d hình đổi · %d hoạt ảnh · %d cảnh báo" % (len(files), changed, len(anims), len(warns)))
    if write:
        merged = {**{k: v for k, v in current.items()}, **statics}
        write_js("IMG32", ['"%s": "%s"' % (k, b64(v)) for k, v in sorted(merged.items())])
        write_js("ANIM32", ['"%s": ["%s", %d, %d, %d]' % (k, b64(a["strip"]), a["n"], a["ms"], a["loop"]) for k, a in sorted(anims.items())])
        write_manifest(man)
        print("✓ đã ghi web/icons.js (%.0f KB)" % (os.path.getsize(JS) / 1024))
        preview(merged, anims)
    return statics, anims


# ───────────────────────── xem trước ─────────────────────────
def font(sz):
    for p in ("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "/usr/share/fonts/dejavu/DejaVuSans.ttf"):
        if os.path.exists(p):
            return ImageFont.truetype(p, sz)
    return ImageFont.load_default()


def preview(imgs, anims):
    out = os.path.join(ART, "out")
    os.makedirs(out, exist_ok=True)
    names = sorted(imgs, key=lambda k: (k.startswith("ui-"), k))
    cols, cw, ch = 12, 96, 112
    sheet = Image.new("RGBA", (cols * cw, math.ceil(len(names) / cols) * ch), (255, 236, 242, 255))
    d, f = ImageDraw.Draw(sheet), font(10)
    for i, n in enumerate(names):
        im = imgs[n]
        k = max(1, 64 // max(im.size))
        big = im.resize((im.width * k, im.height * k), Image.NEAREST)
        x, y = (i % cols) * cw, (i // cols) * ch
        sheet.alpha_composite(big, (x + (cw - big.width) // 2, y + 8 + (64 - big.height) // 2 * (big.height < 64)))
        d.text((x + 4, y + 92), n[:16], fill=(76, 51, 80, 255), font=f)
    sheet.save(os.path.join(out, "preview.png"))
    if anims:
        names = sorted(anims)
        cols = min(4, len(names))
        sc, tick = 2, 40
        cw, ch = 64 * sc + 24, 64 * sc + 34
        total = min(6000, max(2000, 2 * max(a["ms"] for a in anims.values())))
        frames, f = [], font(13)
        for t in range(0, total, tick):
            im = Image.new("RGBA", (cols * cw, math.ceil(len(names) / cols) * ch), (255, 236, 242, 255))
            d = ImageDraw.Draw(im)
            for i, n in enumerate(names):
                a = anims[n]
                j = int(t / 1000 * a["fps"])
                j = j % a["n"] if a["loop"] else min(j, a["n"] - 1)
                fr = a["frames"][a["seq"][j]]
                k = max(1, 128 // max(fr.size))
                x, y = (i % cols) * cw + 12, (i // cols) * ch + 6
                im.alpha_composite(fr.resize((fr.width * k, fr.height * k), Image.NEAREST), (x, y))
                d.text((x, y + 64 * sc + 4), "%s %gfps" % (n, a["fps"]), fill=(76, 51, 80, 255), font=f)
            frames.append(im.convert("RGB"))
        frames[0].save(os.path.join(out, "anims.gif"), save_all=True, append_images=frames[1:], duration=tick, loop=0)
    print("✓ xem trước: art/out/preview.png%s" % (", art/out/anims.gif" if anims else ""))


# ───────────────────────── template + guide cho Procreate ─────────────────────────
def template(force=False):
    os.makedirs(SRC, exist_ok=True)
    man = read_manifest()
    made = 0
    for n, im in img32().items():
        man.setdefault(n, dict(w=im.width * SCALE, h=im.height * SCALE, fps=DEF_FPS, mode=DEF_MODE, hold=[], timed=False))
        p = os.path.join(SRC, n + ".png")
        if force or not os.path.exists(p):
            im.resize((im.width * SCALE, im.height * SCALE), Image.NEAREST).save(p)
            made += 1
    write_manifest(man)
    guides()
    print("✓ art/procreate: tạo %d file (2×, nearest) · art/elements.txt: %d phần tử" % (made, len(man)))


def guides():
    g = os.path.join(ART, "guide")
    os.makedirs(g, exist_ok=True)
    for nf, name in ((1, "grid-64.png"), (8, "grid-64x8-anim.png")):
        im = Image.new("RGBA", (64 * nf, 64))
        px = im.load()
        for c in range(nf):
            ox = c * 64
            for i in range(64):
                for x, y in ((i, 0), (0, i), (i, 63), (63, i)):
                    px[ox + x, y] = (120, 90, 130, 170)          # khung 64
                if i % 2 == 0:                                   # lưới 2px = 1 pixel gốc (32)
                    for j in range(0, 64, 8):
                        px[ox + j, i] = (120, 90, 130, 60)
                        px[ox + i, j] = (120, 90, 130, 60)
                if 6 <= i <= 57:
                    for x, y in ((i, 6), (i, 57), (6, i), (57, i)):
                        px[ox + x, y] = (255, 120, 170, 140)     # vùng vẽ 6…57
            for t in range(720):
                x = round(ox + 31.5 + 25 * math.cos(math.radians(t / 2)))
                y = round(31.5 + 25 * math.sin(math.radians(t / 2)))
                px[x, y] = (90, 160, 240, 140)                   # vòng tròn Ø50
            for x in range(26, 38):
                px[ox + x, 58] = (60, 180, 120, 220)             # đường chân đế y = 58
        im.save(os.path.join(g, name))
    swatches(g)


def swatches(g):
    sys.path.insert(0, os.path.join(ROOT, "tools", "icons32"))
    import from16 as F
    keys = [k for k in PAL if k not in ("Viền mận", "Trắng")]
    groups = [("Spendy 1 – ấm", keys[0:10]), ("Spendy 2 – mát", keys[10:20]), ("Spendy 3 – trung tính", keys[20:] + ["Viền mận", "Trắng"])]
    for title, ks in groups:
        sw = []
        for tone in (2, 3, 4):  # hàng 1 bóng · hàng 2 gốc · hàng 3 sáng
            for k in ks:
                h = F.ramp(PAL[k])[tone] if k not in ("Viền mận", "Trắng") else PAL[k]
                r, gg, b = (int(h[i:i + 2], 16) / 255 for i in (1, 3, 5))
                hh, ss, vv = colorsys.rgb_to_hsv(r, gg, b)
                sw.append({"hue": hh, "saturation": ss, "brightness": vv, "alpha": 1, "colorSpace": 0})
            sw += [None] * (10 - len(ks))
        with zipfile.ZipFile(os.path.join(g, title.split(" –")[0].replace(" ", "-") + ".swatches"), "w", zipfile.ZIP_DEFLATED) as z:
            z.writestr("Swatches.json", json.dumps([{"name": title, "swatches": sw}], ensure_ascii=False))


def pack():
    out = os.path.join(ART, "Spendy-Procreate-64.zip")
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        for d in ("procreate", "guide"):
            for f in sorted(os.listdir(os.path.join(ART, d))):
                z.write(os.path.join(ART, d, f), "%s/%s" % (d, f))
        z.write(MANIFEST, "elements.txt")
        z.write(os.path.join(ROOT, "docs", "ART-WORKFLOW.md"), "HUONG-DAN.md")
    print("✓ %s (%.0f KB)" % (os.path.relpath(out, ROOT), os.path.getsize(out) / 1024))


# ───────────────────────── hoạt ảnh mẫu (vẽ ở 32 rồi phóng 2× → nét pixel y hệt icon) ─────────────────────────
C = 32


def warp(im, sx=1, sy=1, ang=0, dx=0, dy=0, pivot=(16, 29)):
    src, out = im.load(), Image.new("RGBA", im.size)
    dst = out.load()
    px, py = pivot
    ca, sa = math.cos(math.radians(-ang)), math.sin(math.radians(-ang))
    for y in range(C):
        for x in range(C):
            u, v = x + .5 - px - dx, y + .5 - py - dy
            u, v = u * ca - v * sa, u * sa + v * ca
            iu, iv = math.floor(u / sx + px), math.floor(v / sy + py)
            if 0 <= iu < C and 0 <= iv < C:
                dst[x, y] = src[iu, iv]
    return out


def tint(im, test, amt):
    out = im.copy()
    px = out.load()
    for y in range(C):
        for x in range(C):
            r, g, b, a = px[x, y]
            if a and r + g + b > 200 and test(x, y):
                px[x, y] = tuple(round(v + (255 - v) * amt) for v in (r, g, b)) + (255,)
    return out


def blink(im):
    out = im.copy()
    px = out.load()
    scr = px[13, 13]
    for x0, x1 in ((8, 11), (16, 19)):
        for y in range(14, 19):
            for x in range(x0, x1):
                r, g, b, a = px[x, y]
                if a and r + g + b > 600:
                    px[x, y] = scr
        for x in range(x0, x1):
            px[x, 17] = (59, 42, 64, 255)
    return out


HOP = [(dict(), 6), (dict(sx=1.08, sy=.9), 1), (dict(sx=.94, sy=1.08, dy=-2), 1), (dict(dy=-4), 2), (dict(dy=-2), 1), (dict(sx=1.08, sy=.92), 1), (dict(sx=.97, sy=1.03), 1), (dict(), 8)]
TOP, MID = (16, 3), (16, 16)
STARTERS = {  # tên: (fps, kiểu, [(khung, giữ)…])
    "home": (12, "loop", HOP), "wallet": (12, "loop", HOP),
    "chart": (12, "loop", [(dict(), 6), (dict(sx=1.06, sy=.88), 1), (dict(sx=.96, sy=1.06), 1), (dict(sx=1.04, sy=1.06), 1), (dict(sy=.97), 1), (dict(), 10)]),
    "bell": (14, "loop", [(dict(pivot=TOP), 6)] + [(dict(ang=a, pivot=TOP), 1) for a in (-10, 8, -6, 4, -2)] + [(dict(pivot=TOP), 10)]),
    "heart": (12, "loop", [(dict(pivot=MID), 3), (dict(sx=1.14, sy=1.14, pivot=MID), 1), (dict(sx=.96, sy=.96, pivot=MID), 1), (dict(sx=1.08, sy=1.08, pivot=MID), 1), (dict(pivot=MID), 6)]),
    "sparkles": (10, "loop", [(dict(pivot=MID), 2), ("b.25", 1), ("b.5", 1), (dict(sx=1.1, sy=1.1, pivot=MID), 1), ("b.25", 1), (dict(sx=.92, sy=.92, pivot=MID), 1), (dict(pivot=MID), 4)]),
    "coins": (12, "loop", [("s-6", 4)] + [("s%d" % p, 1) for p in range(2, 38, 5)] + [("s-6", 10)]),
    "mai": (8, "loop", [(dict(), 8), ("blink", 1), (dict(), 6), (dict(dy=-1), 2), (dict(), 2), (dict(dy=-1), 2), (dict(), 6)]),
}


def frame(base, spec):
    if isinstance(spec, dict):
        return warp(base, **spec) if spec else base.copy()
    if spec == "blink":
        return blink(base)
    if spec[0] == "s":
        pos = int(spec[1:])
        return tint(base, lambda x, y: 0 <= x + y - pos < 3, .65)
    return tint(base, lambda x, y: True, float(spec[1:]))


def starter(only):
    cur, man = img32(), read_manifest()
    os.makedirs(SRC, exist_ok=True)
    for n, (fps, mode, seq) in STARTERS.items():
        if only and n not in only or n not in cur or cur[n].size != (C, C):
            continue
        strip = Image.new("RGBA", (C * len(seq), C))
        for i, (spec, _) in enumerate(seq):
            strip.paste(frame(cur[n], spec), (i * C, 0))
        strip.resize((strip.width * SCALE, C * SCALE), Image.NEAREST).save(os.path.join(SRC, n + ".png"))
        man[n] = dict(w=64, h=64, fps=fps, mode=mode, hold=[h for _, h in seq], timed=True)
        print("✓ %s: %d khung" % (n, len(seq)))
    write_manifest(man)


if __name__ == "__main__":
    a = sys.argv[1:]
    cmd = a[0] if a else "build"
    if cmd == "check":
        build(write=False)
    elif cmd == "template":
        template("--force" in a)
    elif cmd == "starter":
        starter(a[1:])
    elif cmd == "pack":
        pack()
    elif cmd in ("-h", "--help", "help"):
        print(__doc__)
    else:
        if cmd != "build":
            import_sources(a)
        build()
