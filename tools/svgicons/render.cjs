// node render.cjs IN_DIR OUT_DIR : chuyển mọi .svg thành .png 512px nền trong suốt
const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => {
  const [inDir, outDir] = process.argv.slice(2);
  const b = await chromium.launch(); const p = await b.newPage({ viewport: { width: 512, height: 512 } });
  for (const f of fs.readdirSync(inDir).filter(f => f.endsWith('.svg'))) {
    await p.setContent(`<html><body style="margin:0;background:transparent">${fs.readFileSync(path.join(inDir, f), 'utf8')}</body></html>`);
    await p.locator('svg').screenshot({ path: path.join(outDir, f.replace('.svg', '.png')), omitBackground: true });
  }
  await b.close();
})();
