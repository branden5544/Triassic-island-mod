"""Generates resources for the Create Kinetic Generators. The block only exists when Create is installed, so it can
use Create's textures. Run after gen_phase2.py: python3 tools/gen_phase11.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

ROT6 = {"north": {}, "south": {"y": 180}, "east": {"y": 90}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
for name, coil in (("kinetic_generator_50v", (200, 110, 60)), ("kinetic_generator_200v", (200, 40, 40))):
    side = casing(160, (120, 115, 105))
    for y in range(4, 12):
        for x in range(3, 13): side[y][x] = rgba(coil if y % 2 else tuple(c * 3 // 4 for c in coil))
    png(f"{A}/textures/block/{name}_side.png", side)
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/cube", "textures": {
        "particle": "create:block/andesite_casing", "north": "create:block/andesite_casing",
        "south": f"{M}:block/{name}_side", "east": f"{M}:block/{name}_side", "west": f"{M}:block/{name}_side",
        "up": "create:block/andesite_casing", "down": "create:block/andesite_casing"}})
    w(f"{A}/blockstates/{name}.json", {"variants": {f"facing={d}": dict({"model": f"{M}:block/{name}"}, **rot) for d, rot in ROT6.items()}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}"})
    # No loot table: the block drops itself in code (a loot table naming the item errors when Create is missing).

pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick))
tag["values"] = sorted(set(v if isinstance(v, str) else v["id"] for v in tag["values"]))
tag["values"] += [{"id": f"{M}:kinetic_generator_50v", "required": False}, {"id": f"{M}:kinetic_generator_200v", "required": False}]
w(pick, tag)

for name, coil in (("kinetic_generator_50v", "minecraft:copper_block"), ("kinetic_generator_200v", f"{M}:transformer")):
    w(f"{D}/{M}/recipes/{name}.json", {"conditions": [{"type": "forge:mod_loaded", "modid": "create"}],
        "type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["ASA", "CXC", "ARA"],
        "key": {"A": {"item": "create:andesite_casing"}, "S": {"item": "create:shaft"}, "C": {"item": "minecraft:copper_ingot"},
                "X": {"item": coil}, "R": {"item": "minecraft:redstone"}},
        "result": {"item": f"{M}:{name}"}})

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({
    f"block.{M}.kinetic_generator_50v": "Kinetic Generator (50V)",
    f"block.{M}.kinetic_generator_200v": "Kinetic Generator (200V)",
    f"tooltip.{M}.kinetic_generator": "Generating %s W of %s W possible (%sV)",
})
w(lang_path, lang)
print("ok")
