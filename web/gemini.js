// Gemini đọc thông báo ngân hàng / ví và trả về giao dịch – dùng các model MIỄN PHÍ của Google AI Studio.
// Việc rất đơn giản nên ưu tiên model nhẹ (Flash-Lite: nhanh, hạn mức miễn phí rộng). Model nào hết lượt / quá tải
// thì tự chuyển sang model kế tiếp – mỗi model có hạn mức miễn phí riêng nên cộng lại được nhiều lượt hơn.
// Key: ưu tiên key anh nhập trong Cài đặt (localStorage), không có thì dùng key cài sẵn lúc đóng gói APK (config.js).
// File config.js được tạo lúc build từ file .gemini.key (đã gitignore) – KHÔNG bao giờ đưa key lên GitHub.
(function () {
  var MODELS = [
    { id: "gemini-3.1-flash-lite", label: "3.1 Flash-Lite", thinking: { thinkingLevel: "minimal" }, timeout: 60000 },
    { id: "gemini-3.5-flash-lite", label: "3.5 Flash-Lite", thinking: { thinkingLevel: "low" }, timeout: 60000 },
    { id: "gemini-3.8-flash", label: "3.8 Flash", thinking: { thinkingLevel: "medium" }, timeout: 240000 },
  ];
  var MODEL = MODELS[0].id;
  var BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
  // model đang hết lượt -> bỏ qua tới thời điểm này (lưu lại để mở app lại không gọi thừa)
  function blocked() { try { return JSON.parse(localStorage.getItem("vina_gem_blocked") || "{}"); } catch (e) { return {}; } }
  function block(id, ms) { try { var b = blocked(); b[id] = Date.now() + ms; localStorage.setItem("vina_gem_blocked", JSON.stringify(b)); } catch (e) {} }
  var lastModel = null;

  function getKey() {
    try { var k = localStorage.getItem("vina_gemini_key"); if (k && k.trim()) return k.trim(); } catch (e) {}
    return (window.VINA_CONFIG && window.VINA_CONFIG.geminiKey) || "";
  }
  function hasKey() { return !!getKey(); }
  function keySource() {
    try { if ((localStorage.getItem("vina_gemini_key") || "").trim()) return "custom"; } catch (e) {}
    return window.VINA_CONFIG && window.VINA_CONFIG.geminiKey ? "built-in" : "none";
  }

  var SCHEMA = {
    type: "ARRAY",
    items: {
      type: "OBJECT",
      properties: {
        id: { type: "STRING" },
        is_transaction: { type: "BOOLEAN", description: "true chỉ khi tiền ĐÃ thực sự chuyển vào/ra một tài khoản, thẻ hoặc ví của người dùng" },
        amount: { type: "INTEGER", description: "số tiền VND của giao dịch (không phải số dư)" },
        type: { type: "STRING", enum: ["INCOME", "EXPENSE"] },
        category: { type: "STRING", description: "đúng tên một hạng mục trong danh sách" },
        account: { type: "STRING", description: "tên tài khoản trong danh sách khớp với thông báo, hoặc tên ngân hàng/ví nếu không khớp" },
        note: { type: "STRING", description: "ghi chú ngắn: nơi chi / người chuyển / nội dung chuyển khoản, tối đa 60 ký tự" },
        date: { type: "STRING", description: "thời điểm giao dịch dạng YYYY-MM-DDTHH:mm (giờ Việt Nam)" },
        balance: { type: "INTEGER", nullable: true, description: "số dư sau giao dịch nếu thông báo có" },
        confidence: { type: "NUMBER", description: "0–1: mức chắc chắn về số tiền, chiều tiền VÀ hạng mục" },
        reason: { type: "STRING", description: "lý do ngắn nếu không phải giao dịch" },
      },
      required: ["id", "is_transaction"],
    },
  };

  var SYSTEM = [
    "Bạn là bộ phận ghi sổ của app quản lý chi tiêu VinaSpends ở Việt Nam.",
    "Mỗi thông báo điện thoại (từ app ngân hàng, ví điện tử, SMS) cần xác định: có phải giao dịch tiền ĐÃ xảy ra với tài khoản của người dùng không.",
    "KHÔNG phải giao dịch: OTP, khuyến mãi, quảng cáo, nhắc nợ/sao kê, đơn hàng đang giao, giao dịch thất bại/đang xử lý, tin nhắn chat có số tiền.",
    "Là giao dịch: biến động số dư, chuyển khoản đi/đến, thanh toán thẻ/QR thành công, nạp/rút tiền, hoàn tiền.",
    "type: INCOME nếu tiền vào tài khoản người dùng, EXPENSE nếu tiền ra. Dấu + / - trước số tiền là căn cứ mạnh nhất.",
    "category: chọn đúng một tên trong danh sách hạng mục cùng chiều tiền. Đoán theo tên cửa hàng, người nhận, nội dung chuyển khoản. Luôn ưu tiên ĐIỀU NGƯỜI DÙNG DẶN và CÁC LẦN NGƯỜI DÙNG ĐÃ SỬA.",
    "Nếu thông báo không có nội dung/nơi giao dịch để biết hạng mục thì chọn 'Khác' (hoặc hạng mục thu chung) và để confidence ≤ 0.6.",
    "note: viết gọn, có dấu tiếng Việt khi phù hợp; không chép nguyên thông báo.",
    "Trả về mảng JSON, mỗi thông báo đúng một phần tử, giữ nguyên id.",
  ].join("\n");

  function buildPrompt(items, ctx) {
    var lines = [];
    lines.push("HÔM NAY: " + (ctx.today || new Date().toISOString().slice(0, 10)));
    lines.push("HẠNG MỤC CHI: " + (ctx.expenseCats || []).join(", "));
    lines.push("HẠNG MỤC THU: " + (ctx.incomeCats || []).join(", "));
    lines.push("TÀI KHOẢN CỦA NGƯỜI DÙNG: " + ((ctx.accounts || []).join("; ") || "chưa có"));
    if (ctx.rules && ctx.rules.trim()) lines.push("ĐIỀU NGƯỜI DÙNG DẶN:\n" + ctx.rules.trim());
    if (ctx.examples && ctx.examples.length) {
      lines.push("CÁC LẦN NGƯỜI DÙNG ĐÃ SỬA (làm theo y hệt khi gặp lại):");
      ctx.examples.forEach(function (e) { lines.push("- \"" + e.text + "\" → " + (e.ignore ? "KHÔNG phải giao dịch cần ghi, bỏ qua (is_transaction=false)" : e.category + (e.type === "INCOME" ? " (thu)" : " (chi)"))); });
    }
    lines.push("");
    lines.push("THÔNG BÁO:");
    items.forEach(function (it) {
      var t = it.postTime ? new Date(it.postTime) : null;
      var when = t && !isNaN(t) ? " | nhận lúc " + t.toLocaleString("sv-SE", { timeZone: "Asia/Ho_Chi_Minh" }).slice(0, 16) : "";
      var body = ((it.title ? it.title + " | " : "") + (it.text || "")).replace(/\s+/g, " ").slice(0, 600);
      lines.push("[id=" + it.id + "] app=" + (it.packageName || "dán tay") + when + " | " + body);
    });
    return lines.join("\n");
  }

  async function callModel(m, key, items, ctx) {
    var body = {
      systemInstruction: { parts: [{ text: SYSTEM }] },
      contents: [{ role: "user", parts: [{ text: buildPrompt(items, ctx || {}) }] }],
      generationConfig: {
        temperature: 0.2,
        thinkingConfig: m.thinking,
        responseMimeType: "application/json",
        responseSchema: SCHEMA,
      },
    };
    var ctl = new AbortController();
    var timer = setTimeout(function () { ctl.abort(); }, m.timeout);
    var res;
    try {
      res = await fetch(BASE + m.id + ":generateContent", {
        method: "POST",
        headers: { "Content-Type": "application/json", "x-goog-api-key": key },
        body: JSON.stringify(body),
        signal: ctl.signal,
      });
    } catch (e) {
      // Lỗi 503 (quá tải) của Google không kèm header CORS nên trình duyệt chỉ báo "lỗi mạng"
      throw Object.assign(new Error(e && e.name === "AbortError" ? "Gemini trả lời quá lâu" : (navigator.onLine === false ? "Đang mất mạng" : "Gemini đang quá tải hoặc mạng chập chờn")), { code: navigator.onLine === false ? "offline" : "network" });
    } finally { clearTimeout(timer); }
    var data = null;
    try { data = await res.json(); } catch (e) {}
    if (!res.ok) {
      var msg = (data && data.error && data.error.message) || ("HTTP " + res.status);
      // Hết lượt (429): Google cho biết bao lâu nữa được gọi lại ("retryDelay": "11330s" / "Please retry in 3h9m50s")
      var retryMs = 0;
      try { (data.error.details || []).forEach(function (d) { if (d && d.retryDelay) retryMs = Math.max(retryMs, parseFloat(d.retryDelay) * 1000 || 0); }); } catch (x) {}
      var rm = /retry in\s+(?:(\d+)h)?(?:(\d+)m)?(?:([\d.]+)s)?/i.exec(msg);
      if (!retryMs && rm) retryMs = ((+rm[1] || 0) * 3600 + (+rm[2] || 0) * 60 + (parseFloat(rm[3]) || 0)) * 1000;
      var daily = /per day|PerDay/i.test(msg);
      var code = (res.status === 400 && /api key/i.test(msg)) || res.status === 401 || res.status === 403 ? "bad_key" : res.status === 429 ? "quota" : res.status === 404 ? "gone" : res.status >= 500 ? "server" : "http";
      throw Object.assign(new Error(msg), { code: code, status: res.status, retryMs: retryMs, daily: daily });
    }
    var parts = (data && data.candidates && data.candidates[0] && data.candidates[0].content && data.candidates[0].content.parts) || [];
    var text = parts.filter(function (p) { return p.text && !p.thought; }).map(function (p) { return p.text; }).join("");
    var arr;
    try { arr = JSON.parse(text); } catch (e) { throw Object.assign(new Error("Gemini trả về dữ liệu lạ"), { code: "parse" }); }
    return Array.isArray(arr) ? arr : [arr];
  }

  // items: [{ id, packageName, title, text, postTime }] -> [{ id, is_transaction, amount, type, ... }]
  async function analyze(items, ctx) {
    var key = getKey();
    if (!key) throw Object.assign(new Error("Chưa có Gemini API key"), { code: "no_key" });
    var b = blocked(), now = Date.now(), last = null, soonest = Infinity;
    for (var i = 0; i < MODELS.length; i++) {
      var m = MODELS[i];
      if (b[m.id] && b[m.id] > now) { soonest = Math.min(soonest, b[m.id] - now); continue; }
      try {
        var out = await callModel(m, key, items, ctx);
        lastModel = m.id;
        try { localStorage.setItem("vina_gem_last_model", m.id); } catch (e) {}
        return out;
      } catch (e) {
        last = e;
        if (e.code === "bad_key" || e.code === "offline") throw e;
        // hết lượt: khoá model này tới khi Google cho gọi lại; quá tải / lỗi khác: tạm né 2 phút; model bị gỡ: né 30 ngày
        var wait = e.code === "quota" ? (e.retryMs || (e.daily ? 3 * 3600000 : 60000)) : e.code === "gone" ? 30 * 86400000 : 120000;
        block(m.id, wait);
        soonest = Math.min(soonest, wait);
      }
    }
    var err = last || Object.assign(new Error("Mọi model miễn phí đang hết lượt"), { code: "quota" });
    if (!last) err.code = "quota";
    err.retryMs = isFinite(soonest) ? soonest : 60000;
    err.allModels = true;
    throw err;
  }

  function explain(e) {
    var c = e && e.code;
    if (c === "no_key") return "Chưa có Gemini API key. Nhập key ở mục Cài đặt Gemini.";
    if (c === "bad_key") return "Gemini từ chối key (sai hoặc đã bị khoá). Kiểm tra lại key.";
    var t = e && e.retryMs ? (e.retryMs >= 3600000 ? Math.floor(e.retryMs / 3600000) + " giờ " + Math.round((e.retryMs % 3600000) / 60000) + " phút" : Math.max(1, Math.round(e.retryMs / 60000)) + " phút") : "";
    if (e && e.allModels && c !== "no_key") return "Cả 3 model Gemini miễn phí đang hết lượt hoặc quá tải." + (t ? " App tự gửi lại sau " + t + "." : " App tự thử lại sau.");
    if (c === "quota") return "Hết lượt gọi Gemini miễn phí." + (t ? " App tự gửi lại sau " + t + "." : " App tự thử lại sau.");
    if (c === "offline") return "Đang mất mạng. Có mạng app gửi ngay.";
    if (c === "server") return "Máy chủ Gemini đang quá tải. App tự thử lại sau ít phút.";
    if (c === "network") return e.message + ". App tự thử lại sau ít phút.";
    return (e && e.message) || "Lỗi không rõ";
  }

  window.VinaGemini = { MODEL: MODEL, MODELS: MODELS, lastModel: function () { try { return localStorage.getItem("vina_gem_last_model") || lastModel; } catch (e) { return lastModel; } }, blockedUntil: blocked, analyze: analyze, hasKey: hasKey, keySource: keySource, explain: explain, buildPrompt: buildPrompt };
})();
