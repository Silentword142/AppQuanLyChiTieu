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
    ctx: 4096,
  },
  {
    id: "qwen2.5-0.5b",
    name: "Mai Nhẹ",
    desc: "Qwen2.5 0.5B · nhanh, nhẹ máy, nhưng kém thông minh hơn. Hợp máy RAM 3–4 GB.",
    url: `${HF}/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf`,
    size: 491400032,
    ctx: 4096,
  },
];
const IMPORTED = { id: "imported", name: "Mô hình tự nhập", desc: "File .gguf anh chọn từ máy.", url: "local://mai-imported.gguf", size: 0, ctx: 4096 };
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
let queue = Promise.resolve(); // chạy lần lượt từng việc (chat ưu tiên vẫn phải chờ việc đang chạy)

const state = { status: "idle", modelId: null, progress: 0, loaded: 0, total: 0, error: null, downloaded: [], threads: 0, speed: 0 };
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

async function load(id) {
  const m = byId(id);
  if (!m) throw new Error("Không có mô hình này");
  if (loadedId === id && wllama && wllama.isModelLoaded()) return;
  await unload();
  emit({ status: "loading", modelId: id, error: null });
  try {
    const blob = await cache.open(m.url);
    if (!blob) throw new Error("Chưa tải mô hình về máy");
    wllama = makeWllama();
    const cores = Math.max(1, Math.min(6, (navigator.hardwareConcurrency || 4) - 1));
    const base = { n_ctx: m.ctx, n_batch: 256 };
    try {
      await wllama.loadModel([blob], { ...base, n_threads: cores });
    } catch (e) {
      // WebGPU / đa luồng lỗi trên một số máy -> thử lại kiểu an toàn
      try { await wllama.exit(); } catch (x) {}
      wllama = makeWllama();
      await wllama.loadModel([blob], { ...base, n_gpu_layers: 0 });
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
function chat(opts) {
  const run = async () => {
    if (!wllama || !wllama.isModelLoaded()) throw new Error("Mai chưa bật bộ não AI");
    const t0 = performance.now();
    const params = {
      messages: opts.messages,
      max_tokens: opts.maxTokens || 320,
      temperature: opts.temperature == null ? 0.4 : opts.temperature,
      top_p: 0.9,
      top_k: 40,
      stream: true,
    };
    if (opts.tools && opts.tools.length) { params.tools = opts.tools; params.tool_choice = "auto"; }
    let content = "";
    const calls = [];
    let n = 0;
    const stream = await wllama.createChatCompletion(params);
    for await (const chunk of stream) {
      const d = chunk && chunk.choices && chunk.choices[0] && chunk.choices[0].delta;
      if (!d) continue;
      if (d.content) { content += d.content; n++; if (opts.onToken) opts.onToken(cleanText(content)); }
      if (d.tool_calls) {
        d.tool_calls.forEach((tc) => {
          const i = tc.index == null ? calls.length : tc.index;
          const c = calls[i] || (calls[i] = { name: "", args: "" });
          if (tc.function && tc.function.name) c.name += tc.function.name;
          if (tc.function && tc.function.arguments) c.args += tc.function.arguments;
        });
      }
    }
    const secs = (performance.now() - t0) / 1000;
    if (n > 3) emit({ speed: Math.round((n / secs) * 10) / 10 });
    const toolCalls = calls.filter(Boolean).map((c) => { let a = {}; try { a = JSON.parse(c.args || "{}"); } catch (e) {} return { name: c.name, args: a }; });
    // Mô hình nhỏ đôi khi viết lệnh gọi công cụ thành chữ <tool_call>{...}</tool_call>
    const inline = /<tool_call>\s*([\s\S]*?)\s*<\/tool_call>/g;
    let mm;
    while ((mm = inline.exec(content))) { try { const j = JSON.parse(mm[1]); if (j && j.name) toolCalls.push({ name: j.name, args: j.arguments || {} }); } catch (e) {} }
    return { content: cleanText(content), toolCalls };
  };
  const p = queue.then(run, run);
  queue = p.catch(() => {});
  return p;
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
  return chat({ messages: messages.concat([{ role: "user", content: "." }]), tools, maxTokens: 1, temperature: 0 }).catch(() => {});
}

const MaiLLM = {
  MODELS, IMPORTED,
  getState: () => ({ ...state }),
  subscribe: (fn) => { subs.add(fn); fn({ ...state }); return () => subs.delete(fn); },
  refresh: refreshDownloaded,
  download, cancel, importFile, remove, load, unload, chat, warm,
  isLoaded: () => !!(wllama && loadedId && wllama.isModelLoaded()),
  loadedId: () => loadedId,
  recommended: () => ((navigator.deviceMemory || 4) >= 6 ? "qwen2.5-1.5b" : "qwen2.5-0.5b"),
};
window.MaiLLM = MaiLLM;
refreshDownloaded();
window.dispatchEvent(new Event("maillm-ready"));
