package jet.isur.nsi.generator;

import org.junit.Test;

public class GeneratorTest extends BaseTest{

    private Generator generator;

    @Override
    public void setup() throws Exception {
        super.setup();
        generator = new Generator(dataSource, config);
    }

    @Test
    public void testAddData() {
        /*
        generator.addData("MSG");
        */
    }

    @Test
    public void testfillDatabase() {
        /*
        generator.setDefCount(100);
        generator.setCustomCount("MSG", 1000);
        generator.setCustomCount("MSG_EMP", 1000);
        generator.setCustomCount("MSG_INSTRUCTION", 1200);
        generator.setCustomCount("MSG_INSTRUCTION_ORG", 1200);
        generator.setCustomCount("EVENT", 1000);
        generator.setCustomCount("EVENT_PARAM", 3000);
        generator.setCustomCount("ORG_UNIT", 150);
        generator.fillDatabase();
        */
    }

    @Test
    public void testCleanDatabase() {
        /*
        generator.cleanDatabase();
        */
    }
}
