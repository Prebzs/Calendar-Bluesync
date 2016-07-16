package hsesslingen.calendersync.guibackend;

import android.database.Cursor;

////////////////////////////////////////////////////////////////
// CalendarItem-class
///////////////////////////////////////////////////////////////

public class Calendar
{
	////////////////////////////////////////////////////////////////
	// Members of CalendarViewModel-class
	///////////////////////////////////////////////////////////////

	private String m_name;
	private int    m_color;
	private String m_accountName;
	private String m_owner;
	private int    m_id;

	////////////////////////////////////////////////////////////////
	// Constructor of CalendarViewModel-class
	///////////////////////////////////////////////////////////////

	public Calendar(Cursor calendarCursor)
	{
		m_name        = calendarCursor.getString(0);
		m_color		  = calendarCursor.getInt(1);
		m_accountName = calendarCursor.getString(2);
		m_owner       = calendarCursor.getString(3);
		m_id          = calendarCursor.getInt(4);
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of CalendarViewModel-class
	///////////////////////////////////////////////////////////////

	public String GetTitle() { return m_name; }

	public int GetColor() { return m_color; }

	public String GetAccountName() { return m_accountName; }

	public String GetAccountOwner() { return m_owner; }

	public int GetId(){ return m_id; }
}
