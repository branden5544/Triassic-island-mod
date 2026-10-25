"""Generates grid v2 resources: 200V/800V cables, transformer, circuit breaker, wind turbine, large battery.
Run after gen_phase2.py: python3 tools/gen_phase3.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

# Reuse the drawing helpers from phase 2 without re-running it.
src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
helpers = src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")]
exec(helpers)
exec(src[src.index("def casing("):src.index("top = casing(10)")])

# ---------------------------------------------------------------- textures

def cable_tex(stripe, seed):
    r = random.Random(seed)
    c = [[rgba(jitter((35, 35, 38), r, 5)) for _ in range(16)] for _ in range(16)]
    for i in range(16):
        if i % 4 < 2:
            c[i][2] = c[i][13] = c[2][i] = c[13][i] = rgba(stripe)
    rect(c, 6, 6, 9, 9, (200, 110, 60))
    return c

png(f"{A}/textures/block/medium_voltage_cable.png", cable_tex((200, 40, 40), 40))
png(f"{A}/textures/block/high_voltage_cable.png", cable_tex((230, 200, 40), 41))

def coil_side(seed):
    px = casing(seed, (80, 85, 95))
    for y in range(3, 13):
        for x in range(3, 13):
            px[y][x] = rgba((200, 110, 60) if y % 2 else (150, 80, 45))
    return px

png(f"{A}/textures/block/transformer_side.png", coil_side(50))
def terminal(arrow_up, label_color):
    px = casing(51, (80, 85, 95))
    rect(px, 4, 4, 11, 11, (40, 40, 42)); rect(px, 6, 6, 9, 9, (200, 110, 60))
    rows = range(1, 4) if arrow_up else range(12, 15)
    for k, y in enumerate(rows):
        width = k if arrow_up else 2 - k
        rect(px, 7 - width, y, 8 + width, y, label_color)
    return px
png(f"{A}/textures/block/transformer_back.png", terminal(False, (150, 150, 150)))
png(f"{A}/textures/block/transformer_front_up.png", terminal(True, (90, 220, 120)))
png(f"{A}/textures/block/transformer_front_down.png", terminal(False, (230, 140, 40)))

def breaker_front(open_):
    px = casing(52, (70, 70, 75))
    rect(px, 5, 2, 10, 13, (30, 30, 32))
    if open_:
        rect(px, 6, 9, 9, 12, (200, 40, 40))
    else:
        rect(px, 6, 3, 9, 6, (60, 200, 90))
    return px
png(f"{A}/textures/block/circuit_breaker_closed.png", breaker_front(False))
png(f"{A}/textures/block/circuit_breaker_open.png", breaker_front(True))
side = casing(53, (70, 70, 75)); rect(side, 6, 6, 9, 9, (200, 110, 60))
png(f"{A}/textures/block/circuit_breaker_end.png", side)
png(f"{A}/textures/block/circuit_breaker_side.png", casing(54, (70, 70, 75)))

png(f"{A}/textures/block/wind_turbine.png", casing(55, (225, 228, 232)))
blade = [[rgba((240, 242, 245)) for _ in range(16)] for _ in range(16)]
for i in range(16): blade[i][0] = blade[i][15] = rgba((200, 60, 50)) if i < 3 else rgba((210, 212, 215))
png(f"{A}/textures/block/wind_turbine_blade.png", blade)

lb = casing(56, (50, 70, 110)); rect(lb, 3, 3, 12, 12, (40, 40, 42))
for y in range(4, 12, 2): rect(lb, 4, y, 11, y, (90, 200, 230))
png(f"{A}/textures/block/large_battery_side.png", lb)
png(f"{A}/textures/block/large_battery_top.png", casing(57, (50, 70, 110)))

# ---------------------------------------------------------------- models and blockstates

ROT6 = {"north": {}, "south": {"y": 180}, "east": {"y": 90}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}

def facing_cube(name, front, back, side):
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/cube", "textures": {
        "particle": side, "north": front, "south": back, "east": side, "west": side, "up": side, "down": side}})

def cable_models(name):
    tex = {"cable": f"{M}:block/{name}", "particle": f"{M}:block/{name}"}
    box = lambda frm, to: {"from": frm, "to": to, "faces": {f: {"texture": "#cable"} for f in ("north", "south", "east", "west", "up", "down")}}
    w(f"{A}/models/block/{name}_core.json", {"textures": tex, "elements": [box([5, 5, 5], [11, 11, 11])]})
    w(f"{A}/models/block/{name}_side.json", {"textures": tex, "elements": [box([5, 5, 0], [11, 11, 5])]})
    w(f"{A}/blockstates/{name}.json", {"multipart": [{"apply": {"model": f"{M}:block/{name}_core"}}] + [
        {"when": {d: "true"}, "apply": dict({"model": f"{M}:block/{name}_side"}, **ROT6[d])} for d in ROT6]})
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:block/block", "textures": tex, "elements": [box([5, 5, 0], [11, 11, 16])]})

cable_models("medium_voltage_cable")
cable_models("high_voltage_cable")

facing_cube("transformer_step_up", f"{M}:block/transformer_front_up", f"{M}:block/transformer_back", f"{M}:block/transformer_side")
facing_cube("transformer_step_down", f"{M}:block/transformer_front_down", f"{M}:block/transformer_back", f"{M}:block/transformer_side")
w(f"{A}/blockstates/transformer.json", {"variants": {
    f"facing={d},step_up={up}": dict({"model": f"{M}:block/transformer_step_{'up' if up == 'true' else 'down'}"}, **ROT6[d])
    for d in ROT6 for up in ("true", "false")}})
w(f"{A}/models/item/transformer.json", {"parent": f"{M}:block/transformer_step_up"})

# Breaker: front/back are the cable terminals; the switch is on the top face.
for state in ("closed", "open"):
    w(f"{A}/models/block/circuit_breaker_{state}.json", {"parent": "minecraft:block/cube", "textures": {
        "particle": f"{M}:block/circuit_breaker_side", "north": f"{M}:block/circuit_breaker_end",
        "south": f"{M}:block/circuit_breaker_end", "up": f"{M}:block/circuit_breaker_{state}",
        "down": f"{M}:block/circuit_breaker_side", "east": f"{M}:block/circuit_breaker_{state}",
        "west": f"{M}:block/circuit_breaker_{state}"}})
w(f"{A}/blockstates/circuit_breaker.json", {"variants": {
    f"facing={d},open={o},powered={p}": dict({"model": f"{M}:block/circuit_breaker_{'open' if o == 'true' else 'closed'}"}, **ROT6[d])
    for d in ROT6 for o in ("true", "false") for p in ("true", "false")}})
w(f"{A}/models/item/circuit_breaker.json", {"parent": f"{M}:block/circuit_breaker_closed"})

def el(frm, to, tex):
    return {"from": frm, "to": to, "faces": {f: {"texture": tex} for f in ("north", "south", "east", "west", "up", "down")}}
w(f"{A}/models/block/wind_turbine.json", {"parent": "minecraft:block/block", "textures": {
    "body": f"{M}:block/wind_turbine", "blade": f"{M}:block/wind_turbine_blade", "particle": f"{M}:block/wind_turbine"},
    "elements": [
        el([6, 0, 6], [10, 9, 10], "#body"),          # mast
        el([5, 9, 3], [11, 15, 14], "#body"),         # nacelle
        el([7, 11, 1], [9, 13, 3], "#body"),          # hub
        el([7.5, 13, 1.5], [8.5, 28, 2.5], "#blade"),  # blade up
        el([7.5, -3, 1.5], [8.5, 11, 2.5], "#blade"),  # blade down
        el([-6, 11.5, 1.5], [7, 12.5, 2.5], "#blade"),  # blade left
        el([9, 11.5, 1.5], [22, 12.5, 2.5], "#blade"),  # blade right
    ]})
w(f"{A}/blockstates/wind_turbine.json", {"variants": {"": {"model": f"{M}:block/wind_turbine"}}})
w(f"{A}/models/item/wind_turbine.json", {"parent": f"{M}:block/wind_turbine",
    "display": {"gui": {"rotation": [30, 225, 0], "scale": [0.45, 0.45, 0.45]}}})

w(f"{A}/models/block/large_battery.json", {"parent": "minecraft:block/cube_column", "textures": {
    "end": f"{M}:block/large_battery_top", "side": f"{M}:block/large_battery_side"}})
w(f"{A}/blockstates/large_battery.json", {"variants": {"": {"model": f"{M}:block/large_battery"}}})
w(f"{A}/models/item/large_battery.json", {"parent": f"{M}:block/large_battery"})

# ---------------------------------------------------------------- loot, tags, recipes

new_blocks = ["medium_voltage_cable", "high_voltage_cable", "transformer", "circuit_breaker", "wind_turbine", "large_battery"]
for b in new_blocks:
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick))
tag["values"] = sorted(set(tag["values"]) | {f"{M}:{b}" for b in new_blocks if "cable" not in b})
w(pick, tag)

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})

I, C, R = "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:redstone"
shaped("medium_voltage_cable", ["LLL", "KKK", "LLL"], {"L": f"{M}:low_voltage_cable", "K": "minecraft:dried_kelp"}, 6)
shaped("high_voltage_cable", ["MMM", "III", "MMM"], {"M": f"{M}:medium_voltage_cable", "I": I}, 6)
shaped("transformer", ["ICI", "CIC", "ICI"], {"I": I, "C": "minecraft:copper_block"})
shaped("circuit_breaker", ["ILI", "CRC", "III"], {"I": I, "L": "minecraft:lever", "C": C, "R": R})
shaped("wind_turbine", ["WIW", "ICI", "WRW"], {"W": "#minecraft:wool", "I": I, "C": "minecraft:copper_block", "R": R})
shaped("large_battery", ["BCB", "CRC", "BCB"], {"B": f"{M}:battery", "C": C, "R": "minecraft:redstone_block"})

# ---------------------------------------------------------------- lang

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({
    f"block.{M}.low_voltage_cable": "Low Voltage Cable (50V)",
    f"block.{M}.medium_voltage_cable": "Medium Voltage Cable (200V)",
    f"block.{M}.high_voltage_cable": "High Voltage Cable (800V)",
    f"block.{M}.transformer": "Transformer (4:1)",
    f"block.{M}.circuit_breaker": "Circuit Breaker",
    f"block.{M}.wind_turbine": "Wind Turbine (200V)",
    f"block.{M}.large_battery": "Large Battery (200V)",
    f"message.{M}.transformer.step_up": "Steps UP x4 (back -> front)",
    f"message.{M}.transformer.step_down": "Steps DOWN /4 (back -> front)",
    f"message.{M}.breaker.rating": "Trips above %s A",
    f"message.{M}.meter.input": "In: ",
    f"message.{M}.meter.output": "   Out: ",
})
w(lang_path, lang)
print("ok")
