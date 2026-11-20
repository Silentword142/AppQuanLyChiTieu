# VinaSpends – Phân tích UX/UI & các lần thiết kế lại

## 0c. Bộ icon 32×32 (5.2)

- 173 icon màu vẽ lại ở lưới **32×32** (trước là 16×16) bằng bộ dựng `tools/icons32/` (`engine.py` + `defs_*.py`): mỗi icon là tập khối hình (cầu, trụ, hộp, đa giác, nét) gắn chất liệu; bộ dựng tự tạo dải 6 tông có lệch sắc (bóng ngả tím, sáng ngả vàng), tô khối theo ánh sáng trên-trái, viền trong theo màu khối, viền ngoài màu tối của vật liệu, điểm bóng loáng.
- Sửa/thêm icon: sửa `tools/icons32/defs_*.py` rồi chạy `python3 tools/icons32/build.py --sheet /tmp/xem.png` để xem và `--inject` để nhúng PNG vào `web/icons.js`.
- Icon nét đơn sắc (mũi tên, +, ✕, ‹ ›…) vẫn là SVG 16×16 theo màu chữ.

## 0b. Bản 5.1 – "Spendy"

- **Tên app: Spendy** (tên hiển thị trên web, launcher Android, trang đăng nhập Google, file sao lưu `spendy_sao_luu_*.json`). Không đổi package Android, khoá `localStorage` hay collection Firebase nên dữ liệu cũ giữ nguyên. Trợ lý vẫn tên Mai.
- **Icon app mới**: đồng xu vàng pixel khắc "S" trên ô hồng bo góc pixel (`tools/gen-app-icon.py`, lưới 32×32, xuất 512 px + 5 mật độ Android).
- **Chữ to hơn**: giữ font Handjet / VT323 / Tiny5; chữ thường 18 px, chữ nhỏ 14 px, nhãn pixel 12 px, tiêu đề và số tiền lớn theo. Khoảng cách bố cục (rem) giữ nguyên.
- **Khung pixel toàn bộ**: mọi cửa sổ, thẻ, nút, phím, ô nhập, HUD, TV đều bo góc bậc thang (2–3 nấc) bằng `clip-path` + điểm ảnh góc vẽ lại qua `::after`; nút nổi có gờ đáy dày 2 điểm, bấm thì lún 1 điểm. Nút arcade "GHI" và núm xoay TV là hình tròn pixel vẽ bằng SVG. Các phần tử bo tròn còn lại dùng góc khuyết 1 điểm.
- **Icon đẹp hơn**: bộ dựng icon tự tô bóng kiểu pixel art (viền trong theo màu vùng, mép sáng trên-trái, mép tối dưới-phải, bóng đổ 1 điểm); vẽ lại xe máy, áo dài, em bé, chó, yoga.

## 0. Bản 5.0 – "Pastel Dusk Pixel": màn hình chính TV retro + nút bấm điện tử

Lấy cảm hứng từ phong cách đồ hoạ game pixel pastel (trời hồng hoàng hôn, khung cửa sổ viền mận, giấy kem, nút vuông nổi khối), nhưng toàn bộ hình vẽ, icon và bố cục đều là thiết kế riêng của VinaSpends.

### Bố cục mới (mobile)

