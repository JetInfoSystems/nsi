package jet.nsi.generator;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jet.nsi.generator.helpers.RandomUtils;

public class DynamicContent {

    private static Map<String, StringGenerator> fields = new HashMap<>();

    static {
        StringGenerator sg = new StringGenerator() {
            @Override
            public String next() {
                String[] ms1 = {
                        "Представить", 
                        "Подготовить",
                        "Представить",
                        "Подготовить и представить",
                        };
                String[] ms2 = {
                        "сведения об организационных мероприятиях по доведению анализа реагирования",
                        "анализы реагирования объектового звена РСЧС",
                        "информацию о причинах нарушения требований ЛНД Компании",
                        "сведения о проведенных организационных мероприятиях по доведению анализа реагирования",
                        "информацию об участках трубопроводов с высоким риском аварийности",
                        "график восстановления электроснабжения ОГ и скважин",
                        "cхему электоросетей сетей с повреждениеями, а также перечень необходимых материалов",
                        "предложения по восстановлению запасов МТС, а так же по закупке резервных дизельэлектро станций",
                        "материалы о проведенных организационных мероприятиях",
                        "информацию о проводимых и спланированных мероприятиях, направленных на снижение колличества отказов трубопроводов"
                                
                }; 
                return ms1[getRandomInteger(0, ms1.length-1)] + " " + ms2[getRandomInteger(0, ms1.length-1)];
            }
        };
        fields.put("MSG_INSTRUCTION.SUBJECT", sg);


        sg = new StringGenerator() {
            @Override
            public String next() {
                String[] ms1 = {
                        "Письмо",
                        "Протокол селекторного совещания",
                        "Письмо директора",
                        "Письмо Рассадкина",
                        "Письмо о предоставлении информации" };
                String[] ms2 = {
                        "О разработке алгоритмов действий",
                        "Об анализе реагирования на ЧС (происшествия)",
                        "О предоставлении информации",
                        "О реагировании и представлении информации" };
                SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy");
                return ms1[getRandomInteger(0, ms1.length - 1)] + " от "
                        + sdf.format(RandomUtils.getDate()) + " \""
                        + ms2[getRandomInteger(0, ms1.length - 1)] + "\"";
            }
        };

        fields.put("MSG.SUBJECT", sg);
    }

    private static int getRandomInteger(int min, int max) {
        Random rand = new Random();
        int ii = min + (int) (rand.nextInt((max - min)));
        return ii;
    }

    /**
     * Получение значения для поля. Если генератор не задан, возвращает null.
     * @param field полное имя поля в формате table.field
     * @return значение
     */
    public static String getString(String fieldName) {
        StringGenerator sg = fields.get(fieldName);
        if (sg == null) {
            return null;
        }
        return sg.next();
    }

    public static void addStringGenerator(String fieldName, StringGenerator generator) {
        fields.put(fieldName, generator);
    }
}