const express = require('express');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for all origins and methods so cross-domain & cross-device requests work seamlessly
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200);
  }
  next();
});

// Middleware parsing JSON payload up to 50MB for backups
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Ensure backup storage directory exists
const DATA_DIR = path.join(__dirname, 'data', 'backups');
try {
  fs.mkdirSync(DATA_DIR, { recursive: true });
} catch (e) {
  console.error("Could not create data dir:", e);
}

// In-memory cache for fast cross-instance access
const memoryCache = new Map();

// Real-time SSE subscriber pool: email/key -> Set of client response objects
const sseClients = new Map();

function broadcastToClients(key, data) {
  const normalizedKey = (key || '').toLowerCase().trim();
  const clients = sseClients.get(normalizedKey);
  if (clients && clients.size > 0) {
    const payloadStr = `data: ${JSON.stringify(data)}\n\n`;
    for (const res of clients) {
      try {
        res.write(payloadStr);
      } catch (e) {
        clients.delete(res);
      }
    }
  }
}

// Helper to sanitize room/email/code to filename
function getBackupFilePath(key) {
  const safeName = (key || 'default').toLowerCase().replace(/[^a-z0-9_.-]/g, '_');
  return path.join(DATA_DIR, `backup_${safeName}.json`);
}

// Generate simple 6-digit sync code based on email or timestamp
function generateSyncCode(email) {
  let hash = 0;
  const str = (email || '') + '-' + Date.now();
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i);
    hash |= 0;
  }
  const codeNum = Math.abs(hash % 900000) + 100000;
  return String(codeNum);
}

// ==========================================
// GOOGLE DRIVE / ACCOUNT CLOUD SYNC ENDPOINTS
// ==========================================

// Real-time Server-Sent Events (SSE) stream endpoint (<50ms synchronization)
app.get('/api/cloud-sync/stream', (req, res) => {
  const email = req.query.email ? req.query.email.toLowerCase().trim() : 'drugunhp142@gmail.com';
  const syncCode = req.query.syncCode ? req.query.syncCode.trim() : null;

  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    'Connection': 'keep-alive',
    'Access-Control-Allow-Origin': '*'
  });

  res.write(`data: ${JSON.stringify({ type: 'CONNECTED', email, timestamp: new Date().toISOString() })}\n\n`);

  // Keep connection alive with periodic heartbeat
  const heartbeat = setInterval(() => {
    try {
      res.write(': heartbeat\n\n');
    } catch (e) {
      clearInterval(heartbeat);
    }
  }, 15000);

  // Register client
  const cleanEmail = email.toLowerCase().trim();
  const keys = [cleanEmail, 'global_sync'];
  if (syncCode) keys.push(`code_${syncCode.trim()}`);

  keys.forEach(k => {
    if (!sseClients.has(k)) sseClients.set(k, new Set());
    sseClients.get(k).add(res);
  });

  req.on('close', () => {
    clearInterval(heartbeat);
    keys.forEach(k => {
      if (sseClients.has(k)) {
        sseClients.get(k).delete(res);
        if (sseClients.get(k).size === 0) sseClients.delete(k);
      }
    });
  });
});

// API: Save / Backup Data from any device to Cloud Server
app.post('/api/cloud-sync/backup', (req, res) => {
  try {
    const { email, pin, payload, syncCode: customCode } = req.body;
    if (!email || !payload) {
      return res.status(400).json({ success: false, message: 'Email and payload are required' });
    }

    const cleanEmail = email.toLowerCase().trim();
    const syncCode = customCode || generateSyncCode(cleanEmail);
    const backupRecord = {
      email: cleanEmail,
      syncCode,
      pin: pin || null,
      updatedAt: new Date().toISOString(),
      payload
    };

    // Save by email
    const emailFilePath = getBackupFilePath(cleanEmail);
    fs.writeFileSync(emailFilePath, JSON.stringify(backupRecord, null, 2), 'utf-8');

    // Save by syncCode for fast 6-digit lookup
    const codeFilePath = getBackupFilePath(`code_${syncCode}`);
    fs.writeFileSync(codeFilePath, JSON.stringify(backupRecord, null, 2), 'utf-8');

    // Cache in memory
    memoryCache.set(cleanEmail, backupRecord);
    memoryCache.set(`code_${syncCode}`, backupRecord);
    memoryCache.set('global_latest', backupRecord);

    // Broadcast instant update in real-time to all connected devices / browsers (<50ms)
    const updateEvent = {
      type: 'LIVE_UPDATE',
      email: cleanEmail,
      syncCode,
      updatedAt: backupRecord.updatedAt,
      payload
    };

    broadcastToClients(cleanEmail, updateEvent);
    broadcastToClients('global_sync', updateEvent);
    if (syncCode) {
      broadcastToClients(`code_${syncCode}`, updateEvent);
    }

    return res.json({
      success: true,
      message: 'Backup stored successfully on cloud server',
      updatedAt: backupRecord.updatedAt,
      email: backupRecord.email,
      syncCode
    });
  } catch (error) {
    console.error("Backup error:", error);
    return res.status(500).json({ success: false, message: 'Server error saving backup: ' + error.message });
  }
});

