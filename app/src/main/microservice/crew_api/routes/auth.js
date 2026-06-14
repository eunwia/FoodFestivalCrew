const express = require('express');
const router = express.Router();
const axios = require('axios');
const db = require('../database');

const FESTIVAL = 'http://localhost:1234';

// Parse {{email@example.com}} patterns from server-log
function parseEmails(text) {
  const results = [];
  const regex = /\{\{([^}]+)\}\}/g;
  let m;
  while ((m = regex.exec(text)) !== null) {
    const raw = m[1].trim();
    if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(raw)) {
      results.push(raw.toLowerCase());
    }
  }
  return results;
}

async function syncFromServerLog() {
  try {
    const res = await axios.get(`${FESTIVAL}/api/server-log`);
    const text = typeof res.data === 'string' ? res.data : JSON.stringify(res.data);
    const emails = parseEmails(text);
    const stmt = db.prepare(`INSERT OR IGNORE INTO users (email, authorized) VALUES (?, 1)`);
    for (const email of emails) stmt.run(email);
    return emails;
  } catch (e) {
    console.error('server-log sync failed:', e.message);
    return [];
  }
}

// POST /api/login
router.post('/login', async (req, res) => {
  const email = (req.body.email || '').trim().toLowerCase();
  if (!email) return res.status(400).json({ message: 'Email required' });

  await syncFromServerLog();

  // Check DB
  const user = db.prepare(`SELECT * FROM users WHERE email = ? AND authorized = 1`).get(email);

  if (user) {
    try {
      const tokenRes = await axios.get(`${FESTIVAL}/api/token`);
      const token = tokenRes.data.token || tokenRes.data;
      return res.json({ result: 'Authorized Credential', token, email });
    } catch {
      return res.json({ result: 'Authorized Credential', token: null, email });
    }
  } else {
    return res.json({ result: 'Unauthorized Credential', token: null, email });
  }
});

// GET /api/credentials — list all authorized users
router.get('/credentials', (req, res) => {
  const users = db.prepare(`SELECT email, authorized, created_at FROM users`).all();
  res.json({ authorized_credentials: users });
});

module.exports = router;