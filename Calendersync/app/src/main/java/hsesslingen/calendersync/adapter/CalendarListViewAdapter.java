package hsesslingen.calendersync.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.util.List;

import hsesslingen.calendersync.R;
import hsesslingen.calendersync.activities.CalendarActivity;
import hsesslingen.calendersync.guibackend.Calendar;

public class CalendarListViewAdapter extends BaseAdapter
{
	public interface OnCalenderSelectedCallback
	{
		void OnCalenderSelected(int calendarID);
	}

	private static final String TAG = "CalendarListViewAdapter";

	private Context m_Context;
	private List<Calendar> m_Calendars;
	private boolean[] m_Checkboxes;
	private static LayoutInflater inflater = null;
	private final boolean m_WithRadioButtons;
	private OnCalenderSelectedCallback m_Callback;

	public CalendarListViewAdapter(Context context, List<Calendar> calendars, boolean withRadioButtons)
	{
		m_Context = context;
		m_Calendars = calendars;
		m_Checkboxes = new boolean[m_Calendars.size()];
		inflater = (LayoutInflater) m_Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_WithRadioButtons = withRadioButtons;
	}

	@Override
	public int getCount()
	{
		return m_Calendars.size();
	}

	@Override
	public Object getItem(int position)
	{
		return m_Checkboxes[position];
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final Calendar calendar = m_Calendars.get(position);
		View view;
		if (m_WithRadioButtons)
		{
			view = inflater.inflate(R.layout.calender_chooser_item, null, false);
			CheckBox checkBox = ((CheckBox) view.findViewById(R.id.calendar_item_radio_button));
			checkBox.setText(calendar.GetTitle());
			checkBox.setTextAppearance(view.getContext(), android.R.style.TextAppearance_Large);
			m_Checkboxes[position] = setChecked(calendar.GetId());
			checkBox.setChecked(m_Checkboxes[position]);

			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					m_Checkboxes[position] = isChecked;
				}
			});
		}
		else
		{
			view = inflater.inflate(R.layout.bluetooth_device_list_item, null, false);
			TextView textView = (TextView) view.findViewById(R.id.device_name_textview);
			textView.setText(calendar.GetTitle());
			textView.setTextAppearance(view.getContext(), android.R.style.TextAppearance_Large);
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(m_Callback == null)
					{
						Log.e(TAG, "callback not set");
						return;
					}
					m_Callback.OnCalenderSelected(calendar.GetId());
				}
			});
		}
		return view;
	}

	public void registerForOnCalenderSelectedCallback(OnCalenderSelectedCallback callback)
	{
		m_Callback = callback;
	}

	public List<Calendar> getCalendars()
	{
		return m_Calendars;
	}

	public boolean setChecked(int calenderID)
	{
		int[] ids = CalendarActivity.getActiveCalenderIDs();
		for(int id : ids)
		{
			if(id == calenderID)
			{
				return true;
			}
		}
		return false;
	}
}
