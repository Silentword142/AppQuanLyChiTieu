#!/usr/bin/env python3
"""
WF08 "Pixel Art XL thuần" – vẽ lại icon Spendy bằng SDXL + LoRA nerijs/pixel-art-xl,
làm đúng theo model card (https://huggingface.co/nerijs/pixel-art-xl):
  - Base: sd_xl_base_1.0, KHÔNG dùng refiner, VAE đã sửa (sdxl_vae_fp16_fix).
  - Không cần từ khoá kích hoạt; prompt ngắn kiểu "pixel art, <vật>, simple, flat colors".
  - Kèm LCM LoRA (latent-consistency/lcm-lora-sdxl): 8 bước, CFG 1.5, LoRA pixel 1.2.
    Không có LCM LoRA -> chạy --no-lcm: 25 bước euler_a, CFG 7, LoRA pixel 1.0.
  - Thu nhỏ bằng Nearest để ra pixel chuẩn -> tools/pixelxl/pixelize.py (lưới 32, ép bảng màu app).
Khác WF07: chỉ 1 LoRA pixel ở mức mạnh (WF07 trộn pixel-art-xl 0.65 + FlatIllustration + LogoRedmond
nên lưới điểm không đều, ~500 màu/icon), chỉ dùng node gốc của ComfyUI (không cần custom node).

Chạy trên máy có ComfyUI:
  python tools/pixelxl/gen_pixelxl.py --samples              # 12 icon mẫu + ảnh so sánh
  python tools/pixelxl/gen_pixelxl.py                        # toàn bộ icon
  python tools/pixelxl/gen_pixelxl.py --apply                # nạp vào web/icons.js
Tuỳ chọn: --url http://127.0.0.1:8188 --lora "pixel-art-xl\\pixel-art-xl.safetensors"
          --lcm "lcm-lora-sdxl.safetensors" --variants 3 (sinh 3 bản/icon để chọn)
"""
import argparse, base64, importlib.util, io, json, re, sys, time, urllib.parse, urllib.request
from pathlib import Path
from PIL import Image

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
ICONS_JS = ROOT / "web" / "icons.js"
sys.path.insert(0, str(HERE))
from pixelize import pixelize  # noqa: E402

# Dùng lại mô tả vật thể đã có ở WF07
_spec = importlib.util.spec_from_file_location("wf07", ROOT / "tools" / "icons32" / "comfy_generate_128.py")
_wf07 = importlib.util.module_from_spec(_spec); _spec.loader.exec_module(_wf07)
SUBJECTS = dict(_wf07.SUBJECTS_EN)
SUBJECTS.update({
    "activity": "red heart with white pulse line",
    "users": "two smiling friends side by side, busts",
    "graduation": "black graduation cap with gold tassel",
    "envelope-red": "red lucky money envelope with gold pattern",
    "paw": "pink pet paw print",
    "more": "pastel gift box",
    "palm": "coconut palm tree on small island",
    "book": "closed green textbook",
    "book-open": "open book with pages",
    "phone-call": "smartphone with ringing waves",
    "ao-dai": "pink Vietnamese ao dai long dress, flat lay, isolated, no hanger, no person",
    "receipt": "curled shop receipt paper strip with zigzag bottom edge, black text lines and total, slightly tilted",
    "heart": "plump pink filled heart, cute",
    "wallet": "small brown leather wallet with gold clasp, front view",
    "piggy": "cute pink piggy bank with coin slot, side view",
    "credit-card": "simple blue bank card with gold chip, flat",
    "coffee": "Vietnamese iced coffee glass with straw",
    "home": "small cute house with red roof and one door, compact",
    "banh-mi": "Vietnamese banh mi baguette sandwich, side view",
    "scooter": "mint green vespa scooter, side view",
    "alert": "yellow warning triangle with exclamation mark",
    "alert-circle": "red round alert badge with exclamation mark",
    "bell": "golden bell", "bell-off": "grey bell with slash", "bell-ring": "golden ringing bell with motion lines",
    "calendar": "desk calendar page with red header", "chart-up": "bar chart with rising green arrow",
    "chat": "blue speech bubble", "check-circle": "green circle with white check mark",
    "clock": "round wall clock", "cloud": "fluffy white cloud", "cloud-off": "grey cloud with slash",
    "cloud-up": "white cloud with upward arrow", "contact": "address book contact card",
    "history": "clock with circular arrow", "image": "framed picture of mountains and sun",
    "inbox": "inbox tray with letter", "info": "blue round info badge with letter i",
    "key-house": "golden key with small house keychain", "mic": "retro microphone",
    "moon": "yellow crescent moon", "palette": "wooden paint palette with color blobs",
    "pen": "fountain pen", "save": "floppy disk", "settings": "silver gear cog",
    "shield-check": "blue shield with check mark", "sun": "bright yellow sun",
    "tags": "two yellow price tags", "upload": "box with upward arrow",
})

