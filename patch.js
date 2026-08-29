const fs = require('fs');
let html = fs.readFileSync('web/index.html', 'utf8');

html = html.replace(
  /\} else if \(isNewer\) \{/,
  `} else if (remoteTime > localSavedTime && rTx && JSON.stringify(rTx) === JSON.stringify(transactions)) {
              // Same data, just update time to prevent re-fetching
              lastLocalEditTime.current = Date.now();
            } else if (isNewer) {`
);

fs.writeFileSync('web/index.html', html);
console.log('Patched');
