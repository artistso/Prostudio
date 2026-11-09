"""Draws ProStudio's app icon, one confident brush stroke closed into a ring — an ensō in the
brand blend, amber to ultraviolet over warm ink — and writes every size the app and the
repository use. Needs Pillow. Run from anywhere:

    python tools/app-icon/make_icon.py

Writes:
- assets/app-icon/prostudio-1024.png, prostudio-512.png, prostudio-256.png: the icon on its rounded
  tile (the desktop app's window and Linux menus use the 256 and 512 ones).
- assets/app-icon/prostudio.ico: the tile at every size Windows shows (the desktop app's .exe and
  its installer).
- assets/app-icon/hicolor/<n>x<n>/apps/prostudio.png: the tile at the sizes Linux desktops look up
  for the app's menu entry and window (copied as it is into the packages' share/icons/).
- the Android adaptive icon's layers (108 dp, the tile its middle 72 dp): the stroke (drawable-*dpi/
  ic_launcher_foreground.png), the stroke alone in white for themed icons
  (drawable-*dpi/ic_launcher_monochrome.png), and a large copy for the About screen
  (drawable-nodpi/prostudio_mark.png).

The mark is drawn, not pasted: the ring is stamped along its own arc, so both ends taper like a
pull of the brush, and the tail breaks into ragged dry-brush stamps. The stamps come from a seeded
generator, so the icon comes out the same on every run.
"""

import math
import os
import random

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RES = os.path.join(ROOT, 'android', 'app', 'src', 'main', 'res')

S = 2048  # the 512-unit tile, drawn at 4x
INK = (23, 20, 28)          # warm ink, the same the app's theme sits on
INK_HI = (38, 34, 48)       # a breath of violet in the tile's top light
# the blend: amber, ember, rose, ultraviolet
STOPS = [(0.0, (255, 196, 61)), (0.34, (255, 107, 74)), (0.68, (236, 72, 120)), (1.0, (139, 92, 246))]
# the arc: from the gap's lower lip round, counter-clockwise, to the drying tail above it
A_START = math.radians(232)   # where the stroke is picked up (radians, PIL's clockwise-from-3-o'clock)
A_SWEEP = math.radians(296)   # how far the stroke travels
R = S * 0.295                 # the ring's radius
W = S * 0.088                 # the stroke's belly
SEED = 20261109


def blend(t):
    t = min(1.0, max(0.0, t))
    for (a, ca), (b, cb) in zip(STOPS, STOPS[1:]):
        if t <= b:
            u = (t - a) / (b - a)
            return tuple(round(ca[i] + (cb[i] - ca[i]) * u) for i in range(3))
    return STOPS[-1][1]


def radius_at(t):
    """The stroke's half-width along its arc: a long soft shoulder at the start, full through the
    belly, then a long dry fall-off at the tail."""
    rise = min(1.0, t / 0.10)
    rise = rise * rise * (3 - 2 * rise)  # smoothstep shoulder
    fall = min(1.0, (1 - t) / 0.34)
    fall = fall * fall * (3 - 2 * fall)  # smoothstep dry-out, longer than the rise
    return W * (0.16 + 0.84 * min(rise, 1.0)) * (0.30 + 0.70 * fall)


def stroke():
    """The ring: discs stamped along its arc, each taking its colour from where it sits in the blend
    and its size from [`radius_at`]; the tail breaks into dry-brush flecks."""
    layer = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    cx = cy = S / 2
    length = R * A_SWEEP
    steps = int(length / (W * 0.16))          # a stamp every 16% of its width, dense enough to overlap
    rng = random.Random(SEED)
    for i in range(steps + 1):
        t = i / steps
        angle = A_START - A_SWEEP * t         # the stroke travels counter-clockwise
        x, y = cx + R * math.cos(angle), cy + R * math.sin(angle)
        r = radius_at(t)
        d.ellipse((x - r, y - r, x + r, y + r), fill=blend(t) + (255,))
        # dry brush at the tail: a sparse spray of small flecks just off the stroke's last fifth
        if t > 0.78 and rng.random() < 0.55:
            out = rng.uniform(0.08, 0.5) * W * (1.6 - t)      # just past the stroke's edge
            side = 1 if rng.random() < 0.65 else -1            # usually outward, sometimes within
            along = rng.uniform(-0.8, 0.8) * W                 # smeared along the stroke
            fleck = rng.uniform(0.04, 0.13) * W * (1.6 - t)    # small: a spray of paint
            cos_a, sin_a = math.cos(angle), math.sin(angle)
            fx = x + cos_a * (r * 0.6 + out) * side + math.cos(angle + math.pi / 2) * along
            fy = y + sin_a * (r * 0.6 + out) * side + math.sin(angle + math.pi / 2) * along
            alpha = round(255 * rng.uniform(0.6, 0.95))
            d.ellipse((fx - fleck, fy - fleck, fx + fleck, fy + fleck), fill=blend(t) + (alpha,))
    # a bright fleck of amber where the brush touched down
    d.ellipse((cx + R * math.cos(A_START) - W * 0.30, cy + R * math.sin(A_START) - W * 0.30,
               cx + R * math.cos(A_START) + W * 0.30, cy + R * math.sin(A_START) + W * 0.30),
              fill=(255, 226, 150, 255))
    return layer


