package hsesslingen.calendersync.backend;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import hsesslingen.calendersync.bluetooth.BluetoothReceiver;
import hsesslingen.calendersync.bluetooth.BluetoothSender;
import hsesslingen.calendersync.guibackend.Event;
import hsesslingen.calendersync.enums.CalendarDay;
import hsesslingen.calendersync.enums.CalendarMonth;
import hsesslingen.calendersync.guibackend.Calendar;

////////////////////////////////////////////////////////////////
// CalendarViewModel-class for communication between Model and View
///////////////////////////////////////////////////////////////
public class CalendarViewModel
{
	////////////////////////////////////////////////////////////////
	// Interfaces and register methods of CalendarViewModel-class
	///////////////////////////////////////////////////////////////

	public interface OnDeviceFoundCallback
	{
		void onDeviceFound(BluetoothDevice foundDevice);
	}

	public interface OnTimerStoppedCallback
	{
		void onTimerStopped();
	}

	public interface OnOpenBluetoothCallback
	{
		void onOpenBluetooth(int colorKey, String title, String subTitle);
	}

	public interface OnTransferStartedCallback
	{
		void onTransferStarted();
	}

	public interface OnReceiveActivateCallback
	{
		void onReceiveActivate();
	}

	public interface OnReceiveDeactivateCallback
	{
		void onReceiveDeactivate();
	}

	public interface OnDataReceivedCallback
	{
		void onDataReceived(BluetoothDevice device);
	}

	public interface OnNoDataCallback
	{
		void onNoData();
	}

	public interface OnSaveFinishedCallback
	{
		void onSavedFinished();
	}

	public void RegisterOnDeviceFoundCallback(OnDeviceFoundCallback callback)
	{
		m_deviceFoundCallback = callback;
	}

	public void RegisterOnTimerStoppedCallback(OnTimerStoppedCallback callback)
	{
		m_timerStoppedCallback = callback;
	}

	public void RegisterOnOpenBluetoothCallback(OnOpenBluetoothCallback callback)
	{
		m_openBluetoothCallback = callback;
	}

	public void RegisterOnTransferStartedCallback(OnTransferStartedCallback callback)
	{
		m_transferStartedCallback = callback;
	}

	public void RegisterOnReceiveActivateCallback(OnReceiveActivateCallback callback)
	{
		m_receiveActivateCallback = callback;
	}

	public void RegisterOnReceiveDeactivateCallback(OnReceiveDeactivateCallback callback)
	{
		m_receiveDeactivateCallback = callback;
	}

	public void RegisterOnDataReceivedCallback(OnDataReceivedCallback callback)
	{
		m_dataReceivedCallback = callback;
	}

	public void RegisterOnNoDataCallback(OnNoDataCallback callback)
	{
		m_noDataCallback = callback;
	}

	public void RegisterOnSaveFinishedCallback(OnSaveFinishedCallback callback)
	{
		m_onSaveFinishedCallback  = callback;
	}

	////////////////////////////////////////////////////////////////
	// Members of CalendarViewModel-class
	///////////////////////////////////////////////////////////////
	private Activity                m_activity;

	private CalendarModel           m_calendarModel;
	private BluetoothSender         m_sender;
	private BluetoothReceiver       m_receiver;

	private List<Event>     m_SendEvents;
	private List<Event>     m_receiveEvents;

	private OnDeviceFoundCallback       m_deviceFoundCallback;
	private OnTimerStoppedCallback      m_timerStoppedCallback;
	private OnOpenBluetoothCallback     m_openBluetoothCallback;
	private OnTransferStartedCallback   m_transferStartedCallback;
	private OnReceiveActivateCallback   m_receiveActivateCallback;
	private OnReceiveDeactivateCallback m_receiveDeactivateCallback;
	private OnDataReceivedCallback      m_dataReceivedCallback;
	private OnNoDataCallback			m_noDataCallback;
	private OnSaveFinishedCallback      m_onSaveFinishedCallback;

	public static final String[] EVENT_PROJECTION = new String[]
			{
					CalendarContract.Events.TITLE,                   	// 0
					CalendarContract.Events.EVENT_LOCATION,	         	// 1
					CalendarContract.Events.DESCRIPTION,	         	// 2
					CalendarContract.Events.EVENT_COLOR,		     	// 3
					CalendarContract.Events.EVENT_TIMEZONE,		     	// 4
					CalendarContract.Events.ALL_DAY,                 	// 5
					CalendarContract.Events.DTSTART,                 	// 6
					CalendarContract.Events.DTEND,                   	// 7
					CalendarContract.Events.DURATION,                	// 8
					CalendarContract.Events.RRULE,			         	// 9
					CalendarContract.Events.LAST_DATE,               	// 10
					CalendarContract.Events.ACCESS_LEVEL,		     	// 11
					CalendarContract.Events.AVAILABILITY,		     	// 12
					CalendarContract.Events.GUESTS_CAN_MODIFY,		 	// 13
					CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS,	// 14
					CalendarContract.Events.GUESTS_CAN_SEE_GUESTS,	 	// 15
					CalendarContract.Events.CUSTOM_APP_PACKAGE,	     	// 16
					CalendarContract.Events.CUSTOM_APP_URI,		     	// 17
					CalendarContract.Events.UID_2445,                	// 18
					CalendarContract.Events._ID,                     	// 19

					CalendarContract.Events.CALENDAR_COLOR,			 // 20
			};

