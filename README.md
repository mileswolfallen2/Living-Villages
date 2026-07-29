# Living Villages

A Minecraft Fabric mod where villages evolve over time. Instead of static villagers standing around, villages grow organically — building houses, laying roads, expanding farms, and patrolling at night.

## Features

### Core Systems

| Feature | Description |
|---|---|
| **Village Evolution** | Villages progress through phases: Hamlet → Village → Town → City. Each phase unlocks larger buildings, more roads, and denser population. |
| **Automatic Building** | Villagers construct new houses and workshops over time. Buildings use biome-appropriate materials. |
| **Road Placement** | Dirt paths and stone roads appear between buildings and radiate from the village center. |
| **Farm Expansion** | Existing farms grow with new crop rows, water sources, and fence borders. |
| **Night Patrols** | Iron golems spawn at night to patrol village streets. More guards appear in larger villages. |
| **Village Visuals** | Lanterns, fences, and banners are placed as villages grow, giving each settlement a lived-in feel. |

### Evolution Phases

| Phase | Buildings Required | Population Required | Features Unlocked |
|---|---|---|---|
| **Hamlet** | 0 | 2 | Basic houses, small farms |
| **Village** | 5 | 5 | Roads, golem patrols, lanterns |
| **Town** | 15 | 10 | Perimeter fences, larger buildings, banners |
| **City** | 30 | 20 | Full visual upgrades, dense building grid |

## Commands

All commands require **op level 2** (server operator).

| Command | Description |
|---|---|
| `/livingvillages list` | List all tracked villages with phase, population, and building count |
| `/livingvillages status` | Show detailed info for the village nearest to you |
| `/livingvillages reload` | Reload the mod configuration |
| `/livingvillages test phase <hamlet/village/town/city>` | Set a village's evolution phase |
| `/livingvillages test build` | Force a building to be placed |
| `/livingvillages test road` | Force roads to be placed |
| `/livingvillages test farm` | Force farm expansion |
| `/livingvillages test patrol` | Spawn a guard at the village |
| `/livingvillages test visual` | Apply visual upgrades (lamps, fences, banners) |
| `/livingvillages test scan` | Force a village population scan |

## Configuration

Config file: `config/livingvillages.json`

```json
{
  "evolution_speed": 1.0,
  "max_village_radius": 128,
  "guardian_strength": "iron",
  "road_material": "stone",
  "build_cooldown_ticks": 24000,
  "farm_expand_interval": 12000,
  "road_interval": 18000,
  "patrol_interval": 24000,
  "population_per_house": 2,
  "houses_per_phase_upgrade": 5,
  "place_lamps": true,
  "place_fences": true,
  "place_banners": true,
  "max_buildings_per_village": 50
}
```

### Config Options

| Option | Default | Description |
|---|---|---|
| `evolution_speed` | `1.0` | Multiplier for evolution tick speed. Higher = faster |
| `max_village_radius` | `128` | Maximum radius of any village |
| `guardian_strength` | `"iron"` | Guardian type (`iron`, `diamond`) |
| `road_material` | `"stone"` | Road block type (`dirt`, `gravel`, `cobblestone`, `stone`) |
| `build_cooldown_ticks` | `24000` | Ticks between building attempts (20 ticks = 1 second) |
| `place_lamps` | `true` | Whether lanterns are placed along roads |

## Future Ideas (Brainstorm)

### Planned Features
- **Custom Guard Mob** — A "Village Guardian" villager with armor and a sword that patrols at night
- **Dynamic Walls** — Villages build walls around themselves at Town phase
- **Resource-Based Evolution** — Villages need actual materials (wood, stone) in a nearby chest to build
- **Biome-Specific Architecture** — Desert villages build with sandstone, taiga with spruce, etc.
- **Village Quests** — Interact with village leader to get fetch quests that accelerate growth
- **Road Upgrades** — Dirt → Gravel → Cobblestone → Stone as village advances
- **Market Stalls** — Small trading posts appear in Town+ phase
- **Well / Fountain** — Central village decoration appears at Village phase
- **Lamp Posts** — Tall lamp posts instead of hanging lanterns
- **Gardens** — Decorative flower gardens around houses
- **Village Defenses** — Walls, moats, arrow slits at City phase
- **Child Villagers** — Villagers reproduce over time (population growth)
- **Profession Distribution** — Villagers take on balanced professions automatically
- **Configurable Building Pools** — Datapack-driven building schematics
- **Mini-map Integration** — Village boundaries shown on supported minimaps

### Stretch Goals
- **Inter-Village Trade** — Villages trade with each other via merchant caravans
- **Hostile Village Invasions** — Pillager raids happen dynamically
- **Player Influence** — Build near a village to influence its architectural style
- **Bridges** — Villages spanning rivers build bridges automatically

## Development

### Requirements
- Minecraft 26.2 (Chaos Cubed)
- Fabric Loader 0.19.3+
- Fabric API 0.155.2+
- Java 25+

### Building
```bash
./gradlew build
```

### Project Structure
```
src/
├── main/
│   ├── java/com/livingvillages/
│   │   ├── LivingVillages.java          # Main mod entrypoint
│   │   ├── config/ModConfig.java         # JSON config system
│   │   ├── registry/VillageRegistry.java # Village tracking & persistence
│   │   ├── registry/VillageData.java     # Per-village state
│   │   ├── evolution/
│   │   │   ├── EvolutionScheduler.java   # Tick-based evolution driver
│   │   │   └── VillagePhase.java         # Phase enum
│   │   ├── building/
│   │   │   ├── BuildingPlacer.java       # Structure placement logic
│   │   │   └── BuildingPool.java         # Building blueprint definitions
│   │   ├── road/RoadPlacer.java          # Road/path placement
│   │   ├── farm/FarmExpander.java        # Farm growth logic
│   │   ├── guardian/GuardianSystem.java  # Night patrol spawning
│   │   ├── visuals/VillageVisuals.java   # Lamp, fence, banner placement
│   │   ├── command/
│   │   │   ├── LivingVillagesCommand.java
│   │   │   └── TestCommands.java
│   │   ├── util/
│   │   │   ├── SchematicLoader.java
│   │   │   └── VillageHelper.java
│   │   └── mixin/VillagerEntityMixin.java
│   └── resources/
│       ├── fabric.mod.json
│       └── livingvillages.mixins.json
└── client/java/com/livingvillages/client/
    └── LivingVillagesClient.java
```

## License

MIT
