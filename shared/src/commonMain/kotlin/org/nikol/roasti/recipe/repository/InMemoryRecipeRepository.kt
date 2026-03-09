package org.nikol.roasti.recipe.repository

import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.BrewStep
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RoastLevel

private const val MockRecipeImageUrl = "https://picsum.photos/200"

class InMemoryRecipeRepository : RecipeRepository {

    private val recipes = listOf(
        Recipe(
            id = "1",
            title = "Classic Pour Over",
            description = "A bright and floral pour over with notes of blueberry and citrus.",
            imageUrl = MockRecipeImageUrl,
            brewMethod = BrewMethod.V60,
            difficulty = Difficulty.Medium,
            totalBrewTimeSeconds = 240,
            roastLevel = RoastLevel.MediumLight,
            beans = "Ethiopian Yirgacheffe",
            steps = listOf(
                BrewStep(1, "Prepare Equipment", "Place filter in V60 and rinse with hot water. Discard rinse water.", 30),
                BrewStep(2, "Add Coffee", "Add 20g of medium-fine ground coffee to the filter.", 15),
                BrewStep(3, "Bloom", "Pour 40g of water (200°F) in a circular motion. Let bloom for 30 seconds.", 30),
                BrewStep(4, "First Pour", "Pour water up to 120g in a slow circular motion.", 45),
                BrewStep(5, "Second Pour", "Pour water up to 220g. Maintain steady pace.", 45),
                BrewStep(6, "Final Pour", "Pour remaining water to reach 320g total. Let drain completely.", 90),
                BrewStep(7, "Enjoy", "Remove V60, swirl carafe gently, and pour into your favorite cup.", null),
            ),
        ),
        Recipe(
            id = "2",
            title = "French Press Bold",
            description = "Rich and full-bodied coffee with chocolate and nutty undertones.",
            imageUrl = MockRecipeImageUrl,
            brewMethod = BrewMethod.FrenchPress,
            difficulty = Difficulty.Easy,
            totalBrewTimeSeconds = 300,
            roastLevel = RoastLevel.Dark,
            beans = "Colombian Supremo",
            steps = listOf(
                BrewStep(1, "Heat Water", "Boil water and let it cool to 200°F (93°C).", 60),
                BrewStep(2, "Add Coffee", "Add 30g of coarsely ground coffee to the French press.", 15),
                BrewStep(3, "Steep", "Pour 500ml of hot water, place lid on. Let steep for 4 minutes.", 240),
                BrewStep(4, "Press & Serve", "Slowly press the plunger down. Pour and enjoy.", null),
            ),
        ),
        Recipe(
            id = "3",
            title = "Aeropress Inverted",
            description = "Clean and sweet cup with balanced acidity.",
            imageUrl = MockRecipeImageUrl,
            brewMethod = BrewMethod.Aeropress,
            difficulty = Difficulty.Medium,
            totalBrewTimeSeconds = 180,
            roastLevel = RoastLevel.Medium,
            beans = "Guatemalan Antigua",
            steps = listOf(
                BrewStep(1, "Setup", "Assemble Aeropress in inverted position. Rinse filter.", 20),
                BrewStep(2, "Add Coffee & Water", "Add 17g of fine-medium ground coffee. Pour 220ml water at 195°F.", 15),
                BrewStep(3, "Steep & Stir", "Stir gently, then let steep.", 90),
                BrewStep(4, "Flip & Press", "Attach cap with filter, flip onto mug, press slowly for 30 seconds.", 30),
                BrewStep(5, "Enjoy", "Your Aeropress coffee is ready.", null),
            ),
        ),
        Recipe(
            id = "4",
            title = "Chemex Classic",
            description = "Bright and tea-like with vibrant fruit notes.",
            imageUrl = MockRecipeImageUrl,
            brewMethod = BrewMethod.Chemex,
            difficulty = Difficulty.Medium,
            totalBrewTimeSeconds = 300,
            roastLevel = RoastLevel.Light,
            beans = "Kenyan AA",
            steps = listOf(
                BrewStep(1, "Prepare Filter", "Fold Chemex filter and place in brewer. Rinse with hot water.", 30),
                BrewStep(2, "Add Coffee", "Add 30g of medium-coarse ground coffee.", 15),
                BrewStep(3, "Bloom", "Pour 60g of water (200°F). Wait 45 seconds.", 45),
                BrewStep(4, "Pour", "Slowly pour remaining water to 500g in circular motions.", 180),
                BrewStep(5, "Serve", "Remove filter and serve.", null),
            ),
        ),
        Recipe(
            id = "5",
            title = "Cold Brew Concentrate",
            description = "Smooth, sweet, and low-acidity cold brew concentrate.",
            imageUrl = MockRecipeImageUrl,
            brewMethod = BrewMethod.ColdBrew,
            difficulty = Difficulty.Easy,
            totalBrewTimeSeconds = 43200,
            roastLevel = RoastLevel.MediumDark,
            beans = "Brazilian Santos",
            steps = listOf(
                BrewStep(1, "Combine", "Add 100g coarsely ground coffee to a jar. Pour 600ml cold water.", 30),
                BrewStep(2, "Steep", "Cover and refrigerate for 12-24 hours.", null),
                BrewStep(3, "Filter", "Strain through a fine mesh sieve or coffee filter.", 60),
                BrewStep(4, "Serve", "Dilute concentrate 1:1 with water or milk. Serve over ice.", null),
            ),
        ),
        Recipe(
            id = "6",
            title = "Espresso Shot",
            description = "Rich, intense shot with crema and bold chocolate notes.",
            imageUrl = MockRecipeImageUrl,
            brewMethod = BrewMethod.EspressoMachine,
            difficulty = Difficulty.Hard,
            totalBrewTimeSeconds = 30,
            roastLevel = RoastLevel.Dark,
            beans = "Italian Roast Blend",
            steps = listOf(
                BrewStep(1, "Grind & Dose", "Grind 18g of coffee to fine espresso grind. Distribute evenly in portafilter.", 15),
                BrewStep(2, "Tamp", "Tamp with 30lbs of pressure. Ensure level surface.", 10),
                BrewStep(3, "Extract", "Lock portafilter, start extraction. Target 25-30 seconds for 36ml.", 30),
                BrewStep(4, "Enjoy", "Serve immediately. Note the crema and aroma.", null),
            ),
        ),
    )

    override suspend fun getAll(): List<Recipe> = recipes

    override suspend fun getById(id: String): Recipe? = recipes.find { it.id == id }

    override suspend fun search(query: String): List<Recipe> {
        if (query.isBlank()) return recipes
        val lower = query.lowercase()
        return recipes.filter { recipe ->
            recipe.title.lowercase().contains(lower) ||
                recipe.description.lowercase().contains(lower) ||
                recipe.brewMethod.displayName.lowercase().contains(lower) ||
                recipe.beans?.lowercase()?.contains(lower) == true
        }
    }
}
