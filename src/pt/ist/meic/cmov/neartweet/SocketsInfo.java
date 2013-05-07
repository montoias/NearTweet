package pt.ist.meic.cmov.neartweet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocket;
import android.util.Log;

public class SocketsInfo {

	private OutputStream outputStream;
	private SimWifiP2pSocket socket;
	private ObjectOutputStream oos;

	public SocketsInfo(SimWifiP2pSocket socket) {
		this.setSocket(socket);
		try {
			this.setOutputStream(socket.getOutputStream());
			this.oos = new ObjectOutputStream(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("Paulo", "couldn't create the socket");
		}
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	public SimWifiP2pSocket getSocket() {
		return socket;
	}
	public void setSocket(SimWifiP2pSocket socket) {
		this.socket = socket;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public void setOos(ObjectOutputStream oos) {
		this.oos = oos;
	}

	
	
}
