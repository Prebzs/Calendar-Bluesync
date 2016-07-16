package hsesslingen.calendersync.backend;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import hsesslingen.calendersync.guibackend.Event;
import hsesslingen.calendersync.guibackend.Calendar;
import hsesslingen.calendersync.managers.PermissionManager;

////////////////////////////////////////////////////////////////
// CalendarModel-class for accessing calendar data in
// order to load existing calendar entries or to save new one
///////////////////////////////////////////////////////////////

public class CalendarModel
{
	public interface OnSaveFinishedCallback
	{
		void onSaveFinished();
	}

	//Used to connect Model and ViewModel
	public void RegisterOnSaveFinishedEvent(OnSaveFinishedCallback callback)
	{
		m_onSaveFinishedCallback = callback;
	}

	////////////////////////////////////////////////////////////////
	// Members of CalendarModel-class
	///////////////////////////////////////////////////////////////

	private Activity m_activity;

	private List<Calendar>	m_calendars;

	private OnSaveFinishedCallback m_onSaveFinishedCallback;

	//Projection to query
	private String[] CALENDAR_PROJECTION = new String[]
			{
					CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 0
					CalendarContract.Calendars.CALENDAR_COLOR,        // 1
					CalendarContract.Calendars.ACCOUNT_NAME,          // 2
					CalendarContract.Calendars.OWNER_ACCOUNT,		  // 3
					CalendarContract.Calendars._ID                    // 4
			};

	public static final String[] EVENT_PROJECTION = new String[]
			{
					CalendarContract.Events.TITLE,                   // 0
					CalendarContract.Events.EVENT_LOCATION,	         // 1
					CalendarContract.Events.DESCRIPTION,	         // 2
					CalendarContract.Events.EVENT_COLOR,		     // 3
					CalendarContract.Events.EVENT_TIMEZONE,		     // 4
					CalendarContract.Events.ALL_DAY,                 // 5
					CalendarContract.Events.DTSTART,                 // 6
					CalendarContract.Events.DTEND,                   // 7
					CalendarContract.Events.DURATION,                // 8
					CalendarContract.Events.RRULE,			         // 9
					CalendarContract.Events.LAST_DATE,               // 10
					CalendarContract.Events.ACCESS_LEVEL,		     // 11
					CalendarContract.Events.AVAILABILITY,		     // 12
					CalendarContract.Events.GUESTS_CAN_MODIFY,		 // 13
					CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS,// 14
					CalendarContract.Events.GUESTS_CAN_SEE_GUESTS,	 // 15
					CalendarContract.Events.CUSTOM_APP_PACKAGE,	     // 16
					CalendarContract.Events.CUSTOM_APP_URI,		     // 17
					CalendarContract.Events.UID_2445,                // 18
					CalendarContract.Events._ID,                     // 19

					CalendarContract.Events.CALENDAR_COLOR,			 // 20

					CalendarContract.Instances.BEGIN,				 // 21
					CalendarContract.Events.ORIGINAL_ID				 // 22
			};

	public static final String[] DELETE_PROJECTION = new String[]
			{

					CalendarContract.Events._ID,                     // 0
					CalendarContract.Events.ORIGINAL_ID,		     // 1
					CalendarContract.Events.CALENDAR_ID				 // 2
			};

	////////////////////////////////////////////////////////////////
	// Constructor of CalendarModel-class
	///////////////////////////////////////////////////////////////

