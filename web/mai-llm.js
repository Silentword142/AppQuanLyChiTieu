// Bộ não AI của Mai – mô hình ngôn ngữ chạy NGAY TRÊN MÁY (llama.cpp WebAssembly qua @wllama/wllama).
// Không gửi dữ liệu đi đâu: chỉ tải file mô hình về một lần, sau đó chạy offline.
// Giao diện: window.MaiLLM (xem cuối file). Nạp bằng <script type="module">.
import { Wllama, CacheManager } from "./lib/wllama/index.js";

const HF = "https://huggingface.co";
const MODELS = [
  {
    id: "qwen2.5-1.5b",
    name: "Mai Thông minh",
    desc: "Qwen2.5 1.5B · hiểu tiếng Việt khá, trò chuyện tự nhiên. Hợp máy RAM 6 GB trở lên.",
    url: `${HF}/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf`,
    size: 1117320736,
    ctx: 2048,
  },
  {
    id: "qwen2.5-0.5b",
    name: "Mai Nhẹ",
    desc: "Qwen2.5 0.5B · nhanh, nhẹ máy, nhưng kém thông minh hơn. Hợp máy RAM 3–4 GB.",
    url: `${HF}/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf`,
    size: 491400032,
    ctx: 2048,
  },
];
const IMPORTED = { id: "imported", name: "Mô hình tự nhập", desc: "File .gguf anh chọn từ máy.", url: "local://mai-imported.gguf", size: 0, ctx: 2048 };
const WASM = new URL("./lib/wllama/wllama.wasm", import.meta.url).href;
// Bản tương thích (WebView cũ chưa có JSPI / Memory64) – để sẵn trong app, không tải từ CDN
const COMPAT = { wasm: new URL("./lib/wllama/compat/wllama.wasm", import.meta.url).href, worker: new URL("./lib/wllama/compat/wllama.js", import.meta.url).href };
const makeWllama = () => {
  const w = new Wllama({ default: WASM }, { suppressNativeLog: true, logger: { debug() {}, log() {}, warn: console.warn, error: console.error } });
  w.setCompat(COMPAT, "firefox_safari");
  return w;
};
const byId = (id) => MODELS.find((m) => m.id === id) || (id === "imported" ? IMPORTED : null);

const cache = new CacheManager();
let wllama = null;
let loadedId = null;
let abortCtl = null;
let queue = Promise.resolve(); // chạy lần lượt từng việc
let waitingFg = 0;          // số câu chat của anh đang chờ -> việc nền nhường chỗ
let bgStopper = null;       // dừng việc nền đang chạy (xếp hạng mục, nạp sẵn)
let fg = null;              // câu chat của anh đang chờ/chạy: { stopper, kill, writing }
let resetP = null, resetFire = null; // khi tắt hẳn bộ não, mọi việc đang treo đều thoát ra
let loadingP = null, loadingId = null;
const KILLED = { killed: true };
const newReset = () => { resetP = new Promise((r) => { resetFire = () => r(KILLED); }); };
newReset();

const state = { status: "idle", modelId: null, progress: 0, loaded: 0, total: 0, error: null, downloaded: [], threads: 0, speed: 0, readSpeed: 0 };
const subs = new Set();
const emit = (patch) => { Object.assign(state, patch); subs.forEach((f) => { try { f({ ...state }); } catch (e) {} }); };

async function refreshDownloaded() {
  try {
    const list = await cache.list();
    const urls = new Set(list.map((e) => e.metadata && e.metadata.originalURL));
    const ids = MODELS.filter((m) => urls.has(m.url)).map((m) => m.id);
    if (urls.has(IMPORTED.url)) ids.push("imported");
    emit({ downloaded: ids });
    return ids;
  } catch (e) {
    emit({ downloaded: [] });
    return [];
  }
}

