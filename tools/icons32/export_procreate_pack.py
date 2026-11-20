#!/usr/bin/env python3
"""
Xuất toàn bộ 188 Elements của Spendy (173 Icons + 15 UI Elements) thành gói Procreate cho iPad:
  - 01_Icons/*.png (512x512 PNG nền trong suốt, Nearest-Neighbor từ 32x32 để làm lớp tham chiếu vẽ đè)
  - 02_UI_Elements/*.png (512x512 PNG nền trong suốt cho 4 nút 2.5D, 4 icon đơn màu, nút + Ghi, mây)
  - Spendy_Pastel_Dusk.swatches (bảng màu chuẩn Procreate, chạm để import thẳng vào Procreate)
  - 00_UI_Elements_Map.png (bảng xem nhanh 15 UI Elements kèm tên file)
  - HUONG_DAN_PROCREATE.txt (hướng dẫn chi tiết cách vẽ trên iPad & gửi lại cho AI đóng gói APK)
"""
import base64
import io
import json
import math
import os
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
    # Hàng 1: Mực viền, nền CRT, kem sữa, trắng
    "#22172B", "#4A3350", "#2A2140", "#41325C", "#FCEEDD", "#FFF2E2", "#F5C4BC", "#EE9AA3", "#FFD87E", "#FFFFFF",
    # Hàng 2: Màu 4 nút danh mục 2.5D (Tam giác hồng, Tròn xanh lá, Vuông tím, Sao xanh dương)
    "#FFC8D0", "#F48E9E", "#A83A52", "#E2F2C2", "#ACD078", "#5B802B", "#E7D5FC", "#BE9CE8", "#684498", "#86B9EC",
    # Hàng 3: Màu sáng neon CRT & nhấn icon
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
    for sname, (rows, pal) in SHAPES.items():
        x_off, y_off, depth = 2, 3, 3

        def at(x, y):
            rx, ry = x - x_off, y - y_off
            return 0 <= ry < len(rows) and 0 <= rx < len(rows[0]) and rows[ry][rx] == "#"

        base_g = [[None] * 32 for _ in range(32)]
        base_m = [[False] * 32 for _ in range(32)]
        for d in range(depth, 0, -1):
            for y in range(32 - d):
                for x in range(32):
                    if at(x, y):
                        base_g[y + d][x] = pal["side"]
                        base_m[y + d][x] = True
        add_aseprite_outline(base_g, base_m, pal["side"])
        out[f"ui-dock-{sname}-well"] = grid_to_img(base_g)

        cap_g = [[None] * 32 for _ in range(32)]
        cap_m = [[False] * 32 for _ in range(32)]
        for y in range(32):
            for x in range(32):
                if at(x, y):
                    cap_g[y][x] = pal["hi"] if not at(x, y - 1) else pal["base"]
                    cap_m[y][x] = True
        add_aseprite_outline(cap_g, cap_m, pal["base"])
        out[f"ui-dock-{sname}-cap"] = grid_to_img(cap_g)

    for mname, rows in MONO_ICONS.items():
        g = [[None] * 32 for _ in range(32)]
        for ry, line in enumerate(rows):
            for rx, ch in enumerate(line):
                if ch == "#":
                    for dy in range(2):
                        for dx in range(2):
                            g[1 + ry * 2 + dy][1 + rx * 2 + dx] = "#FFF2E2"
        out[f"ui-dock-ico-{mname}"] = grid_to_img(g)

    def make_disc(d, col_body, col_hl, col_sh, col_ink, extra_pts=()):
        g = [[None] * 32 for _ in range(32)]
        off = (32 - d) // 2
        r = d / 2.0

        def inside(x, y):
            return math.hypot(x + 0.5 - r, y + 0.5 - r) <= r - 0.15

        for y in range(d):
            for x in range(d):
                if not inside(x, y):
                    continue
                if not (inside(x + 1, y) and inside(x - 1, y) and inside(x, y + 1) and inside(x, y - 1)):
                    g[off + y][off + x] = col_ink
                    continue
                c = col_body
                dx, dy = x + 0.5 - r, y + 0.5 - r
                if math.hypot(dx, dy) > r - 2.6:
                    if dx + dy < -r * 0.45:
                        c = col_hl
                    elif dx + dy > r * 0.45:
                        c = col_sh
                g[off + y][off + x] = c
        for ex, ey, ec in extra_pts:
            g[off + ey][off + ex] = ec
        return grid_to_img(g)

    out["ui-arcade-ring"] = make_disc(28, "#4E3651", "#705374", "#352338", "#22172B")
    out["ui-arcade-dome"] = make_disc(22, "#FFD87E", "#FFF6D8", "#D99B38", "#22172B", [(6, 6, "#FFFFFF"), (7, 6, "#FFFFFF"), (6, 7, "#FFFFFF")])

    cg = [[None] * 32 for _ in range(32)]
    cpal = {"w": "#FFF1E8", "h": "#FFFAF4", "s": "#F6D8D2"}
    for ry, line in enumerate(CLOUD_ROWS):
        for rx, ch in enumerate(line):
            if ch in cpal:
                cg[11 + ry][2 + rx] = cpal[ch]
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


