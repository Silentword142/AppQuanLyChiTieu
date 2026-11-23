#!/usr/bin/env python3
"""
Tạo hàng loạt Pixel Art Icon 2D tối giản chuẩn UI/UX mang phong cách & văn hóa Việt Nam,
viền tối sắc nét của icon (không viền trắng, không bóng đổ dưới chân), độ phân giải >= 128x128
bằng ComfyUI Desktop theo Workflow 05 (05_Pixel_Product_Icon_CleanBG.json).

Cách chạy:
  python tools/icons32/comfy_generate_128.py --url https://<tunnel>.trycloudflare.com --size 128 --apply
"""
import argparse
import base64
import io
import json
import re
import time
import urllib.parse
import urllib.request
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ICONS_JS = ROOT / "web" / "icons.js"

# Mô tả mang đậm phong cách & văn hóa Việt Nam (tối ưu cho SDXL + pixel-art-xl 2D UI/UX)
SUBJECTS_EN = {
    "app_icon": "golden coin with bold dollar sign in center on pastel pink retro handheld game console rounded square UI badge",
    "mai": "golden five-petal Vietnamese apricot blossom flower",
    "ui-arcade-dome": "glossy butter-yellow circular game button with dark plum plus sign in center",
    "ui-arcade-ring": "dark plum circular game button bezel ring",
    "ui-sky-cloud": "fluffy pastel cream pixel art cloud",
    # Ăn uống (phong cách Việt Nam)
    "pho": "ceramic bowl of Vietnamese beef pho noodle soup with chopsticks and green basil herbs, floating isolated",
    "banh-mi": "elongated crispy golden Vietnamese banh mi baguette sandwich stuffed with pate meat and cilantro, angled",
    "phin": "traditional Vietnamese stainless steel phin coffee filter sitting on top of a clear glass of iced milk coffee",
    "coffee": "Vietnamese iced milk coffee ca phe sua da in glass with condensation",
    "milk-tea": "pastel bubble milk tea cup with tapioca pearls and straw, clean flat base",
    "cup-soda": "glass of iced kumquat lemon tea tra tac with straw",
    "utensils": "wooden chopsticks and ceramic Asian soup spoon crossed",
    "rice": "traditional Vietnamese ceramic bowl of steamed white rice with wooden chopsticks",
    "chef": "white chef hat",
    "pizza": "slice of pepperoni cheese pizza",
    "cake": "pastel birthday cake slice with strawberry",
    "ice-cream": "pastel coconut ice cream cone",
    "candy": "wrapped pastel sweet candy",
    "apple": "fresh red apple with green leaf",
    "fish": "fresh silver fish",
    "egg": "fried egg sunny side up",
    "carrot": "orange carrot with green top",
    "beer": "glass mug of cold golden beer with white foam",
    "wine": "red wine glass",
    "cart": "pastel shopping cart",
    "basket": "traditional Vietnamese woven bamboo market basket with fresh vegetables",
    # Di chuyển (phong cách Việt Nam)
    "scooter": "cute mint pastel Vietnamese street motor scooter, side view, single object",
    "bike": "cute vintage pastel bicycle",
    "car": "cute compact pastel city car",
    "taxi": "green-yellow Vietnamese city taxi cab",
    "bus": "cute pastel city bus",
    "train": "modern cute train front",
    "plane": "cute white and blue airplane",
    "ship": "cute small wooden boat",
    "fuel": "red gas fuel pump",
    "parking": "blue P parking sign",
    "wrench": "silver mechanic wrench",
    "road": "curved road with white dashed line",
    "map-pin": "red map location pin",
    "compass": "classic golden compass",
    "luggage": "pastel travel suitcase",
    "ticket": "yellow event ticket",
    # Nhà cửa (phong cách Việt Nam)
    "home": "cozy traditional Vietnamese house with red clay tile roof, yellow ochre walls and wooden double door",
    "building": "cute pastel Vietnamese narrow shophouse nha ong with balcony plants",
    "zap": "bright yellow lightning bolt",
    "droplet": "clean blue water droplet",
    "wifi": "glowing cyan wifi signal icon",
    "tv": "cute retro television set",
    "bulb": "warm glowing yellow lightbulb",
    "bed": "cozy wooden bed with bamboo mat and pillow",
    "bath": "white bathtub with bubbles",
    "sofa": "cozy wooden sofa with pastel cushions",
    "fan": "cute pastel electric desk fan",
    "plug": "electric power plug",
    "flame": "warm orange flame",
    "key": "golden vintage key",
    "lock": "golden padlock",
    "tree": "lush green tropical banyan tree",
    "sprout": "fresh green rice plant sprout",
    "flower": "pink Vietnamese lotus flower hoa sen blossom",
    "trash": "clean small trash bin",
    # Mua sắm (phong cách Việt Nam)
    "bag": "cute pastel pink shopping tote bag",
    "shirt": "folded pastel blue shirt",
    "ao-dai": "traditional Vietnamese pastel pink ao dai long dress",
    "shoe": "cute pastel sneaker shoe",
    "watch": "brown strap wristwatch",
    "glasses": "round eyeglasses",
    "gem": "sparkling cyan diamond gem",
    "lipstick": "red-pink lipstick tube",
    "scissors": "craft scissors",
    "gift": "gift box with red ribbon bow",
    "package": "brown cardboard delivery parcel box",
    "store": "cute Vietnamese street convenience shop tiem tap hoa with striped awning",
    "crown": "golden crown with gems",
    "umbrella": "pastel umbrella",
    "non-la": "traditional Vietnamese conical palm leaf hat non la with red chin strap",
    "tag": "yellow price tag",
    "sparkles": "golden stars sparkles",
    # Tài chính & Công nghệ
    "wallet": "cute brown leather wallet with gold clasp",
    "credit-card": "blue bank credit card with gold chip",
    "banknote": "green Vietnamese paper banknote bill",
    "coins": "stack of golden coins",
    "piggy": "cute pastel pink piggy bank",
    "salary": "golden money bag with coin",
    "hand-coins": "hand holding gold coin",
    "landmark": "classical bank building with pillars",
    "safe": "blue-grey metal bank safe",
    "receipt": "white paper receipt bill",
    "calculator": "pastel pocket calculator",
    "percent": "orange percent sign",
    "trend-up": "green upward chart arrow",
    "trend-down": "red downward chart arrow",
    "pie": "colorful pie chart",
    "chart": "green bar chart",
    "briefcase": "brown leather briefcase",
    "handshake": "friendly handshake icon",
    "shield": "blue security shield",
    "phone": "smartphone with glowing screen",
    "laptop": "open laptop computer",
    "monitor": "desktop computer monitor",
    "headphones": "pastel over-ear headphones",
    "gamepad": "retro game controller",
    "camera": "cute retro camera",
    "printer": "compact office printer",
    "radio": "retro radio with antenna",
    "cassette": "retro cassette tape",
    # Sức khỏe, Giải trí, Học tập, Gia đình
    "heart": "cute red heart",
    "heart-pulse": "red heart with pulse line",
    "pill": "two-tone medicine capsule pill",
    "stethoscope": "medical stethoscope",
    "syringe": "clean medical syringe",
    "tooth": "clean white tooth",
    "cross": "first aid medical kit with red cross",
    "dumbbell": "gym dumbbell weight",
    "activity": "fitness tracker smartwatch",
    "yoga": "rolled purple yoga mat",
    "baby": "cute baby milk bottle",
    "film": "cinema film reel",
    "music": "purple music note",
    "guitar": "wooden acoustic guitar",
    "vinyl": "black vinyl record disc",
    "disco": "sparkling disco ball",
    "party": "colorful party popper",
    "dice": "white dice cube",
    "palm": "tropical Vietnamese coconut palm tree",
    "tent": "orange camping tent",
    "mountain": "green limestone mountain peak",
    "paw": "cute pet paw print",
    "cat": "cute Vietnamese tabby cat face",
    "dog": "cute golden Vietnamese puppy face",
    "book": "blue hardcover book",
    "book-open": "open book pages",
    "graduation": "black graduation cap with gold tassel",
    "pencil": "yellow pencil with pink eraser",
    "school": "traditional Vietnamese yellow-wall red-roof school building",
    "lamp": "warm desk study lamp",
    "file": "yellow document folder",
    "globe": "blue and green earth globe",
    "users": "two cute friendly people icons",
    "user": "cute single person avatar",
    "home-heart": "cozy Vietnamese house with pink heart",
    "envelope-red": "Vietnamese lucky red envelope li xi with gold coin",
    "phone-call": "classic telephone handset",
    "mail": "white mail envelope with stamp",
    "pagoda": "traditional Vietnamese curved-roof pagoda chua",
    "flag": "Vietnamese red flag with yellow five-point star",
    "star": "bright golden five-point star",
}


