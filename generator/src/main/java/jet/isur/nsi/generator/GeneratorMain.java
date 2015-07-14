package jet.isur.nsi.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorMain {
    /**
     * список таблиц для максимального заполнения базы
     */
    public List<String> fillDatabaseTables = new ArrayList<>(Arrays.asList("ORG_OBJ","EVENT_PARAM","MSG_INSTRUCTION_ORG","MSG_EMP"));

    /*
    public void fillDatabase() {
        for (String tableName:fillDatabaseTables) {
            addData(tableName);
        }
    }

    public void cleanDatabase() {
        for (String tableName:fillDatabaseTables) {
            cleanData(tableName);
        }
    }

    public void addFillDatabaseTables(String tableName) {
        if (config.getDict(tableName) != null && !fillDatabaseTables.contains(tableName)) {
            fillDatabaseTables.add(tableName);
        }
    }
    */


}
