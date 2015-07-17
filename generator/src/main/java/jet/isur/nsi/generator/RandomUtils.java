package jet.isur.nsi.generator;

import java.util.Date;
import java.util.Random;

import org.joda.time.DateTime;

public class RandomUtils {

    private static Random random = new Random(1000);
    /**
     * Интервал даты в секундах для рандомизации (до текущей)
     */
    private static int timePeriod = 60*60*24*30*6;
    /**
     * Начало периода рандомизации даты
     */
    private static long timeStart = DateTime.now().getMillis() - timePeriod;

    public static Date getDate(){
        long time = timeStart + random.nextInt(timePeriod) * 1000;
        return new Date(time);
    }

    public static DateTime getDateTime(){
        long time = timeStart + random.nextInt(timePeriod) * 1000;
        return new DateTime(time);
    }

    public static int getInt(int max){
        return random.nextInt(max);
    }

    public static boolean getBoolean(){
        return random.nextBoolean();
    }
}
