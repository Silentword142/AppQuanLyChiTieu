// Chạy: node test/mai.test.js
const Mai = require("../web/mai.js");
const POST = new Date(2026, 10, 6, 10, 30).getTime();

let pass = 0, fail = 0;
function check(name, got, exp) {
  const errs = [];
  for (const k of Object.keys(exp)) {
    if (k === "noteHas") { if (!got.note || !Mai.fold(got.note).includes(Mai.fold(exp.noteHas))) errs.push(`note=${JSON.stringify(got.note)} thiếu "${exp.noteHas}"`); continue; }
    if (k === "minConf") { if (!(got.confidence >= exp.minConf)) errs.push(`confidence=${got.confidence} < ${exp.minConf}`); continue; }
    if (k === "day") { const d = new Date(got.date); if (d.getDate() !== exp.day) errs.push(`day=${d.getDate()} != ${exp.day}`); continue; }
    if (got[k] !== exp[k]) errs.push(`${k}=${JSON.stringify(got[k])} != ${JSON.stringify(exp[k])}`);
  }
  if (errs.length) { fail++; console.log("✗", name, "\n   ", errs.join("; "), "\n    raw:", JSON.stringify(got.raw)); }
  else pass++;
}
const n = (packageName, title, text) => ({ packageName, title, text, postTime: POST });