def glow(turned):
    """A soft ember glow hugging the stroke."""
    alpha = turned.split()[3].filter(ImageFilter.GaussianBlur(46)).point(lambda a: a * 0.4)
    ember = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    warm = Image.new('RGB', (S, S), (255, 110, 70))
    ember.paste(warm, (0, 0), alpha)
    return ember


def tile_body():
    """The tile: warm ink with a breath of violet in its top light."""
    tile = Image.new('RGBA', (S, S), INK + (255,))
    ramp = Image.new('L', (1, S))
    for y in range(S):
        ramp.putpixel((0, y), round(90 * (1 - y / S) ** 2))
    light = Image.new('RGBA', (S, S), INK_HI + (0,))
    light.putalpha(ramp.resize((S, S), Image.NEAREST))
    tile.alpha_composite(light)
    return tile


def tile_mask():
    m = Image.new('L', (S, S), 0)
    ImageDraw.Draw(m).rounded_rectangle((0, 0, S - 1, S - 1), radius=112 * 4, fill=255)
    return m


def adaptive(picture):
    """`picture` (the tile's contents) placed in the middle 72 of the adaptive icon's 108."""
    side = round(S * 108 / 72)
    out = Image.new('RGBA', (side, side), (0, 0, 0, 0))
    out.alpha_composite(picture, ((side - S) // 2, (side - S) // 2))
    return out


def save(image, path, size):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.resize((size, size), Image.LANCZOS).save(path, optimize=True)


def main():
    mark = stroke()
    picture = glow(mark)
    picture.alpha_composite(mark)

    tile = tile_body()
    tile.alpha_composite(picture)
    tile.putalpha(tile_mask())
    for size in (1024, 512, 256):
        save(tile, os.path.join(ROOT, 'assets', 'app-icon', f'prostudio-{size}.png'), size)
    sizes = [(n, n) for n in (16, 20, 24, 32, 40, 48, 64, 128, 256)]
    tile.resize((256, 256), Image.LANCZOS).save(os.path.join(ROOT, 'assets', 'app-icon', 'prostudio.ico'), sizes=sizes)
    for size in (16, 24, 32, 48, 64, 128, 256, 512):
        save(tile, os.path.join(ROOT, 'assets', 'app-icon', 'hicolor', f'{size}x{size}', 'apps', 'prostudio.png'), size)

    foreground = adaptive(picture)
    mono = Image.new('RGBA', mark.size, (255, 255, 255, 0))
    mono.putalpha(mark.split()[3])
    monochrome = adaptive(mono)
    for bucket, scale in (('mdpi', 1), ('hdpi', 1.5), ('xhdpi', 2), ('xxhdpi', 3), ('xxxhdpi', 4)):
        size = round(108 * scale)
        save(foreground, os.path.join(RES, f'drawable-{bucket}', 'ic_launcher_foreground.png'), size)
        save(monochrome, os.path.join(RES, f'drawable-{bucket}', 'ic_launcher_monochrome.png'), size)
    # the About screen shows it 450 dp across: sharp up to about 3 pixels a dp
    save(foreground, os.path.join(RES, 'drawable-nodpi', 'prostudio_mark.png'), 1350)
    print('written')


if __name__ == '__main__':
    main()
