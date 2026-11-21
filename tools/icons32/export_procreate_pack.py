#!/usr/bin/env python3
"""
Xuất toàn bộ 188 Elements của Spendy (173 Icons + 15 UI Elements) ĐÚNG KÍCH CỠ PIXEL GỐC (1:1)
để mở trực tiếp trong Procreate trên iPad vẽ chuẩn từng pixel:
  - 01_Icons/*.png       : 32x32 px (173 icons gốc trong web/icons.js)
  - 02_UI_Elements/*.png : Đúng kích cỡ native trong app:
      * ui-dock-{triangle,circle,square,star}-{cap,well}.png : 29x27 px
      * ui-dock-ico-{home,chart,mai,wallet}.png              : 15x15 px
      * ui-arcade-ring.png                                   : 17x17 px
      * ui-arcade-dome.png                                   : 13x13 px
      * ui-sky-cloud.png                                     : 28x10 px
  - Spendy_Pastel_Dusk.swatches : Bảng màu chuẩn Procreate
  - 00_UI_Elements_Map.png      : Bảng tra cứu tên & kích cỡ gốc 15 UI Elements
  - HUONG_DAN_PROCREATE.txt     : Hướng dẫn chi tiết
"""
import base64
import io
import json
import math
import re
import sys
import zipfile
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ICONS_JS = ROOT / "web" / "icons.js"

SHAPES = {
    "triangle": ([
        "...........#####...........",
        "..........#######..........",
        ".........#########.........",
        ".........#########.........",
        "........###########........",
        ".......#############.......",
        ".......#############.......",
        "......###############......",
        ".....#################.....",
        ".....#################.....",
        "....###################....",
        "...#####################...",
        "...#####################...",
        "..#######################..",
        "..#######################..",
        ".#########################.",
        ".#########################.",
        ".#########################.",
        ".#########################.",
        "..#######################..",
        "...#####################..."
    ], {"hi": "#FFC8D0", "base": "#F48E9E", "side": "#A83A52"}),
    "circle": ([
        ".........#########.........",
        ".......#############.......",
        ".....#################.....",
        "....###################....",
        "...#####################...",
        "...#####################...",
        "..#######################..",
        "..#######################..",
        "..#######################..",
        "..#######################..",
        "..#######################..",
        "..#######################..",
        "..#######################..",
        "..#######################..",
        "...#####################...",
        "...#####################...",
        "....###################....",
        ".....#################.....",
        ".......#############.......",
        ".........#########.........",
        "..........................."
    ], {"hi": "#E2F2C2", "base": "#ACD078", "side": "#5B802B"}),
    "square": ([
        ".....#################.....",
        "....###################....",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "...#####################...",
        "....###################....",
        ".....#################.....",
        "..........................."
    ], {"hi": "#E7D5FC", "base": "#BE9CE8", "side": "#684498"}),
    "star": ([
        "............###............",
        "...........#####...........",
        "..........#######..........",
        "..........#######..........",
        ".........#########.........",
        "..#######################..",
        ".#########################.",
        ".#########################.",
        "..#######################..",
        "...#####################...",
        "....###################....",
        ".....#################.....",
        ".....#################.....",
        "....###################....",
        "....###################....",
        "...#####################...",
        "...#########...#########...",
        "..#########.....#########..",
        "..########.......########..",
        "...######.........######...",
        "....####...........####...."
    ], {"hi": "#CAE5FF", "base": "#86B9EC", "side": "#346296"})
}

MONO_ICONS = {
    "home": [
        ".......#.......",
        "......###......",
        ".....#####.##..",
        "....#########..",
        "...##########..",
        "..###########..",
        ".#############.",
        "...............",
        "...#########...",
        "...#########...",
        "...###...###...",
        "...###...###...",
        "...###...###...",
        "...###...###...",
        "..............."
    ],
    "chart": [
        "...............",
        "...............",
        "..........###..",
        "..........###..",
        "..........###..",
        "......###.###..",
        "......###.###..",
        "......###.###..",
        "..###.###.###..",
        "..###.###.###..",
        "..###.###.###..",
        "..###.###.###..",
        "...............",
        ".#############.",
        "..............."
    ],
    "mai": [
        "....#.....#....",
        ".....#...#.....",
        "......###......",
        "..###########..",
        ".#############.",
        ".##.........##.",
        ".##..#...#..##.",
        ".##..#...#..##.",
        ".##.........##.",
        ".#############.",
        "..###########..",
        "....##...##....",
        "...............",
        "...............",
        "..............."
    ],
    "wallet": [
        "...............",
        "...#########...",
        "...##.....##...",
        "...............",
        ".#############.",
        ".#############.",
        ".#######.......",
        ".#######..####.",
        ".#######..####.",
        ".#######.......",
        ".#############.",
        ".#############.",
        "..###########..",
        "...............",
        "..............."
    ]
}