async function download(id) {
  const m = byId(id);
  if (!m || id === "imported") throw new Error("Không có mô hình này");
  abortCtl = new AbortController();
  emit({ status: "downloading", modelId: id, progress: 0, loaded: 0, total: m.size, error: null });
  try { if (navigator.storage && navigator.storage.persist) await navigator.storage.persist(); } catch (e) {}
  try {
    await cache.download(m.url, {
      signal: abortCtl.signal,
      progressCallback: ({ loaded, total }) => emit({ loaded, total: total || m.size, progress: (total || m.size) ? loaded / (total || m.size) : 0 }),
    });
    await refreshDownloaded();
    emit({ status: loadedId ? "loaded" : "idle", progress: 1 });
  } catch (e) {
    const aborted = e && (e.name === "AbortError" || /abort/i.test(String(e.message)));
    emit({ status: loadedId ? "loaded" : "idle", error: aborted ? null : explain(e) });
    if (!aborted) throw e;
  } finally {
    abortCtl = null;
  }
}

function cancel() { if (abortCtl) abortCtl.abort(); }

async function importFile(file) {
  if (!file) return;
  if (!/\.gguf$/i.test(file.name)) throw new Error("Cần file đuôi .gguf");
  emit({ status: "importing", modelId: "imported", progress: 0, error: null, total: file.size, loaded: 0 });
  try {
    const name = await cache.getNameFromURL(IMPORTED.url);
    let loaded = 0;
    const counted = file.stream().pipeThrough(new TransformStream({
      transform(chunk, ctl) { loaded += chunk.byteLength; emit({ loaded, progress: loaded / file.size }); ctl.enqueue(chunk); },
    }));
    await cache.write(name, counted, { etag: String(file.lastModified || Date.now()), originalSize: file.size, originalURL: IMPORTED.url });
    await refreshDownloaded();
    emit({ status: loadedId ? "loaded" : "idle", progress: 1 });
  } catch (e) {
    emit({ status: "idle", error: explain(e) });
    throw e;
  }
}

async function remove(id) {
  const m = byId(id);
  if (!m) return;
  if (loadedId === id) await unload();
  await cache.delete(m.url);
  await refreshDownloaded();
}

function load(id) {
  if (loadingP && loadingId === id) return loadingP;
  loadingId = id;
  loadingP = loadInner(id).finally(() => { loadingP = null; loadingId = null; });
  return loadingP;
}
async function loadInner(id) {
  const m = byId(id);
  if (!m) throw new Error("Không có mô hình này");
  if (loadedId === id && wllama && wllama.isModelLoaded()) return;
  await unload();
  emit({ status: "loading", modelId: id, error: null });
  try {
    const blob = await cache.open(m.url);
    if (!blob) throw new Error("Chưa tải mô hình về máy");
    wllama = makeWllama();
    // Điện thoại có lõi to + lõi nhỏ: dùng 4 luồng là nhanh nhất, nhiều hơn lại chậm
    const cores = Math.max(1, Math.min(4, (navigator.hardwareConcurrency || 4) - 1));
    // Chạy bằng CPU: WebGPU trên điện thoại hay bị treo rất lâu ở lần chạy đầu
    const base = { n_ctx: m.ctx, n_batch: 512, n_gpu_layers: 0, n_parallel: 1, warmup: false };
    try {
      await wllama.loadModel([blob], { ...base, n_threads: cores });
    } catch (e) {
      // đa luồng lỗi trên một số máy -> thử lại 1 luồng
      try { await wllama.exit(); } catch (x) {}
      wllama = makeWllama();
      await wllama.loadModel([blob], { ...base, n_threads: 1 });
    }
    loadedId = id;
    emit({ status: "loaded", modelId: id, threads: wllama.getNumThreads ? wllama.getNumThreads() : 0 });
  } catch (e) {
    wllama = null; loadedId = null;
    emit({ status: "error", error: explain(e) });
    throw e;
  }
}

async function unload() {
  if (wllama) { try { await wllama.exit(); } catch (e) {} }
  wllama = null; loadedId = null;
  if (state.status === "loaded") emit({ status: "idle" });
}

