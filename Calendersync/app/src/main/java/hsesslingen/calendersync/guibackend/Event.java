package hsesslingen.calendersync.guibackend;

import android.database.Cursor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

////////////////////////////////////////////////////////////////
// Event-class
///////////////////////////////////////////////////////////////

public class Event implements Serializable
{
	////////////////////////////////////////////////////////////////
	// Members of Event-class
	///////////////////////////////////////////////////////////////

	private String  m_title;
	private String  m_location;
	private String  m_description;
	private String  m_color;
	private String  m_timezone;
	private String  m_allDay;
	private String  m_startDate;
	private String  m_endDate;
	private String  m_duration;
	private String  m_recurrenceRule;
	private String  m_lastDate;
	private String  m_accessLevel;
	private String  m_availability;
	private String  m_guestCanModify;
	private String  m_guestCanInviteOthers;
	private String  m_guestCanSeeGuests;
	private String  m_customAppPackage;
	private String  m_customAppUri;
	private String  m_UID2445;

	private String m_eventId;

	private Boolean  m_fullTransfer;
	private Boolean  m_viewEvent;
	private String   m_repeatFrequency = null;

	////////////////////////////////////////////////////////////////
	// Constructor of Event-class
	////////////////////////////////////////////////////////////////

	public Event(Cursor eventCursor, Boolean isFullTransfer, Boolean isViewEvent)
	{
		m_fullTransfer              = isFullTransfer;
		m_viewEvent					= isViewEvent;

		m_title 					= eventCursor.getString(0);
		m_location 					= eventCursor.getString(1);
		m_description 				= eventCursor.getString(2);
		m_color 					= eventCursor.getString(3);

		if("0".equals(m_color) || m_color == null)
		{
			//if no event color take the calendar color
			m_color  = eventCursor.getString(20);
		}

		m_timezone 					= eventCursor.getString(4);
		m_allDay 					= eventCursor.getString(5);
		m_startDate 				= eventCursor.getString(6);

		if(IsAllDay() && m_viewEvent)
		{
			Date start = new Date(Long.parseLong(m_startDate));

			start.setHours(0);
			start.setMinutes(0);
			start.setSeconds(1);

			Date end = new Date(start.getTime());

			end.setHours(23);
			end.setMinutes(59);
			end.setSeconds(59);

			Date last = new Date(eventCursor.getLong(10));

			last.setHours(0);
			last.setMinutes(0);
			last.setSeconds(0);

			m_startDate = Long.toString(start.getTime());
			m_endDate   = Long.toString(end.getTime());
			m_lastDate  = Long.toString(last.getTime());
		}
		else
		{
			m_endDate 					= eventCursor.getString(7);
			m_lastDate 					= eventCursor.getString(10);
		}

		m_duration 					= eventCursor.getString(8);
		m_recurrenceRule 			= eventCursor.getString(9);

		//Find out how often the event has to be repeated
		if (m_recurrenceRule != null)
		{
			m_repeatFrequency = m_recurrenceRule.substring(m_recurrenceRule.indexOf("=") + 1, m_recurrenceRule.indexOf(";"));
		}

		m_accessLevel 				= eventCursor.getString(11);
		m_availability 				= eventCursor.getString(12);
		m_guestCanModify 			= eventCursor.getString(13);
		m_guestCanInviteOthers 		= eventCursor.getString(14);
		m_guestCanSeeGuests 		= eventCursor.getString(15);
		m_customAppPackage 			= eventCursor.getString(16);
		m_customAppUri 				= eventCursor.getString(17);
		m_UID2445 					= eventCursor.getString(18);
		m_eventId 					= eventCursor.getString(19);
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of Event-class
	///////////////////////////////////////////////////////////////

	public String GetTitle()
	{
		return m_title;
	}

	public String GetLocation()
	{
		return m_location;
	}

	public String GetDescription()
	{
		return m_description;
	}

	public String GetColor()
	{
		return m_color;
	}

	public String GetTimezone()
	{
		return m_timezone;
	}

	public String GetAllDay()
	{
		return m_allDay;
	}

	public String GetStartDate()
	{
		return m_startDate;
	}

	public String GetEndDate()
	{
		return m_endDate;
	}

	public String GetDuration()
	{
		return m_duration;
	}

	public String GetRRule()
	{
		return m_recurrenceRule;
	}

	public String GetLastDate()
	{
		return m_lastDate;
	}

	public String GetAccessLevel()
	{
		return m_accessLevel;
	}

	public String GetAvailability()
	{
		return m_availability;
	}

	public String GetCanModify()
	{
		return m_guestCanModify;
	}

	public String GetCanInvite()
	{
		return m_guestCanInviteOthers;
	}

	public String GetCanSeeGuests()
	{
		return m_guestCanSeeGuests;
	}

	public String AppPackage()
	{
		return m_customAppPackage;
	}

	public String GetAppUri()
	{
		return m_customAppUri;
	}

	public String GetUID()
	{
		return m_UID2445;
	}

	public String GetId()
	{
		return m_eventId;
	}

	public int GetColorCode() { return Integer.parseInt(m_color); }

	////////////////////////////////////////////////////////////////
	// Public Methods of Event-class
	// to get Information about the event
	///////////////////////////////////////////////////////////////

	public Boolean IsAllDay()
	{
		return !(m_allDay.equals("0"));
	}

	public Boolean IsRepeated()
	{
		if(m_recurrenceRule != null)
		{
			return true;
		}
		return false;
	}

	public Boolean IsDaily() { return ("DAILY".equals(m_repeatFrequency)); }

	public Boolean IsOver(Date date)
	{
		return (Long.parseLong(m_lastDate) <= date.getTime());
	}

	public Boolean IsFullTransfer() { return m_fullTransfer; }

	public void CorrectEventData(Date startDate)
	{
		CorrectRRule(null);
		CorrectStartDate(startDate);
		if(m_allDay.equals("0"))
		{
			CorrectEndDate();
		}
	}

	public String GetTime()
	{
		String time = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Date startDate = new Date(Long.parseLong(m_startDate));

		if(!IsAllDay())
		{
			time += dateFormat.format(startDate);
			time += " - ";

			if(m_recurrenceRule != null)
			{
				String duration = m_duration.substring(1,m_duration.length() - 1);
				Date endDate = new Date(startDate.getTime() + (Long.parseLong(duration)* 1000));
				time += dateFormat.format(endDate);
			}
			else
			{
				time += dateFormat.format(new Date(Long.parseLong(m_endDate)));
			}
			return time;
		}
		return "AllDay-Event";
	}

	////////////////////////////////////////////////////////////////
	// Private Methods of Event-class
	///////////////////////////////////////////////////////////////

	private void CorrectRRule(String rule)
	{
		m_recurrenceRule = rule;
	}

	private void CorrectStartDate(Date date)
	{
		Date start = new Date(Long.parseLong(m_startDate));

		start.setYear(date.getYear());
		start.setMonth(date.getMonth());
		start.setDate(date.getDate());

		m_startDate = Long.toString(start.getTime());
	}

	private void CorrectEndDate()
	{
		Date end = new Date(Long.parseLong(m_startDate));

		String duration = m_duration.substring(1, m_duration.length() - 1);
		end.setTime(end.getTime() + (Long.parseLong(duration) * 1000));

		m_endDate = Long.toString(end.getTime());

		m_duration = null;
	}
}
