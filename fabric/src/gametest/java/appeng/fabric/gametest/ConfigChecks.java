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

package appeng.fabric.gametest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

import appeng.api.config.PowerUnit;
import appeng.api.config.TerminalStyle;
import appeng.core.AEConfig;
import appeng.core.config.ConfigBuilder;
import appeng.core.config.ConfigOption;
import appeng.core.config.ConfigType;
import appeng.fabric.config.FabricConfigBackend;

/**
 * AE2's config on Fabric: that it registered and reads as its defaults, and that the JSON backend corrects a file the
 * way NeoForge corrects its TOML. The NeoForge side is pinned key for key by {@code AEConfigSpecTest}.
 */
final class ConfigChecks {
    private ConfigChecks() {
    }

    static int run() throws IOException {
        aeConfigIsRegisteredWithItsDefaults();
        theCommonFileIsWrittenWithEveryValue();
        var dir = Files.createTempDirectory("ae2-config-check");
        try {
            aFileIsCorrectedTheWayNeoForgeCorrectsOne(dir.resolve("corrected"));
            anUnreadableFileIsKeptAsideNotOverwritten(dir.resolve("unreadable"));
            aChangedValueSurvivesASaveAndAReload(dir.resolve("saved"));
            theClientFileDoesNotExistOnADedicatedServer(dir.resolve("server"));
        } finally {
            try (var files = Files.walk(dir)) {
                files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
        return 6;
    }

    private static void aeConfigIsRegisteredWithItsDefaults() {
        var config = AEConfig.instance();
        check(config != null, "AEConfig should be registered by the Fabric entrypoint");
        check(config.getChargedStaffBattery().getAsDouble() == 8000, "chargedStaff battery should default to 8000");
        check(config.getFormationPlaneEntityLimit() == 128, "formationPlaneEntityLimit should default to 128");
        check(config.getSelectedEnergyUnit() == PowerUnit.AE, "the client's power unit should default to AE");
    }

    private static void theCommonFileIsWrittenWithEveryValue() throws IOException {
        var file = FabricLoader.getInstance().getConfigDir().resolve("ae2-common.json");
        check(Files.exists(file), file + " should have been written");
        var root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        check(root.getAsJsonObject("battery").get("chargedStaff").getAsInt() == 8000,
                "ae2-common.json should hold battery.chargedStaff = 8000");
        check(root.getAsJsonObject("tickRates").has("InterfaceMin"),
                "ae2-common.json should hold every tick rate, as NeoForge's file does");
    }

    private static void aFileIsCorrectedTheWayNeoForgeCorrectsOne(Path dir) throws IOException {
        Files.createDirectories(dir);
        var file = dir.resolve("ae2-common.json");
        Files.writeString(file, """
                {"s": {"i": 500, "d": "not a number", "e": "tall", "b": true}}
                """, StandardCharsets.UTF_8);

        var backend = new FabricConfigBackend(dir, true);
        var declared = new Declared(backend.newBuilder());
        var loads = new int[1];
        backend.register(ConfigType.COMMON, declared.builder, () -> loads[0]++);

        check(loads[0] == 1, "onLoad should run once, got " + loads[0]);
        check(declared.i.get() == 32, "500 is above the range [1, 32] and should clamp to 32, got " + declared.i.get());
        check(declared.d.get() == 2.5, "a value of the wrong kind should become the default, got " + declared.d.get());
        check(declared.e.get() == TerminalStyle.TALL, "enum names should match ignoring case");
        check(declared.b.get(), "a valid value should be read as it is");
        check(declared.missing.get(), "a missing value should be its default");

        var rewritten = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject()
                .getAsJsonObject("s");
        check(rewritten.get("i").getAsInt() == 32 && rewritten.get("d").getAsDouble() == 2.5
                && rewritten.get("e").getAsString().equals("TALL") && rewritten.has("missing"),
                "the corrected file should be written back with every value; got " + rewritten);
    }

    private static void anUnreadableFileIsKeptAsideNotOverwritten(Path dir) throws IOException {
        Files.createDirectories(dir);
        var file = dir.resolve("ae2-common.json");
        Files.writeString(file, "{ this is not json", StandardCharsets.UTF_8);

        var backend = new FabricConfigBackend(dir, true);
        var declared = new Declared(backend.newBuilder());
        backend.register(ConfigType.COMMON, declared.builder, () -> {
        });

        var backup = dir.resolve("ae2-common.json.bak");
        check(Files.exists(backup) && Files.readString(backup).equals("{ this is not json"),
                "the unreadable file should be kept as .bak, unchanged");
        check(declared.i.get() == 5, "values should be their defaults");
        check(Files.exists(file), "a fresh file with the defaults should be written");
    }

    private static void aChangedValueSurvivesASaveAndAReload(Path dir) throws IOException {
        Files.createDirectories(dir);
        var backend = new FabricConfigBackend(dir, true);
        var declared = new Declared(backend.newBuilder());
        var file = backend.register(ConfigType.CLIENT, declared.builder, () -> {
        });
        declared.i.set(7);
        declared.e.set(TerminalStyle.FULL);
        file.save();

        var fresh = new FabricConfigBackend(dir, true);
        var reloaded = new Declared(fresh.newBuilder());
        fresh.register(ConfigType.CLIENT, reloaded.builder, () -> {
        });
        check(reloaded.i.get() == 7 && reloaded.e.get() == TerminalStyle.FULL,
                "saved values should be read back; got " + reloaded.i.get() + ", " + reloaded.e.get());
    }

    private static void theClientFileDoesNotExistOnADedicatedServer(Path dir) throws IOException {
        Files.createDirectories(dir);
        var backend = new FabricConfigBackend(dir, false);
        var declared = new Declared(backend.newBuilder());
        var loads = new int[1];
        backend.register(ConfigType.CLIENT, declared.builder, () -> loads[0]++);

        check(loads[0] == 1, "onLoad should still run");
        check(!Files.exists(dir.resolve("ae2-client.json")), "no client file should be written on a server");
        check(declared.i.get() == 5, "client values should be their defaults on a server");
    }

    /** A small declaration with one value of each kind, in a section. */
    private static final class Declared {
        final ConfigBuilder builder;
        final ConfigOption<Integer> i;
        final ConfigOption<Double> d;
        final ConfigOption<TerminalStyle> e;
        final ConfigOption<Boolean> b;
        final ConfigOption<Boolean> missing;

        Declared(ConfigBuilder builder) {
            this.builder = builder;
            builder.push("s");
            i = builder.defineInRange("i", 5, 1, 32);
            d = builder.defineInRange("d", 2.5, 0.0, 10.0);
            e = builder.defineEnum("e", TerminalStyle.SMALL);
            b = builder.define("b", false);
            missing = builder.define("missing", true);
            builder.pop();
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("AE2 config check failed: " + message);
        }
    }
}
