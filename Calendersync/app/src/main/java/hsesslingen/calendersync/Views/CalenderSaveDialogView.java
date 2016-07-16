package hsesslingen.calendersync.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hsesslingen.calendersync.R;
import hsesslingen.calendersync.adapter.BluetoothDevicesListViewAdapter;
import hsesslingen.calendersync.guibackend.Calendar;

public class CalenderSaveDialogView extends RelativeLayout
{

	private final List<Calendar> m_Calendars;
	private TextView m_ReceiveTextView;
	private TextView m_SyncTextView;
	private TextView m_OwnerTextView;
	private Spinner m_CalendarSpinner;
	private CheckBox m_CheckBox;

	public CalenderSaveDialogView(Context context, List<Calendar> calendars)
	{
		super(context);
		m_Calendars = calendars;
		createLayout();
	}

	public CalenderSaveDialogView(Context context, AttributeSet attrs, List<Calendar> calendars)
	{
		super(context, attrs);
		m_Calendars = calendars;
		createLayout();
	}

	public CalenderSaveDialogView(Context context, AttributeSet attrs, int defStyleAttr, List<Calendar> calendars)
	{
		super(context, attrs, defStyleAttr);
		m_Calendars = calendars;
		createLayout();
	}

	private void createLayout()
	{
		inflate(getContext(), R.layout.calendar_save_dialog, this);
		m_ReceiveTextView = (TextView) findViewById(R.id.calender_save_dialog_receive);
		m_SyncTextView = (TextView) findViewById(R.id.calender_save_dialog_sync);
		m_OwnerTextView = (TextView) findViewById(R.id.calender_save_dialog_owner);
		m_CalendarSpinner = (Spinner) findViewById(R.id.calender_save_dialog_spinner);
		m_CheckBox = (CheckBox) findViewById(R.id.calender_save_dialog_checkbox);
		m_CheckBox.setVisibility(VISIBLE);
		m_OwnerTextView.setVisibility(VISIBLE);

		List<String> calendarDropdownList = new ArrayList<>();
		for(Calendar c : m_Calendars)
		{
			calendarDropdownList.add(c.GetTitle());
		}
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, calendarDropdownList);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		m_CalendarSpinner.setAdapter(spinnerArrayAdapter);
	}

	public void setTextReceive(String text)
	{
		m_ReceiveTextView.setText(text);
	}

	public void setTextSync(String text)
	{
		m_SyncTextView.setText(text);
	}

	public void setTextOwner(String text)
	{
		m_OwnerTextView.setText(text);
	}

	public int getSelectedCalendarID()
	{
		return m_Calendars.get(m_CalendarSpinner.getSelectedItemPosition()).GetId();
	}

	public boolean getCheckedState()
	{
		return m_CheckBox.isChecked();
	}
}
