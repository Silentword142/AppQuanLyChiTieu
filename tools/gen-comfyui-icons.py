#!/usr/bin/env python3
"""
Tự động gọi ComfyUI API (mặc định http://127.0.0.1:8188) để sinh bộ icon 16x16
bằng model MOSS-Image-Base + LoRA Angais/MOSS-Image-PixelArt-16x16.

Ví dụ chạy trên máy đã bật ComfyUI (--listen):
  python3 tools/gen-comfyui-icons.py --ckpt moss_image_base.safetensors --lora moss-image-pixelart-16x16.safetensors
Sau khi sinh xong:
  python3 tools/import-16x16-icons.py ./generated_icons
"""
import argparse, json, os, time, urllib.request

PROMPTS = {
    "home": "cozy wooden cottage house with red roof",
    "home-heart": "cozy house with a red heart sign",
    "chart": "bar chart with colorful bars",
    "pie": "colorful pie chart",
    "trend-up": "green upward arrow chart",
    "trend-down": "red downward arrow chart",
    "tags": "yellow price tag",
    "wallet": "brown leather wallet with gold clasp",
    "credit-card": "blue credit card with gold chip",
    "banknote": "green paper banknote bill",
    "landmark": "classical bank building with columns",
    "receipt": "white paper shopping receipt",
    "calculator": "retro pocket calculator",
    "percent": "red percent symbol",
    "settings": "silver mechanical gear cog",
    "sun": "bright yellow smiling sun",
    "moon": "yellow crescent moon",
    "cloud": "fluffy light blue cloud",
    "bell": "golden bell",
    "chat": "blue speech bubble",
    "sparkles": "golden sparkles",
    "trash": "silver trash bin",
    "pencil": "yellow wooden pencil with pink eraser",
    "scooter": "red motor scooter",
    "bike": "blue bicycle",
    "car": "red compact car",
    "taxi": "yellow taxi cab",
    "bus": "orange city bus",
    "train": "green passenger train",
    "plane": "white passenger airplane with blue wings",
    "ship": "red and white ferry ship",
    "fuel": "red gas fuel pump",
    "parking": "blue parking P sign",
    "ticket": "golden admission ticket",
    "building": "tall office apartment building",
    "droplet": "blue water droplet",
    "wifi": "blue wifi signal icon",
    "tv": "retro television set with antenna",
    "bed": "wooden bed with red blanket",
    "bath": "white bathtub",
    "sofa": "cozy red armchair sofa",
    "fan": "blue desk fan",
    "plug": "electrical power plug",
    "ao-dai": "pink traditional dress",
    "watch": "silver wristwatch",
    "glasses": "blue round eyeglasses",
    "gift": "red gift box with golden ribbon",
    "package": "cardboard shipping box with tape",
    "lipstick": "red lipstick tube",
    "store": "cute shop storefront with striped awning",
    "phone": "modern smartphone",
    "laptop": "silver laptop computer",
    "headphones": "red over-ear headphones",
    "gamepad": "retro game controller",
    "monitor": "desktop computer monitor",
    "printer": "office printer",
    "radio": "vintage wooden radio",
    "cassette": "yellow audio cassette tape",
    "briefcase": "brown leather briefcase",
    "handshake": "two hands shaking",
    "pill": "red and white medicine capsule",
    "stethoscope": "medical stethoscope",
    "dumbbell": "gym dumbbell weight",
    "syringe": "medical syringe",
    "tooth": "white smiling tooth",
    "cross": "white first aid box with red cross",
    "film": "cinema clapperboard",
    "music": "purple eighth musical note",
    "paw": "pink animal paw print",
    "tent": "orange camping tent",
    "mountain": "snow-capped mountain peak",
    "vinyl": "black vinyl record disc",
    "disco": "shiny blue disco ball",
    "graduation": "graduation mortarboard cap",
    "school": "red school building",
    "pagoda": "red asian pagoda temple",
    "camera": "vintage brown and silver camera",
    "ice-cream": "three-scoop ice cream cone",
    "calendar": "red and white desk calendar",
    "star": "bright golden five-pointed star",
    "palm": "tropical palm tree on a tiny island",
    "sprout": "green two-leaf plant sprout in soil",
}

def queue_prompt(url, ckpt, lora, prompt_text):
    full_prompt = f"pixel art 16x16 game icon, {prompt_text}, dark outline, plain magenta background"
    workflow = {
        "3": {"class_type": "KSampler", "inputs": {"cfg": 3.0, "denoise": 1.0, "latent_image": ["5", 0], "model": ["10", 0], "negative": ["7", 0], "positive": ["6", 0], "sampler_name": "euler", "scheduler": "simple", "seed": 42, "steps": 30}},
        "4": {"class_type": "CheckpointLoaderSimple", "inputs": {"ckpt_name": ckpt}},
        "5": {"class_type": "EmptyLatentImage", "inputs": {"batch_size": 1, "height": 512, "width": 512}},
        "6": {"class_type": "CLIPTextEncode", "inputs": {"clip": ["10", 1], "text": full_prompt}},
        "7": {"class_type": "CLIPTextEncode", "inputs": {"clip": ["10", 1], "text": "blurry, gradient, 3d, realistic, text, watermark"}},
        "8": {"class_type": "VAEDecode", "inputs": {"samples": ["3", 0], "vae": ["4", 2]}},
        "9": {"class_type": "SaveImage", "inputs": {"filename_prefix": "moss_icon", "images": ["8", 0]}},
        "10": {"class_type": "LoraLoader", "inputs": {"lora_name": lora, "model": ["4", 0], "clip": ["4", 1], "strength_model": 1.0, "strength_clip": 1.0}},
    }
    data = json.dumps({"prompt": workflow}).encode("utf-8")
    req = urllib.request.Request(f"{url.rstrip('/')}/prompt", data=data, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read())["prompt_id"]

def wait_image(url, prompt_id):
    base = url.rstrip("/")
    while True:
        with urllib.request.urlopen(f"{base}/history/{prompt_id}", timeout=15) as resp:
            hist = json.loads(resp.read())
        if prompt_id in hist:
            outputs = hist[prompt_id].get("outputs", {})
            for node_out in outputs.values():
                imgs = node_out.get("images", [])
                if imgs:
                    info = imgs[0]
                    q = f"filename={info['filename']}&subfolder={info.get('subfolder','')}&type={info.get('type','output')}"
                    with urllib.request.urlopen(f"{base}/view?{q}", timeout=30) as ir:
                        return ir.read()
        time.sleep(0.5)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--url", default="http://127.0.0.1:8188")
    ap.add_argument("--ckpt", required=True, help="Tên file checkpoint trong ComfyUI/models/checkpoints")
    ap.add_argument("--lora", default="moss-image-pixelart-16x16.safetensors")
    ap.add_argument("--out", default="generated_icons")
    args = ap.parse_args()
    os.makedirs(args.out, exist_ok=True)
    for key, desc in PROMPTS.items():
        out_path = os.path.join(args.out, f"{key}.png")
        if os.path.exists(out_path):
            continue
        print(f"Generating {key}: {desc}...")
        pid = queue_prompt(args.url, args.ckpt, args.lora, desc)
        png_bytes = wait_image(args.url, pid)
        open(out_path, "wb").write(png_bytes)
    print("Xong! Chạy tiếp: python3 tools/import-16x16-icons.py", args.out)

if __name__ == "__main__":
    main()
