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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import appeng.api.util.AEColor;
import appeng.core.MainCreativeTab;
import appeng.util.BootstrapMinecraft;

/**
 * {@link AECommonItems} is a second declaration of items {@link AEItems} already declares. It exists only because
 * {@code :fabric} cannot run {@code AEItems} yet, and it should be deleted once it can. Two declarations of the same
 * thing drift, so this test is what stops them.
 * <p>
 * It fails if an item is added to {@code AEItems} and to neither {@code AECommonItems.entries()} nor
 * {@code AECommonItems.notYetPortable()}, so no one can add an item and silently leave Fabric behind. It also pins the
 * English names that drive the generated translations, the creative tab placement, and the vanilla tab keys
 * {@code AECommonItems} has to spell out by hand because vanilla keeps its own constants private.
 */
@BootstrapMinecraft
class AECommonItemsTest {

    @Test
    void everyAEItemIsEitherPortedOrExplainedAsNotPortable() {
        var ported = portedIds();
        var explained = AECommonItems.notYetPortable();

        var unaccounted = new LinkedHashSet<String>();
        declaredInAEItems().forEach((fieldName, ids) -> {
            for (var id : ids) {
                if (!ported.contains(id) && !explained.containsKey(fieldName)) {
                    unaccounted.add("AEItems." + fieldName + " (" + id + ")");
                }
            }
        });

        assertThat(unaccounted)
                .as("every AEItems entry must be in AECommonItems.entries() or explained in notYetPortable()")
                .isEmpty();
    }

    @Test
    void notYetPortableDoesNotListPortedOrUnknownItems() {
        var ported = portedIds();
        var declared = declaredInAEItems();

        for (var fieldName : AECommonItems.notYetPortable().keySet()) {
            var ids = declared.get(fieldName);
            assertThat(ids)
                    .as("notYetPortable() names AEItems." + fieldName + ", which is not an item definition")
                    .isNotNull();
            assertThat(ported)
                    .as("AEItems." + fieldName + " is ported, so it should not be in notYetPortable()")
                    .doesNotContainAnyElementsOf(ids);
        }
    }

    @Test
    void portedEntriesMatchAEItems() {
        var byId = new HashMap<Identifier, ItemDefinition<?>>();
        for (var definition : AEItems.getItems()) {
            byId.put(definition.id(), definition);
        }

        for (var entry : AECommonItems.entries()) {
            var definition = byId.get(entry.id());
            assertThat(definition).as("AEItems has no item with id " + entry.id()).isNotNull();
            assertThat(entry.englishName())
                    .as("English name for " + entry.id())
                    .isEqualTo(definition.getEnglishName());
        }
    }

    @Test
    void externalTabPlacementMatchesAEItems() {
        var expected = externalTabsFromMainCreativeTab();

        for (var entry : AECommonItems.entries()) {
            assertThat(entry.externalTab())
                    .as("external creative tab for " + entry.id())
                    .isEqualTo(expected.get(entry.id()));
        }
    }

    @Test
    void handWrittenVanillaTabKeysMatchVanilla() {
        assertThat(AECommonItems.TOOLS_AND_UTILITIES).isEqualTo(CreativeModeTabs.TOOLS_AND_UTILITIES);
        assertThat(AECommonItems.COMBAT).isEqualTo(CreativeModeTabs.COMBAT);
        assertThat(AECommonItems.INGREDIENTS).isEqualTo(CreativeModeTabs.INGREDIENTS);
    }

    private static Set<Identifier> portedIds() {
        return AECommonItems.entries().stream().map(AECommonItems.Entry::id).collect(Collectors.toSet());
    }

    /**
     * The ids each {@link AEItems} field declares, keyed by field name. Deliberately reflective over the fields rather
     * than reading {@code AEItems.getItems()}: that list also collects every part item registered through
     * {@code AEParts}, and parts are a separate porting problem from items.
     */
    private static Map<String, List<Identifier>> declaredInAEItems() {
        var result = new LinkedHashMap<String, List<Identifier>>();
        for (var field : AEItems.class.getDeclaredFields()) {
            // AEItems also has a private ITEMS list, which reflection may not read
            if (!ItemDefinition.class.equals(field.getType())
                    && !ColoredItemDefinition.class.equals(field.getType())) {
                continue;
            }
            try {
                var value = field.get(null);
                if (value instanceof ItemDefinition<?> definition) {
                    result.put(field.getName(), List.of(definition.id()));
                } else if (value instanceof ColoredItemDefinition<?> colored) {
                    var ids = new ArrayList<Identifier>();
                    for (var color : AEColor.values()) {
                        var id = colored.id(color);
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                    result.put(field.getName(), List.copyOf(ids));
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError("Cannot read AEItems." + field.getName(), e);
            }
        }
        return result;
    }

    /**
     * {@link MainCreativeTab} is the only record of which vanilla tab an item was put into, and it keeps that in a
     * private field, so read it rather than duplicating the mapping into this test.
     */
    @SuppressWarnings("unchecked")
    private static Map<Identifier, ResourceKey<CreativeModeTab>> externalTabsFromMainCreativeTab() {
        try {
            var field = MainCreativeTab.class.getDeclaredField("externalItemDefs");
            field.setAccessible(true);
            var multimap = (Multimap<ResourceKey<CreativeModeTab>, ItemDefinition<?>>) field.get(null);

            var result = new HashMap<Identifier, ResourceKey<CreativeModeTab>>();
            for (var mapEntry : multimap.entries()) {
                result.put(mapEntry.getValue().id(), mapEntry.getKey());
            }
            return result;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("MainCreativeTab.externalItemDefs is gone; update this test", e);
        }
    }
}
