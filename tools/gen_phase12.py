"""Fence decorations: pillars, warning light, pillar cap, footing. Run after gen_phase2.py: python3 tools/gen_phase12.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

def faces(tex):
    return {f: {"texture": tex} for f in ("north", "south", "east", "west", "up", "down")}

def el(frm, to, tex):
    return {"from": frm, "to": to, "faces": faces(tex)}

r = random.Random(170)
steel = [[rgba(jitter((95, 100, 108), r, 7)) for _ in range(16)] for _ in range(16)]
for y in (0, 15): rect(steel, 0, y, 15, y, (70, 74, 80))
for x in (2, 13): rect(steel, x, 2, x, 13, (120, 125, 132))
png(f"{A}/textures/block/steel_fence_pillar.png", steel)
concrete = [[rgba(jitter((170, 168, 160), r, 6)) for _ in range(16)] for _ in range(16)]
for x in range(16):
    for y in (11, 12, 13):
        concrete[y][x] = rgba((230, 190, 30) if (x + y) // 2 % 2 else (30, 30, 30))   # hazard band
png(f"{A}/textures/block/concrete_fence_pillar.png", concrete)
png(f"{A}/textures/block/concrete_top.png", [[rgba(jitter((160, 158, 150), r, 6)) for _ in range(16)] for _ in range(16)])
for lit in (False, True):
    glass = [[rgba(jitter((255, 60, 40) if lit else (120, 25, 20), r, 10)) for _ in range(16)] for _ in range(16)]
    png(f"{A}/textures/block/fence_warning_light{'_on' if lit else ''}.png", glass)
footing = [[rgba(jitter((150, 148, 140), r, 6)) for _ in range(16)] for _ in range(16)]
rect(footing, 0, 0, 15, 1, (120, 118, 110)); rect(footing, 6, 0, 9, 1, (60, 60, 64))
png(f"{A}/textures/block/fence_footing.png", footing)

for name, tex, top in (("steel_fence_pillar", f"{M}:block/steel_fence_pillar", f"{M}:block/socket_metal"),
                       ("concrete_fence_pillar", f"{M}:block/concrete_fence_pillar", f"{M}:block/concrete_top")):
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/block", "textures": {"side": tex, "top": top, "particle": tex},
        "elements": [{"from": [3, 0, 3], "to": [13, 16, 13], "faces": {
            **{f: {"texture": "#side"} for f in ("north", "south", "east", "west")}, "up": {"texture": "#top"}, "down": {"texture": "#top"}}},
            el([7, 6, 1], [9, 7, 3], "#top"), el([7, 6, 13], [9, 7, 15], "#top"), el([1, 6, 7], [3, 7, 9], "#top"), el([13, 6, 7], [15, 7, 9], "#top")]})
    w(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{M}:block/{name}"}}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}"})

for lit in (False, True):
    suffix = "_on" if lit else ""
    w(f"{A}/models/block/fence_warning_light{suffix}.json", {"parent": "minecraft:block/block", "textures": {
        "glass": f"{M}:block/fence_warning_light{suffix}", "base": f"{M}:block/socket_metal", "particle": f"{M}:block/fence_warning_light"},
        "elements": [el([5, 0, 5], [11, 2, 11], "#base"), el([6, 2, 6], [10, 6, 10], "#glass")]})
w(f"{A}/blockstates/fence_warning_light.json", {"variants": {"lit=false": {"model": f"{M}:block/fence_warning_light"},
                                                             "lit=true": {"model": f"{M}:block/fence_warning_light_on"}}})
w(f"{A}/models/item/fence_warning_light.json", {"parent": f"{M}:block/fence_warning_light_on"})

w(f"{A}/models/block/fence_pillar_cap.json", {"parent": "minecraft:block/block", "textures": {"top": f"{M}:block/concrete_top", "particle": f"{M}:block/concrete_top"},
    "elements": [el([2, 0, 2], [14, 2, 14], "#top"), el([4, 2, 4], [12, 4, 12], "#top")]})
w(f"{A}/blockstates/fence_pillar_cap.json", {"variants": {"": {"model": f"{M}:block/fence_pillar_cap"}}})
w(f"{A}/models/item/fence_pillar_cap.json", {"parent": f"{M}:block/fence_pillar_cap"})
w(f"{A}/models/block/fence_footing.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "side": f"{M}:block/fence_footing", "top": f"{M}:block/concrete_top", "bottom": f"{M}:block/concrete_top"}})
w(f"{A}/blockstates/fence_footing.json", {"variants": {"": {"model": f"{M}:block/fence_footing"}}})
w(f"{A}/models/item/fence_footing.json", {"parent": f"{M}:block/fence_footing"})

blocks = ["steel_fence_pillar", "concrete_fence_pillar", "fence_warning_light", "fence_pillar_cap", "fence_footing"]
for b in blocks:
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick))
existing = {v if isinstance(v, str) else v["id"] for v in tag["values"]}
tag["values"] += [f"{M}:{b}" for b in blocks if f"{M}:{b}" not in existing]
w(pick, tag)

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})
shaped("steel_fence_pillar", ["I", "C", "I"], {"I": "minecraft:iron_block", "C": f"{M}:medium_voltage_cable"}, 4)
shaped("concrete_fence_pillar", ["S", "C", "S"], {"S": "minecraft:smooth_stone", "C": f"{M}:medium_voltage_cable"}, 4)
shaped("fence_warning_light", [" G ", "GRG", " I "], {"G": "minecraft:red_stained_glass", "R": "minecraft:redstone", "I": "minecraft:iron_ingot"}, 2)
shaped("fence_pillar_cap", ["SSS"], {"S": "minecraft:smooth_stone_slab"}, 4)
shaped("fence_footing", ["SSS", "SIS", "SSS"], {"S": "minecraft:smooth_stone", "I": "minecraft:iron_ingot"}, 8)

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({f"block.{M}.steel_fence_pillar": "Steel Fence Pillar", f"block.{M}.concrete_fence_pillar": "Concrete Fence Pillar",
             f"block.{M}.fence_warning_light": "Fence Warning Light", f"block.{M}.fence_pillar_cap": "Fence Pillar Cap",
             f"block.{M}.fence_footing": "Fence Footing"})
w(lang_path, lang)
print("ok")