CLOUD_ROWS = [
    "..........wwwwww............",
    "........wwhhhhhhww..........",
    ".......whhhhhhhhhhw...www...",
    "....wwwhhhhhhhhhhhhwwhhhww..",
    "..wwhhhhhhhhhhhhhhhhhhhhhhw.",
    ".whhhhhhhhhhhhhhhhhhhhhhhhhw",
    "whhhhhhhhhhhhhhhhhhhhhhhhhhw",
    "wwwwwwwwwwwwwwwwwwwwwwwwwwww",
    ".sssswwwwwwwwwwwwwwwwwsssss.",
    "...ssssssssssssssssssss....."
]

SWATCH_HEXES = [
    "#22172B", "#4A3350", "#2A2140", "#41325C", "#FCEEDD", "#FFF2E2", "#F5C4BC", "#EE9AA3", "#FFD87E", "#FFFFFF",
    "#FFC8D0", "#F48E9E", "#A83A52", "#E2F2C2", "#ACD078", "#5B802B", "#E7D5FC", "#BE9CE8", "#684498", "#86B9EC",
    "#346296", "#9DF5C2", "#FFA5BC", "#FFE08A", "#FF8674", "#52CF68", "#63C7FF", "#FF9638", "#EC5F93", "#B5703D"
]