def make_sheet(items, cols=5, cell=160, title="SPENDY UI ELEMENTS"):
    names = list(items.keys())
    rows = math.ceil(len(names) / cols)
    pad = 16
    hdr = 44
    W = cols * cell + pad * 2
    H = rows * cell + pad * 2 + hdr
    sheet = Image.new("RGBA", (W, H), (28, 21, 46, 255))
    draw = ImageDraw.Draw(sheet)
    draw.rectangle([0, 0, W, hdr], fill=(42, 31, 66, 255))
    draw.text((pad, 14), f"{title} ({len(names)} files - 512x512 PNG)", fill=(157, 245, 194, 255))
    for i, name in enumerate(names):
        r, c = divmod(i, cols)
        x0 = pad + c * cell
        y0 = hdr + pad + r * cell
        draw.rectangle([x0 + 4, y0 + 4, x0 + cell - 4, y0 + cell - 4], fill=(40, 30, 62, 255), outline=(94, 74, 132, 255), width=2)
        im = items[name].resize((96, 96), Image.NEAREST)
        sheet.alpha_composite(im, (x0 + (cell - 96) // 2, y0 + 16))
        label = name if len(name) <= 23 else name[:21] + ".."
        draw.text((x0 + 8, y0 + cell - 26), label, fill=(255, 242, 226, 255))
    return sheet


README_VN = """========================================================================
  SPENDY · GÓI VẼ LẠI TOÀN BỘ ELEMENTS BẰNG PROCREATE TRÊN IPAD (v4.8)
========================================================================

1. TRONG GÓI NÀY CÓ GÌ?
   - Spendy_Pastel_Dusk.swatches : Bảng màu chuẩn của app Spendy.
     → Trên iPad, anh chỉ cần chạm vào file .swatches này là Procreate tự
       thêm nguyên bảng màu (viền đen #22172B, màu 4 nút 2.5D, màu CRT...)!
   - 02_UI_Elements/ (15 files PNG 512x512 nền trong suốt):
       • ui-dock-triangle-cap.png / ui-dock-triangle-well.png  (Nút NHÀ - mặt trên & chân đế)
       • ui-dock-circle-cap.png   / ui-dock-circle-well.png    (Nút BÁO CÁO)
       • ui-dock-square-cap.png   / ui-dock-square-well.png    (Nút TỰ ĐỘNG)
       • ui-dock-star-cap.png     / ui-dock-star-well.png      (Nút VÍ & NỢ)
       • ui-dock-ico-home.png, ui-dock-ico-chart.png,
         ui-dock-ico-mai.png, ui-dock-ico-wallet.png           (4 icon đơn màu nằm giữa nút)
       • ui-arcade-ring.png / ui-arcade-dome.png               (Nút tròn "+ GHI" ở giữa)
       • ui-sky-cloud.png                                      (Đám mây trôi nền trời)
   - 01_Icons/ (173 files PNG 512x512 nền trong suốt):
       • Toàn bộ 173 icon hạng mục, ví, công cụ, mũi tên, đồng xu, heo đất...
   - 00_UI_Elements_Map.png : Bảng tra cứu nhanh tên 15 UI Elements.

2. CÁCH VẼ TRÊN PROCREATE (IPAD) SIÊU NHANH:
   Bước 1: Giải nén Spendy-Procreate-Pack.zip trong app Tệp (Files) của iPad.
   Bước 2: Chạm vào file Spendy_Pastel_Dusk.swatches để nạp bảng màu vào Procreate.
   Bước 3: Mở file PNG anh muốn vẽ lại bằng Procreate (hoặc kéo thả từ Files vào Procreate).
   Bước 4: Giảm Opacity của layer gốc xuống 30% làm nét mờ tham chiếu, tạo 1 Layer mới ở trên
           để vẽ nét tay của anh bằng Apple Pencil!
           (Mẹo: nếu vẽ pixel art, bật Actions -> Canvas -> Drawing Guide -> 2D Grid 16px).
   Bước 5: Trước khi xuất, NHỚ TẮT con mắt của:
           - Layer gốc tham chiếu
           - Layer "Background Color" (để nền trong suốt!)
   Bước 6: Xuất Share -> PNG, GIỮ NGUYÊN TÊN FILE GỐC (ví dụ: pho.png, ui-dock-triangle-cap.png).

3. CÁCH GỬI CHO AI ĐỂ TỰ ĐỘNG THAY VÀO APP & XUẤT APK MỚI:
   - Anh chỉ cần gom các file PNG anh đã vẽ xong vào 1 file .zip (hoặc gửi từng ảnh vào khung chat),
     nhắn em: "Thay các element này vào app (giữ nét vẽ tay HD / hoặc chuyển pixel 32x32) rồi xuất APK".
   - Em sẽ tự động nạp tất cả các file anh vẽ (dù anh vẽ 5 file hay vẽ hết 188 file) vào thẳng
     mã nguồn app và xuất file APK mới cài đè ngay!
"""


def main():
    out_zip = Path(sys.argv[1]) if len(sys.argv) > 1 else (ROOT / "dist" / "Spendy-Procreate-Pack.zip")
    out_map = Path(sys.argv[2]) if len(sys.argv) > 2 else None
    out_zip.parent.mkdir(parents=True, exist_ok=True)

    icons = load_icons_from_js()
    ui_els = generate_ui_elements()
    ui_sheet = make_sheet(ui_els, cols=5, cell=170, title="SPENDY UI ELEMENTS (15)")

    if out_map:
        ui_sheet.save(out_map, format="PNG", optimize=True)

    with zipfile.ZipFile(out_zip, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("HUONG_DAN_PROCREATE.txt", README_VN.encode("utf-8"))
        zf.writestr("Spendy_Pastel_Dusk.swatches", make_procreate_swatches_bytes())

        buf_map = io.BytesIO()
        ui_sheet.save(buf_map, format="PNG", optimize=True)
        zf.writestr("00_UI_Elements_Map.png", buf_map.getvalue())

        for name, im in sorted(ui_els.items()):
            hd = im.resize((512, 512), Image.NEAREST)
            b = io.BytesIO()
            hd.save(b, format="PNG", optimize=True)
            zf.writestr(f"02_UI_Elements/{name}.png", b.getvalue())

        for name, im in sorted(icons.items()):
            hd = im.resize((512, 512), Image.NEAREST)
            b = io.BytesIO()
            hd.save(b, format="PNG", optimize=True)
            zf.writestr(f"01_Icons/{name}.png", b.getvalue())

    print(f"Exported {len(icons)} icons + {len(ui_els)} UI elements -> {out_zip} ({out_zip.stat().st_size / 1024:.1f} KB)")


if __name__ == "__main__":
    main()
