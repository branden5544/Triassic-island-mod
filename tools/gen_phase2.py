"""Generates phase-2 resources: DNA lab machines, power grid blocks, their items, GUI, recipes and names.
Run from anywhere: python3 tools/gen_phase2.py <repo root>"""
import json, os, random, struct, zlib, sys

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2); f.write("\n")

def png(path, px):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    h, wd = len(px), len(px[0])
    raw = b"".join(b"\x00" + bytes(c for p in row for c in p) for row in px)
    chunk = lambda t, d: struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", wd, h, 8, 6, 0, 0, 0))
                + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))

def canvas(wd=16, h=16, color=(0, 0, 0, 0)):
    return [[color] * wd for _ in range(h)]

def rgba(c, a=255):
    return tuple(c) + (a,)

def jitter(c, r, v):
    return tuple(max(0, min(255, x + r.randint(-v, v))) for x in c)

def rect(px, x0, y0, x1, y1, color):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[y][x] = rgba(color) if len(color) == 3 else color

# ---------------------------------------------------------------- block textures

def casing(seed, base=(150, 155, 160)):
    """Riveted steel panel with a bevelled edge."""
    r = random.Random(seed)
    px = [[rgba(jitter(base, r, 6)) for _ in range(16)] for _ in range(16)]
    for i in range(16):
        px[0][i] = px[i][0] = rgba((190, 195, 200))
        px[15][i] = px[i][15] = rgba((90, 95, 100))
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[y][x] = rgba((105, 110, 115))
    return px

def screen(px, x0, y0, x1, y1, glow):
    rect(px, x0 - 1, y0 - 1, x1 + 1, y1 + 1, (60, 62, 66))
    rect(px, x0, y0, x1, y1, (20, 28, 30))
    r = random.Random(x0 * 31 + y0)
    for y in range(y0, y1 + 1, 2):
        for x in range(x0, x0 + r.randint(1, x1 - x0)):
            px[y][x] = rgba(glow)

top = casing(10)
side = casing(11)
bottom = casing(12, (110, 115, 120))
png(f"{A}/textures/block/machine_top.png", top)
png(f"{A}/textures/block/machine_side.png", side)
png(f"{A}/textures/block/machine_bottom.png", bottom)

fronts = {}
# Fossil cleaner: water window with a fossil inside.
f = casing(20); rect(f, 3, 3, 12, 12, (60, 62, 66)); rect(f, 4, 4, 11, 11, (70, 120, 200))
for x, y in ((6, 7), (7, 7), (8, 7), (9, 7), (6, 8), (9, 8), (7, 6), (8, 9)): f[y][x] = rgba((225, 215, 185))
for x, y in ((5, 5), (10, 5), (5, 10)): f[y][x] = rgba((150, 190, 240))
fronts["fossil_cleaner"] = f
# DNA extractor: a syringe slot and a red sample light.
f = casing(21); screen(f, 3, 3, 12, 6, (90, 220, 120)); rect(f, 6, 9, 9, 13, (60, 62, 66)); rect(f, 7, 9, 8, 12, (200, 40, 40))
fronts["dna_extractor"] = f
# DNA sequencer: a big readout of base pairs.
f = casing(22); rect(f, 2, 2, 13, 13, (60, 62, 66)); rect(f, 3, 3, 12, 12, (15, 20, 25))
r = random.Random(5)
for y in range(4, 12, 2):
    for x in range(4, 12):
        f[y][x] = rgba(r.choice([(220, 60, 60), (60, 200, 90), (70, 120, 230), (230, 200, 60)]))
fronts["dna_sequencer"] = f
# Embryo fabricator: an egg under glass.
f = casing(23); rect(f, 3, 3, 12, 12, (60, 62, 66)); rect(f, 4, 4, 11, 11, (180, 210, 215))
for y in range(5, 12):
    for x in range(5, 11):
        if ((x - 7.5) / 2.6) ** 2 + ((y - 8.2) / 3.3) ** 2 < 1: f[y][x] = rgba((235, 225, 200))
