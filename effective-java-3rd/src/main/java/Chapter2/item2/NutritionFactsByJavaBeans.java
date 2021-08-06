package Chapter2.item2;

public class NutritionFactsByJavaBeans {
    private int servingSize;   // 필수
    private int servings;      // 필수
    private int calories;      // 선택
    private int fat;           // 선택
    private int sodium;        // 선택
    private int carbohydrate;  // 선택

    public NutritionFactsByJavaBeans() {}

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public void setSodium(int sodium) {
        this.sodium = sodium;
    }

    public void setCarbohydrate(int carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public static void main(String[] args) {
        NutritionFactsByJavaBeans cocaCola = new NutritionFactsByJavaBeans();

        cocaCola.setServingSize(240);
        cocaCola.setServings(8);
        cocaCola.setCalories(100);
        cocaCola.setSodium(35);
        cocaCola.setCarbohydrate(27);
    }
}
