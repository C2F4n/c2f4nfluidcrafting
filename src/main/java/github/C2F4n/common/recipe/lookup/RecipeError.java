package github.C2F4n.common.recipe.lookup;

/** 机器为何不在合成的统一原因模型（后续可用于 GUI 警告）。 */
public enum RecipeError {
    WORKING_DISABLED,
    NO_MATCHING_RECIPE,
    NOT_ENOUGH_INPUT,
    OUTPUT_NOT_SPACE;

    public String getTranslationKey() {
        return switch (this) {
            case WORKING_DISABLED -> "gui.c2f4nfluidcrafting.recipe_error.working_disabled";
            case NO_MATCHING_RECIPE -> "gui.c2f4nfluidcrafting.recipe_error.no_matching_recipe";
            case NOT_ENOUGH_INPUT -> "gui.c2f4nfluidcrafting.recipe_error.not_enough_input";
            case OUTPUT_NOT_SPACE -> "gui.c2f4nfluidcrafting.recipe_error.output_not_space";
        };
    }
}
