package appeng.api.stacks;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;

/**
 * Manages the registry used to synchronize key spaces to the client.
 */
@ApiStatus.Internal
public final class AEKeyTypesInternal {
    @Nullable
    private static Registry<AEKeyType> registry;

    @Nullable
    private static Set<AEKeyType> allTypes;

    private static int cachedSize = -1;

    private AEKeyTypesInternal() {
    }

    public static Registry<AEKeyType> getRegistry() {
        Preconditions.checkState(registry != null, "AE2 isn't initialized yet.");
        return registry;
    }

    public static void setRegistry(Registry<AEKeyType> registry) {
        Preconditions.checkState(AEKeyTypesInternal.registry == null);
        AEKeyTypesInternal.registry = registry;
    }

    /**
     * The set is cached because callers iterate it on hot paths, and rebuilt whenever the registry has grown since it
     * was built.
     * <p>
     * This used to hang off NeoForge's {@code BakeCallback}, which has no Fabric counterpart. Comparing the size is
     * both loader-agnostic and a little stronger: it picks up a key type registered after the registry was baked, which
     * the callback would have missed.
     */
    public static Set<AEKeyType> getAllTypes() {
        var registry = getRegistry();
        if (allTypes == null || cachedSize != registry.size()) {
            var types = new HashSet<AEKeyType>();
            for (var aeKeyType : registry) {
                types.add(aeKeyType);
            }
            allTypes = Set.copyOf(types);
            cachedSize = registry.size();
        }
        return allTypes;
    }

    public static void register(AEKeyType keyType) {
        Registry.register(getRegistry(), keyType.getId(), keyType);
        // Registering never shrinks the registry, but drop the cache anyway so a replaced entry is not served stale
        allTypes = null;
    }
}
