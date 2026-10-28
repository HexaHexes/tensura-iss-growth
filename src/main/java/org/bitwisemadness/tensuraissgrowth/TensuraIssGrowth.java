package org.bitwisemadness.tensuraissgrowth;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
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
        LOGGER.info("Tensura - Iron's Spells Growth initialized");
    }
}