const TX = [
  // ---- Vietcombank
  ["VCB chi", n("com.VCB", "Thông báo VCB", "Số dư TK VCB 0071000123456 -50,000 VND lúc 06-11-2026 10:15:32. Số dư 1,234,567 VND. Ref MBVCB.1234567.NGUYEN VAN A chuyen tien."), { amount: 50000, type: "EXPENSE", bank: "Vietcombank", balance: 1234567, minConf: 0.75 }],
  ["VCB thu SMS", n("com.google.android.apps.messaging", "Vietcombank", "SD TK 0071000123456 +2,000,000VND luc 06-11-2026 08:30:12. SD 5,432,100VND. Ref 123456.LUONG THANG 10"), { amount: 2000000, type: "INCOME", bank: "Vietcombank", balance: 5432100, category: "Lương chính", minConf: 0.75 }],
  // ---- Techcombank
  ["TCB thu", n("vn.com.techcombank.bb.app", "Techcombank", "Tài khoản: 1903xxxx890\nSố tiền: + VND 1,500,000\nSố dư: VND 10,250,000\nMô tả: TRAN THI B chuyen tien an trua"), { amount: 1500000, type: "INCOME", bank: "Techcombank", balance: 10250000, minConf: 0.75 }],
  ["TCB chi QR Grab", n("vn.com.techcombank.bb.app", "Biến động số dư", "Tài khoản: 1903xxxx890\nSố tiền: - VND 120,000\nSố dư: VND 3,450,000\nMô tả: THANH TOAN QR GRAB"), { amount: 120000, type: "EXPENSE", balance: 3450000, category: "Grab & Taxi", minConf: 0.75 }],
  ["TCB tiếng Anh", n("vn.com.techcombank.bb.app", "Techcombank", "Account: 1903xxxx890\nAmount: -VND 65,000\nBalance: VND 3,385,000\nDescription: HIGHLANDS COFFEE NGUYEN HUE"), { amount: 65000, type: "EXPENSE", balance: 3385000, category: "Cà phê & Trà", minConf: 0.75 }],
  // ---- MB
  ["MB chi", n("com.mbmobile", "MB Bank", "TK 0123xxx456|GD: -45,000VND 06/11/26 12:30|SD: 1,200,000VND|ND: thanh toan Highlands Coffee"), { amount: 45000, type: "EXPENSE", bank: "MB Bank", balance: 1200000, category: "Cà phê & Trà", minConf: 0.75, day: 6 }],
  ["MB thu", n("com.mbmobile", "Thông báo biến động số dư", "TK 0123xxx456|GD: +3,500,000VND 05/11/26 09:01|SD: 4,700,000VND|ND: NGUYEN VAN C chuyen tien thue nha"), { amount: 3500000, type: "INCOME", balance: 4700000, minConf: 0.75, day: 5 }],
  // ---- BIDV
  ["BIDV điện", n("com.vnpay.bidv", "BIDV", "TK123xxx456 tai BIDV -200,000VND vao 10:20 06/11/2026. So du:3,000,000VND. ND: TT hoa don dien EVN HCMC"), { amount: 200000, type: "EXPENSE", bank: "BIDV", balance: 3000000, category: "Tiền điện & Nước", minConf: 0.75 }],
  ["BIDV thu", n("com.google.android.apps.messaging", "BIDV", "BIDV: TK 31410001234567 +1,000,000VND luc 06/11/2026 07:45. SD: 9,500,000VND. ND: MOMO CASHIN"), { amount: 1000000, type: "INCOME", bank: "BIDV", balance: 9500000, minConf: 0.75 }],
  // ---- VietinBank
  ["Vietin app", n("com.vietinbank.ipay", "VietinBank iPay", "Thời gian: 06/11/2026 09:15\nTài khoản: 10xxxx789\nGiao dịch: -350,000VND\nSố dư hiện tại: 2,150,000VND\nNội dung: Thanh toan Shopee don hang"), { amount: 350000, type: "EXPENSE", balance: 2150000, category: "Mua sắm", minConf: 0.75 }],
  ["Vietin SMS", n("com.samsung.android.messaging", "VietinBank", "VietinBank:06/11/2026 09:15|TK:10xxxx789|GD:+5,000,000VND|SDC:7,150,000VND|ND:CTY ABC TRA LUONG T10"), { amount: 5000000, type: "INCOME", bank: "VietinBank", balance: 7150000, category: "Lương chính", minConf: 0.75 }],
  // ---- ACB
  ["ACB thu", n("mobile.acb.com.vn", "ACB", "ACB: TK 12345678(VND) + 500,000 luc 08:12 06/11/2026. So du 1,500,000. GD: NGUYEN VAN C CK"), { amount: 500000, type: "INCOME", bank: "ACB", balance: 1500000, minConf: 0.75 }],
  ["ACB chi", n("mobile.acb.com.vn", "ACB", "ACB: TK 12345678(VND) - 89,000 luc 19:40 06/11/2026. So du 1,411,000. GD: CIRCLE K VN"), { amount: 89000, type: "EXPENSE", balance: 1411000, category: "Đi chợ & Siêu thị", minConf: 0.75 }],
  // ---- TPBank
  ["TPBank", n("com.tpb.mb.gprsandroid", "TPBank", "(TPBank): 06/11/26;10:20\nTK: xxxx1234\nPS:-99.000VND\nSD: 801.000VND\nSUND: Netflix.com"), { amount: 99000, type: "EXPENSE", bank: "TPBank", balance: 801000, category: "Game & Ứng dụng", minConf: 0.75 }],
  // ---- Agribank
  ["Agribank", n("com.vnpay.Agribank3g", "Agribank", "Agribank: 10:05 06/11/2026 Tài khoản 1500205123456: -30,000VND. Nội dung giao dịch: Nap tien dien thoai Viettel. Số dư: 970,000VND."), { amount: 30000, type: "EXPENSE", bank: "Agribank", balance: 970000, category: "Cước điện thoại", minConf: 0.75 }],
  // ---- Sacombank
  ["Sacombank", n("com.sacombank.ewallet", "Sacombank", "Sacombank 06/11/2026 10:20 TK: 0601xxxx1234 PS: -150,000 VND So du kha dung: 2,350,000 VND ND: PETROLIMEX CH 12"), { amount: 150000, type: "EXPENSE", bank: "Sacombank", balance: 2350000, category: "Xăng xe", minConf: 0.75 }],
  // ---- VPBank
  ["VPBank", n("com.vnpay.vpbankonline", "VPBank", "TK 12xxx345 | -60,000 VND | 06/11/26 10:22 | So du: 940,000 VND | ND: GRABFOOD order"), { amount: 60000, type: "EXPENSE", bank: "VPBank", balance: 940000, minConf: 0.75 }],
  // ---- Biến động tăng/giảm không dấu
  ["Tăng không dấu", n("com.VCB", "Biến động số dư", "Tài khoản 0071000123456 tăng 750.000 VND lúc 06/11/2026 11:00. Số dư: 2.000.000 VND. Nội dung: hoan tien Shopee"), { amount: 750000, type: "INCOME", balance: 2000000, minConf: 0.75 }],
  ["Giảm không dấu", n("vn.com.techcombank.bb.app", "Techcombank", "Tài khoản 1903xxxx890 giảm 1.250.000 VND. Số dư 8.000.000 VND. Nội dung: TIEN NHA THANG 11"), { amount: 1250000, type: "EXPENSE", balance: 8000000, category: "Tiền thuê nhà", minConf: 0.75 }],
  // ---- Ví điện tử
  ["MoMo thanh toán", n("com.mservice.momotransfer", "Thanh toán thành công", "Bạn đã thanh toán 55.000đ cho Highlands Coffee."), { amount: 55000, type: "EXPENSE", bank: "Ví MoMo", category: "Cà phê & Trà", noteHas: "Highlands", minConf: 0.75 }],
  ["MoMo nhận", n("com.mservice.momotransfer", "Nhận tiền thành công", "Bạn vừa nhận 200.000đ từ NGUYEN VAN D. Lời nhắn: tra tien an"), { amount: 200000, type: "INCOME", bank: "Ví MoMo", minConf: 0.75 }],
  ["ZaloPay chuyển", n("vn.com.vng.zalopay", "ZaloPay", "Bạn đã chuyển 100.000 VND đến TRAN THI E thành công"), { amount: 100000, type: "EXPENSE", bank: "ZaloPay", minConf: 0.75 }],
  ["ZaloPay đơn", n("vn.com.vng.zalopay", "Thanh toán thành công", "Thanh toán đơn hàng Be Group 49.000đ thành công"), { amount: 49000, type: "EXPENSE", category: "Grab & Taxi", minConf: 0.75 }],
  ["ShopeePay", n("com.beeasy.shopee.vn", "ShopeePay", "Thanh toán thành công ₫129.000 cho đơn hàng ShopeeFood"), { amount: 129000, type: "EXPENSE", category: "Cơm trưa / tối", minConf: 0.6 }],
  ["Cake nhận", n("xyz.be.cake", "Cake", "Bạn vừa nhận được 500,000 VND từ LE VAN F. Nội dung: gop tien du lich"), { amount: 500000, type: "INCOME", bank: "Cake", minConf: 0.75 }],
  // ---- Phí & lãi
  ["Phí SMS", n("com.VCB", "VCB", "SD TK 0071000123456 -11,000VND luc 01-11-2026 00:05:00. SD 1,223,567VND. Ref PHI SMS BANKING THANG 10"), { amount: 11000, type: "EXPENSE", category: "Phí dịch vụ & Ngân hàng", balance: 1223567, minConf: 0.75 }],
  ["Lãi", n("com.mbmobile", "MB Bank", "TK 0123xxx456|GD: +12,345VND 01/11/26 00:10|SD: 1,212,345VND|ND: Tra lai tien gui"), { amount: 12345, type: "INCOME", category: "Lãi đầu tư", minConf: 0.75 }],
  // ---- Số thập phân
  ["Có ,00", n("com.vietinbank.ipay", "VietinBank", "Giao dịch: -1.234.567,00 VND\nSố dư: 9.876.543,00 VND\nNội dung: chuyen tien"), { amount: 1234567, type: "EXPENSE", balance: 9876543 }],
  ["Có .00", n("vn.com.techcombank.bb.app", "Techcombank", "Amount: +VND 2,500,000.00\nBalance: VND 12,750,000.00\nDescription: SALARY OCT"), { amount: 2500000, type: "INCOME", balance: 12750000, category: "Lương chính" }],
  // ---- Văn bản dán tay không có package
  ["Dán tay", "MB Bank: +4,500,000 VND vao 08/06/2026. ND: CHUYEN KHOAN LUONG", { amount: 4500000, type: "INCOME", bank: "MB Bank", category: "Lương chính" }],
];

