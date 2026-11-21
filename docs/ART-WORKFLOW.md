# Spendy · Vẽ icon & hoạt ảnh trên Procreate (khung 64×64)

**Mỗi phần tử là 1 file PNG.** Một khung có cỡ **64×64**. Hoạt ảnh thì đặt **các khung cạnh nhau theo chiều ngang**.
Anh vẽ xong, lưu lại và gửi cho Claude; Claude chạy 1 lệnh để đưa vào app.

```
art/
├── procreate/<tên>.png     ← 188 file: 173 icon + 15 UI element (dock, nút arcade, mây)
├── elements.txt            ← cỡ khung + nhịp hoạt ảnh của từng phần tử
├── guide/                  ← lưới 64 cho Procreate, 3 bảng màu .swatches
├── Spendy-Procreate-64.zip ← tất cả những thứ trên trong 1 file để chép sang iPad
└── out/                    ← ảnh xem trước (tự sinh, không commit)
```

## 1. Cỡ khung

| Loại | 1 khung | Ví dụ hoạt ảnh 8 khung |
|---|---|---|
| Icon (173 cái) | 64 × 64 | 512 × 64 |
| `ui-dock-*-cap/well` | 58 × 54 | 464 × 54 |
| `ui-dock-ico-*` | 30 × 30 | 240 × 30 |
| `ui-arcade-ring` / `ui-arcade-dome` | 34 × 34 / 26 × 26 | |
| `ui-sky-cloud` | 56 × 20 | |

Cỡ chính xác của từng phần tử ghi trong `art/elements.txt`.

**Tự phóng to mà không vỡ nét pixel:**
- File mẫu được làm từ hình đang có trong app, phóng ×2 bằng nearest-neighbour. Mỗi pixel cũ thành một khối 2×2, nên mở ra là y hệt app. Anh vẽ thêm chi tiết đến từng pixel nhỏ của khung 64.
- Canvas Procreate phóng to theo bội số nguyên (128, 256, 512 px mỗi khung) cũng được. Khi nhập, mỗi khối được lấy đúng điểm giữa, nên viền nhoè của cọ bị loại bỏ.
- Anh vẫn có thể vẽ ở 32×32; ảnh sẽ được tự phóng lên 64.
- Phần nào anh chưa vẽ thêm chi tiết (vẫn là các khối 2×2) sẽ được lưu ở bản nhỏ cho app nhẹ. Hiển thị không khác gì.
- Trong app, ảnh luôn được vẽ bằng `image-rendering: pixelated`, nên nét pixel giữ nguyên ở mọi cỡ.

## 2. Cài Procreate (làm 1 lần)

1. Giải nén `art/Spendy-Procreate-64.zip` trong app Tệp trên iPad.
2. Chạm vào 3 file `guide/Spendy-*.swatches` để nhập bảng màu. Mỗi bảng có 3 hàng: bóng, màu gốc, sáng.
3. **Cọ:** *Airbrushing → Hard Airbrush*, cỡ 1 px, độ mờ 100 %, Streamline 0.
4. **Lưới:** *Actions → Canvas → Drawing Guide → 2D Grid*, **Grid Size 64 px**: mỗi ô lưới là một khung.
5. Khi dịch hoặc xoay hình, chọn **Nearest Neighbor** trong công cụ Transform.

## 3. Vẽ một icon

1. *Gallery → Import* rồi chọn `procreate/<tên>.png`. Canvas tự đúng 64×64.
2. (Tuỳ chọn) Kéo `guide/grid-64.png` vào làm layer trên cùng, chế độ Multiply, độ mờ khoảng 30 %. Lớp này có:
   - khung 64 (tím);
   - lưới mỗi 8 px;
   - vùng vẽ 6…57 (hồng);
   - vòng tròn Ø50 (xanh);
   - đường chân đế y = 58 (xanh lá).
3. Vẽ xong, ẩn lớp guide, *Share → PNG*, giữ nguyên tên file.

**Luật để cả bộ đều nhau:**
- Hình chính nằm trong vùng 6…57.
- Viền 1–2 px, liền nét. Dùng màu tối nhất (hàng 1) của mảng đó, hoặc màu mực `#3B2A40`.
- Ánh sáng chiếu từ **trên-trái**: bóng phía dưới-phải, mảng sáng và 1–3 điểm trắng bóng loáng phía trên-trái.
- Mỗi icon có khoảng 4 mảng màu chính, mỗi mảng 3 sắc độ. Không tô chuyển màu, không dùng cọ mềm.
- Pixel có độ trong suốt dưới 50 % sẽ bị xoá, trên 50 % sẽ thành đặc.

## 4. Vẽ hoạt ảnh: các khung cạnh nhau

1. Mở file của phần tử, rồi *Actions → Canvas → Crop & Resize*. Tăng **chiều rộng** lên số khung × 64 (8 khung → 512), neo canvas ở bên trái.
2. Nhân đôi layer khung 1 rồi dịch sang phải 64 px (Transform, bật Snapping) để làm khung tiếp theo. Giảm độ mờ khung trước còn 30 % để làm onion skin.
3. Có thể dùng `guide/grid-64x8-anim.png` làm lớp lưới.
4. *Share → PNG*, giữ nguyên tên.

