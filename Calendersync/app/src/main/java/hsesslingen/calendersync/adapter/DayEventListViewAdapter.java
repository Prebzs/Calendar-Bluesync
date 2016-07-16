package hsesslingen.calendersync.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import hsesslingen.calendersync.R;
import hsesslingen.calendersync.backend.CalendarViewModelFactory;
import hsesslingen.calendersync.guibackend.Event;
import hsesslingen.calendersync.enums.CalendarMonth;

public class DayEventListViewAdapter extends BaseAdapter
{
	private Context m_Context;
	private List<Event> m_Events;
	private static LayoutInflater inflater = null;
	private AlertDialog m_Dialog;
	private int m_Year;
	private CalendarMonth m_Month;
	private int m_Day;

	public DayEventListViewAdapter(Context context, List<Event> events, int year, CalendarMonth month, int day)
	{
		m_Context = context;
		m_Events = events;
		inflater = (LayoutInflater) m_Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_Year = year;
		m_Month = month;
		m_Day = day;
	}

	@Override
	public int getCount()
	{
		return m_Events.size();
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final Event event = m_Events.get(position);
		View view = inflater.inflate(R.layout.day_event_list_item, null, false);
		TextView title = ((TextView) view.findViewById(R.id.event_title));
		title.setText(event.GetTitle());
		title.setTypeface(null, Typeface.BOLD);
		title.setTextAppearance(view.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
		((TextView) view.findViewById(R.id.event_time)).setText(event.GetTime());
		view.findViewById(R.id.event_color).setBackgroundColor(event.GetColorCode());
		view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(m_Dialog != null)
				{
					m_Dialog.dismiss();
				}
				CalendarViewModelFactory.GetCalendarViewModel().GetSingleEventForSending(event, new Date(m_Year - 1900, m_Month.getID() - 1, m_Day));
			}
		});
		return view;
	}

	public void setAlertDialog(AlertDialog dialog)
	{
		m_Dialog = dialog;
	}
}
