"""Generates phase-1 resources (JSON + placeholder 16x16 textures). Run from repo root."""
import json, os, random, struct, zlib, sys

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2); f.write("\n")

def png(path, pixels):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = b"".join(b"\x00" + bytes(c for px in row for c in px) for row in pixels)
    chunk = lambda t, d: struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
                + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))

def noise(base, var, seed):
    r = random.Random(seed)
    return [[tuple(max(0, min(255, c + r.randint(-var, var))) for c in base) + (255,) for _ in range(16)] for _ in range(16)]

def speckle(px, color, seed, n=14):
    r = random.Random(seed)
    for _ in range(n):
        x, y = r.randint(1, 13), r.randint(1, 13)
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            if r.random() < 0.8: px[y + dy][x + dx] = color + (255,)
    return px

def blob(color, seed, accent=None):
    """Simple item sprite: a rounded blob on transparent background."""
    r = random.Random(seed)
    px = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 30 + r.randint(-4, 4):
                px[y][x] = tuple(max(0, min(255, c + r.randint(-20, 20))) for c in color) + (255,)
    if accent:
        for x, y in ((6, 6), (7, 7), (8, 7), (9, 6), (7, 8), (8, 8)):
            px[y][x] = accent + (255,)
    return px

# --- Textures ---
png(f"{A}/textures/block/fossil_ore.png", speckle(noise((125, 125, 125), 12, 1), (230, 220, 190), 2))
png(f"{A}/textures/block/deepslate_fossil_ore.png", speckle(noise((75, 75, 80), 10, 3), (215, 205, 175), 4))
png(f"{A}/textures/block/amber_ore.png", speckle(noise((125, 125, 125), 12, 5), (235, 150, 30), 6, 10))
png(f"{A}/textures/item/fossil_fragment.png", blob((225, 215, 185), 7))
png(f"{A}/textures/item/amber.png", blob((235, 145, 25), 8))
png(f"{A}/textures/item/mosquito_in_amber.png", blob((235, 145, 25), 9, accent=(40, 25, 15)))
syringe = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
for i in range(3, 13):
    for t in (-1, 0, 1):
        syringe[15 - i][i + t] = (210, 230, 240, 255)
for i in range(13, 16): syringe[15 - i][i] = (160, 160, 170, 255)
for i in range(1, 3): syringe[15 - i][i] = (90, 90, 100, 255)
png(f"{A}/textures/item/empty_syringe.png", syringe)

# --- Models / blockstates ---
blocks = ["fossil_ore", "deepslate_fossil_ore", "amber_ore"]
for b in blocks:
    w(f"{A}/blockstates/{b}.json", {"variants": {"": {"model": f"{M}:block/{b}"}}})
    w(f"{A}/models/block/{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{M}:block/{b}"}})
    w(f"{A}/models/item/{b}.json", {"parent": f"{M}:block/{b}"})
for i in ["fossil_fragment", "amber", "mosquito_in_amber", "empty_syringe"]:
    w(f"{A}/models/item/{i}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/{i}"}})

# --- Lang ---
w(f"{A}/lang/en_us.json", {
    "itemGroup.triassicisland.main": "Triassic Island",
    "block.triassicisland.fossil_ore": "Fossil Ore",
    "block.triassicisland.deepslate_fossil_ore": "Deepslate Fossil Ore",
    "block.triassicisland.amber_ore": "Amber Ore",
    "item.triassicisland.fossil_fragment": "Fossil Fragment",
    "item.triassicisland.amber": "Amber",
    "item.triassicisland.mosquito_in_amber": "Mosquito in Amber",
    "item.triassicisland.empty_syringe": "Empty Syringe",
})

# --- Loot tables (silk touch drops the ore, otherwise items, fortune applies) ---
silk = {"condition": "minecraft:match_tool", "predicate": {"enchantments": [
    {"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}
def ore_loot(block, entries):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{
        "type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{M}:{block}", "conditions": [silk]}] + entries}]}]}
def drop(item, lo, hi):
    return {"type": "minecraft:item", "name": f"{M}:{item}", "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}},
        {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
        {"function": "minecraft:explosion_decay"}]}
w(f"{D}/{M}/loot_tables/blocks/fossil_ore.json", ore_loot("fossil_ore", [drop("fossil_fragment", 1, 2)]))
w(f"{D}/{M}/loot_tables/blocks/deepslate_fossil_ore.json", ore_loot("deepslate_fossil_ore", [drop("fossil_fragment", 1, 3)]))
# Amber: 15% chance of a mosquito, otherwise plain amber.
mosq = {"type": "minecraft:item", "name": f"{M}:mosquito_in_amber", "conditions": [
    {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.15, 0.2, 0.25, 0.33]}]}
w(f"{D}/{M}/loot_tables/blocks/amber_ore.json", ore_loot("amber_ore", [mosq, drop("amber", 1, 2)]))

# --- Tags ---
ids = [f"{M}:{b}" for b in blocks]
w(f"{D}/minecraft/tags/blocks/mineable/pickaxe.json", {"values": ids})
w(f"{D}/minecraft/tags/blocks/needs_iron_tool.json", {"values": ids})

# --- Worldgen: ores in the overworld ---
def ore(name, targets, size, count, lo, hi):
    w(f"{D}/{M}/worldgen/configured_feature/{name}.json", {"type": "minecraft:ore", "config": {
        "size": size, "discard_chance_on_air_exposure": 0.0, "targets": targets}})
    w(f"{D}/{M}/worldgen/placed_feature/{name}.json", {"feature": f"{M}:{name}", "placement": [
        {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
            "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}}},
        {"type": "minecraft:biome"}]})
    w(f"{D}/{M}/forge/biome_modifier/{name}.json", {"type": "forge:add_features",
        "biomes": "#minecraft:is_overworld", "features": f"{M}:{name}", "step": "underground_ores"})
tgt = lambda tag, block: {"target": {"predicate_type": "minecraft:tag_match", "tag": tag}, "state": {"Name": f"{M}:{block}"}}
ore("fossil_ore", [tgt("minecraft:stone_ore_replaceables", "fossil_ore"),
                   tgt("minecraft:deepslate_ore_replaceables", "deepslate_fossil_ore")], 7, 8, -48, 64)
ore("amber_ore", [tgt("minecraft:stone_ore_replaceables", "amber_ore")], 5, 5, 0, 80)
print("ok")
