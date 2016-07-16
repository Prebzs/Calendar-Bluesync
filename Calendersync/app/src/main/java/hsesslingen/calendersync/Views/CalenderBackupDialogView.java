package hsesslingen.calendersync.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import hsesslingen.calendersync.R;

public class CalenderBackupDialogView extends RelativeLayout
{

	private final List<String> m_CalendarBackups;
	private TextView m_ReceiveTextView;
	private TextView m_SyncTextView;
	private TextView m_OwnerTextView;
	private Spinner m_CalendarSpinner;

	public CalenderBackupDialogView(Context context, List<String> calendarBackups)
	{
		super(context);
		m_CalendarBackups = calendarBackups;
		createLayout();
	}

	public CalenderBackupDialogView(Context context, AttributeSet attrs, List<String> calendarBackups)
	{
		super(context, attrs);
		m_CalendarBackups = calendarBackups;
		createLayout();
	}

	public CalenderBackupDialogView(Context context, AttributeSet attrs, int defStyleAttr, List<String> calendarBackups)
	{
		super(context, attrs, defStyleAttr);
		m_CalendarBackups = calendarBackups;
		createLayout();
	}

	private void createLayout()
	{
		inflate(getContext(), R.layout.calendar_save_dialog, this);
		m_ReceiveTextView = (TextView) findViewById(R.id.calender_save_dialog_receive);
		m_SyncTextView = (TextView) findViewById(R.id.calender_save_dialog_sync);
		m_OwnerTextView = (TextView) findViewById(R.id.calender_save_dialog_owner);
		m_CalendarSpinner = (Spinner) findViewById(R.id.calender_save_dialog_spinner);

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, m_CalendarBackups);
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

	public String getSelectedCalendarBackup()
	{
		return m_CalendarBackups.get(m_CalendarSpinner.getSelectedItemPosition());
	}
}
