# WF08 – Vẽ lại icon bằng Pixel Art XL (nerijs/pixel-art-xl)

Cách làm theo đúng model card của LoRA:

| Mục | Giá trị |
|---|---|
| Base | `sd_xl_base_1.0` (không dùng refiner) |
| VAE | `sdxl_vae_fp16_fix` (VAE đã sửa để tránh nhiễu) |
| LoRA pixel | `pixel-art-xl.safetensors` mức **1.2** (khi có LCM) hoặc 1.0 |
| LCM LoRA | `latent-consistency/lcm-lora-sdxl` mức 1.0 · **8 bước · CFG 1.5** · sampler `lcm` |
| Prompt | `pixel art, <vật>, simple icon, flat colors, bold dark outline, ...` (không cần trigger word) |
| Negative | `3d render, realistic, ...` |
| Thu nhỏ | Nearest Neighbor, lấy màu đa số mỗi ô → lưới 32×32, ép bảng màu app, phóng x2 = 64×64 |

So với WF07: chỉ một LoRA pixel (WF07 trộn pixel-art-xl 0.65 với 2 LoRA khác nên lưới điểm lệch,
mỗi icon ~500 màu), chỉ dùng node gốc của ComfyUI, hậu kỳ ép cả bộ về chung một bảng màu.

## Chạy (trên máy có ComfyUI + GPU)

1. Tải LCM LoRA: `latent-consistency/lcm-lora-sdxl` → đổi tên `lcm-lora-sdxl.safetensors`, bỏ vào `ComfyUI/models/loras/`.
   (Không muốn tải thì thêm `--no-lcm`, chậm hơn ~3 lần.)
2. `pip install pillow`
3. Mẫu: `python tools/pixelxl/gen_pixelxl.py --samples --variants 3` → xem `dist/pixelxl/_sheet.png`
4. Toàn bộ: `python tools/pixelxl/gen_pixelxl.py`
5. Nạp vào app: `python tools/pixelxl/gen_pixelxl.py --apply` rồi `npm run build:apk`

Muốn chọn bản khác cho một icon: đổi tên `dist/pixelxl/<tên>_v2.png` thành `<tên>.png` rồi `--apply`.
Nút/khung UI (`ui-*`) và logo Google không vẽ bằng AI: UI dùng bản pixel vẽ bằng code trong `web/index.html`.
