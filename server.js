// Máy chủ tĩnh tối giản cho bản web (Render.com / chạy local): chỉ phục vụ thư mục web/.
// Đồng bộ dữ liệu đi thẳng qua Firebase, không qua máy chủ này.
const express = require('express');
const path = require('path');

const app = express();
const PORT = process.env.DEFAULT_APP_PORT || process.env.PORT || 3000;

app.use(express.static(path.join(__dirname, 'web')));
app.get('*', (req, res) => res.sendFile(path.join(__dirname, 'web', 'index.html')));

app.listen(PORT, '0.0.0.0', () => console.log(`VinaSpends chạy tại http://localhost:${PORT}`));
