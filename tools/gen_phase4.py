"""Generates park resources: electric fences, fence gate, feeder, warning sign, shock damage type.
Run after gen_phase2.py: python3 tools/gen_phase4.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

CLEAR = (0, 0, 0, 0)

# ---------------------------------------------------------------- textures

def fence_tex(wire, post, insulator):
    """Horizontal wires strung between posts on the left and right edges, with insulators where they meet."""
    px = canvas()
    for y in range(1, 16, 3):
        for x in range(16):
            px[y][x] = rgba(wire)
    for y in range(16):
        for x in (0, 15):
            px[y][x] = rgba(post)
    for y in range(1, 16, 3):
        px[y][1] = px[y][14] = rgba(insulator)
    return px

png(f"{A}/textures/block/electric_fence.png", fence_tex((150, 155, 160), (90, 95, 100), (240, 240, 240)))
png(f"{A}/textures/block/high_voltage_fence.png", fence_tex((170, 175, 180), (60, 62, 66), (230, 200, 40)))
edge = canvas()
for y in range(16):
    for x in range(16):
        edge[y][x] = rgba((90, 95, 100)) if 7 <= x <= 8 else CLEAR
png(f"{A}/textures/block/electric_fence_edge.png", edge)

gate = fence_tex((170, 175, 180), (60, 62, 66), (230, 200, 40))
for x in range(2, 14):
    for y in (0, 15):
        gate[y][x] = rgba((230, 200, 40)) if (x // 2) % 2 else rgba((30, 30, 30))
png(f"{A}/textures/block/electric_fence_gate.png", gate)
png(f"{A}/textures/block/electric_fence_gate_post.png", casing(60, (60, 62, 66)))

png(f"{A}/textures/block/feeder_side.png", casing(61, (120, 125, 130)))
r = random.Random(62)
hay = [[rgba(jitter((200, 170, 60), r, 25)) for _ in range(16)] for _ in range(16)]
png(f"{A}/textures/block/feeder_food.png", hay)

sign = canvas()
rect(sign, 0, 0, 15, 15, (30, 30, 30)); rect(sign, 1, 1, 14, 14, (240, 200, 30))
for x in range(3, 13): sign[2][x] = sign[3][x] = rgba((200, 30, 30))   # red "DANGER" band
bolt = [(9, 5), (8, 6), (8, 7), (7, 7), (7, 8), (6, 8), (8, 8), (9, 8), (8, 9), (7, 10), (7, 11), (6, 12), (9, 6), (10, 5)]
for x, y in bolt: sign[y][x] = rgba((20, 20, 20))
png(f"{A}/textures/block/high_voltage_sign.png", sign)

# ---------------------------------------------------------------- fences (reuse vanilla iron bars geometry)

PARTS = ["post_ends", "post", "cap", "cap_alt", "side", "side_alt"]
for name in ("electric_fence", "high_voltage_fence"):
    tex = {"particle": f"{M}:block/{name}", "bars": f"{M}:block/{name}", "edge": f"{M}:block/electric_fence_edge"}
    for part in PARTS:
        w(f"{A}/models/block/{name}_{part}.json", {"parent": f"minecraft:block/iron_bars_{part}", "textures": tex})
    m = lambda part, **rot: dict({"model": f"{M}:block/{name}_{part}"}, **rot)
    none = {"north": "false", "east": "false", "south": "false", "west": "false"}
    only = lambda d: dict(none, **{d: "true"})
    w(f"{A}/blockstates/{name}.json", {"multipart": [
        {"apply": m("post_ends")},
        {"when": none, "apply": m("post")},
        {"when": only("north"), "apply": m("cap")},
        {"when": only("east"), "apply": m("cap", y=90)},
        {"when": only("south"), "apply": m("cap_alt")},
        {"when": only("west"), "apply": m("cap_alt", y=90)},
        {"when": {"north": "true"}, "apply": m("side")},
        {"when": {"east": "true"}, "apply": m("side", y=90)},
        {"when": {"south": "true"}, "apply": m("side_alt")},
        {"when": {"west": "true"}, "apply": m("side_alt", y=90)},
    ]})
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:block/{name}"}})

# ---------------------------------------------------------------- gate

def faces(tex):
    return {f: {"texture": tex} for f in ("north", "south", "east", "west", "up", "down")}
posts = [{"from": [0, 0, 7], "to": [2, 16, 9], "faces": faces("#post")},
         {"from": [14, 0, 7], "to": [16, 16, 9], "faces": faces("#post")}]
gate_tex = {"bars": f"{M}:block/electric_fence_gate", "post": f"{M}:block/electric_fence_gate_post",
            "particle": f"{M}:block/electric_fence_gate_post"}
w(f"{A}/models/block/electric_fence_gate_closed.json", {"textures": gate_tex, "elements": posts + [
    {"from": [2, 0, 8], "to": [14, 16, 8], "faces": {"north": {"texture": "#bars", "uv": [2, 0, 14, 16]},
                                                    "south": {"texture": "#bars", "uv": [2, 0, 14, 16]}}}]})
w(f"{A}/models/block/electric_fence_gate_open.json", {"textures": gate_tex, "elements": posts})
w(f"{A}/blockstates/electric_fence_gate.json", {"variants": {
    f"facing={d},open={o}": dict({"model": f"{M}:block/electric_fence_gate_{'open' if o == 'true' else 'closed'}"}, **({"y": rot} if rot else {}))
    for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)) for o in ("true", "false")}})
w(f"{A}/models/item/electric_fence_gate.json", {"parent": f"{M}:block/electric_fence_gate_closed"})

# ---------------------------------------------------------------- feeder

side_faces = lambda: {f: {"texture": "#side"} for f in ("north", "south", "east", "west", "up", "down")}
w(f"{A}/models/block/feeder.json", {"textures": {"side": f"{M}:block/feeder_side", "food": f"{M}:block/feeder_food",
                                                  "particle": f"{M}:block/feeder_side"}, "elements": [
    {"from": [0, 0, 0], "to": [16, 2, 16], "faces": side_faces()},
    {"from": [0, 2, 0], "to": [16, 8, 1], "faces": side_faces()},
    {"from": [0, 2, 15], "to": [16, 8, 16], "faces": side_faces()},
    {"from": [0, 2, 1], "to": [1, 8, 15], "faces": side_faces()},
    {"from": [15, 2, 1], "to": [16, 8, 15], "faces": side_faces()},
    {"from": [1, 2, 1], "to": [15, 6, 15], "faces": {"up": {"texture": "#food"}}},
]})
w(f"{A}/blockstates/feeder.json", {"variants": {"": {"model": f"{M}:block/feeder"}}})
w(f"{A}/models/item/feeder.json", {"parent": f"{M}:block/feeder"})

# ---------------------------------------------------------------- sign

w(f"{A}/models/block/high_voltage_sign.json", {"textures": {"sign": f"{M}:block/high_voltage_sign", "particle": f"{M}:block/high_voltage_sign"},
    "elements": [{"from": [0, 0, 15], "to": [16, 16, 16], "faces": {
        "north": {"texture": "#sign"}, "south": {"texture": "#sign"},
        **{f: {"texture": "#sign", "uv": [0, 0, 1, 16]} for f in ("east", "west", "up", "down")}}}]})
w(f"{A}/blockstates/high_voltage_sign.json", {"variants": {
    f"facing={d}": dict({"model": f"{M}:block/high_voltage_sign"}, **({"y": rot} if rot else {}))
    for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
w(f"{A}/models/item/high_voltage_sign.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:block/high_voltage_sign"}})

# ---------------------------------------------------------------- data

blocks = ["electric_fence", "high_voltage_fence", "electric_fence_gate", "feeder", "high_voltage_sign"]
for b in blocks:
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick))
tag["values"] = sorted(set(tag["values"]) | {f"{M}:{b}" for b in blocks})
w(pick, tag)

w(f"{D}/{M}/damage_type/electric_shock.json", {"exhaustion": 0.1, "message_id": f"{M}.electric_shock", "scaling": "never"})
w(f"{D}/{M}/tags/items/dinosaur_plant_food.json", {"values": [
    "minecraft:wheat", "minecraft:hay_block", "minecraft:sugar_cane", "#minecraft:leaves", "#minecraft:saplings",
    "minecraft:kelp", "minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds",
    "minecraft:fern", "minecraft:large_fern", "minecraft:grass", "minecraft:vine", "minecraft:lily_pad",
    "minecraft:cactus", "minecraft:bamboo"]})

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})

shaped("electric_fence", ["BCB", "BCB"], {"B": "minecraft:iron_bars", "C": f"{M}:medium_voltage_cable"}, 6)
shaped("high_voltage_fence", ["FFF", "FHF", "FFF"], {"F": f"{M}:electric_fence", "H": f"{M}:high_voltage_cable"}, 8)
shaped("electric_fence_gate", ["IFI", "IRI"], {"I": "minecraft:iron_ingot", "F": f"{M}:high_voltage_fence", "R": "minecraft:redstone"})
shaped("feeder", ["I I", "IHI", "III"], {"I": "minecraft:iron_ingot", "H": "minecraft:hay_block"})
shaped("high_voltage_sign", ["YBY", " I "], {"Y": "minecraft:yellow_dye", "B": "minecraft:black_dye", "I": "minecraft:iron_ingot"}, 3)

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({
    f"block.{M}.electric_fence": "Electric Fence (200V)",
    f"block.{M}.high_voltage_fence": "High Voltage Fence (800V)",
    f"block.{M}.electric_fence_gate": "Electric Fence Gate",
    f"block.{M}.feeder": "Feeder",
    f"block.{M}.high_voltage_sign": "Danger: High Voltage Sign",
    f"container.{M}.feeder": "Feeder",
    f"message.{M}.gate.energized": "The gate is electrified! Cut the power first.",
    f"death.attack.{M}.electric_shock": "%1$s was electrocuted",
    f"death.attack.{M}.electric_shock.player": "%1$s was electrocuted while fighting %2$s",
})
w(lang_path, lang)
print("ok")
