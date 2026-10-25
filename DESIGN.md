# Triassic Island — Design Doc

Minecraft **1.20.1**, **Forge** (built against 47.1.0; also runs on NeoForge 1.20.1). Mod ID: `triassicisland`. Package: `com.triassicisland`.

An all-in-one mod combining prehistoric life with 21st-century technology.

## 1. Dinosaurs (30+ species)
- **Fossils & DNA cloning**: fossil ore / amber in stone → Fossil Cleaner → DNA Extractor →
  DNA Sequencer (quality %) → Embryo Fabricator → Incubator → hatchling. Hybrid DNA later.
- **Park building**: enclosures, electric fences, feeders, visitor center decor, dinosaur
  escape behavior when fences lose power.
- Species list (initial target, grouped):
  - Theropods: T. rex, Velociraptor, Dilophosaurus, Spinosaurus, Allosaurus, Carnotaurus,
    Baryonyx, Compsognathus, Giganotosaurus, Ceratosaurus, Coelophysis (Triassic!), Herrerasaurus,
    Utahraptor, Gallimimus, Therizinosaurus, Oviraptor
  - Sauropods: Brachiosaurus, Apatosaurus, Diplodocus, Plateosaurus, Mamenchisaurus
  - Ornithischians: Triceratops, Stegosaurus, Ankylosaurus, Parasaurolophus, Pachycephalosaurus,
    Edmontosaurus, Iguanodon, Styracosaurus, Kentrosaurus
  - Flyers: Pteranodon, Dimorphodon, Quetzalcoatlus
  - Marine: Mosasaurus, Plesiosaurus, Ichthyosaurus, Liopleurodon
- Models: generated in code (`client/dino/DinoModelBuilder`) from `dino/DinoSpecs`; skins painted to match (`DinoSkin`).
  Chosen over GeckoLib because GeckoLib assets are hand-made in Blockbench.

## 2. Electricity (Electrical Age–style)
- Real voltage / current / resistance simulation over a node graph. Every block is a node tied to ground
  (loads = resistors, sources = Norton equivalents); each connected network is solved every tick
  with conjugate gradients (`power/Network.java`). Tiers like Electrical Age: 50V / 200V / 800V.
- Wires have gauges & current ratings; overcurrent → heat → melting/fire.
- Transformers (step up/down), low/medium/high voltage tiers.
- Extras:
  - **Generators**: solar, wind, nuclear reactor, gas/diesel generators (Immersive Petroleum fuels).
  - **Power poles & overhead lines** (catenary rendering); bridge to Immersive Engineering wires.
  - **Electric fences** for enclosures; power loss = dinosaurs can break out.
  - **Grid control**: breakers, fuses, meters, backup generators, control room integration.
  - **Hidden cables**: power & data cables can be disguised as any block (camo);
    **Engineer's Goggles** reveal them.

## 3. Computers (ComputerCraft-style, Python)
- Language: **Python 2.7 via Jython** (bundled with jarJar), sandboxed, time-sliced per tick (line hook).
  Jython's own stdlib modules expose Java's class loader, so imports only return our own modules.
- Computers consume power from the mod's grid; shut down on brownout.
- Data cables / network: computers, cameras, monitors, doors join a network.
- **Pre-made programs**: `cameras` (browse/view cameras on the network), `fences` (grid/fence
  status), `doors`, `dinotracker`.
- Peripherals: monitors, security cameras, keycard readers, speakers, modems.
- **Security**: security cameras, keycard doors, alarms.

## 4. Modern life
- **Buildings/decor**: concrete, steel, glass panels, lab furniture, modern lighting (powered).
- **Appliances & phones**: fridge, TV (shows camera feeds), smartphone item (park control app).
  - ✅ Smartphone: messages to players (stored per world), calls via Simple Voice Chat (optional, compileOnly API;
    a call is a hidden isolated voice group). ✅ Battery Chargers 50V/200V for phones and flashlights. ⬜ park control app
  - ✅ WiFi Router (48 blocks): phones need signal; links computers to wireless devices. ✅ Smart Switch (player/computer),
    ✅ automatic Sliding Glass Door (auto / held open / locked). Program: smart
