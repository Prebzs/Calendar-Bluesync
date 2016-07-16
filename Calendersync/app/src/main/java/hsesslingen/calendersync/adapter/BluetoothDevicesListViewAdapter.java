package hsesslingen.calendersync.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hsesslingen.calendersync.R;

public class BluetoothDevicesListViewAdapter extends BaseAdapter
{

	public interface OnDeviceSelectedCallback
	{
		void onDeviceSelected(BluetoothDevice foundDevice);
	}

	private Context m_Context;
	private List<BluetoothDevice> m_BluetoothDevices;
	private static LayoutInflater inflater = null;
	private OnDeviceSelectedCallback m_Callback;

	public BluetoothDevicesListViewAdapter(Context context, OnDeviceSelectedCallback callback)
	{
		m_Context = context;
		m_BluetoothDevices = new ArrayList<>();
		inflater = (LayoutInflater) m_Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_Callback = callback;
	}

	@Override
	public int getCount()
	{
		return m_BluetoothDevices.size();
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
		final BluetoothDevice device = m_BluetoothDevices.get(position);
		View view = inflater.inflate(R.layout.bluetooth_device_list_item, null, false);
		TextView textView = (TextView) view.findViewById(R.id.device_name_textview);
		textView.setText(device.getName());
		textView.setTextAppearance(view.getContext(), android.R.style.TextAppearance_Large);
		view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				m_Callback.onDeviceSelected(device);
			}
		});
		return view;
	}

	public void addItem(BluetoothDevice device)
	{
		if(m_BluetoothDevices.contains(device))
		{
			return;
		}
		m_BluetoothDevices.add(device);
		notifyDataSetChanged();
	}

	public void clearItems()
	{
		m_BluetoothDevices.clear();
		notifyDataSetChanged();
	}
}
