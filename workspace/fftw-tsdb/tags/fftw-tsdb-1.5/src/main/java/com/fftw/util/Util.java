package com.fftw.util;

import java.util.Calendar;

public class Util
{
    public static Calendar addWeekDay (Calendar cal, int nDay)
    {
        Calendar calendar = (Calendar)cal.clone();
        int nRemainder = nDay % 5;       
        int nDay5 = (nDay - nRemainder) / 5;
        if (nDay5 != 0)
        {
            calendar.add(Calendar.DAY_OF_YEAR, nDay5 * 7);
        }
        int nStep = nRemainder > 0 ? 1 : -1;
        while (nRemainder != 0)
        {
            calendar.add(Calendar.DAY_OF_YEAR, nStep);
            int nDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (nDayOfWeek != Calendar.SATURDAY && nDayOfWeek != Calendar.SUNDAY)
            {
                nRemainder -= nStep;
            }
        }
        return calendar;
    }
    
    public static boolean isWindowsPlatform ()
    {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