| Vùng | Trước | Sau |
|---|---|---|
| Đầu trang | Logo + ngày + pill đồng bộ + bánh răng | **HUD** kiểu game: ô *tài sản ròng* (số chạy, nút ⊕ ghi nhanh) · ô *Mai chờ duyệt* · ô *LOCAL/CLOUD/LIVE*; hàng dưới là **thanh ngân sách tháng** (bấm để đặt hạn mức); bên phải 2 nút vuông *Cài đặt* và *Hạng mục* |
| Màn hình chính | Thẻ tài sản + thống kê + danh sách ví + 2 nút | **TV CRT retro**: màn hình 4 kênh (Tài sản · Tháng này + biểu đồ chi từng ngày · Ngân sách · Ví & thẻ), đổi kênh bằng núm xoay / CH▲▼ / vuốt ngang / chấm kênh; **hàng phím bấm điện tử có đèn LED**: CHI · THU · NÓI (nếu có giọng nói) · QUÉT hoá đơn · HẠN MỨC. Bên dưới là cửa sổ **Nhật ký gần đây** nhóm theo ngày |
| Thanh điều hướng | 5 tab chữ + icon, "Tự động" ở giữa | **Dock 4 phím vuông màu** (Nhà · Báo cáo · Tự động · Ví & Nợ) + **nút arcade tròn "GHI"** ở giữa – hành động dùng nhiều nhất luôn trong tầm ngón cái |
| Hạng mục | Một tab chính | Chuyển lên nút HUD + nút trong màn Báo cáo (ít dùng hằng ngày) |
| Báo cáo | Khối tiêu đề lớn + thanh Chi/Thu tách rời; biểu đồ 30 ngày phải cuộn ngang | Một cửa sổ gọn: phím Chi tiêu/Thu nhập + "Xem theo nhóm lớn/mục nhỏ"; biểu đồ tháng vừa khung, nhãn ngày thưa |
| Thẻ ví | Màu đậm theo thương hiệu ngân hàng | Bản pastel của màu thương hiệu (trộn kem sữa), viền mực, chấm bi pixel |

### Hệ thiết kế

- **6 bảng màu**: Hoàng Hôn Hồng (mặc định), Soda Bạc Hà, Mộng Oải Hương, Kẹo Bầu Trời, Đào Bơ Sữa, Đêm Mận (tối). Theme cũ tự chuyển sang theme mới tương ứng.
- **Khung cửa sổ**: viền mực + vành hồng mận + viền mực (đổ bóng khối), thanh tiêu đề màu mận, nút ✕ đỏ.
- **Nút bấm điện tử** (`.ekey`, `.dock-face`, `.arcade`): mặt phím sáng, cạnh dưới tối, lún xuống khi bấm rồi bật lại kiểu lò xo; đèn LED báo trạng thái; rung nhẹ 8–14ms trên Android.
- **TV CRT** (`RetroTV`): ăng-ten pixel lắc nhẹ, kính cong + vệt phản sáng, scanline, dải quét chạy dọc, nhấp nháy rất nhẹ, hiệu ứng *bật nguồn* (vạch sáng nở ra), *đổi kênh* (nhiễu sóng + lệch hình), chữ phát sáng kiểu phosphor, OSD "CH01 · giờ".
- **Bộ icon mới hoàn toàn** (`web/icons.js`): 203 icon pixel 16×16 vẽ tay dạng lưới ký tự, tông pastel + viền mận đổi theo theme (`--icon-ink`); icon nét đơn (mũi tên, dấu +, ✕…) dùng `currentColor`. Linh vật **Mai** giờ là chiếc TV nhỏ mặt cười. Icon ứng dụng mới sinh bằng `tools/gen-app-icon.py`.

### Chuyển động

- Chuyển màn trượt theo hướng tab + các khối con nổi lên lần lượt (stagger).
- Hộp thoại: nền mờ dần + blur, sheet trượt lên kiểu lò xo; **đóng cũng có hiệu ứng** (component `Presence` giữ lại 260ms).
- Số tiền chạy (`CountUp`), cột biểu đồ mọc lên, ô ngân sách lần lượt sáng, toast nảy xuống kèm vạch đếm ngược.
- Mây pixel trôi chậm trên nền trời, sao lấp lánh ở theme tối.
- Tôn trọng `prefers-reduced-motion`: tắt mọi chuyển động trang trí.

---

# VinaSpends 2.0 – Phân tích UX/UI & thiết kế lại "Vintage 70s" cùng Mai

## 1. Vấn đề của bản cũ

