#!/usr/bin/env python3
"""
Chuyển đổi thư mục ảnh PNG (16x16 trong suốt hoặc 512x512 nền #FF00FF từ ComfyUI / MOSS-Image-PixelArt-16x16)
thành SVG 16x16 siêu nét và cập nhật trực tiếp vào web/icons.js.

Cách dùng:
  python3 tools/import-16x16-icons.py /duong/dan/thu/muc/png
"""
import json, os, re, sys
from PIL import Image

def to_16x16_svg(png_path):
    im = Image.open(png_path).convert("RGBA")
    if im.size != (16, 16):
        # Lấy tâm mỗi block khi ảnh là 512x512 (32px/cell) hoặc resize nearest
        w, h = im.size
        if w >= 32 and w == h and w % 16 == 0:
            step = w // 16
            out = Image.new("RGBA", (16, 16))
            for y in range(16):
                for x in range(16):
                    out.putpixel((x, y), im.getpixel((x * step + step // 2, y * step + step // 2)))
            im = out
        else:
            im = im.resize((16, 16), Image.NEAREST)

    px = im.load()
    rects = []
    for y in range(16):
        x = 0
        while x < 16:
            r, g, b, a = px[x, y]
            # Lọc nền trong suốt hoặc nền magenta #FF00FF (ngưỡng khoảng cách màu <= 45)
            is_magenta = (a >= 128 and abs(r - 255) + g + abs(b - 255) <= 75)
            if a < 128 or is_magenta:
                x += 1
                continue
            hex_c = f"#{r:02x}{g:02x}{b:02x}"
            x2 = x + 1
            while x2 < 16:
                r2, g2, b2, a2 = px[x2, y]
                m2 = (a2 >= 128 and abs(r2 - 255) + g2 + abs(b2 - 255) <= 75)
                if a2 >= 128 and not m2 and f"#{r2:02x}{g2:02x}{b2:02x}" == hex_c:
                    x2 += 1
                else:
                    break
            rects.append(f'<rect x="{x}" y="{y}" width="{x2-x}" height="1" fill="{hex_c}"/>')
            x = x2
    return "".join(rects)

def main():
    if len(sys.argv) < 2:
        print("Cần truyền đường dẫn thư mục chứa các file <icon-name>.png")
        sys.exit(1)
    folder = sys.argv[1]
    icons_js_path = os.path.join(os.path.dirname(__file__), "..", "web", "icons.js")
    src = open(icons_js_path, "r", encoding="utf-8").read()
    m = re.search(r"var I = (\{.*?\});", src, re.S)
    if not m:
        print("Không tìm thấy bảng var I = {...} trong web/icons.js")
        sys.exit(1)
    I = json.loads(m.group(1))
    updated = 0
    for fn in sorted(os.listdir(folder)):
        if not fn.lower().endswith(".png"):
            continue
        key = os.path.splitext(fn)[0]
        if key not in I:
            continue
        svg_inner = to_16x16_svg(os.path.join(folder, fn))
        if svg_inner:
            I[key] = svg_inner
            updated += 1
    new_src = src[:m.start(1)] + json.dumps(I) + src[m.end(1):]
    open(icons_js_path, "w", encoding="utf-8").write(new_src)
    print(f"Đã cập nhật {updated} icon từ {folder} vào web/icons.js!")

if __name__ == "__main__":
    main()
