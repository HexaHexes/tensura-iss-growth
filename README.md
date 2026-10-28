# Tensura - Iron's Spells Growth

A NeoForge mod for the **Tensura RPG** pack (MC 1.21.1) that gives Iron's Spells
'n Spellbooks gear a full evolution progression along Tensura's own EP ladder,
the way this org's Ice and Fire mod does for Ice and Fire equipment. The
official integration (`tensura_iron_spells`) ships every spell book, mage armor
set and spell weapon as a **flat terminal EP band** - gear that earns but never
climbs. This mod re-weaves those bands into chains: armor sets, spell books and
weapons each forge upward, fork at gates, and hand off into Tensura metals, so a
wizard's kit grows with their power instead of stalling at its spawn floor.

## What it does

*(Design phase - no release yet. The open issues on this repository hold the
full plan, research tables and open design decisions.)*

- **Re-weaves the official integration's bands.** Same-path overrides of the
  `tensura_iron_spells` gear files plus Gear Evolution addon chains, following
  the house doctrines: native floors and EP rates preserved, no tier skips,
  fork points inert with chains, terminals keep earning.
- **Spell books climb their tier ladder** - wimpy, copper, iron, gold, diamond,
  netherite, legendary and the school books (ice, blaze, druidic, evoker,
  necronomicon, cursed doll, dragonskin) each progress rather than sit terminal.
- **Mage armor sets and spell weapons weave upward** toward the Tensura metal
  spine at the same gates Tensura gear of that tier climbs.
- **Covers gear the official integration misses** - frosted helve, truthseeker,
  staff of the nines, claymore, improved blood staff, cinderous soulcaller,
  illusioner spell book, and the base spell book.
- **Optional addon integrations**, inert when the addon is absent: AllTheWizard
  Gear (allthemodium/vibranium/unobtainium mage gear), Wind Spellbooks (wind
  school, aeromancer set, wind staff), Leylines (ley staff, leyline codex),
  Resonant Arcana (deathchill/phoenix/runic echoes spell books).
- **Entity existence bands** where the official integration leaves gaps (for
  example Wind Spellbooks' aeromancer mob).

## Getting it in-game

Not yet available - no release has been cut. When the first alpha ships: drop
the jar in `mods/` alongside Tensura: Reincarnated and Iron's Spells, and the
rebalanced datapack inside the jar applies itself on world load.

## Dependencies

| | Mod | Why |
|---|---|---|
| Required | Minecraft / NeoForge | 1.21.1 / 21.1.259 |
| Required | Tensura: Reincarnated (+ ManasCore) | The EP ladder and gear_existence registry this mod writes into |
| Required | Iron's Spells 'n Spellbooks | The gear being re-woven |
| Required | Tensura: Iron's Spells (official integration) | Its flat bands are what this mod overrides |
| Optional | Gear Evolution | Evolution chains only resolve with GE installed; without it the band overrides still apply |
| Optional | AllTheWizardGear, Wind Spellbooks, Leylines, Resonant Arcana | Their gear gets bands only from this mod; files are inert when the addon is absent |

## Credits

- Built on [NeoForge](https://neoforged.net/).
- Integrates [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spellbooks)
  and [Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura-reincarnated).

## License

GNU Lesser General Public License v2.1 - see [`LICENSE`](LICENSE).
Copyright (c) 2026 Bitwise Madness.

## AI disclosure

Parts of this project were written with the assistance of AI coding tools.
AI-assisted code, data and documentation ship only after human review by the
maintainers.
