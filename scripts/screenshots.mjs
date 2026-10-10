// Captures screenshots of the running web application for the README.
// Usage: node scripts/screenshots.mjs   (front end on http://localhost:8090)
import { chromium } from 'playwright';
import { mkdirSync } from 'node:fs';

const base = process.env.SDEM_UI || 'http://localhost:8090';
const out = 'docs/screenshots';
mkdirSync(out, { recursive: true });

const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
const errors = [];
page.on('pageerror', e => errors.push(e.message));
page.on('console', m => { if (m.type() === 'error') errors.push(m.text()); });

await page.goto(`${base}/login`);
await page.screenshot({ path: `${out}/01-login.png` });
await page.fill('input[name=username]', 'admin');
await page.fill('input[name=password]', 'admin123');
await page.click('button[type=submit]');
await page.waitForURL(`${base}/`);

const shots = [
  ['/', '02-region-overview.png'],
  ['/cities/newcastle', '03-city-newcastle.png'],
  ['/providers/b', '04-provider-b.png'],
  ['/admin/users', '05-manage-users.png'],
  ['/account/password', '06-change-password.png'],
];
for (const [path, file] of shots) {
  await page.goto(base + path);
  await page.waitForTimeout(1200); // let chart animations finish
  await page.screenshot({ path: `${out}/${file}`, fullPage: true });
}
// Mobile layout
await page.setViewportSize({ width: 390, height: 844 });
await page.goto(`${base}/cities/durham`);
await page.waitForTimeout(1200);
await page.screenshot({ path: `${out}/07-mobile-city.png`, fullPage: true });

const canvases = await page.locator('canvas').count();
await browser.close();
if (errors.length) {
  console.error('Browser errors:\n' + errors.join('\n'));
  process.exit(1);
}
console.log(`Screenshots saved to ${out} (${canvases} chart canvas on the last page)`);