	////////////////////////////////////////////////////////////////
	// Constructor of CalendarViewModel-class
	///////////////////////////////////////////////////////////////

	public CalendarViewModel(CalendarModel model, Activity activity)
	{
		m_calendarModel = model;
		m_activity      = activity;

		m_SendEvents = new ArrayList<>();

		m_sender   = new BluetoothSender(m_activity);
		m_receiver = new BluetoothReceiver(m_activity);

		Init();
	}

	//Register the ViewModel to changes in the Model
	private void Init()
	{
		m_calendarModel.RegisterOnSaveFinishedEvent(new CalendarModel.OnSaveFinishedCallback()
		{
			@Override
			public void onSaveFinished()
			{
				m_onSaveFinishedCallback.onSavedFinished();
			}
		});

		m_sender.RegisterOnDeviceFoundCallback(new BluetoothSender.OnDeviceFoundCallback()
		{
			@Override
			public void onDeviceFoundInSender(BluetoothDevice foundDevice)
			{
				m_deviceFoundCallback.onDeviceFound(foundDevice);
			}
		});

		m_sender.RegisterOnSearchTimerStoppedCallback(new BluetoothSender.OnSearchTimerStoppedCallback()
		{
			@Override
			public void onSearchTimerStopped()
			{
				m_timerStoppedCallback.onTimerStopped();
			}
		});

		m_sender.RegisterOnTransferStartedCallback(new BluetoothSender.OnTransferStartedCallback()
		{
			@Override
			public void onTransferStarted()
			{
				m_transferStartedCallback.onTransferStarted();
			}
		});

		m_receiver.RegisterOnReceiveActivateCallback(new BluetoothReceiver.OnReceiveActivateCallback()
		{
			@Override
			public void onReceiveActivate()
			{
				m_receiveActivateCallback.onReceiveActivate();
			}
		});

		m_receiver.RegisterOnReceiveDeactivateCallback(new BluetoothReceiver.OnReceiveDeactivateCallback()
		{
			@Override
			public void onReceiveDeactivate()
			{
				m_receiveDeactivateCallback.onReceiveDeactivate();
			}
		});

		m_receiver.RegisterOnDataReceivedCallback(new BluetoothReceiver.OnDataReceivedCallback()
		{
			@Override
			public void onDataReceived(List<Event> events, BluetoothDevice device)
			{
				m_receiveEvents = events;
				m_dataReceivedCallback.onDataReceived(device);
			}
		});
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of CalendarViewModel-class
	///////////////////////////////////////////////////////////////

	public ArrayList<ArrayList<Event>> GetEventsForInterval(int[] calendarIds, Date startInterval, Date endInterval)
	{
		List<Calendar> calendars = m_calendarModel.GetCalendarsById(calendarIds);

		ArrayList<ArrayList<Event>> events = new ArrayList<>();

		//fill eventList with lists
		for(int day = 0; day < 42; ++day)
		{
			events.add( new ArrayList<Event>());
		}

		//Get events for all calendars
		for(Calendar calendar : calendars)
		{
			Date date = new Date(startInterval.getTime());
			int  index = 0;

			while (date.getTime() <= endInterval.getTime())
			{
				//correct time because of possible time change in summer and winter
				if(date.getHours() == 23)
				{
					date.setTime(date.getTime() + 60* 60 * 1000);
				}
				else if(date.getHours() == 1)
				{
					date.setHours(0);
				}

				//Set start of search interval to current day
				long startMillis = (date.getTime() + 1000);
				//Set end of search interval to next day
				long endMillis = startMillis + ((24 * 60 * 60 * 1000) - 2000);

				ContentResolver cr = m_activity.getContentResolver();

				// The ID of the recurring event whose instances you are searching
				String selection = CalendarContract.Instances.CALENDAR_ID + " = ?";
				String[] selectionArgs = new String[]{Integer.toString(calendar.GetId())};

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
						if (eventCursor.getString(0) != null && !eventCursor.getString(0).isEmpty())
						{
							Boolean addEvent = true;
							Event event = new Event(eventCursor, false, true);

							if (index > 0)
							{
								//weekly, monthly, yearly AllDay events only lasts one day
								if(event.IsRepeated() && event.IsAllDay() && !event.IsDaily())
								{
									//check if same event was displayed yesterday
									if (index > 1 && event.IsAllDay())
									{
										for (Event lastDayEvent : events.get(index - 2))
										{
											if (lastDayEvent.GetId().equals(event.GetId()))
											{
												addEvent = false;
											}
										}
									}
								}
								//allDay events ans repeated Daily AllDay events last to their lastDate
								else if(event.IsAllDay())
								{
									if(event.IsOver(date))
									{
										addEvent = false;
									}
								}
								if(addEvent)
								{
									events.get(index - 1).add(event);
								}
							}
						}
					}
					eventCursor.close();
				}

				//Go to the next day
				date.setTime(date.getTime() + (24 * 60 * 60 * 1000));
				++index;
			}
		}
		return events;
	}

	public int GetCurrentDay()
	{
		java.util.Calendar cal = java.util.Calendar.getInstance();

		return cal.get(java.util.Calendar.DAY_OF_MONTH);
	}

	public CalendarDay GetDayOfMonth(int year, CalendarMonth month, int day)
	{
		java.util.Calendar cal = new GregorianCalendar();
		cal.set(java.util.Calendar.MONTH, month.getID() - 1);
		cal.set(java.util.Calendar.YEAR, year);
		cal.set(java.util.Calendar.DAY_OF_MONTH, day);

		switch (cal.get(java.util.Calendar.DAY_OF_WEEK))
		{
			case java.util.Calendar.SUNDAY:
				return CalendarDay.SUNDAY;
			case java.util.Calendar.MONDAY:
				return CalendarDay.MONDAY;
			case java.util.Calendar.TUESDAY:
				return CalendarDay.TUESDAY;
			case java.util.Calendar.WEDNESDAY:
				return CalendarDay.WEDNESDAY;
			case java.util.Calendar.THURSDAY:
				return CalendarDay.THURSDAY;
			case java.util.Calendar.FRIDAY:
				return CalendarDay.FRIDAY;
			case java.util.Calendar.SATURDAY:
				return CalendarDay.SATURDAY;
		}
		return CalendarDay.NONE;
	}

	public List<Calendar> GetCalendars()
	{
		return m_calendarModel.GetCalendars();
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of CalendarViewModel-class for Backup
	///////////////////////////////////////////////////////////////

	public List<String> GetBackupFiles()
	{
		List<String> filenames = new ArrayList<>();

		File[] backupFiles = m_activity.getFilesDir().listFiles();

		for(File backupFile : backupFiles)
		{
			if (backupFile.getName().contains("Backup"))
			{
				filenames.add(backupFile.getName());
			}
		}
		return filenames;
	}

	public void LoadBackup(String filename)
	{
		m_calendarModel.LoadBackup(filename);
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of CalendarViewModel-class for Receiving
	///////////////////////////////////////////////////////////////

	public void ActivateBluetoothForReceiving()
	{
		m_receiver.ToggleBluetooth();
	}

	public void SaveEvents(int calendarId, Boolean doBackup)
	{
		m_calendarModel.SaveReceivedData(calendarId, m_receiveEvents, doBackup);
	}

	////////////////////////////////////////////////////////////////
	// Methods of CalendarViewModel-class for Sending
	///////////////////////////////////////////////////////////////

	public void GetSingleEventForSending(Event event, Date date)
	{
		m_SendEvents.clear();

		m_SendEvents = m_calendarModel.GetSingleEvent(event, date);

		if (IsDataEmpty())
		{
			m_noDataCallback.onNoData();
		}
		else
		{
			m_openBluetoothCallback.onOpenBluetooth(event.GetColorCode(), event.GetTitle(), event.GetTime());
		}
	}

	public void GetEventRangeForSending(int calendarId, Date startDate, Date endDate)
	{
		m_SendEvents.clear();

		// Get the information for the view
		Calendar calendar = m_calendarModel.GetCalendarById(calendarId);

		m_SendEvents = m_calendarModel.GetEventRange(calendarId, startDate, endDate);

		if (IsDataEmpty())
		{
			m_noDataCallback.onNoData();
		}
		else
		{
			m_openBluetoothCallback.onOpenBluetooth(calendar.GetColor(), calendar.GetTitle(), calendar.GetAccountName());
		}
	}

	public void GetAllEventsForSending(int calendarId)
	{
		m_SendEvents.clear();

		// Get the information for the view
		Calendar calendar = m_calendarModel.GetCalendarById(calendarId);

		m_SendEvents = m_calendarModel.GetAllEvents(calendarId);

		if (IsDataEmpty())
		{
			m_noDataCallback.onNoData();
		}
		else
		{
			m_openBluetoothCallback.onOpenBluetooth(calendar.GetColor(), calendar.GetTitle(), calendar.GetAccountName());
		}
	}

	public void SearchForDevices()
	{
		m_sender.ActivateBluetoothAndSearch();
	}

	public void ConnectToDevice(BluetoothDevice device)
	{
		m_sender.Connect(device, m_SendEvents);
	}

	////////////////////////////////////////////////////////////////
	// Private Methods of CalendarViewModel-class
	///////////////////////////////////////////////////////////////

	private Boolean IsDataEmpty()
	{
		if(m_SendEvents.size() == 0)
		{
			return true;
		}
		return false;
	}

}

