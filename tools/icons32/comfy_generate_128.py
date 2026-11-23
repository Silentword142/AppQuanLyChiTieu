#!/usr/bin/env python3
"""
Tạo hàng loạt Icon Pack, App Icon & UI Elements độ phân giải >= 128x128 bằng ComfyUI Desktop.

Hỗ trợ chạy:
  1) Trực tiếp trên máy Windows (G:\\2.Phan mem\\Comfy Desktop đang chạy tại http://127.0.0.1:8188):
     python tools/icons32/comfy_generate_128.py --url http://127.0.0.1:8188
  2) Từ xa qua Cloudflare Tunnel:
     python tools/icons32/comfy_generate_128.py --url https://<tunnel>.trycloudflare.com

Tự động dò checkpoint + node tách nền (RMBG/BRIA/Inspyrenet nếu có, hoặc tự tách nền thông minh bằng Pillow),
xuất ảnh RGBA trong suốt 128x128 (hoặc --size 256 / 512) và nạp thẳng vào web/icons.js + android/res/mipmap-*/ic_launcher.png!
"""
import argparse
import io
import json
import re
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ICONS_JS = ROOT / "web" / "icons.js"

# Prompt mô tả cho các UI Elements & App Icon
UI_PROMPTS = {
    "app_icon": "cute retro pastel handheld TV mascot icon with smiling screen, cozy pixel-inspired 2.5D game icon, dark plum outline #22172B, centered isolated on solid pure white background",
    "ui-arcade-ring": "dark plum circular arcade button bezel ring, retro game console UI, top-down 2.5D, dark outline, isolated on pure white background",
    "ui-arcade-dome": "glossy butter-yellow circular arcade dome button, cozy pastel retro game UI, isolated on pure white background",
    "ui-sky-cloud": "cute fluffy pastel cream pixel-styled cloud, cozy game sky element, isolated on pure magenta #FF00FF background",
}

SUBJECT_OVERRIDES = {
    "pho": "steaming bowl of Vietnamese pho noodle soup with chopsticks and herbs",
    "banh-mi": "crispy Vietnamese banh mi sandwich with fresh herbs",
    "phin": "Vietnamese coffee phin filter dripping over glass cup",
    "milk-tea": "pastel bubble milk tea cup with boba pearls and straw",
    "non-la": "traditional Vietnamese conical palm leaf hat non la",
    "ao-dai": "traditional Vietnamese ao dai dress",
    "envelope-red": "lucky red envelope li xi with gold coin emblem",
    "mai": "cute retro pastel mini TV mascot with smiling cyan face and rabbit-ear antenna",
    "scooter": "cute pastel retro motor scooter",
    "piggy": "cute pastel pink piggy bank with gold coin",
}


