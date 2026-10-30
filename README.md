# Tensura - Iron's Spells Growth

A NeoForge mod for the **Tensura RPG** pack (MC 1.21.1) that gives Iron's
Spells 'n Spellbooks gear a full evolution progression along Tensura's own EP
ladder. The official integration (`tensura_iron_spells`) ships every spell
book, mage
armor set and spell weapon as a **flat terminal EP band**: gear that earns but
never climbs. This mod re-weaves those bands into chains. Armor sets, spell
books and weapons each forge upward, fork at gates, and hand off into Tensura
metals, so a wizard's kit grows with their power instead of stalling at its
spawn floor.

## What it does

*(Design phase. No release has been cut yet. The open issues on this repository
hold the full plan, research tables and open design decisions.)*

- Re-weaves the official integration's bands. Same-path overrides of the
  `tensura_iron_spells` gear files plus Gear Evolution addon chains, following
  the house doctrines: native floors and EP rates preserved, no tier skips,
  fork points inert with chains, terminals keep earning.
- Spell books climb their tier ladder: wimpy, copper, iron, gold, diamond,
  netherite and legendary, plus the school books (ice, blaze, druidic, evoker,
  necronomicon, cursed doll, dragonskin), each progress rather than sit
  terminal.
- Mage armor sets and spell weapons weave upward toward the Tensura metal
  spine at the same gates Tensura gear of that tier climbs.
- Covers gear the official integration misses: frosted helve, staff of the
  nines, claymore and cinderous soulcaller. The plan's other four candidates
  ("improved blood staff", "illusioner spell book", `truthseeker` and the base
  `spell_book`) exist only as orphan model/lang entries in Iron's Spells
  3.16.3, with no registry id and nothing to band (verified by scanning every
  class in the jar, and by the datapack GameTest).
- Optional addon integrations, inert when the addon is absent: AllTheWizard
  Gear (allthemodium/vibranium/unobtainium mage gear), Wind Spellbooks (wind
  school, aeromancer set, wind staff), Leylines (ley staff, leyline codex),
  Resonant Arcana (deathchill/phoenix/runic echoes spell books).
