package jet.isur.nsi.api.data;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class ConvertUtils {

    private static final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();
    
    public static final String TRUE_VALUE = "true";
    public static final String FALSE_VALUE = "false";
    
    public static Boolean dbStringToBool(String boolValue) {
        if(boolValue == null) {
            return null;
        }
        if("Y".equalsIgnoreCase(boolValue)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public static String dbBoolToString(Boolean value) {
        if(value == null) {
            return null;
        } else if(value) {
            return "Y";
        } else {
            return "N";
        }
    }

    public static Boolean stringToBool(String boolValue) {
        if(boolValue == null) {
            return null;
        }
        if(TRUE_VALUE.equalsIgnoreCase(boolValue)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public static String boolToString(Boolean value) {
        if(value == null) {
            return null;
        } else if(value) {
            return TRUE_VALUE;
        } else {
            return FALSE_VALUE;
        }
    }

    public static String timestampToString(Timestamp dateValue) {
        if(dateValue == null) {
            return null;
        } else {
            return new DateTime(dateValue.getTime()).toString(dateFormatter);
        }
    }

    public static String dateTimeToString(DateTime value) {
        if(value == null) {
            return null;
        } else {
            return value.toString(dateFormatter);
        }
    }

    public static DateTime stringToDateTime(String value) {
        if(value == null) {
            return null;
        } else {
            return DateTime.parse(value, dateFormatter);
        }
    }

    public static String longToString(Long value) {
        if(value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    public static Long stringToLong(String value) {
        if(value == null) {
            return null;
        } else {
            return Long.parseLong(value);
        }
    }

    public static String integerToString(Integer value) {
        if(value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    public static Integer stringToInteger(String value) {
        if(value == null) {
            return null;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static String doubleToString(Double value) {
        if(value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    public static Double stringToDouble(String value) {
        if(value == null) {
            return null;
        } else {
            return Double.parseDouble(value);
        }
    }

    public static String bigDecimalToString(BigDecimal bigDecimalValue) {
        if(bigDecimalValue == null) {
            return null;
        } else {
            return bigDecimalValue.toString();
        }
    }


}
