"""Generates computer-phase resources: data/combined cables, hidden cables, computer, Engineer's Goggles.
Run after gen_phase2.py: python3 tools/gen_phase5.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

DATA_BLUE, COPPER = (40, 110, 220), (200, 110, 60)

# ---------------------------------------------------------------- textures

r = random.Random(70)
data = [[rgba(jitter((30, 45, 80), r, 6)) for _ in range(16)] for _ in range(16)]
for i in range(0, 16, 3):
    data[i][7] = data[i][8] = rgba(DATA_BLUE)
rect(data, 6, 6, 9, 9, (230, 190, 60))
png(f"{A}/textures/block/data_cable.png", data)

r = random.Random(71)
combined = [[rgba(jitter((35, 35, 38), r, 5)) for _ in range(16)] for _ in range(16)]
for i in range(16):
    if i % 4 < 2:
        combined[i][2] = combined[i][13] = combined[2][i] = combined[13][i] = rgba(DATA_BLUE)
rect(combined, 5, 6, 7, 9, COPPER); rect(combined, 8, 6, 10, 9, (230, 190, 60))
png(f"{A}/textures/block/combined_cable.png", combined)

HIDDEN = {"hidden_low_voltage_cable": [COPPER], "hidden_medium_voltage_cable": [(200, 40, 40)],
          "hidden_high_voltage_cable": [(230, 200, 40)], "hidden_data_cable": [DATA_BLUE],
          "hidden_combined_cable": [COPPER, DATA_BLUE]}
for name, colors in HIDDEN.items():
    px = casing(72, (95, 98, 105))
    rect(px, 3, 3, 12, 12, (40, 40, 44))
    for k, color in enumerate(colors):
        if len(colors) == 1:
            rect(px, 5, 5, 10, 10, color)
        else:
            rect(px, 4 + k * 4, 5, 6 + k * 4 + 1, 10, color)
    for x in (1, 14):
        for y in (1, 14): px[y][x] = rgba((60, 62, 66))
    png(f"{A}/textures/block/{name}.png", px)

pc = casing(73, (205, 205, 200))
rect(pc, 2, 2, 13, 10, (60, 62, 66)); rect(pc, 3, 3, 12, 9, (10, 20, 12))
for x in range(4, 7): pc[4][x] = rgba((51, 255, 102))
pc[6][4] = rgba((51, 255, 102)); pc[6][5] = rgba((51, 255, 102))
rect(pc, 3, 12, 12, 13, (150, 150, 145)); pc[12][11] = rgba((60, 200, 90))
png(f"{A}/textures/block/computer_front.png", pc)
png(f"{A}/textures/block/computer_side.png", casing(74, (205, 205, 200)))
png(f"{A}/textures/block/computer_top.png", casing(75, (190, 190, 185)))

# Goggles item: brass rims, blue lenses, leather strap.
g = canvas()
rect(g, 0, 7, 15, 8, (90, 60, 35))
for cx in (4, 11):
    for y in range(16):
        for x in range(16):
            d = (x - cx) ** 2 + (y - 7.5) ** 2
            if d < 12: g[y][x] = rgba((200, 160, 60))
            if d < 5: g[y][x] = rgba((120, 190, 230))
png(f"{A}/textures/item/engineers_goggles.png", g)

# Armor texture (64x32): a strap around the head at eye height, lenses on the face.
armor = canvas(64, 32)
for x0 in (0, 8, 16, 24):           # right, front, left, back faces of the helmet layer
    for x in range(x0, x0 + 8):
        for y in (11, 12):
            armor[y][x] = rgba((90, 60, 35))
for cx in (9.5, 13.5):
    for y in range(9, 15):
        for x in range(8, 16):
            d = (x - cx) ** 2 + (y - 11.5) ** 2
            if d < 4.5: armor[y][x] = rgba((200, 160, 60))
            if d < 1.6: armor[y][x] = rgba((120, 190, 230))
png(f"{A}/textures/models/armor/goggles_layer_1.png", armor)
png(f"{A}/textures/models/armor/goggles_layer_2.png", canvas(64, 32))

# ---------------------------------------------------------------- models and blockstates

ROT6 = {"north": {}, "south": {"y": 180}, "east": {"y": 90}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}

def cable_models(name, lo, hi):
    tex = {"cable": f"{M}:block/{name}", "particle": f"{M}:block/{name}"}
    box = lambda frm, to: {"from": frm, "to": to, "faces": {f: {"texture": "#cable"} for f in ("north", "south", "east", "west", "up", "down")}}
    w(f"{A}/models/block/{name}_core.json", {"textures": tex, "elements": [box([lo, lo, lo], [hi, hi, hi])]})
    w(f"{A}/models/block/{name}_side.json", {"textures": tex, "elements": [box([lo, lo, 0], [hi, hi, lo])]})
    w(f"{A}/blockstates/{name}.json", {"multipart": [{"apply": {"model": f"{M}:block/{name}_core"}}] + [
        {"when": {d: "true"}, "apply": dict({"model": f"{M}:block/{name}_side"}, **ROT6[d])} for d in ROT6]})
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:block/block", "textures": tex, "elements": [box([lo, lo, 0], [hi, hi, 16])]})

cable_models("data_cable", 6, 10)
cable_models("combined_cable", 5, 11)

for name in HIDDEN:
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{M}:block/{name}"}})
    w(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{M}:block/{name}"}}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}"})

w(f"{A}/models/block/computer.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
    "top": f"{M}:block/computer_top", "bottom": f"{M}:block/machine_bottom",
    "side": f"{M}:block/computer_side", "front": f"{M}:block/computer_front"}})
w(f"{A}/blockstates/computer.json", {"variants": {
    f"facing={d}": dict({"model": f"{M}:block/computer"}, **({"y": rot} if rot else {}))
    for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
w(f"{A}/models/item/computer.json", {"parent": f"{M}:block/computer"})
w(f"{A}/models/item/engineers_goggles.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/engineers_goggles"}})

# ---------------------------------------------------------------- data

blocks = ["data_cable", "combined_cable", "computer"] + list(HIDDEN)
for b in blocks:
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick))
tag["values"] = sorted(set(tag["values"]) | {f"{M}:{b}" for b in ["computer"] + list(HIDDEN)})
w(pick, tag)

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})

def shapeless(name, ingredients, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"tag": i[1:]} if i.startswith("#") else {"item": i} for i in ingredients],
        "result": {"item": f"{M}:{name}", "count": count}})

shaped("data_cable", ["WWW", "RGR", "WWW"], {"W": "#minecraft:wool", "R": "minecraft:redstone", "G": "minecraft:gold_ingot"}, 12)
shapeless("combined_cable", [f"{M}:low_voltage_cable", f"{M}:data_cable"])
for name in HIDDEN:
    shapeless(name, [f"{M}:{name[len('hidden_'):]}", "#minecraft:stone_crafting_materials"])
shaped("computer", ["IGI", "RCR", "IDI"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass_pane", "R": "minecraft:redstone",
                                          "C": "minecraft:comparator", "D": f"{M}:data_cable"})
shaped("engineers_goggles", ["LLL", "GIG"], {"L": "minecraft:leather", "G": "minecraft:glass_pane", "I": "minecraft:gold_ingot"})

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({
    f"block.{M}.data_cable": "Data Cable",
    f"block.{M}.combined_cable": "Combined Cable (50V + Data)",
    f"block.{M}.hidden_low_voltage_cable": "Hidden Low Voltage Cable (50V)",
    f"block.{M}.hidden_medium_voltage_cable": "Hidden Medium Voltage Cable (200V)",
    f"block.{M}.hidden_high_voltage_cable": "Hidden High Voltage Cable (800V)",
    f"block.{M}.hidden_data_cable": "Hidden Data Cable",
    f"block.{M}.hidden_combined_cable": "Hidden Combined Cable (50V + Data)",
    f"block.{M}.computer": "Computer (50V)",
    f"item.{M}.engineers_goggles": "Engineer's Goggles",
    f"message.{M}.disguise.invalid": "Can't disguise as that block",
    f"gui.{M}.terminal.input": "Type a command",
    f"gui.{M}.terminal.terminate": "Terminate",
    f"gui.{M}.terminal.reboot": "Reboot",
    f"gui.{M}.terminal.power": "Power",
    f"gui.{M}.editor.title": "Editing %s",
    f"gui.{M}.editor.save": "Save",
    f"gui.{M}.editor.close": "Back",
    f"gui.{M}.editor.saved": "Saved",
    f"gui.{M}.editor.read_only": "Built-in file (read-only). Copy it with cp to edit.",
    f"gui.{M}.editor.too_big": "Too big to save (30 KB max)",
})
w(lang_path, lang)
print("ok")
