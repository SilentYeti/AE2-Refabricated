/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import appeng.util.BootstrapMinecraft;

/**
 * The block counterpart of {@code AECommonItemsTest}, and there for the same reason: {@link AECommonBlocks} is a second
 * declaration of blocks {@link AEBlocks} already declares, so the two will drift unless something stops them.
 * <p>
 * It fails if a block is added to {@code AEBlocks} and to neither {@code AECommonBlocks.entries()} nor
 * {@code AECommonBlocks.notYetPortable()}, so no one can add a block and silently leave Fabric behind.
 */
@BootstrapMinecraft
class AECommonBlocksTest {

    @Test
    void everyAEBlockIsEitherPortedOrExplainedAsNotPortable() {
        var ported = portedIds();
        var explained = AECommonBlocks.notYetPortable();

        var unaccounted = new LinkedHashSet<String>();
        declaredInAEBlocks().forEach((fieldName, id) -> {
            if (!ported.contains(id) && !explained.containsKey(fieldName)) {
                unaccounted.add("AEBlocks." + fieldName + " (" + id + ")");
            }
        });

        assertThat(unaccounted)
                .as("every AEBlocks entry must be in AECommonBlocks.entries() or explained in notYetPortable()")
                .isEmpty();
    }

    @Test
    void notYetPortableDoesNotListPortedOrUnknownBlocks() {
        var ported = portedIds();
        var declared = declaredInAEBlocks();

        var wrong = new LinkedHashMap<String, String>();
        AECommonBlocks.notYetPortable().forEach((fieldName, reason) -> {
            var id = declared.get(fieldName);
            if (id == null) {
                wrong.put(fieldName, "is not a field of AEBlocks");
            } else if (ported.contains(id)) {
                wrong.put(fieldName, "is already registered by AECommonBlocks, so the entry is stale");
            }
        });

        assertThat(wrong)
                .as("notYetPortable() must describe blocks that exist and are genuinely not ported")
                .isEmpty();
    }

    @Test
    void portedBlocksKeepTheNamesAndOrderOfAEBlocks() {
        var declared = declaredInAEBlocks();
        var byId = declared.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a));

        for (var entry : AECommonBlocks.entries()) {
            assertThat(byId)
                    .as("AECommonBlocks declares %s, which AEBlocks does not", entry.id())
                    .containsKey(entry.id());
        }

        // The stair, slab and wall variants copy a base block's state, so a variant declared before the
        // block it is cut from would resolve against a block that is not registered yet.
        var seen = new LinkedHashSet<Identifier>();
        for (var entry : AECommonBlocks.entries()) {
            seen.add(entry.id());
        }
        assertThat(seen).as("AECommonBlocks must not declare the same block twice")
                .hasSameSizeAs(AECommonBlocks.entries());
    }

    private static Set<Identifier> portedIds() {
        return AECommonBlocks.entries().stream()
                .map(AECommonBlocks.Entry::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** AEBlocks' public BlockDefinition fields, by field name. */
    private static Map<String, Identifier> declaredInAEBlocks() {
        var out = new LinkedHashMap<String, Identifier>();
        for (var field : AEBlocks.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !BlockDefinition.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                var definition = (BlockDefinition<?>) field.get(null);
                out.put(field.getName(), definition.id());
            } catch (IllegalAccessException e) {
                throw new AssertionError("could not read AEBlocks." + field.getName(), e);
            }
        }
        return out;
    }
}
