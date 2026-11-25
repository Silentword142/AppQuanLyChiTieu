/* Spendy – đọc ảnh giao dịch trên máy (không cần Gemini).
 * 1) Làm sạch ảnh: cắt thanh trạng thái, phóng chữ nhỏ, xám hoá, đảo nền tối, kéo giãn tương phản.
 * 2) Tesseract đọc ra từng dòng kèm vị trí + cỡ chữ.
 * 3) Đọc theo bố cục: số tiền là số có ngữ cảnh tiền (đ/VND/dấu +-/nhãn "Số tiền") và chữ to nhất,
 *    bỏ số dư, phí, số tài khoản, mã giao dịch, ngày giờ. Ảnh lịch sử nhiều dòng +/- thì tách thành nhiều khoản.
 */
(function (root) {
  "use strict";

  var MAP = { "đ": "d", "Đ": "d" };
  function fold(s) {
    return String(s || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[đĐ]/g, function (c) { return MAP[c]; }).toLowerCase();
  }

  // ---------- 1) Làm sạch ảnh ----------
  function loadImage(src) {
    return new Promise(function (resolve, reject) {
      if (src && src.naturalWidth) return resolve(src);
      var url = typeof src === "string" ? src : URL.createObjectURL(src);
      var img = new Image();
      img.onload = function () { if (typeof src !== "string") URL.revokeObjectURL(url); resolve(img); };
      img.onerror = function () { if (typeof src !== "string") URL.revokeObjectURL(url); reject(new Error("Không đọc được ảnh")); };
      img.src = url;
    });
  }

  var GAMMA = (function () { var t = new Uint8Array(256); for (var i = 0; i < 256; i++) { var v = Math.round(255 * Math.pow(i / 255, 2.2)); t[i] = v > 160 ? 255 : v; } return t; })();
  function prepare(img, plain) {
    var w = img.naturalWidth || img.width, h = img.naturalHeight || img.height;
    // ảnh chụp màn hình điện thoại (dọc, cao) -> bỏ thanh trạng thái (giờ, pin) để không lẫn giờ hệ thống
    var top = h / w > 1.55 ? Math.round(h * 0.035) : 0;
    var ch = h - top;
    // chữ quá nhỏ thì phóng lên, ảnh quá lớn thì thu lại cho nhanh
    var k = w < 1000 ? Math.min(2.5, 1400 / w) : w > 2200 ? 2000 / w : 1;
    var c = document.createElement("canvas");
    c.width = Math.round(w * k); c.height = Math.round(ch * k);
    var g = c.getContext("2d", { willReadFrequently: true });
    g.imageSmoothingQuality = "high";
    g.fillStyle = "#fff"; g.fillRect(0, 0, c.width, c.height);
    g.drawImage(img, 0, top, w, ch, 0, 0, c.width, c.height);
    var id = g.getImageData(0, 0, c.width, c.height), d = id.data, n = d.length / 4;
    var hist = new Uint32Array(256), sum = 0, i, y;
    for (i = 0; i < d.length; i += 4) {
      y = (d[i] * 299 + d[i + 1] * 587 + d[i + 2] * 114) / 1000 | 0;
      d[i] = y; hist[y]++; sum += y;
    }
    var dark = sum / n < 110;
    // kéo giãn tương phản theo phân vị 1% – 99%
    var lo = 0, hi = 255, acc = 0, cut = n * 0.01;
    for (i = 0; i < 256; i++) { acc += hist[i]; if (acc > cut) { lo = i; break; } }
    acc = 0;
    for (i = 255; i >= 0; i--) { acc += hist[i]; if (acc > cut) { hi = i; break; } }
    var span = Math.max(1, hi - lo);
    for (i = 0; i < d.length; i += 4) {
      y = Math.max(0, Math.min(255, (d[i] - lo) * 255 / span | 0));
      if (dark) y = 255 - y;
      if (!plain) y = GAMMA[y]; // làm đậm chữ xám nhạt (tiêu đề ngày, dòng phụ); nền xám nhạt của thẻ -> trắng (Tesseract hay bỏ cả khối nền xám)
      d[i] = d[i + 1] = d[i + 2] = y; d[i + 3] = 255;
    }
    g.putImageData(id, 0, 0);
    return { canvas: c, scale: k, dark: dark };
  }

  // ---------- 2) Đọc chữ ----------
  // Giữ 1 worker để các ảnh sau đọc nhanh; tự đóng sau 90 giây rảnh cho nhẹ máy.
  // Quan trọng: tesseract.js mặc định coi cả ảnh là 1 khối chữ (PSM 6) nên hay bỏ sót dòng chữ to tách riêng
  // (chính là dòng số tiền). Dùng PSM 3 = tự chia bố cục như màn hình app ngân hàng.
  var worker = null, workerKey = "", idleTimer = null;
  async function getWorker(Tesseract, opts) {
    var key = JSON.stringify(opts || {});
    if (worker && workerKey === key) return worker;
    if (worker) { try { await (await worker).terminate(); } catch (e) {} }
    workerKey = key;
    worker = (async function () {
      var w = await Tesseract.createWorker("vie+eng", 1, Object.assign({ logger: function () {} }, opts || {}));
      await w.setParameters({ tessedit_pageseg_mode: "3", preserve_interword_spaces: "1" });
      return w;
    })();
    worker.catch(function () { worker = null; });
    return worker;
  }
  function scheduleIdle() {
    clearTimeout(idleTimer);
    idleTimer = setTimeout(function () { var w = worker; worker = null; if (w) w.then(function (x) { x.terminate(); }).catch(function () {}); }, 90000);
  }

  async function readPrepared(p, Tesseract, opts) {
    var data;
    if (Tesseract.createWorker) {
      var w = await getWorker(Tesseract, opts);
      clearTimeout(idleTimer);
      try { data = (await w.recognize(p.canvas)).data || {}; } finally { scheduleIdle(); }
    } else {
      data = (await Tesseract.recognize(p.canvas, "vie+eng", Object.assign({ logger: function () {} }, opts || {}))).data || {};
    }
    var lines = (data.lines || []).map(function (l) {
      var b = l.bbox || { x0: 0, y0: 0, x1: 0, y1: 0 };
      return { text: (l.text || "").replace(/\s+$/, "").replace(/ {2,}/g, " "), x0: b.x0, y0: b.y0, x1: b.x1, y1: b.y1, h: b.y1 - b.y0, conf: l.confidence };
    }).filter(function (l) { return l.text.trim(); });
    if (!lines.length && data.text) lines = linesFromText(data.text);
    return lines;
  }

  async function read(file, Tesseract, opts) {
    var img = await loadImage(file);
    var p = prepare(img);
    var lines = await readPrepared(p, Tesseract, opts);
    // ít chữ quá -> đọc lại bản xử lý nhẹ hơn, lấy bản nhiều chữ hơn
    if (lines.length < 3) {
      var l2 = await readPrepared(prepare(img, true), Tesseract, opts);
      if (l2.length > lines.length) lines = l2;
    }
    return { text: lines.map(function (l) { return l.text; }).join("\n"), lines: lines, dark: p.dark };
  }

  function linesFromText(text) {
    return String(text || "").split(/\n/).filter(function (t) { return t.trim(); }).map(function (t, i) { return { text: t, x0: 0, y0: i * 40, x1: 1000, y1: i * 40 + 30, h: 30 }; });
  }

  // ---------- 3) Đọc theo bố cục ----------
  var AMT_LABEL = /\b(t[o0][a-z]{1,2}g\s?c[o0q]ng|so tien|tong tien|tong cong|tong thanh toan|thanh tien|tong|amount|total|gia tri|so tien giao dich|so tien chuyen|so tien thanh toan|so tien nhan)\b/;
  var BAL = /\b(so du|sd|balance|kha dung|han muc|du no|so du cuoi|so du kha dung)\b/;
  var FEE = /\b(phi|fee|vat|thue|giam gia|chiet khau|khuyen mai|discount|tien khach dua|khach dua|tien thoi|tien thua|tien mat nhan|change|cash)\b/;
  var IDLINE = /\b(stk|so tk|tai khoan|tk nhan|tk nguon|account|ma gd|ma giao dich|ma tham chieu|so tham chieu|ref|trace|so the|sdt|so dien thoai|hotline|ma don|ma hoa don|invoice|ftid|ft\d)\b/;
  var IN_WORDS = /\b(nhan tien|da nhan|nhan duoc|tien vao|chuyen den|ghi co|hoan tien|cong tien|thu nhap|received|credit|nap tien vao)\b/;
  var OUT_WORDS = /\b(chuyen tien|da chuyen|chuyen khoan thanh cong|thanh toan|tru tien|ghi no|rut tien|chuyen di|debit|paid|mua|da thanh toan)\b/;
  var NOTE_LABEL = /^(.*?)(noi dung|loi nhan|mo ta|dien giai|description|message|ghi chu)\s*(chuyen khoan|giao dich|ck)?\s*[:\-]?\s*/;
  var WHO_LABEL = /^(.*?)(ten nguoi nhan|nguoi nhan|nguoi thu huong|den tai khoan|nguoi chuyen|tu tai khoan|ten nguoi chuyen|beneficiary|merchant|cua hang|don vi)\s*[:\-]?\s*/;
  var TIME_LABEL = /\b(thoi gian|ngay giao dich|ngay|time|date)\b/;

  var MONEY_RE = /([+\-−–]\s?)?(\d{1,3}(?:[.,\s]\d{3})+|\d{4,12})(?:[.,]\d{1,2})?\s?(vnd|vnđ|đ|₫|d\b|dong)?/gi;

  function tokensOf(line) {
    var out = [], m, t = line.text;
    MONEY_RE.lastIndex = 0;
    while ((m = MONEY_RE.exec(t))) {
      var raw = m[2];
      var before = t.slice(Math.max(0, m.index - 1), m.index), after = t.slice(m.index + m[0].length, m.index + m[0].length + 1);
      // nằm trong ngày (25/11/2026), giờ (12:30) hay chuỗi chữ-số (FT2532…) thì bỏ
      if (/[\/:]/.test(before) || /^[\/:]/.test(after) || /[a-z]/i.test(before) && !/\s/.test(before)) continue;
      var sep = /[.,\s]\d{3}/.test(raw);
      var digits = raw.replace(/\D/g, "");
      if (raw.indexOf(" ") >= 0 && !m[3] && !m[1]) { // "12 345" dễ là mảnh số TK
        if (digits.length > 7) continue;
      }
      var amount = parseInt(digits, 10);
      if (!(amount > 0) || digits.length > 12) continue;
      out.push({ amount: amount, sign: m[1] ? (m[1].trim() === "+" ? 1 : -1) : 0, currency: !!m[3], sep: sep, digits: digits.length, index: m.index, len: m[0].length });
    }
    return out;
  }

  function median(a) { if (!a.length) return 1; var s = a.slice().sort(function (x, y) { return x - y; }); return s[s.length >> 1] || 1; }

  var DATE_RES = [
    /(\d{1,2})[\/\-.](\d{1,2})[\/\-.](20\d{2}|\d{2})[\s,\-–]*(?:lúc\s*)?(\d{1,2})[:h](\d{2})/i,
    /(\d{1,2})[:h](\d{2})(?::\d{2})?[\s,\-–]*(?:ngày\s*)?(\d{1,2})[\/\-.](\d{1,2})[\/\-.](20\d{2}|\d{2})/i,
    /(\d{1,2})[\/\-.](\d{1,2})[\/\-.](20\d{2})/
  ];
  function parseDate(text, now) {
    now = now || Date.now();
    text = String(text || "").replace(/(\d[\/\-.])\s+(?=\d)/g, "$1"); // ngày bị xuống dòng: "25- 11-2026"
    var m, dd, mo, yy, hh = "12", mi = "00";
    if ((m = text.match(DATE_RES[0]))) { dd = m[1]; mo = m[2]; yy = m[3]; hh = m[4]; mi = m[5]; }
    else if ((m = text.match(DATE_RES[1]))) { hh = m[1]; mi = m[2]; dd = m[3]; mo = m[4]; yy = m[5]; }
    else if ((m = text.match(DATE_RES[2]))) { dd = m[1]; mo = m[2]; yy = m[3]; }
    else return null;
    if (yy.length === 2) yy = "20" + yy;
    if (+mo > 12 && +dd <= 12) { var t = mo; mo = dd; dd = t; }
    var d = new Date(yy + "-" + String(mo).padStart(2, "0") + "-" + String(dd).padStart(2, "0") + "T" + String(hh).padStart(2, "0") + ":" + mi + ":00+07:00");
    if (isNaN(d) || d.getTime() > now + 86400000 || d.getTime() < now - 3 * 366 * 86400000) return null;
    return d.toISOString();
  }

  function cleanNote(s) {
    return String(s || "").replace(MONEY_RE, " ").replace(/[|_~=<>{}\[\]]+/g, " ").replace(/\s{2,}/g, " ").replace(/^[\s:\-–.,]+|[\s:\-–.,]+$/g, "").trim();
  }

  function valueAfterLabel(lines, i, re) {
    var f = fold(lines[i].text), m = f.match(re);
    if (!m) return null;
    var v = lines[i].text.slice(m[0].length).trim();
    if (!v && lines[i + 1]) v = lines[i + 1].text.trim();
    // nhãn bên trái, giá trị bên phải: cùng dòng đã lấy ở trên
    return cleanNote(v) || null;
  }

  // Tesseract hay tách nhãn (cột trái) và giá trị (cột phải) thành 2 dòng riêng -> ghép lại theo hàng ngang trên màn hình
  function toRows(lines) {
    var ls = lines.filter(function (l) { return l && String(l.text || "").trim(); }).map(function (l) {
      return { text: String(l.text).trim(), x0: l.x0 || 0, x1: l.x1 || 0, y0: l.y0 || 0, y1: l.y1 || 0, h: l.h || ((l.y1 || 0) - (l.y0 || 0)) || 1 };
    }).sort(function (a, b) { return (a.y0 + a.y1) - (b.y0 + b.y1); });
    var rows = [];
    ls.forEach(function (l) {
      var cy = (l.y0 + l.y1) / 2, row = null;
      for (var i = rows.length - 1; i >= 0 && i >= rows.length - 3; i--) {
        var r = rows[i], rcy = (r.y0 + r.y1) / 2;
        var overlapsX = r.parts.some(function (p) { return !(l.x1 <= p.x0 || l.x0 >= p.x1); });
        if (!overlapsX && Math.abs(cy - rcy) < Math.max(r.h, l.h) * 0.55) { row = r; break; }
      }
      if (row) { row.parts.push(l); row.y0 = Math.min(row.y0, l.y0); row.y1 = Math.max(row.y1, l.y1); row.h = Math.max(row.h, l.h); }
      else rows.push({ parts: [l], y0: l.y0, y1: l.y1, h: l.h });
    });
    return rows.map(function (r) {
      r.parts.sort(function (a, b) { return a.x0 - b.x0; });
      // chiều cao chữ của phần chứa số (số tiền to nằm 1 bên, nhãn nhỏ bên kia)
      return { text: r.parts.map(function (p) { return p.text; }).join("  "), x0: r.parts[0].x0, x1: r.parts[r.parts.length - 1].x1, y0: r.y0, y1: r.y1, h: r.h, parts: r.parts };
    });
  }

  // Tiêu đề ngày của nhóm (ảnh lịch sử: "24/11/2026", "Hôm nay", "Thứ Hai, 24/11") – chỉ có ngày, không có giờ và số tiền
  function headerDate(text, now) {
    now = now || Date.now();
    var f = fold(text).trim();
    if (/\d{1,2}[:h]\d{2}/.test(f) || tokensOf({ text: text.replace(/\d{1,2}[\/\-.]\d{1,2}([\/\-.]\d{2,4})?/g, " ") }).length) return null;
    var day = function (k) { var d = new Date(now + 7 * 3600000); d.setUTCHours(12 - 7, 0, 0, 0); return new Date(d.getTime() - k * 86400000).toISOString(); };
    if (/^(hom nay|today)\b/.test(f)) return day(0);
    if (/^(hom qua|yesterday)\b/.test(f)) return day(1);
    if (f.replace(/[^a-z]/g, "").length > 14) return null;
    var m = f.match(/(\d{1,2})[\/\-.](\d{1,2})(?:[\/\-.](20\d{2}|\d{2}))?/);
    if (!m) return null;
    var yy = m[3] ? (m[3].length === 2 ? "20" + m[3] : m[3]) : String(new Date(now).getFullYear());
    var d = parseDate(m[1] + "/" + m[2] + "/" + yy, now);
    return d;
  }
  // ngày tiêu đề gần nhất phía trên mỗi hàng (mang xuống cho cả nhóm)
  function headerDates(rows, now) {
    var cur = null;
    return rows.map(function (r) { var h = headerDate(r.text, now); if (h) cur = h; return { date: cur, isHeader: !!h }; });
  }

  // Phân tích 1 ảnh -> { items:[{amount,type,note,date,confidence}], mode:"single"|"list", text }
  function analyze(input, opts) {
    opts = opts || {};
    var lines = toRows(Array.isArray(input) ? input : input && input.lines ? input.lines : linesFromText(input && input.text || input));
    var hMed = median(lines.map(function (l) { return l.h || 1; }));
    var fullFold = fold(lines.map(function (l) { return l.text; }).join("\n"));
    var cands = [];
    lines.forEach(function (l, i) {
      var f = fold(l.text), prev = i > 0 ? fold(lines[i - 1].text) : "";
      var toks = tokensOf(l);
      toks.forEach(function (t) {
        var pre = f.slice(0, t.index);
        if (BAL.test(pre)) return;
        if (!/[a-z]{3}/.test(pre) && BAL.test(prev) && !AMT_LABEL.test(prev) && !t.sign) return; // nhãn "Số dư" ở dòng trên
        if (BAL.test(f) && !AMT_LABEL.test(f) && !t.sign) return;
        var idCtx = IDLINE.test(f) && !t.currency && !t.sign;
        if (idCtx) return;
        var label = AMT_LABEL.test(f) || (AMT_LABEL.test(prev) && !/\d/.test(prev));
        if (!t.currency && !t.sep && !t.sign && !label) return; // số trơn không ngữ cảnh: STK, mã, năm
        if (!t.currency && !t.sep && t.digits > 9) return;
        var fee = FEE.test(pre) || FEE.test(f) && !AMT_LABEL.test(f);
        var big = (l.h || hMed) / hMed;
        var score = Math.min(big, 4) * 2.5 + (t.currency ? 2 : 0) + (t.sign ? 3 : 0) + (label ? 4 : 0) + (t.sep ? 1 : 0) - (fee ? 6 : 0) - (t.amount < 1000 ? 3 : 0);
        cands.push({ line: i, tok: t, score: score, big: big, label: label, fee: fee });
      });
    });

    var dateAll = parseDate(lines.map(function (l) { return l.text; }).join(" "), opts.now);
    var hd = headerDates(lines, opts.now), anyHeader = hd.some(function (x) { return x.isHeader; });

    // Ảnh lịch sử: nhiều dòng có dấu +/- -> nhiều giao dịch
    var signed = cands.filter(function (c) { return c.tok.sign && !c.fee; });
    var signedLines = {}; signed.forEach(function (c) { signedLines[c.line] = c; });
    var nSigned = Object.keys(signedLines).length;
    if (nSigned >= 3 && !cands.some(function (c) { return c.label && c.big > 1.6; })) {
      var items = Object.keys(signedLines).map(Number).sort(function (a, b) { return a - b; }).map(function (li, k, arr) {
        var c = signedLines[li], l = lines[li];
        var from = k > 0 ? arr[k - 1] + 1 : Math.max(0, li - 3), to = k + 1 < arr.length ? Math.min(arr[k + 1] - 1, li + 2) : Math.min(lines.length - 1, li + 2);
        var near = [];
        for (var j = from; j <= to; j++) near.push(lines[j].text);
        var block = near.join(" ");
        var note = cleanNote(l.text.slice(0, c.tok.index) + " " + l.text.slice(c.tok.index + c.tok.len));
        if (note.replace(/[^a-zA-ZÀ-ỹ]/g, "").length < 3) {
          // tên giao dịch thường nằm ngay trên / dưới dòng số tiền
          var pick = function (j) { var s = lines[j] ? cleanNote(lines[j].text) : ""; var fs = fold(s); return s.replace(/[^a-zA-ZÀ-ỹ]/g, "").length >= 3 && !BAL.test(fs) && !IDLINE.test(fs) && !/lich su|giao dich gan day/.test(fs) && !parseDate(s, opts.now) ? s : ""; };
          // tên giao dịch: ưu tiên dòng ngay trên, rồi dòng ngay dưới
          note = pick(li - 1) || pick(li + 1) || "";
        }
        // ngày riêng của dòng (có giờ) nằm sát dòng, không vượt qua tiêu đề ngày của nhóm khác
        var date = null;
        for (var q = li; q <= to && !date; q++) { if (q > li && hd[q].isHeader) break; date = parseDate(lines[q].text, opts.now); }
        for (var r = li - 1; r >= from && !date; r--) { if (hd[r].isHeader) break; date = parseDate(lines[r].text, opts.now); }
        return { amount: c.tok.amount, type: c.tok.sign > 0 ? "INCOME" : "EXPENSE", note: note, date: date || hd[li].date || (anyHeader ? null : dateAll), confidence: 0.7 };
      });
      return { mode: "list", items: items, text: lines.map(function (l) { return l.text; }).join("\n") };
    }

    if (!cands.length) return { mode: "single", items: [], text: lines.map(function (l) { return l.text; }).join("\n") };
    cands.sort(function (a, b) { return b.score - a.score || b.tok.amount - a.tok.amount; });
    var best = cands[0];

    var type = null;
    if (best.tok.sign) type = best.tok.sign > 0 ? "INCOME" : "EXPENSE";
    else if (IN_WORDS.test(fullFold) && !OUT_WORDS.test(fullFold)) type = "INCOME";
    else if (OUT_WORDS.test(fullFold)) type = "EXPENSE";
    else if (IN_WORDS.test(fullFold)) type = "INCOME";

    var note = null, who = null, date = null;
    for (var i = 0; i < lines.length; i++) {
      var f = fold(lines[i].text);
      if (!note && NOTE_LABEL.test(f) && /(noi dung|loi nhan|mo ta|dien giai|description|message|ghi chu)/.test(f)) note = valueAfterLabel(lines, i, NOTE_LABEL);
      if (!who && /(ten nguoi nhan|nguoi nhan|nguoi thu huong|nguoi chuyen|ten nguoi chuyen|beneficiary|merchant|cua hang|don vi)/.test(f)) who = valueAfterLabel(lines, i, WHO_LABEL);
      if (!date && TIME_LABEL.test(f)) date = parseDate(lines[i].text + " " + (lines[i + 1] ? lines[i + 1].text : ""), opts.now);
    }
    var second = cands[1];
    var conf = 0.5 + (best.label ? 0.2 : 0) + (best.tok.currency || best.tok.sign ? 0.1 : 0) + (!second || best.score - second.score > 3 || second.tok.amount === best.tok.amount ? 0.15 : 0);
    return {
      mode: "single",
      items: [{ amount: best.tok.amount, type: type, note: note, who: who, date: date || dateAll, confidence: Math.min(0.95, conf) }],
      alternatives: cands.slice(1, 4).map(function (c) { return c.tok.amount; }),
      hero: cands.some(function (c) { return c.big > 1.5; }),
      text: lines.map(function (l) { return l.text; }).join("\n")
    };
  }

  // Tách ảnh thành các khối (mỗi thẻ thông báo / mỗi tin nhắn là 1 khối) theo khoảng trống dọc giữa các hàng
  function blocks(input, opts) {
    var rows = toRows(Array.isArray(input) ? input : input && input.lines ? input.lines : linesFromText(input && input.text || input));
    if (!rows.length) return [];
    var hMed = median(rows.map(function (r) { return r.h || 1; }));
    var out = [], cur = [rows[0]];
    for (var i = 1; i < rows.length; i++) {
      var gap = rows[i].y0 - rows[i - 1].y1;
      if (gap > hMed * 0.75) { out.push(cur); cur = []; }
      cur.push(rows[i]);
    }
    out.push(cur);
    var hd = headerDates(rows, opts && opts.now), idx = 0;
    return out.map(function (b) {
      var first = idx; idx += b.length;
      var now = (opts && opts.now) || Date.now();
      // "MB Bank • 10 phút", "2 giờ trước" cạnh tên app trên thẻ thông báo
      var rel = fold(b[0].text).match(/\b(\d{1,2})\s*(phut|gio|ngay|min|mins|h|hr|hrs|d)\b/), relDate = null;
      if (rel) relDate = new Date(now - (+rel[1]) * ({ phut: 60, min: 60, mins: 60, gio: 3600, h: 3600, hr: 3600, hrs: 3600, ngay: 86400, d: 86400 })[rel[2]] * 1000).toISOString();
      // tên dòng (người nhận / cửa hàng): hàng đầu có chữ, bỏ số tiền và tiêu đề ngày
      var title = "";
      for (var k = 0; k < b.length && !title; k++) {
        if (hd[first + k].isHeader) continue;
        var t = cleanNote(b[k].text).replace(/^[^A-Za-zÀ-ỹ(]+/, "");
        if (t.replace(/[^A-Za-zÀ-ỹ]/g, "").length >= 3) title = t;
      }
      return { text: b.map(function (r) { return r.text; }).join("\n"), y0: b[0].y0, y1: b[b.length - 1].y1, rows: b, date: hd[first + b.length - 1].date, relDate: relDate, title: title };
    });
  }

  var api = { headerDate: headerDate, blocks: blocks, fold: fold, prepare: prepare, read: read, analyze: analyze, parseDate: parseDate, tokensOf: tokensOf };
  if (typeof module !== "undefined" && module.exports) module.exports = api;
  else root.VinaOCR = api;
})(typeof window !== "undefined" ? window : this);
