package jet.isur.nsi.generator;

import java.util.HashMap;
import java.util.Map;

import jet.isur.nsi.generator.helpers.RandomUtils;

public class StaticContent {

    private static Map<String, String[]> fields = new HashMap<>();

    static {

        String[] values = new String[] {"Описание "};

        fields.put("MSG_INSTRUCTION.DESCRIPTION", values);
        fields.put("MSG_INSTRUCTION_ORG.DESCRIPTION", values);
        fields.put("EMP.DESCRIPTION", values);
        fields.put("ORG.DESCRIPTION", values);

        values = new String[] {"Содержание "};
        fields.put("MSG.DESCRIPTION", values);

        values = new String[] {"Подразделение "};
        fields.put("ORG_UNIT.ORG_UNIT_NAME", values);

        values = new String[] {"Должность "};
        fields.put("EMP.EMP_FUNCTION", values);

        values = new String[] {"Александр", "Алексей", "Иван", "Петр", "Сергей", "Николай"};
        fields.put("EMP.FIRST_NAME", values);
		values = new String[] { "Александров", "Алексеев", "Иванов", "Петров",
		        "Сергеев", "Николаев" };
		fields.put("EMP.SECONDARY_NAME", values);
        values = new String[] {"Александрович", "Алексеевич", "Иванович", "Петрович", "Сергеевич", "Николаевич"};
        fields.put("EMP.MIDDLE_NAME", values);

        /*values = new String[]{
                "ОАО Восточно-Сибирская нефтегазовая компания",
                "ООО РН-Юганскнефтегаз", "ООО РН-Пурнефтегаз", "ЗАО Ванкорнефть",
                "ООО РН-Северная нефть", "ООО РН-Краснодарнефтегаз",
                "ООО РН-Ставропольнефтегаз", "ОАО Грознефтегаз",
                "НК  Роснефть-Дагнефть", "ООО РН-Сахалинморнефтегаз",
                "ОАО Самаранефтегаз", "ОАО Удмуртнефть", "ОАО Верхнечонскнефтегаз",
                "ОАО Дагнефтегаз", "ЗАО РОСПАН ИНТЕРНЕШНЛ", "ОАО Варьеганнефтегаз",
                "ОАО Нижневартовское нефтегазодобывающее предприятие",
                "ОАО Оренбургнефть", "ОАО Самотлорнефтегаз",
                "ОАО РН-Нижневартовск", "ОАО РН-Няганьнефтегаз",
                "ОАО Тюменнефтегаз", "ООО Бугурусланнефть", "ООО РН-Уватнефтегаз",
                "ООО СП Ваньеганнефть", "ООО Таас-Юрях Нефтегазодобыча",
                "ОАО НГК Славнефть", "ОАО Братскэкогаз",
                "ООО Компания Полярное Сияние", "ОАО Томскнефть ВНК"
                };*/
        values = new String[] { "Роснефть", "ООО «РН-Юганскнефтегаз»",
                "ЗАО «Ванкорнефть»", "ОАО «Самаранефтегаз»",
                "ОАО «Куйбышевский НПЗ»" };
        fields.put("ORG.ORG_NAME", values);
    }

    public static String getString(String fieldName, int index) {
        String[] values = fields.get(fieldName);
        if (values == null) {
            return null;
        }
        if (values.length == 1) {
            return values[0] + index;
        }
        if (fieldName.endsWith("_NAME")) {
            return values[index % values.length];
        }
        return values[RandomUtils.getInt(values.length)];
    };

    public static boolean checkPredefinedNames(String tableName) {
        String[] values = fields.get(tableName+"."+tableName+"_NAME");
        return values != null && values.length > 1;
    };

    public static int getPredefinedSize(String tableName) {
        String[] values = fields.get(tableName+"." +tableName+"_NAME");
        if (values != null && values.length > 1){
            return values.length;
        }
        return 0;
    };

    public static void addStringContent(String fieldName, String[] content) {
        fields.put(fieldName, content);
    }
}