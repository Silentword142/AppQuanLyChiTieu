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

Kiểm thử: `npm test` (53 mẫu: 30 thông báo thật của các ngân hàng/ví, 6 tin không phải giao dịch, định dạng tiền, câu gõ nhanh, học hạng mục, chống trùng).

## 4. Bản Android (APK)

- `android/` – vỏ Java tối giản: WebView chạy app từ assets (origin `https://appassets.androidplatform.net`, không cần mạng) + `NotificationListenerService` nghe thông báo ngân hàng/ví/SMS.
- Thông báo chỉ được giữ lại nếu có số tiền; được xếp hàng trong máy và Mai xử lý ngay khi app đang mở, hoặc lúc anh mở app.
- Có thể **Chia sẻ** một tin nhắn SMS vào VinaSpends để Mai ghi.
- Build: `npm install && npm run build:apk` (cần `aapt apksigner zipalign android-sdk-platform-23 dalvik-exchange` từ apt). Khoá ký `android/vinaspends.keystore` **không** đưa lên Git; giữ lại file này để các bản sau cài đè được mà không mất dữ liệu.
