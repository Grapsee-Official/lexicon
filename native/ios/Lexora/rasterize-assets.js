// One-shot rasterizer: converts lexora-mark.svg + lexora-countryside-day.svg
// to PNG @1x/@2x/@3x and copies all existing PNG illustrations into Resources/.
// Run with: node /home/z/my-project/native/ios/Lexora/rasterize-assets.js
const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

const SRC_SVG_DIR = '/home/z/my-project/public/assets';
const SRC_PNG_DIR = '/home/z/my-project/public/illustrations';
const DEST = '/home/z/my-project/native/ios/Lexora/Resources';

fs.mkdirSync(DEST, { recursive: true });

// SVG sources to rasterize. Output sizes are tuned so the @3x feels crisp.
// mark: 40x40 logical -> 40/80/120
// countryside: keep natural aspect, output 480px tall (≈ @3x of 160pt).
const jobs = [
  {
    svg: path.join(SRC_SVG_DIR, 'lexora-mark.svg'),
    outputs: [
      { file: 'lexora-mark.png', w: 40 },
      { file: 'lexora-mark@2x.png', w: 80 },
      { file: 'lexora-mark@3x.png', w: 120 },
    ],
  },
  {
    svg: path.join(SRC_SVG_DIR, 'lexora-countryside-day.svg'),
    outputs: [
      { file: 'lexora-countryside.png', w: 240 },
      { file: 'lexora-countryside@2x.png', w: 480 },
      { file: 'lexora-countryside@3x.png', w: 720 },
    ],
  },
];

(async () => {
  for (const job of jobs) {
    if (!fs.existsSync(job.svg)) {
      console.warn('MISSING SVG:', job.svg);
      continue;
    }
    for (const o of job.outputs) {
      await sharp(job.svg, { density: 384 })
        .resize({ width: o.w, withoutEnlargement: false })
        .png()
        .toFile(path.join(DEST, o.file));
      console.log('RASTER:', o.file);
    }
  }

  // Copy all PNG illustrations (article, category, hero, wotd, avatar, paper)
  for (const f of fs.readdirSync(SRC_PNG_DIR)) {
    if (!f.endsWith('.png')) continue;
    const src = path.join(SRC_PNG_DIR, f);
    const dst = path.join(DEST, f);
    fs.copyFileSync(src, dst);
    console.log('COPY:', f);
  }

  console.log('DONE');
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
