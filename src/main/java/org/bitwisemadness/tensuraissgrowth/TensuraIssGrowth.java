package org.bitwisemadness.tensuraissgrowth;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.bitwisemadness.tensuraissgrowth.datagen.JsonFilesProvider;
import org.bitwisemadness.tensuraissgrowth.datagen.RebalanceContent;
import org.slf4j.Logger;

/**
 * Mod entry point.
 *
 * <p>Re-weaves Iron's Spells and Spellbooks gear into Tensura's EP ladder and
 * Gear Evolution chains, replacing the official {@code tensura_iron_spells}
 * integration's flat EP bands with progression chains.</p>
 */
@Mod(TensuraIssGrowth.MOD_ID)
public final class TensuraIssGrowth {

    public static final String MOD_ID = "tensura_iss_growth";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TensuraIssGrowth(IEventBus modBus) {
        modBus.addListener(TensuraIssGrowth::gatherData);
        LOGGER.info("Tensura - Iron's Spells Growth initialized");
    }

    /** Feeds the rebalanced datapack ({@link RebalanceContent}) into datagen. */
    private static void gatherData(GatherDataEvent event) {
        if (!event.includeServer()) {
            return;
        }
        RebalanceContent.Content content = RebalanceContent.build();
        var output = event.getGenerator().getPackOutput();
        event.addProvider(new JsonFilesProvider("gear_existence", output, content.geGearExistence()));
        event.addProvider(new JsonFilesProvider("addon_gear_existence", output, content.issGearExistence()));
        event.addProvider(new JsonFilesProvider("tensura_gear_existence", output, content.tensuraGearExistence()));
        event.addProvider(new JsonFilesProvider("optional_addon_gear_existence", output, content.addonGearExistence()));
        event.addProvider(new JsonFilesProvider("addon_chains", output, content.addonChains()));
        event.addProvider(new JsonFilesProvider("entity_existence", output, content.entityExistence()));
        event.addProvider(new JsonFilesProvider("structure_magicule", output, content.structureMagicule()));
        event.addProvider(new JsonFilesProvider("safety_exempt", output, content.safetyExempt()));
    }
}
