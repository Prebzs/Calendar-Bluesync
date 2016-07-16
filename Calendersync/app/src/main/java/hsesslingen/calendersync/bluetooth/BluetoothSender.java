package hsesslingen.calendersync.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import hsesslingen.calendersync.guibackend.Event;

////////////////////////////////////////////////////////////////
// BluetoothSender-class for sending data to remote device
///////////////////////////////////////////////////////////////

public class BluetoothSender
{
    ////////////////////////////////////////////////////////////////
    // Interfaces and register Methods of BluetoothSender-class
    ///////////////////////////////////////////////////////////////

    public interface OnDeviceFoundCallback
    {
        void onDeviceFoundInSender(BluetoothDevice foundDevice);
    }

    public interface OnSearchTimerStoppedCallback
    {
        void onSearchTimerStopped();
    }

	public interface OnTransferStartedCallback
	{
		void onTransferStarted();
	}

    public void RegisterOnDeviceFoundCallback(OnDeviceFoundCallback callback)
    {
        m_deviceFoundCallback = callback;
    }

    public void RegisterOnSearchTimerStoppedCallback(OnSearchTimerStoppedCallback callback)
    {
        m_searchedStoppedCallback = callback;
    }

	public void RegisterOnTransferStartedCallback(OnTransferStartedCallback callback)
	{
		m_transferStartedCallback = callback;
	}

    ////////////////////////////////////////////////////////////////
    // Members of BluetoothSender-class
    ///////////////////////////////////////////////////////////////

	private Boolean				  m_isRegistered = false;

	private Activity			  m_activity;
	private BluetoothAdapter      m_bluetoothAdapter;

    private ConnectThread         m_connectThread;

    private OnDeviceFoundCallback        m_deviceFoundCallback;
    private OnSearchTimerStoppedCallback m_searchedStoppedCallback;
	private OnTransferStartedCallback    m_transferStartedCallback;

	private final BroadcastReceiver m_broadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				m_deviceFoundCallback.onDeviceFoundInSender(device);
			}
		}
	};

    ////////////////////////////////////////////////////////////////
    // Constructor of BluetoothSender-class
    ///////////////////////////////////////////////////////////////

	public BluetoothSender(Activity activity)
	{
		m_activity = activity;
	}

	////////////////////////////////////////////////////////////////
	// Public Methods of BluetoothSender-class
	///////////////////////////////////////////////////////////////

    public void ActivateBluetoothAndSearch()
    {
        //Check whether device supports Bluetooth or not
        if(IsBluetoothAvailable())
        {
            //Only search if search is not in progress at the moment
            if (!m_bluetoothAdapter.isEnabled())
            {
                m_bluetoothAdapter.enable();
            }

            SearchForDevices();

            (new Handler(Looper.getMainLooper())).postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    StopDiscovery();
                    m_searchedStoppedCallback.onSearchTimerStopped();
                }
            }, 30000);
        }
    }

	public void Connect(BluetoothDevice remoteDevice, List<Event> events)
	{
		m_connectThread = new ConnectThread(remoteDevice, m_bluetoothAdapter, events);
		m_connectThread.start();
	}

    ////////////////////////////////////////////////////////////////
    // Private Methods of BluetoothSender-class
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

	private void SearchForDevices() {
		if (!m_bluetoothAdapter.isDiscovering())
		{
			//search for new devices and store them into a list of found devices
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			if(!m_isRegistered)
			{
				m_activity.registerReceiver(m_broadcastReceiver, filter);
				m_isRegistered = true;
			}
			m_bluetoothAdapter.startDiscovery();
		}
	}

	private void StopDiscovery()
	{
		m_bluetoothAdapter.cancelDiscovery();
	}

	////////////////////////////////////////////////////////////////
	//inner Thread-class for connecting to devices and sending data
	///////////////////////////////////////////////////////////////

    private class ConnectThread extends Thread
	{
        private final BluetoothSocket     m_clientSocket;
        private final BluetoothAdapter    m_adapter;
		private final List<Event> m_events;

        private final UUID m_uuid = new UUID(1,1);

        public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, List<Event> events)
		{
			// Use a temporary object that is later assigned to m_clientSocket,
            // because m_clientSocket is final
            BluetoothSocket tempSocket = null;
			m_adapter = adapter;
			m_events  = events;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try
			{
                // m_uuid is the app's UUID string, also used by the server code
                tempSocket = device.createRfcommSocketToServiceRecord(m_uuid);
            }
			catch (IOException e)
			{
				Log.e("BluetoothSender:", "Couldn't create the BluetoothSocket.");
			}
			m_clientSocket = tempSocket;
        }

        public void run()
		{
            // Cancel discovery because it will slow down the connection
			m_adapter.cancelDiscovery();

            try
			{
				m_clientSocket.connect();
            }
			catch (IOException connectException)
			{
                // Unable to connect; close the socket and get out
                try
				{
					m_clientSocket.close();
                }
				catch (IOException closeException)
				{
					Log.e("BluetoothSender:", "Couldn't close the BluetoothSocket.");
				}
				return;
            }
			manageConnectedSocket(m_clientSocket);
        }

		private void manageConnectedSocket(BluetoothSocket socket)
		{
			SendingThread thread = new SendingThread(socket, m_events);
			thread.start();
		}
    }

	private class SendingThread extends Thread
	{
		private final BluetoothSocket	  m_clientSocket;
		private final ObjectOutputStream  m_output;
		private final List<Event> m_events;

		public SendingThread(BluetoothSocket socket, List<Event> events)
		{
			ObjectOutputStream tempOut = null;
			m_events = events;
			m_clientSocket = socket;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try
			{
				tempOut = new ObjectOutputStream(socket.getOutputStream());
			}
			catch (IOException e)
			{
				Log.e("BluetoothSender:", "Couldn't create the ObjectOutputStream.");
			}

			m_output = tempOut;
		}

		public void run()
		{
			// Keep listening to the InputStream until an exception occurs
			while (true)
			{
				try
				{
					//callback to show transfer-started toast
					m_transferStartedCallback.onTransferStarted();

					//write the events to the outputStream
					m_output.writeObject(m_events);

					if(m_isRegistered)
					{
						m_activity.unregisterReceiver(m_broadcastReceiver);
						m_isRegistered = false;
					}

					m_clientSocket.close();
					break;
				}
				catch (IOException e)
				{
					break;
				}
			}
		}
	}
}

