package de.ellpeck.naturesaura.compat.patchouli;

import de.ellpeck.naturesaura.recipes.OfferingRecipe;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class ProcessorOffering implements IComponentProcessor {

    private OfferingRecipe recipe;

    @Override
    public void setup(IVariableProvider provider) {
        this.recipe = PatchouliCompat.getRecipe("offering", provider.get("recipe").asString());
    }

    @Override
    public IVariable process(String key) {
        if (this.recipe == null)
            return null;
        return switch (key) {
            case "input" -> PatchouliCompat.ingredientVariable(this.recipe.input);
            case "output" -> IVariable.from(this.recipe.output);
            case "start" -> PatchouliCompat.ingredientVariable(this.recipe.startItem);
            case "name" -> IVariable.wrap(this.recipe.output.getHoverName().getString());
            default -> null;
        };
    }
}