	public CalendarModel(Activity activity)
	{
		m_activity = activity;
		m_calendars = new ArrayList<>();

		LoadAllCalendars();
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of CalendarModel-class
	///////////////////////////////////////////////////////////////

	public void LoadAllCalendars()
	{
		//Create the ContentResolver which is necessary to query for the different calendars
		ContentResolver contentResolver = m_activity.getContentResolver();

		//Get the URI which identifies the content
		Uri uri = CalendarContract.Calendars.CONTENT_URI;

		//Due to changes in Android 6.0 a permissionCheck is necessary when reading information from a device
		//readPermission can be PERMISSION_GRANTED or PERMISSION_DENIED
		Boolean readPermission = PermissionManager.hasPermission(m_activity, Manifest.permission.READ_CALENDAR);

		if(readPermission)
		{
			//If permission is granted query for the calendar data and return it
			Cursor calendarCursor = contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null);

			//For each calendar Create a Calendar-object
			while (calendarCursor.moveToNext())
			{
				Calendar calendar = new Calendar(calendarCursor);

				m_calendars.add(calendar);
			}
		}
		else
		{
			//TO-DO: Android 6.0 Abfrage

			//Ask the user for the needed permission
			PermissionManager.requestPermission(m_activity, Manifest.permission.READ_CALENDAR,
					new PermissionManager.OnRequestPermissionsResultCallback()
					{
						@Override
						public void onRequestPermissionsResult()
						{
							;
						}
					}
			);
		}
	}

	public List<Event> GetSingleEvent(Event event, Date date)
	{
		ArrayList<Event> events      = new ArrayList<>();
		ArrayList<Event> singleEvent = new ArrayList<>();

		//Set start of search interval to current day
		long startMillis = date.getTime();
		//Set end of search interval to end of current day
		long endMillis = date.getTime() + (24 * 60 * 60 * 1000) - 1000;

		ContentResolver cr = m_activity.getContentResolver();

		// Construct the query with the desired date range.
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startMillis);
		ContentUris.appendId(builder, endMillis);

		// Submit the query
		Cursor eventCursor = cr.query(builder.build(), EVENT_PROJECTION, null, null, null);

		if(eventCursor != null)
		{
			while (eventCursor.moveToNext())
			{
				Event senEvent = new Event(eventCursor, false, false);

				if (senEvent.GetTitle().equals(event.GetTitle()))
				{
					if (senEvent.IsRepeated())
					{
						//correct the data before sending
						senEvent.CorrectEventData(date);
					}
					events.add(senEvent);
				}
			}
			eventCursor.close();
		}

		//make sure that only one event is sent
		singleEvent.add(events.get(0));

