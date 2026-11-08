/*
 * Mai – trợ lý đọc thông báo giao dịch ngân hàng / ví điện tử, chạy 100% offline.
 *
 * Không gọi mạng, không cần API key. Toàn bộ là luật (rule-based) được kiểm thử bằng
 * bộ mẫu trong test/mai.test.js. Mỗi kết quả có `confidence`; app chỉ tự ghi sổ khi đủ
 * chắc chắn, còn lại đưa vào "Hộp chờ duyệt" để người dùng xác nhận — nhờ vậy không có
 * giao dịch sai nào bị ghi âm thầm.
 *
 * API:
 *   MaiAgent.parse(input, opts)  input: chuỗi | { packageName, title, text, postTime }
 *   MaiAgent.parseQuick(text)    câu nhập tay kiểu "cafe 35k", "nhận lương 15tr"
 *   MaiAgent.parseMoney(str)     "1.234.567,00" -> 1234567
 *   MaiAgent.fingerprint(r)      khoá chống trùng (SMS + thông báo app của cùng 1 giao dịch)
 *   MaiAgent.memoryKey(r)        khoá để Mai "học" hạng mục người dùng đã sửa
 */
(function (root) {
  "use strict";

  // ---------- Chuẩn hoá chữ: bỏ dấu nhưng giữ nguyên độ dài để cắt chuỗi gốc theo vị trí ----------
  function foldChar(c) {
    if (c === "đ") return "d";
    if (c === "Đ") return "d";
    if (c === "−" || c === "–" || c === "—") return "-";
    var f = c.normalize("NFD").replace(/[̀-ͯ]/g, "");
    if (f.length !== 1) return c.length === 1 ? c.toLowerCase() : " ";
    return f.toLowerCase();
  }
  function fold(s) {
    var out = "";
    for (var i = 0; i < s.length; i++) out += foldChar(s[i]);
    return out;
  }

  // ---------- Ngân hàng & ví ----------
  var BANKS = [
    { name: "Vietcombank", pkgs: ["com.VCB", "com.vcb"], re: /\b(vietcombank|vcb|vcb digibank)\b/ },
    { name: "Techcombank", pkgs: ["vn.com.techcombank.bb.app", "com.fastacash.tcb"], re: /\b(techcombank|tcb)\b/ },
    { name: "MB Bank", pkgs: ["com.mbmobile", "com.mbbank.mobile"], re: /\b(mb ?bank|mbbank|mbb)\b/ },
    { name: "BIDV", pkgs: ["com.vnpay.bidv", "com.bidv.smartbanking"], re: /\bbidv\b/ },
    { name: "VietinBank", pkgs: ["com.vietinbank.ipay"], re: /\b(vietinbank|vietin bank|ipay)\b/ },
    { name: "Agribank", pkgs: ["com.vnpay.Agribank3g", "com.agribank.mobile"], re: /\bagribank\b/ },
    { name: "ACB", pkgs: ["mobile.acb.com.vn", "com.acb.one"], re: /\bacb\b/ },
    { name: "TPBank", pkgs: ["com.tpb.mb.gprsandroid"], re: /\b(tpbank|tpb)\b/ },
    { name: "VPBank", pkgs: ["com.vnpay.vpbankonline", "com.vpbank.neo", "com.vpb.neo"], re: /\b(vpbank|vpb|vpbank neo)\b/ },
    { name: "Sacombank", pkgs: ["com.sacombank.ewallet", "com.sacombank.mbanking"], re: /\b(sacombank|stb)\b/ },
    { name: "VIB", pkgs: ["com.vib.myvib2", "com.vib.myvib"], re: /\bvib\b/ },
    { name: "SHB", pkgs: ["vn.shb.mbanking", "vn.shb.saha.mbanking"], re: /\bshb\b/ },
    { name: "Shinhan Bank", pkgs: ["com.shinhan.global.vn.bank"], re: /\bshinhan\b/ },
    { name: "HDBank", pkgs: ["com.vnpay.hdbank", "com.hdbank.mobilebanking"], re: /\bhdbank\b/ },
    { name: "OCB", pkgs: ["vn.com.ocb.awe"], re: /\bocb\b/ },
    { name: "MSB", pkgs: ["vn.com.msb.smartBanking"], re: /\bmsb\b/ },
    { name: "SeABank", pkgs: ["vn.com.seabank.mb1"], re: /\bseabank\b/ },
    { name: "LPBank", pkgs: ["vn.com.lienvietpostbank"], re: /\b(lpbank|lienvietpostbank|lpb)\b/ },
    { name: "Eximbank", pkgs: ["com.vnpay.eximbank"], re: /\beximbank\b/ },
    { name: "Nam A Bank", pkgs: ["com.namabank.app"], re: /\bnam a ?bank\b/ },
    { name: "Bac A Bank", pkgs: ["com.bab.retailUAT"], re: /\bbac a ?bank\b/ },
    { name: "PVcomBank", pkgs: ["com.pvcombank.retail"], re: /\bpvcombank\b/ },
    { name: "Cake", pkgs: ["xyz.be.cake"], re: /\bcake\b/ },
    { name: "Timo", pkgs: ["io.lifestyle.plus"], re: /\btimo\b/ },
    { name: "Ví MoMo", pkgs: ["com.mservice.momotransfer"], re: /\bmomo\b/ },
    { name: "ZaloPay", pkgs: ["vn.com.vng.zalopay"], re: /\bzalo ?pay\b/ },
    { name: "Ví ShopeePay", pkgs: ["com.beeasy.shopee.vn"], re: /\bshopee ?pay\b/ },
    { name: "Viettel Money", pkgs: ["com.viettel.vtt.vn.emoneycustomer", "com.bplus.vtpay"], re: /\bviettel ?money\b/ }
  ];
  var SMS_PKGS = ["com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms", "com.android.messaging", "com.oneplus.mms", "com.miui.mms"];

  function bankFromPackage(pkg) {
    if (!pkg) return null;
    for (var i = 0; i < BANKS.length; i++) {
      for (var j = 0; j < BANKS[i].pkgs.length; j++) {
        if (BANKS[i].pkgs[j].toLowerCase() === String(pkg).toLowerCase()) return BANKS[i].name;
      }
    }
    return null;
  }
  function bankFromText(lower) {
    for (var i = 0; i < BANKS.length; i++) if (BANKS[i].re.test(lower)) return BANKS[i].name;
    return null;
  }
  function isKnownPackage(pkg) {
    return !!bankFromPackage(pkg) || SMS_PKGS.indexOf(pkg) !== -1;
  }

  // ---------- Tiền ----------
  // Nhận "1,234,567" "1.234.567" "1.234.567,00" "1,234,567.00" "50000" "1.5" -> số đồng (làm tròn)
  function parseMoney(str) {
    if (str === null || str === undefined) return NaN;
    var s = String(str).replace(/\s/g, "");
    if (!/\d/.test(s)) return NaN;
    var lastDot = s.lastIndexOf("."), lastComma = s.lastIndexOf(",");
    var hasDot = lastDot !== -1, hasComma = lastComma !== -1;
    var intPart = s, dec = "";
    if (hasDot && hasComma) {
      var decSep = lastDot > lastComma ? "." : ",";
      var idx = s.lastIndexOf(decSep);
      intPart = s.slice(0, idx); dec = s.slice(idx + 1);
    } else if (hasDot || hasComma) {
      var sep = hasDot ? "." : ",";
      var parts = s.split(sep);
      var groupsOk = parts.length > 1 && parts.slice(1).every(function (p) { return p.length === 3; }) && parts[0].length <= 3;
      if (!groupsOk && parts.length === 2 && parts[1].length <= 2) { intPart = parts[0]; dec = parts[1]; }
    }
    var n = parseInt(intPart.replace(/[^\d]/g, ""), 10);
    if (isNaN(n)) return NaN;
    if (dec && /^\d{1,2}$/.test(dec)) n += parseInt(dec, 10) / Math.pow(10, dec.length);
    return Math.round(n);
  }

  // Đơn vị nói: 35k, 1tr2, 1.5 triệu, 200 nghìn, 2 tỷ
  function parseSpokenMoney(numStr, unit, tail) {
    var base = parseFloat(String(numStr).replace(",", "."));
    if (isNaN(base)) return NaN;
    var mult = 1;
    if (/^(k|nghin|ngan|ng)$/.test(unit)) mult = 1e3;
    else if (/^(tr|trieu|cu|m)$/.test(unit)) mult = 1e6;
    else if (/^(ty|ti|b)$/.test(unit)) mult = 1e9;
    var v = base * mult;
    if (tail && /^\d{1,3}$/.test(tail)) { // 1tr2 = 1.200.000 ; 2k5 = 2.500 ; 1tr250 = 1.250.000
      v += parseInt(tail, 10) * mult / Math.pow(10, tail.length);
    }
    return Math.round(v);
  }

  var MONEY_RE = /([+\-])?[ \t]*(?:(vnd|vnđ|₫)[ \t]*)?(\d{1,3}(?:[.,]\d{3})+(?:[.,]\d{1,2})?|\d+(?:[.,]\d{1,2})?)(?:[ \t]*(vnd|dong|₫|d(?![a-z])|k(?![a-z])|tr(?![a-z])|trieu|nghin|ngan))?/g;

  var DELIM_RE = /[|\n;]|\.\s|,\s(?=[a-z])/g;
  function segmentBefore(lower, idx) {
    var start = 0, m;
    DELIM_RE.lastIndex = 0;
    while ((m = DELIM_RE.exec(lower)) && m.index < idx) start = m.index + m[0].length;
    return lower.slice(Math.max(start, idx - 45), idx);
  }

  var BAL_PREFIX = /(?:\bso du|\bsd|\bsdc|\bsdkd|\bbalance|\bbal|\bsdcuoi|\bsodu|\bhan muc(?: kha dung| con lai)?|\bdu no)\b[^0-9+\-]{0,26}$/;
  var AMT_PREFIX = /(?:\bquy doi|\bso tien(?: gd| giao dich)?|\bgd|\bps|\bphat sinh|\bgiao dich|\bst|\bamount|\bthay doi|\bbien dong|\btang|\bgiam|\bthanh toan|\bnhan(?: duoc)?|\bchuyen(?: khoan| tien)?|\btru|\bcong|\bghi co|\bghi no|\brut|\bnap)\b[^0-9]{0,18}$/;
  var FEE_PREFIX = /\b(phi|fee|vat)\b[^0-9]{0,14}$/;

  function findMoneyTokens(lower) {
    var out = [], m;
    MONEY_RE.lastIndex = 0;
    while ((m = MONEY_RE.exec(lower))) {
      var full = m[0];
      var startNum = m.index + full.indexOf(m[3]);
      var before = lower.charAt(startNum - 1), after = lower.charAt(startNum + m[3].length);
      var hasSign = !!m[1];
      // bỏ các số dính vào ngày/giờ/mã: 06/11/2026, 10:20, ABC123, 123abc
      if ((/[\/\d]/.test(before) || (before === ":" && /\d/.test(lower.charAt(startNum - 2)))) && !hasSign) continue;
      if (/[a-z]/.test(before) && !hasSign && !m[2]) continue;
      if (/[\/:]/.test(after) || /[a-z0-9]/.test(after) && !m[4]) {
        if (!(m[4])) continue;
      }
      if (/^[.,]\d/.test(lower.slice(startNum + m[3].length))) continue;
      var cur = m[2] || (m[4] && /^(vnd|dong|d|₫)$/.test(m[4]) ? m[4] : null);
      var unit = m[4] && /^(k|tr|trieu|nghin|ngan)$/.test(m[4]) ? m[4] : null;
      // dấu "-" ngay sau chữ/số (vd "06-11-2026", "MB-123") không phải dấu âm
      if (hasSign) {
        var signIdx = m.index + full.indexOf(m[1]);
        var pc = lower.charAt(signIdx - 1);
        if (/[a-z0-9]/.test(pc)) hasSign = false;
      }
      // Số tiền ngoại tệ (USD 12.99, 12.99 EUR) -> không phải số tiền VND, bỏ qua
      if (/\b(usd|eur|jpy|krw|sgd|thb|cny|aud|gbp|\$)\s*$/.test(lower.slice(Math.max(0, startNum - 5), startNum)) || /^\s*(usd|eur|jpy|krw|sgd|thb|cny|aud|gbp)\b/.test(lower.slice(startNum + m[3].length, startNum + m[3].length + 5))) continue;
      var value = unit ? parseSpokenMoney(m[3].replace(/[.,](?=\d{3})/g, ""), unit) : parseMoney(m[3]);
      if (!(value > 0)) continue;
      var hasSep = /[.,]\d{3}/.test(m[3]);
      out.push({
        index: startNum, end: m.index + full.length, raw: full.trim(), value: value,
        sign: hasSign ? (m[1] === "-" ? -1 : 1) : 0,
        currency: !!cur, unit: unit, sep: hasSep, digits: m[3].replace(/\D/g, "").length
      });
    }
    return out;
  }

  // ---------- Ngày giờ ----------
  function parseDateTime(lower, fallback) {
    var d = null;
    var dm = lower.match(/\b(\d{1,2})[\/\-.](\d{1,2})(?:[\/\-.](\d{2,4}))?\b/);
    var tm = lower.match(/\b(\d{1,2})[:h](\d{2})(?::(\d{2}))?\b/);
    var base = fallback ? new Date(fallback) : new Date();
    if (isNaN(base)) base = new Date();
    if (dm) {
      var day = +dm[1], mon = +dm[2], yr = dm[3] ? +dm[3] : base.getFullYear();
      if (yr < 100) yr += 2000;
      if (day >= 1 && day <= 31 && mon >= 1 && mon <= 12 && yr > 2000 && yr < 2100) {
        d = new Date(yr, mon - 1, day, tm ? +tm[1] : base.getHours(), tm ? +tm[2] : base.getMinutes(), tm && tm[3] ? +tm[3] : 0);
        if (tm && (+tm[1] > 23 || +tm[2] > 59)) d = new Date(yr, mon - 1, day, base.getHours(), base.getMinutes());
      }
    }
    if (!d) return { date: base, explicit: false };
    // Thông báo vừa đến thì ngày không thể lệch xa thời điểm nhận: nếu lệch >3 ngày thì tin thời điểm nhận
    if (fallback && Math.abs(d - base) > 3 * 86400000) return { date: base, explicit: false };
    if (!fallback && d - Date.now() > 86400000) return { date: base, explicit: false };
    return { date: d, explicit: true };
  }

  // ---------- Hạng mục ----------
  var CATEGORY_RULES = [
    { cat: "Phí dịch vụ & Ngân hàng", re: /\b(phi (sms|dich vu|thuong nien|duy tri|quan ly|chuyen tien|giao dich|rut tien)|sms ?banking|annual fee|service fee|phi thuong nien the)\b/ },
    { cat: "Lãi đầu tư", re: /\b(lai tien gui|tra lai|lai tiet kiem|interest|lai suat|tat toan|co tuc|lai khong ky han)\b/ },
    { cat: "Lương chính", re: /\b(luong|salary|payroll|tra luong|chi luong)\b/ },
    { cat: "Tiền thưởng & Làm thêm", re: /\b(thuong|bonus|hoa hong|commission|hoan tien|cashback|refund)\b/ },
    { cat: "Grab & Taxi", re: /\b(grab|be group|bevn|\bbe\b|xanh sm|xanhsm|gojek|taxi|mai linh|vinasun|uber)\b/ },
    { cat: "Xăng xe", re: /\b(xang|petrolimex|plx|pvoil|do xang|nhien lieu|comeco)\b/ },
    { cat: "Gửi xe", re: /\b(gui xe|ve xe thang|parking|bai xe)\b/ },
    { cat: "Vé tàu & Máy bay", re: /\b(vietnam airlines|vietjet|bamboo|vietravel airlines|ve may bay|ve tau|dsvn|traveloka|vexere|futa|phuong trang)\b/ },
    { cat: "Cà phê & Trà", re: /\b(highlands?|phuc long|starbucks|cong caphe|the coffee house|tch|katinat|cafe|ca phe|coffee|tra sua|gong cha|koi the|phe la|trung nguyen|milk ?tea|tocotoco)\b/ },
    { cat: "Đi chợ & Siêu thị", re: /\b(winmart|vinmart|bach hoa xanh|bhx|coop ?mart|co\.op|lotte mart|go!|big c|aeon|mega market|circle k|ministop|familymart|gs25|7-eleven|kingfoodmart|di cho|sieu thi)\b/ },
    { cat: "Cơm trưa / tối", re: /\b(shopee ?food|grab ?food|baemin|be food|com tam|com trua|com toi|nha hang|quan an|lau|nuong|kfc|lotteria|jollibee|mcdonald|pizza|texas chicken|bun|pho|an trua|an toi)\b/ },
    { cat: "Ăn sáng", re: /\b(an sang|banh mi|xoi|breakfast)\b/ },
    { cat: "Cước điện thoại", re: /\b(nap tien dien thoai|nap dt|topup|top up|cuoc (di dong|dien thoai)|viettel|vinaphone|mobifone|vietnamobile|nap the)\b/ },
    { cat: "Tiền điện & Nước", re: /\b(evn|tien dien(?! thoai)|hoa don dien|dien luc|tien nuoc|cap nuoc|sawaco|nuoc sach)\b/ },
    { cat: "Internet & Wifi", re: /\b(fpt telecom|vnpt|internet|wifi|cap quang|viettel internet)\b/ },
    { cat: "Tiền thuê nhà", re: /\b(tien nha|thue nha|tien phong|thue phong)\b/ },
    { cat: "Game & Ứng dụng", re: /\b(google play|app ?store|apple\.com|itunes|netflix|spotify|youtube premium|steam|garena|icloud|chatgpt|openai|anthropic|claude)\b/ },
    { cat: "Xem phim", re: /\b(cgv|lotte cinema|galaxy cinema|bhd|beta cinemas|rap phim|ve xem phim)\b/ },
    { cat: "Quần áo & Giày dép", re: /\b(uniqlo|zara|h&m|canifa|routine|yody|adidas|nike|giay|quan ao)\b/ },
    { cat: "Đồ công nghệ", re: /\b(the gioi di dong|tgdd|dien may xanh|fpt shop|cellphones|hoang ha mobile|apple store|phong vu)\b/ },
    { cat: "Mỹ phẩm & Làm đẹp", re: /\b(hasaki|guardian|watsons|my pham|spa|cat toc|lam nail|nail)\b/ },
    { cat: "Thuốc men", re: /\b(nha thuoc|pharmacity|long chau|an khang|thuoc)\b/ },
    { cat: "Khám chữa bệnh", re: /\b(benh vien|phong kham|kham benh|vinmec|hospital|clinic|nha khoa)\b/ },
    { cat: "Tập gym & Thể thao", re: /\b(gym|california fitness|citigym|yoga|the thao|swimming|be boi)\b/ },
    { cat: "Học phí & Khóa học", re: /\b(hoc phi|khoa hoc|udemy|coursera|tieng anh|ielts|truong)\b/ },
    { cat: "Sách vở & Tài liệu", re: /\b(fahasa|tiki trading|nha sach|sach)\b/ },
    { cat: "Bảo hiểm & Thuế", re: /\b(bao hiem|prudential|manulife|aia|bao viet|nop thue|thue tncn|thue thu nhap|le phi truoc ba|tax|bhxh|bhyt)\b/ },
    { cat: "Hiếu hỉ & Quà biếu", re: /\b(mung cuoi|dam cuoi|dam hieu|qua tang|li xi|mung sinh nhat|phung vieng)\b/ },
    { cat: "Con cái & Tiền sữa", re: /\b(tien sua|bim|con tho|kids|mam non|tre em)\b/ },
    { cat: "Thú cưng", re: /\b(pet|thu cung|cho meo|thuc an cho)\b/ },
    { cat: "Mua sắm", re: /\b(shopee|lazada|tiki|tiktok shop|sendo|amazon|aliexpress)\b/ },
    { cat: "Du lịch & Nghỉ dưỡng", re: /\b(agoda|booking\.com|airbnb|khach san|hotel|resort|homestay)\b/ }
  ];

  function suggestCategory(lowerNote, type, memory, memKey) {
    if (memory && memKey && memory[memKey]) return { category: memory[memKey], source: "memory" };
    for (var i = 0; i < CATEGORY_RULES.length; i++) {
      var r = CATEGORY_RULES[i];
      if (r.re.test(lowerNote)) {
        var isIncomeCat = /^(Lương chính|Tiền thưởng & Làm thêm|Lãi đầu tư)$/.test(r.cat);
        if (type === "INCOME" && !isIncomeCat) continue;
        if (type === "EXPENSE" && isIncomeCat && r.cat !== "Tiền thưởng & Làm thêm") continue;
        if (type === "EXPENSE" && r.cat === "Tiền thưởng & Làm thêm") continue;
        return { category: r.cat, source: "rule" };
      }
    }
    return { category: type === "INCOME" ? "Lương & Thu nhập" : "Khác", source: "default" };
  }


  // ---------- Bộ não học hỏi của Mai ----------
  // Học từ: người dùng sửa hạng mục / sửa thu-chi / bấm Bỏ qua, và từ lịch sử giao dịch đã ghi.
  // Lưu dạng JSON nhỏ gọn trên máy: keys (đối tác/nội dung cụ thể), tok (từ khoá -> hạng mục), ignore (mẫu tin bỏ qua).
  var STOP = {};
  ("chuyen tien tk stk so du sd vnd vnđ dong giao dich gd thanh toan tai cho tu den qua ngan hang nhan cua ban la va " +
   "ck nd noi dung ma luc ngay thang nam gio phi the tai khoan tien tai the vi so ref ft mbvcb ibft trace acsp bank " +
   "toi den di vao ra mot cac nhung duoc da vua con co khong em anh chi ban minh ok nhe a " +
   // tên ngân hàng / ví: có trong mọi thông báo nên không nói lên hạng mục
   "techcombank tcb vietcombank vcb mbbank mb bidv vietinbank agribank acb tpbank vpbank sacombank hdbank vib shb ocb msb seabank " +
   "lpbank lienviet eximbank namabank abbank bacabank pvcombank kienlongbank cake timo ubank momo zalopay viettelpay vnpay shopeepay " +
   "tieu nhan thu visa mastercard napas debit credit vnd").split(" ").forEach(function (w) { STOP[w] = 1; });

  function tokenize(text) {
    var words = fold(text || "").replace(/[^a-z0-9 ]/g, " ").split(/\s+/).filter(function (w) {
      return w.length >= 2 && !STOP[w] && !/^\d+$/.test(w) && !/^[a-z]{0,3}\d{4,}/.test(w);
    });
    var out = {};
    words.forEach(function (w, i) {
      out[w] = 1;
      if (i + 1 < words.length) out[w + " " + words[i + 1]] = 1;
    });
    return Object.keys(out);
  }

  function newBrain() { return { v: 1, keys: {}, tok: {}, cats: {}, ignore: {}, n: 0 }; }

  function bump(map, k, w, decayOthers) {
    if (decayOthers) Object.keys(map).forEach(function (o) { if (o !== k) map[o] = Math.round(map[o] * 50) / 100; });
    map[k] = Math.round(((map[k] || 0) + w) * 100) / 100;
  }

  // ex = { text, key, category, type, weight, typeFix }
  //  weight 1 = xác nhận / ghi tay, weight 3 = người dùng sửa (chấn chỉnh) -> ưu tiên mạnh, làm nhạt lựa chọn cũ
  function learn(brain, ex) {
    brain = brain || newBrain();
    if (!ex) return brain;
    var w = ex.weight || 1, fix = w >= 3, type = ex.type === "INCOME" ? "INCOME" : "EXPENSE";
    var key = ex.key || memoryKeyFrom(null, ex.text);
    if (ex.category) {
      var cs = brain.cats[ex.category] || (brain.cats[ex.category] = { INCOME: 0, EXPENSE: 0 });
      cs[type] = (cs[type] || 0) + w;
      if (key) { var k = brain.keys[key] || (brain.keys[key] = { c: {}, t: {} }); bump(k.c, ex.category, w, fix); k.at = Date.now(); if (ex.label || (ex.text && !k.l)) k.l = String(ex.label || ex.text).trim().slice(0, 40); }
      if (!(key && key.indexOf("amt:") === 0)) tokenize(ex.text).forEach(function (t) { bump(brain.tok[t] || (brain.tok[t] = {}), ex.category, w, fix); });
    }
    if (ex.typeFix && key) {
      var kk = brain.keys[key] || (brain.keys[key] = { c: {}, t: {} });
      bump(kk.t, ex.typeFix === "INCOME" ? "INCOME" : "EXPENSE", w, true);
    }
    brain.n = (brain.n || 0) + 1;
    // Giữ gọn: tối đa ~5000 từ khoá, bỏ những từ ít gặp nhất
    var tk = Object.keys(brain.tok);
    if (tk.length > 5000) {
      tk.map(function (t) { var d = brain.tok[t], s = 0; for (var c in d) s += d[c]; return [t, s]; })
        .sort(function (a, b) { return a[1] - b[1]; }).slice(0, tk.length - 4000)
        .forEach(function (x) { delete brain.tok[x[0]]; });
    }
    return brain;
  }

  function topOf(map, ok) {
    var best = null, bv = 0, total = 0;
    for (var c in map) { if (ok && !ok(c)) continue; total += map[c]; if (map[c] > bv) { bv = map[c]; best = c; } }
    return { name: best, value: bv, total: total };
  }

  // Hạng mục này có dùng được cho chiều tiền này không (không gợi ý hạng mục chi cho khoản thu)
  function typeOk(brain, type) {
    return function (c) {
      var s = brain.cats && brain.cats[c];
      if (!s || !type) return true;
      var other = type === "INCOME" ? "EXPENSE" : "INCOME";
      return (s[type] || 0) > 0 || !(s[other] > 0);
    };
  }

  // -> { category, source: "learned-key" | "learned", strong } hoặc null
  function predictCategory(brain, text, type, key) {
    if (!brain || !brain.keys) return null;
    var ok = typeOk(brain, type);
    if (key && brain.keys[key]) {
      var t = topOf(brain.keys[key].c, ok);
      if (t.name && t.value >= 1 && t.value / t.total >= 0.6) return { category: t.name, source: "learned-key", strong: true, weight: t.value };
    }
    var scores = {}, toks = tokenize(text);
    toks.forEach(function (tk) {
      var d = brain.tok[tk]; if (!d) return;
      var tot = 0, c; for (c in d) tot += d[c];
      if (tot <= 0) return;
      var conf = Math.min(1.5, tot / 2) * (tk.indexOf(" ") > 0 ? 1.5 : 1); // 1 lần anh sửa (trọng số 3) đủ mạnh để thắng luật
      for (c in d) if (ok(c)) scores[c] = (scores[c] || 0) + (d[c] / tot) * conf;
    });
    var arr = Object.keys(scores).map(function (c) { return [c, scores[c]]; }).sort(function (a, b) { return b[1] - a[1]; });
    if (!arr.length || arr[0][1] < 0.6) return null;
    var second = arr[1] ? arr[1][1] : 0;
    if (arr[0][1] < second * 1.4) return null;
    return { category: arr[0][0], source: "learned", strong: arr[0][1] >= 1.2 && arr[0][1] >= second * 2 };
  }

  function learnedType(brain, key) {
    if (!brain || !key || !brain.keys || !brain.keys[key]) return null;
    var t = brain.keys[key].t || {}, i = t.INCOME || 0, e = t.EXPENSE || 0;
    if (i >= 1 && i > e) return "INCOME";
    if (e >= 1 && e > i) return "EXPENSE";
    return null;
  }

  // "Khuôn" của một thông báo (bỏ số, giữ chữ) -> nhận ra loại tin người dùng hay bấm Bỏ qua
  function signature(text, pkg) {
    var f = fold(text || "").replace(/[^a-z ]/g, " ").replace(/\s+/g, " ").trim().split(" ").filter(function (w) { return w.length > 1; }).slice(0, 8).join(" ");
    return (pkg || "") + "|" + f;
  }
  function learnIgnore(brain, sig) {
    brain = brain || newBrain();
    if (sig) brain.ignore[sig] = (brain.ignore[sig] || 0) + 1;
    return brain;
  }
  function shouldIgnore(brain, sig) { return !!(brain && brain.ignore && sig && brain.ignore[sig] >= 2); }

  // Danh sách điều Mai đã học (để hiển thị / cho người dùng xoá)
  function learnedList(brain) {
    if (!brain || !brain.keys) return [];
    return Object.keys(brain.keys).map(function (k) {
      var it = brain.keys[k], t = topOf(it.c || {});
      return { key: k, label: it.l || k, category: t.name, weight: t.value, type: learnedType(brain, k), at: it.at || 0 };
    }).filter(function (x) { return x.category || x.type; }).sort(function (a, b) { return b.at - a.at || b.weight - a.weight; });
  }
  function forget(brain, key) {
    if (!brain || !brain.keys || !brain.keys[key]) return brain;
    var cats = brain.keys[key].c || {};
    tokenize(key).forEach(function (t) { var d = brain.tok[t]; if (d) for (var c in cats) delete d[c]; });
    delete brain.keys[key];
    brain.gone = brain.gone || {}; brain.gone[key] = Date.now();
    var g = Object.keys(brain.gone); if (g.length > 300) g.sort(function (a, b) { return brain.gone[a] - brain.gone[b]; }).slice(0, g.length - 300).forEach(function (x) { delete brain.gone[x]; });
    return brain;
  }


  // ---------- Đồng bộ & học cùng cộng đồng ----------
  // Gộp 2 bộ não (máy này + Firebase): đối tác nào sửa gần hơn thì theo bản đó, các đếm lấy lớn nhất (không cộng dồn
  // để đồng bộ nhiều lần không bị nhân đôi).
  function mergeBrain(a, b) {
    if (!a || !a.keys) return b && b.keys ? JSON.parse(JSON.stringify(b)) : newBrain();
    if (!b || !b.keys) return JSON.parse(JSON.stringify(a));
    var out = newBrain(), k, c;
    var keys = {}; for (k in a.keys) keys[k] = 1; for (k in b.keys) keys[k] = 1;
    for (k in keys) {
      var x = a.keys[k], y = b.keys[k];
      if (!x || !y) { out.keys[k] = JSON.parse(JSON.stringify(x || y)); continue; }
      var newer = (y.at || 0) > (x.at || 0) ? y : x;
      out.keys[k] = JSON.parse(JSON.stringify(newer));
    }
    [["tok", true], ["cats", true]].forEach(function (pair) {
      var f = pair[0], A = a[f] || {}, B = b[f] || {}, t;
      var all = {}; for (t in A) all[t] = 1; for (t in B) all[t] = 1;
      for (t in all) {
        var m = {}, aa = A[t] || {}, bb = B[t] || {};
        for (c in aa) m[c] = aa[c];
        for (c in bb) m[c] = Math.max(m[c] || 0, bb[c]);
        out[f][t] = m;
      }
    });
    var ig = {}; for (k in (a.ignore || {})) ig[k] = a.ignore[k]; for (k in (b.ignore || {})) ig[k] = Math.max(ig[k] || 0, b.ignore[k]);
    out.ignore = ig;
    // Đối tác đã bị "quên" ở máy kia: ghi nhận danh sách quên (giữ 300 mục gần nhất)
    var gone = {}; [a.gone || {}, b.gone || {}].forEach(function (g) { for (k in g) gone[k] = Math.max(gone[k] || 0, g[k]); });
    for (k in gone) if (out.keys[k] && (out.keys[k].at || 0) <= gone[k]) delete out.keys[k];
    out.gone = gone;
    out.n = Math.max(a.n || 0, b.n || 0);
    return out;
  }

  // Họ, tên đệm, tên phổ biến -> KHÔNG BAO GIỜ chia sẻ (tránh lộ tên người nhận/người gửi)
  var NAMES = {};
  ("nguyen tran le pham hoang huynh phan vu vo dang bui do ho ngo duong ly dinh trinh truong lam mai luong ha " +
   "cao tang chau ta quach lu thai kieu khuc doan la van thi duc huu cong minh anh hoa lan linh huong thu thao " +
   "trang ngoc hai hung dung tuan nam long son thanh trung kien hieu phuong quang khanh tam tien nga hang yen " +
   "nhung huyen loan giang vy my nhi tu quoc bao gia hoai kim tuyet xuan thuy diep hien hanh tai phat loc toan " +
   "dat thang vinh hoan an binh chi hong nhat nhan uyen tran ngan oanh trinh dieu nhu tuong luan khoa phong " +
   "duy tung viet hao hau sang quan cuong dong hoang em chi anh chu co bac ong ba me bo").split(" ").forEach(function (w) { NAMES[w] = 1; });

  function shareableTokens(text) {
    return tokenize(text).filter(function (t) {
      var parts = t.split(" ");
      if (parts.some(function (w) { return w.length < 3 || /\d/.test(w); })) return false;
      return !parts.some(function (w) { return NAMES[w]; });
    }).slice(0, 4);
  }

  // Điều đáng chia sẻ: chỉ những gì anh đã CHẤN CHỈNH (trọng số >= 3) -> [{ t: từ khoá, c: hạng mục }]
  function communityExport(brain, allowedCats) {
    var out = {}, list = [];
    if (!brain || !brain.keys) return list;
    Object.keys(brain.keys).forEach(function (k) {
      var it = brain.keys[k], top = topOf(it.c || {});
      if (!top.name || top.value < 3) return;
      if (allowedCats && allowedCats.indexOf(top.name) === -1) return;
      shareableTokens(it.l || "").concat(shareableTokens(k)).forEach(function (t) { if (!out[t]) { out[t] = top.name; list.push({ t: t, c: top.name }); } });
    });
    return list.slice(0, 300);
  }

  // Bảng cộng đồng: { từ khoá: { hạng mục: số người } } -> gợi ý khi nhiều người cùng xếp giống nhau
  function predictCommunity(table, text, type, okCat) {
    if (!table) return null;
    var votes = {};
    tokenize(text).forEach(function (t) {
      var d = table[t]; if (!d) return;
      var tot = 0, c; for (c in d) tot += d[c];
      for (c in d) if (!okCat || okCat(c)) {
        var v = votes[c] || (votes[c] = { users: 0, share: 0 });
        v.users = Math.max(v.users, d[c]); v.share = Math.max(v.share, d[c] / tot);
      }
    });
    var best = null;
    for (var c in votes) if (!best || votes[c].users > votes[best].users || (votes[c].users === votes[best].users && votes[c].share > votes[best].share)) best = c;
    if (!best || votes[best].users < 2 || votes[best].share < 0.6) return null;
    return { category: best, source: "community", strong: votes[best].users >= 3 && votes[best].share >= 0.75, users: votes[best].users };
  }

  // Thứ tự: điều người dùng đã dạy (đúng đối tác) -> học mạnh từ từ khoá -> luật có sẵn -> học yếu -> mặc định
  function categorize(ctx, type, opts, memKey) {
    opts = opts || {};
    if (opts.memory && memKey && opts.memory[memKey]) return { category: opts.memory[memKey], source: "memory" };
    var p = opts.brain ? predictCategory(opts.brain, ctx, type, memKey) : null;
    if (p && p.strong) return p;
    var com = opts.community ? predictCommunity(opts.community, ctx, type, opts.brain ? typeOk(opts.brain, type) : null) : null;
    if (com && com.strong) return com;
    var rule = suggestCategory(fold(ctx), type, null, null);
    if (rule.source === "rule") return rule;
    if (p) return p;
    if (com) return com;
    return rule;
  }

  // ---------- Hướng tiền ----------
  var INCOME_WORDS = /\b(nhan duoc|vua nhan|da nhan|nhan tien|nhan chuyen khoan|ghi co|cong tien|tien vao|tien ve|duoc cong|da cong|hoan tien|hoan tra|credit|received|bien dong tang|tang\b|nap tien vao vi|chuyen den tai khoan cua ban|nhan \d)/;
  var EXPENSE_WORDS = /\b(thanh toan|da chuyen|chuyen tien den|chuyen tien toi|chuyen khoan den|tru tien|bi tru|ghi no|rut tien|da tra|da mua|debit|paid|chi tieu|bien dong giam|giam\b|nap tien dien thoai|nap dt|phi\b)/;

  function directionFromWords(lower) {
    var inc = 0, exp = 0, m;
    var ri = new RegExp(INCOME_WORDS.source, "g"), re = new RegExp(EXPENSE_WORDS.source, "g");
    while ((m = ri.exec(lower))) inc++;
    while ((m = re.exec(lower))) exp++;
    if (inc > exp) return { type: "INCOME", strength: inc - exp };
    if (exp > inc) return { type: "EXPENSE", strength: exp - inc };
    return null;
  }

  // ---------- Nội dung / đối tác ----------
  var NOTE_MARKERS = /(?:\bnoi dung(?: giao dich| gd| chuyen khoan| ck| thanh toan)?|\bnd gd|\bnd ck|\bnd|\bsund|\bloi nhan|\bmo ta|\bdien giai|\bref|\bcontent|\bremark|\bghi chu|\bdescription|\bdesc)\b\s*[:\-]?\s*/g;

  function extractNote(text, lower) {
    var m, best = null;
    NOTE_MARKERS.lastIndex = 0;
    while ((m = NOTE_MARKERS.exec(lower))) { best = m; }
    if (!best) {
      // bắt đầu bằng marker đầu tiên thay vì cuối cùng nếu có nhiều? Dùng marker đầu tiên đủ dài
      return null;
    }
    // ưu tiên marker đầu tiên (ND thường chỉ có 1); nếu marker cuối là 'ref' và có 'nd' trước đó thì lấy 'nd'
    NOTE_MARKERS.lastIndex = 0;
    var first = NOTE_MARKERS.exec(lower);
    var pick = first || best;
    var start = pick.index + pick[0].length;
    var rest = text.slice(start);
    var stop = rest.search(/\||\n|\s+(?:SD|Số dư|So du|SDC)\b|\.\s+(?:SD|Số dư|So du)\b/i);
    var note = (stop === -1 ? rest : rest.slice(0, stop)).trim();
    note = note.replace(/[.\s]+$/, "");
    return note || null;
  }

  function cleanNote(note) {
    if (!note) return "";
    return note
      .replace(/\b(MBVCB|IBFT|FT\d{6,}|TKThe|CT DEN|CT DI|TRACE|REF)\b[:.\s]*\d*/gi, " ")
      .replace(/\b\d{6,}\b/g, " ")
      .replace(/[.\-_]{2,}/g, " ")
      .replace(/\s{2,}/g, " ")
      .trim()
      .replace(/^[.:\-\s]+|[.:\-\s]+$/g, "");
  }

  function extractCounterparty(text, lower) {
    // "từ NGUYEN VAN A", "đến TRAN THI B", "cho Highlands Coffee"
    var m = lower.match(/\b(tu|den|toi|cho|from|to)\s+(?!tai khoan|tk\b|ban\b|vi\b|so\b|ngay|luc)([a-z0-9&'.\- ]{2,40}?)(?=\s*(?:[.|,;\n]|$|loi nhan|noi dung|vao luc|luc\b|thanh cong|qua\b|tai\b|ngay\b))/);
    if (m) {
      var s = m.index + m[0].indexOf(m[2]);
      var name = text.slice(s, s + m[2].length).trim();
      if (name.length >= 2 && !/^\d+$/.test(name)) return name;
    }
    // "NGUYEN VAN A chuyen tien" trong nội dung chuyển khoản
    var up = text.match(/\b([A-ZĐ][A-ZĐÀ-Ỹ]+(?:\s+[A-ZĐ][A-ZĐÀ-Ỹ]+){1,5})\s+(?:chuyen|CHUYEN|chuyển|CK|ck|gui|GUI)\b/);
    if (up) return up[1].trim();
    return null;
  }

  // Nơi quẹt thẻ: "... tai CIRCLE K luc ...", "tại SHOPEE"
  function extractMerchant(text, lower) {
    var m = lower.match(/\b(?:tai|at)\s+(?!tk\b|tai khoan)([a-z0-9*&'.\- ]{2,40}?)(?=\s*(?:[.,;|\n]|$|luc\b|vao\b|ngay\b|quy doi|han muc|so du|sd\b))/);
    if (!m) return null;
    var s = m.index + m[0].indexOf(m[1]);
    return text.slice(s, s + m[1].length).trim();
  }

  function extractAccount(lower) {
    var m = lower.match(/\b(?:tk|tai khoan|stk|account|acct|the)\b[\s:.]*(?:so\s*)?([0-9x*.]{4,24}\d)/);
    if (!m) m = lower.match(/\b(\d{2,6}[x*.]{2,}\d{2,6})\b/);
    if (!m) return null;
    var digits = m[1].replace(/[^0-9]/g, "");
    return digits.length >= 3 ? digits.slice(-4) : null;
  }

  // ---------- Bộ lọc tin không phải giao dịch ----------
  var OTP_RE = /\b(otp|ma xac thuc|ma xac nhan|ma kich hoat|passcode|smart ?otp|ma giao dich la|ma bao mat)\b/;
  var PROMO_RE = /\b(uu dai|khuyen mai|giam gia|giam ngay|voucher|qua tang|hoan tien (len|toi) den|len den|chi tu|co hoi|trung thuong|mien phi|flash sale|sale\b|nhan ngay|tang ngay|dang ky ngay|mo the|vay tieu dung)\b/;
  var LOGIN_RE = /\b(dang nhap|login|thiet bi moi|doi mat khau|cap nhat ung dung|phien ban moi)\b/;
  var PENDING_RE = /\b(that bai|khong thanh cong|bi tu choi|da huy|failed|declined|dang xu ly|cho xu ly)\b/;
  // Dấu hiệu tiền thực sự đi vào / ra khỏi một tài khoản (chuyển khoản, nhận tiền, biến động số dư, quẹt thẻ...)
  var MOVE_RE = /\b(tk|stk|tai khoan|acc|account|the \*|the so|so du|sd|bien dong|ghi co|ghi no|chuyen tien|chuyen khoan|ck|nhan tien|nhan duoc|da nhan|vua nhan|da chuyen|vua chuyen|tu tk|den tk|nap tien|da nap|rut tien|thanh toan thanh cong|giao dich thanh cong|da thanh toan|vua thanh toan|hoan tien|ps|phat sinh|credit|debit|gd|giao dich)\b/;
  var REMIND_RE = /\b(nhac no|den han|sap den han|vui long thanh toan|du no|sao ke|han thanh toan|can thanh toan)\b/;

  function parse(input, opts) {
    opts = opts || {};
    var pkg = null, title = "", body = "", postTime = null;
    if (typeof input === "string") body = input;
    else if (input) { pkg = input.packageName || null; title = input.title || ""; body = input.text || input.body || ""; postTime = input.postTime || null; }
    var text = ((title ? title + "\n" : "") + body).normalize("NFC").replace(/\r/g, "").replace(/ /g, " ");
    var lower = fold(text);
    var result = { ok: false, raw: text, packageName: pkg };

    if (!text.trim()) return Object.assign(result, { reason: "empty" });

    var tokens = findMoneyTokens(lower);
    var balanceTok = null, fee = null, cands = [];
    tokens.forEach(function (t) {
      var pre = segmentBefore(lower, t.index - (t.sign ? 1 : 0));
      var preTrim = pre.replace(/[+\-\s]+$/, " ");
      if (BAL_PREFIX.test(preTrim) && (t.currency || t.sep) && t.digits < 13) { if (!balanceTok) balanceTok = t; t.isBalance = true; return; }
      if (FEE_PREFIX.test(preTrim) && !t.sign && cands.length) { fee = t; t.isFee = true; return; }
      var score = 0;
      if (t.sign) score += 4;
      if (AMT_PREFIX.test(preTrim)) score += 3;
      if (t.currency) score += 2;
      if (t.unit) score += 2;
      if (t.sep) score += 1;
      if (!t.currency && !t.sep && !t.sign && !t.unit) {
        if (!AMT_PREFIX.test(preTrim)) return; // số trơn không có ngữ cảnh -> có thể là STK, mã GD
        if (t.digits > 9) return;
      }
      if (t.digits >= 13) return;
      t.score = score;
      cands.push(t);
    });

    var isOtp = OTP_RE.test(lower);
    var isPromo = PROMO_RE.test(lower);
    var isLogin = LOGIN_RE.test(lower);
    var strongTxSignal = !!balanceTok || cands.some(function (c) { return c.sign; }) || /\b(so tien|ps|phat sinh|giao dich|bien dong so du|thanh toan thanh cong|da nhan|vua nhan|nhan duoc|da chuyen|vua chuyen|da nap|vua thanh toan|da thanh toan|hoan tien|chi tieu|thay doi|rut tien)\b/.test(lower);

    if (isOtp && !balanceTok) return Object.assign(result, { reason: "otp" });
    if (PENDING_RE.test(lower)) return Object.assign(result, { reason: "failed_or_pending" });
    if (REMIND_RE.test(lower) && !cands.some(function (c) { return c.sign; })) return Object.assign(result, { reason: "reminder" });
    if ((isPromo || isLogin) && !balanceTok && !cands.some(function (c) { return c.sign; }) && !/\b(da thanh toan|thanh toan thanh cong|da nhan|vua nhan|nhan duoc|da chuyen)\b/.test(lower)) {
      return Object.assign(result, { reason: isPromo ? "promo" : "account_notice" });
    }
    if (!cands.length) return Object.assign(result, { reason: "no_amount" });
    // Chỉ nhận tin có tiền chuyển đi/đến một tài khoản; tin có số tiền khác (đơn hàng, quảng cáo, hóa đơn...) bỏ qua
    if (!opts.loose && !balanceTok && !cands.some(function (c) { return c.sign; }) && !extractAccount(lower) && !MOVE_RE.test(lower)) {
      return Object.assign(result, { reason: "not_transfer" });
    }

    // Thông báo từ app lạ (chat, mạng xã hội...) mà không có dấu hiệu ngân hàng -> không phải biến động số dư
    if (pkg && pkg !== "share" && !isKnownPackage(pkg) && !bankFromText(lower) && !balanceTok && !extractAccount(lower)) {
      return Object.assign(result, { reason: "not_bank_source" });
    }
    cands.sort(function (a, b) { return b.score - a.score || a.index - b.index; });
    var pick = cands[0];

    // hướng tiền
    var type = null, dirSource = null;
    if (pick.sign) { type = pick.sign > 0 ? "INCOME" : "EXPENSE"; dirSource = "sign"; }
    if (!type && /\bnap\b[^|\n]{0,40}\bvao (vi|tai khoan)\b/.test(lower)) { type = "INCOME"; dirSource = "prefix"; }
    if (!type) {
      var pre2 = segmentBefore(lower, pick.index);
      if (/\b(tang|cong tien|duoc cong|ghi co|nhan(?: duoc)?|credit|hoan tien|hoan)\b[^0-9]{0,18}$/.test(pre2)) { type = "INCOME"; dirSource = "prefix"; }
      else if (/\b(giam|tru|ghi no|thanh toan|chuyen(?: khoan| tien)?|rut|debit|nap)\b[^0-9]{0,18}$/.test(pre2)) { type = "EXPENSE"; dirSource = "prefix"; }
    }
    if (!type) {
      var w = directionFromWords(lower);
      if (w) { type = w.type; dirSource = "words"; }
    }
    // Quẹt thẻ tại cửa hàng ("The *1234 giao dich ... tai NETFLIX") là khoản chi
    if (!type && /\b(the|card|visa|mastercard)\b/.test(lower) && /\b(tai|at)\s+[a-z0-9]/.test(lower)) { type = "EXPENSE"; dirSource = "prefix"; }
    if (!type) { type = "EXPENSE"; dirSource = "default"; }

    var bank = bankFromPackage(pkg) || bankFromText(lower);
    var rawNote = extractNote(text, lower);
    var counterparty = extractCounterparty(text, lower);
    var merchant = extractMerchant(text, lower);
    var bankNm = bank || "";
    var note = cleanNote(rawNote) || (counterparty ? ((type === "INCOME" ? "Từ " : "Cho ") + counterparty) : "") || (merchant ? merchant : "");
    // Không có nội dung / nơi giao dịch: ghi ngắn gọn, không chép nguyên thông báo
    var generic = !note;
    if (!note) note = (type === "INCOME" ? "Nhận tiền" : "Chi tiêu") + (bankNm ? " · " + bankNm : "");
    var when = parseDateTime(lower.replace(/\b(?:sd|so du|tk|stk)[^|\n]{0,40}/g, function (s) { return s.replace(/\d/g, "#"); }), postTime || opts.now || null);
    var accountTail = extractAccount(lower);

    var memKey = memoryKeyFrom(counterparty, rawNote || merchant);
    var keyKind = memKey ? "content" : null;
    // Không có nội dung gì: nhớ theo "ngân hàng + số tiền" (gói cước, thuê bao... thường lặp đúng số tiền).
    // Bỏ qua số tròn (50.000, 100.000...) vì quá nhiều khoản khác nhau trùng số đó.
    // Ưu tiên nội dung chuyển khoản / nơi giao dịch; chỉ khi không ra mới xét cả thông báo
    var noteCtx = (rawNote || "") + " " + (counterparty || "") + " " + (merchant || "");
    // Mai đã được dạy chiều tiền cho đối tác này (chỉ áp dụng khi thông báo không có dấu +/-)
    var lt = dirSource !== "sign" ? learnedType(opts.brain, memKey) : null;
    if (lt && lt !== type) { type = lt; dirSource = "learned"; }
    if (!memKey && pick.value >= 20000 && pick.value % 50000 !== 0) { memKey = "amt:" + (fold(bankNm).replace(/[^a-z0-9]/g, "") || "?") + ":" + pick.value + ":" + (type === "INCOME" ? "I" : "E"); keyKind = "amount"; }
    var cat = categorize(noteCtx, type, opts, memKey);
    if (cat.source === "default") cat = categorize((title || "") + " " + body, type, { brain: opts.brain, community: opts.community }, null);
    // Chắc chắn hạng mục? (luật rõ ràng, hoặc anh đã dạy). Không chắc thì Mai hỏi anh, không đoán bừa.
    var certain = cat.category !== "Khác" && cat.category !== "Lương & Thu nhập" && dirSource !== "default" &&
      (cat.source === "rule" || cat.source === "memory" || (cat.source === "learned-key" && (keyKind !== "amount" || (cat.weight || 0) >= 3)) || (cat.source === "learned" && cat.strong) || (cat.source === "community" && cat.strong));

    // độ tin cậy
    var conf = 0.35;
    if (dirSource === "sign") conf += 0.3; else if (dirSource === "prefix" || dirSource === "learned") conf += 0.22; else if (dirSource === "words") conf += 0.15;
    if (bank) conf += 0.12;
    if (balanceTok) conf += 0.12;
    if (accountTail) conf += 0.05;
    if (pick.currency || pick.unit) conf += 0.06;
    if (/\b(thanh toan thanh cong|da thanh toan|vua nhan|da nhan|nhan duoc|da chuyen|vua chuyen|da nap|chuyen tien thanh cong|giao dich thanh cong|hoan tien)\b|thanh cong/.test(lower)) conf += 0.1;
    if (cands.length > 1 && cands[1].score === pick.score && cands[1].value !== pick.value) conf -= 0.25; // 2 số tiền ngang nhau -> mơ hồ
    if (isPromo) conf -= 0.15;
    if (!strongTxSignal) conf -= 0.1;
    if (pkg && !isKnownPackage(pkg)) conf -= 0.05;
    conf = Math.max(0.05, Math.min(0.99, conf));

    return Object.assign(result, {
      ok: true,
      amount: pick.value,
      type: type,
      bank: bank,
      balance: balanceTok ? balanceTok.value : null,
      fee: fee ? fee.value : null,
      accountTail: accountTail,
      note: note,
      rawNote: rawNote,
      counterparty: counterparty,
      date: when.date.toISOString(),
      dateExplicit: when.explicit,
      category: cat.category,
      categorySource: cat.source,
      memoryKey: memKey,
      keyKind: keyKind,
      learnText: (noteCtx || "").trim(),
      genericNote: generic,
      certain: !!certain,
      directionSource: dirSource,
      // khuôn tin dựa vào nội dung giao dịch (không phải phần đầu chung của ngân hàng) + chiều tiền
      signature: signature((rawNote || counterparty || merchant || text), (bank || pkg || "")) + "|" + type,
      confidence: Math.round(conf * 100) / 100
    });
  }

  // ---------- Nhập nhanh bằng câu nói ----------
  var QUICK_RE = /(\d+(?:[.,]\d+)*)\s*(k|nghin|ngan|ng|tr|trieu|cu|ty|ti|d|dong|vnd)?(\d{1,3})?(?![a-z0-9])/g;
  function parseQuick(text, opts) {
    opts = opts || {};
    var src = String(text || "").normalize("NFC").trim();
    var lower = fold(src);
    var m, pick = null;
    QUICK_RE.lastIndex = 0;
    while ((m = QUICK_RE.exec(lower))) {
      var unit = m[2] || "";
      var v;
      if (/^(k|nghin|ngan|ng|tr|trieu|cu|ty|ti)$/.test(unit)) v = parseSpokenMoney(m[1].replace(/[.,](?=\d{3})/g, ""), unit, m[3]);
      else v = parseMoney(m[1]);
      if (!(v > 0)) continue;
      if (!unit && v < 1000) v = v * 1000; // "cafe 35" -> 35.000đ (thói quen nói tắt)
      pick = { value: v, index: m.index, len: m[0].length };
      break;
    }
    if (!pick) return { ok: false, reason: "no_amount", raw: src };
    var type = /\b(nhan|thu|luong|thuong|duoc cho|ban duoc|lai|hoan tien|tien ve|income)\b/.test(lower) && !/\b(tra|mua|chi|nop|dong)\b/.test(lower.replace(/\bthu nhap\b/, "")) ? "INCOME" : "EXPENSE";
    var note = (src.slice(0, pick.index) + " " + src.slice(pick.index + pick.len)).replace(/\s{2,}/g, " ").replace(/^[\s,.:\-]+|[\s,.:\-]+$/g, "");
    var memKey = memoryKeyFrom(null, note);
    var lt = learnedType(opts.brain, memKey);
    if (lt) type = lt;
    var cat = categorize(note, type, opts, memKey);
    var bank = null;
    (opts.accounts || []).forEach(function (a) { if (!bank && a && a.name && lower.indexOf(fold(a.name)) !== -1) bank = a.name; });
    return { ok: true, amount: pick.value, type: type, note: note, category: cat.category, categorySource: cat.source, memoryKey: memKey, bank: bank, date: new Date().toISOString(), confidence: 0.9, raw: src };
  }

  function memoryKeyFrom(counterparty, note) {
    var s = fold(counterparty || note || "").replace(/[^a-z0-9 ]/g, " ").replace(/\b\d+\b/g, " ").replace(/\s+/g, " ").trim();
    if (!s) return null;
    return s.split(" ").slice(0, 3).join(" ");
  }

  function fingerprint(r) {
    if (!r || !r.ok) return null;
    var minute = r.date ? Math.floor(new Date(r.date).getTime() / 60000) : 0;
    return [r.bank || "?", r.type, r.amount, r.balance === null || r.balance === undefined ? "" : r.balance, minute].join("|");
  }

  // Hai thông báo có phải cùng 1 giao dịch không (SMS + app, hoặc app đăng lại thông báo)
  function isDuplicate(a, b, windowMs) {
    if (!a || !b) return false;
    windowMs = windowMs || 5 * 60000;
    if (a.amount !== b.amount || a.type !== b.type) return false;
    if (a.bank && b.bank && a.bank !== b.bank) return false;
    if (a.balance != null && b.balance != null) return a.balance === b.balance;
    return Math.abs(new Date(a.date) - new Date(b.date)) <= windowMs;
  }


  // ---------- Trò chuyện: hiểu câu anh dặn Mai ----------
  function squash(s) { return fold(s).replace(/[^a-z0-9]+/g, " ").trim(); }
  // Tìm hạng mục khớp nhất với cụm từ anh gõ ("an uong", "cafe", "di chuyen"...)
  function matchCategory(target, names) {
    var t = squash(target).replace(/^(muc|hang muc|danh muc|loai|nhom)\s+/, "");
    if (!t) return null;
    var best = null, bestScore = 0;
    (names || []).forEach(function (n) {
      var f = squash(n), sc = 0;
      if (!f) return;
      if (f === t) sc = 100;
      else if (f.replace(/ /g, "") === t.replace(/ /g, "")) sc = 95;
      else if (f.indexOf(t) === 0) sc = 80;
      else {
        var parts = fold(n).split(/[&,\/]| va /).map(squash).filter(Boolean);
        if (parts.some(function (p) { return p === t; })) sc = 85;
        else if (parts.some(function (p) { return p.indexOf(t) === 0 || t.indexOf(p) === 0; })) sc = 60;
        else if (t.length >= 3 && f.indexOf(t) >= 0) sc = 50;
      }
      if (sc > bestScore || (sc === bestScore && sc > 0 && best && n.length < best.length)) { bestScore = sc; best = n; }
    });
    return bestScore >= 50 ? best : null;
  }
  var TYPE_WORDS = { INCOME: /^(thu|thu nhap|khoan thu|tien vao|tien ve|duoc nhan|tien nhan)$/, EXPENSE: /^(chi|khoan chi|chi tieu|tien ra|tien chi)$/ };
  function cleanSubject(x) {
    return squash(x).replace(/^(tin|thong bao|giao dich|khoan|cac khoan|nhung khoan|chuyen khoan|chuyen tien|ck|tien chuyen)\s+/, "").replace(/^(tu|cua|co chu|ve|chua chu|gui toi|toi|den)\s+/, "")
      .replace(/^(tin|thong bao)\s+/, "").replace(/\s+di$/, "").trim();
  }
  // Trả về { intent, ... }: teach | teach_type | block | unblock | forget | ask | list | help | greet | thanks | txn | unknown
  function chatIntent(text, categoryNames) {
    var raw = String(text || "").trim();
    // bỏ lời gọi đầu câu và tiểu từ cuối câu (nhé, nha, ạ) – không đụng chữ "nhà"
    var tail = raw.normalize("NFC").replace(/[\s.!?,~]+$/, "").replace(/(\s*,?\s*(nhé|nha|nhá|ạ|nhe|nhen)(\s+(mai|em))?)+$/i, "");
    var f = squash(tail).replace(/^(mai oi|mai|em oi)\s+/, "");
    if (!f) return { intent: "unknown" };
    if (/^(chao|xin chao|hi|hello|alo)\b/.test(f) && f.split(" ").length <= 4) return { intent: "greet" };
    if (/\b(cam on|thank|thanks|tot lam|gioi lam)\b/.test(f) && f.split(" ").length <= 6) return { intent: "thanks" };
    if (/\b(huong dan|giup|lam duoc gi|biet lam gi|help|cach day|day the nao)\b/.test(f)) return { intent: "help" };
    if (/\b(da hoc|hoc duoc|nho duoc|nho gi|biet gi|hoc gi)\b/.test(f)) return { intent: "list" };
    var m;
    if ((m = f.match(/^(?:bo chan|cho phep|ghi lai|van ghi|doc lai|lai ghi)\s+(.+)$/))) {
      var sub2 = cleanSubject(m[1]);
      if (sub2) return { intent: "unblock", word: sub2 };
    }
    if ((m = f.match(/^(?:dung|khong|dung co|khong duoc|cam)\s+(?:ghi|doc|nhan|lay|dua)(?:\s+vao)?\s+(.+)$/)) || (m = f.match(/^(?:bo qua|chan|loai bo|loai|bo)\s+(.+)$/))) {
      var sub = cleanSubject(m[1]);
      if (sub) return { intent: "block", word: sub };
    }
    if ((m = f.match(/^(?:quen|xoa|bo nho)\s+(?:di\s+)?(?:dieu\s+|viec\s+)?(.+)$/))) {
      var k = cleanSubject(m[1]);
      if (k) return { intent: "forget", key: k };
    }
    if ((m = f.match(/^(.+?)\s+(?:la|thuoc|vao)\s+(?:muc|hang muc|danh muc)?\s*(?:gi|nao)$/))) return { intent: "ask", key: cleanSubject(m[1]) };
    var subj = null, target = null;
    if ((m = f.match(/^(?:xep|cho|ghi|dua|tinh)\s+(.+?)\s+(?:vao|la|thanh|sang)\s+(?:muc|hang muc|danh muc)?\s*(.+)$/))) { subj = m[1]; target = m[2]; }
    else if ((m = f.match(/^(.+?)\s+(?:la|thuoc|tinh la|tinh vao|ghi vao|xep vao|cho vao|vao|la khoan|la tien)\s+(?:muc|hang muc|danh muc)?\s*(.+)$/))) { subj = m[1]; target = m[2]; }
    // câu hỏi ("tiêu nhiều nhất vào đâu?", "... là bao nhiêu") không phải câu dạy
    var isQuestion = /\?\s*$/.test(raw) || /\b(dau|gi|nao|bao nhieu|khong|chua|sao|may|ko)$/.test(f) || /\b(bao nhieu|nhieu nhat|it nhat|tai sao|the nao)\b/.test(f);
    if (isQuestion) { subj = null; target = null; }
    if (subj && target) {
      var s2 = cleanSubject(subj);
      var tg = squash(target);
      // "khoản 149k hôm qua là Giải trí" -> sửa giao dịch đã ghi (và Mai nhớ luôn)
      var amtM = s2.match(/(\d+(?:[.,]\d+)*)\s*(k|nghin|ngan|tr|trieu|cu|d|dong|vnd)?(\d{1,3})?\b/);
      if (amtM) {
        var unit = amtM[2] || "", v;
        if (/^(k|nghin|ngan|tr|trieu|cu)$/.test(unit)) v = parseSpokenMoney(amtM[1].replace(/[.,](?=\d{3})/g, ""), unit, amtM[3]);
        else v = parseMoney(amtM[1]);
        if (!unit && v < 1000) v = v * 1000;
        if (v > 0) {
          if (TYPE_WORDS.INCOME.test(tg)) return { intent: "fix_tx", amount: v, type: "INCOME" };
          if (TYPE_WORDS.EXPENSE.test(tg)) return { intent: "fix_tx", amount: v, type: "EXPENSE" };
          var fc = matchCategory(target, categoryNames);
          if (fc) return { intent: "fix_tx", amount: v, category: fc };
        }
      }
      if (s2) {
        if (TYPE_WORDS.INCOME.test(tg)) return { intent: "teach_type", key: s2, type: "INCOME" };
        if (TYPE_WORDS.EXPENSE.test(tg)) return { intent: "teach_type", key: s2, type: "EXPENSE" };
        var cat = matchCategory(target, categoryNames);
        if (cat && !/\d/.test(s2.replace(/\s/g, "").slice(-3)) ) return { intent: "teach", key: s2, category: cat };
        if (!findMoneyTokens(fold(raw)).length) return { intent: "teach_unknown", key: s2, target: target.trim() };
      }
    }
    if (findMoneyTokens(fold(raw)).length || /\d/.test(raw)) return { intent: "txn" };
    return { intent: "unknown" };
  }
  // Tin có chứa cụm từ anh đã dặn bỏ qua?
  function isBlocked(text, pkg, words) {
    if (!words || !words.length) return null;
    var hay = " " + squash((pkg || "") + " " + (text || "")) + " ";
    for (var i = 0; i < words.length; i++) { var w = squash(words[i]); if (w && hay.indexOf(" " + w + " ") >= 0) return words[i]; }
    return null;
  }

  var MaiAgent = {
    version: "1.3.0",
    newBrain: newBrain,
    learn: learn,
    predictCategory: predictCategory,
    learnedType: learnedType,
    learnIgnore: learnIgnore,
    shouldIgnore: shouldIgnore,
    learnedList: learnedList,
    forget: forget,
    signature: signature,
    tokenize: tokenize,
    mergeBrain: mergeBrain,
    communityExport: communityExport,
    predictCommunity: predictCommunity,
    shareableTokens: shareableTokens,
    parse: parse,
    chatIntent: chatIntent,
    matchCategory: matchCategory,
    isBlocked: isBlocked,
    parseQuick: parseQuick,
    parseMoney: parseMoney,
    fingerprint: fingerprint,
    isDuplicate: isDuplicate,
    memoryKey: memoryKeyFrom,
    bankFromPackage: bankFromPackage,
    fold: fold,
    BANKS: BANKS,
    AUTO_SAVE_THRESHOLD: 0.75
  };
  if (typeof module !== "undefined" && module.exports) module.exports = MaiAgent;
  root.MaiAgent = MaiAgent;
})(typeof window !== "undefined" ? window : globalThis);
