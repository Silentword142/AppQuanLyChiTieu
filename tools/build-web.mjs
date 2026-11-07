// Đóng gói web/index.html thành bản chạy offline hoàn toàn (dùng cho APK Android).
//  - Thay các thư viện CDN bằng bản copy trong dist/www/lib
//  - Biên dịch JSX trước (không cần Babel chạy trên điện thoại -> mở app nhanh hơn)
//  - Sinh CSS Tailwind tĩnh, nhúng font Fraunces + Be Vietnam Pro, dữ liệu OCR tiếng Việt
// Chạy: npm run build:web
import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";

const require = createRequire(import.meta.url);
const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const OUT = path.join(ROOT, "dist", "www");
const NM = path.join(ROOT, "node_modules");

fs.rmSync(OUT, { recursive: true, force: true });
fs.mkdirSync(path.join(OUT, "lib", "tesseract", "lang"), { recursive: true });
fs.mkdirSync(path.join(OUT, "fonts"), { recursive: true });

const copy = (from, to) => fs.copyFileSync(path.join(NM, from), path.join(OUT, to));
copy("react/umd/react.production.min.js", "lib/react.min.js");
copy("react-dom/umd/react-dom.production.min.js", "lib/react-dom.min.js");
copy("firebase/firebase-app-compat.js", "lib/firebase-app-compat.js");
copy("firebase/firebase-auth-compat.js", "lib/firebase-auth-compat.js");
copy("firebase/firebase-firestore-compat.js", "lib/firebase-firestore-compat.js");
copy("tesseract.js/dist/tesseract.min.js", "lib/tesseract/tesseract.min.js");
copy("tesseract.js/dist/worker.min.js", "lib/tesseract/worker.min.js");
for (const f of ["tesseract-core-lstm.wasm.js", "tesseract-core-simd-lstm.wasm.js"]) copy(`tesseract.js-core/${f}`, `lib/tesseract/${f}`);
// Giải nén sẵn: công cụ đóng gói APK (aapt) tự bỏ đuôi .gz nên không dùng được file nén
const zlib = require("node:zlib");
for (const l of ["vie", "eng"]) {
  const gz = fs.readFileSync(path.join(NM, `@tesseract.js-data/${l}/4.0.0_best_int/${l}.traineddata.gz`));
  fs.writeFileSync(path.join(OUT, `lib/tesseract/lang/${l}.traineddata`), zlib.gunzipSync(gz));
}

for (const f of fs.readdirSync(path.join(ROOT, "assets-src", "fonts"))) {
  fs.copyFileSync(path.join(ROOT, "assets-src", "fonts", f), path.join(OUT, "fonts", f));
}
for (const f of ["mai.js", "icons.js"]) fs.copyFileSync(path.join(ROOT, "web", f), path.join(OUT, f));
// Logo gốc ngân hàng / ví điện tử (offline)
fs.mkdirSync(path.join(OUT, "logos"), { recursive: true });
for (const f of fs.readdirSync(path.join(ROOT, "web", "logos"))) fs.copyFileSync(path.join(ROOT, "web", "logos", f), path.join(OUT, "logos", f));

let html = fs.readFileSync(path.join(ROOT, "web", "index.html"), "utf8");

// --- Tailwind config lấy từ chính index.html để bản web và bản APK luôn giống nhau
const cfgMatch = html.match(/tailwind\.config\s*=\s*(\{[\s\S]*?\n {4}\})\s*\n/);
if (!cfgMatch) throw new Error("Không tìm thấy tailwind.config trong index.html");
const twConfig = Function(`return (${cfgMatch[1]})`)();

// --- Biên dịch JSX
const babel = require("@babel/core");
const jsxMatch = html.match(/<script type="text\/babel">([\s\S]*?)<\/script>/);
if (!jsxMatch) throw new Error("Không tìm thấy <script type=text/babel>");
const compiled = babel.transformSync(jsxMatch[1], { presets: [require.resolve("@babel/preset-react")], compact: false, comments: false }).code;
fs.writeFileSync(path.join(OUT, "app.js"), compiled);

// --- Tailwind CSS tĩnh
const tailwind = require("tailwindcss");
const pc = require("postcss");
const css = await pc([tailwind({ ...twConfig, content: [{ raw: html, extension: "html" }] })])
  .process("@tailwind base;\n@tailwind components;\n@tailwind utilities;\n", { from: undefined });
fs.writeFileSync(path.join(OUT, "app.css"), css.css);

// --- Ghép HTML
const libs = `
  <link rel="stylesheet" href="fonts/fonts.css" />
  <link rel="stylesheet" href="app.css" />
  <script src="lib/react.min.js"></script>
  <script src="lib/react-dom.min.js"></script>
  <script src="lib/firebase-app-compat.js"></script>
  <script src="lib/firebase-auth-compat.js"></script>
  <script src="lib/firebase-firestore-compat.js"></script>
  <script src="lib/tesseract/tesseract.min.js"></script>
  <script>
    window.VINA_OFFLINE_OCR = {
      workerPath: new URL("lib/tesseract/worker.min.js", location.href).href,
      corePath: new URL("lib/tesseract/", location.href).href,
      langPath: new URL("lib/tesseract/lang", location.href).href,
      gzip: false,
      cacheMethod: "none"
    };
  </script>`;
html = html.replace(/<!-- BUILD:CDN-START[\s\S]*?<!-- BUILD:CDN-END -->/, libs);
html = html.replace(/<script type="text\/babel">[\s\S]*?<\/script>/, '<script src="app.js"></script>');
if (/cdn\.|unpkg|gstatic|googleapis/.test(html.replace(/https:\/\/(www\.)?googleapis\.com\/auth[^\s"']*/g, ""))) {
  const left = html.match(/https?:\/\/[^\s"')]+/g).filter(u => /cdn\.|unpkg|gstatic|fonts\.googleapis/.test(u));
  if (left.length) throw new Error("Còn tham chiếu CDN: " + left.join(", "));
}
fs.writeFileSync(path.join(OUT, "index.html"), html);

const size = (p) => fs.statSync(p).isDirectory() ? fs.readdirSync(p).reduce((s, f) => s + size(path.join(p, f)), 0) : fs.statSync(p).size;
console.log(`dist/www sẵn sàng (${(size(OUT) / 1048576).toFixed(1)} MB)`);
