package org.bitwisemadness.tensuraissgrowth.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for the rebalanced datapack this mod ships (issue #2),
 * mirroring tensura-iaf-growth's datagen doctrine: the Java tables below
 * generate every file, and {@link #validate(Content)} fails the build on any
 * drift, skip or path collision before a single byte is written.
 *
 * <h2>What the official integration ships</h2>
 * All 87 of the official {@code tensura_iron_spells} gear files are flat
 * terminals: keys are only {@code type}, {@code minEP}, {@code epGain} (52
 * files state {@code 0.02}; the other 35 omit it) and {@code uniqueEvolutions}
 * - the 18,000 / 52,000 / 225,000 / 750,000 milestone spine. No file carries
 * {@code maxEP} or {@code evolution}: gear earns forever and never climbs.
 * This mod overrides every one of those 87 files at the same resource paths
 * (pack datapack priority) and adds the {@code addon_chains} ladders beside
 * them.
 *
 * <h2>Decisions (see issues #1 and #2)</h2>
 * <ul>
 *   <li><b>Fork where equivalent options exist, single-target otherwise</b>
 *       (maintainer decision on D1): the spell-book ladder forks between
 *       within-tier siblings (wimpy -&gt; copper | rotten, the 10k books
 *       -&gt; the five 15k books, ...) as even {@code evolutionOptions}
 *       lists. The hand-offs to Tensura mithril are single-target
 *       {@code evolution} rungs, because GE forks choose among <em>distinct
 *       target items</em> - "keep earning" is the absence of a chain, not a
 *       branch option - and because any addon_chains entry makes Gear
 *       Evolution strip the item's {@code uniqueEvolutions}, which would
 *       destroy the official milestone spine this mod must preserve.</li>
 *   <li><b>epGain preserved exactly</b>: Tensura's {@code GearExistenceData}
 *       codec declares {@code epGain} as {@code optionalFieldOf("epGain",
 *       0.03)} - the native rate of every key-absent official file is
 *       <b>0.03</b>. This table therefore omits the key wherever the official
 *       file omits it (the codec fills 0.03, unchanged), writes {@code 0.02}
 *       verbatim where the official file states it, and puts an explicit
 *       {@code 0.03} only on the addon_chains entries, whose {@code epGain}
 *       Gear Evolution requires to be present.</li>
 *   <li><b>Milestones survive only on plain rungs</b>: every kept
 *       {@code uniqueEvolutions} entry sits strictly between the item's
 *       floor and its gate. Fork books drop theirs (a chain entry is
 *       required for the fork, and GE strips milestones for chain-driven
 *       items); rungs whose gate is at or below the first spine EP (the
 *       7,000-&gt;8,000 book rungs) carry none because none could ever
 *       fire.</li>
 *   <li><b>Mithril receiving rungs</b> at {@code data/tensura/gear_existence/}
 *       are byte-aligned with tensura-iaf-growth's files (the sanctioned
 *       same-path copy per REPO.md): the hand-off carry would otherwise
 *       land past base Tensura's mithril floor and instant-re-evolve.</li>
 * </ul>
 */
public final class RebalanceContent {

    private RebalanceContent() {
    }

    // --- Output paths --------------------------------------------------------

    /** Same-path overrides of the official integration's files. */
    private static final String ISS_GEAR = "data/irons_spellbooks/gear_existence/";
    /** Gear Evolution chain ladders for the fork books. */
    private static final String CHAINS = "data/gearevolution/addon_chains/irons_spellbooks/";
    /** Tensura-side receiving rungs for the mithril hand-offs. */
    private static final String TENSURA_GEAR = "data/tensura/gear_existence/";
    /** Gear Evolution ships no ISS builtins, so this map stays empty. */
    private static final String GE_GEAR = "data/gearevolution/gear_existence/irons_spellbooks/";

    // --- Ladder gates --------------------------------------------------------

    /** wimpy -&gt; copper | rotten. */
    private static final int WIMPY_GATE = 5000;
    /** copper | rotten -&gt; iron | blaze. */
    private static final int COMMON_TIER_GATE = 6000;
    /** iron | blaze -&gt; villager | gold. */
    private static final int UNCOMMON_TIER_GATE = 7000;
    /** villager | gold -&gt; diamond (the one single-target book step below the school tiers). */
    private static final int DIAMOND_GATE = 8000;
    /** diamond -&gt; ice | druidic | netherite. */
    private static final int SCHOOL_TIER_GATE = 10000;
    /** ice | druidic | netherite -&gt; the five legendary books (terminals). */
    private static final int LEGENDARY_GATE = 15000;
    /** The fifteen-thousand weapons hand off to Tensura mithril. */
    private static final int MITHRIL_WEAPON_GATE = 225000;
    /** The school armor sets hand off to Tensura mithril. */
    private static final int MITHRIL_ARMOR_GATE = 750000;

    // --- Milestone spines (verbatim from the official files) -----------------

    private static final List<Integer> WEAPON_SPINE = List.of(18000, 52000, 225000, 750000);
    private static final List<Integer> ARMOR_SPINE = List.of(18000, 52000, 225000, 750000);

    /** mainhand {@code generic.attack_damage} amounts per spine EP. */
    private static final Map<Integer, Double> WEAPON_BONUS = Map.of(
            18000, 7.0d,
            52000, 13.0d,
            225000, 15.0d,
            750000, 29.0d);

    /** Per-slot {@code generic.armor} / {@code generic.armor_toughness} pairs per spine EP. */
    private static final Map<Integer, double[]> ARMOR_BONUS = Map.of(
            18000, new double[] {0.0d, 1.5d},
            52000, new double[] {3.0d, 3.0d},
            225000, new double[] {1.0d, 1.0d},
            750000, new double[] {1.0d, 2.0d});

    private static final List<String> ARMOR_SLOTS = List.of("head", "chest", "legs", "feet");

    // --- epGain ---------------------------------------------------------------

    /** Stated by the official files that carry the key; written verbatim. */
    private static final double EP_GAIN_STATED = 0.02d;
    /**
     * Tensura's codec default ({@code optionalFieldOf("epGain", 0.03)}) - the
     * native rate of every key-absent official file. Written explicitly only
     * on addon_chains entries, whose {@code epGain} Gear Evolution requires.
     */
    private static final double EP_GAIN_DEFAULT = 0.03d;
    /**
     * Tolerance when validating a generated {@code epGain} against
     * {@link #EP_GAIN_DEFAULT}: the pack writes the constant itself, so the
     * delta is exactly zero unless the generator drifted - but validation
     * compares parsed doubles, so an epsilon (never a bit comparison) is the
     * correct check.
     */
    private static final double EP_GAIN_TOLERANCE = 1.0e-9;

    // --- Item tables ----------------------------------------------------------
    // Every id in these tables is FULL (namespace + path): chain
    // evolutionOptions are ResourceLocation-parsed by Gear Evolution, and a
    // bare path would land in the minecraft namespace, be filtered as absent
    // and silently flatten a fork into a terminal (caught live by the
    // datapack_references_resolve GameTest).

    private static final String ISAS = "irons_spellbooks:";

    /** Fork books: the within-tier sibling fork targets, all full ids. */
    private static final List<String> WIMPY_FORK = List.of(ISAS + "copper_spell_book", ISAS + "rotten_spell_book");
    private static final List<String> COMMON_FORK = List.of(ISAS + "iron_spell_book", ISAS + "blaze_spell_book");
    private static final List<String> UNCOMMON_FORK = List.of(ISAS + "villager_spell_book",
            ISAS + "gold_spell_book");
    /** diamond's fork: the three ISAS school books plus wind_spell_book (joins per D4). */
    private static final List<String> SCHOOL_FORK = List.of(ISAS + "ice_spell_book", ISAS + "druidic_spell_book",
            ISAS + "netherite_spell_book", "wind_spellbooks:wind_spell_book");
    private static final List<String> LEGENDARY_FORK = List.of(ISAS + "evoker_spell_book",
            ISAS + "cursed_doll_spell_book", ISAS + "necronomicon_spell_book", ISAS + "dragonskin_spell_book",
            ISAS + "legendary_spell_book");

    /** School armor sets: 11 schools x 4 slots plus the 5-piece wizard set. */
    private static final List<String> ARMOR_SCHOOLS = List.of(
            "archevoker", "cryomancer", "cultist", "electromancer", "netherite_mage", "plagued",
            "priest", "pumpkin", "pyromancer", "shadowwalker", "wandering_magician");
    private static final List<String> SCHOOL_SLOTS = List.of("helmet", "chestplate", "leggings", "boots");
    private static final List<String> WIZARD_PIECES = List.of(
            "wizard_hat", "wizard_helmet", "wizard_chestplate", "wizard_leggings", "wizard_boots");

    /** The fifteen-thousand weapons that hand off to tensura:mithril_sword. */
    private static final List<String> SPELL_WEAPONS = List.of(
            "amethyst_rapier", "artificer_cane", "autoloader_crossbow", "blood_staff", "boreal_blade",
            "decrepit_scythe", "graybeard_staff", "hellrazor", "hither_thither_wand", "ice_staff",
            "keeper_flamberge", "legionnaire_flamberge", "lightning_rod", "misery", "pyrium_staff",
            "spellbreaker", "twilight_gale");

    // --- Optional addon gear (issue #3, placement per D4 in #1) ---------------

    /** Per-namespace roots; each addon namespace is owned by this mod (REPO.md). */
    private static final String WIND_GEAR = "data/wind_spellbooks/gear_existence/";
    private static final String LEY_GEAR = "data/leylines/gear_existence/";
    private static final String RESONANT_GEAR = "data/resonant_arcana/gear_existence/";
    private static final String ATWG_GEAR = "data/allthewizardgear/gear_existence/";

    /** Aeromancer pieces join the 5,000 school-armor weave. */
    private static final List<String> AEROMANCER_PIECES = List.of(
            "aeromancer_helmet", "aeromancer_chestplate", "aeromancer_leggings", "aeromancer_boots");

    /** wind_spell_book joins the school-book ladder at the 10,000 tier. */
    private static final String WIND_BOOK = "wind_spellbooks:wind_spell_book";

    /** The two staves join the 15,000 weapons and hand off to mithril_sword. */
    private static final List<String> ADDON_STAVES = List.of("wind_spellbooks:wind_staff", "leylines:ley_staff");

    /** Rare books joining the legendary books as 15,000 terminals. */
    private static final List<String> RARE_BOOKS = List.of(
            "leylines:leyline_codex", "resonant_arcana:deathchill_spell_book",
            "resonant_arcana:phoenix_spell_book", "resonant_arcana:runic_echoes_spell_book");

    /**
     * The AllTheWizardGear book ladder (D3): netherite_spell_book smiths into
     * allthemodium_spell_book (recipe base verified in the ATWG jar), which
     * climbs allthemodium -&gt; vibranium -&gt; unobtainium on the spine.
     */
    private static final List<String> ATWG_BOOK_TIERS = List.of("allthemodium", "vibranium", "unobtainium");
    private static final int ATWG_BOOK_TIER_2_GATE = 52000;
    private static final int ATWG_BOOK_TIER_3_GATE = 225000;

    /**
     * The AllTheWizardGear mage armor climbs its own smithing ladder as
     * standalone bands: the recipes base on the generic base_wizard_* tag
     * rather than any one school set, so no ISAS piece forks into them and
     * every school keeps its milestone spine intact.
     */
    private static final int ATWG_ARMOR_TIER_1_FLOOR = 225000;
    private static final int ATWG_ARMOR_TIER_2_GATE = 500000;
    private static final int ATWG_ARMOR_TIER_3_FLOOR = 1000000;

    /** netherite_spell_book's fork: the five legendary books plus the ATWG ladder's first rung. */
    private static final List<String> NETHERITE_FORK = List.of(ISAS + "evoker_spell_book",
            ISAS + "cursed_doll_spell_book", ISAS + "necronomicon_spell_book", ISAS + "dragonskin_spell_book",
            ISAS + "legendary_spell_book", "allthewizardgear:allthemodium_spell_book");

    // --- Gear the official integration skips (issue #4) ------------------------

    /**
     * The ISAS items with no official band that actually exist in
     * irons_spellbooks 3.16.3 (issue #4). The recovered plan's original list
     * named eight - four are phantom references kept alive only by orphan
     * model/lang entries and were verified absent from every class in the jar
     * (and, for the base book, from the live registry by the
     * {@code datapack_references_resolve} GameTest): "improved blood staff",
     * "illusioner spell book", {@code truthseeker}, and the base
     * {@code spell_book}.
     */
    private static final List<String> SKIPPED_WEAPONS = List.of(
            "staff_of_the_nines", "claymore", "cinderous_soulcaller");

    /** Every file the pack ships, grouped by the ladder it belongs to. */
    public record Content(
            Map<String, JsonObject> geGearExistence,
            Map<String, JsonObject> issGearExistence,
            Map<String, JsonObject> tensuraGearExistence,
            Map<String, JsonObject> addonGearExistence,
            Map<String, JsonObject> addonChains,
            Map<String, JsonObject> entityExistence,
            Map<String, JsonObject> structureMagicule,
            Map<String, JsonObject> safetyExempt) {
    }

    public static Content build() {
        Map<String, JsonObject> geGear = new LinkedHashMap<>();
        Map<String, JsonObject> issGear = new LinkedHashMap<>();
        Map<String, JsonObject> tensuraGear = new LinkedHashMap<>();
        Map<String, JsonObject> addonGear = new LinkedHashMap<>();
        Map<String, JsonObject> chains = new LinkedHashMap<>();
        Map<String, JsonObject> entities = new LinkedHashMap<>();
        Map<String, JsonObject> structures = new LinkedHashMap<>();
        Map<String, JsonObject> exempt = new LinkedHashMap<>();

        addSpellBooks(issGear, chains);
        addWeapons(issGear);
        addArmor(issGear);
        addSpecials(issGear);
        addSkippedGear(issGear);
        addMithrilRunners(tensuraGear);
        addAddonGear(addonGear, chains);
        addEntityBands(entities);
        addStructureData(structures, exempt);

        Content content = new Content(geGear, issGear, tensuraGear, addonGear, chains, entities,
                structures, exempt);
        validate(content);
        return content;
    }

    // --- Spell books ----------------------------------------------------------

    private static void addSpellBooks(Map<String, JsonObject> issGear, Map<String, JsonObject> chains) {
        // Fork books: inert companions whose EP the chain drives.
        forkBook(issGear, chains, "wimpy_spell_book", 2000, WIMPY_GATE, WIMPY_FORK);
        forkBook(issGear, chains, "copper_spell_book", 5000, COMMON_TIER_GATE, COMMON_FORK);
        forkBook(issGear, chains, "rotten_spell_book", 5000, COMMON_TIER_GATE, COMMON_FORK);
        forkBook(issGear, chains, "iron_spell_book", 6000, UNCOMMON_TIER_GATE, UNCOMMON_FORK);
        forkBook(issGear, chains, "blaze_spell_book", 6000, UNCOMMON_TIER_GATE, UNCOMMON_FORK);
        forkBook(issGear, chains, "diamond_spell_book", 8000, SCHOOL_TIER_GATE, SCHOOL_FORK);
        forkBook(issGear, chains, "ice_spell_book", 10000, LEGENDARY_GATE, LEGENDARY_FORK);
        forkBook(issGear, chains, "druidic_spell_book", 10000, LEGENDARY_GATE, LEGENDARY_FORK);
        forkBook(issGear, chains, "netherite_spell_book", 10000, LEGENDARY_GATE, NETHERITE_FORK);

        // The 7,000 books step to diamond by plain rung: no chain entry, so no
        // milestone strip - but the gate (8,000) sits below the first spine EP,
        // so no milestone could ever fire and none are carried (native rate key
        // is absent; omitted, codec default 0.03).
        for (String book : UNCOMMON_FORK) {
            put(issGear, ISS_GEAR + path(book) + ".json",
                    gear(book, 7000, DIAMOND_GATE, null, null, null, ISAS + "diamond_spell_book"));
        }

        // The five legendary books are the ladder's keep-earning top: plain
        // terminals carrying the full weapon spine.
        for (String book : LEGENDARY_FORK) {
            put(issGear, ISS_GEAR + path(book) + ".json",
                    gear(book, 15000, null, null, weaponSpine(15000, null), null, null));
        }
    }

    private static void forkBook(Map<String, JsonObject> issGear, Map<String, JsonObject> chains, String item,
            int floor, int gate, List<String> options) {
        put(issGear, ISS_GEAR + item + ".json", gear(ISAS + item, floor, 0, 0.0d, null, null, null));
        put(chains, CHAINS + item + ".json", chainOptions(ISAS + item, floor, gate, options));
    }

    // --- Weapons ---------------------------------------------------------------

    private static void addWeapons(Map<String, JsonObject> issGear) {
        // Magehunter: the anti-mage entry weapon and a plain terminal - it
        // climbs the full weapon spine and never hands off.
        put(issGear, ISS_GEAR + "magehunter.json",
                gear(ISAS + "magehunter", 6000, null, EP_GAIN_STATED, weaponSpine(6000, null), null, null));

        // The fifteen-thousand weapons climb to the mithril weapon gate and
        // hand off to tensura:mithril_sword. Kept milestones: 18k and 52k
        // (225k is the gate itself, 750k lies beyond it). epGain key absent
        // natively - omitted, codec default 0.03.
        for (String weapon : SPELL_WEAPONS) {
            put(issGear, ISS_GEAR + weapon + ".json",
                    gear(ISAS + weapon, 15000, MITHRIL_WEAPON_GATE, null,
                            weaponSpine(15000, MITHRIL_WEAPON_GATE), null, "tensura:mithril_sword"));
        }
    }

    // --- Armor ------------------------------------------------------------------

    private static void addArmor(Map<String, JsonObject> issGear) {
        // School sets: plain rungs climbing the armor spine to the mithril
        // armor gate, then handing off by slot. Kept milestones: 18k, 52k,
        // 225k (750k is the gate itself). Native epGain 0.02 written verbatim.
        for (String school : ARMOR_SCHOOLS) {
            for (String slot : SCHOOL_SLOTS) {
                armorRung(issGear, school + "_" + slot, "mithril_" + slot);
            }
        }
        // The wizard set is the only 5-piece set; both head pieces hand off to
        // the mithril helmet.
        armorRung(issGear, "wizard_hat", "mithril_helmet");
        armorRung(issGear, "wizard_helmet", "mithril_helmet");
        armorRung(issGear, "wizard_chestplate", "mithril_chestplate");
        armorRung(issGear, "wizard_leggings", "mithril_leggings");
        armorRung(issGear, "wizard_boots", "mithril_boots");
    }

    private static void armorRung(Map<String, JsonObject> issGear, String piece, String target) {
        put(issGear, ISS_GEAR + piece + ".json",
                gear(ISAS + piece, 5000, MITHRIL_ARMOR_GATE, EP_GAIN_STATED,
                        armorSpine(5000, MITHRIL_ARMOR_GATE), null, "tensura:" + target));
    }

    // --- Specials ----------------------------------------------------------------

    private static void addSpecials(Map<String, JsonObject> issGear) {
        // The specials stay at their native floors as terminals (doctrine: no
        // tier skips - their floors sit between the school sets' floor and
        // gate in the global ordering, so no ladder rung may cross them).
        put(issGear, ISS_GEAR + "paladin_chestplate.json",
                gear(ISAS + "paladin_chestplate", 15000, null, EP_GAIN_STATED,
                        armorSpine(15000, null), null, null));
        put(issGear, ISS_GEAR + "speed_boots.json",
                gear(ISAS + "speed_boots", 15000, null, EP_GAIN_STATED,
                        armorSpine(15000, null), null, null));
        // Floor 20,000 lies above the 18,000 spine EP, so those items never
        // carried (nor can carry) that bump - 52k/225k/750k only.
        put(issGear, ISS_GEAR + "infernal_sorcerer_chestplate.json",
                gear(ISAS + "infernal_sorcerer_chestplate", 20000, null, null,
                        armorSpine(20000, null), null, null));
        put(issGear, ISS_GEAR + "tarnished_helmet.json",
                gear(ISAS + "tarnished_helmet", 20000, null, null,
                        armorSpine(20000, null), null, null));
    }

    // --- Tensura-side receiving rungs --------------------------------------------

    /**
     * The five mithril rungs the hand-offs land on, byte-aligned with
     * tensura-iaf-growth's generated files (the sanctioned same-path copy -
     * see REPO.md's path-ownership gotcha). Floored at the hand-off EP so the
     * carry lands exactly on the floor instead of past base Tensura's mithril
     * gate and instant-re-evolving.
     */
    private static void addMithrilRunners(Map<String, JsonObject> tensuraGear) {
        put(tensuraGear, TENSURA_GEAR + "mithril_sword.json",
                mithril("tensura:mithril_sword", MITHRIL_WEAPON_GATE, 500000, 0.025d, "tensura:adamantite_sword"));
        for (String slot : SCHOOL_SLOTS) {
            put(tensuraGear, TENSURA_GEAR + "mithril_" + slot + ".json",
                    mithril("tensura:mithril_" + slot, MITHRIL_ARMOR_GATE, 1000000, EP_GAIN_DEFAULT,
                            "tensura:adamantite_" + slot));
        }
    }

    // --- Gear the official integration skips (issue #4) --------------------------

    /**
     * The four ISAS items with no official band (issue #4), placed by tier:
     * the frosted helve is a mid-tier weapon stepping into the ice staff at
     * the legendary gate; the three endgame pieces join the
     * fifteen-thousand weapons on the mithril hand-off.
     */
    private static void addSkippedGear(Map<String, JsonObject> issGear) {
        put(issGear, ISS_GEAR + "frosted_helve.json",
                gear(ISAS + "frosted_helve", SCHOOL_TIER_GATE, LEGENDARY_GATE, null,
                        weaponSpine(SCHOOL_TIER_GATE, LEGENDARY_GATE), null, ISAS + "ice_staff"));
        for (String weapon : SKIPPED_WEAPONS) {
            put(issGear, ISS_GEAR + weapon + ".json",
                    gear(ISAS + weapon, 15000, MITHRIL_WEAPON_GATE, null, weaponSpine(15000, MITHRIL_WEAPON_GATE),
                            null, "tensura:mithril_sword"));
        }
    }

    // --- Optional addon gear (issue #3) ----------------------------------------

    /**
     * Gear of the four optional ISAS addons (D4 placement in #1). The files
     * ship unconditionally: {@code gear_existence.type} is a plain
     * ResourceLocation, so files referencing an absent addon's items parse
     * fine and never match at runtime.
     */
    private static void addAddonGear(Map<String, JsonObject> addonGear, Map<String, JsonObject> chains) {
        // Wind Spellbooks: the aeromancer set joins the 5,000 school-armor
        // weave - same floor, rate, milestones and mithril hand-off.
        for (String piece : AEROMANCER_PIECES) {
            String slot = piece.substring("aeromancer_".length());
            put(addonGear, WIND_GEAR + piece + ".json",
                    gear("wind_spellbooks:" + piece, 5000, MITHRIL_ARMOR_GATE, EP_GAIN_STATED,
                            armorSpine(5000, MITHRIL_ARMOR_GATE), null, "tensura:mithril_" + slot));
        }

        // wind_spell_book joins the school-book ladder at the 10,000 tier:
        // inert companion plus the same fork to the five legendary books.
        put(addonGear, WIND_GEAR + "wind_spell_book.json", gear(WIND_BOOK, 10000, 0, 0.0d, null, null, null));
        put(chains, CHAINS + "wind_spell_book.json", chainOptions(WIND_BOOK, 10000, LEGENDARY_GATE, LEGENDARY_FORK));

        // The two staves join the 15,000 weapons and hand off to mithril.
        for (String staff : ADDON_STAVES) {
            put(addonGear, gearPathFor(staff),
                    gear(staff, 15000, MITHRIL_WEAPON_GATE, null, weaponSpine(15000, MITHRIL_WEAPON_GATE), null,
                            "tensura:mithril_sword"));
        }

        // The rare books join the legendary books as 15,000 terminals on the
        // full weapon spine.
        for (String book : RARE_BOOKS) {
            put(addonGear, gearPathFor(book), gear(book, 15000, null, null, weaponSpine(15000, null), null, null));
        }

        // AllTheWizardGear book ladder (D3): netherite_spell_book forks into
        // the first rung; the ladder climbs on the spine at the native-default
        // rate and terminates at unobtainium.
        int[] bookFloors = {LEGENDARY_GATE, ATWG_BOOK_TIER_2_GATE, ATWG_BOOK_TIER_3_GATE};
        int[] bookGates = {ATWG_BOOK_TIER_2_GATE, ATWG_BOOK_TIER_3_GATE, 0};
        for (int i = 0; i < ATWG_BOOK_TIERS.size(); i++) {
            String tier = ATWG_BOOK_TIERS.get(i);
            Integer gate = bookGates[i] == 0 ? null : bookGates[i];
            String evolution = gate == null ? null
                    : "allthewizardgear:" + ATWG_BOOK_TIERS.get(i + 1) + "_spell_book";
            put(addonGear, ATWG_GEAR + tier + "_spell_book.json",
                    gear("allthewizardgear:" + tier + "_spell_book", bookFloors[i], gate, null,
                            weaponSpine(bookFloors[i], gate), null, evolution));
        }

        // AllTheWizardGear mage armor: standalone smithing-entry bands (their
        // recipes base on the generic base_wizard_* tag, so no one school set
        // forks into them) climbing allthemodium -> vibranium -> unobtainium.
        for (String slot : SCHOOL_SLOTS) {
            put(addonGear, ATWG_GEAR + "allthemodium_mage_" + slot + ".json",
                    gear("allthewizardgear:allthemodium_mage_" + slot, ATWG_ARMOR_TIER_1_FLOOR,
                            ATWG_ARMOR_TIER_2_GATE, null, armorSpine(ATWG_ARMOR_TIER_1_FLOOR, ATWG_ARMOR_TIER_2_GATE),
                            null, "allthewizardgear:vibranium_mage_" + slot));
            put(addonGear, ATWG_GEAR + "vibranium_mage_" + slot + ".json",
                    gear("allthewizardgear:vibranium_mage_" + slot, ATWG_ARMOR_TIER_2_GATE,
                            ATWG_ARMOR_TIER_3_FLOOR, null,
                            armorSpine(ATWG_ARMOR_TIER_2_GATE, ATWG_ARMOR_TIER_3_FLOOR), null,
                            "allthewizardgear:unobtainium_mage_" + slot));
            put(addonGear, ATWG_GEAR + "unobtainium_mage_" + slot + ".json",
                    gear("allthewizardgear:unobtainium_mage_" + slot, ATWG_ARMOR_TIER_3_FLOOR, null, null,
                            armorSpine(ATWG_ARMOR_TIER_3_FLOOR, null), null, null));
        }
    }

    /** {@code data/<namespace>/gear_existence/<path>.json} for a full item id. */
    private static String gearPathFor(String fullId) {
        int colon = fullId.indexOf(':');
        return "data/" + fullId.substring(0, colon) + "/gear_existence/" + path(fullId) + ".json";
    }

    /** The path part of a full item id (file names carry no namespace). */
    private static String path(String fullId) {
        return fullId.substring(fullId.indexOf(':') + 1);
    }

    // --- Entity existence bands (issue #5) --------------------------------------

    /**
     * The one mob band this mod adds (D2 in #1): Wind Spellbooks' aeromancer,
     * banded at the official integration's wizard-mage tier (the cryomancer /
     * priest / pyromancer bands: aura 2,000-4,000, magicule 8,000-12,000,
     * spiritualHealth 400) with the one wind ability Tensura provides.
     * Leylines' nightborn_elf is worldgen/faction content, not a combat mob -
     * deliberately unbanded; the official 18 ISAS bands are left alone (D2).
     */
    private static void addEntityBands(Map<String, JsonObject> entities) {
        put(entities, "data/wind_spellbooks/entity_existence/aeromancer.json",
                entityBand("wind_spellbooks:aeromancer", 2000, 4000, 8000, 12000, 400,
                        List.of("tensura:wind_gust")));
    }

    private static JsonObject entityBand(String type, int minAura, int maxAura, int minMagicule, int maxMagicule,
            int spiritualHealth, List<String> abilities) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        JsonArray array = new JsonArray();
        abilities.forEach(array::add);
        json.add("abilities", array);
        json.addProperty("min_aura", minAura);
        json.addProperty("max_aura", maxAura);
        json.addProperty("min_magicule", minMagicule);
        json.addProperty("max_magicule", maxMagicule);
        json.addProperty("spiritualHealth", spiritualHealth);
        return json;
    }

    // --- Structure elevation + spawn exemptions (infinite-dungeon-spawn-safety) ---
    //
    // Pack rule: every ISAS-family structure's surrounding chunks carry at
    // least the magicule baseline of the mobs related to it. Elevations are
    // the residents' EP band maxima (NBT-proven palettes for the big
    // dungeons); wayward_compass_locator is a locator, not a structure, and
    // priest houses are vanilla village pieces with no structure id - both
    // out of scope.

    /** structure id -> [radius, elevation], id order, one row per physical structure. */
    private static final String[][] ISAS_STRUCTURE_ELEVATIONS = {
            {"irons_spellbooks:ancient_battleground", "192", "10000"},
            {"irons_spellbooks:catacombs", "256", "120000"},
            {"irons_spellbooks:citadel", "128", "10000"},
            {"irons_spellbooks:evoker_fort", "128", "10000"},
            {"irons_spellbooks:ice_spider_den", "64", "6000"},
            {"irons_spellbooks:impaled_icebreaker", "96", "6000"},
            {"irons_spellbooks:mangrove_hut", "64", "10000"},
            {"irons_spellbooks:mountain_tower", "96", "12000"},
            {"irons_spellbooks:pyromancer_tower", "96", "12000"},
    };

    /** Wind Spellbooks' structure ships under its own namespace (path ownership). */
    private static final String[][] WIND_STRUCTURE_ELEVATIONS = {
            {"wind_spellbooks:windmill", "64", "12000"},
    };

    /** The ISAS bosses this module must never gate: their baselines outstrip the world. */
    private static final String[] SPAWN_EXEMPT = {
            "irons_spellbooks:dead_king",
            "irons_spellbooks:fire_boss",
    };

    private static void addStructureData(Map<String, JsonObject> structures,
                                         Map<String, JsonObject> exempt) {
        put(structures, "data/irons_spellbooks/infinite_dungeon/structure_magicule/structures.json",
                structureFile(ISAS_STRUCTURE_ELEVATIONS));
        put(structures, "data/wind_spellbooks/infinite_dungeon/structure_magicule/structures.json",
                structureFile(WIND_STRUCTURE_ELEVATIONS));

        JsonArray entities = new JsonArray();
        for (String id : SPAWN_EXEMPT) {
            entities.add(id);
        }
        JsonObject exemptJson = new JsonObject();
        exemptJson.add("entities", entities);
        put(exempt, "data/irons_spellbooks/infinite_dungeon/safety/exempt.json", exemptJson);
    }

    private static JsonObject structureFile(String[][] rows) {
        JsonArray array = new JsonArray();
        for (String[] row : rows) {
            JsonObject entry = new JsonObject();
            entry.addProperty("structure", row[0]);
            entry.addProperty("radius", Double.parseDouble(row[1]));
            entry.addProperty("elevation", Double.parseDouble(row[2]));
            array.add(entry);
        }
        JsonObject json = new JsonObject();
        json.add("structures", array);
        return json;
    }

    // --- Milestone builders -------------------------------------------------------

    /** Weapon-spine bumps an item between {@code floor} and {@code gate} can actually fire. */
    private static JsonArray weaponSpine(int floor, Integer gate) {
        JsonArray milestones = new JsonArray();
        for (int ep : WEAPON_SPINE) {
            if (ep < floor || (gate != null && ep >= gate)) {
                continue;
            }
            JsonArray attributes = new JsonArray();
            attributes.add(attribute("minecraft:generic.attack_damage", WEAPON_BONUS.get(ep), "mainhand"));
            milestones.add(milestone(ep, attributes));
        }
        return milestones;
    }

    /** Armor-spine bumps an item between {@code floor} and {@code gate} can actually fire. */
    private static JsonArray armorSpine(int floor, Integer gate) {
        JsonArray milestones = new JsonArray();
        for (int ep : ARMOR_SPINE) {
            if (ep < floor || (gate != null && ep >= gate)) {
                continue;
            }
            JsonArray attributes = new JsonArray();
            double[] bonus = ARMOR_BONUS.get(ep);
            for (String slot : ARMOR_SLOTS) {
                attributes.add(attribute("minecraft:generic.armor", bonus[0], slot));
                attributes.add(attribute("minecraft:generic.armor_toughness", bonus[1], slot));
            }
            milestones.add(milestone(ep, attributes));
        }
        return milestones;
    }

    private static JsonObject milestone(int ep, JsonArray attributes) {
        JsonObject json = new JsonObject();
        json.addProperty("EP", ep);
        json.add("attributes", attributes);
        return json;
    }

    private static JsonObject attribute(String type, double amount, String slot) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("amount", amount);
        json.addProperty("slot", slot);
        return json;
    }

    // --- File builders ------------------------------------------------------------

    /**
     * A gear_existence file.
     *
     * @param maxEP     the EP at which the item evolves; {@code null} for a
     *                  terminal (the codec then applies its 2,000,000
     *                  ceiling); an explicit {@code 0} marks an inert file
     *                  whose EP the addon_chains ladder drives
     * @param epGain    the climb rate, or {@code null} to omit the key and
     *                  keep Tensura's codec default (0.03) untouched
     */
    private static JsonObject gear(String item, int minEP, Integer maxEP, Double epGain, JsonArray milestones,
            JsonObject engravings, String evolution) {
        JsonObject json = new JsonObject();
        json.addProperty("type", item);
        json.addProperty("minEP", minEP);
        if (maxEP != null) {
            json.addProperty("maxEP", maxEP);
        }
        if (epGain != null) {
            json.addProperty("epGain", number(epGain));
        }
        if (engravings != null) {
            json.add("engravings", engravings);
        }
        if (evolution != null) {
            json.addProperty("evolution", evolution);
        }
        if (milestones != null && !milestones.isEmpty()) {
            json.add("uniqueEvolutions", milestones);
        }
        return json;
    }

    /** A fork book's chain: even sibling roll, explicit default rate (GE requires the key). */
    private static JsonObject chainOptions(String item, int minEP, int maxEP, List<String> options) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        json.addProperty("minEP", minEP);
        json.addProperty("maxEP", maxEP);
        json.addProperty("epGain", number(EP_GAIN_DEFAULT));
        JsonArray array = new JsonArray();
        options.forEach(array::add);
        json.add("evolutionOptions", array);
        return json;
    }

    private static JsonObject mithril(String item, int minEP, int maxEP, double epGain, String evolution) {
        JsonObject engravings = new JsonObject();
        engravings.addProperty("tensura:holy_coat", 2);
        return gear(item, minEP, maxEP, epGain, null, engravings, evolution);
    }

    /** Writes {@code 0} as an integer rather than {@code 0.0}, as the iaf pack does. */
    private static Number number(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return (int) value;
        }
        return value;
    }

    private static void put(Map<String, JsonObject> files, String path, JsonObject json) {
        if (files.put(path, json) != null) {
            throw new IllegalStateException("duplicate datapack path " + path);
        }
    }

    /** The item namespaces this mod owns: Iron's Spells plus the four optional addons. */
    private static boolean ownedNamespace(String fullId) {
        if (fullId.startsWith(ISAS)) {
            return true;
        }
        return List.of("wind_spellbooks:", "leylines:", "resonant_arcana:", "allthewizardgear:")
                .stream().anyMatch(fullId::startsWith);
    }

    // --- Validation ---------------------------------------------------------------

    private static void validate(Content content) {
        List<String> errors = new ArrayList<>();

        expect(errors, "gear_existence files (every official file, plus the four the official pack skips)", 91,
                content.issGearExistence().size());
        expect(errors, "inert companions", 9, count(content.issGearExistence(), json ->
                json.has("maxEP") && json.get("maxEP").getAsInt() == 0));
        expect(errors, "plain rungs with evolution", 72, count(content.issGearExistence(), json ->
                json.has("maxEP") && json.get("maxEP").getAsInt() > 0 && json.has("evolution")));
        expect(errors, "plain terminals", 10, count(content.issGearExistence(), json -> !json.has("maxEP")));
        expect(errors, "addon gear files (all four optional addons)", 26, content.addonGearExistence().size());
        expect(errors, "addon_chains files", 10, content.addonChains().size());
        expect(errors, "tensura receiving rungs", 5, content.tensuraGearExistence().size());
        expect(errors, "gear_existence files under the GE namespace (no ISS builtins to neutralise)", 0,
                content.geGearExistence().size());
        expect(errors, "structure elevation files", 2, content.structureMagicule().size());
        expect(errors, "spawn exemption files", 1, content.safetyExempt().size());
        expect(errors, "total files", 136, content.geGearExistence().size() + content.issGearExistence().size()
                + content.tensuraGearExistence().size() + content.addonGearExistence().size()
                + content.addonChains().size() + content.entityExistence().size()
                + content.structureMagicule().size() + content.safetyExempt().size());
        expect(errors, "entity existence bands", 1, content.entityExistence().size());

        // The pack rule's headline rows: the Dead King's catacombs floor at his
        // own band maximum, the windmill at the aeromancer's.
        expectStructureRow(errors, content,
                "data/irons_spellbooks/infinite_dungeon/structure_magicule/structures.json",
                "irons_spellbooks:catacombs", 256.0, 120000.0);
        expectStructureRow(errors, content,
                "data/wind_spellbooks/infinite_dungeon/structure_magicule/structures.json",
                "wind_spellbooks:windmill", 64.0, 12000.0);
        expect(errors, "ISAS structure elevation rows", ISAS_STRUCTURE_ELEVATIONS.length,
                structureRowCount(content, "data/irons_spellbooks/infinite_dungeon/structure_magicule/structures.json"));
        if (!exemptContains(content, "irons_spellbooks:dead_king")
                || !exemptContains(content, "irons_spellbooks:fire_boss")) {
            errors.add("the spawn exemptions must cover the Dead King and the fire boss");
        }

        for (Map.Entry<String, JsonObject> entry : content.entityExistence().entrySet()) {
            String label = entry.getKey();
            JsonObject json = entry.getValue();
            checkFileName(errors, label);
            String namespace = label.split("/")[1];
            if (!json.get("type").getAsString().startsWith(namespace + ":")) {
                errors.add(label + ": type is outside its namespace " + namespace);
            }
            int minAura = json.get("min_aura").getAsInt();
            int maxAura = json.get("max_aura").getAsInt();
            int minMagicule = json.get("min_magicule").getAsInt();
            int maxMagicule = json.get("max_magicule").getAsInt();
            if (minAura > maxAura || minMagicule > maxMagicule) {
                errors.add(label + ": inverted band");
            }
            if (json.get("spiritualHealth").getAsInt() <= 0) {
                errors.add(label + ": spiritualHealth must be positive");
            }
            for (var ability : json.getAsJsonArray("abilities")) {
                if (!ability.getAsString().startsWith("tensura:")) {
                    errors.add(label + ": ability " + ability.getAsString() + " is not a tensura id");
                }
            }
        }

        checkPrefix(errors, content.issGearExistence(), ISS_GEAR, "irons_spellbooks gear_existence");
        checkPrefix(errors, content.addonChains(), CHAINS, "addon_chains");
        checkPrefix(errors, content.tensuraGearExistence(), TENSURA_GEAR, "tensura gear_existence");
        for (String path : content.addonGearExistence().keySet()) {
            boolean owned = List.of(WIND_GEAR, LEY_GEAR, RESONANT_GEAR, ATWG_GEAR).stream().anyMatch(path::startsWith);
            if (!owned) {
                errors.add("addon gear_existence: " + path + " is outside the four addon roots");
            }
        }

        for (Map.Entry<String, JsonObject> entry : content.issGearExistence().entrySet()) {
            checkFileName(errors, entry.getKey());
            checkGearEntry(errors, entry.getKey(), entry.getValue(), ISAS);
        }
        for (Map.Entry<String, JsonObject> entry : content.addonGearExistence().entrySet()) {
            checkFileName(errors, entry.getKey());
            String namespace = entry.getKey().split("/")[1];
            checkGearEntry(errors, entry.getKey(), entry.getValue(), namespace + ":");
        }

        for (Map.Entry<String, JsonObject> entry : content.addonChains().entrySet()) {
            String label = entry.getKey();
            JsonObject json = entry.getValue();
            checkFileName(errors, label);
            String item = json.get("item").getAsString();
            if (!ownedNamespace(item)) {
                errors.add(label + ": item " + item + " is outside the namespaces this mod owns");
            }
            boolean hasEvolution = json.has("evolution");
            boolean hasOptions = json.has("evolutionOptions");
            if (hasEvolution == hasOptions) {
                errors.add(label + ": chain needs exactly one of evolution or evolutionOptions");
            }
            if (json.get("maxEP").getAsInt() <= json.get("minEP").getAsInt()) {
                errors.add(label + ": chain gate must exceed its floor");
            }
            if (Math.abs(json.get("epGain").getAsDouble() - EP_GAIN_DEFAULT) > EP_GAIN_TOLERANCE) {
                errors.add(label + ": chain epGain must be the codec default " + EP_GAIN_DEFAULT);
            }
            if (hasOptions) {
                for (var option : json.getAsJsonArray("evolutionOptions")) {
                    String target = option.isJsonObject()
                            ? option.getAsJsonObject().get("id").getAsString()
                            : option.getAsString();
                    // Bare paths land in the minecraft namespace when GE parses
                    // them and get filtered as absent - forks would silently
                    // flatten to terminals. Full ids or nothing.
                    if (!ownedNamespace(target) && !target.startsWith("tensura:")) {
                        errors.add(label + ": fork target " + target
                                + " is not a full id in an owned or tensura namespace");
                    }
                    boolean known = content.issGearExistence().containsKey(gearPathFor(target))
                            || content.addonGearExistence().containsKey(gearPathFor(target));
                    if (!known) {
                        errors.add(label + ": fork target " + target + " has no override file");
                    }
                }
            }
        }

        for (Map.Entry<String, JsonObject> entry : content.tensuraGearExistence().entrySet()) {
            String label = entry.getKey();
            JsonObject json = entry.getValue();
            checkFileName(errors, label);
            if (!json.get("type").getAsString().startsWith("tensura:")) {
                errors.add(label + ": receiving rung is not a tensura id");
            }
            if (!json.has("engravings") || !json.has("evolution")) {
                errors.add(label + ": receiving rung must keep its engravings and evolution");
            }
        }

        // Every evolution target must resolve to a file this pack ships.
        Map<String, JsonObject> allGear = new LinkedHashMap<>(content.issGearExistence());
        allGear.putAll(content.addonGearExistence());
        for (Map.Entry<String, JsonObject> entry : allGear.entrySet()) {
            if (!entry.getValue().has("evolution")) {
                continue;
            }
            String target = entry.getValue().get("evolution").getAsString();
            boolean known = content.tensuraGearExistence().containsKey(gearPathFor(target))
                    || allGear.containsKey(gearPathFor(target));
            if (!known) {
                errors.add(entry.getKey() + ": evolution target " + target + " is not generated by this pack");
            }
        }

        checkAscending(errors, "spell-book ladder",
                List.of(WIMPY_GATE, COMMON_TIER_GATE, UNCOMMON_TIER_GATE, DIAMOND_GATE, SCHOOL_TIER_GATE,
                        LEGENDARY_GATE));
        checkAscending(errors, "mithril hand-offs", List.of(LEGENDARY_GATE, MITHRIL_WEAPON_GATE, MITHRIL_ARMOR_GATE));
        if (!WEAPON_SPINE.contains(MITHRIL_WEAPON_GATE)) {
            errors.add("mithril weapon gate is not on the official weapon spine");
        }
        if (!ARMOR_SPINE.contains(MITHRIL_ARMOR_GATE)) {
            errors.add("mithril armor gate is not on the official armor spine");
        }

        spotCheckInert(errors, content, ISS_GEAR, "wimpy_spell_book", 2000);
        spotCheckFork(errors, content, "wimpy_spell_book", 2000, WIMPY_GATE, WIMPY_FORK);
        spotCheckFork(errors, content, "diamond_spell_book", 8000, SCHOOL_TIER_GATE, SCHOOL_FORK);
        spotCheckFork(errors, content, "ice_spell_book", 10000, LEGENDARY_GATE, LEGENDARY_FORK);
        spotCheckFork(errors, content, "netherite_spell_book", 10000, LEGENDARY_GATE, NETHERITE_FORK);
        spotCheckRung(errors, content, ISS_GEAR, "villager_spell_book", 7000, DIAMOND_GATE,
                ISAS + "diamond_spell_book", List.of(), null);
        spotCheckTerminal(errors, content, ISS_GEAR, "evoker_spell_book", 15000, WEAPON_SPINE, null);
        spotCheckRung(errors, content, ISS_GEAR, "amethyst_rapier", 15000, MITHRIL_WEAPON_GATE,
                "tensura:mithril_sword", List.of(18000, 52000), null);
        spotCheckRung(errors, content, ISS_GEAR, "archevoker_helmet", 5000, MITHRIL_ARMOR_GATE,
                "tensura:mithril_helmet", List.of(18000, 52000, 225000), EP_GAIN_STATED);
        spotCheckRung(errors, content, ISS_GEAR, "wizard_hat", 5000, MITHRIL_ARMOR_GATE,
                "tensura:mithril_helmet", List.of(18000, 52000, 225000), EP_GAIN_STATED);
        spotCheckTerminal(errors, content, ISS_GEAR, "paladin_chestplate", 15000, ARMOR_SPINE, EP_GAIN_STATED);
        spotCheckTerminal(errors, content, ISS_GEAR, "infernal_sorcerer_chestplate", 20000,
                List.of(52000, 225000, 750000), null);
        spotCheckTerminal(errors, content, ISS_GEAR, "tarnished_helmet", 20000, List.of(52000, 225000, 750000),
                null);
        spotCheckRung(errors, content, ISS_GEAR, "magehunter", 6000, null, null, WEAPON_SPINE, EP_GAIN_STATED);

        // Skipped-gear archetypes (issue #4).
        spotCheckRung(errors, content, ISS_GEAR, "frosted_helve", SCHOOL_TIER_GATE, LEGENDARY_GATE,
                ISAS + "ice_staff", List.of(), null);
        spotCheckRung(errors, content, ISS_GEAR, "staff_of_the_nines", 15000, MITHRIL_WEAPON_GATE,
                "tensura:mithril_sword", List.of(18000, 52000), null);

        // Addon archetypes (issue #3).
        spotCheckRung(errors, content, WIND_GEAR, "aeromancer_helmet", 5000, MITHRIL_ARMOR_GATE,
                "tensura:mithril_helmet", List.of(18000, 52000, 225000), EP_GAIN_STATED);
        spotCheckInert(errors, content, WIND_GEAR, "wind_spell_book", 10000);
        spotCheckRung(errors, content, WIND_GEAR, "wind_staff", 15000, MITHRIL_WEAPON_GATE, "tensura:mithril_sword",
                List.of(18000, 52000), null);
        spotCheckRung(errors, content, LEY_GEAR, "ley_staff", 15000, MITHRIL_WEAPON_GATE, "tensura:mithril_sword",
                List.of(18000, 52000), null);
        spotCheckTerminal(errors, content, LEY_GEAR, "leyline_codex", 15000, WEAPON_SPINE, null);
        spotCheckTerminal(errors, content, RESONANT_GEAR, "deathchill_spell_book", 15000, WEAPON_SPINE, null);
        spotCheckRung(errors, content, ATWG_GEAR, "allthemodium_spell_book", 15000, ATWG_BOOK_TIER_2_GATE,
                "allthewizardgear:vibranium_spell_book", List.of(18000), null);
        spotCheckRung(errors, content, ATWG_GEAR, "vibranium_spell_book", ATWG_BOOK_TIER_2_GATE,
                ATWG_BOOK_TIER_3_GATE, "allthewizardgear:unobtainium_spell_book", List.of(52000), null);
        spotCheckTerminal(errors, content, ATWG_GEAR, "unobtainium_spell_book", ATWG_BOOK_TIER_3_GATE,
                List.of(225000, 750000), null);
        spotCheckRung(errors, content, ATWG_GEAR, "allthemodium_mage_helmet", ATWG_ARMOR_TIER_1_FLOOR,
                ATWG_ARMOR_TIER_2_GATE, "allthewizardgear:vibranium_mage_helmet", List.of(225000), null);
        spotCheckRung(errors, content, ATWG_GEAR, "vibranium_mage_helmet", ATWG_ARMOR_TIER_2_GATE,
                ATWG_ARMOR_TIER_3_FLOOR, "allthewizardgear:unobtainium_mage_helmet", List.of(750000), null);
        spotCheckTerminal(errors, content, ATWG_GEAR, "unobtainium_mage_helmet", ATWG_ARMOR_TIER_3_FLOOR,
                List.of(), null);

        // Entity band (issue #5).
        JsonObject aeromancer = content.entityExistence().get("data/wind_spellbooks/entity_existence/aeromancer.json");
        if (aeromancer == null) {
            errors.add("aeromancer entity band missing");
        } else {
            expectInt(errors, "aeromancer", 2000, aeromancer, "min_aura");
            expectInt(errors, "aeromancer", 4000, aeromancer, "max_aura");
            expectInt(errors, "aeromancer", 8000, aeromancer, "min_magicule");
            expectInt(errors, "aeromancer", 12000, aeromancer, "max_magicule");
            expectInt(errors, "aeromancer", 400, aeromancer, "spiritualHealth");
            List<String> abilities = new ArrayList<>();
            aeromancer.getAsJsonArray("abilities").forEach(ability -> abilities.add(ability.getAsString()));
            if (!abilities.equals(List.of("tensura:wind_gust"))) {
                errors.add("aeromancer abilities are " + abilities);
            }
        }

        // The receiving rungs must stay byte-aligned with tensura-iaf-growth.
        expectJson(errors, "mithril_sword.json", content, TENSURA_GEAR + "mithril_sword.json", """
                {"type":"tensura:mithril_sword","minEP":225000,"maxEP":500000,"epGain":0.025,
                 "engravings":{"tensura:holy_coat":2},"evolution":"tensura:adamantite_sword"}""");
        for (String slot : SCHOOL_SLOTS) {
            // Substituted rather than formatted: both placeholders always carry the
            // same slot, and the template's line feed must stay a literal - the
            // generated files are byte-aligned with tensura-iaf-growth's LF output
            // on every platform, so %n (platform newline) would break the copy.
            expectJson(errors, "mithril_" + slot + ".json", content, TENSURA_GEAR + "mithril_" + slot + ".json", """
                    {"type":"tensura:mithril_%s","minEP":750000,"maxEP":1000000,"epGain":0.03,
                     "engravings":{"tensura:holy_coat":2},"evolution":"tensura:adamantite_%s"}"""
                    .replace("%s", slot));
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Rebalanced datapack failed validation:\n  " + String.join("\n  ", errors));
        }
    }

    // --- Validation helpers --------------------------------------------------------

    @FunctionalInterface
    private interface FilePredicate {
        boolean test(JsonObject json);
    }

    private static int count(Map<String, JsonObject> files, FilePredicate predicate) {
        return (int) files.values().stream().filter(predicate::test).count();
    }

    /** The number of rows inside one generated structures file. */
    private static int structureRowCount(Content content, String path) {
        JsonObject json = content.structureMagicule().get(path);
        return json == null || !json.has("structures") ? 0 : json.getAsJsonArray("structures").size();
    }

    /** Whether the generated exempt file lists the entity id. */
    private static boolean exemptContains(Content content, String entityId) {
        JsonObject json = content.safetyExempt()
                .get("data/irons_spellbooks/infinite_dungeon/safety/exempt.json");
        if (json == null || !json.has("entities")) {
            return false;
        }
        for (var element : json.getAsJsonArray("entities")) {
            if (entityId.equals(element.getAsString())) {
                return true;
            }
        }
        return false;
    }

    /** Pins one structure row: floor and radius must match the pack rule's table. */
    private static void expectStructureRow(List<String> errors, Content content, String path,
                                           String structure, double radius, double elevation) {
        JsonObject json = content.structureMagicule().get(path);
        if (json == null || !json.has("structures")) {
            errors.add(path + ": structure elevation file missing");
            return;
        }
        for (var element : json.getAsJsonArray("structures")) {
            JsonObject row = element.getAsJsonObject();
            if (structure.equals(row.get("structure").getAsString())) {
                if (row.get("radius").getAsDouble() != radius
                        || row.get("elevation").getAsDouble() != elevation) {
                    errors.add(structure + ": expected radius " + radius + " elevation " + elevation
                            + ", found radius " + row.get("radius").getAsDouble()
                            + " elevation " + row.get("elevation").getAsDouble());
                }
                return;
            }
        }
        errors.add(structure + ": no structure elevation row");
    }

    private static void expect(List<String> errors, String what, int wanted, int found) {
        if (wanted != found) {
            errors.add(what + ": expected " + wanted + " but found " + found);
        }
    }

    private static void checkPrefix(List<String> errors, Map<String, JsonObject> files, String prefix, String ladder) {
        for (String path : files.keySet()) {
            if (!path.startsWith(prefix)) {
                errors.add(ladder + ": " + path + " is outside " + prefix);
            }
        }
    }

    private static void checkFileName(List<String> errors, String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (!name.matches("[a-z0-9_]+\\.json")) {
            errors.add("file name " + name + " is not [a-z0-9_]+.json");
        }
    }

    private static void checkMilestones(List<String> errors, String label, JsonObject json, Integer gate) {
        int floor = json.get("minEP").getAsInt();
        for (var milestone : json.getAsJsonArray("uniqueEvolutions")) {
            int ep = milestone.getAsJsonObject().get("EP").getAsInt();
            if (ep < floor) {
                errors.add(label + ": milestone " + ep + " lies below the floor " + floor);
            }
            if (gate != null && ep >= gate) {
                errors.add(label + ": milestone " + ep + " can never fire before the gate " + gate);
            }
        }
    }

    private static void checkAscending(List<String> errors, String ladder, List<Integer> gates) {
        for (int i = 1; i < gates.size(); i++) {
            if (gates.get(i) <= gates.get(i - 1)) {
                errors.add(ladder + ": gates do not strictly ascend: " + gates);
                return;
            }
        }
    }

    private static JsonObject file(List<String> errors, Content content, String mapPath, String path) {
        Map<String, JsonObject> map;
        if (mapPath.startsWith(CHAINS)) {
            map = content.addonChains();
        } else if (mapPath.startsWith(TENSURA_GEAR)) {
            map = content.tensuraGearExistence();
        } else if (mapPath.startsWith(ISS_GEAR)) {
            map = content.issGearExistence();
        } else {
            map = content.addonGearExistence();
        }
        JsonObject json = map.get(path);
        if (json == null) {
            errors.add(path + ": expected but not generated");
        }
        return json;
    }

    /** Shared per-file checks for a gear_existence entry (ISS overrides and addon bands alike). */
    private static void checkGearEntry(List<String> errors, String label, JsonObject json, String namespace) {
        String type = json.has("type") ? json.get("type").getAsString() : "";
        if (!type.startsWith(namespace)) {
            errors.add(label + ": type " + type + " is not a " + namespace + " id");
        }
        int maxEP = json.has("maxEP") ? json.get("maxEP").getAsInt() : -1;
        boolean hasEvolution = json.has("evolution");
        if (maxEP < 0 && hasEvolution) {
            errors.add(label + ": terminal names an evolution");
        }
        if (maxEP == 0) {
            if (hasEvolution) {
                errors.add(label + ": inert file names an evolution");
            }
            if (json.has("uniqueEvolutions")) {
                errors.add(label + ": inert file carries milestones GE would strip");
            }
            if (!json.has("epGain") || json.get("epGain").getAsDouble() != 0.0d) {
                errors.add(label + ": inert file must state epGain 0");
            }
        }
        if (json.has("uniqueEvolutions")) {
            checkMilestones(errors, label, json, maxEP > 0 ? maxEP : null);
        }
    }

    private static void spotCheckInert(List<String> errors, Content content, String root, String item, int floor) {
        JsonObject json = file(errors, content, root, root + item + ".json");
        if (json == null) {
            return;
        }
        expectInt(errors, item, floor, json, "minEP");
        expectInt(errors, item, 0, json, "maxEP");
        expectInt(errors, item, 0, json, "epGain");
        if (json.has("evolution") || json.has("uniqueEvolutions")) {
            errors.add(item + ": inert companion must name no target and carry no milestones");
        }
    }

    private static void spotCheckFork(List<String> errors, Content content, String item, int floor, int gate,
            List<String> options) {
        JsonObject json = file(errors, content, CHAINS, CHAINS + item + ".json");
        if (json == null) {
            return;
        }
        expectInt(errors, item + " chain", floor, json, "minEP");
        expectInt(errors, item + " chain", gate, json, "maxEP");
        expectInt(errors, item + " chain", (int) EP_GAIN_DEFAULT, json, "epGain");
        List<String> found = new ArrayList<>();
        json.getAsJsonArray("evolutionOptions").forEach(option -> found.add(option.getAsString()));
        if (!found.equals(options)) {
            errors.add(item + " chain fork is " + found + " but the plan forks to " + options);
        }
    }

    private static void spotCheckRung(List<String> errors, Content content, String root, String item, int floor,
            Integer gate, String target, List<Integer> milestoneEps, Double epGain) {
        JsonObject json = file(errors, content, root, root + item + ".json");
        if (json == null) {
            return;
        }
        expectInt(errors, item, floor, json, "minEP");
        if (gate == null) {
            if (json.has("maxEP")) {
                errors.add(item + ": expected a terminal but carries maxEP");
            }
        } else {
            expectInt(errors, item, gate, json, "maxEP");
        }
        if (target == null) {
            if (json.has("evolution")) {
                errors.add(item + ": expected no target but names " + json.get("evolution").getAsString());
            }
        } else if (!json.has("evolution") || !target.equals(json.get("evolution").getAsString())) {
            errors.add(item + ": expected evolution " + target);
        }
        if (epGain == null) {
            if (json.has("epGain")) {
                errors.add(item + ": native epGain is absent - the key must be omitted, found "
                        + json.get("epGain").getAsDouble());
            }
        } else if (!json.has("epGain") || json.get("epGain").getAsDouble() != epGain) {
            errors.add(item + ": expected epGain " + epGain);
        }
        expectMilestones(errors, item, json, milestoneEps);
    }

    private static void spotCheckTerminal(List<String> errors, Content content, String root, String item, int floor,
            List<Integer> milestoneEps, Double epGain) {
        spotCheckRung(errors, content, root, item, floor, null, null, milestoneEps, epGain);
    }

    private static void expectMilestones(List<String> errors, String item, JsonObject json, List<Integer> eps) {
        List<Integer> found = new ArrayList<>();
        if (json.has("uniqueEvolutions")) {
            json.getAsJsonArray("uniqueEvolutions")
                    .forEach(milestone -> found.add(milestone.getAsJsonObject().get("EP").getAsInt()));
        }
        if (!found.equals(eps)) {
            errors.add(item + ": milestones are " + found + " but the plan keeps " + eps);
        }
    }

    private static void expectInt(List<String> errors, String item, int wanted, JsonObject json, String key) {
        if (!json.has(key) || json.get(key).getAsInt() != wanted) {
            errors.add(item + ": expected " + key + " " + wanted + " but found "
                    + (json.has(key) ? json.get(key).getAsString() : "(absent)"));
        }
    }

    private static void expectJson(List<String> errors, String label, Content content, String path, String literal) {
        JsonObject actual = content.tensuraGearExistence().get(path);
        if (actual == null) {
            errors.add(path + ": expected but not generated");
            return;
        }
        JsonObject expected = JsonParser.parseString(literal).getAsJsonObject();
        if (!expected.equals(actual)) {
            errors.add(path + " drifted from the tensura-iaf-growth copy: " + actual);
        }
    }
}