| # | Vấn đề | Ảnh hưởng | Cách sửa |
|---|---|---|---|
| 1 | Header 4–5 nút icon, nút "Đồng bộ/Live" luôn màu xanh kể cả khi chưa đăng nhập | Rối, báo sai trạng thái | Còn 3 nút; pill đồng bộ hiện đúng `Offline / Đã lưu / Live` |
| 2 | Huy hiệu "AES-256 MÃ HOÁ" trên thẻ tài sản | Cam kết bảo mật không có thật | Bỏ |
| 3 | Màn Tổng quan không có số liệu **tháng này** | Phải sang Báo cáo mới biết đã tiêu bao nhiêu | Thẻ "vé" mới: Thu / Chi / Còn lại của tháng + thanh ngân sách có vạch "hôm nay" |
| 4 | Giao dịch gần đây là danh sách phẳng, nút sửa/xoá chỉ hiện khi rê chuột | Khó đọc; trên điện thoại không bấm được | Nhóm theo ngày (Hôm nay / Hôm qua…) kèm tổng ngày; chạm cả dòng để sửa/xoá |
| 5 | Chọn Chi/Thu chỉ gạch chân | Không rõ đang chọn gì | Nút tô màu đặc (đỏ gạch / xanh bơ) |
| 6 | Form thêm giao dịch tự chọn sẵn hạng mục đầu tiên ("Ăn sáng") | Ghi sai hạng mục âm thầm | Bắt buộc chọn; thêm ô **Ngày** để ghi bù |
| 7 | "Trợ lý AI" nằm sâu trong tab Ví, cần Gemini API key + mạng | Ít ai dùng, không chạy offline | Mai thành tab chính giữa thanh điều hướng, chạy offline |
| 8 | Class `dark:` ăn theo chế độ tối của **điện thoại** chứ không theo theme của app | Chữ nhạt trên nền sáng khi điện thoại bật Dark mode | `darkMode: 'class'`, app tự gắn class theo theme |
| 9 | Màu theme dạng `var(--x)` nên mọi lớp `/10`, `/20` (nền nhạt) không có tác dụng | Thẻ, chip mất màu nền | Biến màu dạng RGB + `<alpha-value>` |
| 10 | Chữ 9–10px ở nhiều chỗ, toast nhảy (bounce) | Khó đọc, gây phân tâm | Tối thiểu 11px, toast trượt nhẹ |
| 11 | Toàn bộ thư viện tải từ CDN, JSX dịch trên máy mỗi lần mở | Không có mạng là trắng màn hình; mở chậm | Bản APK đóng gói sẵn mọi thứ, JSX/Tailwind biên dịch trước |

## 2. Ngôn ngữ thiết kế Vintage 70s

- **Bảng màu**: kem giấy `#F2E6CC`, cam cháy `#C8551B`, mù tạt `#E0A526`, nâu mực `#3A2618`, xanh bơ `#5E7A2B`, gạch `#B23A1F`.
- **Chữ**: tiêu đề & số tiền dùng *Fraunces* (serif mềm, trục SOFT/WONK – hơi hướng Cooper Black), nội dung dùng *Be Vietnam Pro*. Cả hai hỗ trợ đầy đủ tiếng Việt và được nhúng sẵn trong app.
- **Hình khối**: viền mực 2px + bóng đổ cứng 4px (kiểu in lụa); nút nhấn lún xuống khi bấm.
- **Hoạ tiết**: dải sọc cầu vồng 4 màu, tia nắng (sunburst) và vòng cung đồng tâm trên thẻ tài sản, nền hạt giấy in.
- **3 theme**: Kem & Cam cháy (mặc định), Xanh bơ & Mù tạt, Đêm Disco (tối). Các theme cũ vẫn giữ.

## 2b. Bản 2.1

