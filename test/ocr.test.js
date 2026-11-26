// Chạy: node test/ocr.test.js – đọc ảnh theo bố cục (web/ocr.js) trên các dòng chữ giả lập
const O = require("../web/ocr.js");
let pass = 0, fail = 0;
const eq = (name, got, exp) => { if (JSON.stringify(got) === JSON.stringify(exp)) pass++; else { fail++; console.log("✗", name, "got", JSON.stringify(got), "exp", JSON.stringify(exp)); } };
// dòng: [chữ, x0, y0, chiều cao, x1]
const L = rows => rows.map(([text, x0, y0, h, x1]) => ({ text, x0, y0, y1: y0 + h, h, x1: x1 || x0 + text.length * h * 0.5 }));

// biên lai: số tiền chữ to, nhãn – giá trị tách 2 cột, có số dư/phí/mã GD/STK
let a = O.analyze(L([["Chuyển tiền thành công", 200, 100, 30], ["-150.000 VND", 250, 160, 70], ["12:30 25/11/2026", 280, 250, 26],
  ["Tài khoản nguồn", 40, 330, 30], ["0071000123456", 650, 330, 30], ["Phí giao dịch", 40, 390, 30], ["0VND", 900, 390, 30],
  ["Mã giao dịch", 40, 450, 30], ["FT25329104871", 650, 450, 30], ["Nội dung", 40, 510, 30], ["Tien an trua", 700, 510, 30],
  ["Số dư sau GD", 40, 570, 30], ["2.345.678 VND", 650, 570, 30]]));
eq("biên lai: số tiền", a.items[0].amount, 150000);
eq("biên lai: chi", a.items[0].type, "EXPENSE");
eq("biên lai: nội dung", a.items[0].note, "Tien an trua");
eq("biên lai: ngày", a.items[0].date, "2026-11-25T05:30:00.000Z");

// hoá đơn: lấy TỔNG CỘNG (OCR đọc lệch "TORG CONG"), bỏ tiền khách đưa / tiền thối / giảm giá
a = O.analyze(L([["Mi ly x2 24.000", 40, 100, 30, 900], ["Giảm giá -10.500", 40, 150, 30, 900], ["TORG CONG 120.000d", 40, 200, 40, 900],
  ["Tiền khách đưa 200.000", 40, 260, 30, 900], ["Tiền thối 80.000", 40, 310, 30, 900]]), { now: Date.UTC(2026, 10, 25) });
eq("hoá đơn: tổng", a.items[0].amount, 120000);

// lịch sử: nhiều dòng +/- -> nhiều khoản, bỏ số dư đầu trang
a = O.analyze(L([["Số dư khả dụng", 40, 50, 28], ["6.560.000 VND", 40, 90, 40], ["GRAB*TRANSPORT HCM", 40, 200, 30], ["-62.000 đ", 800, 205, 30],
  ["24/11/2026 08:12", 40, 240, 22], ["LUONG THANG 11", 40, 300, 30], ["+15.000.000 đ", 760, 305, 30], ["23/11/2026 17:00", 40, 340, 22],
  ["HIGHLANDS COFFEE", 40, 400, 30], ["-55.000 đ", 800, 405, 30], ["23/11/2026 09:40", 40, 440, 22]]), { now: Date.UTC(2026, 10, 25) });
eq("lịch sử: chế độ", a.mode, "list");
eq("lịch sử: số tiền", a.items.map(i => (i.type === "INCOME" ? "+" : "-") + i.amount), ["-62000", "+15000000", "-55000"]);
eq("lịch sử: tên", a.items.map(i => i.note), ["GRAB*TRANSPORT HCM", "LUONG THANG 11", "HIGHLANDS COFFEE"]);

// nhiều thông báo trong 1 ảnh -> tách thành từng khối
const b = O.blocks(L([["MB Bank • 10 phút", 40, 100, 24], ["TK 0123|GD: -50,000VND|SD: 1,234,000VND", 40, 130, 28],
  ["Vietcombank • 1 giờ", 40, 230, 24], ["So du TK +2,000,000 VND luc 07:30", 40, 260, 28], ["So du 5,000,000 VND", 40, 292, 28],
  ["MoMo • 3 giờ", 40, 390, 24], ["Bạn vừa thanh toán 35.000đ", 40, 420, 28]]));
eq("thông báo: số khối", b.length, 3);
eq("thông báo: khối 2", b[1].text, "Vietcombank • 1 giờ\nSo du TK +2,000,000 VND luc 07:30\nSo du 5,000,000 VND");

// không lấy ngày / giờ / STK làm số tiền
eq("token: bỏ ngày giờ", O.tokensOf({ text: "12:30 25/11/2026" }).length, 0);
eq("token: 1.250.000đ", O.tokensOf({ text: "Số tiền: 1.250.000đ" })[0].amount, 1250000);

// ảnh lịch sử có tiêu đề ngày (ShopCash): mỗi dòng lấy ngày của tiêu đề gần nhất phía trên, không lấy hôm nay
const NOW = Date.UTC(2026, 10, 25, 17, 5);
a = O.analyze(L([["Lịch sử giao dịch", 40, 300, 30], ["24/11/2026", 40, 440, 24], ["VU THANH THUY", 140, 580, 34], ["- 85,000", 740, 578, 30],
  ["CAO LE HOANG LONG chuyen", 140, 630, 26], ["22/11/2026", 40, 770, 24], ["HO KINH DOANH HU TIEU", 140, 910, 34], ["- 135,000", 720, 908, 30],
  ["TRAN THI THIEN LY", 140, 1140, 34], ["- 75,000", 740, 1138, 30], ["CAO LE HOANG LONG chuyen tien", 140, 1190, 26]]), { now: NOW });
eq("tiêu đề ngày: chế độ", a.mode, "list");
eq("tiêu đề ngày: ngày", a.items.map(i => i.date && i.date.slice(0, 10)), ["2026-11-24", "2026-11-22", "2026-11-22"]);
eq("tiêu đề ngày: tên", a.items.map(i => i.note), ["VU THANH THUY", "HO KINH DOANH HU TIEU", "TRAN THI THIEN LY"]);
eq("tiêu đề: Hôm qua", O.headerDate("Hôm qua", NOW).slice(0, 10), "2026-11-25");
eq("tiêu đề: không phải dòng có giờ", O.headerDate("24/11/2026 08:12", NOW), null);
eq("ngày bị xuống dòng", O.parseDate("luc 25- 11-2026 07:30:15", NOW), "2026-11-25T00:30:00.000Z");

console.log(`ocr: ${pass} đạt, ${fail} lỗi`);
if (fail) process.exit(1);
