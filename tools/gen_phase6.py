"""Generates security resources: camera, monitor, keycard reader, alarm, keycards.
Run after gen_phase2.py: python3 tools/gen_phase6.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

# ---------------------------------------------------------------- textures

png(f"{A}/textures/block/camera_body.png", casing(80, (225, 228, 230)))
lens = canvas(); rect(lens, 0, 0, 15, 15, (30, 30, 34)); rect(lens, 4, 4, 11, 11, (15, 25, 45)); rect(lens, 5, 5, 7, 7, (90, 130, 200))
png(f"{A}/textures/block/camera_lens.png", lens)

screen_tex = canvas(); rect(screen_tex, 0, 0, 15, 15, (40, 42, 46)); rect(screen_tex, 1, 1, 14, 14, (8, 12, 10))
png(f"{A}/textures/block/monitor_front.png", screen_tex)
png(f"{A}/textures/block/monitor_side.png", casing(81, (60, 62, 66)))

def reader(on):
    px = casing(82, (55, 57, 62))
    rect(px, 3, 4, 12, 5, (20, 20, 22))                       # card slot
    rect(px, 6, 9, 9, 11, (60, 220, 90) if on else (200, 40, 40))
    for x in (4, 6, 8, 10):
        for y in (13, 14): px[y][x] = rgba((120, 120, 125))      # keypad
    return px
png(f"{A}/textures/block/keycard_reader.png", reader(False))
png(f"{A}/textures/block/keycard_reader_on.png", reader(True))

def alarm(lit):
    px = canvas()
    r = random.Random(83)
    base = (255, 70, 50) if lit else (150, 25, 20)
    for y in range(16):
        for x in range(16):
            px[y][x] = rgba(jitter(base, r, 12))
    rect(px, 5, 5, 7, 7, (255, 200, 180) if lit else (200, 90, 80))
    return px
png(f"{A}/textures/block/alarm.png", alarm(False))
png(f"{A}/textures/block/alarm_on.png", alarm(True))
png(f"{A}/textures/block/alarm_base.png", casing(84, (60, 62, 66)))

CARD_COLORS = {1: (240, 240, 240), 2: (90, 170, 230), 3: (90, 200, 110), 4: (240, 190, 50), 5: (220, 60, 60)}
for level, color in CARD_COLORS.items():
    card = canvas()
    rect(card, 1, 4, 14, 12, color)
    rect(card, 1, 6, 14, 7, (30, 30, 30))                      # magnetic stripe
    rect(card, 3, 9, 5, 10, (230, 200, 80))                     # chip
    for i in range(level): card[10][8 + i] = rgba((30, 30, 30))  # level pips
    png(f"{A}/textures/item/keycard_{level}.png", card)

# ---------------------------------------------------------------- models (all face north, mounted on the south side)

def box(frm, to, tex, front=None):
    faces = {f: {"texture": tex} for f in ("north", "south", "east", "west", "up", "down")}
    if front: faces["north"] = {"texture": front}
    return {"from": frm, "to": to, "faces": faces}

def model(name, textures, elements):
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/block", "textures": textures, "elements": elements})

def facing_states(name, variants=None):
    """variants: {"prop=value": model suffix}; facing is added for every one."""
    variants = variants or {"": ""}
    out = {}
    for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for prop, suffix in variants.items():
            key = f"facing={d}" + ("," + prop if prop else "")
            out[key] = dict({"model": f"{M}:block/{name}{suffix}"}, **({"y": rot} if rot else {}))
    w(f"{A}/blockstates/{name}.json", {"variants": out})

model("security_camera", {"body": f"{M}:block/camera_body", "lens": f"{M}:block/camera_lens", "particle": f"{M}:block/camera_body"}, [
    box([6, 8, 13], [10, 12, 16], "#body"),
    box([4, 7, 6], [12, 12, 13], "#body", "#lens"),
    box([6, 8, 5], [10, 11, 6], "#lens"),
])
facing_states("security_camera")

model("monitor", {"side": f"{M}:block/monitor_side", "screen": f"{M}:block/monitor_front", "particle": f"{M}:block/monitor_side"}, [
    box([0, 0, 13], [16, 16, 16], "#side", "#screen")])
facing_states("monitor")

for suffix, tex in (("", "keycard_reader"), ("_on", "keycard_reader_on")):
    model(f"keycard_reader{suffix}", {"front": f"{M}:block/{tex}", "side": f"{M}:block/monitor_side", "particle": f"{M}:block/{tex}"}, [
        box([5, 3, 14], [11, 13, 16], "#side", "#front")])
facing_states("keycard_reader", {"powered=false": "", "powered=true": "_on"})

for suffix, tex in (("", "alarm"), ("_on", "alarm_on")):
    model(f"alarm{suffix}", {"dome": f"{M}:block/{tex}", "base": f"{M}:block/alarm_base", "particle": f"{M}:block/{tex}"}, [
        box([4, 4, 14], [12, 12, 16], "#base"),
        box([5, 5, 11], [11, 11, 14], "#dome"),
    ])
facing_states("alarm", {"lit=false": "", "lit=true": "_on"})

for name in ("security_camera", "monitor", "keycard_reader", "alarm"):
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}"})
for level in CARD_COLORS:
    w(f"{A}/models/item/keycard_{level}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/keycard_{level}"}})

# ---------------------------------------------------------------- data

blocks = ["security_camera", "monitor", "keycard_reader", "alarm"]
for b in blocks:
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick))
tag["values"] = sorted(set(tag["values"]) | {f"{M}:{b}" for b in blocks})
w(pick, tag)

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})

def shapeless(name, ingredients, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"tag": i[1:]} if i.startswith("#") else {"item": i} for i in ingredients],
        "result": {"item": f"{M}:{name}", "count": count}})

I, R, DC = "minecraft:iron_ingot", "minecraft:redstone", f"{M}:data_cable"
shaped("security_camera", ["III", "IGR", " D "], {"I": I, "G": "minecraft:glass", "R": R, "D": DC})
shaped("monitor", ["III", "IGI", "IDI"], {"I": I, "G": "minecraft:glass_pane", "D": DC})
shaped("keycard_reader", ["IRI", "IDI"], {"I": I, "R": R, "D": DC})
shaped("alarm", ["IRI", "RNR", "IDI"], {"I": I, "R": R, "N": "minecraft:note_block", "D": DC})
shaped("keycard_1", ["PPP", "GRG"], {"P": "minecraft:paper", "G": "minecraft:gold_nugget", "R": R}, 2)
for level, extra in ((2, "minecraft:iron_ingot"), (3, "minecraft:gold_ingot"), (4, "minecraft:diamond"), (5, "minecraft:emerald")):
    shapeless(f"keycard_{level}", [f"{M}:keycard_{level - 1}", extra])

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({
    f"block.{M}.security_camera": "Security Camera (50V)",
    f"block.{M}.monitor": "Monitor (50V)",
    f"block.{M}.keycard_reader": "Keycard Reader (50V)",
    f"block.{M}.alarm": "Alarm (50V)",
    **{f"item.{M}.keycard_{level}": f"Keycard (Level {level})" for level in CARD_COLORS},
    f"tooltip.{M}.keycard": "Access level %s",
    f"message.{M}.reader.level": "Reader now needs level %s",
    f"message.{M}.reader.needs": "Needs a level %s keycard",
    f"message.{M}.reader.denied": "Access denied - needs level %s",
    f"message.{M}.reader.no_power": "The reader has no power",
    f"gui.{M}.camera": "Security Camera",
    f"gui.{M}.camera.no_signal": "NO SIGNAL",
    f"gui.{M}.camera.help": "A/D pan   W/S tilt   Left/Right switch camera   Esc back",
})
w(lang_path, lang)
print("ok")
