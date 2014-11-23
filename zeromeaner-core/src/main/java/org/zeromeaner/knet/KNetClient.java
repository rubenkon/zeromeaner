package org.zeromeaner.knet;

import static org.zeromeaner.knet.KNetEventArgs.ADDRESS;
import static org.zeromeaner.knet.KNetEventArgs.CONNECTED;
import static org.zeromeaner.knet.KNetEventArgs.DISCONNECTED;
import static org.zeromeaner.knet.KNetEventArgs.IN_REPLY_TO;
import static org.zeromeaner.knet.KNetEventArgs.UDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.event.EventListenerList;

import org.mmmq.Message;
import org.mmmq.MessageListener;
import org.mmmq.Topic;
import org.mmmq.io.MasterClient;
import org.mmmq.io.MessagePacket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;

public class KNetClient implements MessageListener, KNetListener {
	private class KNetMasterClient extends MasterClient {
		private KNetMasterClient() {
		}

		@Override
		public void disconnected(Connection connection) {
			if(source != null)
				issue(source.event(DISCONNECTED, true));
		}

		@Override
		public void received(Connection connection, Object object) {
			super.received(connection, object);
//			if(object instanceof Control)
//				controlled((Control) object);
//			if(object instanceof Meta)
//				metad((Meta) object);
		}
	}

	protected String type;
	protected String host;
	protected int port;

	protected Kryo sendKryo;
	protected Kryo recvKryo;
	protected MasterClient client;

	protected KNetEventSource source;

	protected Topic origin;
	
	protected EventListenerList listenerList = new EventListenerList();

	public KNetClient(String host, int port) {
		this("Unknown", host, port);
	}

	public KNetClient(String type, String host, int port) {
		this.type = type;
		this.host = host;
		this.port = port;
		KNetKryo.configure(sendKryo = new Kryo());
		KNetKryo.configure(recvKryo = new Kryo());
		client = new KNetMasterClient();
		addKNetListener(this);
	}

	public KNetClient start() throws IOException, InterruptedException {
		client.start();
		client.connect(5000, host, port, port);
		source = new KNetEventSource((origin = client.getDirectTopic()).toString(), client.getID());
		source.setType(type);
		source.setName(type + source.getTopic());
		
		client.subscribe(client.getDirectTopic(), this);
		client.subscribe(new Topic(KNetTopics.GLOBAL), this);
		client.subscribe(new Topic(KNetTopics.CONNECTION), this);
		
		issue(source.event(CONNECTED, true));
		return this;
	}

	public void stop() throws IOException {
		client.stop();
	}

	@Override
	public synchronized void messageReceived(Message message) {
		
		Object obj;
		synchronized(recvKryo) {
			obj = recvKryo.readClassAndObject(new Input(message.message()));
		}
		if(!(obj instanceof KNetEvent))
			return;
		received((KNetEvent) obj);
	}
	
	protected void received(KNetEvent e) {
		issue(e);
	}

	protected void issue(KNetEvent e) {
		try {
			Object[] ll = listenerList.getListenerList();
			for(int i = ll.length - 2; i >= 0; i -= 2) {
				if(ll[i] == KNetListener.class) {
					((KNetListener) ll[i+1]).knetEvented(this, e);
				}
			}
		} catch(RuntimeException re) {
			re.printStackTrace();
			throw re;
		} catch(Error er) {
			er.printStackTrace();
			throw er;
		}
	}

	protected KNetEvent process(KNetEvent e) {
		return e;
	}

	public KNetEventSource getSource() {
		return source;
	}

	public KNetEvent event(Object... args) {
		return getSource().event(args);
	}

	public void addKNetListener(KNetListener l) {
		listenerList.add(KNetListener.class, l);
	}

	public void removeKNetListener(KNetListener l) {
		listenerList.remove(KNetListener.class, l);
	}

	public boolean isExternal(KNetEvent e) {
		return !getSource().equals(e.getSource());
	}

	public boolean isLocal(KNetEvent e) {
		return getSource().equals(e.getSource());
	}

	public boolean isMine(KNetEvent e) {
		return !isLocal(e) && !e.is(ADDRESS) || getSource().equals(e.get(ADDRESS));
	}

	public void reply(KNetEvent e, Object... args) {
		KNetEvent resp = event(args);
		resp.set(ADDRESS, e.getSource());
		resp.set(IN_REPLY_TO, e);
		fire(resp);
	}

	public void fire(Object... args) {
		fire(event(args));
	}

	public void fire(KNetEvent e) {
		if(e.is(UDP))
			fireUDP(e);
		else
			fireTCP(e);
	}

	public void fireTCP(Object... args) {
		fireTCP(event(args));
	}

	public void fireTCP(KNetEvent e) {
		e = process(e);
		e.getArgs().remove(UDP);
		issue(e);
		e.getSource();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output out = new Output(bout);
		synchronized(sendKryo) {
			sendKryo.writeClassAndObject(out, e);
		}
		out.flush();
		Message m = (Message) new MessagePacket(origin, new Topic(e.getTopic())).withMessage(bout.toByteArray()).tcp();
		client.sendMessage(m);
	}

	public void fireUDP(Object... args) {
		fireUDP(event(args));
	}

	public void fireUDP(KNetEvent e) {
		e = process(e);
		e.getArgs().put(UDP, true);
		issue(e);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output out = new Output(bout);
		synchronized(sendKryo) {
			sendKryo.writeClassAndObject(out, e);
		}
		out.flush();
		Message m = (Message) new MessagePacket(origin, new Topic(e.getTopic())).withMessage(bout.toByteArray()).udp();
		client.sendMessage(m);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		// TODO Auto-generated method stub
		
	}
}
