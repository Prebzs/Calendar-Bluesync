package hsesslingen.calendersync.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;

import hsesslingen.calendersync.guibackend.Event;

public class BluetoothReceiver
{
	////////////////////////////////////////////////////////////////
	// Interfaces and register methods of BluetoothReceiver-class
	///////////////////////////////////////////////////////////////

	public interface OnReceiveActivateCallback
	{
		void onReceiveActivate();
	}

	public interface OnReceiveDeactivateCallback
	{
		void onReceiveDeactivate();
	}

	public interface OnDataReceivedCallback
	{
		void onDataReceived(List<Event> events, BluetoothDevice device);
	}

	public void RegisterOnReceiveActivateCallback(OnReceiveActivateCallback callback)
	{
		m_receiveActivateCallback = callback;
	}

	public void RegisterOnReceiveDeactivateCallback(OnReceiveDeactivateCallback callback)
	{
		m_receiveDeactivateCallback = callback;
	}

	public void RegisterOnDataReceivedCallback(OnDataReceivedCallback callback)
	{
		m_dataReceivedCallback = callback;
	}

	////////////////////////////////////////////////////////////////
	// Members of BluetoothReceiver-class
	///////////////////////////////////////////////////////////////

	private Activity m_activity;

	private BluetoothAdapter m_bluetoothAdapter;

	private Boolean m_isRegistered	  = false;
    private Boolean m_wasEnabled      = false;
    private Boolean m_nextCallEnables = true;

	private OnReceiveActivateCallback   m_receiveActivateCallback;
	private OnReceiveDeactivateCallback m_receiveDeactivateCallback;
	private OnDataReceivedCallback      m_dataReceivedCallback;

	private final BroadcastReceiver m_broadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action))
			{
				int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

				switch(mode)
				{
					case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE: //Bluetooth visible mode was activated
					{
						m_receiveActivateCallback.onReceiveActivate();
						m_nextCallEnables = false;
						break;
					}
					case BluetoothAdapter.SCAN_MODE_CONNECTABLE: //Bluetooth visible mode was deactivated
					{
						m_receiveDeactivateCallback.onReceiveDeactivate();
						m_nextCallEnables = true;
						break;
					}
				}
			}
		}
	};

	////////////////////////////////////////////////////////////////
	// Constructor of BluetoothReceiver-class
	///////////////////////////////////////////////////////////////

    public BluetoothReceiver(Activity activity)
	{
		m_activity = activity;

		if (IsBluetoothAvailable())
		{
			if (m_bluetoothAdapter.isEnabled())
			{
				m_wasEnabled = true;
			}
		}
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of BluetoothReceiver-class
	///////////////////////////////////////////////////////////////

	public void ToggleBluetooth()
	{
		//check if bluetooth is available for this device
		if (IsBluetoothAvailable())
		{
			if (m_bluetoothAdapter.isEnabled())
			{
				if(m_nextCallEnables)
				{
					MakeDeviceVisible();
					Connect();
				}
				else
				{
					m_nextCallEnables = true;
					m_receiveDeactivateCallback.onReceiveDeactivate();

					//Disable Bluetooth if it was not activated on app start
					if(!m_wasEnabled)
					{
						m_bluetoothAdapter.disable();
					}
				}
			}
			else
			{
				while (!m_bluetoothAdapter.isEnabled())
				{
					m_bluetoothAdapter.enable();
				}

				MakeDeviceVisible();
				Connect();
			}
		}
	}

    public void MakeDeviceVisible()
	{
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
		m_activity.startActivity(discoverableIntent);

		if (!m_isRegistered)
		{
			m_activity.registerReceiver(m_broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
			m_isRegistered = true;
		}
	}

    ////////////////////////////////////////////////////////////////
    // Private Methods of BluetoothReceiver-class
    ///////////////////////////////////////////////////////////////

	private Boolean IsBluetoothAvailable()
    {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if(adapter == null)
        {
            //Device does not support Bluetooth
            return false;
        }
        else
        {
            //Device does support Bluetooth
            m_bluetoothAdapter = adapter;
            return true;
        }
    }

	private void Connect()
	{
		AcceptThread thread = new AcceptThread(m_bluetoothAdapter);
		thread.start();
	}

	////////////////////////////////////////////////////////////////
	//inner Thread-class for connecting to devices
	///////////////////////////////////////////////////////////////
	private class AcceptThread extends Thread
	{
		private final BluetoothServerSocket m_serverSocket;
		private final BluetoothAdapter      m_adapter;

		private final String NAME = "BluetoothServer";
		private final UUID m_uuid = new UUID(1,1);

		public AcceptThread(BluetoothAdapter adapter)
		{
			// Use a temporary object that is later assigned to m_serverSocket,
			// because m_serverSocket is final
			BluetoothServerSocket tempSocket = null;
			m_adapter = adapter;

			try
			{
				// m_uuid is the app's UUID string, also used by the client code
				tempSocket = m_adapter.listenUsingRfcommWithServiceRecord(NAME, m_uuid);
			}
			catch (IOException e)
			{
				Log.e("BluetoothReceiver:", "Couldn't create the BluetoothSocket.");

			}

			m_serverSocket = tempSocket;
		}

		public void run()
		{
			BluetoothSocket socket;

			// Keep listening until exception occurs or a socket is returned
			while (true)
			{
				try
				{
					socket = m_serverSocket.accept();
				}
				catch (IOException e)
				{
					break;
				}

				// If a connection was accepted
				if (socket != null)
				{
					manageConnectedSocket(socket);
				}
			}
		}

		private void manageConnectedSocket(BluetoothSocket socket)
		{
			ReceivingThread thread = new ReceivingThread(socket);
			thread.start();
		}
	}

	private class ReceivingThread extends Thread
	{
		private final BluetoothSocket   m_serverSocket;
		private final ObjectInputStream m_input;

		public ReceivingThread(BluetoothSocket socket)
		{
			m_serverSocket = socket;
			ObjectInputStream tmpIn = null;

			try
			{
				tmpIn = new ObjectInputStream(socket.getInputStream());
			}
			catch (IOException e)
			{
				Log.e("BluetoothReceiver:", "Couldn't create the ObjectInputStream.");

			}

			m_input = tmpIn;
		}

		public void run()
		{
			while (true)
			{
				try
				{
					//receive the events from the other side
					List<Event> events = (List<Event>) m_input.readObject();

					BluetoothDevice device = m_serverSocket.getRemoteDevice();

					m_dataReceivedCallback.onDataReceived(events, device);

					m_serverSocket.close();

					if (m_isRegistered)
					{
						m_activity.unregisterReceiver(m_broadcastReceiver);
						m_isRegistered = false;
					}

					m_receiveDeactivateCallback.onReceiveDeactivate();
					m_nextCallEnables = true;
				}
				catch (ClassNotFoundException e)
				{
					Log.e("BluetoothReceiver:", "ClassNotFound while reading data from ObjectInputStream.");

				}
				catch (IOException e)
				{
					Log.e("BluetoothReceiver:", "Couldn't read data from ObjectInputStream.");
				}

				break;
			}
		}
	}
}