- **Weapons/tools**: tranquilizer gun & darts, taser, power drill / chainsaw (battery powered).

## 5. Compatibility (Forge 1.20.1)
| Mod | Integration |
|---|---|
| Immersive Engineering | Wire connection adapter, IE ↔ grid power conversion |
| Immersive Petroleum | Gasoline/diesel as fuel in our generators |
| Immersive Vehicles (MTS) | Park tour vehicles via content pack; charging stations |
| Immersive Railroading | Park monorail/train content; electrified track power |

| Create (optional) | ✅ Kinetic Generators 50V/200V: shaft rotation -> grid power, 1 SU = 1 W (16 SU/RPM). Registered only if Create is loaded; drops itself in code, tag entries `required: false`, recipes `forge:mod_loaded` |
| Simple Voice Chat (optional) | ✅ phone calls |

All integrations are **optional** — the mod runs standalone.

## Build phases
Dinosaurs are saved for last (user's call, 2026-10-25).
1. ✅ Project foundation: registries, creative tab, fossil ore & first items
2. ✅ Fossil → DNA → hatching machine chain (machines with GUIs). All machines need grid power.
3. Electrical grid
   - ✅ v1: node solver, 50V cable (burns above 20 A), combustion generator, battery, small solar panel, multimeter
   - ✅ v2: 200V/800V cables, 4:1 transformer, circuit breaker (redstone-controllable), wind turbine, large battery; overvoltage destroys any rated block
   - ✅ v3 (more Electrical Age): 5-block wind turbine tower with spinning rotor, electric furnace, macerator,
     200V fuel generator, 50V lamp / 200V floodlight, variable transformer (2/4/8/16), energy meter, fuse holder + lead fuses
   - ✅ all Electrical Age lights: 11 lamp sockets (incl. swinging suspended), 9 bulb types with lifetime/warm-up/farming,
     streetlight, string lights, lamp supply (wireless), 50/200V emergency lamps, small/improved flashlights
   - Later: steam/gas turbines + heat furnaces, signal cables/processors, battery charger + portable batteries,
     nuclear reactor, diesel/gas generators (Immersive Petroleum)
4. ✅ Electric fences (200V / 800V) + gate, feeder, warning sign. `ElectricFenceBlockEntity.isEnergized()` and `FeederBlockEntity.takeFood()` are ready for dinosaur escape/feeding AI
5. ✅ Computers: Jython 2.7 (sandboxed: no stdlib/Java; safe math/random/term/fs/net/computer modules), terminal + editor GUI,
   1 MB filesystem, data network (every grid block is a device), messaging, programs: devices/power/fences/doors/chat
6. ✅ Security cameras (viewed full-screen from computers), text monitors, keycards 1-5 + readers, alarms;
   programs: cameras, security, dinotracker. ⬜ live camera feed on monitors/TVs (needs render-to-texture)
7. Hidden cables + goggles, power poles & overhead lines
   - ✅ data cable, combined cable (50V + data), hidden (disguisable) versions of all cables, Engineer's Goggles
   - ✅ power poles & overhead lines (utility pole, pole insulator, 50/200/800V wire coils, sagging wires up to 32 blocks)
   - ✅ lever on a hidden power cable = switch
8. Modern decor, appliances, phone, weapons/tools
9. Compat modules (IE, IP, MTS, IR)
10. Dinosaurs
   - ✅ 30 land species: models generated in code from body plan + proportions + features (no GeckoLib),
     procedural animation and skins; growth, hunger, feeders, grazing, hunting, temperaments, fence escapes, hatching
   - ✅ flyers (Pteranodon, Dimorphodon, Quetzalcoatlus: fly, fold wings on ground) and marine (Mosasaurus, Plesiosaurus,
     Ichthyosaurus, Liopleurodon: swim, dry out on land, hatch in water)
   - ✅ 24 more land species (61 total). Add more by appending to Species (keep order) + one DinoSpecs line
