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
  // ---- Ngân hàng khác
  ["HDBank", n("com.vnpay.hdbank", "HDBank", "HDBank: TK 0123xxxx789 -500,000 VND luc 10:12 06/11/2026. SD: 4,500,000 VND. ND: CHUYEN TIEN NHA"), { amount: 500000, type: "EXPENSE", bank: "HDBank", balance: 4500000, category: "Tiền thuê nhà", minConf: 0.75 }],
  ["VIB thay doi", n("com.vib.myvib2", "MyVIB", "VIB: TK *1234 thay doi -2,000,000 VND luc 06/11/2026 11:20. So du 10,000,000 VND. Noi dung: Thanh toan hoc phi IELTS"), { amount: 2000000, type: "EXPENSE", balance: 10000000, category: "Học phí & Khóa học", minConf: 0.75 }],
  ["SHB hoàn tiền", n("vn.shb.mbanking", "SHB", "SHB: TK 1012xxxx: +700.000 VND tai 06/11/2026 14:30. SD: 1.700.000 VND. ND: Hoan tien don hang Lazada"), { amount: 700000, type: "INCOME", balance: 1700000, minConf: 0.75 }],
  ["OCB giảm", n("vn.com.ocb.awe", "OCB OMNI", "Tài khoản ...1234 giảm 89.000 VND lúc 06/11/2026 08:00. Số dư: 911.000 VND. Nội dung: GRAB*TRIP"), { amount: 89000, type: "EXPENSE", bank: "OCB", balance: 911000, category: "Grab & Taxi", minConf: 0.75 }],
  ["MSB đ", n("vn.com.msb.smartBanking", "MSB", "TK 0300xxxx5678 thay đổi +1.000.000đ lúc 06/11 09:00. Số dư 3.250.000đ. ND: NGUYEN THI H chuyen tien"), { amount: 1000000, type: "INCOME", bank: "MSB", balance: 3250000, minConf: 0.75 }],
  ["LPBank", n("vn.com.lienvietpostbank", "LPBank", "LPBank: TK 0123456789 GD -75,000VND luc 06/11/2026 07:15 SD 425,000VND ND THANH TOAN PHUC LONG"), { amount: 75000, type: "EXPENSE", balance: 425000, category: "Cà phê & Trà", minConf: 0.75 }],
  ["Timo", n("io.lifestyle.plus", "Timo", "Bạn vừa nhận 2,500,000 VND từ CONG TY ABC. Nội dung: Thuong thang 10"), { amount: 2500000, type: "INCOME", category: "Tiền thưởng & Làm thêm", minConf: 0.75 }],
  ["Viettel Money", n("com.viettel.vtt.vn.emoneycustomer", "Viettel Money", "Quý khách đã thanh toán thành công 100.000đ cước di động Viettel cho thuê bao 0987xxx123"), { amount: 100000, type: "EXPENSE", category: "Cước điện thoại", minConf: 0.75 }],
  // ---- Thẻ
  ["Thẻ tín dụng + hạn mức", n("vn.com.techcombank.bb.app", "Techcombank", "The tin dung *1234 giao dich 450,000 VND tai SHOPEE luc 06/11 12:00. Han muc kha dung 20,000,000 VND"), { amount: 450000, type: "EXPENSE", category: "Mua sắm", minConf: 0.6 }],
  ["Thẻ ghi nợ POS", n("com.VCB", "VCB", "The 4xxx1234 chi tieu 120,000 VND tai CIRCLE K luc 06/11/2026 21:10. SD 2,000,000 VND"), { amount: 120000, type: "EXPENSE", balance: 2000000, category: "Đi chợ & Siêu thị", minConf: 0.75 }],
  ["Ngoại tệ", n("vn.com.techcombank.bb.app", "Techcombank", "The *1234 giao dich USD 12.99 tai NETFLIX.COM, quy doi 330,000 VND luc 06/11/2026 03:00"), { amount: 330000, type: "EXPENSE", category: "Game & Ứng dụng", minConf: 0.6 }],
  ["Rút ATM + phí", n("com.vnpay.Agribank3g", "Agribank", "Agribank: Rut tien ATM -2,000,000VND luc 18:00 06/11/2026. Phi 1,100VND. So du 3,000,000VND"), { amount: 2000000, type: "EXPENSE", balance: 3000000, fee: 1100, minConf: 0.75 }],
  // ---- Ví
  ["Nạp ví MoMo", n("com.mservice.momotransfer", "Nạp tiền thành công", "Bạn đã nạp 500.000đ vào Ví MoMo từ Vietcombank"), { amount: 500000, type: "INCOME", bank: "Ví MoMo", minConf: 0.75 }],
  ["Hoàn ShopeePay", n("com.beeasy.shopee.vn", "ShopeePay", "Hoàn tiền 25.000đ vào ví ShopeePay cho đơn hàng 2311ABC"), { amount: 25000, type: "INCOME", minConf: 0.6 }],
  ["Cake chuyển", n("xyz.be.cake", "Cake", "Bạn vừa chuyển 300,000 VND tới NGUYEN VAN A. Nội dung: tra tien an"), { amount: 300000, type: "EXPENSE", minConf: 0.75 }],
  // ---- Không có đơn vị tiền nhưng có dấu
  ["Dấu không đơn vị", n("com.vnpay.bidv", "BIDV", "Biến động số dư: TK 31410001234567 | GD: -1.000.000 | 06/11/2026 10:00 | SD: 5.000.000 | ND: chuyen tien hoc phi"), { amount: 1000000, type: "EXPENSE", balance: 5000000, minConf: 0.75 }],
  ["Giờ dạng 10h15", n("com.mbmobile", "MB", "TK 0123xxx456|GD: +200,000VND 06/11/26 10h15|SD: 1,400,000VND|ND: em gui tien"), { amount: 200000, type: "INCOME", balance: 1400000, day: 6 }],
  ["VNĐ chữ hoa", n("com.vnpay.bidv", "BIDV", "Số tiền: -1.500.000 VNĐ. Số dư cuối: 8.500.000 VNĐ. Nội dung: Dong tien dien thang 10"), { amount: 1500000, type: "EXPENSE", balance: 8500000, category: "Tiền điện & Nước" }],
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
  ["KM hoàn tiền", n("com.vpbank.neo", "VPBank NEO", "Ưu đãi hoàn tiền 50.000đ khi thanh toán QR tại Highlands. Áp dụng đến 30/11"), "promo"],
  ["Sao kê thẻ", n("vn.com.techcombank.bb.app", "Techcombank", "Sao kê thẻ tín dụng kỳ 10/2026: Dư nợ 5,200,000 VND. Hạn thanh toán 25/11/2026"), "reminder"],
  ["OTP chuyển tiền", n("com.google.android.apps.messaging", "TPBank", "TPBank: Ma xac thuc giao dich chuyen 3,000,000 VND den NGUYEN VAN A la 556677. Tuyet doi khong cung cap ma"), "otp"],
  ["Chat có số tiền", n("com.zing.zalo", "Lan", "Mai nhớ chuyển mình 200.000đ tiền vé nhé"), null],
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