def hex_to_rgba(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def hex_to_hsb(h):
    r, g, b, _ = hex_to_rgba(h)
    r, g, b = r / 255.0, g / 255.0, b / 255.0
    mx, mn = max(r, g, b), min(r, g, b)
    df = mx - mn
    if df == 0:
        hue = 0.0
    elif mx == r:
        hue = ((g - b) / df) % 6.0 / 6.0
    elif mx == g:
        hue = (((b - r) / df) + 2.0) / 6.0
    else:
        hue = (((r - g) / df) + 4.0) / 6.0
    sat = 0.0 if mx == 0 else df / mx
    return hue, sat, mx


def make_procreate_swatches_bytes(name="Spendy Pastel Dusk"):
    swatches = []
    for hx in SWATCH_HEXES:
        h, s, b = hex_to_hsb(hx)
        swatches.append({
            "hue": round(h, 6),
            "saturation": round(s, 6),
            "brightness": round(b, 6),
            "alpha": 1,
            "colorSpace": 0
        })
    payload = [{"name": name, "swatches": swatches}]
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("Swatches.json", json.dumps(payload, ensure_ascii=False))
    return buf.getvalue()


def add_aseprite_outline(grid, mask, default_fill, ink="#22172B"):
    H, W = len(grid), len(grid[0])
    is_out = [[False] * W for _ in range(H)]

    def hasF(x, y):
        return 0 <= x < W and 0 <= y < H and mask[y][x]

    for y in range(H):
        for x in range(W):
            if hasF(x, y):
                continue
            if hasF(x + 1, y) or hasF(x - 1, y) or hasF(x, y + 1) or hasF(x, y - 1):
                grid[y][x] = ink
                is_out[y][x] = True

    def hasO(x, y):
        return 0 <= x < W and 0 <= y < H and is_out[y][x]

    for y in range(1, H - 1):
        for x in range(1, W - 1):
            if not is_out[y][x]:
                continue
            for dx, dy in ((1, 1), (1, -1), (-1, 1), (-1, -1)):
                if hasO(x - dx, y) and hasO(x, y - dy) and hasF(x + dx, y) and hasF(x, y + dy) and not hasF(x - dx, y - dy):
                    grid[y][x] = grid[y][x + dx] or default_fill
                    is_out[y][x] = False
                    mask[y][x] = True
                    break


def grid_to_img(grid):
    h, w = len(grid), len(grid[0])
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    px = img.load()
    for y in range(h):
        for x in range(w):
            if grid[y][x]:
                px[x, y] = hex_to_rgba(grid[y][x])
    return img


def generate_ui_elements():
    out = {}
    # 1) 4 nút 2.5D: đúng kích cỡ native 29x27 px như pixel25dBtnSvg trong web/index.html
    for sname, (rows, pal) in SHAPES.items():
        H0, W0, DEPTH = len(rows), len(rows[0]), 3
        P = ["." * (W0 + 2)] + ["." + r + "." for r in rows] + ["." * (W0 + 2)]
        H, W = len(P), W0 + 2
        VH = H + DEPTH + 1  # 23 + 3 + 1 = 27

        def at(x, y):
            return 0 <= x < W and 0 <= y < H and P[y][x] == "#"

        base_g = [[None] * W for _ in range(VH)]
        base_m = [[False] * W for _ in range(VH)]
        for d in range(DEPTH, 0, -1):
            for y in range(H):
                for x in range(W):
                    if at(x, y):
                        base_g[y + d][x] = pal["side"]
                        base_m[y + d][x] = True
        add_aseprite_outline(base_g, base_m, pal["side"])
        out[f"ui-dock-{sname}-well"] = grid_to_img(base_g)

        cap_g = [[None] * W for _ in range(VH)]
        cap_m = [[False] * W for _ in range(VH)]
        for y in range(H):
            for x in range(W):
                if at(x, y):
                    cap_g[y][x] = pal["hi"] if not at(x, y - 1) else pal["base"]
                    cap_m[y][x] = True
        add_aseprite_outline(cap_g, cap_m, pal["base"])
        out[f"ui-dock-{sname}-cap"] = grid_to_img(cap_g)

    # 2) 4 icon đơn màu giữa nút: đúng kích cỡ native 15x15 px
    for mname, rows in MONO_ICONS.items():
        H, W = len(rows), len(rows[0])
        g = [[("#FFF2E2" if rows[y][x] == "#" else None) for x in range(W)] for y in range(H)]
        out[f"ui-dock-ico-{mname}"] = grid_to_img(g)

    # 3) Nút + Ghi: đúng kích cỡ native 17x17 px (vòng ngoài) & 13x13 px (nút vòm)
    def make_disc(d, col_body, col_hl, col_sh, col_ink, extra_pts=()):
        g = [[None] * d for _ in range(d)]
        r = d / 2.0

        def inside(x, y):
            return math.hypot(x + 0.5 - r, y + 0.5 - r) <= r - 0.15

        for y in range(d):
            for x in range(d):
                if not inside(x, y):
                    continue
                if not (inside(x + 1, y) and inside(x - 1, y) and inside(x, y + 1) and inside(x, y - 1)):
                    g[y][x] = col_ink
                    continue
                c = col_body
                dx, dy = x + 0.5 - r, y + 0.5 - r
                if math.hypot(dx, dy) > r - 2.6:
                    if dx + dy < -r * 0.45:
                        c = col_hl
                    elif dx + dy > r * 0.45:
                        c = col_sh
                g[y][x] = c
        for ex, ey, ec in extra_pts:
            g[ey][ex] = ec
        return grid_to_img(g)

    out["ui-arcade-ring"] = make_disc(17, "#4E3651", "#705374", "#352338", "#22172B")
    out["ui-arcade-dome"] = make_disc(13, "#FFD87E", "#FFF6D8", "#D99B38", "#22172B", [(3, 3, "#FFFFFF"), (4, 3, "#FFFFFF"), (3, 4, "#FFFFFF")])

    # 4) Đám mây nền trời: đúng kích cỡ native 28x10 px
    cpal = {"w": "#FFF1E8", "h": "#FFFAF4", "s": "#F6D8D2"}
    cg = [[cpal.get(ch) for ch in line] for line in CLOUD_ROWS]
    out["ui-sky-cloud"] = grid_to_img(cg)
    return out


def load_icons_from_js():
    text = ICONS_JS.read_text(encoding="utf-8")
    m = re.search(r"var IMG32\s*=\s*\{([\s\S]*?)\};", text)
    entries = re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(1))
    out = {}
    for name, b64 in entries:
        out[name] = Image.open(io.BytesIO(base64.b64decode(b64))).convert("RGBA")
    return out


