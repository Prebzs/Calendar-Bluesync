package hsesslingen.calendersync.Fragments;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import hsesslingen.calendersync.Views.CalendarMonthView;
import hsesslingen.calendersync.activities.CalendarActivity;
import hsesslingen.calendersync.backend.CalendarViewModel;
import hsesslingen.calendersync.backend.CalendarViewModelFactory;
import hsesslingen.calendersync.guibackend.Event;
import hsesslingen.calendersync.enums.CalendarDay;
import hsesslingen.calendersync.enums.CalendarMonth;

/**
 * A placeholder fragment containing a simple view.
 */
public class CalendarMonthFragment extends Fragment
{
	private final static String TAG = "CalendarMonthFragment";
	private final static int FIRST_DAY = 1;

	private final CalendarViewModel m_CalendarViewModel;

	private static SparseArray<CalendarMonthView> m_Table = new SparseArray<>();

	private static CalendarActivity.OnSwipeHandlingCallback m_Callback;

	public CalendarMonthFragment()
	{
		m_CalendarViewModel = CalendarViewModelFactory.GetCalendarViewModel();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		return createCalendarMonth();
	}

	private LinearLayout createCalendarMonth()
	{
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;

		TableLayout layout = new TableLayout(getContext())
		{
			@Override
			public boolean onInterceptTouchEvent(MotionEvent ev)
			{
				Log.d(TAG, "Swipe");
				m_Callback.OnSwipeHandlingCallback(ev);
				return false;
			}
		};
		TableLayout.LayoutParams lp = new TableLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
		);
		lp.width = screenWidth;

		layout.setLayoutParams(lp);
		layout.setStretchAllColumns(true);

		TableLayout.LayoutParams rowHeading = new TableLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
		);
		lp.width = screenWidth;

		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT,
				1.0f
		);
		rowLp.width = screenWidth;

		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT,
				1.0f
		);
		cellLp.width = screenWidth / 7;

		CalendarDay[] tableHeadText = CalendarDay.values();
		TableRow tableHeadRow = new TableRow(getContext());
		for (int day = 0; day < 7; ++day)
		{
			TextView tableHeadEntry = new TextView(getContext());
			tableHeadEntry.setText(tableHeadText[day + 1].getShortcut());
			tableHeadEntry.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
			tableHeadRow.addView(tableHeadEntry);
		}
		layout.addView(tableHeadRow, rowHeading);

		int index = 0;
		for (int week = 0; week < 6; ++week)
		{
			TableRow layoutWeek = new TableRow(getContext());
			for (int day = 0; day < 7; ++day, ++index)
			{
				CalendarMonthView layoutDay = new CalendarMonthView(getContext());
				m_Table.append(index, layoutDay);
				layoutWeek.addView(layoutDay, cellLp);
			}
			layout.addView(layoutWeek, rowLp);
		}
		return layout;
	}

	public void updateFragment(int year, CalendarMonth month)
	{
		CalendarDay firstDay = m_CalendarViewModel.GetDayOfMonth(year, month, FIRST_DAY);
		if (firstDay == CalendarDay.NONE)
		{
			Log.e(TAG, "could not get first Day in month");
			return;
		}
		int offset = firstDay.getID() - 1;

		int index = 0;
		CalendarMonth previousMonth = CalendarMonth.getPreviousMonth(month, year);
		int firstDateYear = (previousMonth == CalendarMonth.DECEMBER) ? year - 1 : year;
		Date firstDate = new Date(firstDateYear - 1900, previousMonth.getID() - 1, previousMonth.getLength() - offset);

		CalendarMonth nextMonth = CalendarMonth.getNextMonth(month, year);
		int lastDateYear = (nextMonth == CalendarMonth.JANUARY) ? year + 1 : year;
		Date lastDate = new Date(lastDateYear - 1900, nextMonth.getID() - 1, m_Table.size() - offset - month.getLength());

		ArrayList<ArrayList<Event>> events = m_CalendarViewModel.GetEventsForInterval(CalendarActivity.getActiveCalenderIDs(), firstDate, lastDate);

		while (index < offset && index < m_Table.size())
		{
			int day = previousMonth.getLength() + (index + 1) - offset;
			updateCalendarMonthView(m_Table.valueAt(index), year, previousMonth, day, events.get(index));
			m_Table.valueAt(index).dimDay();
			++index;
		}
		for (int day = 1; index < offset + month.getLength() && index < m_Table.size(); ++day, ++index)
		{
			updateCalendarMonthView(m_Table.valueAt(index), year, month, day, events.get(index));
		}
		for (int day = 1; index < m_Table.size() && index < events.size(); ++day, ++index)
		{
			updateCalendarMonthView(m_Table.valueAt(index), year, nextMonth, day, events.get(index));
			m_Table.valueAt(index).dimDay();
		}
		if (CalendarActivity.isCurrentMonth())
		{
			index = offset + m_CalendarViewModel.GetCurrentDay() - 1;
			m_Table.valueAt(index).markAsCurrentDay();
		}
	}

	public void registerOnSwipeHandlingCallback(CalendarActivity.OnSwipeHandlingCallback callback)
	{
		m_Callback = callback;
	}

	private void updateCalendarMonthView(final CalendarMonthView view, final int year, final CalendarMonth month, final int day, final List<Event> events)
	{
		view.update(day, events);

		if (events == null)
		{
			return;
		}

		final CalendarDay calendarDay = m_CalendarViewModel.GetDayOfMonth(year, month, day);
		view.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				Log.d("CalenderMonthView", "onClick event");
				view.openDayEventViewFragment(year, month, calendarDay, events);
				return true;
			}
		});
	}
}
