#!/usr/bin/env python3
"""Sinh icon ứng dụng Spendy: đồng xu vàng chữ "S" có đôi cánh bay (tiền "bay" đi = spend), nền hồng bo góc pixel.
Hình vẽ ở tools/appicon/concepts.py (mẫu B). Chạy: python3 tools/gen-app-icon.py
 -> web/icon-512.png + android/res/drawable-*/ic_launcher.png
Lưới 32×32 điểm ảnh, phóng to nguyên lần (512 = 16×); mật độ Android không chia hết thì thu nhỏ từ bản 512.
"""
import os, sys
from PIL import Image
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
sys.path.insert(0, os.path.join(ROOT, "tools", "appicon"))
import concepts
icon = concepts.out["B_wings"]
big = icon.img(16)
big.save(os.path.join(ROOT, "web", "icon-512.png"))
for name, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
    p = os.path.join(ROOT, "android", "res", "drawable-" + name, "ic_launcher.png")
    if os.path.isdir(os.path.dirname(p)):
        (icon.img(size // 32) if size % 32 == 0 else big.resize((size, size), Image.LANCZOS)).save(p)
print("ok")
