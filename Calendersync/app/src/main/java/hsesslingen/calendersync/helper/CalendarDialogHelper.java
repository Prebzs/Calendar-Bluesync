package hsesslingen.calendersync.helper;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import hsesslingen.calendersync.R;
import hsesslingen.calendersync.Views.CalenderBackupDialogView;
import hsesslingen.calendersync.Views.CalenderBluetoothDeviceView;
import hsesslingen.calendersync.Views.CalenderSaveDialogView;
import hsesslingen.calendersync.Views.CalenderSyncRangeDialogView;
import hsesslingen.calendersync.activities.CalendarActivity;
import hsesslingen.calendersync.adapter.BluetoothDevicesListViewAdapter;
import hsesslingen.calendersync.adapter.CalendarListViewAdapter;
import hsesslingen.calendersync.adapter.DayEventListViewAdapter;
import hsesslingen.calendersync.backend.CalendarViewModel;
import hsesslingen.calendersync.backend.CalendarViewModelFactory;
import hsesslingen.calendersync.enums.CalendarMonth;
import hsesslingen.calendersync.guibackend.Event;

public class CalendarDialogHelper
{
	public interface OnSetDateCallback
	{
		void onDateSet(int year, int month);
	}

	public interface OnCalendarSelectedCallback
	{
		void onCalendarSelected(CalendarListViewAdapter adapter);
	}

	private final static String TAG = "CalendarDialogHelper";
	private static AlertDialog m_AlertDialog;