// Gọi mô hình. opts: { messages, tools, maxTokens, temperature, onToken(textSoFar) }
// -> { content, toolCalls: [{ name, args }] }
// opts.background = true: việc nền, tự nhường khi anh nhắn. opts.onStage("reading"|"writing")
function chat(opts) {
  const bg = !!opts.background;
  let me = null;
  let killP = resetP;
  if (!bg) {
    waitingFg++;
    if (bgStopper) bgStopper.abort();
    me = { stopper: new AbortController(), writing: false };
    killP = Promise.race([resetP, new Promise((r) => { me.kill = () => r(KILLED); })]);
    fg = me;
  }
  const myReset = resetP;
  const run = async () => {
    if (!bg) waitingFg--;
    if (myReset !== resetP || (me && me.stopper.signal.aborted)) return { content: "", toolCalls: [], stopped: !bg, skipped: bg };
    if (!wllama || !wllama.isModelLoaded()) throw new Error("Mai chưa bật bộ não AI");
    if (bg && waitingFg > 0) return { content: "", toolCalls: [], skipped: true };
    const t0 = performance.now();
    let tFirst = 0;
    const params = {
      messages: opts.messages,
      max_tokens: opts.maxTokens || 320,
      temperature: opts.temperature == null ? 0.4 : opts.temperature,
      top_p: 0.9,
      top_k: 40,
      frequency_penalty: 0.3,
      presence_penalty: 0.2,
      stream: true,
    };
    const stopper = bg ? new AbortController() : me.stopper;
    if (bg) bgStopper = stopper;
    params.abortSignal = stopper.signal;
    if (opts.onStage) opts.onStage("reading");
    if (opts.tools && opts.tools.length) { params.tools = opts.tools; params.tool_choice = "auto"; }
    let content = "";
    const calls = [];
    let n = 0;
    let killed = false;
    try {
      const stream = await Promise.race([wllama.createChatCompletion(params), killP]);
      if (stream === KILLED) throw KILLED;
      const iter = stream[Symbol.asyncIterator]();
      while (true) {
        const step = await Promise.race([iter.next(), killP]);
        if (step === KILLED) throw KILLED;
        if (step.done) break;
        const d = step.value && step.value.choices && step.value.choices[0] && step.value.choices[0].delta;
        if (!d) continue;
        if ((d.content || d.tool_calls) && !tFirst) { tFirst = performance.now(); if (me) me.writing = true; if (opts.onStage) opts.onStage("writing"); }
        if (d.content) {
          content += d.content; n++;
          if (opts.onToken) opts.onToken(cleanText(content));
          // Mô hình nhỏ đôi khi lặp lại một câu mãi -> cắt
          if (content.length > 160 && n % 8 === 0) {
            const tail = content.slice(-48);
            if (content.slice(0, -48).split(tail).length > 2) { content = content.slice(0, content.indexOf(tail) + tail.length); stopper.abort(); break; }
          }
        }
        if (d.tool_calls) {
          d.tool_calls.forEach((tc) => {
            const i = tc.index == null ? calls.length : tc.index;
            const c = calls[i] || (calls[i] = { name: "", args: "" });
            if (tc.function && tc.function.name) c.name += tc.function.name;
            if (tc.function && tc.function.arguments) c.args += tc.function.arguments;
          });
        }
      }
    } catch (e) {
      if (e === KILLED) killed = true;
      else if (!stopper.signal.aborted) throw e;
    } finally {
      if (bgStopper === stopper) bgStopper = null;
    }
    if (killed) return { content: "", toolCalls: [], stopped: !bg, skipped: bg };
    const tEnd = performance.now();
    if (n > 3 && tFirst) emit({ speed: Math.round((n / ((tEnd - tFirst) / 1000)) * 10) / 10 });
    if (tFirst && opts.promptChars) emit({ readSpeed: Math.round(opts.promptChars / 3.5 / ((tFirst - t0) / 1000)) });
    const toolCalls = calls.filter(Boolean).map((c) => { let a = {}; try { a = JSON.parse(c.args || "{}"); } catch (e) {} return { name: c.name, args: a }; });
    // Mô hình nhỏ đôi khi viết lệnh gọi công cụ thành chữ <tool_call>{...}</tool_call>
    const inline = /<tool_call>\s*([\s\S]*?)\s*<\/tool_call>/g;
    let mm;
    while ((mm = inline.exec(content))) { try { const j = JSON.parse(mm[1]); if (j && j.name) toolCalls.push({ name: j.name, args: j.arguments || {} }); } catch (e) {} }
    return { content: cleanText(content), toolCalls, stopped: !bg && stopper.signal.aborted && !content };
  };
  const p = queue.then(run, run);
  queue = p.catch(() => {});
  if (bg) return p;
  const out = Promise.race([p, killP.then(() => ({ content: "", toolCalls: [], stopped: true }))]);
  out.finally(() => { if (fg === me) fg = null; }).catch(() => {});
  return out;
}