- Entity existence bands where the official integration leaves gaps (for
  example Wind Spellbooks' aeromancer mob).

## Re-banded files

Every row below is generated from the datagen tables in
[`RebalanceContent`](src/main/java/org/bitwisemadness/tensuraissgrowth/datagen/RebalanceContent.java).
The Java tables are the single source of truth, and validation fails the
build on any drift: **87 same-path overrides + 9 addon chains + 5 Tensura
receiving rungs = 101 files**.

Native `epGain` is preserved exactly. Where the official file states `0.02`,
the override writes `0.02`; where it omits the key (35 files), the override
omits it too, so Tensura's codec default **0.03**, the item's true native
rate, applies unchanged. Only the nine chain entries carry an explicit
`0.03`, because Gear Evolution requires the key there.

Hand-offs to Tensura mithril are **single-target** rungs (maintainer decision
on D1 in #1). Forks are used only where equivalent sibling options exist
(within-tier spell books); GE forks choose among distinct target items, so
"keep earning" is not a branch option, and a chain entry would make Gear
Evolution strip the official milestone spine this mod preserves.

### Spell books (16)

| Rungs | Floor | Gate | Becomes | Milestones kept |
|---|---|---|---|---|
| `wimpy_spell_book` | 2,000 | 5,000 (chain) | `copper_spell_book` \| `rotten_spell_book` | none (chain-driven) |
| `copper_spell_book`, `rotten_spell_book` | 5,000 | 6,000 (chain) | `iron_spell_book` \| `blaze_spell_book` | none |
| `iron_spell_book`, `blaze_spell_book` | 6,000 | 7,000 (chain) | `villager_spell_book` \| `gold_spell_book` | none |
| `villager_spell_book`, `gold_spell_book` | 7,000 | 8,000 (rung) | `diamond_spell_book` | none (gate below the spine) |
| `diamond_spell_book` | 8,000 | 10,000 (chain) | `ice_spell_book` \| `druidic_spell_book` \| `netherite_spell_book` | none |
| `ice_spell_book`, `druidic_spell_book`, `netherite_spell_book` | 10,000 | 15,000 (chain) | the five legendary books | none |
| `evoker_spell_book`, `cursed_doll_spell_book`, `necronomicon_spell_book`, `dragonskin_spell_book`, `legendary_spell_book` | 15,000 | terminal | — | full weapon spine (18k/52k/225k/750k) |

### Weapons (18)

| Rungs | Floor | Gate | Becomes | Milestones kept |
|---|---|---|---|---|
| `magehunter` | 6,000 | terminal | — | full weapon spine |
| `amethyst_rapier`, `artificer_cane`, `autoloader_crossbow`, `blood_staff`, `boreal_blade`, `decrepit_scythe`, `graybeard_staff`, `hellrazor`, `hither_thither_wand`, `ice_staff`, `keeper_flamberge`, `legionnaire_flamberge`, `lightning_rod`, `misery`, `pyrium_staff`, `spellbreaker`, `twilight_gale` | 15,000 | 225,000 | `tensura:mithril_sword` | 18k, 52k |

### Armor (53)

| Rungs | Floor | Gate | Becomes | Milestones kept |
|---|---|---|---|---|
| 11 school sets × 4 slots + `wizard_hat`/`wizard_helmet`/`wizard_chestplate`/`wizard_leggings`/`wizard_boots` (49 pieces) | 5,000 | 750,000 | `tensura:mithril_<slot>` (`wizard_hat` → `mithril_helmet`) | 18k, 52k, 225k |
| `paladin_chestplate`, `speed_boots` | 15,000 | terminal | — | full armor spine |
| `infernal_sorcerer_chestplate`, `tarnished_helmet` | 20,000 | terminal | — | 52k, 225k, 750k (18k lies below the floor) |

### Tensura receiving rungs (5)

Shipped at `data/tensura/gear_existence/`, byte-aligned with the same-path
rungs already established there, so the hand-off carry lands exactly on the
rung's floor instead of past base Tensura's mithril gate:

| Rung | Floor | Gate | Becomes | epGain |
|---|---|---|---|---|
| `tensura:mithril_sword` | 225,000 | 500,000 | `tensura:adamantite_sword` | 0.025 |
| `tensura:mithril_helmet`/`chestplate`/`leggings`/`boots` | 750,000 | 1,000,000 | `tensura:adamantite_<slot>` | 0.03 |

| Rungs | Floor | Gate | Becomes | Milestones kept |
|---|---|---|---|---|
| `irons_spellbooks:frosted_helve` | 10,000 | 15,000 | `ice_staff` | none (gate below the spine) |
| `irons_spellbooks:staff_of_the_nines`, `claymore`, `cinderous_soulcaller` | 15,000 | 225,000 | `tensura:mithril_sword` | 18k, 52k |

### Optional addon integrations (27 files)

Bands for the four ISAS addons. The files ship unconditionally and are inert
when the addon is absent (item-id lookup simply misses). Placement per D4 in
#1; epGain is omitted everywhere it has no native source, so the codec default
0.03 applies, except the aeromancer set which joins the school weave at the
stated 0.02.

| Rungs | Floor | Gate | Becomes | Milestones kept |
|---|---|---|---|---|
| `wind_spellbooks:aeromancer_helmet`/`chestplate`/`leggings`/`boots` | 5,000 | 750,000 | `tensura:mithril_<slot>` | 18k, 52k, 225k |
| `wind_spellbooks:wind_spell_book` (fork, joins the school ladder at diamond's fork too) | 10,000 | 15,000 (chain) | the five legendary books | none (chain-driven) |
| `wind_spellbooks:wind_staff`, `leylines:ley_staff` | 15,000 | 225,000 | `tensura:mithril_sword` | 18k, 52k |
| `leylines:leyline_codex`, `resonant_arcana:deathchill_spell_book`/`phoenix_spell_book`/`runic_echoes_spell_book` | 15,000 | terminal | — | full weapon spine |
| `allthewizardgear:allthemodium_spell_book` (entered via netherite's fork, D3) | 15,000 | 52,000 | `allthewizardgear:vibranium_spell_book` | 18k |
| `allthewizardgear:vibranium_spell_book` | 52,000 | 225,000 | `allthewizardgear:unobtainium_spell_book` | 52k |
| `allthewizardgear:unobtainium_spell_book` | 225,000 | terminal | — | 225k, 750k |
| `allthewizardgear:allthemodium_mage_<slot>` (standalone smithing-entry bands) | 225,000 | 500,000 | `allthewizardgear:vibranium_mage_<slot>` | 225k |
| `allthewizardgear:vibranium_mage_<slot>` | 500,000 | 1,000,000 | `allthewizardgear:unobtainium_mage_<slot>` | 750k |
| `allthewizardgear:unobtainium_mage_<slot>` | 1,000,000 | terminal | — | — |

Without an addon installed, GE's resolver drops its absent targets: the
netherite fork rolls among the five ISAS books, the diamond fork among the
three ISAS school books, and the addon bands simply never match an item.

### Structure magicule + spawn exemptions (infinite-dungeon-spawn-safety)

Every ISAS-family structure lifts the surrounding chunks to at least the
related mobs' EP baseline (datapack rows read by infinite-dungeon-spawn-safety;
radii are the structure's grounds scale):

| Structure | Radius | Floor (= related mob baseline) |
|---|---|---|
| `irons_spellbooks:catacombs` | 256 | 120,000 (Dead King) |
| `irons_spellbooks:pyromancer_tower`, `mountain_tower` | 96 | 12,000 (pyromancer / cryomancer) |
| `irons_spellbooks:citadel`, `evoker_fort`, `mangrove_hut`, `ancient_battleground` | 128 / 128 / 64 / 192 | 10,000 (keeper / archevoker / apothecarist) |
| `irons_spellbooks:ice_spider_den`, `impaled_icebreaker` | 64 / 96 | 6,000 (ice spider; provisional for the icebreaker) |
| `wind_spellbooks:windmill` | 64 | 12,000 (aeromancer) |

`wayward_compass_locator` is a locator (not a structure) and priest houses are
vanilla village pieces (no structure id) - both out of scope. The ISAS bosses
`dead_king` and `fire_boss` are **exempt** from the spawn-safety gate and
spawn wherever their encounters take them.

### Entity existence (1 file)

| Mob | Aura | Magicule | Spiritual health | Abilities |
|---|---|---|---|---|
| `wind_spellbooks:aeromancer` | 2,000 – 4,000 | 8,000 – 12,000 | 400 | `tensura:wind_gust` |

Banded at the official integration's wizard-mage tier (the cryomancer /
priest / pyromancer bands). Leylines' nightborn_elf is worldgen/faction
content, not a combat mob, and is deliberately unbanded; the official 18
ISAS bands are untouched (D2 in #1).

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
