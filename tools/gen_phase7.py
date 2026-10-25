"""Generates resources for the extra Electrical Age blocks: electric furnace, macerator, fuel generator, lamps,
variable transformer, energy meter, fuse holder and fuses. Run after gen_phase2.py: python3 tools/gen_phase7.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

ROT6 = {"north": {}, "south": {"y": 180}, "east": {"y": 90}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
ROT4 = (("north", 0), ("east", 90), ("south", 180), ("west", 270))

# ---------------------------------------------------------------- machines (GUI blocks)

fronts = {}
f = casing(100); rect(f, 3, 3, 12, 12, (40, 40, 42)); rect(f, 4, 6, 11, 11, (20, 20, 22))
for x in range(4, 12, 2): rect(f, x, 9, x, 11, (240, 110, 30))          # glowing elements
fronts["electric_furnace"] = f
f = casing(101); rect(f, 4, 2, 11, 6, (60, 62, 66))
for x in range(5, 11): f[4][x] = rgba((30, 30, 32))                       # hopper mouth
for y in range(8, 14):
    for x in range(3, 13): f[y][x] = rgba((110, 110, 115) if (x + y) % 3 else (70, 70, 75))  # grinder teeth
fronts["macerator"] = f
f = casing(102, (70, 90, 70)); rect(f, 2, 7, 13, 13, (40, 40, 42))
for x in range(3, 13): f[10 + (x % 2)][x] = rgba((240, 120, 30))
rect(f, 3, 2, 12, 4, (60, 62, 66)); rect(f, 4, 3, 6, 3, (230, 200, 40))
fronts["fuel_generator"] = f
for name, px in fronts.items():
    png(f"{A}/textures/block/{name}_front.png", px)
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "top": f"{M}:block/machine_top", "bottom": f"{M}:block/machine_bottom", "side": f"{M}:block/machine_side",
        "front": f"{M}:block/{name}_front"}})
    w(f"{A}/blockstates/{name}.json", {"variants": {
        f"facing={d}": dict({"model": f"{M}:block/{name}"}, **({"y": rot} if rot else {})) for d, rot in ROT4}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}"})

# ---------------------------------------------------------------- lamps

def lamp_tex(lit, warm):
    px = canvas()
    r = random.Random(110 + lit + warm)
    glass = ((255, 245, 200) if warm else (230, 240, 255)) if lit else (120, 120, 110)
    for y in range(16):
        for x in range(16):
            px[y][x] = rgba(jitter(glass, r, 10))
    for i in range(16):
        px[0][i] = px[15][i] = px[i][0] = px[i][15] = rgba((80, 82, 86))
    for i in range(3, 13, 3):
        for j in range(1, 15): px[i][j] = rgba((90, 92, 96))              # cage bars
    return px

for name, warm in (("electric_lamp", True), ("floodlight", False)):
    for lit in (0, 1):
        suffix = "_on" if lit else ""
        png(f"{A}/textures/block/{name}{suffix}.png", lamp_tex(lit, warm))
        w(f"{A}/models/block/{name}{suffix}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{M}:block/{name}{suffix}"}})
    w(f"{A}/blockstates/{name}.json", {"variants": {
        f"light={n}": {"model": f"{M}:block/{name}{'_on' if n > 0 else ''}"} for n in range(16)}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}_on"})

# ---------------------------------------------------------------- variable transformer, energy meter, fuse holder

def front(seed, base, mark):
    px = casing(seed, base)
    rect(px, 4, 4, 11, 11, (40, 40, 42)); rect(px, 6, 6, 9, 9, (200, 110, 60))
    rect(px, 1, 1, 3, 3, mark)
    return px

png(f"{A}/textures/block/variable_transformer_side.png", casing(120, (70, 75, 110)))
png(f"{A}/textures/block/variable_transformer_front_up.png", front(121, (70, 75, 110), (90, 220, 120)))
png(f"{A}/textures/block/variable_transformer_front_down.png", front(122, (70, 75, 110), (230, 140, 40)))
png(f"{A}/textures/block/variable_transformer_back.png", front(123, (70, 75, 110), (150, 150, 150)))
for mode in ("up", "down"):
    w(f"{A}/models/block/variable_transformer_step_{mode}.json", {"parent": "minecraft:block/cube", "textures": {
        "particle": f"{M}:block/variable_transformer_side", "north": f"{M}:block/variable_transformer_front_{mode}",
        "south": f"{M}:block/variable_transformer_back", **{s: f"{M}:block/variable_transformer_side" for s in ("east", "west", "up", "down")}}})
w(f"{A}/blockstates/variable_transformer.json", {"variants": {
    f"facing={d},step_up={up}": dict({"model": f"{M}:block/variable_transformer_step_{'up' if up == 'true' else 'down'}"}, **ROT6[d])
    for d in ROT6 for up in ("true", "false")}})
w(f"{A}/models/item/variable_transformer.json", {"parent": f"{M}:block/variable_transformer_step_up"})

meter = casing(124, (200, 200, 195)); rect(meter, 3, 3, 12, 8, (20, 30, 25))
for x in (4, 6, 8, 10): rect(meter, x, 5, x, 6, (90, 220, 120))
rect(meter, 6, 10, 9, 13, (60, 62, 66))
png(f"{A}/textures/block/energy_meter_side.png", meter)
end = casing(125, (200, 200, 195)); rect(end, 6, 6, 9, 9, (200, 110, 60))
png(f"{A}/textures/block/energy_meter_end.png", end)
w(f"{A}/models/block/energy_meter.json", {"parent": "minecraft:block/cube", "textures": {
    "particle": f"{M}:block/energy_meter_side", "north": f"{M}:block/energy_meter_end", "south": f"{M}:block/energy_meter_end",
    **{s: f"{M}:block/energy_meter_side" for s in ("east", "west", "up", "down")}}})
w(f"{A}/blockstates/energy_meter.json", {"variants": {f"facing={d}": dict({"model": f"{M}:block/energy_meter"}, **ROT6[d]) for d in ROT6}})
w(f"{A}/models/item/energy_meter.json", {"parent": f"{M}:block/energy_meter"})

for has in (False, True):
    side = casing(126, (60, 62, 66)); rect(side, 4, 3, 11, 12, (25, 25, 27))
    if has:
        rect(side, 5, 4, 10, 11, (210, 210, 200)); rect(side, 5, 4, 10, 5, (200, 110, 60)); rect(side, 5, 10, 10, 11, (200, 110, 60))
    png(f"{A}/textures/block/fuse_holder{'_full' if has else ''}.png", side)
    w(f"{A}/models/block/fuse_holder{'_full' if has else ''}.json", {"parent": "minecraft:block/cube", "textures": {
        "particle": f"{M}:block/fuse_holder", "north": f"{M}:block/energy_meter_end", "south": f"{M}:block/energy_meter_end",
        **{s: f"{M}:block/fuse_holder{'_full' if has else ''}" for s in ("east", "west", "up", "down")}}})
w(f"{A}/blockstates/fuse_holder.json", {"variants": {
    f"facing={d},has_fuse={h}": dict({"model": f"{M}:block/fuse_holder{'_full' if h == 'true' else ''}"}, **ROT6[d])
    for d in ROT6 for h in ("true", "false")}})
w(f"{A}/models/item/fuse_holder.json", {"parent": f"{M}:block/fuse_holder_full"})

FUSE_COLORS = {5: (90, 170, 230), 10: (240, 80, 80), 20: (240, 200, 60), 40: (90, 200, 110)}
for amps, color in FUSE_COLORS.items():
    fz = canvas()
    rect(fz, 5, 2, 10, 13, (220, 220, 210)); rect(fz, 5, 2, 10, 3, (200, 110, 60)); rect(fz, 5, 12, 10, 13, (200, 110, 60))
    rect(fz, 6, 6, 9, 9, color)
    png(f"{A}/textures/item/fuse_{amps}a.png", fz)
    w(f"{A}/models/item/fuse_{amps}a.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/fuse_{amps}a"}})

# ---------------------------------------------------------------- data

blocks = list(fronts) + ["electric_lamp", "floodlight", "variable_transformer", "energy_meter", "fuse_holder"]
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

I, C, R, G = "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:redstone", "minecraft:glass"
shaped("electric_furnace", ["III", "CFC", "IRI"], {"I": I, "C": C, "F": "minecraft:furnace", "R": "minecraft:redstone_block"})
shaped("macerator", ["IFI", "CPC", "IRI"], {"I": I, "F": "minecraft:flint", "C": C, "P": "minecraft:piston", "R": R})
shaped("fuel_generator", ["III", "GTG", "IRI"], {"I": I, "G": f"{M}:combustion_generator", "T": f"{M}:transformer", "R": R})
shaped("electric_lamp", [" G ", "GRG", " C "], {"G": G, "R": "minecraft:glowstone_dust", "C": C}, 2)
shaped("floodlight", ["GGG", "GLG", "ICI"], {"G": G, "L": "minecraft:glowstone", "I": I, "C": C}, 2)
shaped("variable_transformer", ["RTR"], {"R": "minecraft:comparator", "T": f"{M}:transformer"})
shaped("energy_meter", ["IGI", "CRC", "III"], {"I": I, "G": "minecraft:glass_pane", "C": C, "R": "minecraft:clock"})
shaped("fuse_holder", ["I I", "C C", "I I"], {"I": I, "C": C})
for amps, wire in ((5, "minecraft:iron_nugget"), (10, "minecraft:copper_ingot"), (20, "minecraft:gold_nugget"), (40, "minecraft:gold_ingot")):
    shaped(f"fuse_{amps}a", ["G", "W", "G"], {"G": "minecraft:glass_pane", "W": wire}, 4)

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
for key, name in (("electric_furnace", "Electric Furnace"), ("macerator", "Macerator")):
    lang[f"block.{M}.{key}"] = f"{name} (50V)"
    lang[f"container.{M}.{key}"] = name
lang.update({
    f"block.{M}.fuel_generator": "Fuel Generator (200V)",
    f"container.{M}.fuel_generator": "Fuel Generator",
    f"block.{M}.electric_lamp": "Electric Lamp (50V)",
    f"block.{M}.floodlight": "Floodlight (200V)",
    f"block.{M}.variable_transformer": "Variable Transformer",
    f"block.{M}.energy_meter": "Energy Meter",
    f"block.{M}.fuse_holder": "Fuse Holder",
    **{f"item.{M}.fuse_{a}a": f"Lead Fuse ({a} A)" for a in FUSE_COLORS},
    f"tooltip.{M}.fuse": "Blows above %s A",
    f"message.{M}.fuse.empty": "No fuse fitted - right-click with a fuse",
    f"message.{M}.transformer.ratio": "Ratio is now %s : 1",
})
w(lang_path, lang)
print("ok")
