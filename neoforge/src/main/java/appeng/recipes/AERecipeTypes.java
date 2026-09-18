package appeng.recipes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

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

    public static final RecipeType<TransformRecipe> TRANSFORM = register(TransformRecipe.TYPE_ID, TransformRecipe.TYPE);
    public static final RecipeType<EntropyRecipe> ENTROPY = register(EntropyRecipe.TYPE_ID, EntropyRecipe.TYPE);
    public static final RecipeType<InscriberRecipe> INSCRIBER = register(InscriberRecipe.TYPE_ID, InscriberRecipe.TYPE);
    public static final RecipeType<ChargerRecipe> CHARGER = register(ChargerRecipe.TYPE_ID, ChargerRecipe.TYPE);
    public static final RecipeType<MatterCannonAmmo> MATTER_CANNON_AMMO = register(MatterCannonAmmo.TYPE_ID,
            MatterCannonAmmo.TYPE);
    public static final RecipeType<QuartzCuttingRecipe> QUARTZ_CUTTING = register(QuartzCuttingRecipe.TYPE_ID,
            QuartzCuttingRecipe.TYPE);
    public static final RecipeType<CraftingUnitTransformRecipe> CRAFTING_UNIT_TRANSFORM = register(
            CraftingUnitTransformRecipe.TYPE_ID, CraftingUnitTransformRecipe.TYPE);
    public static final RecipeType<StorageCellDisassemblyRecipe> CELL_DISASSEMBLY = register(
            StorageCellDisassemblyRecipe.TYPE_ID, StorageCellDisassemblyRecipe.TYPE);

    /**
     * Records a type the recipe class already created.
     * <p>
     * The types are declared on the recipe classes rather than here so that they do not have to name this one -- that
     * was a cycle, and it kept the whole package on the NeoForge side of the build. This is still where anything
     * outside the package should look one up.
     */
    private static <T extends Recipe<?>> RecipeType<T> register(Identifier id, RecipeType<T> type) {
        ALL.put(id, type);
        return type;
    }
}
