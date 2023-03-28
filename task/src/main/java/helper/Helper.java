package helper;

import com.github.javafaker.Faker;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Helper {

    public static final Faker FAKER = new Faker();

    public static String getRandomText() {
        return FAKER.howIMetYourMother().catchPhrase();
    }

    public static int getRandomId() { return (int) (Math.random() * 99999999);}
}