> Nếu anh quen **Animation Assist** (mỗi layer một khung), cứ *Share → Animated PNG / GIF* với tên `<tên>.png`/`.gif`.
> Khi nhập, các khung sẽ tự được ghép cạnh nhau.

Nhịp chạy khai báo trong `elements.txt` (anh tự sửa được):

```
# tên    rộng cao  fps  kiểu  giữ-khung (khung 1, 2, 3… đứng bao nhiêu nhịp)
bell     64   64   14   loop  6 1 1 1 1 1 10
```

- **kiểu:** `loop` (lặp mãi) · `once` (chạy 1 lần) · `pingpong` (đi rồi về).
- **giữ-khung:** đây là cách tạo easing cho pixel art. Khung giữ lâu tạo cảm giác chậm hoặc dừng, khung 1 nhịp tạo cảm giác nhanh. Không cần vẽ khung trùng.
- Phần tử có nhiều khung sẽ tự chạy hoạt ảnh trong app:
  - icon có `anim`, ví dụ linh vật Mai;
  - UI element như nút dock, nút arcade, mây.
  
  Hình anh tự đặt trong Studio vẫn được ưu tiên. Người dùng bật *Giảm chuyển động* chỉ thấy khung 1.

### Guideline chuyển động

**Nhịp chuẩn:** 10–12 fps.

Hoạt ảnh lặp kiểu idle có cấu trúc:

```
NGHỈ (≥ 0.5s) → LẤY ĐÀ (1) → HÀNH ĐỘNG (2–4) → VƯỢT ĐÍCH (1) → ỔN ĐỊNH (1) → NGHỈ
```

Tổng 1.5–3.5 giây. Khung nghỉ dài đặt ở cuối.

| Kiểu | Mẫu có sẵn | Công thức |
|---|---|---|
| Nhảy | home, wallet | đứng ×6 · nén 108/90 % · vươn lên 4px · đỉnh ×2 · rơi · chạm đất nén · hồi ×8 |
| Nảy | chart | đứng ×6 · nén · vươn · phồng nhẹ · hồi · đứng ×10 |
| Lắc | bell | treo ở đỉnh: −10° · +8° · −6° · +4° · −2° · đứng ×10 |
| Đập | heart | 100 % ×3 · 114 % · 96 % · 108 % · 100 % ×6 |
| Lấp lánh | sparkles | sáng 25/50 % · phóng 110 % · thu 92 % · về |
| Vệt sáng | coins | vệt chéo trắng quét qua 8 khung · nghỉ ×10 |
| Chớp mắt | mai | mở ×8 · nhắm 1 · mở ×6 · nhún 2px · về |

Các mẫu này được sinh tự động từ icon để anh có điểm bắt đầu; anh vẽ đè lên cho đẹp hơn.

**Luật chung:**
1. **Khung 1 = hình tĩnh** của phần tử. Với hoạt ảnh lặp, khung cuối phải nối mượt về khung 1.
2. **Chân đế cố định** ở y = 58. Nhảy cao tối đa 8 px; nén/giãn trong khoảng ±10 %, nén ngang thì giãn dọc.
3. **Xoay quanh điểm treo**, tối đa 10°. Vẽ lại viền sau khi xoay để viền vẫn liền.
4. **Ánh sáng không đổi** (trên-trái) ở mọi khung.
5. **Không vẽ ra ngoài khung 64**: phần đó sẽ bị cắt.
6. Tối đa khoảng 12 khung khác nhau. Kéo dài thời gian bằng số nhịp giữ khung.
7. Không nhấp nháy quá 3 lần mỗi giây.

## 5. Đưa lên app: nhờ Claude, tốn ít usage nhất

1. Gom các file PNG đã vẽ vào **một file zip** (chọn nhiều file trong app Tệp, chọn *Nén*). Đặt tên file nào cũng được.
2. Đính kèm zip vào chat, hoặc tải lên GitHub vào `art/procreate/`.
3. Nhắn một câu: **"Đẩy art lên app + xuất APK"**.

Claude chỉ chạy:

```bash
python3 tools/art.py file.zip    # nhập → đưa về khung chuẩn → ghi web/icons.js → art/out/preview.png
```

Lệnh in cảnh báo khi:
- sai cỡ;
- số khung không khớp số nhịp giữ;
- khung 1 trống;
- có tên mới.

Claude chỉ đọc phần cảnh báo, không cần mở ảnh. Anh nên gom nhiều thay đổi vào một lần gửi.

| Lệnh khác | Tác dụng |
|---|---|
| `python3 tools/art.py check` | chỉ kiểm tra |
| `python3 tools/art.py template [--force]` | tạo lại file mẫu từ hình trong app (`--force` ghi đè cả file anh đã vẽ) |
| `python3 tools/art.py starter [tên…]` | sinh lại hoạt ảnh mẫu |
| `python3 tools/art.py pack` | đóng gói lại `art/Spendy-Procreate-64.zip` |

**Thêm icon mới:** đặt file `tên-mới.png` (64×64) vào zip. Dòng trong `elements.txt` sẽ được thêm tự động.
Muốn icon hiện trong bộ chọn hạng mục thì nhờ Claude thêm tên vào `GROUPS` trong `web/icons.js`.

`tools/icons32/apply_patch.py` và Studio trong app vẫn dùng được như trước. `art/procreate` mới là nguồn chính khi build lại toàn bộ.