fronts["embryo_fabricator"] = f
# Incubator: warm orange window.
f = casing(24); rect(f, 2, 3, 13, 12, (60, 62, 66)); rect(f, 3, 4, 12, 11, (230, 140, 40))
for x in range(4, 12, 3): rect(f, x, 6, x + 1, 9, (245, 225, 200))
fronts["incubator"] = f
# Combustion generator: firebox grill.
f = casing(25, (95, 98, 102)); rect(f, 3, 7, 12, 13, (40, 40, 42))
for x in range(4, 12): f[10 + (x % 2)][x] = rgba((240, 120, 30))
for x in range(4, 12, 2): rect(f, x, 3, x, 5, (60, 62, 66))
fronts["combustion_generator"] = f
for name, px in fronts.items():
    png(f"{A}/textures/block/{name}_front.png", px)

# Cable: black rubber with a copper core showing at the ends.
c = canvas()
r = random.Random(30)
for y in range(16):
    for x in range(16):
        c[y][x] = rgba(jitter((35, 35, 38), r, 5))
rect(c, 6, 6, 9, 9, (200, 110, 60))
png(f"{A}/textures/block/low_voltage_cable.png", c)

# Battery: casing with a charge gauge.
b = casing(31, (60, 90, 70)); rect(b, 4, 3, 11, 12, (40, 40, 42))
for y in range(5, 12, 2): rect(b, 5, y, 10, y, (90, 220, 120))
rect(b, 6, 1, 9, 2, (200, 110, 60))
png(f"{A}/textures/block/battery_side.png", b)
png(f"{A}/textures/block/battery_top.png", casing(32, (60, 90, 70)))

# Solar panel: blue cells with silver grid lines.
s = canvas()
r = random.Random(33)
for y in range(16):
    for x in range(16):
        s[y][x] = rgba((190, 195, 200)) if x % 5 == 0 or y % 5 == 0 else rgba(jitter((35, 60, 140), r, 10))
png(f"{A}/textures/block/solar_panel_top.png", s)
png(f"{A}/textures/block/solar_panel_side.png", casing(34))

# ---------------------------------------------------------------- item textures

def egg_shape(px, base, spots, seed):
    r = random.Random(seed)
    for y in range(16):
        for x in range(16):
            if ((x - 7.5) / 5.2) ** 2 + ((y - 8.5) / 6.8) ** 2 < 1:
                px[y][x] = rgba(jitter(base, r, 8))
    for _ in range(9):
        x, y = r.randint(4, 11), r.randint(4, 13)
        if px[y][x][3]: px[y][x] = rgba(spots)
    return px

png(f"{A}/textures/item/dinosaur_egg.png", egg_shape(canvas(), (225, 215, 175), (120, 140, 80), 40))

# Cleaned fossil: a bone.
bone = canvas()
for i in range(3, 13):
    bone[15 - i][i] = bone[15 - i - 1][i] = rgba((235, 228, 205))
for cx, cy in ((3, 12), (12, 3)):
    for dx, dy in ((-1, 0), (0, -1), (1, 1), (-1, 1), (1, -1), (0, 1), (1, 0)):
        bone[cy + dy][cx + dx] = rgba((235, 228, 205))
for i in range(4, 12, 3): bone[15 - i][i] = rgba((190, 180, 150))
png(f"{A}/textures/item/cleaned_fossil.png", bone)

# DNA sample: the syringe from phase 1, filled red.
syr = canvas()
for i in range(3, 13):
    for t in (-1, 0, 1):
        syr[15 - i][i + t] = rgba((210, 230, 240))
    syr[15 - i][i] = rgba((190, 30, 40))
for i in range(13, 16): syr[15 - i][i] = rgba((160, 160, 170))
for i in range(1, 3): syr[15 - i][i] = rgba((90, 90, 100))
png(f"{A}/textures/item/dna_sample.png", syr)

# Multimeter: yellow body, screen, dial.
mm = canvas()
rect(mm, 4, 1, 11, 14, (230, 190, 40)); rect(mm, 5, 2, 10, 5, (25, 35, 30)); rect(mm, 6, 3, 8, 3, (90, 220, 120))
rect(mm, 6, 7, 9, 10, (40, 40, 42)); mm[8][7] = rgba((220, 220, 220))
mm[12][6] = rgba((200, 40, 40)); mm[12][9] = rgba((30, 30, 30))
png(f"{A}/textures/item/multimeter.png", mm)

# ---------------------------------------------------------------- GUI texture (256x256)