	public static void openDatePickingDialog(Activity activity, final OnSetDateCallback callback)
	{
		LayoutInflater inflater = activity.getLayoutInflater();
		View view = inflater.inflate(R.layout.costum_date_picker, null, false);

		final NumberPicker monthSpinner = (NumberPicker) view.findViewById(R.id.month_spinner);
		monthSpinner.setMinValue(CalendarMonth.JANUARY.getID());
		monthSpinner.setMaxValue(CalendarMonth.DECEMBER.getID());
		monthSpinner.setDisplayedValues(CalendarMonth.getMonthNames());
		monthSpinner.setValue(CalendarActivity.getActiveMonth().getID());

		final NumberPicker yearSpinner = (NumberPicker) view.findViewById(R.id.year_spinner);
		yearSpinner.setMinValue(CalendarActivity.getMinYear());
		yearSpinner.setMaxValue(CalendarActivity.getMaxYear());
		yearSpinner.setValue(CalendarActivity.getYear());

		View title = inflater.inflate(R.layout.dialog_default_header, null, false);
		((TextView) title.findViewById(R.id.dialog_default_title)).setText("Choose Date");

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCustomTitle(title)
				.setView(view)
				.setCancelable(true)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						Log.d(TAG, "Setting new Date");
						callback.onDateSet(yearSpinner.getValue(), monthSpinner.getValue());
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						Log.d(TAG, "Date Choosing canceled");
					}
				});
		builder.create().show();
	}

	public static void openLoadBackupDialog(final CalendarActivity activity)
	{
		final CalendarViewModel viewModel = CalendarViewModelFactory.GetCalendarViewModel();

		final CalenderBackupDialogView saveDialog = new CalenderBackupDialogView(activity, viewModel.GetBackupFiles());

		LayoutInflater inflater = activity.getLayoutInflater();
		View title = inflater.inflate(R.layout.dialog_default_header, null, false);
		((TextView) title.findViewById(R.id.dialog_default_title)).setText("Backup");

		saveDialog.setTextReceive("Load backup prior to last change");
		saveDialog.setTextSync("select calendar");

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCustomTitle(title)
				.setView(saveDialog)
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						String filename = saveDialog.getSelectedCalendarBackup();
						Log.d(TAG, "Load Backup: " + filename);
						activity.setLoadingScreenVisibility(true);
						viewModel.LoadBackup(filename);
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	public static void openSaveSyncDialog(final CalendarActivity activity, BluetoothDevice device)
	{
		final CalendarViewModel viewModel = CalendarViewModelFactory.GetCalendarViewModel();

		final CalenderSaveDialogView saveDialog = new CalenderSaveDialogView(activity, viewModel.GetCalendars());

		LayoutInflater inflater = activity.getLayoutInflater();
		View title = inflater.inflate(R.layout.dialog_default_header, null, false);
		((TextView) title.findViewById(R.id.dialog_default_title)).setText("Incoming calendar");

		saveDialog.setTextReceive("Received a calendar from");
		saveDialog.setTextOwner(device.getName());
		saveDialog.setTextSync("Do you want to sync it with your device?");

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCustomTitle(title)
				.setView(saveDialog)
				.setCancelable(true)
				.setPositiveButton("Accept", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						int calendarID = saveDialog.getSelectedCalendarID();
						Log.d(TAG, "CalenderID: " + calendarID);
						activity.setLoadingScreenVisibility(true);
						viewModel.SaveEvents(calendarID, saveDialog.getCheckedState());
					}
				})
				.setNegativeButton("Deny", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}


	public static void openBluetoothDevicesDialog(final Activity activity, int colorKey, String title, String subTitle)
	{
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View titleView = inflater.inflate(R.layout.bluetooth_dialog_titel, null, false);
		((TextView) titleView.findViewById(R.id.bluetooth_dialog_title)).setText(title);
		((TextView) titleView.findViewById(R.id.bluetooth_dialog_second_title)).setText(subTitle);
		titleView.findViewById(R.id.dialog_title_color).setBackgroundColor(colorKey);

		final CalendarViewModel viewModel = CalendarViewModelFactory.GetCalendarViewModel();

		final BluetoothDevicesListViewAdapter bluetoothDevicesListViewAdapter = new BluetoothDevicesListViewAdapter(activity, new BluetoothDevicesListViewAdapter.OnDeviceSelectedCallback()
		{
			@Override
			public void onDeviceSelected(BluetoothDevice foundDevice)
			{
				viewModel.ConnectToDevice(foundDevice);
				if(m_AlertDialog != null)
				{
					m_AlertDialog.dismiss();
					m_AlertDialog = null;
				}
			}
		});
		final CalenderBluetoothDeviceView calenderBluetoothDeviceView = new CalenderBluetoothDeviceView(activity, bluetoothDevicesListViewAdapter);

		viewModel.RegisterOnTimerStoppedCallback(new CalendarViewModel.OnTimerStoppedCallback()
		{
			@Override
			public void onTimerStopped()
			{
				calenderBluetoothDeviceView.onLoadingStopped();
				final ImageView view = (ImageView) titleView.findViewById(R.id.bluetooth_imageview);
				view.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_search_black_48dp));
				view.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						view.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_bluetooth_searching_black_48dp));
						view.setOnClickListener(null);
						calenderBluetoothDeviceView.onLoadingStarted();
						bluetoothDevicesListViewAdapter.clearItems();
						viewModel.SearchForDevices();
					}
				});
			}
		});

		viewModel.RegisterOnDeviceFoundCallback(new CalendarViewModel.OnDeviceFoundCallback()
		{
			@Override
			public void onDeviceFound(BluetoothDevice foundDevice)
			{
				bluetoothDevicesListViewAdapter.addItem(foundDevice);
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCustomTitle(titleView)
				.setView(calenderBluetoothDeviceView)
				.setCancelable(true);
		m_AlertDialog = builder.create();
		m_AlertDialog.show();

		viewModel.SearchForDevices();
	}


	public static void openCalendarChooserDialog(Activity activity, boolean withCheckboxes, final OnCalendarSelectedCallback callback)
	{
		final CalendarViewModel viewModel = CalendarViewModelFactory.GetCalendarViewModel();

		ListView listView = new ListView(activity);
		final CalendarListViewAdapter adapter = new CalendarListViewAdapter(activity, viewModel.GetCalendars(), withCheckboxes);
		listView.setAdapter(adapter);

		LayoutInflater inflater = activity.getLayoutInflater();
		View title = inflater.inflate(R.layout.dialog_default_header, null, false);
		((TextView) title.findViewById(R.id.dialog_default_title)).setText("Choose a calendar");

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCustomTitle(title)
				.setView(listView)
				.setCancelable(true);

		if (withCheckboxes)
		{
			builder.setPositiveButton("show Calendars", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					callback.onCalendarSelected(adapter);
				}
			})
					.setNegativeButton("cancel", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
		}
		final AlertDialog alertDialog = builder.create();

		adapter.registerForOnCalenderSelectedCallback(new CalendarListViewAdapter.OnCalenderSelectedCallback()
		{
			@Override
			public void OnCalenderSelected(int calendarID)
			{
				viewModel.GetAllEventsForSending(calendarID);
				alertDialog.dismiss();
			}
		});

		alertDialog.show();
	}

	public static void openSyncRangeDialog(Activity activity, Date fromDate, Date untilDate)
	{
		final CalendarViewModel viewModel = CalendarViewModelFactory.GetCalendarViewModel();

		final CalenderSyncRangeDialogView syncRangeDialogView = new CalenderSyncRangeDialogView(activity, viewModel.GetCalendars());

		LayoutInflater inflater = activity.getLayoutInflater();
		View title = inflater.inflate(R.layout.dialog_default_header, null, false);
		((TextView) title.findViewById(R.id.dialog_default_title)).setText("Synce date range");

		syncRangeDialogView.setFromDate(fromDate);
		syncRangeDialogView.setUntilDate(untilDate);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCustomTitle(title)
				.setView(syncRangeDialogView)
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						int calendarID = syncRangeDialogView.getSelectedCalendarID();
						Date from = syncRangeDialogView.getSyncFromDate();
						Date until = syncRangeDialogView.getSyncUntilDate();
						Log.d(TAG, "CalenderID: " + calendarID + " from: " + from + " until: " + until);
						viewModel.GetEventRangeForSending(calendarID, from, until);
						dialog.dismiss();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	public static void openDayEventViewDialog(Context context, LayoutInflater inflater, String title, int year, CalendarMonth month, int day, List<Event> events)
	{
		final View view;
		DayEventListViewAdapter adapter = new DayEventListViewAdapter(context, events, year, month, day);
		if (events.size() > 0)
		{
			ListView listView = new ListView(context);
			listView.setAdapter(adapter);
			view = listView;
		}
		else
		{
			TextView textView = new TextView(context);
			textView.setText("No Events for " + title);
			textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
			view = textView;
			view.setPadding(0, 20, 0, 0);
		}
		View titleView = inflater.inflate(R.layout.dialog_default_header, null, false);
		((TextView) titleView.findViewById(R.id.dialog_default_title)).setText(title);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCustomTitle(titleView)
				.setView(view)
				.setCancelable(true);
		AlertDialog dialog = builder.create();
		adapter.setAlertDialog(dialog);
		dialog.show();
	}
}