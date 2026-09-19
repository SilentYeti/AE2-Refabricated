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

package appeng.fabric.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appeng.core.config.ConfigBackend;
import appeng.core.config.ConfigBuilder;
import appeng.core.config.ConfigFile;
import appeng.core.config.ConfigOption;
import appeng.core.config.ConfigType;

/**
 * {@link ConfigBackend} for Fabric, which has no config system of its own: each file is JSON in the config directory,
 * {@code ae2-client.json} and {@code ae2-common.json}, with one object per section.
 * <p>
 * It reads a file the way NeoForge corrects one, so the same hand-edit means the same thing on either loader: a number
 * outside its range is clamped to the range; a value of the wrong kind, or missing, becomes the default; an enum is
 * matched by name ignoring case. Anything it had to correct -- including a key the file did not have yet -- is written
 * back, so the file always lists every value. A file that is not JSON at all is kept aside as {@code .bak} and replaced
 * with the defaults, rather than overwritten.
 * <p>
 * Not carried over: the comments. JSON has none, so the explanations live only in the NeoForge files and the source.
 * <p>
 * The client file exists only on the physical client, as on NeoForge; on a dedicated server its values stay at their
 * defaults and nothing is written.
 */
public final class FabricConfigBackend implements ConfigBackend {
    private static final Logger LOG = LoggerFactory.getLogger(FabricConfigBackend.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configDir;
    private final boolean physicalClient;

    public FabricConfigBackend(Path configDir, boolean physicalClient) {
        this.configDir = configDir;
        this.physicalClient = physicalClient;
    }

    @Override
    public ConfigBuilder newBuilder() {
        return new Builder();
    }

    @Override
    public ConfigFile register(ConfigType type, ConfigBuilder builder, Runnable onLoad) {
        var options = ((Builder) builder).options;
        if (type == ConfigType.CLIENT && !physicalClient) {
            onLoad.run();
            return () -> {
            };
        }

        var file = configDir.resolve("ae2-" + type.name().toLowerCase(Locale.ROOT) + ".json");
        var loaded = new File(file, options);
        loaded.load();
        onLoad.run();
        return loaded::save;
    }

    // --- one file ---

    private record File(Path path, List<Option<?>> options) {
        void load() {
            JsonObject root = null;
            if (Files.exists(path)) {
                try {
                    var parsed = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
                    if (parsed.isJsonObject()) {
                        root = parsed.getAsJsonObject();
                    } else {
                        throw new IllegalStateException("not a JSON object");
                    }
                } catch (Exception e) {
                    var backup = path.resolveSibling(path.getFileName() + ".bak");
                    LOG.warn("AE2 config {} could not be read ({}); keeping it as {} and using the defaults", path,
                            e.getMessage(), backup.getFileName());
                    try {
                        Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException moveFailed) {
                        LOG.error("Could not move the unreadable config {} aside", path, moveFailed);
                        return; // leave the player's file alone rather than overwrite it
                    }
                }
            }

            boolean corrected = root == null;
            for (var option : options) {
                corrected |= option.load(root == null ? null : find(root, option.path));
            }
            if (corrected) {
                save();
            }
        }

        void save() {
            var root = new JsonObject();
            for (var option : options) {
                var parent = root;
                for (var section : option.path.subList(0, option.path.size() - 1)) {
                    if (!parent.has(section) || !parent.get(section).isJsonObject()) {
                        parent.add(section, new JsonObject());
                    }
                    parent = parent.getAsJsonObject(section);
                }
                parent.add(option.path.getLast(), option.write());
            }
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOG.error("Could not write AE2 config {}", path, e);
            }
        }

        @Nullable
        private static JsonElement find(JsonObject root, List<String> path) {
            JsonElement current = root;
            for (var part : path) {
                if (current == null || !current.isJsonObject()) {
                    return null;
                }
                current = current.getAsJsonObject().get(part);
            }
            return current;
        }
    }

    // --- declaring ---

    private static final class Builder implements ConfigBuilder {
        private final List<String> sections = new ArrayList<>();
        private final List<Option<?>> options = new ArrayList<>();

        @Override
        public ConfigBuilder push(String section) {
            sections.add(section);
            return this;
        }

        @Override
        public ConfigBuilder pop() {
            sections.removeLast();
            return this;
        }

        @Override
        public ConfigBuilder comment(String comment) {
            return this; // JSON has nowhere to put it
        }

        @Override
        public ConfigOption<Boolean> define(String name, boolean defaultValue) {
            return add(new Option<>(path(name), defaultValue) {
                @Override
                @Nullable
                Boolean read(JsonElement json) {
                    return json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean() ? json.getAsBoolean() : null;
                }

                @Override
                JsonElement write() {
                    return new JsonPrimitive(get());
                }
            });
        }

        @Override
        public ConfigOption<Integer> defineInRange(String name, int defaultValue, int min, int max) {
            return add(new Option<>(path(name), defaultValue) {
                @Override
                @Nullable
                Integer read(JsonElement json) {
                    if (!isNumber(json)) {
                        return null;
                    }
                    // As NeoForge does: clamp as a number, then read it as an int
                    double value = json.getAsDouble();
                    return value < min ? min : value > max ? max : (int) value;
                }

                @Override
                JsonElement write() {
                    return new JsonPrimitive(get());
                }
            });
        }

        @Override
        public ConfigOption<Double> defineInRange(String name, double defaultValue, double min, double max) {
            return add(new Option<>(path(name), defaultValue) {
                @Override
                @Nullable
                Double read(JsonElement json) {
                    if (!isNumber(json)) {
                        return null;
                    }
                    double value = json.getAsDouble();
                    return value < min ? min : value > max ? max : value;
                }

                @Override
                JsonElement write() {
                    return new JsonPrimitive(get());
                }
            });
        }

        @Override
        public <E extends Enum<E>> ConfigOption<E> defineEnum(String name, E defaultValue) {
            var constants = defaultValue.getDeclaringClass().getEnumConstants();
            return add(new Option<>(path(name), defaultValue) {
                @Override
                @Nullable
                E read(JsonElement json) {
                    if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
                        return null;
                    }
                    var text = json.getAsString();
                    for (var constant : constants) {
                        if (constant.name().equalsIgnoreCase(text)) {
                            return constant;
                        }
                    }
                    return null;
                }

                @Override
                JsonElement write() {
                    return new JsonPrimitive(get().name());
                }
            });
        }

        private List<String> path(String name) {
            var path = new ArrayList<>(sections);
            path.add(name);
            return List.copyOf(path);
        }

        private <T> ConfigOption<T> add(Option<T> option) {
            options.add(option);
            return option;
        }

        private static boolean isNumber(JsonElement json) {
            return json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber();
        }
    }

    // --- one value ---

    private abstract static class Option<T> implements ConfigOption<T> {
        final List<String> path;
        private final T defaultValue;
        private volatile T value;

        Option(List<String> path, T defaultValue) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        /** The value in the file, corrected, or null if it cannot be used at all. */
        @Nullable
        abstract T read(JsonElement json);

        abstract JsonElement write();

        /** Takes the value from the file; answers whether the file needs rewriting to match. */
        boolean load(@Nullable JsonElement json) {
            var read = json == null ? null : read(json);
            value = read != null ? read : defaultValue;
            return read == null || !write().equals(json);
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T newValue) {
            value = newValue;
        }
    }
}
