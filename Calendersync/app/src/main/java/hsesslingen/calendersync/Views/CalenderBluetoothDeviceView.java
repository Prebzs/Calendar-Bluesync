package hsesslingen.calendersync.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hsesslingen.calendersync.R;
import hsesslingen.calendersync.adapter.BluetoothDevicesListViewAdapter;

public class CalenderBluetoothDeviceView extends RelativeLayout
{

	private final BluetoothDevicesListViewAdapter m_Adapter;
	private ListView m_BluetoothDevicesListView;
	private LinearLayout m_LoadingLayout;
	private TextView m_NoDevicesFoundTextView;

	public CalenderBluetoothDeviceView(Context context, BluetoothDevicesListViewAdapter adapter)
	{
		super(context);
		m_Adapter = adapter;
		createLayout();
	}

	public CalenderBluetoothDeviceView(Context context, AttributeSet attrs, BluetoothDevicesListViewAdapter adapter)
	{
		super(context, attrs);
		m_Adapter = adapter;
		createLayout();
	}

	public CalenderBluetoothDeviceView(Context context, AttributeSet attrs, int defStyleAttr, BluetoothDevicesListViewAdapter adapter)
	{
		super(context, attrs, defStyleAttr);
		m_Adapter = adapter;
		createLayout();
	}

	private void createLayout()
	{
		inflate(getContext(), R.layout.bluetooth_device_list_dialog, this);
		m_BluetoothDevicesListView = (ListView) findViewById(R.id.bluetooth_devices_listview);
		m_LoadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
		m_NoDevicesFoundTextView = (TextView) findViewById(R.id.no_devices_found_textview);

		m_BluetoothDevicesListView.setAdapter(m_Adapter);


		m_NoDevicesFoundTextView.setVisibility(GONE);
		m_BluetoothDevicesListView.setVisibility(VISIBLE);
		m_LoadingLayout.setVisibility(VISIBLE);
	}

	public void onLoadingStopped()
	{
		if(m_Adapter.getCount() == 0)
		{
			m_NoDevicesFoundTextView.setVisibility(VISIBLE);
			m_BluetoothDevicesListView.setVisibility(GONE);
		}
		m_LoadingLayout.setVisibility(GONE);
	}

	public void onLoadingStarted()
	{
		m_NoDevicesFoundTextView.setVisibility(GONE);
		m_BluetoothDevicesListView.setVisibility(VISIBLE);
		m_LoadingLayout.setVisibility(VISIBLE);
	}
}
