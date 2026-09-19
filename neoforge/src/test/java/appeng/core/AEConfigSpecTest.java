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

package appeng.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import org.junit.jupiter.api.Test;

import net.neoforged.neoforge.common.ModConfigSpec;

import appeng.core.config.ConfigBuilder;
import appeng.neoforge.config.NeoForgeConfigBuilder;
import appeng.util.BootstrapMinecraft;

/**
 * Pins the exact shape of AE2's NeoForge config files: every section and key, its type, default, range and comment.
 * <p>
 * The configs are the one place a refactor can break something that lives outside the jar. A key renamed, moved to
 * another section or given a different range does not fail anything at runtime -- NeoForge quietly resets the user's
 * value to the default and rewrites their file. So the whole spec is compared against {@code ae2-config-spec.txt},
 * which was written from the config as it stood before it was made loader-agnostic.
 * <p>
 * When a change to the config is intended, the failure message names where the new dump was written: review it, and
 * copy it over {@code src/test/resources/appeng/core/ae2-config-spec.txt} in the same commit.
 */
@BootstrapMinecraft
class AEConfigSpecTest {
    private static final String EXPECTED = "/appeng/core/ae2-config-spec.txt";

    @Test
    void theConfigFilesKeepTheirExactShape() throws Exception {
        var actual = new StringBuilder();
        for (var entry : specs().entrySet()) {
            actual.append("### ").append(entry.getKey()).append('\n');
            dump(entry.getValue(), entry.getValue().getSpec(), new ArrayList<>(), actual);
        }
        // Tests run in the run directory, not the project, so say exactly where the dump went
        var dumped = Path.of("ae2-config-spec.actual.txt").toAbsolutePath();
        Files.writeString(dumped, actual, StandardCharsets.UTF_8);

        try (var expected = AEConfigSpecTest.class.getResourceAsStream(EXPECTED)) {
            assertThat(expected).as("%s is missing from the test resources; the dump is in %s", EXPECTED, dumped)
                    .isNotNull();
            assertThat(actual.toString())
                    .as("the config's shape changed; the new dump is in %s", dumped)
                    .isEqualTo(new String(expected.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n"));
        }
    }

    /**
     * The two specs AE2 registers, by config type. The only part of this test that knows how AE2 builds them.
     */
    private static Map<String, ModConfigSpec> specs() throws Exception {
        var specs = new LinkedHashMap<String, ModConfigSpec>();
        specs.put("client", specOf("appeng.core.AEConfig$ClientConfig"));
        specs.put("common", specOf("appeng.core.AEConfig$CommonConfig"));
        return specs;
    }

    /**
     * Declares one of AE2's config holders against the NeoForge backend's builder and returns what it built -- the same
     * path {@code AEConfig.register} takes, minus the mod container.
     */
    private static ModConfigSpec specOf(String holderClass) throws Exception {
        var clazz = Class.forName(holderClass);
        var constructor = clazz.getDeclaredConstructor(ConfigBuilder.class);
        constructor.setAccessible(true);
        var builder = new NeoForgeConfigBuilder();
        constructor.newInstance(builder);
        return builder.build();
    }

    private static void dump(ModConfigSpec spec, UnmodifiableConfig level, List<String> path, StringBuilder out)
            throws IOException {
        var keys = new ArrayList<>(level.valueMap().keySet());
        keys.sort(null);
        for (var key : keys) {
            var childPath = new ArrayList<>(path);
            childPath.add(key);
            var value = level.valueMap().get(key);
            var name = String.join(".", childPath);
            if (value instanceof UnmodifiableConfig section) {
                out.append("[").append(name).append("]");
                var comment = spec.getLevelComment(childPath);
                if (comment != null) {
                    out.append(" # ").append(comment.replace("\n", "\\n"));
                }
                out.append('\n');
                dump(spec, section, childPath, out);
            } else if (value instanceof ModConfigSpec.ValueSpec valueSpec) {
                out.append(name)
                        .append(" : ").append(valueSpec.getClazz().getSimpleName())
                        .append(" = ").append(valueSpec.getDefault());
                if (valueSpec.getClazz().isEnum()) {
                    // Which constants the file may hold -- defineEnum validates rather than ranges
                    var accepted = new ArrayList<String>();
                    for (var constant : valueSpec.getClazz().getEnumConstants()) {
                        if (valueSpec.test(constant)) {
                            accepted.add(constant.toString());
                        }
                    }
                    out.append(" one of ").append(accepted);
                }
                var range = valueSpec.getRange();
                if (range != null) {
                    out.append(" in [").append(range.getMin()).append(", ").append(range.getMax()).append("]");
                }
                if (valueSpec.getComment() != null) {
                    out.append(" # ").append(valueSpec.getComment().replace("\n", "\\n"));
                }
                out.append('\n');
            } else {
                out.append(name).append(" : unexpected ").append(value).append('\n');
            }
        }
    }
}