def api_json(base_url, path, data=None, timeout=60):
    url = base_url.rstrip("/") + path
    body = json.dumps(data).encode("utf-8") if data is not None else None
    for attempt in range(5):
        try:
            req = urllib.request.Request(url, method="POST" if data is not None else "GET")
            req.add_header("User-Agent", "Mozilla/5.0")
            if data is not None:
                req.add_header("Content-Type", "application/json")
            with urllib.request.urlopen(req, data=body, timeout=timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except Exception:
            if attempt == 4:
                raise
            time.sleep(2.5)


def api_bytes(base_url, path, timeout=60):
    url = base_url.rstrip("/") + path
    for attempt in range(5):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return resp.read()
        except Exception:
            if attempt == 4:
                raise
            time.sleep(2.5)


def clean_icon_rgba(im: Image.Image, target_size: int = 128) -> Image.Image:
    """Giữ nguyên viền tối sắc nét của icon, loại bỏ hoàn toàn bóng xám dưới chân nếu sót lại, căn giữa 128x128."""
    im = im.convert("RGBA")
    w, h = im.size
    px = im.load()
    # Xóa các pixel bóng xám/tím nhạt bán trong suốt hoặc nằm rời ở rìa dưới không có viền tối
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            if a < 180:
                px[x, y] = (0, 0, 0, 0)
    bbox = im.getbbox()
    if bbox:
        im = im.crop(bbox)
    cw, ch = im.size
    pad = max(4, target_size // 20)
    inner = target_size - pad * 2
    scale = min(inner / max(1, cw), inner / max(1, ch))
    nw, nh = max(1, int(round(cw * scale))), max(1, int(round(ch * scale)))
    im = im.resize((nw, nh), Image.NEAREST if target_size <= 128 else Image.LANCZOS)
    canvas = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
    canvas.alpha_composite(im, ((target_size - nw) // 2, (target_size - nh) // 2))
    return canvas


def build_wf07_3lora(subject: str, target_size: str, seed: int):
    idea = f"logo, {subject}, đơn giản"
    pix_res = "256x256" if int(target_size) >= 256 else "64x64"
    return {
        "1": {"class_type": "CLIPromptExpander", "inputs": {
            "idea": idea,
            "style": "logo_design",
            "camera_angle": "orthographic_front",
            "pixel_resolution": pix_res,
            "cli_backend": "agy",
            "ollama_model": "qwen2.5:7b",
            "fallback_to_template": True
        }},
        "2": {"class_type": "CheckpointLoaderSimple", "inputs": {"ckpt_name": "sd_xl_base_1.0.safetensors"}},
        "3": {"class_type": "VAELoader", "inputs": {"vae_name": "sdxl_vae_fp16_fix.safetensors"}},
        "4": {"class_type": "LoraLoader", "inputs": {
            "lora_name": "pixel-art-xl\\pixel-art-xl.safetensors",
            "strength_model": 0.65,
            "strength_clip": 0.65,
            "model": ["2", 0],
            "clip": ["2", 1]
        }},
        "15": {"class_type": "LoraLoader", "inputs": {
            "lora_name": "SDXL-FlatIllustration\\pytorch_lora_weights.safetensors",
            "strength_model": 0.3,
            "strength_clip": 0.3,
            "model": ["4", 0],
            "clip": ["4", 1]
        }},
        "16": {"class_type": "LoraLoader", "inputs": {
            "lora_name": "LogoRedmond-V2\\LogoRedmondV2-Logo-LogoRedmAF.safetensors",
            "strength_model": 0.45,
            "strength_clip": 0.45,
            "model": ["15", 0],
            "clip": ["15", 1]
        }},
        "5": {"class_type": "CLIPTextEncode", "inputs": {"text": ["1", 0], "clip": ["16", 1]}},
        "6": {"class_type": "CLIPTextEncode", "inputs": {"text": ["1", 1], "clip": ["16", 1]}},
        "7": {"class_type": "EmptyLatentImage", "inputs": {"width": 1024, "height": 1024, "batch_size": 1}},
        "8": {"class_type": "KSampler", "inputs": {
            "seed": seed, "steps": 25, "cfg": 7.5,
            "sampler_name": "euler_ancestral", "scheduler": "normal", "denoise": 1.0,
            "model": ["16", 0], "positive": ["5", 0], "negative": ["6", 0], "latent_image": ["7", 0]
        }},
        "9": {"class_type": "VAEDecode", "inputs": {"samples": ["8", 0], "vae": ["3", 0]}},
        "10": {"class_type": "RembgRemoveBackground", "inputs": {
            "images": ["9", 0],
            "model_name": "floodfill_solid_bg",
            "bg_tolerance": 31,
            "shadow_style": "none",
            "shadow_opacity": 0.0
        }},
        "11": {"class_type": "PixelSnapAndResize", "inputs": {
            "images": ["10", 0],
            "masks": ["10", 1],
            "target_size": target_size,
            "resize_method": "pixel_sharp",
            "auto_crop": True,
            "color_quantize": "None"
        }},
        "12": {"class_type": "SaveTransparentPNG", "inputs": {
            "images": ["11", 0],
            "masks": ["11", 1],
            "filename_prefix": "Hybrid_3LoRA_Logo_RGBA"
        }}
    }


def apply_to_app(out_dir: Path):
    text = ICONS_JS.read_text(encoding="utf-8")
    m = re.search(r"(var IMG32\s*=\s*\{)([\s\S]*?)(\};)", text)
    entries = dict(re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(2)))
    updated = 0
    for p in sorted(out_dir.glob("*.png")):
        key = p.stem.lower()
        if key == "app_icon":
            continue
        entries[key] = base64.b64encode(p.read_bytes()).decode("ascii")
        updated += 1
    lines = [f'    "{k}": "{v}"' for k, v in sorted(entries.items())]
    new_block = m.group(1) + "\n" + ",\n".join(lines) + "\n  " + m.group(3)
    ICONS_JS.write_text(text[:m.start()] + new_block + text[m.end():], encoding="utf-8")
    print(f"Đã nạp {updated} icons 2D pixel art Việt Nam (>=128x128) vào web/icons.js")

    app_ico = out_dir / "app_icon.png"
    if app_ico.exists():
        im = Image.open(app_ico).convert("RGBA")
        sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
        for dens, px in sizes.items():
            dst = ROOT / "android" / "res" / f"drawable-{dens}" / "ic_launcher.png"
            if dst.parent.exists():
                im.resize((px, px), Image.LANCZOS).save(dst, format="PNG", optimize=True)
        print("Đã cập nhật android/res/drawable-*/ic_launcher.png từ app_icon.png")


def main():
    ap = argparse.ArgumentParser(description="Generate >=128x128 Vietnamese 2D Pixel Art UI/UX icons (icon outline, zero shadow) via ComfyUI WF05")
    ap.add_argument("--url", default="http://127.0.0.1:8188", help="ComfyUI server URL")
    ap.add_argument("--size", type=int, default=64, choices=[64, 128, 256, 512], help="Target resolution (64, 128, 256, 512)")
    ap.add_argument("--out", default=str(ROOT / "dist" / "comfy_icons_128"), help="Output folder")
    ap.add_argument("--keys", default="", help="Comma-separated subset of keys to generate")
    ap.add_argument("--limit", type=int, default=0, help="Max items to generate in this run (0 = all)")
    ap.add_argument("--apply", action="store_true", help="Apply generated icons to web/icons.js and Android ic_launcher")
    args = ap.parse_args()

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    size = max(64, args.size)

    text = ICONS_JS.read_text(encoding="utf-8")
    m = re.search(r"var IMG32\s*=\s*\{([\s\S]*?)\};", text)
    img_keys = [k for k, _ in re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(1))]
    CATEGORY_IDEAS_VI = {
        "app_icon": "đồng tiền vàng kí hiệu dollar ở giữa trên nền hồng máy chơi game cầm tay",
        "mai": "hoa mai vàng năm cánh cười dễ thương",
        "wallet": "một chiếc ví da nâu",
        "banknote": "tờ tiền giấy xanh lá",
        "coins": "đồng tiền xu vàng xếp chồng",
        # 11 Danh mục lớn (DEFAULT_PARENT_CATEGORIES)
        "pho": "tô phở bò việt nam",
        "scooter": "xe tay ga nhỏ gọn",
        "home": "ngôi nhà mái ngói",
        "bag": "túi xách mua sắm",
        "palm": "cây dừa du lịch",
        "heart": "trái tim đỏ y tế",
        "graduation": "một chiếc mũ tốt nghiệp đen",
        "receipt": "tờ hóa đơn thanh toán",
        "users": "biểu tượng gia đình",
        "salary": "túi tiền lương",
        "more": "hộp quà đa năng",
        # Danh mục nhỏ (DEFAULT_SUBCATEGORIES)
        "banh-mi": "bánh mì việt nam thơm ngon",
        "rice": "bát cơm trắng việt nam",
        "phin": "ly cà phê phin việt nam",
        "ice-cream": "que kem ốc quế",
        "basket": "giỏ đi chợ rau củ",
        "beer": "ly bia vàng bọt trắng",
        "fuel": "trụ bơm xăng",
        "taxi": "xe taxi màu vàng",
        "parking": "biển báo đỗ xe màu xanh",
        "wrench": "cờ lê sửa xe",
        "plane": "máy bay chở khách",
        "bulb": "bóng đèn điện sáng",
        "wifi": "biểu tượng sóng wifi",
        "tv": "tivi màn hình phẳng",
        "ao-dai": "áo dài việt nam",
        "phone": "điện thoại thông minh",
        "lipstick": "thỏi son môi đỏ",
        "watch": "đồng hồ đeo tay",
        "film": "cuộn phim xem phim",
        "luggage": "vali du lịch",
        "gamepad": "tay cầm chơi game",
        "pill": "viên thuốc y tế",
        "stethoscope": "ống nghe bác sĩ",
        "dumbbell": "quả tạ tập gym",
        "book": "quyển sách giáo khoa",
        "book-open": "cuốn sách mở trang",
        "phone-call": "điện thoại đang gọi",
        "credit-card": "thẻ ngân hàng",
        "shield": "chiếc khiên bảo hiểm",
        "baby": "bình sữa em bé",
        "envelope-red": "bao lì xì đỏ may mắn",
        "paw": "dấu chân thú cưng",
        "trend-up": "mũi tên tăng trưởng",
        "piggy": "heo đất tiết kiệm hồng"
    }
    ordered = list(CATEGORY_IDEAS_VI.keys())

    if args.keys:
        ordered = [x.strip() for x in args.keys.split(",") if x.strip()]

    done = 0
    for idx, key in enumerate(ordered, 1):
        out_file = out_dir / f"{key}.png"
        if out_file.exists():
            continue
        if args.limit > 0 and done >= args.limit:
            break
        subj = CATEGORY_IDEAS_VI.get(key) or SUBJECTS_EN.get(key, f"icon {key.replace('-', ' ')}")
        tsize = 512 if key == "app_icon" else size
        t0 = time.time()
        im_info = None
        for attempt in range(3):
            wf = build_wf07_3lora(subj, str(tsize), seed=8000 + idx + attempt * 1000)
            res = api_json(args.url, "/prompt", {"prompt": wf})
            pid = res["prompt_id"]
            while True:
                hist = api_json(args.url, f"/history/{pid}")
                if pid in hist and (hist[pid].get("status", {}).get("completed", False) or "outputs" in hist[pid]):
                    break
                time.sleep(1.0)
            outs = hist[pid].get("outputs", {})
            if "12" in outs and outs["12"].get("images"):
                im_info = outs["12"]["images"][0]
                break
        if not im_info:
            print(f"[SKIP] {key}.png failed to produce output 12")
            continue
        q = urllib.parse.urlencode({"filename": im_info["filename"], "subfolder": im_info.get("subfolder", ""), "type": im_info.get("type", "output")})
        raw = api_bytes(args.url, f"/view?{q}")
        im = clean_icon_rgba(Image.open(io.BytesIO(raw)), target_size=tsize)
        im.save(out_file, format="PNG", optimize=True)
        done += 1
        print(f"[{done}] {key}.png ({tsize}x{tsize}, Vietnamese 2D pixel, no white border, no shadow) - {time.time() - t0:.1f}s")

    if args.apply:
        apply_to_app(out_dir)


if __name__ == "__main__":
    main()