// ---- bộ não học hỏi
{
  const mb = (nd, sign = "-") => n("com.mbmobile", "MB", `TK 0123xxx456|GD: ${sign}60,000VND 06/11/26 07:30|SD: 1,140,000VND|ND: ${nd}`);
  // 1) Người dùng sửa hạng mục 1 lần -> lần sau gặp đúng nội dung đó Mai xếp theo
  let br = Mai.newBrain();
  const r1 = Mai.parse(mb("THANH TOAN QR GRAB"), { brain: br });
  check("brain: trước khi học", r1, { category: "Grab & Taxi" });
  br = Mai.learn(br, { text: r1.rawNote, key: r1.memoryKey, category: "Đi làm", type: "EXPENSE", weight: 3 });
  check("brain: sửa 1 lần là nhớ", Mai.parse(mb("THANH TOAN QR GRAB"), { brain: br }), { category: "Đi làm" });
  // 2) Sửa lại lần nữa sang hạng mục khác -> Mai theo lần sửa mới nhất
  br = Mai.learn(br, { text: r1.rawNote, key: r1.memoryKey, category: "Grab & Taxi", type: "EXPENSE", weight: 3 });
  check("brain: theo lần sửa mới nhất", Mai.parse(mb("THANH TOAN QR GRAB"), { brain: br }), { category: "Grab & Taxi" });
  // 3) Học từ khoá: nội dung khác nhưng cùng từ đặc trưng
  let b2 = Mai.newBrain();
  ["CO LAN BAN XOI SANG", "XOI CO LAN", "an xoi co lan ngo 5"].forEach(t => Mai.learn(b2, { text: t, category: "Ăn sáng", type: "EXPENSE", weight: 1 }));
  check("brain: tổng quát theo từ khoá", Mai.parse(mb("CK XOI CO LAN 0912"), { brain: b2 }), { category: "Ăn sáng" });
  check("brain: nhập nhanh cũng dùng", Mai.parseQuick("xôi cô lan 25k", { brain: b2 }), { category: "Ăn sáng", amount: 25000 });
  // 4) Không để từ khoá đã học lấn át luật rõ ràng khi chỉ học yếu
  let b3 = Mai.newBrain();
  Mai.learn(b3, { text: "highlands", category: "Tiếp khách", type: "EXPENSE", weight: 1 });
  check("brain: học yếu không phá luật", Mai.parse(mb("thanh toan Highlands Coffee"), { brain: b3 }), { category: "Cà phê & Trà" });
  // 5) Không dùng hạng mục chi cho khoản thu
  let b4 = Mai.newBrain();
  Mai.learn(b4, { text: "NGUYEN VAN C", key: "nguyen van c", category: "Ăn sáng", type: "EXPENSE", weight: 3 });
  const inc = Mai.parse(mb("NGUYEN VAN C chuyen tien", "+"), { brain: b4 });
  if (inc.category === "Ăn sáng") { fail++; console.log("✗ brain: gán hạng mục chi cho khoản thu"); } else pass++;
  // 6) Học chiều tiền khi người dùng sửa Thu/Chi (thông báo không có dấu)
  const noSign = n("com.vnpay.bidv", "BIDV", "BIDV: TK 31410001234567 so tien 300,000VND luc 06/11/2026. SD: 9,500,000VND. ND: QUY LOP 12A");
  const before = Mai.parse(noSign);
  let b5 = Mai.newBrain();
  Mai.learn(b5, { text: before.rawNote, key: before.memoryKey, type: before.type === "INCOME" ? "EXPENSE" : "INCOME", typeFix: before.type === "INCOME" ? "EXPENSE" : "INCOME", weight: 3 });
  const after = Mai.parse(noSign, { brain: b5 });
  if (after.type === before.type) { fail++; console.log("✗ brain: không học được chiều thu/chi", before.type, after.type); } else pass++;
  // Dấu +/- của ngân hàng luôn thắng điều đã học
  let b6 = Mai.newBrain();
  Mai.learn(b6, { key: Mai.memoryKey(null, "QUY LOP 12A"), typeFix: "INCOME", weight: 3 });
  check("brain: dấu - thắng điều đã học", Mai.parse(mb("QUY LOP 12A"), { brain: b6 }), { type: "EXPENSE" });
  // 7) Bỏ qua 2 lần cùng loại tin -> tự bỏ qua
  const promo = n("com.mbmobile", "MB", "TK 0123xxx456|GD: +1,000VND 06/11/26|SD: 1,141,000VND|ND: LAI TIET KIEM KHONG KY HAN");
  const sig = Mai.parse(promo).signature;
  const promo2 = Mai.parse(n("com.mbmobile", "MB", "TK 0123xxx456|GD: +1,200VND 07/11/26|SD: 1,142,200VND|ND: LAI TIET KIEM KHONG KY HAN")).signature;
  let b7 = Mai.newBrain();
  Mai.learnIgnore(b7, sig);
  if (Mai.shouldIgnore(b7, promo2)) { fail++; console.log("✗ brain: bỏ qua quá sớm"); } else pass++;
  Mai.learnIgnore(b7, sig);
  if (!Mai.shouldIgnore(b7, promo2) || sig !== promo2) { fail++; console.log("✗ brain: không nhận ra cùng khuôn tin", sig, promo2); } else pass++;
  const other = Mai.parse(n("com.mbmobile", "MB", "TK 0123xxx456|GD: +5,000,000VND 07/11/26|SD: 6,142,200VND|ND: CTY ABC TRA LUONG")).signature;
  if (Mai.shouldIgnore(b7, other)) { fail++; console.log("✗ brain: bỏ qua lan sang tin khác cùng ngân hàng"); } else pass++;
  // 9) Sửa 1 lần cho "grab đi làm" -> các giao dịch grab khác cũng theo (thắng luật có sẵn)
  let b8 = Mai.newBrain();
  Mai.learn(b8, { text: "grab đi làm", category: "Đi làm", type: "EXPENSE", weight: 3 });
  check("brain: 1 lần sửa thắng luật", Mai.parseQuick("grab về nhà 30k", { brain: b8 }), { category: "Đi làm" });
  // 10) Sau đó anh xác nhận nhiều lần là Grab & Taxi -> Mai quay lại
  for (let i = 0; i < 4; i++) Mai.learn(b8, { text: "grab " + i, category: "Grab & Taxi", type: "EXPENSE", weight: 1 });
  check("brain: xác nhận nhiều lần đảo lại", Mai.parseQuick("grab đi chơi 50k", { brain: b8 }), { category: "Grab & Taxi" });
  // 8) Quên điều đã học
  Mai.forget(br, r1.memoryKey);
  if (Mai.learnedList(br).some(x => x.key === r1.memoryKey)) { fail++; console.log("✗ brain: quên không được"); } else pass++;
}


