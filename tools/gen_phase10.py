"""Generates resources for the WiFi router, smart switch and sliding glass door. Run after gen_phase2.py:
python3 tools/gen_phase10.py <repo root>"""
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

# Router
for on in (False, True):
    px = casing(150, (235, 235, 230)); rect(px, 2, 6, 13, 9, (40, 40, 44))
    for i, x in enumerate((3, 6, 9, 12)): px[7][x] = rgba((80, 230, 120) if on else (90, 90, 90))
    png(f"{A}/textures/block/wifi_router{'_on' if on else ''}.png", px)
    w(f"{A}/models/block/wifi_router{'_on' if on else ''}.json", {"parent": "minecraft:block/block", "textures": {
        "body": f"{M}:block/wifi_router{'_on' if on else ''}", "antenna": f"{M}:block/socket_metal", "particle": f"{M}:block/wifi_router"},
        "elements": [el([3, 0, 4], [13, 3, 12], "#body"), el([4, 3, 10], [5, 9, 11], "#antenna"), el([11, 3, 10], [12, 9, 11], "#antenna")]})
w(f"{A}/blockstates/wifi_router.json", {"variants": {"powered=false": {"model": f"{M}:block/wifi_router"},
                                                     "powered=true": {"model": f"{M}:block/wifi_router_on"}}})
w(f"{A}/models/item/wifi_router.json", {"parent": f"{M}:block/wifi_router_on"})

# Smart switch
for on in (False, True):
    px = casing(151, (200, 205, 210)); rect(px, 5, 3, 10, 12, (240, 240, 240))
    rect(px, 6, 4 if on else 8, 9, 7 if on else 11, (60, 200, 90) if on else (200, 50, 50))
    png(f"{A}/textures/block/smart_switch{'_on' if on else ''}.png", px)
    w(f"{A}/models/block/smart_switch{'_on' if on else ''}.json", {"parent": "minecraft:block/cube_all",
        "textures": {"all": f"{M}:block/smart_switch{'_on' if on else ''}"}})
w(f"{A}/blockstates/smart_switch.json", {"variants": {"on=false": {"model": f"{M}:block/smart_switch"},
                                                      "on=true": {"model": f"{M}:block/smart_switch_on"}}})
w(f"{A}/models/item/smart_switch.json", {"parent": f"{M}:block/smart_switch_on"})

# Sliding door: the block model is just the frame rails; the glass is drawn by the renderer.
glass = canvas(64, 64)
for y in range(64):
    for x in range(64):
        glass[y][x] = (200, 230, 240, 90)
for y in range(0, 34):
    for x in range(0, 36):
        if x in (0, 1, 17, 18, 34, 35) or y in (0, 1, 32, 33): glass[y][x] = (150, 155, 160, 255)
png(f"{A}/textures/block/sliding_door_glass.png", glass)
rail = f"{M}:block/socket_metal"
for half, box in (("lower", [0, 0, 6, 16, 1, 10]), ("upper", [0, 15, 6, 16, 16, 10])):
    w(f"{A}/models/block/sliding_glass_door_{half}.json", {"parent": "minecraft:block/block", "textures": {"rail": rail, "particle": rail},
        "elements": [el(box[:3], box[3:], "#rail")]})
parts = []
for facing, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    for half in ("lower", "upper"):
        parts.append({"when": {"facing": facing, "half": half}, "apply": dict({"model": f"{M}:block/sliding_glass_door_{half}"}, **({"y": rot} if rot else {}))})
w(f"{A}/blockstates/sliding_glass_door.json", {"multipart": parts})
icon = canvas()
rect(icon, 3, 0, 12, 15, (150, 155, 160)); rect(icon, 4, 1, 11, 14, (190, 225, 240))
for y in range(2, 14, 3): icon[y][5] = rgba((240, 250, 255))
png(f"{A}/textures/item/sliding_glass_door.png", icon)
w(f"{A}/models/item/sliding_glass_door.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/sliding_glass_door"}})

# Data
for b in ("wifi_router", "smart_switch"):
    w(f"{D}/{M}/loot_tables/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
w(f"{D}/{M}/loot_tables/blocks/sliding_glass_door.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
    "entries": [{"type": "minecraft:item", "name": f"{M}:sliding_glass_door", "conditions": [{"condition": "minecraft:block_state_property",
        "block": f"{M}:sliding_glass_door", "properties": {"half": "lower"}}]}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick)); tag["values"] = sorted(set(tag["values"]) | {f"{M}:{b}" for b in ("wifi_router", "smart_switch", "sliding_glass_door")}); w(pick, tag)

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})
I, R, DC = "minecraft:iron_ingot", "minecraft:redstone", f"{M}:data_cable"
shaped("wifi_router", ["N N", "IRI", "IDI"], {"N": "minecraft:iron_nugget", "I": I, "R": "minecraft:ender_pearl", "D": DC})
shaped("smart_switch", ["IRI", "CLC", "IDI"], {"I": I, "R": R, "C": "minecraft:copper_ingot", "L": "minecraft:lever", "D": DC})
shaped("sliding_glass_door", ["GI", "GR", "GI"], {"G": "minecraft:glass", "I": I, "R": "minecraft:piston"})

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({
    f"block.{M}.wifi_router": "WiFi Router (50V)", f"block.{M}.smart_switch": "Smart Switch",
    f"block.{M}.sliding_glass_door": "Sliding Glass Door (50V)",
    f"message.{M}.door.mode.auto": "Door: automatic", f"message.{M}.door.mode.open": "Door: held open",
    f"message.{M}.door.mode.locked": "Door: locked",
    f"message.{M}.phone.no_signal": "No signal - you need to be near a powered WiFi Router",
    f"message.{M}.phone.signal_lost": "Lost signal - call dropped",
    f"gui.{M}.phone.no_signal": "No signal",
})
w(lang_path, lang)
print("ok")
