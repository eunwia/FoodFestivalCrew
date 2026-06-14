const express = require('express');
const router = express.Router();
const axios = require('axios');

const FESTIVAL = 'http://localhost:1234';

// GET /api/ingredients/:foodOfDayId
// Retrieves all ingredients then filters by food_id
// Response format matches original FoodFestivalAPI format exactly
router.get('/ingredients/:foodOfDayId', async (req, res) => {
  try {
    const { foodOfDayId } = req.params;
    const response = await axios.get(`${FESTIVAL}/api/ingredients`, {
      headers: forwardToken(req)
    });

    const raw = response.data;

    if (Array.isArray(raw)) {
      const filtered = raw.filter(i =>
        String(i.food_id ?? i.foodId ?? i.food_of_day_id) === String(foodOfDayId)
      );
      return res.json(filtered);
    }

    if (raw?.data && Array.isArray(raw.data)) {
      const filtered = raw.data.filter(i =>
        String(i.food_id ?? i.foodId ?? i.food_of_day_id) === String(foodOfDayId)
      );
      return res.json({ ...raw, data: filtered });
    }

    res.json(raw);
  } catch (e) {
    res.status(500).json({ message: 'Failed to fetch ingredients', error: e.message });
  }
});

function forwardToken(req) {
  const t = req.headers['x-token'] || req.headers['authorization'] || '';
  return t ? { 'x-token': t, 'Authorization': t } : {};
}

module.exports = router;