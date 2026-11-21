# Spendy · Quy trình vẽ icon & hoạt ảnh trên Procreate

Toàn bộ hình vẽ của app nằm trong **2 ảnh**. Anh vẽ trên Procreate, xuất PNG, chép đè vào `art/`,
rồi chạy **1 lệnh**. Không cần đụng vào code.

```
art/
├── icons.png      ← MỌI icon 32×32, 16 ô / hàng        (anh vẽ)
├── icons.txt      ← tên từng ô, mỗi dòng = 1 hàng        (anh sửa khi thêm icon)
├── anims.png      ← MỌI hoạt ảnh, mỗi hàng 1 hoạt ảnh, tối đa 12 khung   (anh vẽ)
├── anims.txt      ← tên · fps · kiểu lặp · số nhịp giữ từng khung        (anh sửa)
├── anims/         ← (tuỳ chọn) <tên>.gif / <tên>.png động xuất từ Animation Assist
├── guide/         ← lớp lưới, bản đồ tên ô, bảng màu .swatches cho Procreate
└── out/           ← ảnh xem trước do lệnh build sinh ra (không commit)
```

```bash
python3 tools/art.py          # cắt ảnh → kiểm tra → nhúng vào web/icons.js → art/out/*
python3 tools/art.py check    # chỉ kiểm tra
```

---

## 1. Chuẩn bị Procreate (làm 1 lần)

| Việc | Cách làm |
|---|---|
| Bảng màu | AirDrop/Files 3 file `art/guide/Spendy-*.swatches` sang iPad, chạm vào là Procreate nhập. Mỗi bảng có 10 màu × 3 hàng: **hàng 1 bóng · hàng 2 màu gốc · hàng 3 sáng**. |
| Cọ pixel | Dùng *Airbrushing → Hard Airbrush*, cỡ **1 px**, độ mờ 100 %, Streamline 0. Tẩy dùng cùng cọ. |
| Lưới | *Actions → Canvas → Drawing Guide → 2D Grid*, **Grid Size 32 px**. Tắt Drawing Assist. |
| Dịch/xoay | Trong công cụ Transform, chọn **Nearest Neighbor**, nếu không điểm ảnh sẽ bị nhoè. |

## 2. Vẽ / sửa icon

1. Mở `art/icons.png` trong Procreate (*Gallery → Import*). Canvas tự đúng cỡ 512 × (số hàng × 32) px.
2. Kéo `art/guide/icons_guide.png` vào làm **layer trên cùng**, chế độ Multiply, độ mờ khoảng 30 %. Lớp này có:
   khung ô (tím), vùng vẽ 3…28 (hồng), vòng tròn Ø25 (xanh), chấm chân đế y = 29 (xanh lá).
3. Mở `art/guide/icons_map.png` bên cạnh (Split View) để biết ô nào là icon nào.
4. Vẽ, rồi **ẩn lớp guide** và lớp nền, sau đó *Share → PNG*. Chép đè lên `art/icons.png`.

> Canvas phóng to 2×, 4×, 8× (lưới 64/128/256 px) cũng được. Lệnh build tự nhận hệ số phóng.

### Luật vẽ icon (để cả bộ đều nhau)

- **Khung hình:** hình chính nằm trong vùng 3…28. Hình tròn Ø25, hình vuông 24, ngang 26×20, đứng 20×26.
  Viền ngoài có thể chạm 1…30, không vượt ra ngoài.
- **Viền:** 1 px, liền nét. Dùng màu tối nhất (hàng 1) của chính mảng màu đó, hoặc màu mực `#3B2A40` cho nét chi tiết.
- **Ánh sáng:** luôn chiếu từ **trên-trái**. Bóng đổ phía dưới-phải bằng màu hàng 1, mảng sáng phía trên-trái bằng màu hàng 3,
  thêm 1–3 điểm trắng bóng loáng.
- **Màu:** tối đa khoảng 4 mảng màu chính mỗi icon, mỗi mảng 3 sắc độ. Không tô chuyển màu, không dùng cọ mềm.
- **Không có pixel nửa trong suốt.** Điểm nào mờ dưới 50 % sẽ bị xoá, trên 50 % sẽ thành đặc.

### Thêm icon mới

1. Ghi tên vào một ô `-` trống trong `art/icons.txt`, hoặc thêm một dòng mới. Tên viết thường, nối bằng dấu gạch (`bubble-tea`).
2. Vẽ vào đúng ô đó trên `icons.png`. Hàng mới thì nới canvas cao thêm 32 px (*Actions → Canvas → Crop & Resize*).
3. Chạy `python3 tools/art.py guides` để vẽ lại bản đồ tên ô.
4. Để icon mới hiện trong bộ chọn icon của app, cần thêm tên vào nhóm `GROUPS` trong `web/icons.js` (1 dòng). Nhờ Claude làm việc này.

