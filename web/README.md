# VinaSpends - Web Application Edition

Ứng dụng quản lý tài chính thông minh, phân tích biến động số dư và sao kê hóa đơn bằng Google Gemini AI dành riêng cho Trình duyệt Web (Chrome, Safari, Edge, Firefox, Cốc Cốc).

---

## 🚀 Cách Mở & Chạy Trên Trình Duyệt

### Cách 1: Mở Trực Tiếp (Không cần cài đặt bất kỳ công cụ nào)
1. Tải file `web/index.html` về máy tính hoặc điện thoại của bạn.
2. Nhấp đúp chuột vào file `index.html` để mở trực tiếp bằng trình duyệt Web bất kỳ.

### Cách 2: Chạy Local Server (Tùy chọn)
Nếu bạn có Node.js hoặc Python trên máy:
- **Python**: `python3 -m http.server 3000 --directory web` -> Mở `http://localhost:3000`
- **Node.js (npx serve)**: `npx serve web` -> Mở `http://localhost:3000`

### Cách 3: Đưa lên Web Miễn Phí (GitHub Pages, Vercel, Netlify)
Chỉ cần tải file `index.html` lên GitHub Pages hoặc kéo thả vào Vercel / Netlify là có thể truy cập qua đường link web cá nhân từ mọi thiết bị.

---

## ✨ Tính Năng Nổi Bật Trên Web
- **Bảng điều khiển trực quan**: Tổng số dư tất cả tài khoản ngân hàng, dòng tiền thu nhập, chi tiêu thực tế.
- **Quản lý đa tài khoản & ví**: Vietcombank, MB Bank, Techcombank, VPBank, MoMo, Tiền mặt,...
- **Quét biến động số dư bằng Gemini AI**: Dán tin nhắn SMS hoặc tải ảnh chụp màn hình/hóa đơn để AI tự động trích xuất số tiền, danh mục, ngân hàng và ngày giờ.
- **Sổ ghi nợ & Cho vay**: Theo dõi các khoản vay nợ, hạn trả và trạng thái thanh toán.
- **Bảo mật & Dữ liệu cá nhân**: Dữ liệu lưu 100% trong LocalStorage của trình duyệt, hỗ trợ Xuất/Nhập file JSON sao lưu bất cứ lúc nào.