def make_sheet(items, cols=5, cell=170, title="SPENDY UI ELEMENTS (NATIVE 1:1)"):
    names = list(items.keys())
    rows = math.ceil(len(names) / cols)
    pad = 16
    hdr = 44
    W = cols * cell + pad * 2
    H = rows * cell + pad * 2 + hdr
    sheet = Image.new("RGBA", (W, H), (28, 21, 46, 255))
    draw = ImageDraw.Draw(sheet)
    draw.rectangle([0, 0, W, hdr], fill=(42, 31, 66, 255))
    draw.text((pad, 14), f"{title} ({len(names)} files - Exact Native Pixel Size)", fill=(157, 245, 194, 255))
    for i, name in enumerate(names):
        r, c = divmod(i, cols)
        x0 = pad + c * cell
        y0 = hdr + pad + r * cell
        draw.rectangle([x0 + 4, y0 + 4, x0 + cell - 4, y0 + cell - 4], fill=(40, 30, 62, 255), outline=(94, 74, 132, 255), width=2)
        im = items[name]
        # Hiển thị phóng to giữ nguyên tỉ lệ gốc trên bảng tra cứu
        scale = min(96 // max(1, im.width), 96 // max(1, im.height))
        scale = max(2, scale)
        prev = im.resize((im.width * scale, im.height * scale), Image.NEAREST)
        sheet.alpha_composite(prev, (x0 + (cell - prev.width) // 2, y0 + 12 + (96 - prev.height) // 2))
        label = name if len(name) <= 23 else name[:21] + ".."
        draw.text((x0 + 8, y0 + cell - 36), label, fill=(255, 242, 226, 255))
        draw.text((x0 + 8, y0 + cell - 20), f"{im.width}x{im.height} px", fill=(157, 245, 194, 255))
    return sheet


README_VN = """========================================================================
  SPENDY · GÓI 188 ELEMENTS ĐÚNG KÍCH CỠ PIXEL GỐC (1:1) CHO PROCREATE
========================================================================

1. KÍCH CỠ GỐC (1:1) CỦA TẤT CẢ ELEMENTS TRONG GÓI:
   - 01_Icons/ (173 files)       : 32x32 px  (đúng kích cỡ gốc trong app)
   - 02_UI_Elements/ (15 files)  :
       • ui-dock-{triangle,circle,square,star}-{cap,well}.png : 29x27 px
       • ui-dock-ico-{home,chart,mai,wallet}.png              : 15x15 px
       • ui-arcade-ring.png                                   : 17x17 px
       • ui-arcade-dome.png                                   : 13x13 px
       • ui-sky-cloud.png                                     : 28x10 px
   - Spendy_Pastel_Dusk.swatches : Bảng màu chuẩn 30 màu của Spendy
   - 00_UI_Elements_Map.png      : Bảng xem nhanh tên & kích cỡ từng UI Element

2. CÁCH VẼ PIXEL 1:1 CHUẨN TRÊN PROCREATE (IPAD):
   Bước 1: Giải nén Spendy-Procreate-Pack.zip trong app Tệp (Files) trên iPad.
   Bước 2: Chạm vào file Spendy_Pastel_Dusk.swatches để nạp bảng màu vào Procreate.
   Bước 3: Mở thẳng file PNG muốn sửa bằng Procreate -> Canvas tự có đúng kích cỡ gốc
           (ví dụ 32x32 px, 29x27 px, 15x15 px, 28x10 px...).
   Bước 4: Dùng cọ Pixel 1px (hoặc bật Drawing Guide -> 2D Grid -> Grid Size: 1 px) để vẽ
           từng ô pixel sắc nét!
   Bước 5: Nhớ tắt Background Color (để nền trong suốt) rồi Share -> PNG, giữ nguyên tên file.

3. GỬI LẠI CHO EM ĐỂ ĐÓNG GÓI APK:
   - Nén các file PNG anh đã vẽ xong vào 1 file .zip (hoặc gửi trực tiếp vào chat).
   - Em sẽ giữ nguyên ĐÚNG kích cỡ gốc anh vẽ (không resize làm mờ), nạp vào app và xuất APK mới!
"""


def main():
    out_zip = Path(sys.argv[1]) if len(sys.argv) > 1 else (ROOT / "tools" / "icons32" / "Spendy-Procreate-Pack.zip")
    out_map = Path(sys.argv[2]) if len(sys.argv) > 2 else None
    out_zip.parent.mkdir(parents=True, exist_ok=True)

    icons = load_icons_from_js()
    ui_els = generate_ui_elements()
    ui_sheet = make_sheet(ui_els, cols=5, cell=170, title="SPENDY UI ELEMENTS (NATIVE 1:1)")

    if out_map:
        ui_sheet.save(out_map, format="PNG", optimize=True)

    with zipfile.ZipFile(out_zip, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("HUONG_DAN_PROCREATE.txt", README_VN.encode("utf-8"))
        zf.writestr("Spendy_Pastel_Dusk.swatches", make_procreate_swatches_bytes())

        buf_map = io.BytesIO()
        ui_sheet.save(buf_map, format="PNG", optimize=True)
        zf.writestr("00_UI_Elements_Map.png", buf_map.getvalue())

        # Xuất nguyên bản 1:1 đúng kích cỡ gốc (KHÔNG upscale)
        for name, im in sorted(ui_els.items()):
            b = io.BytesIO()
            im.save(b, format="PNG", optimize=True)
            zf.writestr(f"02_UI_Elements/{name}.png", b.getvalue())

        for name, im in sorted(icons.items()):
            b = io.BytesIO()
            im.save(b, format="PNG", optimize=True)
            zf.writestr(f"01_Icons/{name}.png", b.getvalue())

    print(f"Exported native 1:1: {len(icons)} icons (32x32) + {len(ui_els)} UI elements -> {out_zip} ({out_zip.stat().st_size / 1024:.1f} KB)")


if __name__ == "__main__":
    main()