		return singleEvent;
	}

	public List<Event> GetEventRange(int calendarId, Date startDate, Date endDate)
	{
		ArrayList<Event> events = new ArrayList<>();
		ArrayList<Event> sendEvents = new ArrayList<>();

		long startMillis = startDate.getTime();
		long endMillis   = (endDate.getTime() + (24 * 60 * 60 * 1000) - 1000);

		ContentResolver cr = m_activity.getContentResolver();

		// The ID of the recurring event whose instances you are searching
		String selection = CalendarContract.Instances.CALENDAR_ID + " = ?";
		String[] selectionArgs = new String[]{Integer.toString(calendarId)};

		// Construct the query with the desired date range.
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startMillis);
		ContentUris.appendId(builder, endMillis);

		// Submit the query
		Cursor eventCursor = cr.query(builder.build(), EVENT_PROJECTION, selection, selectionArgs, null);

		if(eventCursor != null)
		{
			while (eventCursor.moveToNext())
			{
				Event event = new Event(eventCursor, false, false);

				if (event.GetRRule() != null)
				{
					//get start date of instance
					long start = eventCursor.getLong(21);
					Date startDate2 = new Date(start);

					//correct the data before sending
					event.CorrectEventData(startDate2);
				}
				events.add(event);
			}
			eventCursor.close();
		}

		List<String> eventIds = new ArrayList<>();

		//Filter events for double entries
		for (Event event : events)
		{
			if(event.GetAllDay().equals("0") && !eventIds.contains(event.GetId()))
			{
				//events that are not all-day can be send without any problems
				sendEvents.add(event);
				eventIds.add(event.GetId());
			}
			else if(!eventIds.contains(event.GetId()))
			{
				sendEvents.add(event);
				eventIds.add(event.GetId());
			}
		}
		return sendEvents;
	}

	public List<Event> GetAllEvents(int calendarId)
	{
		Date now = new Date();

		// create Calendar instance with actual date
		java.util.GregorianCalendar calendar = new GregorianCalendar();

		//prepare start and end dates for the query ( today +/- 10 years)
		calendar.setTime(now);
		calendar.add(java.util.GregorianCalendar.YEAR, -10);
		Date startDate = calendar.getTime();
		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(1);

		calendar.setTime(now);
		calendar.add(java.util.GregorianCalendar.YEAR, 10);
		Date endDate   = calendar.getTime();
		endDate.setHours(23);
		endDate.setMinutes(59);
		endDate.setSeconds(59);

		return GetEventRange(calendarId,startDate,endDate);
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of CalendarModel-class for Receiving Events
	///////////////////////////////////////////////////////////////

	public void SaveReceivedData(int calendarId, List<Event> eventList, Boolean doBackup)
	{
		Boolean isFullTransfer = eventList.get(0).IsFullTransfer();

		//check if backup is needed (if checkbox is clicked or if fullTransfer)
		if(doBackup || isFullTransfer)
		{
			CreateBackup(calendarId);
		}

		//check if fullTransfer or not
		if(isFullTransfer)
		{
			//if fullTransfer delete all events in the calendar
			DeleteAllEventsFromCalendar(calendarId);
		}
		SaveEvents(calendarId, eventList);
	}

	public void LoadBackup(String filename)
	{
		List<Event> backup = new ArrayList<>();
		FileInputStream fis;

		try
		{
			fis = m_activity.getApplicationContext().openFileInput(filename);

			ObjectInputStream in = new ObjectInputStream(fis);

			try
			{
				backup = (List<Event>) in.readObject();
			}
			catch (ClassNotFoundException e) {e.printStackTrace();}

			in.close();
			fis.close();

		} catch (IOException e) {e.printStackTrace();}

		//get the calendar id from the filename (example: file-1 -> id = 1)
		int calendarId = Integer.parseInt(filename.substring(filename.indexOf("-") + 1, filename.indexOf("-") + 2));

		//delete all events and save backup to calendar
		DeleteAllEventsFromCalendar(calendarId);
		SaveEvents(calendarId, backup);
	}

	public List<Calendar> GetCalendars()
	{
		return m_calendars;
	}

	public Calendar GetCalendarById(int calendarId)
	{
		for(Calendar calendar : m_calendars)
		{
			if (calendarId == calendar.GetId())
			{
				return calendar;
			}
		}
		return null;
	}

	public List<Calendar> GetCalendarsById(int[] calendarIds)
	{
		//List which should be filled with the calendars for the view
		List<Calendar> calendars = new ArrayList<>();

		for(int index = 0; index < calendarIds.length; ++index)
		{
			int calendarId = calendarIds[index];

			for(Calendar calendar : m_calendars)
			{
				if (calendarId == calendar.GetId())
				{
					calendars.add(calendar);
				}
			}
		}
		return calendars;
	}

	////////////////////////////////////////////////////////////////
	// Private Methods of CalendarModel-class
	///////////////////////////////////////////////////////////////

	private void SaveEvents(int calendarId, List<Event> events)
	{
		ContentResolver contentResolver = m_activity.getContentResolver();
		ContentValues contentValues = new ContentValues();

		for(Event event : events)
		{
			contentValues.clear();

			//save the values of the received event
			contentValues.put(CalendarContract.Events.TITLE, 						event.GetTitle());
			contentValues.put(CalendarContract.Events.ORGANIZER, 					GetCalendarById(calendarId).GetAccountOwner());
			contentValues.put(CalendarContract.Events.EVENT_LOCATION, 				event.GetLocation());
			contentValues.put(CalendarContract.Events.DESCRIPTION, 					event.GetDescription());
			contentValues.put(CalendarContract.Events.EVENT_COLOR, 					event.GetColor());
			contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, 				event.GetTimezone());
			contentValues.put(CalendarContract.Events.ALL_DAY, 						event.GetAllDay());
			contentValues.put(CalendarContract.Events.DTSTART, 						event.GetStartDate());
			contentValues.put(CalendarContract.Events.DTEND, 						event.GetEndDate());
			contentValues.put(CalendarContract.Events.DURATION,						event.GetDuration());
			contentValues.put(CalendarContract.Events.LAST_DATE, 					event.GetLastDate());
			contentValues.put(CalendarContract.Events.ACCESS_LEVEL, 				event.GetAccessLevel());
			contentValues.put(CalendarContract.Events.AVAILABILITY, 				event.GetAvailability());
			contentValues.put(CalendarContract.Events.GUESTS_CAN_MODIFY, 			event.GetCanModify());
			contentValues.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, 	event.GetCanInvite());
			contentValues.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, 		event.GetCanSeeGuests());
			contentValues.put(CalendarContract.Events.CUSTOM_APP_PACKAGE, 			event.AppPackage());
			contentValues.put(CalendarContract.Events.CUSTOM_APP_URI, 				event.GetAppUri());
			contentValues.put(CalendarContract.Events.UID_2445, 					event.GetUID());
			contentValues.put(CalendarContract.Events.CALENDAR_ID, 					calendarId);

			//save the events to the selected calendar
			contentResolver.insert(Uri.parse("content://com.android.calendar/events"), contentValues);
		}

		m_onSaveFinishedCallback.onSaveFinished();
	}

	////////////////////////////////////////////////////////////////
	// Private Methods of CalendarModel-class for backup
	///////////////////////////////////////////////////////////////

	private void CreateBackup(int calendarId)
	{
		List<Event> backup;
		FileOutputStream fos;

		//get the events of the calendar you want to backup
		Date startDate = new Date();
		Date endDate   = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String start = "01-01-2000 00:00:00";
		String end   = "31-12-2100 00:00:00";

		try
		{
			startDate = sdf.parse(start);
			endDate   = sdf.parse(end);
		}
		catch (ParseException e)
		{
			Log.e("CalendarModel:", "Error while trying to parse startDate and endDate.");
		}

		backup = GetEventRange(calendarId, startDate, endDate);

		try
		{
			//save the calendar backup with id in filename
			fos = m_activity.getApplicationContext().openFileOutput("Backup-" + calendarId + " of "+ GetCalendarById(calendarId).GetTitle(), Context.MODE_PRIVATE);

			ObjectOutputStream out = new ObjectOutputStream(fos);

			out.writeObject(backup);
			out.close();
			fos.close();

		} catch (IOException e) {e.printStackTrace();}
	}

	private void DeleteAllEventsFromCalendar(int calendarId)
	{
		ContentResolver cr = m_activity.getContentResolver();

		String selection = CalendarContract.Events.CALENDAR_ID + " = ?";
		String[] selectionArgs = new String[]{Integer.toString(calendarId)};

		//first query for all events in the calendar
		Cursor eventCursor = cr.query(Uri.parse("content://com.android.calendar/events"), DELETE_PROJECTION, selection, selectionArgs, null);

		if(eventCursor != null)
		{
			while (eventCursor.moveToNext())
			{
				//only delete events, do not try to delete exceptions because this results in an endless-loop
				if (eventCursor.getString(1) == null || eventCursor.getString(1).isEmpty())
				{
					//only delete the event of the calendar in which the backup should be loaded
					if (eventCursor.getInt(2) == calendarId)
					{
						long eventId = eventCursor.getLong(0);
						cr.delete(android.content.ContentUris.withAppendedId(Uri.parse("content://com.android.calendar/events"), eventId), null, null);
					}
				}
			}
			eventCursor.close();
		}
	}
}