g = canvas(256, 256)
LIGHT, MID, DARK, BLACK, WHITE = (198, 198, 198), (139, 139, 139), (85, 85, 85), (0, 0, 0), (255, 255, 255)
rect(g, 1, 1, 174, 164, LIGHT)
rect(g, 1, 0, 174, 0, BLACK); rect(g, 1, 165, 174, 165, BLACK); rect(g, 0, 1, 0, 164, BLACK); rect(g, 175, 1, 175, 164, BLACK)
rect(g, 1, 1, 173, 2, WHITE); rect(g, 1, 1, 2, 163, WHITE)
rect(g, 3, 163, 174, 164, DARK); rect(g, 173, 3, 174, 164, DARK)

def slot(px, x, y):
    rect(px, x, y, x + 17, y + 17, MID)
    rect(px, x, y, x + 16, y, (55, 55, 55)); rect(px, x, y, x, y + 16, (55, 55, 55))
    rect(px, x + 1, y + 17, x + 17, y + 17, WHITE); rect(px, x + 17, y + 1, x + 17, y + 17, WHITE)

for row in range(3):
    for col in range(9):
        slot(g, 7 + col * 18, 83 + row * 18)
for col in range(9):
    slot(g, 7 + col * 18, 141)
slot(g, 176, 0)  # slot sprite used for machine slots

def arrow(px, ox, oy, color):
    for y in range(17):
        for x in range(24):
            shaft = x < 15 and 6 <= y <= 10
            head = x >= 15 and abs(y - 8) <= 8 - (x - 15)
            if shaft or head: px[oy + y][ox + x] = rgba(color)

arrow(g, 176, 18, MID)
arrow(g, 200, 18, WHITE)

def flame(px, ox, oy, outer, inner):
    for y in range(14):
        for x in range(14):
            d = ((x - 6.5) / 5.5) ** 2 + ((y - 8.5) / 5.5) ** 2 - max(0, (8 - y) / 9) * 0.6
            if d < 1 and abs(x - 6.5) < 6.5 - max(0, 6 - y) * 0.9:
                px[oy + y][ox + x] = rgba(inner if d < 0.4 else outer)

flame(g, 176, 35, MID, MID)
flame(g, 190, 35, (230, 110, 20), (250, 220, 60))
png(f"{A}/textures/gui/machine.png", g)

# ---------------------------------------------------------------- models and blockstates

machines = list(fronts)
for name in machines:
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "top": f"{M}:block/machine_top", "bottom": f"{M}:block/machine_bottom",
        "side": f"{M}:block/machine_side", "front": f"{M}:block/{name}_front"}})
    w(f"{A}/blockstates/{name}.json", {"variants": {
        f"facing={d}": ({"model": f"{M}:block/{name}", "y": rot} if rot else {"model": f"{M}:block/{name}"})
        for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}"})

cable_tex = {"cable": f"{M}:block/low_voltage_cable", "particle": f"{M}:block/low_voltage_cable"}
def box(frm, to):
    return {"from": frm, "to": to, "faces": {f: {"texture": "#cable"} for f in ("north", "south", "east", "west", "up", "down")}}
w(f"{A}/models/block/low_voltage_cable_core.json", {"textures": cable_tex, "elements": [box([5, 5, 5], [11, 11, 11])]})
w(f"{A}/models/block/low_voltage_cable_side.json", {"textures": cable_tex, "elements": [box([5, 5, 0], [11, 11, 5])]})
w(f"{A}/blockstates/low_voltage_cable.json", {"multipart": [{"apply": {"model": f"{M}:block/low_voltage_cable_core"}}] + [
    {"when": {d: "true"}, "apply": dict({"model": f"{M}:block/low_voltage_cable_side"}, **rot)}
    for d, rot in (("north", {}), ("east", {"y": 90}), ("south", {"y": 180}), ("west", {"y": 270}),
                   ("up", {"x": 270}), ("down", {"x": 90}))]})
w(f"{A}/models/item/low_voltage_cable.json", {"parent": "minecraft:block/block", "textures": cable_tex,
    "elements": [box([5, 5, 0], [11, 11, 16])]})

w(f"{A}/models/block/battery.json", {"parent": "minecraft:block/cube_column", "textures": {
    "end": f"{M}:block/battery_top", "side": f"{M}:block/battery_side"}})
w(f"{A}/blockstates/battery.json", {"variants": {"": {"model": f"{M}:block/battery"}}})
w(f"{A}/models/item/battery.json", {"parent": f"{M}:block/battery"})

w(f"{A}/models/block/solar_panel.json", {"parent": "minecraft:block/block", "textures": {
    "top": f"{M}:block/solar_panel_top", "side": f"{M}:block/solar_panel_side", "particle": f"{M}:block/solar_panel_side"},
    "elements": [{"from": [0, 0, 0], "to": [16, 6, 16], "faces": {
        "up": {"texture": "#top"}, "down": {"texture": "#side", "cullface": "down"},
        **{f: {"texture": "#side", "uv": [0, 10, 16, 16], "cullface": f} for f in ("north", "south", "east", "west")}}}]})
w(f"{A}/blockstates/solar_panel.json", {"variants": {"": {"model": f"{M}:block/solar_panel"}}})
w(f"{A}/models/item/solar_panel.json", {"parent": f"{M}:block/solar_panel"})

for item in ("cleaned_fossil", "dna_sample", "dinosaur_egg", "multimeter"):
    w(f"{A}/models/item/{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/{item}"}})

