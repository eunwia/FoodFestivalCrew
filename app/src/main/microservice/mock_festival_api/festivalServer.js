const express = require('express');
const app = express();
app.use(express.json());

// ── api/server-log ──────────────────────────────────────────
// Returns a log with emails in {{email}} format
// Some valid, some that look blacklisted (not in latest logs)
app.get('/api/server-log', (req, res) => {
  res.send(`
[2025-02-20 08:00] System started
[2025-02-20 08:01] User login attempt: {{alice@food.my}}
[2025-02-20 08:05] User login attempt: {{bob@crew.my}}
[2025-02-20 09:00] User login attempt: {{chef.maria@food.my}}
[2025-02-20 10:00] INVALID attempt: notanemail
[2025-02-20 11:00] User login attempt: {{supervisor@festival.my}}
[2025-02-20 12:00] User login attempt: {{admin@food.my}}
[2025-02-20 13:00] Blacklisted: {{blocked@food.my}}
[2025-02-20 14:00] User login attempt: {{crew1@food.my}}
  `);
});

// ── api/token ────────────────────────────────────────────────
app.get('/api/token', (req, res) => {
  res.json({ token: 'FESTIVAL-TOKEN-ABC123XYZ' });
});

// ── api/food ─────────────────────────────────────────────────
app.get('/api/food', (req, res) => {
  const month = parseInt(req.query.month) || new Date().getMonth() + 1;
  const year  = parseInt(req.query.year)  || new Date().getFullYear();

  const foods = [];
  const themes = ['Red', 'Blue', 'Green', 'Yellow', 'Orange', 'Purple'];
  const names  = [
    'Nasi Lemak', 'Pad Thai', 'Rendang', 'Pho', 'Satay',
    'Laksa', 'Som Tum', 'Banh Mi', 'Char Kway Teow', 'Hainanese Chicken'
  ];

  // Generate food for each day of the given month
  const daysInMonth = new Date(year, month, 0).getDate();
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
    foods.push({
      id: (month * 100) + d,
      name: names[(d - 1) % names.length],
      date: dateStr,
      theme: themes[(d - 1) % themes.length]
    });
  }

  res.json({ data: foods });
});

// ── api/ingredients/{food_id} ────────────────────────────────
app.get('/api/ingredients', (req, res) => {
  // Returns ALL ingredients; CrewAPI will filter by food_id
  const allIngredients = [
    // food_id 201 (Jan day 1 = month 2 * 100 + 1 etc.)
    // We'll generate for a few food IDs for demo
    { id: 1, food_id: 101, name: 'Rice',       code: 'A', position: 1, prepare_time: 10, scoop: 2 },
    { id: 2, food_id: 101, name: 'Coconut',    code: 'B', position: 2, prepare_time:  5, scoop: 1 },
    { id: 3, food_id: 101, name: 'Sambal',     code: 'C', position: 3, prepare_time:  8, scoop: 1 },
    { id: 4, food_id: 102, name: 'Noodles',    code: 'A', position: 1, prepare_time: 12, scoop: 2 },
    { id: 5, food_id: 102, name: 'Egg',        code: 'B', position: 2, prepare_time:  3, scoop: 1 },
    { id: 6, food_id: 102, name: 'Bean Sprout',code: 'C', position: 3, prepare_time:  4, scoop: 1 },
    { id: 7, food_id: 102, name: 'Tofu',       code: 'D', position: 4, prepare_time:  6, scoop: 2 },
    { id: 8, food_id: 103, name: 'Beef',       code: 'A', position: 1, prepare_time: 20, scoop: 3 },
    { id: 9, food_id: 103, name: 'Lemongrass', code: 'B', position: 2, prepare_time:  5, scoop: 1 },
    { id:10, food_id: 103, name: 'Chilli',     code: 'C', position: 3, prepare_time:  3, scoop: 1 },
  ];
  res.json({ data: allIngredients });
});

// ── api/status ───────────────────────────────────────────────
let statusCallCount = 0;
app.get('/api/status', (req, res) => {
  statusCallCount++;
  const allMessages = [
    { id: 1, message: 'Packaging line A started' },
    { id: 2, message: 'Packaging line B started' },
    { id: 3, message: '[Alert] Low stock on Rice - reorder needed' },
    { id: 4, message: 'Container batch #001 completed' },
    { id: 5, message: 'Quality check passed for batch #001' },
    { id: 6, message: '[Alert] Packaging line C halted - maintenance' },
    { id: 7, message: 'Batch #002 packaging in progress' },
    { id: 8, message: 'All lines operational' },
	{ id: 9, message: 'Packaging line A started' },
    { id: 10, message: 'Packaging line B started' },
	{ id: 11, message: 'Packaging line A started' },
    { id: 12, message: 'Packaging line B started' },
    { id: 13, message: '[Alert] Low stock on Rice - reorder needed' },
    { id: 14, message: 'Container batch #001 completed' },
    { id: 15, message: 'Quality check passed for batch #001' },
    { id: 16, message: '[Alert] Packaging line C halted - maintenance' },
    { id: 17, message: 'Batch #002 packaging in progress' },
    { id: 18, message: 'All lines operational' },
	{ id: 19, message: 'Packaging line A started' },
    { id: 20, message: 'Packaging line B started' },
	{ id: 21, message: 'Packaging line A started' },
    { id: 22, message: 'Packaging line B started' },
    { id: 23, message: '[Alert] Low stock on Rice - reorder needed' },
    { id: 24, message: 'Container batch #001 completed' },
    { id: 25, message: 'Quality check passed for batch #001' },
    { id: 26, message: '[Alert] Packaging line C halted - maintenance' },
    { id: 27, message: 'Batch #002 packaging in progress' },
    { id: 28, message: 'All lines operational' },
	{ id: 29, message: 'Packaging line A started' },
    { id: 30, message: 'Packaging line B started' },
	

  ];
  // Return messages up to statusCallCount to simulate streaming new messages
  const toReturn = allMessages.slice(0, Math.min(statusCallCount * 2, allMessages.length));
  const done = toReturn.length >= allMessages.length;
  res.json({ data: toReturn, done: done });
});

// ── api/packaging ─────────────────────────────────────────────
app.get('/api/packaging', (req, res) => {
  const option = req.query.option || '';
  // Return steps for the packaging option
  const steps = generatePackagingSteps(option);
  res.json({ option: option, steps: steps, total_time: steps.reduce((s,x) => s + x.packaging_time, 0) });
});

function generatePackagingSteps(option) {
  // Parse option like "(A X B)", "((A X B) X C)" etc.
  if (!option) return [];
  const steps = [];
  // Simple demo steps based on option string
  const parts = option.replace(/[()]/g, '').split(' X ').map(s => s.trim());
  for (let i = 0; i < parts.length - 1; i++) {
    const a = parts[i], b = parts[i + 1];
    steps.push({
      step: i + 1,
      combination: `${a} X ${b}`,
      packaging_time: parseFloat((Math.random() * 20 + 5).toFixed(3))
    });
  }
  return steps;
}

app.listen(1234, () => console.log('Mock FoodFestivalAPI running on http://localhost:1234'));