package org.bitwisemadness.tensuraissgrowth.datagen;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for the rebalanced datapack this mod ships, mirroring
 * tensura-iaf-growth's datagen doctrine: the Java tables below generate every
 * file, and {@link #validate(Content)} fails the build on any drift, skip or
 * path collision before a single byte is written.
 *
 * <p>The ladders (issue #2) fill the four maps:
 * <ul>
 *   <li>{@code geGearExistence} - files at
 *       {@code data/gearevolution/gear_existence/irons_spellbooks/**}, where
 *       Gear Evolution's own ISS catalog (if any) must be neutralised;</li>
 *   <li>{@code issGearExistence} - files at
 *       {@code data/irons_spellbooks/gear_existence/**}, the same-path
 *       overrides of the official {@code tensura_iron_spells} integration's
 *       flat bands;</li>
 *   <li>{@code tensuraGearExistence} - files at
 *       {@code data/tensura/gear_existence/**}, where ISS gear steps onto
 *       Tensura rungs;</li>
 *   <li>{@code addonChains} - files at
 *       {@code data/gearevolution/addon_chains/irons_spellbooks/**}, the GE
 *       chain ladders and fork points.</li>
 * </ul>
 */
public final class RebalanceContent {

    private RebalanceContent() {
    }

    /** Every file the pack ships, grouped by the ladder it belongs to. */
    public record Content(
            Map<String, JsonObject> geGearExistence,
            Map<String, JsonObject> issGearExistence,
            Map<String, JsonObject> tensuraGearExistence,
            Map<String, JsonObject> addonChains) {
    }

    /**
     * Builds all maps and validates them. The ladders are not written yet -
     * this scaffolding exists so runData, the jar packaging and CI all run the
     * real pipeline (build -&gt; validate -&gt; write) from day one; the
     * ladder commits fill the tables and the validation rules together.
     */
    public static Content build() {
        Content content = new Content(new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), new LinkedHashMap<>());
        validate(content);
        return content;
    }

    /** Fails the build on structural drift; extended alongside each ladder. */
    private static void validate(Content content) {
        // No rules yet: the maps are empty until the ladders land in #2.
    }
}
