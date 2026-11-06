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

  var BAL_PREFIX = /(?:\bso du|\bsd|\bsdc|\bsdkd|\bbalance|\bbal|\bsdcuoi|\bsodu)\b[^0-9+\-]{0,26}$/;
  var AMT_PREFIX = /(?:\bso tien(?: gd| giao dich)?|\bgd|\bps|\bphat sinh|\bgiao dich|\bst|\bamount|\bthay doi|\bbien dong|\btang|\bgiam|\bthanh toan|\bnhan(?: duoc)?|\bchuyen(?: khoan| tien)?|\btru|\bcong|\bghi co|\bghi no|\brut|\bnap)\b[^0-9]{0,18}$/;
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
    { cat: "Phí dịch vụ & Ngân hàng", re: /\b(phi (sms|dich vu|thuong nien|duy tri|quan ly|chuyen tien|giao dich|rut tien)|sms ?banking|phi\b.*\btk\b|annual fee|service fee|phi thanh toan)\b/ },
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
    { cat: "Bảo hiểm & Thuế", re: /\b(bao hiem|prudential|manulife|aia|bao viet|thue|tax|bhxh|bhyt)\b/ },
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

  function extractAccount(lower) {
    var m = lower.match(/\b(?:tk|tai khoan|stk|account|acct|the)\b[\s:.]*(?:so\s*)?([0-9x*.]{4,24}\d)/);
    if (!m) m = lower.match(/\b(\d{2,6}[x*.]{2,}\d{2,6})\b/);
    if (!m) return null;
    var digits = m[1].replace(/[^0-9]/g, "");
    return digits.length >= 3 ? digits.slice(-4) : null;
  }

  // ---------- Bộ lọc tin không phải giao dịch ----------
  var OTP_RE = /\b(otp|ma xac thuc|ma xac nhan|ma kich hoat|passcode|smart ?otp|ma giao dich la|ma bao mat)\b/;
  var PROMO_RE = /\b(uu dai|khuyen mai|giam gia|giam ngay|voucher|qua tang|hoan tien (len|toi) den|len den|chi tu|co hoi|trung thuong|mien phi|flash sale|sale\b|nhan ngay|tang ngay|dang ky ngay|mo the|vay tieu dung|han muc)\b/;
  var LOGIN_RE = /\b(dang nhap|login|thiet bi moi|doi mat khau|cap nhat ung dung|phien ban moi)\b/;
  var PENDING_RE = /\b(that bai|khong thanh cong|bi tu choi|da huy|failed|declined|dang xu ly|cho xu ly)\b/;
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
    var strongTxSignal = !!balanceTok || cands.some(function (c) { return c.sign; }) || /\b(so tien|ps|phat sinh|giao dich|bien dong so du|thanh toan thanh cong|da nhan|vua nhan|nhan duoc|da chuyen)\b/.test(lower);

    if (isOtp && !balanceTok) return Object.assign(result, { reason: "otp" });
    if (PENDING_RE.test(lower)) return Object.assign(result, { reason: "failed_or_pending" });
    if (REMIND_RE.test(lower) && !balanceTok && !cands.some(function (c) { return c.sign; })) return Object.assign(result, { reason: "reminder" });
    if ((isPromo || isLogin) && !balanceTok && !cands.some(function (c) { return c.sign; }) && !/\b(da thanh toan|thanh toan thanh cong|da nhan|vua nhan|nhan duoc|da chuyen)\b/.test(lower)) {
      return Object.assign(result, { reason: isPromo ? "promo" : "account_notice" });
    }
    if (!cands.length) return Object.assign(result, { reason: "no_amount" });

    cands.sort(function (a, b) { return b.score - a.score || a.index - b.index; });
    var pick = cands[0];

    // hướng tiền
    var type = null, dirSource = null;
    if (pick.sign) { type = pick.sign > 0 ? "INCOME" : "EXPENSE"; dirSource = "sign"; }
    if (!type) {
      var pre2 = segmentBefore(lower, pick.index);
      if (/\b(tang|cong tien|duoc cong|ghi co|nhan(?: duoc)?|credit)\b[^0-9]{0,18}$/.test(pre2)) { type = "INCOME"; dirSource = "prefix"; }
      else if (/\b(giam|tru|ghi no|thanh toan|chuyen(?: khoan| tien)?|rut|debit|nap)\b[^0-9]{0,18}$/.test(pre2)) { type = "EXPENSE"; dirSource = "prefix"; }
    }
    if (!type) {
      var w = directionFromWords(lower);
      if (w) { type = w.type; dirSource = "words"; }
    }
    if (!type) { type = "EXPENSE"; dirSource = "default"; }

    var bank = bankFromPackage(pkg) || bankFromText(lower);
    var rawNote = extractNote(text, lower);
    var counterparty = extractCounterparty(text, lower);
    var note = cleanNote(rawNote) || (counterparty ? ((type === "INCOME" ? "Từ " : "Cho ") + counterparty) : "");
    if (!note) {
      var t1 = (title || "").trim();
      note = t1 && t1.length < 60 ? t1 : "";
    }
    var when = parseDateTime(lower.replace(/\b(?:sd|so du|tk|stk)[^|\n]{0,40}/g, function (s) { return s.replace(/\d/g, "#"); }), postTime || opts.now || null);
    var accountTail = extractAccount(lower);

    var lowerNoteCtx = fold((rawNote || "") + " " + (counterparty || "") + " " + (title || "") + " " + body);
    var memKey = memoryKeyFrom(counterparty, rawNote);
    var cat = suggestCategory(lowerNoteCtx, type, opts.memory, memKey);

    // độ tin cậy
    var conf = 0.35;
    if (dirSource === "sign") conf += 0.3; else if (dirSource === "prefix") conf += 0.22; else if (dirSource === "words") conf += 0.15;
    if (bank) conf += 0.12;
    if (balanceTok) conf += 0.12;
    if (accountTail) conf += 0.05;
    if (pick.currency || pick.unit) conf += 0.06;
    if (/\b(thanh toan thanh cong|da thanh toan|vua nhan|da nhan|nhan duoc|da chuyen|chuyen tien thanh cong|giao dich thanh cong)\b|thanh cong/.test(lower)) conf += 0.1;
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
      directionSource: dirSource,
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
    var cat = suggestCategory(fold(note), type, opts.memory, memKey);
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

  var MaiAgent = {
    version: "1.0.0",
    parse: parse,
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