const NOT_TX = [
  ["OTP", n("com.google.android.apps.messaging", "VCB", "Ma OTP cua Quy khach la 123456. Khong chia se ma nay voi bat ky ai. Giao dich 2,000,000 VND"), "otp"],
  ["Khuyến mãi", n("com.mservice.momotransfer", "Ưu đãi hôm nay", "Giảm ngay 50.000đ khi thanh toán hóa đơn điện qua MoMo. Nhận ngay!"), "promo"],
  ["Đăng nhập", n("com.VCB", "VCB Digibank", "Quý khách đã đăng nhập VCB Digibank trên thiết bị mới lúc 10:20 06/11/2026"), null],
  ["Thất bại", n("com.mbmobile", "MB Bank", "Giao dịch chuyển tiền 500,000VND không thành công. Vui lòng thử lại"), "failed_or_pending"],
  ["Nhắc nợ thẻ", n("vn.com.techcombank.bb.app", "Techcombank", "Thẻ tín dụng của Quý khách sắp đến hạn thanh toán. Số tiền cần thanh toán tối thiểu 1,500,000 VND"), "reminder"],
  ["Tin thường", n("com.zing.zalo", "Lan", "Tối nay đi ăn không?"), "no_amount"],
];

for (const [name, input, exp] of TX) check(name, Mai.parse(input), { ok: true, ...exp });
for (const [name, input, reason] of NOT_TX) {
  const r = Mai.parse(input);
  if (r.ok || (reason && r.reason !== reason)) { fail++; console.log("✗", name, "-> ok:", r.ok, "reason:", r.reason, "amount:", r.amount); } else pass++;
}