---

## 3. Vẽ hoạt ảnh — tất cả trên một ảnh

`art/anims.png` rộng 384 px (12 ô), mỗi **hàng là một hoạt ảnh**, khung đọc từ trái sang phải.
Ô trống ở cuối hàng sẽ được bỏ qua. Ở `art/anims.txt`, mỗi dòng tương ứng một hàng của ảnh:

```
# tên      fps  kiểu     giữ khung
bell       14   loop     6 1 1 1 1 1 1 10
```

- **tên:** trùng tên icon thì icon đó tự chạy hoạt ảnh khi được bấm hoặc chọn (nút dock, Mai…).
  Tên khác thì code gọi bằng `VinaIcons.anim("tên")`.
- **fps:** số nhịp mỗi giây, thường là 12.
- **kiểu:** `loop` (lặp mãi) · `once` (chạy một lần rồi về khung 1) · `pingpong` (chạy đi rồi chạy về).
- **giữ khung:** khung thứ *i* đứng yên bao nhiêu nhịp. Đây là cách tạo **easing** cho pixel art:
  khung giữ lâu tạo cảm giác chậm hoặc dừng, khung 1 nhịp tạo cảm giác nhanh.
  Nhờ vậy không cần vẽ nhiều khung trùng nhau.

Hai cách vẽ trong Procreate:

**A. Vẽ thẳng trên ảnh chung (khuyên dùng).** Mở `art/anims.png`, đặt `art/guide/anims_guide.png` làm lớp lưới,
mở `anims_map.png` bên cạnh để xem số khung và số nhịp giữ. Bật **Onion skin thủ công** bằng cách nhân đôi
layer, giảm độ mờ còn 30 % và dịch sang ô bên cạnh (Transform, bật Snapping, dịch đúng 32 px).

**B. Animation Assist.** Tạo canvas 32×32 (hoặc 256×256), mỗi layer một khung, có onion skin sẵn.
*Share → Animated PNG* (hoặc GIF), lưu thành `art/anims/<tên>.png`. File riêng này **được ưu tiên** hơn
hàng tương ứng trong `anims.png`. Nếu dòng trong `anims.txt` để trống phần giữ khung, nhịp sẽ lấy theo
thời lượng từng khung trong file.

### Guideline chuyển động

**Nhịp chuẩn:** 12 fps, tức 1 nhịp khoảng 83 ms.

Hoạt ảnh lặp kiểu *idle* có cấu trúc:

```
NGHỈ (≥ 0.5s) → LẤY ĐÀ (1 khung) → HÀNH ĐỘNG (2–4 khung) → VƯỢT ĐÍCH (1) → ỔN ĐỊNH (1) → NGHỈ
```

Tổng thời lượng 1.5–3.5 giây để không gây rối mắt. Khung nghỉ dài đặt ở **cuối** hàng.

| Kiểu | Dùng cho | Công thức khung (ví dụ có sẵn trong `anims.png`) |
|---|---|---|
| **Nhảy** (hop) | home, wallet | đứng ×6 · nén 108/90 % · vươn 94/108 % lên 2px · đỉnh 4px ×2 · rơi 2px · chạm đất nén · hồi ×8 |
| **Nảy** (pop) | chart | đứng ×6 · nén · vươn · phồng 106/110 % · hồi 96 % · đứng ×10 |
| **Lắc** (swing) | bell | treo tại đỉnh (16,3) · −10° · +8° · −6° · +4° · −2° · 0° ×10 (biên độ giảm dần) |
| **Đập** (beat) | heart | 100 % ×3 · 114 % · 96 % · 108 % · 100 % ×6 (nhịp "thình–thịch") |
| **Lấp lánh** (twinkle) | sparkles | sáng lên 25/50 % · phóng 110 % · thu 92 % · về |
| **Vệt sáng** (sheen) | coins | vệt chéo trắng 3px quét từ trái-trên sang phải-dưới, 8 khung, rồi nghỉ ×10 |
| **Chớp mắt** (blink) | mai | mở ×8 · nhắm 1 · mở ×6 · nhún 1px ×2 · về · nhún · về |

**Luật chung**

