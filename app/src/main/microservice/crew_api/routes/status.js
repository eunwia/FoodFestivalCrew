const express = require('express');
const router = express.Router();
const axios = require('axios');

const FESTIVAL = 'http://localhost:1234';

router.get('/status', async (req, res) => {
  try {
    const response = await axios.get(`${FESTIVAL}/api/status`, {
      headers: forwardToken(req)
    });
    res.json(response.data);
  } catch (e) {
    res.status(500).json({ message: 'Failed to fetch status', error: e.message });
  }
});

function forwardToken(req) {
  const t = req.headers['x-token'] || req.headers['authorization'] || '';
  return t ? { 'x-token': t, 'Authorization': t } : {};
}

module.exports = router;