package hsesslingen.calendersync.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hsesslingen.calendersync.Fragments.CalendarMonthFragment;
import hsesslingen.calendersync.R;
import hsesslingen.calendersync.adapter.CalendarListViewAdapter;
import hsesslingen.calendersync.backend.CalendarViewModel;
import hsesslingen.calendersync.backend.CalendarViewModelFactory;
import hsesslingen.calendersync.enums.CalendarMonth;
import hsesslingen.calendersync.helper.CalendarDialogHelper;

public class CalendarActivity extends AppCompatActivity
{
	public interface OnSwipeHandlingCallback
	{
		void OnSwipeHandlingCallback(MotionEvent event);
	}

	private static final String TAG = "CalendarActivity";

	private static List<Integer> m_ActiveCalenderIDs = new ArrayList<>();
	private static Fragment m_ActiveFragment;

	private static CalendarMonth m_ActiveMonth = null;
	private static CalendarMonth m_CurrentMonth;
	private static int m_CurrentYear;
	private static int m_Year = 0;
	private static final int MAX_YEAR = 2100;
	private static final int MIN_YEAR = 1900;
	private static final int MIN_SWIPE_DISTANCE = 120;


	private float m_lastX;
	private float m_lastY;

	private TextView m_TextViewMonth;
	private TextView m_TextViewYear;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
		}

		Calendar c = Calendar.getInstance();
		m_CurrentYear = c.get(Calendar.YEAR);
		if(m_Year == 0)
		{
			m_Year = m_CurrentYear;
		}
		m_CurrentMonth = CalendarMonth.getMonth(c.get(Calendar.MONTH) + 1, m_Year);
		if(m_ActiveMonth == null)
		{
			m_ActiveMonth = m_CurrentMonth;
		}
		registerCallbacks();

		CalendarMonthFragment fragment = new CalendarMonthFragment();
		fragment.registerOnSwipeHandlingCallback(new OnSwipeHandlingCallback()
		{
			@Override
			public void OnSwipeHandlingCallback(MotionEvent event)
			{
				handleSwipe(event);
			}
		});
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_placeholder, fragment).commit();
		m_ActiveFragment = fragment;

		List<hsesslingen.calendersync.guibackend.Calendar> calendars = CalendarViewModelFactory.GetCalendarViewModel().GetCalendars();
		for (hsesslingen.calendersync.guibackend.Calendar calendar : calendars)
		{
			int id = calendar.GetId();
			if (!m_ActiveCalenderIDs.contains(id))
			{
				m_ActiveCalenderIDs.add(id);
			}
		}

		m_TextViewMonth = (TextView) findViewById(R.id.date_month);
		m_TextViewYear = (TextView) findViewById(R.id.date_year);

		m_TextViewYear.setText(Integer.toString(m_Year));
		m_TextViewMonth.setText(m_ActiveMonth.getName());

		findViewById(R.id.button_today).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setDateToday();
			}
		});

		((LinearLayout) findViewById(R.id.date_layout)).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				CalendarDialogHelper.openDatePickingDialog(CalendarActivity.this, new CalendarDialogHelper.OnSetDateCallback()
				{
					@Override
					public void onDateSet(int year, int month)
					{
						m_Year = year;
						m_ActiveMonth = CalendarMonth.getMonth(month, m_Year);
						updateFragment();
					}
				});
			}
		});

		((FloatingActionButton) findViewById(R.id.button_sync)).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickSynchronizeCalendar();
			}
		});

		((FloatingActionButton) findViewById(R.id.button_receive)).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				toggleReceive();
			}
		});
		Log.d(TAG, "DEBUG onCreate()");
	}

	private void registerCallbacks()
	{
		CalendarViewModel viewModel = CalendarViewModelFactory.GetCalendarViewModel();
		viewModel.RegisterOnNoDataCallback(new CalendarViewModel.OnNoDataCallback()
		{
			@Override
			public void onNoData()
			{
				showToast("No Events to send");
			}
		});
		viewModel.RegisterOnOpenBluetoothCallback(new CalendarViewModel.OnOpenBluetoothCallback()
		{
			@Override
			public void onOpenBluetooth(int colorKey, String title, String subTitle)
			{
				CalendarDialogHelper.openBluetoothDevicesDialog(CalendarActivity.this, colorKey, title, subTitle);
			}
		});
		viewModel.RegisterOnTransferStartedCallback(new CalendarViewModel.OnTransferStartedCallback()
		{
			@Override
			public void onTransferStarted()
			{
				showToast("Send in progress... Do not close the app");
			}
		});
		viewModel.RegisterOnSaveFinishedCallback(new CalendarViewModel.OnSaveFinishedCallback()
		{
			@Override
			public void onSavedFinished()
			{
				setLoadingScreenVisibility(false);
				updateFragment();
			}
		});
		viewModel.RegisterOnReceiveActivateCallback(new CalendarViewModel.OnReceiveActivateCallback()
		{
			@Override
			public void onReceiveActivate()
			{
				((FloatingActionButton) findViewById(R.id.button_receive)).setImageDrawable(getResources().getDrawable(R.drawable.ic_sync_black_48dp));
			}
		});
		viewModel.RegisterOnReceiveDeactivateCallback(new CalendarViewModel.OnReceiveDeactivateCallback()
		{
			@Override
			public void onReceiveDeactivate()
			{
				(new Handler(Looper.getMainLooper())).post(new Runnable()
				{
					@Override
					public void run()
					{
						((FloatingActionButton) findViewById(R.id.button_receive)).setImageDrawable(getResources().getDrawable(R.drawable.ic_sync_disabled_black_48dp));
					}
				});
			}
		});
		viewModel.RegisterOnDataReceivedCallback(new CalendarViewModel.OnDataReceivedCallback()
		{
			@Override
			public void onDataReceived(final BluetoothDevice device)
			{
				Looper.prepare();
				(new Handler(Looper.getMainLooper())).post(new Runnable()
				{
					@Override
					public void run()
					{
						CalendarDialogHelper.openSaveSyncDialog(CalendarActivity.this, device);
					}
				});
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Log.d(TAG, "DEBUG onStart()");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//CalendarViewModelFactory.GetCalendarViewModel().LoadCalendars();
		updateFragment();
	}

	public void showPopup(View v)
	{
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_calendar, popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId())
				{
					case R.id.menu_sync_calendar:
						onClickSynchronizeCalendar();
						return true;
					case R.id.menu_sync_range:
						int today = CalendarViewModelFactory.GetCalendarViewModel().GetCurrentDay();
						Date todayDate = new Date(m_CurrentYear - 1900, m_CurrentMonth.getID() - 1, today);
						Date tomorrowDate = new Date(m_CurrentYear - 1900, m_CurrentMonth.getID() - 1, today + 1);
						CalendarDialogHelper.openSyncRangeDialog(CalendarActivity.this, todayDate, tomorrowDate);
						return true;
					case R.id.menu_turn_receive_on:
						toggleReceive();
						return true;
					case R.id.menu_choose_calendars:
						CalendarDialogHelper.openCalendarChooserDialog(CalendarActivity.this, true, new CalendarDialogHelper.OnCalendarSelectedCallback()
						{
							@Override
							public void onCalendarSelected(CalendarListViewAdapter adapter)
							{
								m_ActiveCalenderIDs = getSelectedCalendarIDs(adapter);
								updateFragment();
							}
						});
						return true;
					case R.id.menu_load_backup:
						CalendarDialogHelper.openLoadBackupDialog(CalendarActivity.this);
						return true;
					case R.id.menu_today:
						setDateToday();
						return true;
					case R.id.menu_tutorial:
						Intent intent = new Intent(CalendarActivity.this, TutorialActivity.class);
						startActivityForResult(intent, 0);
						return true;
					default:
						return false;
				}
			}
		});

		popup.show();
	}

	private void toggleReceive()
	{
		CalendarViewModelFactory.GetCalendarViewModel().ActivateBluetoothForReceiving();
	}

	private void updateFragment()
	{
		m_TextViewMonth.setText(m_ActiveMonth.getName());
		m_TextViewYear.setText(Integer.toString(m_Year));
		Log.d(TAG, "DEBUG updating Fragment");
		if (m_ActiveFragment instanceof CalendarMonthFragment)
		{
			Log.d(TAG, "DEBUG updating CalendarMonthFragment");

			((CalendarMonthFragment) m_ActiveFragment).updateFragment(m_Year, m_ActiveMonth);
		}
	}

	public static boolean isCurrentMonth()
	{
		return (m_CurrentMonth == m_ActiveMonth && m_CurrentYear == m_Year);
	}

	private void onClickSynchronizeCalendar()
	{
		if (m_ActiveCalenderIDs.size() > 1)
		{
			CalendarDialogHelper.openCalendarChooserDialog(CalendarActivity.this, false, null);
		}
		else
		{
			CalendarViewModelFactory.GetCalendarViewModel().GetAllEventsForSending(m_ActiveCalenderIDs.get(0));
		}
	}

	private void setDateToday()
	{
		m_ActiveMonth = m_CurrentMonth;
		m_Year = m_CurrentYear;
		updateFragment();
	}

	public static int[] getActiveCalenderIDs()
	{
		int[] ids = new int[m_ActiveCalenderIDs.size()];
		int index = 0;
		for (int id : m_ActiveCalenderIDs)
		{
			ids[index] = id;
			index++;
		}
		return ids;
	}

	private List<Integer> getSelectedCalendarIDs(CalendarListViewAdapter adapter)
	{
		List<hsesslingen.calendersync.guibackend.Calendar> calendars = adapter.getCalendars();
		List<Integer> ids = new ArrayList<>();
		for (int position = 0; position < adapter.getCount(); ++position)
		{
			boolean checked = (boolean) adapter.getItem(position);
			if (checked)
			{
				ids.add(calendars.get(position).GetId());
			}
		}
		if (ids.size() == 0)
		{
			return m_ActiveCalenderIDs;
		}
		return ids;
	}

	private void showToast(final String text)
	{
		(new Handler(Looper.getMainLooper())).post(new Runnable()
		{
			@Override
			public void run()
			{
				Toast toast = Toast.makeText(CalendarActivity.this, text, Toast.LENGTH_LONG);
				toast.show();
			}
		});
	}

	private void handleSwipe(MotionEvent event)
	{
		switch (event.getAction())
		{
			//onPress
			case MotionEvent.ACTION_DOWN:
			{
				Log.d(TAG, "motion Down");
				m_lastX = event.getX();
				m_lastY = event.getY();
				break;
			}

			//onRelease
			case MotionEvent.ACTION_UP:
			{
				Log.d(TAG, "motion Up");
				float disX = event.getX() - m_lastX;
				float disY = event.getY() - m_lastY;

				//compare with minimum values for swipeEvent
				if (Math.abs(disX) > Math.abs(disY) && Math.abs(disX) > MIN_SWIPE_DISTANCE)
				{
					//swipe to left side (next screen right)
					if (disX < 0)
					{
						Log.d(TAG, "swipe left");
						if (m_Year >= MAX_YEAR)
						{
							break;
						}

						m_ActiveMonth = CalendarMonth.getNextMonth(m_ActiveMonth, m_Year);
						if (m_ActiveMonth == CalendarMonth.JANUARY)
						{
							++m_Year;
						}
						updateFragment();
					}
					//swipe to right side (next screen left)
					else
					{
						Log.d(TAG, "swipe right");
						if (m_Year <= MIN_YEAR)
						{
							break;
						}

						m_ActiveMonth = CalendarMonth.getPreviousMonth(m_ActiveMonth, m_Year);
						if (m_ActiveMonth == CalendarMonth.DECEMBER)
						{
							--m_Year;
						}
						updateFragment();
					}
				}
				break;
			}
		}
	}

	public static int getMaxYear()
	{
		return MAX_YEAR;
	}

	public static int getMinYear()
	{
		return MIN_YEAR;
	}

	public static int getYear()
	{
		return m_Year;
	}

	public static CalendarMonth getActiveMonth()
	{
		return m_ActiveMonth;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if ( keyCode == KeyEvent.KEYCODE_MENU ) {
			findViewById(R.id.menu_button).performClick();
			return true;
		}

		// let the system handle all other key events
		return super.onKeyDown(keyCode, event);
	}

	public void setLoadingScreenVisibility(boolean visible)
	{
		View v = findViewById(R.id.loading_screen_layout);

		if(v == null)
		{
			Log.e(TAG, "View not found");
			return;
		}

		if(visible)
		{
			v.setVisibility(View.VISIBLE);
		}
		else
		{
			v.setVisibility(View.GONE);
		}
	}
}
