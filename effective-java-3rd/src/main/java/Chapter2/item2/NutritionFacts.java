package Chapter2.item2;

import lombok.Builder;

@Builder(builderMethodName = "requireArgsBuilder")
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static NutritionFactsBuilder builder(int servingSize, int servings) {
        return requireArgsBuilder()
            .servingSize(servingSize)
            .servings(servings);
    }

    public static void main(String[] args) {
        NutritionFacts cocaCola = NutritionFacts.builder(240, 8)
            .calories(100)
            .sodium(35)
            .carbohydrate(27).build();
    }
}