# Không vẽ bằng AI: nút/khung UI (đã có bản pixel vẽ bằng code trong index.html, đổi màu theo theme)
# và logo thương hiệu (Google) phải giữ đúng bản gốc.
SKIP = lambda k: k.startswith("ui-") or k == "google"

# Icon cho nút bấm / dock / HUD của giao diện TV mới (vẽ bằng AI, có màu)
UI_SUBJECTS = {
    "ui-key-chi": "big bold red arrow pointing up above a pink wallet",
    "ui-key-thu": "big bold green arrow pointing down into an open brown wallet with gold coins",
    "ui-key-scan": "cute pastel blue retro photo camera",
    "ui-key-budget": "red and white archery target with golden arrow in bullseye",
    "ui-key-voice": "cute retro pink microphone",
    "ui-hud-settings": "single dark grey metal gear cog with eight square teeth and round hole, bold outline",
    "ui-hud-coin": "shiny gold coin with a dollar sign",
    "ui-dock-home": "cozy small house with red roof, chimney and round window",
    "ui-dock-report": "simple bar chart with three thick colorful bars on a small board",
    "ui-dock-auto": "cute small retro tv robot with antenna and smiling face",
    "ui-dock-wallet": "brown leather wallet with green banknote and gold clasp",
}
UI_KEYS = list(UI_SUBJECTS)
SUBJECTS.update(UI_SUBJECTS)

SAMPLE_KEYS = ["pho", "banh-mi", "scooter", "home", "wallet", "piggy",
               "credit-card", "coffee", "ao-dai", "gift", "heart", "receipt"]

PROMPT = "pixel art, {subj}, simple icon, flat colors, bold dark outline, centered, single object, front view, plain white background"
NEG = "3d render, realistic, photo, gradient, blurry, text, watermark, frame, border, circle badge, multiple objects, cast shadow, drop shadow, shadow on floor, ground, cropped, person, top-down view"


def workflow(subj, seed, a):
    lora_w = 1.0 if a.no_lcm else 1.2
    wf = {
        "1": {"class_type": "CheckpointLoaderSimple", "inputs": {"ckpt_name": a.ckpt}},
        "2": {"class_type": "VAELoader", "inputs": {"vae_name": a.vae}},
        "3": {"class_type": "LoraLoader", "inputs": {"lora_name": a.lora, "strength_model": lora_w,
              "strength_clip": lora_w, "model": ["1", 0], "clip": ["1", 1]}},
        "5": {"class_type": "CLIPTextEncode", "inputs": {"text": PROMPT.format(subj=subj), "clip": ["3", 1]}},
        "6": {"class_type": "CLIPTextEncode", "inputs": {"text": NEG, "clip": ["3", 1]}},
        "7": {"class_type": "EmptyLatentImage", "inputs": {"width": 1024, "height": 1024, "batch_size": 1}},
        "9": {"class_type": "VAEDecode", "inputs": {"samples": ["8", 0], "vae": ["2", 0]}},
        "10": {"class_type": "SaveImage", "inputs": {"images": ["9", 0], "filename_prefix": "spendy_pxl"}},
    }
    model = ["3", 0]
    if not a.no_lcm:
        wf["4"] = {"class_type": "LoraLoader", "inputs": {"lora_name": a.lcm, "strength_model": 1.0,
                   "strength_clip": 1.0, "model": ["3", 0], "clip": ["3", 1]}}
        model = ["4", 0]
        ks = dict(steps=8, cfg=1.5, sampler_name="lcm", scheduler="sgm_uniform")
    else:
        ks = dict(steps=25, cfg=7.0, sampler_name="euler_ancestral", scheduler="normal")
    wf["8"] = {"class_type": "KSampler", "inputs": dict(seed=seed, denoise=1.0, model=model,
               positive=["5", 0], negative=["6", 0], latent_image=["7", 0], **ks)}
    return wf


def call(url, path, data=None):
    req = urllib.request.Request(url.rstrip("/") + path, data=json.dumps(data).encode() if data else None,
                                 headers={"Content-Type": "application/json", "User-Agent": "spendy-pxl"})
    with urllib.request.urlopen(req, timeout=120) as r:
        body = r.read()
    return body if path.startswith("/view") else json.loads(body)


def render(subj, seed, a):
    pid = call(a.url, "/prompt", {"prompt": workflow(subj, seed, a)})["prompt_id"]
    while True:
        h = call(a.url, f"/history/{pid}")
        if pid in h and h[pid].get("outputs"):
            break
        if pid in h and h[pid].get("status", {}).get("status_str") == "error":
            raise RuntimeError(json.dumps(h[pid]["status"])[:500])
        time.sleep(1)
    info = h[pid]["outputs"]["10"]["images"][0]
    q = urllib.parse.urlencode({"filename": info["filename"], "subfolder": info.get("subfolder", ""), "type": info.get("type", "output")})
    return Image.open(io.BytesIO(call(a.url, f"/view?{q}")))