// ---- tiền
const money = [["1,234,567", 1234567], ["1.234.567", 1234567], ["1.234.567,00", 1234567], ["1,234,567.00", 1234567], ["50000", 50000], ["12,5", 13], ["999", 999]];
for (const [s, v] of money) { const g = Mai.parseMoney(s); if (g !== v) { fail++; console.log("✗ parseMoney", s, g, "!=", v); } else pass++; }

// ---- nhập nhanh
const quick = [
  ["cafe 35k", { amount: 35000, type: "EXPENSE", category: "Cà phê & Trà" }],
  ["đổ xăng 1tr2", { amount: 1200000, type: "EXPENSE", category: "Xăng xe" }],
  ["nhận lương 15tr", { amount: 15000000, type: "INCOME", category: "Lương chính" }],
  ["ăn phở 50.000", { amount: 50000, type: "EXPENSE" }],
  ["grab đi làm 42k", { amount: 42000, category: "Grab & Taxi" }],
  ["tiền điện 1.5 triệu", { amount: 1500000, category: "Tiền điện & Nước" }],
  ["trà sữa 45", { amount: 45000, category: "Cà phê & Trà" }],
];
for (const [s, exp] of quick) check("quick: " + s, Mai.parseQuick(s), { ok: true, ...exp });

// ---- học hạng mục
const mem = { [Mai.memoryKey(null, "PHO THIN BO HO")]: "Ăn sáng" };
check("memory", Mai.parse(n("com.mbmobile", "MB", "TK 0123xxx456|GD: -60,000VND 06/11/26 07:30|SD: 1,140,000VND|ND: PHO THIN BO HO"), { memory: mem }), { ok: true, category: "Ăn sáng" });

// ---- chống trùng: cùng giao dịch qua SMS và app
const a = Mai.parse(n("com.VCB", "VCB", "Số dư TK VCB 0071000123456 -50,000 VND lúc 06-11-2026 10:15:32. Số dư 1,234,567 VND. Ref ABC"));
const b = Mai.parse(n("com.google.android.apps.messaging", "Vietcombank", "SD TK 0071000123456 -50,000VND luc 06-11-2026 10:15:40. SD 1,234,567VND. Ref ABC"));
if (!Mai.isDuplicate(a, b)) { fail++; console.log("✗ duplicate SMS + app"); } else pass++;
const c = Mai.parse(n("com.VCB", "VCB", "Số dư TK VCB 0071000123456 -50,000 VND lúc 06-11-2026 10:25:32. Số dư 1,184,567 VND. Ref ABC"));
if (Mai.isDuplicate(a, c)) { fail++; console.log("✗ 2 giao dịch khác số dư bị coi là trùng"); } else pass++;

console.log(`\n${pass} đạt, ${fail} lỗi`);
process.exit(fail ? 1 : 0);
