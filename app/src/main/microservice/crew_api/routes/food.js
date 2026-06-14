const express = require('express');
const router = express.Router();
const axios = require('axios');

const FESTIVAL = 'http://localhost:1234';

router.get('/food', async (req, res) => {
  try {
    const { month, year } = req.query;
    const response = await axios.get(`${FESTIVAL}/api/food`, {
      params: { month, year },
      headers: forwardToken(req)
    });

    let data = response.data;

    // Sort by date ascending
    if (Array.isArray(data)) {
      data.sort((a, b) => new Date(a.date) - new Date(b.date));
    } else if (data?.data && Array.isArray(data.data)) {
      data.data.sort((a, b) => new Date(a.date) - new Date(b.date));
    }

    res.json(data);
  } catch (e) {
    res.status(500).json({ message: 'Failed to fetch food', error: e.message });
  }
});

function forwardToken(req) {
  const t = req.headers['x-token'] || req.headers['authorization'] || '';
  return t ? { 'x-token': t, 'Authorization': t } : {};
}

module.exports = router;

