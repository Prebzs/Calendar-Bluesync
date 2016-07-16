package hsesslingen.calendersync.enums;

public enum CalendarMonth
{
    NONE(0,0,"None"),
    JANUARY(1, 31, "January"),
    FEBRUARY28(2, 28, "February"),
    FEBRUARY29(2, 29, "February"),
    MARCH(3, 31, "March"),
    APRIL(4, 30, "April"),
    MAY(5, 31, "May"),
    JUNE(6, 30, "June"),
    JULY(7, 31, "July"),
    AUGUST(8, 31, "August"),
    SEPTEMBER(9, 30, "September"),
    OCTOBER(10, 31, "October"),
    NOVEMBER(11, 30, "November"),
    DECEMBER(12, 31, "December");

    private final int m_ID;
    private final int m_Length;
    private final String m_MonthName;

    CalendarMonth(int id, int length, String monthName)
    {
        m_ID = id;
        m_Length = length;
        m_MonthName = monthName;
    }

    public int getID()
    {
        return m_ID;
    }

    public int getLength()
    {
        return m_Length;
    }

    public String getName()
    {
        return m_MonthName;
    }

    public static String[] getMonthNames()
    {
        String[] months =  new String[12];
        months[0] = JANUARY.getName();
        months[1] = FEBRUARY28.getName();
        months[2] = MARCH.getName();
        months[3] = APRIL.getName();
        months[4] = MAY.getName();
        months[5] = JUNE.getName();
        months[6] = JULY.getName();
        months[7] = AUGUST.getName();
        months[8] = SEPTEMBER.getName();
        months[9] = OCTOBER.getName();
        months[10] = NOVEMBER.getName();
        months[11] = DECEMBER.getName();
        return months;
    }

    public static CalendarMonth getMonth(int id, int year)
    {
        for(CalendarMonth month : CalendarMonth.values())
        {
            if(month.getID() == id)
            {
                if(month == FEBRUARY28 && year % 4 == 0)
                {
                    return FEBRUARY29;
                }
                return month;
            }
        }
        return NONE;
    }

    public static CalendarMonth getPreviousMonth(CalendarMonth calendarMonth, int year)
    {
        if(calendarMonth == NONE)
        {
            return NONE;
        }

        int id = calendarMonth.getID();
        if(id == 1)
        {
            return DECEMBER;
        }
        return getMonth(id - 1, year);
    }

    public static CalendarMonth getNextMonth(CalendarMonth calendarMonth, int year)
    {
        return getMonth((calendarMonth.getID() % 12) + 1, year);
    }
}
