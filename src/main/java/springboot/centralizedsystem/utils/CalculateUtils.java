package springboot.centralizedsystem.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;

public class CalculateUtils {

    public static int getDurationPercent(String start, String expired) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(Configs.DATETIME_FORMAT);
        Date dateStart = format.parse(start);
        Date dateExpired = format.parse(expired);

        // in milliseconds
        long diffStartExpired = dateExpired.getTime() - dateStart.getTime();
        long diffStartNow = new Date().getTime() - dateStart.getTime();

        int result = (int) Math.round((double) diffStartNow / diffStartExpired * 100);
        if (result > 100) {
            result = 100;
        }

        return result;
    }

    public static String getTypeProgressBar(int percent) {
        if (percent < 25) {
            return Keys.SUCCESS;
        } else if (percent < 50) {
            return Keys.PRIMARY;
        } else if (percent < 75) {
            return Keys.WARNING;
        } else if (percent < 100) {
            return Keys.INFO;
        }
        return Keys.DANGER;
    }

    public static boolean isFormPendingOrExpired(String dateCompareStr) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(Configs.DATETIME_FORMAT);
        Date dateCompare = format.parse(dateCompareStr);
        Date dateNow = new Date();
        return dateCompare.compareTo(dateNow) >= 0;
    }
}