// API: Restore Data to any device from Cloud Server (by email or 6-digit syncCode)
app.get('/api/cloud-sync/restore', (req, res) => {
  try {
    const email = req.query.email ? req.query.email.toLowerCase().trim() : null;
    const syncCode = req.query.syncCode ? req.query.syncCode.trim() : null;
    const pin = req.query.pin;

    if (!email && !syncCode) {
      return res.status(400).json({ success: false, message: 'Email or Sync Code is required' });
    }

    let backupRecord = null;

    // Check by sync code if provided
    if (syncCode) {
      if (memoryCache.has(`code_${syncCode}`)) {
        backupRecord = memoryCache.get(`code_${syncCode}`);
      } else {
        const codeFile = getBackupFilePath(`code_${syncCode}`);
        if (fs.existsSync(codeFile)) {
          backupRecord = JSON.parse(fs.readFileSync(codeFile, 'utf-8'));
        }
      }
    }

    // Check by email if not found yet
    if (!backupRecord && email) {
      if (memoryCache.has(email)) {
        backupRecord = memoryCache.get(email);
      } else {
        const emailFile = getBackupFilePath(email);
        if (fs.existsSync(emailFile)) {
          backupRecord = JSON.parse(fs.readFileSync(emailFile, 'utf-8'));
        }
      }
    }

    if (!backupRecord) {
      return res.json({
        success: true,
        exists: false,
        message: `Chưa tìm thấy bản sao lưu nào cho ${email || `Mã ${syncCode}`}. Vui lòng tạo sao lưu ở máy nguồn trước.`
      });
    }

    // If PIN protection is set
    if (backupRecord.pin && backupRecord.pin !== pin) {
      return res.status(401).json({
        success: false,
        message: 'Mã PIN bảo mật không chính xác!'
      });
    }

    return res.json({
      success: true,
      exists: true,
      email: backupRecord.email,
      syncCode: backupRecord.syncCode,
      updatedAt: backupRecord.updatedAt,
      payload: backupRecord.payload
    });
  } catch (error) {
    console.error("Restore error:", error);
    return res.status(500).json({ success: false, message: 'Server error restoring backup: ' + error.message });
  }
});

// API: Get Info / Status of cloud backup for an email or syncCode
app.get('/api/cloud-sync/info', (req, res) => {
  try {
    const email = req.query.email ? req.query.email.toLowerCase().trim() : null;
    const syncCode = req.query.syncCode ? req.query.syncCode.trim() : null;

    if (!email && !syncCode) return res.status(400).json({ success: false, message: 'Email or code required' });
    
    let backupRecord = null;
    if (syncCode && memoryCache.has(`code_${syncCode}`)) {
      backupRecord = memoryCache.get(`code_${syncCode}`);
    } else if (email && memoryCache.has(email)) {
      backupRecord = memoryCache.get(email);
    } else {
      const targetFile = syncCode ? getBackupFilePath(`code_${syncCode}`) : getBackupFilePath(email);
      if (fs.existsSync(targetFile)) {
        backupRecord = JSON.parse(fs.readFileSync(targetFile, 'utf-8'));
      }
    }

    if (!backupRecord) {
      return res.json({ success: true, exists: false });
    }

    const txCount = backupRecord.payload?.data?.transactions?.length || 0;
    const accCount = backupRecord.payload?.data?.accounts?.length || 0;

    return res.json({
      success: true,
      exists: true,
      updatedAt: backupRecord.updatedAt,
      email: backupRecord.email,
      syncCode: backupRecord.syncCode,
      hasPin: !!backupRecord.pin,
      txCount,
      accCount
    });
  } catch (e) {
    return res.status(500).json({ success: false, message: e.message });
  }
});

// Phục vụ file tĩnh từ thư mục web
app.use(express.static(path.join(__dirname, 'web')));

// Định tuyến tất cả request về index.html (SPA)
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'web', 'index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server is running on port ${PORT}`);
});
