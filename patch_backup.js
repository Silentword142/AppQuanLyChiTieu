const fs = require('fs');
let html = fs.readFileSync('web/index.html', 'utf8');

html = html.replace(
  /await fetch\('\/api\/cloud-sync\/backup', \{[\s\S]*?\}\);/,
  `const bRes = await fetch('/api/cloud-sync/backup', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                email,
                payload: backupPayload
              })
            });
            if (!bRes.ok) {
              const text = await bRes.text();
              console.warn("Cloud backup failed:", text);
              if (!isSilent) showToast("Lỗi đồng bộ Cloud: " + text, "error");
            }`
);

fs.writeFileSync('web/index.html', html);
console.log('Patched Backup');
