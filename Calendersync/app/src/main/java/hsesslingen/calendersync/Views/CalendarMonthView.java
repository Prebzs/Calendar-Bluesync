package hsesslingen.calendersync.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import hsesslingen.calendersync.R;
import hsesslingen.calendersync.adapter.DayEventListViewAdapter;
import hsesslingen.calendersync.guibackend.Event;
import hsesslingen.calendersync.enums.CalendarDay;
import hsesslingen.calendersync.enums.CalendarMonth;
import hsesslingen.calendersync.helper.CalendarDialogHelper;

public class CalendarMonthView extends RelativeLayout
{
    private int m_Day;
    private TextView m_TextViewDay;
    private ImageView m_Dim;
    private final int m_TextViewDayID = View.generateViewId();
    private LinearLayout m_EventListView;
    private static ColorStateList m_DefaultTextColor;
    private static LayoutInflater m_Inflater = null;

    public CalendarMonthView(Context context)
    {
        super(context);
        m_Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        createLayout(context);
    }

    public CalendarMonthView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        m_Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        createLayout(context);
    }

    public CalendarMonthView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        m_Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        createLayout(context);
    }

    private void createLayout(Context context)
    {
        setBorder(1, 0xFF000000);

        int twoDP = (int) (2 * Resources.getSystem().getDisplayMetrics().density);

        m_TextViewDay = new TextView(context);
        m_TextViewDay.setId(m_TextViewDayID);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.leftMargin = twoDP;
        addView(m_TextViewDay, layoutParams);

        m_EventListView = new LinearLayout(context);
        m_EventListView.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams layoutParamsEvent = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParamsEvent.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParamsEvent.addRule(RelativeLayout.ABOVE, m_TextViewDayID);
        layoutParamsEvent.leftMargin = twoDP;
        layoutParamsEvent.topMargin = twoDP;
        layoutParamsEvent.rightMargin = twoDP;

        addView(m_EventListView, layoutParamsEvent);
        m_DefaultTextColor = m_TextViewDay.getTextColors();

        LinearLayout.LayoutParams layoutParamsDim = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        m_Dim = new ImageView(getContext());
        m_Dim.setBackgroundColor(0x5FC0C0C0);
        addView(m_Dim, layoutParamsDim);
    }

    public void update(int day, List<Event> events)
    {
        m_Dim.setVisibility(GONE);
        setBorder(1, 0xFFC0C0C0);
        m_Day = day;
        m_TextViewDay.setText(Integer.toString(m_Day));
        m_TextViewDay.setTextColor(m_DefaultTextColor);
        m_EventListView.removeAllViews();
        if (events == null || events.size() <= 0)
        {
            return;
        }

        LinearLayout.LayoutParams layoutParamsEventItemView = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParamsEventItemView.bottomMargin = 5;
        for (Event eventItem : events)
        {
            TextView eventItemView = new TextView(getContext());
            eventItemView.setMaxLines(1);
            eventItemView.setEllipsize(TextUtils.TruncateAt.END);
            eventItemView.setText(eventItem.GetTitle());
            eventItemView.setBackgroundColor(eventItem.GetColorCode());
            eventItemView.setPadding(8, 0, 8, 0);
            m_EventListView.addView(eventItemView, layoutParamsEventItemView);
        }
    }

    public void dimDay()
    {
        m_Dim.setVisibility(VISIBLE);
    }

    public void markAsCurrentDay()
    {
        setBorder(4, 0xFF42A5F5);
        m_TextViewDay.setTextColor(0xFF42A5F5);
    }

    private void setBorder(int stroke, int color)
    {
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFFFFFFF); //white background
        border.setStroke(stroke, color);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            setBackgroundDrawable(border);
        }
        else
        {
            setBackground(border);
        }
    }

    public void openDayEventViewFragment(int year, CalendarMonth month, CalendarDay day, List<Event> events)
    {
        String title = day.getShortName() + ", " + m_Day + ". " + month.getName() + " " + year;
        Context context = getContext();
        CalendarDialogHelper.openDayEventViewDialog(context, m_Inflater, title, year, month, m_Day, events);
    }
}