def icon_keys():
    m = re.search(r"var IMG32\s*=\s*\{([\s\S]*?)\};", ICONS_JS.read_text(encoding="utf-8"))
    return [k for k, _ in re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(1)) if not SKIP(k)]


def sheet(out_dir, keys, dst, cell=128):
    cols = 6
    files = [(k, sorted(out_dir.glob(f"{k}*.png"))) for k in keys]
    files = [(k, fs) for k, fs in files if fs]
    rows = sum((len(fs) + cols - 1) // cols for _, fs in files) or 1
    im = Image.new("RGBA", (cols * cell, rows * cell), (255, 240, 230, 255))
    r = 0
    for _, fs in files:
        for i, f in enumerate(fs):
            if i and i % cols == 0: r += 1
            im.alpha_composite(Image.open(f).convert("RGBA").resize((cell, cell), Image.NEAREST), ((i % cols) * cell, r * cell))
        r += 1
    im.save(dst)


def apply(out_dir):
    text = ICONS_JS.read_text(encoding="utf-8")
    m = re.search(r"(var IMG32\s*=\s*\{)([\s\S]*?)(\};)", text)
    entries = dict(re.findall(r'"([^"]+)":\s*"([^"]+)"', m.group(2)))
    n = 0
    for k in entries:
        f = out_dir / f"{k}.png"  # bản đã chọn (không có hậu tố _v2...)
        if f.exists():
            entries[k] = base64.b64encode(f.read_bytes()).decode("ascii"); n += 1
    body = ",\n".join(f'    "{k}": "{v}"' for k, v in sorted(entries.items()))
    ICONS_JS.write_text(text[:m.start()] + m.group(1) + "\n" + body + "\n  " + m.group(3) + text[m.end():], encoding="utf-8")
    print(f"Đã nạp {n} icon Pixel Art XL vào web/icons.js")


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--url", default="http://127.0.0.1:8188")
    ap.add_argument("--ckpt", default="sd_xl_base_1.0.safetensors")
    ap.add_argument("--vae", default="sdxl_vae_fp16_fix.safetensors")
    ap.add_argument("--lora", default="pixel-art-xl\\pixel-art-xl.safetensors")
    ap.add_argument("--lcm", default="lcm-lora-sdxl.safetensors")
    ap.add_argument("--no-lcm", action="store_true")
    ap.add_argument("--samples", action="store_true", help="chỉ sinh 12 icon mẫu")
    ap.add_argument("--ui", action="store_true", help="chỉ sinh icon nút/dock/HUD (ui-*)")
    ap.add_argument("--keys", default="")
    ap.add_argument("--variants", type=int, default=1)
    ap.add_argument("--grid", type=int, default=32, help="số ô pixel mỗi cạnh: 32 đơn giản, 48/64 chi tiết hơn")
    ap.add_argument("--size", type=int, default=0, help="cỡ PNG xuất ra (mặc định = 2 x grid)")
    ap.add_argument("--out", default=str(ROOT / "dist" / "pixelxl"))
    ap.add_argument("--raw", default="", help="thư mục ảnh gốc 1024px dùng chung (để thử nhiều --grid không phải vẽ lại)")
    ap.add_argument("--apply", action="store_true")
    a = ap.parse_args()
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

    a.size = a.size or a.grid * 2
    out = Path(a.out); raw = Path(a.raw) if a.raw else out / "raw"; raw.mkdir(parents=True, exist_ok=True); out.mkdir(parents=True, exist_ok=True)
    keys = UI_KEYS if a.ui else SAMPLE_KEYS if a.samples else ([k.strip() for k in a.keys.split(",") if k.strip()] or icon_keys())
    for i, k in enumerate(keys, 1):
        subj = SUBJECTS.get(k, k.replace("-", " "))
        for v in range(a.variants):
            name = k if v == 0 else f"{k}_v{v + 1}"
            if (out / f"{name}.png").exists(): continue
            t0 = time.time()
            if (raw / f"{name}.png").exists():  # đã vẽ rồi -> chỉ làm lại hậu kỳ (đổi --grid)
                img = Image.open(raw / f"{name}.png")
            else:
                img = render(subj, 2400 + i * 7 + v * 1000, a)
                img.save(raw / f"{name}.png")
            pixelize(img, a.grid, a.size).save(out / f"{name}.png", optimize=True)
            print(f"[{i}/{len(keys)}] {name} {time.time() - t0:.1f}s")
    sheet(out, keys, out / "_sheet.png")
    print("Ảnh so sánh:", out / "_sheet.png")
    if a.apply:
        apply(out)


if __name__ == "__main__":
    main()
