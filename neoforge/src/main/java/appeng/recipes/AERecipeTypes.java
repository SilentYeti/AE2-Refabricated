package appeng.recipes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.api.ids.AEConstants;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import appeng.recipes.transform.TransformRecipe;

public final class AERecipeTypes {
    private AERecipeTypes() {
    }

    /**
     * Every type declared here, in declaration order, for whichever loader is registering them.
     * <p>
     * A plain table rather than a {@code DeferredRegister}, because that is NeoForge's and this class has to be
     * reachable from {@code :common} -- the recipe classes name these constants as their type, so it cannot sit on the
     * far side of the loader boundary from them.
     */
    private static final Map<Identifier, RecipeType<?>> ALL = new LinkedHashMap<>();

    public static Map<Identifier, RecipeType<?>> all() {
        return Collections.unmodifiableMap(ALL);
    }

    public static final RecipeType<TransformRecipe> TRANSFORM = register("transform");
    public static final RecipeType<EntropyRecipe> ENTROPY = register("entropy");
    public static final RecipeType<InscriberRecipe> INSCRIBER = register("inscriber");
    public static final RecipeType<ChargerRecipe> CHARGER = register("charger");
    public static final RecipeType<MatterCannonAmmo> MATTER_CANNON_AMMO = register("matter_cannon");
    public static final RecipeType<QuartzCuttingRecipe> QUARTZ_CUTTING = register("quartz_cutting");
    public static final RecipeType<CraftingUnitTransformRecipe> CRAFTING_UNIT_TRANSFORM = register(
            "crafting_unit_transform");
    public static final RecipeType<StorageCellDisassemblyRecipe> CELL_DISASSEMBLY = register(
            "storage_cell_disassembly");

    private static <T extends Recipe<?>> RecipeType<T> register(String id) {
        var key = AEConstants.makeId(id);
        RecipeType<T> type = RecipeType.simple(key);
        ALL.put(key, type);
        return type;
    }
}