- **Theme**: bỏ toàn bộ 8 theme cũ, còn 6 bảng màu vintage: Cam cháy, Xanh bơ, Hồng đất, Xanh ngọc, Đêm Disco, Đêm Nhung. Màu hạng mục và mọi màu xanh/đỏ/xanh dương kiểu Tailwind mặc định đều đổi sang tông vintage; màu hạng mục mặc định trong dữ liệu cũ tự chuyển.
- **Icon**: bộ icon SVG vẽ mới hoàn toàn (`web/icons.js`, ~170 icon, nét 2px bo tròn + tô nhạt kiểu in lụa), có icon riêng cho Việt Nam (xe máy, phin, trà sữa, phở, bánh mì, áo dài, nón lá, chùa, lì xì). Không dùng thư viện icon ngoài. Logo ngân hàng thay bằng tem chữ tự vẽ (VCB, TCB, MB…). Tên icon cũ trong dữ liệu đã lưu được tự đổi sang icon mới.
- **Đồng bộ**: chỉ còn Firebase (email/mật khẩu – chạy cả trong app Android; trình duyệt có thêm Google và nút "Đặt mật khẩu" để dùng cùng tài khoản trên Android) và xuất/nhập file JSON. Đã bỏ: mã PIN, QR, chuỗi chuyển dữ liệu, máy chủ riêng `/api/cloud-sync`, đồng bộ giữa các tab. Cài đặt + giao diện + đồng bộ gộp vào một màn "Cài đặt".
- **Bỏ API ngoài**: Gemini API key, logo từ api.vietqr.io, endpoint máy chủ. `server.js` chỉ còn phục vụ trang tĩnh.

## 3. Mai trong app

`web/mai.js` – bộ đọc thông báo giao dịch chạy 100% offline (rule-based, không gọi mạng).

- Hiểu định dạng thông báo/SMS của Vietcombank, Techcombank, MB, BIDV, VietinBank, Agribank, ACB, TPBank, VPBank, Sacombank, Cake… và ví MoMo, ZaloPay, ShopeePay.
- Lấy ra: số tiền, thu/chi, ngân hàng, 4 số cuối tài khoản, **số dư sau giao dịch**, nội dung, người gửi/nhận, thời gian, hạng mục gợi ý.
- Bỏ qua: OTP, khuyến mãi, đăng nhập, giao dịch thất bại, nhắc nợ thẻ.
- Chống trùng khi cùng 1 giao dịch đến qua cả SMS và app ngân hàng.
- **Học thói quen**: anh sửa hạng mục 1 lần, lần sau gặp cùng nơi bán/người nhận Mai phân loại đúng.
- **Không ghi sai âm thầm**: mỗi kết quả có độ chắc chắn; ≥ 75% thì tự ghi, thấp hơn thì vào "Chờ anh duyệt". Mọi giao dịch Mai ghi đều có nút *Hoàn tác*.
- Gõ nhanh kiểu nói: `cafe 35k`, `đổ xăng 1tr2`, `nhận lương 15tr`.
- Quét ảnh chụp thông báo/hoá đơn: OCR tiếng Việt chạy offline → Mai đọc.

Kiểm thử: `npm test` (75 mẫu: 48 thông báo của các ngân hàng/ví/thẻ – gồm thẻ tín dụng có hạn mức, rút ATM kèm phí, giao dịch ngoại tệ có quy đổi, nạp ví, hoàn tiền –, 10 tin không phải giao dịch – OTP, khuyến mãi, sao kê, nhắc nợ, tin chat có số tiền –, định dạng tiền, câu gõ nhanh, học hạng mục, chống trùng).

## 4. Bản Android (APK)

- `android/` – vỏ Java tối giản: WebView chạy app từ assets (origin `https://appassets.androidplatform.net`, không cần mạng) + `NotificationListenerService` nghe thông báo ngân hàng/ví/SMS.
- Thông báo chỉ được giữ lại nếu có số tiền; được xếp hàng trong máy và Mai xử lý ngay khi app đang mở, hoặc lúc anh mở app.
- Có thể **Chia sẻ** một tin nhắn SMS vào VinaSpends để Mai ghi.
- Build: `npm install && npm run build:apk` (cần `aapt apksigner zipalign android-sdk-platform-23 dalvik-exchange` từ apt). Khoá ký `android/vinaspends.keystore` **không** đưa lên Git; giữ lại file này để các bản sau cài đè được mà không mất dữ liệu.
