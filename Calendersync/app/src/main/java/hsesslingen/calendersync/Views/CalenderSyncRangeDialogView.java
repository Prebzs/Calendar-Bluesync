package hsesslingen.calendersync.Views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hsesslingen.calendersync.R;
import hsesslingen.calendersync.activities.CalendarActivity;
import hsesslingen.calendersync.enums.CalendarMonth;
import hsesslingen.calendersync.guibackend.Calendar;

public class CalenderSyncRangeDialogView extends RelativeLayout
{
	private final List<Calendar> m_Calendars;
	private TextView m_SyncFromTextView;
	private TextView m_SyncUntilTextView;
	private Date m_SyncFromDate;
	private Date m_SyncUntilDate;
	private Spinner m_CalendarSpinner;
	private Context m_Context;

	public CalenderSyncRangeDialogView(Context context, List<Calendar> calendars)
	{
		super(context);
		m_Context = context;
		m_Calendars = calendars;
		createLayout();
	}

	private void createLayout()
	{
		inflate(getContext(), R.layout.calendar_sync_range_dialog, this);
		m_SyncFromTextView = (TextView) findViewById(R.id.calender_sync_range_from);
		m_SyncUntilTextView = (TextView) findViewById(R.id.calender_sync_range_until);
		m_CalendarSpinner = (Spinner) findViewById(R.id.calender_sync_range_dialog_spinner);

		List<String> calendarDropdownList = new ArrayList<>();
		for(Calendar c : m_Calendars)
		{
			calendarDropdownList.add(c.GetTitle());
		}
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, calendarDropdownList);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		m_CalendarSpinner.setAdapter(spinnerArrayAdapter);

		m_SyncFromTextView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				openDatePickerDialog(m_SyncFromDate, new DatePickerDialog.OnDateSetListener()
				{
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
					{
						Date tempFromDate = new Date(year - 1900, monthOfYear, dayOfMonth);
						if(tempFromDate.after(m_SyncUntilDate))
						{
							showToast();
							return;
						}
						setFromDate(tempFromDate);
					}
				});
			}
		});

		m_SyncUntilTextView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				openDatePickerDialog(m_SyncUntilDate, new DatePickerDialog.OnDateSetListener()
				{
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
					{
						Date tempUntilDate = new Date(year - 1900, monthOfYear, dayOfMonth);
						if(tempUntilDate.before(m_SyncFromDate))
						{
							showToast();
							return;
						}
						setUntilDate(tempUntilDate);
					}
				});
			}
		});
	}

	public int getSelectedCalendarID()
	{
		return m_Calendars.get(m_CalendarSpinner.getSelectedItemPosition()).GetId();
	}

	public Date getSyncFromDate()
	{
		return m_SyncFromDate;
	}

	public Date getSyncUntilDate()
	{
		return m_SyncUntilDate;
	}

	private void openDatePickerDialog(Date date, DatePickerDialog.OnDateSetListener listener)
	{
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
		int year = calendar.get(java.util.Calendar.YEAR);
		int month = calendar.get(java.util.Calendar.MONTH);
		DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener, year, month, day);
		datePickerDialog.updateDate(year, month, day);
		datePickerDialog.show();
	}

	public void setFromDate(Date fromDate)
	{
		m_SyncFromDate = fromDate;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		m_SyncFromTextView.setText(dateFormat.format(m_SyncFromDate));
	}

	public void setUntilDate(Date untilDate)
	{
		m_SyncUntilDate = untilDate;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		m_SyncUntilTextView.setText(dateFormat.format(m_SyncUntilDate));
	}

	private void showToast()
	{
		Toast toast = Toast.makeText(m_Context, "Invalid date range", Toast.LENGTH_LONG);
		toast.show();
	}
}