1. **Khung 1 = icon tĩnh**, giống y hệt ô trong `icons.png`, để lúc chuyển giữa tĩnh và động không bị giật.
   Với `loop`, khung cuối phải nối mượt về khung 1.
2. **Chân đế cố định** tại y = 29 với các chuyển động nhảy. Nhảy cao tối đa 4 px; nén/giãn trong khoảng ±10 %.
   Khi nén ngang thì giãn dọc, giữ diện tích gần như không đổi.
3. **Xoay quanh điểm treo** (chuông: đỉnh, biển hiệu: móc treo), tối đa 10°. Vẽ lại viền sau khi xoay để viền vẫn liền 1 px.
4. **Ánh sáng không đổi** (trên-trái) ở mọi khung. Vệt bóng không được "đi theo" vật xoay.
5. **Không vượt khỏi ô 32×32**: phần nằm ngoài ô sẽ bị cắt.
6. **Tối đa 12 khung khác nhau.** Kéo dài thời gian bằng số nhịp giữ khung, không vẽ khung trùng.
7. Không nhấp nháy quá 3 lần mỗi giây. Người dùng bật *Giảm chuyển động* sẽ chỉ thấy khung 1 (app tự xử lý).

### Chuyển động giao diện (CSS, trong `web/index.html`)

| Token | Giá trị | Dùng cho |
|---|---|---|
| `--ease-out` | `cubic-bezier(.16,1,.3,1)` | vào màn, chuyển tab (460 ms), thẻ trồi lên (500 ms) |
| `--ease-spring` | `cubic-bezier(.34,1.56,.64,1)` | nút bấm, icon nảy khi hover/chạm (300 ms) |
| `--ease-soft` | `cubic-bezier(.45,0,.2,1)` | chuyển động lặp chậm: mây, đèn LED, ăng-ten |

Nguyên tắc: phần tử **vào** dùng ease-out từ 300 đến 500 ms, phần tử **ra** nhanh hơn khoảng 30 %.
Nút bấm nhấn xuống tức thì và bật lên bằng spring. Hoạt ảnh pixel dùng `steps()`, không dùng nội suy mượt.

---

## 4. Nhờ Claude sửa mà tốn ít usage nhất

Claude chỉ cần chạy một lệnh, không phải vẽ lại hay đọc code:

1. **Gửi file, không gửi ảnh chụp màn hình.** Tải `icons.png` / `anims.png` (và `.txt` nếu có sửa) lên
   nhánh GitHub vào thư mục `art/`, hoặc đính kèm vào chat.
2. **Một câu yêu cầu, nói rõ phạm vi**, ví dụ:
   - "Build art + xuất APK" → Claude chạy `python3 tools/art.py`, test, build APK, push.
   - "Thêm icon `bubble-tea` vào nhóm Ăn uống" → sửa 1 dòng `GROUPS`.
   - "Hoạt ảnh mẫu cho `gift`, kiểu hop" → thêm 1 dòng vào `STARTERS` trong `tools/art.py`, chạy `starter gift`.
3. **Gom nhiều thay đổi vào một lần** thay vì gửi từng icon.
4. Trong `art/icons.txt` và `art/anims.txt`, anh **tự sửa được**: tên, fps, số nhịp giữ, thứ tự hàng.
   Chỉ cần chạy lại build.

Lệnh build in ra các cảnh báo: ô trống, hình quá nhỏ, quá nhiều màu (dấu hiệu cọ bị nhoè), số khung không khớp số nhịp giữ.
Claude đọc phần này để biết cần sửa gì mà không phải mở ảnh.

## 5. Các lệnh khác

| Lệnh | Tác dụng |
|---|---|
| `python3 tools/art.py export` | Xuất lại `icons.png` từ `web/icons.js` (chỉ dùng để khởi tạo; **ghi đè** ảnh anh đã vẽ) |
| `python3 tools/art.py starter [tên…]` | Tự sinh hoạt ảnh mẫu (nhảy, lắc, đập…) để làm điểm bắt đầu; chỉ ghi đè những hàng được liệt kê |
| `python3 tools/art.py vector tên…` | Vẽ icon từ bản vector (`tools/icons32/vec_*.py`) vào ô của nó trên `icons.png` |
| `python3 tools/art.py guides` | Vẽ lại `art/guide/*` sau khi thêm hàng hoặc ô |

> Từ giờ `art/icons.png` là nguồn chính. **Không** chạy `tools/icons32/vector.py --inject` nữa,
> vì lệnh đó ghi thẳng vào `icons.js` và bỏ qua ảnh anh đã vẽ. Hãy dùng `art.py vector` thay thế.