function cleanText(s) {
  return String(s || "")
    .replace(/<think>[\s\S]*?(<\/think>|$)/g, "")
    .replace(/<tool_call>[\s\S]*?(<\/tool_call>|$)/g, "")
    .replace(/<\|[^|>]*\|>/g, "")
    .trim();
}

function explain(e) {
  const m = String((e && e.message) || e || "");
  if (/quota|QuotaExceeded|space/i.test(m)) return "Máy hết dung lượng trống để lưu mô hình.";
  if (/Failed to fetch|NetworkError|network|load failed/i.test(m)) return "Không tải được (mất mạng hoặc máy chủ chặn). Anh thử lại bằng Wi-Fi, hoặc tải file .gguf về máy rồi bấm “Nhập file”.";
  if (/memory|OOM|allocation|Out of bounds/i.test(m)) return "Máy không đủ RAM cho mô hình này. Anh thử “Mai Nhẹ”.";
  return m.slice(0, 160) || "Lỗi không rõ";
}

// Nạp sẵn phần dặn dò (system prompt) vào bộ nhớ đệm để câu hỏi đầu tiên trả lời nhanh
function warm(messages, tools) {
  if (!wllama || !wllama.isModelLoaded()) return Promise.resolve();
  return chat({ messages: messages.concat([{ role: "user", content: "." }]), tools, maxTokens: 1, temperature: 0, background: true }).catch(() => {});
}

// Nút "Dừng": dừng câu trả lời đang chạy
// Nút "Dừng". Đang viết thì dừng nhẹ nhàng; còn đang đọc/đang chờ thì máy không ngắt giữa chừng được
// -> tắt hẳn bộ não (mọi việc đang treo thoát ra) rồi bật lại ở nền.
function stop() {
  const me = fg;
  if (!me) return;
  me.stopper.abort();
  if (me.writing) return;
  if (me.kill) me.kill();
  const id = loadedId, w = wllama;
  if (!w) return;
  wllama = null; loadedId = null;
  try { w.proxy && w.proxy.worker && w.proxy.worker.terminate(); } catch (e) {}
  const fire = resetFire;
  newReset();
  fire();
  queue = Promise.resolve();
  waitingFg = 0;
  emit({ status: "idle" });
  if (id) setTimeout(() => load(id).catch(() => {}), 50);
}

const MaiLLM = {
  MODELS, IMPORTED,
  getState: () => ({ ...state }),
  subscribe: (fn) => { subs.add(fn); fn({ ...state }); return () => subs.delete(fn); },
  refresh: refreshDownloaded,
  download, cancel, importFile, remove, load, unload, chat, warm, stop,
  isLoaded: () => !!(wllama && loadedId && wllama.isModelLoaded()),
  loadedId: () => loadedId,
  recommended: () => ((navigator.deviceMemory || 4) >= 6 ? "qwen2.5-1.5b" : "qwen2.5-0.5b"),
};
window.MaiLLM = MaiLLM;
refreshDownloaded();
window.dispatchEvent(new Event("maillm-ready"));
