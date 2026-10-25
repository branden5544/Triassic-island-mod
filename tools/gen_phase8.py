"""Generates resources for lights (sockets, bulbs, streetlight, string lights, lamp supply, emergency lamps,
flashlights) and power poles (utility pole, insulator, wire coils). Run after gen_phase2.py:
python3 tools/gen_phase8.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

ROT6 = {"up": {}, "down": {"x": 180}, "north": {"x": 90}, "south": {"x": 90, "y": 180}, "east": {"x": 90, "y": 90}, "west": {"x": 90, "y": 270}}
LIT = "|".join(str(n) for n in range(1, 16))

def faces(tex):
    return {f: {"texture": tex} for f in ("north", "south", "east", "west", "up", "down")}

def el(frm, to, tex):
    return {"from": frm, "to": to, "faces": faces(tex)}

# ---------------------------------------------------------------- textures

r = random.Random(130)
png(f"{A}/textures/block/socket_metal.png", [[rgba(jitter((70, 72, 76), r, 8)) for _ in range(16)] for _ in range(16)])
png(f"{A}/textures/block/bulb_off.png", [[rgba(jitter((185, 185, 170), r, 6)) for _ in range(16)] for _ in range(16)])
png(f"{A}/textures/block/bulb_on.png", [[rgba(jitter((255, 244, 200), r, 5)) for _ in range(16)] for _ in range(16)])
png(f"{A}/textures/block/lamp_shade.png", [[rgba(jitter((40, 70, 55), r, 6)) for _ in range(16)] for _ in range(16)])
chain = canvas()
for y in range(16):
    for x in range(6, 10):
        if (y % 4 < 2) == (x in (6, 9)): chain[y][x] = rgba((90, 90, 95))
png(f"{A}/textures/block/lamp_chain.png", [[p if p[3] else rgba((60, 60, 64)) for p in row] for row in chain])

supply = casing(131, (90, 90, 60)); rect(supply, 4, 4, 11, 11, (40, 40, 42))
for i in range(5, 11, 2): rect(supply, i, 6, i, 9, (255, 230, 120))
png(f"{A}/textures/block/lamp_supply.png", supply)

for lit in (0, 1):
    em = casing(132, (210, 210, 205)); rect(em, 2, 4, 6, 8, (255, 245, 200) if lit else (150, 150, 140))
    rect(em, 9, 4, 13, 8, (255, 245, 200) if lit else (150, 150, 140)); rect(em, 5, 10, 10, 12, (40, 180, 80))
    png(f"{A}/textures/block/emergency_lamp{'_on' if lit else ''}.png", em)

r = random.Random(133)
strings = [[rgba(jitter((30, 30, 32), r, 4)) for _ in range(16)] for _ in range(16)]
for lit in (0, 1):
    px = [row[:] for row in strings]
    for i, color in enumerate(((240, 80, 80), (80, 200, 90), (80, 140, 240), (240, 200, 60))):
        c = color if lit else tuple(v // 3 for v in color)
        rect(px, 2 + i * 3, 6, 3 + i * 3, 9, c)
    png(f"{A}/textures/block/string_lights{'_on' if lit else ''}.png", px)

BULB_COLORS = {"carbon_incandescent_50v": (255, 200, 120), "incandescent_200v": (255, 215, 140), "economic_50v": (240, 245, 255),
               "economic_200v": (225, 235, 255), "small_economic_50v": (240, 245, 255), "led_50v": (200, 230, 255),
               "led_200v": (180, 215, 255), "farming_50v": (240, 120, 220), "farming_200v": (220, 90, 200)}
for name, glass in BULB_COLORS.items():
    b = canvas()
    small = name.startswith("small")
    for y in range(16):
        for x in range(16):
            if ((x - 7.5) / (3.5 if small else 5)) ** 2 + ((y - 6) / (3.5 if small else 5)) ** 2 < 1: b[y][x] = rgba(glass)
    rect(b, 6, 11, 9, 14, (170, 170, 175))
    for y in (12, 14): rect(b, 6, y, 9, y, (120, 120, 125))
    if "led" in name: rect(b, 6, 5, 9, 6, (255, 255, 255))
    png(f"{A}/textures/item/{name}_bulb.png", b)

for name, body in (("small_flashlight", (60, 60, 64)), ("improved_flashlight", (200, 160, 40))):
    f = canvas()
    for i in range(2, 12):
        for t in (-1, 0, 1): f[15 - i][i + t] = rgba(body)
    for i in range(11, 15):
        for t in (-2, -1, 0, 1, 2):
            if 0 <= i + t < 16: f[15 - i][i + t] = rgba((210, 210, 215))
    f[2][13] = f[1][14] = rgba((255, 250, 210))
    png(f"{A}/textures/item/{name}.png", f)

for tier, color in (("low", (200, 110, 60)), ("medium", (200, 40, 40)), ("high", (230, 200, 40))):
    c = canvas()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if 3 < d < 7: c[y][x] = rgba(color if int(d * 2) % 2 else tuple(v * 3 // 4 for v in color))
    png(f"{A}/textures/item/{tier}_voltage_wire_coil.png", c)

# ---------------------------------------------------------------- lamp sockets

M_ = "#metal"; B_ = "#bulb"; S_ = "#shade"; C_ = "#chain"
SOCKETS = {
    "lamp_socket_a": ([el([5, 0, 5], [11, 1, 11], M_), el([6, 1, 6], [10, 3, 10], M_)], [el([6, 3, 6], [10, 8, 10], B_)]),
    "lamp_socket_b_projector": ([el([4, 0, 4], [12, 8, 12], M_)], [el([5, 8, 5], [11, 9, 11], B_)]),
    "robust_lamp_socket": ([el([3, 0, 3], [13, 1, 13], M_)] + [el([x, 1, z], [x + 1, 10, z + 1], M_) for x in (3, 12) for z in (3, 12)]
                           + [el([3, 9, 3], [13, 10, 4], M_), el([3, 9, 12], [13, 10, 13], M_)], [el([5, 1, 5], [11, 8, 11], B_)]),
    "flat_lamp_socket": ([el([2, 0, 2], [14, 2, 14], M_)], [el([3, 2, 3], [13, 3, 13], B_)]),
    "simple_lamp_socket": ([el([6, 0, 6], [10, 3, 10], M_)], [el([6.5, 3, 6.5], [9.5, 8, 9.5], B_)]),
    "sconce_lamp_socket": ([el([5, 0, 4], [11, 1, 12], M_), el([7, 1, 7], [9, 5, 9], M_), el([5, 5, 5], [11, 7, 11], S_)],
                           [el([6, 7, 6], [10, 11, 10], B_)]),
    "fluorescent_lamp_socket": ([el([1, 0, 5], [15, 2, 11], M_)], [el([2, 2, 6], [14, 4, 10], B_)]),
    "suspended_lamp_socket": ([el([6, 0, 6], [10, 1, 10], M_), el([7.5, 1, 7.5], [8.5, 8, 8.5], C_), el([4, 8, 4], [12, 11, 12], S_)],
                              [el([6, 11, 6], [10, 14, 10], B_)]),
    "long_suspended_lamp_socket": ([el([6, 0, 6], [10, 1, 10], M_), el([7.5, 1, 7.5], [8.5, 20, 8.5], C_), el([5, 20, 5], [11, 23, 11], S_)],
                                   [el([6.5, 23, 6.5], [9.5, 27, 9.5], B_)]),
}
SOCKETS["suspended_lamp_socket_no_swing"] = SOCKETS["suspended_lamp_socket"]
SOCKETS["long_suspended_lamp_socket_no_swing"] = SOCKETS["long_suspended_lamp_socket"]
SWINGS = {"suspended_lamp_socket", "long_suspended_lamp_socket"}
TEX = {"metal": f"{M}:block/socket_metal", "shade": f"{M}:block/lamp_shade", "chain": f"{M}:block/lamp_chain", "particle": f"{M}:block/socket_metal"}

def flip_down(elements):
    """Up-frame elements to world space for a ceiling mount (blockstate x=180): y -> 16 - y, z -> 16 - z."""
    out = []
    for e in elements:
        (x1, y1, z1), (x2, y2, z2) = e["from"], e["to"]
        out.append(dict(e, **{"from": [x1, 16 - y2, 16 - z2], "to": [x2, 16 - y1, 16 - z1]}))
    return out

for name, (body, bulb) in SOCKETS.items():
    looks = {"empty": body, "off": body + bulb, "on": body + bulb}
    if name in SWINGS:
        # The block only shows the ceiling mount; the renderer swings the rest.
        mount, hanging = body[:1], body[1:]
        w(f"{A}/models/block/{name}_mount.json", {"parent": "minecraft:block/block", "textures": TEX, "elements": mount})
        for look, bulb_tex in (("empty", None), ("off", "bulb_off"), ("on", "bulb_on")):
            elements = flip_down(hanging + (bulb if bulb_tex else []))
            w(f"{A}/models/block/{name}_hanging_{look}.json", {"parent": "minecraft:block/block",
                "textures": dict(TEX, bulb=f"{M}:block/{bulb_tex or 'bulb_off'}"), "elements": elements})
        w(f"{A}/blockstates/{name}.json", {"multipart": [{"apply": dict({"model": f"{M}:block/{name}_mount"}, **ROT6["down"])}]})
        w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}_hanging_off"})
        continue
    for look, elements in looks.items():
        w(f"{A}/models/block/{name}_{look}.json", {"parent": "minecraft:block/block",
            "textures": dict(TEX, bulb=f"{M}:block/bulb_{'on' if look == 'on' else 'off'}"), "elements": elements})
    parts = []
    for d, rot in ROT6.items():
        parts.append({"when": {"facing": d, "has_bulb": "false"}, "apply": dict({"model": f"{M}:block/{name}_empty"}, **rot)})
        parts.append({"when": {"facing": d, "has_bulb": "true", "light": "0"}, "apply": dict({"model": f"{M}:block/{name}_off"}, **rot)})
        parts.append({"when": {"facing": d, "has_bulb": "true", "light": LIT}, "apply": dict({"model": f"{M}:block/{name}_on"}, **rot)})
    w(f"{A}/blockstates/{name}.json", {"multipart": parts})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}_off"})

# ---------------------------------------------------------------- streetlight

w(f"{A}/models/block/street_light.json", {"parent": "minecraft:block/block", "textures": TEX, "elements": [
    el([4, 0, 4], [12, 2, 12], M_), el([6.5, 2, 6.5], [9.5, 32, 9.5], M_)]})
w(f"{A}/blockstates/street_light.json", {"variants": {f"facing={d}": dict({"model": f"{M}:block/street_light"}, **({"y": rot} if rot else {}))
                                                      for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
w(f"{A}/models/item/street_light.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:block/bulb_on"}})
for look in ("off", "on"):
    w(f"{A}/models/block/street_light_head_{look}.json", {"parent": "minecraft:block/block",
        "textures": dict(TEX, bulb=f"{M}:block/bulb_{look}"), "elements": [
        el([6.5, 0, 6.5], [9.5, 8, 9.5], M_), el([7, 6, 1], [9, 8, 9], M_),
        el([4, 4, -3], [12, 7, 5], S_), el([5, 3, -2], [11, 4, 4], B_)]})
w(f"{A}/blockstates/street_light_head.json", {"multipart": [
    {"when": {"facing": d, "light": light}, "apply": dict({"model": f"{M}:block/street_light_head_{look}"}, **({"y": rot} if rot else {}))}
    for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)) for light, look in (("0", "off"), (LIT, "on"))]})
for invisible in ("street_light_pole", "temporary_light"):
    w(f"{A}/blockstates/{invisible}.json", {"variants": {"": {"model": "minecraft:block/air"}}})

# ---------------------------------------------------------------- string lights (connect like a cable)

for look in ("", "_on"):
    tex = {"wire": f"{M}:block/string_lights{look}", "particle": f"{M}:block/string_lights"}
    w(f"{A}/models/block/string_lights_core{look}.json", {"textures": tex, "elements": [
        el([7, 7, 7], [9, 9, 9], "#wire"), el([6.5, 4, 6.5], [9.5, 7, 9.5], "#wire")]})
    w(f"{A}/models/block/string_lights_side{look}.json", {"textures": tex, "elements": [el([7.5, 7.5, 0], [8.5, 8.5, 7], "#wire")]})
DIRS = {"north": {}, "south": {"y": 180}, "east": {"y": 90}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
parts = [{"when": {"light": "0"}, "apply": {"model": f"{M}:block/string_lights_core"}},
         {"when": {"light": LIT}, "apply": {"model": f"{M}:block/string_lights_core_on"}}]
parts += [{"when": {d: "true"}, "apply": dict({"model": f"{M}:block/string_lights_side"}, **rot)} for d, rot in DIRS.items()]
w(f"{A}/blockstates/string_lights.json", {"multipart": parts})
w(f"{A}/models/item/string_lights.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:block/string_lights_on"}})

# ---------------------------------------------------------------- lamp supply, emergency lamps

w(f"{A}/models/block/lamp_supply.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{M}:block/lamp_supply"}})
w(f"{A}/blockstates/lamp_supply.json", {"variants": {"": {"model": f"{M}:block/lamp_supply"}}})
w(f"{A}/models/item/lamp_supply.json", {"parent": f"{M}:block/lamp_supply"})
for look in ("", "_on"):
    w(f"{A}/models/block/emergency_lamp{look}.json", {"parent": "minecraft:block/block", "textures": {
        "front": f"{M}:block/emergency_lamp{look}", "side": f"{M}:block/socket_metal", "particle": f"{M}:block/emergency_lamp"},
        "elements": [dict(el([3, 5, 13], [13, 11, 16], "#side"), faces=dict(faces("#side"), north={"texture": "#front"}))]})
for v in ("50v", "200v"):
    w(f"{A}/blockstates/emergency_lamp_{v}.json", {"multipart": [
        {"when": {"facing": d, "light": light}, "apply": dict({"model": f"{M}:block/emergency_lamp{look}"}, **({"y": rot} if rot else {}))}
        for d, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)) for light, look in (("0", ""), (LIT, "_on"))]})
    w(f"{A}/models/item/emergency_lamp_{v}.json", {"parent": f"{M}:block/emergency_lamp_on"})

# ---------------------------------------------------------------- poles

POLE = "minecraft:block/stripped_spruce_log"
w(f"{A}/models/block/utility_pole.json", {"parent": "minecraft:block/block", "textures": {"wood": POLE, "particle": POLE},
    "elements": [el([5, 0, 5], [11, 16, 11], "#wood")]})
w(f"{A}/blockstates/utility_pole.json", {"variants": {"": {"model": f"{M}:block/utility_pole"}}})
w(f"{A}/models/item/utility_pole.json", {"parent": f"{M}:block/utility_pole"})
w(f"{A}/models/block/pole_insulator.json", {"parent": "minecraft:block/block", "textures": {
    "wood": POLE, "glaze": "minecraft:block/white_terracotta", "particle": POLE}, "elements": [
    el([5, 0, 5], [11, 16, 11], "#wood"), el([0, 10, 6], [16, 13, 10], "#wood"),
    el([1.5, 13, 7], [3.5, 15, 9], "#glaze"), el([12.5, 13, 7], [14.5, 15, 9], "#glaze"), el([7, 16, 7], [9, 17, 9], "#glaze")]})
w(f"{A}/blockstates/pole_insulator.json", {"variants": {"": {"model": f"{M}:block/pole_insulator"}}})
w(f"{A}/models/item/pole_insulator.json", {"parent": f"{M}:block/pole_insulator"})

# ---------------------------------------------------------------- item models

for name in list(BULB_COLORS):
    w(f"{A}/models/item/{name}_bulb.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/{name}_bulb"}})
for name in ("small_flashlight", "improved_flashlight", "low_voltage_wire_coil", "medium_voltage_wire_coil", "high_voltage_wire_coil"):
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/{name}"}})

# ---------------------------------------------------------------- data

metal_blocks = list(SOCKETS) + ["street_light", "lamp_supply", "emergency_lamp_50v", "emergency_lamp_200v"]
wood_blocks = ["utility_pole", "pole_insulator"]
for b in metal_blocks + wood_blocks + ["string_lights"]:
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
for tool, blocks in (("pickaxe", metal_blocks), ("axe", wood_blocks)):
    path = f"{D}/minecraft/tags/blocks/mineable/{tool}.json"
    tag = json.load(open(path)) if os.path.exists(path) else {"values": []}
    tag["values"] = sorted(set(tag["values"]) | {f"{M}:{b}" for b in blocks})
    w(path, tag)

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})

I, N, C, G, R = "minecraft:iron_ingot", "minecraft:iron_nugget", "minecraft:copper_ingot", "minecraft:glass_pane", "minecraft:redstone"
socket_recipes = {
    "lamp_socket_a": [" N ", "NCN"], "simple_lamp_socket": ["NCN"], "robust_lamp_socket": ["IBI", "NCN"],
    "flat_lamp_socket": ["NNN", "NCN"], "sconce_lamp_socket": ["N  ", "NC "], "lamp_socket_b_projector": ["INI", "ICI"],
    "fluorescent_lamp_socket": ["NNN", "CCC"], "suspended_lamp_socket": [" H ", "NCN"], "long_suspended_lamp_socket": [" H ", " H ", "NCN"],
}
for name, pattern in socket_recipes.items():
    shaped(name, pattern, {k: v for k, v in {"N": N, "C": C, "I": I, "B": "minecraft:iron_bars", "H": "minecraft:chain"}.items()
                           if any(k in row for row in pattern)}, 2)
for name in ("suspended_lamp_socket", "long_suspended_lamp_socket"):
    w(f"{D}/{M}/recipes/{name}_no_swing.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": f"{M}:{name}"}, {"item": N}], "result": {"item": f"{M}:{name}_no_swing"}})
shaped("street_light", [" CG", " I ", " I "], {"C": f"{M}:lamp_socket_a", "G": G, "I": I})
shaped("string_lights", ["GCG"], {"G": "minecraft:glowstone_dust", "C": f"{M}:low_voltage_cable"}, 4)
shaped("lamp_supply", ["ICI", "CTC", "ICI"], {"I": I, "C": C, "T": f"{M}:transformer"})
shaped("emergency_lamp_50v", ["IGI", "GBG"], {"I": I, "G": G, "B": f"{M}:battery"})
shaped("emergency_lamp_200v", ["IGI", "GBG"], {"I": I, "G": G, "B": f"{M}:large_battery"})
bulb_recipes = {"carbon_incandescent_50v": "minecraft:coal", "incandescent_200v": "minecraft:iron_nugget",
                "economic_50v": "minecraft:glowstone_dust", "economic_200v": "minecraft:glowstone", "small_economic_50v": "minecraft:glowstone_dust",
                "led_50v": "minecraft:redstone", "led_200v": "minecraft:redstone_torch", "farming_50v": "minecraft:bone_meal",
                "farming_200v": "minecraft:emerald"}
for name, core in bulb_recipes.items():
    w(f"{D}/{M}/recipes/{name}_bulb.json", {"type": "minecraft:crafting_shaped", "category": "misc",
        "pattern": ([" G ", "GXG"] if name.startswith("small") else [" G ", "GXG", " C "]),
        "key": {"G": {"item": G}, "X": {"item": core}, **({} if name.startswith("small") else {"C": {"item": C}})},
        "result": {"item": f"{M}:{name}_bulb", "count": 2}})
shaped("small_flashlight", ["G  ", " I ", "  B"], {"G": G, "I": I, "B": f"{M}:empty_syringe"})
w(f"{D}/{M}/recipes/small_flashlight.json", {"type": "minecraft:crafting_shaped", "category": "misc",
    "pattern": ["GL ", " I ", "  R"], "key": {"G": {"item": G}, "L": {"item": f"{M}:led_50v_bulb"}, "I": {"item": I}, "R": {"item": R}},
    "result": {"item": f"{M}:small_flashlight"}})
w(f"{D}/{M}/recipes/improved_flashlight.json", {"type": "minecraft:crafting_shaped", "category": "misc",
    "pattern": ["GL ", " F ", "  B"], "key": {"G": {"item": G}, "L": {"item": f"{M}:led_200v_bulb"}, "F": {"item": f"{M}:small_flashlight"},
                                              "B": {"item": "minecraft:redstone_block"}}, "result": {"item": f"{M}:improved_flashlight"}})
shaped("utility_pole", ["L", "L", "L"], {"L": "#minecraft:logs"}, 6)
shaped("pole_insulator", ["TLT", " L "], {"T": "minecraft:terracotta", "L": "#minecraft:logs"}, 2)
for tier, insulation in (("low", "#minecraft:wool"), ("medium", "minecraft:dried_kelp"), ("high", "minecraft:iron_ingot")):
    shaped(f"{tier}_voltage_wire_coil", [" C ", "CXC", " C "], {"C": f"{M}:{tier}_voltage_cable", "X": "minecraft:stick" if tier == "low" else insulation}, 4)

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
pretty = lambda s: " ".join(w.capitalize() for w in s.split("_")).replace("No Swing", "(No Swing)").replace("Socket B Projector", "Socket B Projector")
for name in SOCKETS:
    lang[f"block.{M}.{name}"] = pretty(name)
BULB_NAMES = {"carbon_incandescent_50v": "50V Carbon Incandescent Light Bulb", "incandescent_200v": "200V Incandescent Light Bulb",
              "economic_50v": "50V Economic Light Bulb", "economic_200v": "200V Economic Light Bulb",
              "small_economic_50v": "Small 50V Economic Light Bulb", "led_50v": "50V LED Bulb", "led_200v": "200V LED Bulb",
              "farming_50v": "50V Farming Lamp Bulb", "farming_200v": "200V Farming Lamp Bulb"}
for name, title in BULB_NAMES.items():
    lang[f"item.{M}.{name}_bulb"] = title
lang.update({
    f"block.{M}.street_light": "Streetlight (3 blocks tall)", f"block.{M}.street_light_pole": "Streetlight",
    f"block.{M}.street_light_head": "Streetlight", f"block.{M}.string_lights": "String Lights (50V)",
    f"block.{M}.lamp_supply": "Lamp Supply", f"block.{M}.emergency_lamp_50v": "50V Emergency Lamp",
    f"block.{M}.emergency_lamp_200v": "200V Emergency Lamp", f"block.{M}.temporary_light": "Light",
    f"item.{M}.small_flashlight": "Small Flashlight", f"item.{M}.improved_flashlight": "Improved Flashlight",
    f"block.{M}.utility_pole": "Utility Pole", f"block.{M}.pole_insulator": "Pole Insulator",
    f"item.{M}.low_voltage_wire_coil": "Low Voltage Wire Coil (50V)", f"item.{M}.medium_voltage_wire_coil": "Medium Voltage Wire Coil (200V)",
    f"item.{M}.high_voltage_wire_coil": "High Voltage Wire Coil (800V)",
    f"tooltip.{M}.bulb": "%sV, %sW - %s%% life left", f"tooltip.{M}.bulb.warmup": "Takes a few seconds to warm up",
    f"tooltip.{M}.bulb.farming": "Speeds up crops within 4 blocks", f"tooltip.{M}.flashlight": "Battery %s%% - reaches %s blocks",
    f"tooltip.{M}.wire_coil": "Overhead wire, %sV - right-click two Pole Insulators",
    f"message.{M}.socket.empty": "Right-click with a light bulb to fit one",
    f"message.{M}.flashlight.empty": "Battery flat - right-click a Battery block to charge",
    f"message.{M}.flashlight.charged": "Flashlight charged to %s%%",
    f"message.{M}.no_power": "No power - needs %s V (has %s V)",
    f"message.{M}.wire.start": "Now right-click another Pole Insulator (up to 32 blocks away)",
    f"message.{M}.wire.done": "Wire strung (%s blocks)", f"message.{M}.wire.cancelled": "Wire cancelled",
    f"message.{M}.wire.too_far": "Too far - wires can span up to 32 blocks", f"message.{M}.wire.already": "Those are already connected",
    f"message.{M}.wire.full": "That insulator already has 4 wires", f"message.{M}.wire.gone": "The first insulator is gone",
    f"message.{M}.wire.need": "Needs %s coils for that distance",
})
w(lang_path, lang)
print("ok")
