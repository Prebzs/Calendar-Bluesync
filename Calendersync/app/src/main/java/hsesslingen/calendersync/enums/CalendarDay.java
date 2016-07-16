package hsesslingen.calendersync.enums;

public enum CalendarDay
{
    NONE(0, "None", "None", "None"),
    MONDAY(1, "Monday", "M", "Mo"),
    TUESDAY(2, "Tuesday", "T", "Tu"),
    WEDNESDAY(3, "Wednesday", "W", "We"),
    THURSDAY(4, "Thursday", "T", "Th"),
    FRIDAY(5, "Friday", "F", "Fr"),
    SATURDAY(6, "Saturday", "S", "Sa"),
    SUNDAY(7, "Sunday", "S", "Su");

    private final int m_ID;
    private final String m_MonthName;
    private final String m_Shortcut;
    private final String m_ShortName;

    CalendarDay(int id, String monthName, String shortcut, String shortName)
    {
        m_ID = id;
        m_MonthName = monthName;
        m_Shortcut = shortcut;
        m_ShortName = shortName;
    }

    public int getID()
    {
        return m_ID;
    }

    public String getName()
    {
        return m_MonthName;
    }

    public String getShortcut()
    {
        return m_Shortcut;
    }

    public String getShortName()
    {
        return m_ShortName;
    }
}
