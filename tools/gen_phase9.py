"""Generates resources for battery chargers and the smartphone. Run after gen_phase2.py: python3 tools/gen_phase9.py <repo root>"""
import json, os, random, struct, sys, zlib

ROOT = sys.argv[1]
A = f"{ROOT}/src/main/resources/assets/triassicisland"
D = f"{ROOT}/src/main/resources/data"
M = "triassicisland"

src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_phase2.py")).read()
exec(src[src.index("def w(path"):src.index("# ---------------------------------------------------------------- block textures")])
exec(src[src.index("def casing("):src.index("top = casing(10)")])

ROT4 = (("north", 0), ("east", 90), ("south", 180), ("west", 270))
for name, base in (("charger_50v", (150, 155, 160)), ("charger_200v", (110, 120, 150))):
    f = casing(140 + len(name), base)
    for i, x in enumerate((2, 6, 10)):
        rect(f, x, 3, x + 3, 9, (30, 30, 32)); rect(f, x + 1, 4, x + 2, 8, (60, 200, 90) if i < 2 else (230, 180, 40))
    rect(f, 3, 11, 12, 12, (60, 200, 90))
    png(f"{A}/textures/block/{name}_front.png", f)
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "top": f"{M}:block/machine_top", "bottom": f"{M}:block/machine_bottom", "side": f"{M}:block/machine_side",
        "front": f"{M}:block/{name}_front"}})
    w(f"{A}/blockstates/{name}.json", {"variants": {
        f"facing={d}": dict({"model": f"{M}:block/{name}"}, **({"y": rot} if rot else {})) for d, rot in ROT4}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{M}:block/{name}"})
    w(f"{D}/{M}/loot_tables/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{M}:{name}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

phone = canvas()
rect(phone, 4, 1, 11, 14, (25, 26, 30)); rect(phone, 5, 2, 10, 12, (60, 120, 200))
for x, y, c in ((6, 4, (240, 240, 240)), (8, 4, (120, 220, 120)), (6, 7, (240, 200, 60)), (8, 7, (230, 90, 90))):
    rect(phone, x, y, x + 1, y + 1, c)
rect(phone, 7, 13, 8, 13, (90, 90, 95))
png(f"{A}/textures/item/smartphone.png", phone)
w(f"{A}/models/item/smartphone.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/smartphone"}})

pick = f"{D}/minecraft/tags/blocks/mineable/pickaxe.json"
tag = json.load(open(pick)); tag["values"] = sorted(set(tag["values"]) | {f"{M}:charger_50v", f"{M}:charger_200v"}); w(pick, tag)

def shaped(name, pattern, keys, count=1):
    w(f"{D}/{M}/recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in keys.items()},
        "result": {"item": f"{M}:{name}", "count": count}})

I, C, R = "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:redstone"
shaped("charger_50v", ["ICI", "IBI", "IRI"], {"I": I, "C": C, "B": f"{M}:battery", "R": R})
shaped("charger_200v", ["ICI", "IBI", "IRI"], {"I": I, "C": C, "B": f"{M}:large_battery", "R": R})
shaped("smartphone", ["IGI", "ICI", "IBI"], {"I": I, "G": "minecraft:glass_pane", "C": "minecraft:comparator", "B": f"{M}:small_economic_50v_bulb"})

lang_path = f"{A}/lang/en_us.json"
lang = json.load(open(lang_path))
lang.update({
    f"block.{M}.charger_50v": "Battery Charger (50V)", f"container.{M}.charger_50v": "Battery Charger",
    f"block.{M}.charger_200v": "Battery Charger (200V)", f"container.{M}.charger_200v": "Battery Charger",
    f"item.{M}.smartphone": "Smartphone",
    f"tooltip.{M}.phone": "Battery %s%% - right-click to open",
    f"gui.{M}.phone.message": "Message", f"gui.{M}.phone.send": "Send", f"gui.{M}.phone.call": "Call",
    f"gui.{M}.phone.call_no_voice": "Call (needs Simple Voice Chat)", f"gui.{M}.phone.answer": "Answer",
    f"gui.{M}.phone.decline": "Decline", f"gui.{M}.phone.hangup": "Hang up",
    f"gui.{M}.phone.nobody": "No one else online",
    f"gui.{M}.phone.status.calling": "Calling %s...", f"gui.{M}.phone.status.ringing": "%s is calling!",
    f"gui.{M}.phone.status.in_call": "In a call with %s",
    f"message.{M}.phone.flat": "Phone battery is flat - charge it in a Battery Charger",
    f"message.{M}.phone.unreachable": "They're offline or don't have a charged phone",
    f"message.{M}.phone.no_voice_mod": "Calls need the Simple Voice Chat mod on the server",
    f"message.{M}.phone.no_voice": "%s doesn't have Simple Voice Chat running",
    f"message.{M}.phone.busy": "One of you is already on a call",
    f"message.{M}.phone.calling": "Calling %s...",
    f"message.{M}.phone.incoming": "%s is calling you!",
    f"message.{M}.phone.button.answer": "[Answer]", f"message.{M}.phone.button.decline": "[Decline]",
    f"message.{M}.phone.button.hangup": "[Hang up]",
    f"message.{M}.phone.no_call": "No one is calling you",
    f"message.{M}.phone.call_failed": "Couldn't connect the call (is voice chat connected?)",
    f"message.{M}.phone.connected": "Call connected - talk with your voice chat key.",
    f"message.{M}.phone.declined": "%s declined the call",
    f"message.{M}.phone.ended": "Call ended",
    f"message.{M}.phone.no_answer": "No answer",
})
w(lang_path, lang)
print("ok")