# ---------------------------------------------------------------- loot tables and tags

grid_blocks = machines + ["low_voltage_cable", "battery", "solar_panel"]
for b in grid_blocks:
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick))
tag["values"] = sorted(set(tag["values"]) | {f"{M}:{b}" for b in machines + ["battery", "solar_panel"]})
w(pick, tag)

# ---------------------------------------------------------------- recipes

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})

I, C, G, R = "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:glass", "minecraft:redstone"
SYR = f"{M}:empty_syringe"
shaped("empty_syringe", [" G", "N "], {"G": "minecraft:glass_pane", "N": "minecraft:iron_nugget"}, 4)
shaped("multimeter", ["G", "R", "C"], {"G": "minecraft:glass_pane", "R": R, "C": C})
shaped("low_voltage_cable", ["WWW", "CCC", "WWW"], {"W": "#minecraft:wool", "C": C}, 12)
shaped("combustion_generator", ["III", "CFC", "IRI"], {"I": I, "C": C, "F": "minecraft:furnace", "R": R})
shaped("battery", ["ICI", "RRR", "ICI"], {"I": I, "C": C, "R": R})
shaped("solar_panel", ["GGG", "LCL", "III"], {"G": G, "L": "minecraft:lapis_lazuli", "C": C, "I": I})
shaped("fossil_cleaner", ["IGI", "GBG", "CRC"], {"I": I, "G": G, "B": "minecraft:bucket", "C": C, "R": R})
shaped("dna_extractor", ["IGI", "SRS", "ICI"], {"I": I, "G": G, "S": SYR, "R": R, "C": C})
shaped("dna_sequencer", ["IGI", "QPQ", "ICI"], {"I": I, "G": G, "Q": "minecraft:quartz", "P": "minecraft:comparator", "C": C})
shaped("embryo_fabricator", ["IGI", "SES", "ICI"], {"I": I, "G": G, "S": SYR, "E": "minecraft:egg", "C": C})
shaped("incubator", ["GGG", "WRW", "ICI"], {"G": G, "W": "#minecraft:wool", "R": R, "I": I, "C": C})

# ---------------------------------------------------------------- lang

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
names = {
    "fossil_cleaner": "Fossil Cleaner", "dna_extractor": "DNA Extractor", "dna_sequencer": "DNA Sequencer",
    "embryo_fabricator": "Embryo Fabricator", "incubator": "Incubator", "combustion_generator": "Combustion Generator",
}
for k, v in names.items():
    lang[f"block.{M}.{k}"] = f"{v} (50V)"
    lang[f"container.{M}.{k}"] = v
lang.update({
    f"block.{M}.low_voltage_cable": "Low Voltage Cable",
    f"block.{M}.battery": "Battery (50V)",
    f"block.{M}.solar_panel": "Small Solar Panel (50V)",
    f"item.{M}.cleaned_fossil": "Cleaned Fossil",
    f"item.{M}.dna_sample": "DNA Sample",
    f"item.{M}.dna_sample.of": "%s DNA Sample",
    f"item.{M}.dinosaur_egg": "Dinosaur Egg",
    f"item.{M}.dinosaur_egg.of": "%s Egg",
    f"item.{M}.multimeter": "Multimeter",
    f"tooltip.{M}.unknown_species": "Unknown species",
    f"tooltip.{M}.quality": "DNA quality: %s%%",
    f"tooltip.{M}.incubated": "Ready to hatch",
    f"gui.{M}.water": "Water: %s / %s mB",
    f"gui.{M}.needs_power": "No power - needs %s W at 50 V",
})
w(lang_path, lang)
print("ok")