def api_json(base_url, path, data=None, timeout=30):
    url = base_url.rstrip("/") + path
    req = urllib.request.Request(url, method="POST" if data is not None else "GET")
    if data is not None:
        req.add_header("Content-Type", "application/json")
        body = json.dumps(data).encode("utf-8")
    else:
        body = None
    with urllib.request.urlopen(req, data=body, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def api_bytes(base_url, path, timeout=30):
    url = base_url.rstrip("/") + path
    with urllib.request.urlopen(url, timeout=timeout) as resp:
        return resp.read()


def remove_flat_bg(im: Image.Image) -> Image.Image:
    """Tách nền đồng màu (trắng hoặc hồng cánh sen) từ biên ảnh nếu ComfyUI chưa cài node RMBG."""
    im = im.convert("RGBA")
    w, h = im.size
    px = im.load()
    corners = [px[0, 0], px[w - 1, 0], px[0, h - 1], px[w - 1, h - 1]]
    bg = tuple(sum(c[i] for c in corners) // 4 for i in range(3))
    visited = [[False] * w for _ in range(h)]
    stack = [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]

    def close(c):
        return max(abs(c[0] - bg[0]), abs(c[1] - bg[1]), abs(c[2] - bg[2])) <= 38

    while stack:
        x, y = stack.pop()
        if not (0 <= x < w and 0 <= y < h) or visited[y][x]:
            continue
        visited[y][x] = True
        if close(px[x, y]):
            px[x, y] = (0, 0, 0, 0)
            stack.extend([(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)])
    # Cắt viền thừa & căn giữa vuông
    bbox = im.getbbox()
    if bbox:
        im = im.crop(bbox)
    side = max(im.width, im.height)
    pad = max(4, side // 14)
    canvas = Image.new("RGBA", (side + pad * 2, side + pad * 2), (0, 0, 0, 0))
    canvas.alpha_composite(im, ((canvas.width - im.width) // 2, (canvas.height - im.height) // 2))
    return canvas


def build_workflow(ckpt, prompt_text, neg_text, seed, width=512, height=512, rmbg_node=None):
    wf = {
        "3": {"class_type": "KSampler", "inputs": {"seed": seed, "steps": 22, "cfg": 7.0, "sampler_name": "euler_ancestral", "scheduler": "normal", "denoise": 1.0, "model": ["4", 0], "positive": ["6", 0], "negative": ["7", 0], "latent_image": ["5", 0]}},
        "4": {"class_type": "CheckpointLoaderSimple", "inputs": {"ckpt_name": ckpt}},
        "5": {"class_type": "EmptyLatentImage", "inputs": {"width": width, "height": height, "batch_size": 1}},
        "6": {"class_type": "CLIPTextEncode", "inputs": {"text": prompt_text, "clip": ["4", 1]}},
        "7": {"class_type": "CLIPTextEncode", "inputs": {"text": neg_text, "clip": ["4", 1]}},
        "8": {"class_type": "VAEDecode", "inputs": {"samples": ["3", 0], "vae": ["4", 2]}},
    }
    if rmbg_node:
        wf["9"] = {"class_type": rmbg_node, "inputs": {"image": ["8", 0]}}
        wf["10"] = {"class_type": "SaveImage", "inputs": {"filename_prefix": "spendy_128", "images": ["9", 0]}}
    else:
        wf["10"] = {"class_type": "SaveImage", "inputs": {"filename_prefix": "spendy_128", "images": ["8", 0]}}
    return wf


def main():
    ap = argparse.ArgumentParser(description="Generate Spendy >=128x128 icon pack via ComfyUI Desktop")
    ap.add_argument("--url", default="http://127.0.0.1:8188", help="ComfyUI server URL")
    ap.add_argument("--size", type=int, default=128, help="Output icon size >= 128 (default 128)")
    ap.add_argument("--out", default=str(ROOT / "dist" / "comfy_icons_128"), help="Output PNG folder")
    ap.add_argument("--limit", type=int, default=0, help="Limit number of icons (0 = all)")
    args = ap.parse_args()

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    size = max(128, args.size)

    info = api_json(args.url, "/object_info")
    ckpts = info["CheckpointLoaderSimple"]["input"]["required"]["ckpt_name"][0]
    if not ckpts:
        raise SystemExit("ComfyUI chưa có checkpoint model nào!")
    ckpt = next((c for c in ckpts if "pixel" in c.lower() or "sdxl" in c.lower() or "flux" in c.lower()), ckpts[0])
    rmbg = next((n for n in ("BRIA_RMBG_Zho", "RMBG", "ImageRembg+", "InspyrenetRembg") if n in info), None)
    print(f"Connected ComfyUI: ckpt={ckpt}, rmbg={rmbg or 'Pillow fallback'}, target={size}x{size}")

    text = ICONS_JS.read_text(encoding="utf-8")
    m = re.search(r"var IMG32\s*=\s*\{([\s\S]*?)\};", text)
    keys = [k for k, _ in re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(1))]
    all_targets = list(UI_PROMPTS.keys()) + keys
    if args.limit > 0:
        all_targets = all_targets[:args.limit]

    neg = "blurry, photo, realistic 3d, text, watermark, signature, frame, border, busy background, multiple objects, cropped"
    for idx, key in enumerate(all_targets, 1):
        out_file = out_dir / f"{key}.png"
        if out_file.exists():
            continue
        subj = UI_PROMPTS.get(key) or f"{SUBJECT_OVERRIDES.get(key, key.replace('-', ' '))}, cozy retro pastel game icon, crisp dark plum outline #22172B, minimalist 2.5D flat shading, centered isolated on solid pure white background"
        wf = build_workflow(ckpt, subj, neg, seed=4200 + idx, rmbg_node=rmbg)
        resp = api_json(args.url, "/prompt", {"prompt": wf})
        pid = resp["prompt_id"]
        while True:
            hist = api_json(args.url, f"/history/{pid}")
            if pid in hist:
                break
            time.sleep(0.6)
        outs = hist[pid]["outputs"]["10"]["images"][0]
        q = urllib.parse.urlencode({"filename": outs["filename"], "subfolder": outs.get("subfolder", ""), "type": outs.get("type", "output")})
        raw = api_bytes(args.url, f"/view?{q}")
        im = Image.open(io.BytesIO(raw)).convert("RGBA")
        if not rmbg:
            im = remove_flat_bg(im)
        im = im.resize((size, size), Image.LANCZOS)
        im.save(out_file, format="PNG", optimize=True)
        print(f"[{idx}/{len(all_targets)}] {key}.png ({size}x{size})")

    print(f"Xong! Chạy tiếp: python tools/icons32/apply_patch.py \"{out_dir}\" --mode hd --size {size}")


if __name__ == "__main__":
    main()