// ---- đồng bộ & cộng đồng
{
  const A = Mai.newBrain(), B = Mai.newBrain();
  Mai.learn(A, { text: "pho thin", category: "Ăn sáng", type: "EXPENSE", weight: 3 });
  Mai.learn(B, { text: "bun cha huong lien", category: "Ăn trưa", type: "EXPENSE", weight: 3 });
  let M = Mai.mergeBrain(A, B);
  check("sync: gộp 2 máy", Mai.parseQuick("phở thìn 60k", { brain: M }), { category: "Ăn sáng" });
  check("sync: gộp 2 máy (2)", Mai.parseQuick("bún chả hương liên 80k", { brain: M }), { category: "Ăn trưa" });
  const M2 = Mai.mergeBrain(M, M);
  if (JSON.stringify(M2.tok) !== JSON.stringify(M.tok)) { fail++; console.log("✗ sync: gộp lặp lại làm thay đổi số đếm"); } else pass++;
  // Máy B sửa sau -> thắng
  const B2 = JSON.parse(JSON.stringify(M));
  B2.keys["pho thin"].at = Date.now() + 1000; B2.keys["pho thin"].c = { "Ăn trưa": 3 };
  if (Mai.mergeBrain(M, B2).keys["pho thin"].c["Ăn trưa"] !== 3) { fail++; console.log("✗ sync: lần sửa mới hơn không thắng"); } else pass++;
  // Quên ở 1 máy -> máy kia cũng quên sau khi gộp
  const F = Mai.forget(JSON.parse(JSON.stringify(M)), "pho thin");
  if (Mai.mergeBrain(M, F).keys["pho thin"]) { fail++; console.log("✗ sync: quên không lan sang máy khác"); } else pass++;
  // Chia sẻ cộng đồng: không lộ tên người, số
  const P = Mai.newBrain();
  Mai.learn(P, { text: "NGUYEN VAN AN chuyen tien", key: "nguyen van an", category: "Gia đình", type: "EXPENSE", weight: 3 });
  Mai.learn(P, { text: "TT DIEN MAY XANH 0123456", category: "Mua sắm", type: "EXPENSE", weight: 3 });
  Mai.learn(P, { text: "cafe sang", category: "Cà phê & Trà", type: "EXPENSE", weight: 1 });
  const ex = Mai.communityExport(P);
  const bad = ex.filter(e => /nguyen|van|an\b|\d/.test(e.t) || e.c === "Cà phê & Trà");
  if (bad.length || !ex.some(e => e.t === "dien may" || e.t === "xanh")) { fail++; console.log("✗ community: chia sẻ sai", JSON.stringify(ex)); } else pass++;
  // Nhiều người cùng xếp -> Mai dùng; 1 người thì chưa
  const table = { "tokyolife": { "Mua sắm": 1 } };
  check("community: 1 người chưa đủ", Mai.parseQuick("tokyolife 89k", { community: table }), { category: "Khác" });
  table["tokyolife"]["Mua sắm"] = 3;
  check("community: 3 người đồng ý", Mai.parseQuick("tokyolife 89k", { community: table }), { category: "Mua sắm" });
  // Điều anh tự dạy luôn thắng cộng đồng
  const mine = Mai.newBrain(); Mai.learn(mine, { text: "tokyolife", category: "Quà tặng", type: "EXPENSE", weight: 3 });
  check("community: thua điều anh dạy", Mai.parseQuick("tokyolife 89k", { community: table, brain: mine }), { category: "Quà tặng" });
}

// ---- chống trùng: cùng giao dịch qua SMS và app
const a = Mai.parse(n("com.VCB", "VCB", "Số dư TK VCB 0071000123456 -50,000 VND lúc 06-11-2026 10:15:32. Số dư 1,234,567 VND. Ref ABC"));
const b = Mai.parse(n("com.google.android.apps.messaging", "Vietcombank", "SD TK 0071000123456 -50,000VND luc 06-11-2026 10:15:40. SD 1,234,567VND. Ref ABC"));
if (!Mai.isDuplicate(a, b)) { fail++; console.log("✗ duplicate SMS + app"); } else pass++;
const c = Mai.parse(n("com.VCB", "VCB", "Số dư TK VCB 0071000123456 -50,000 VND lúc 06-11-2026 10:25:32. Số dư 1,184,567 VND. Ref ABC"));
if (Mai.isDuplicate(a, c)) { fail++; console.log("✗ 2 giao dịch khác số dư bị coi là trùng"); } else pass++;

console.log(`\n${pass} đạt, ${fail} lỗi`);
process.exit(fail ? 1 : 0);
