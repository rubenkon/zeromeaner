package org.zeromeaner.knet;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import javax.swing.event.EventListenerList;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryonet.Client;
import org.kryomq.kryonet.Connection;
import org.kryomq.kryonet.KryoSerialization;
import org.kryomq.kryonet.Listener;
import org.kryomq.mq.Control;
import org.kryomq.mq.Message;
import org.kryomq.mq.MessageListener;
import org.kryomq.mq.Meta;
import org.kryomq.mq.MqClient;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetClient implements MessageListener {
	private class KNetMqClient extends MqClient {
		private KNetMqClient(String host, int port) {
			super(host, port);
		}

		@Override
		public void disconnected(Connection connection) {
			if(source != null)
				issue(source.event(DISCONNECTED, true));
		}

		@Override
		public void received(Connection connection, Object object) {
			super.received(connection, object);
			if(object instanceof Control)
				controlled((Control) object);
			if(object instanceof Meta)
				metad((Meta) object);
		}
	}

	protected String type;
	protected String host;
	protected int port;

	protected Kryo kryo;
	protected MqClient client;

	protected KNetEventSource source;

	protected EventListenerList listenerList = new EventListenerList();

	public KNetClient(String host, int port) {
		this("Unknown", host, port);
	}

	public KNetClient(String type, String host, int port) {
		this.type = type;
		this.host = host;
		this.port = port;
		KNetKryo.configure(kryo = new Kryo());
		client = new KNetMqClient(host, port);
	}

	public KNetClient start() throws IOException, InterruptedException {
		client.start();
		source = new KNetEventSource(client.getPersonalTopic(), client.getPersonalId());
		source.setType(type);
		source.setName(type + source.getTopic());
		issue(source.event(CONNECTED, true));
		
		client.subscribe(client.getPersonalTopic(), this);
		client.subscribe(KNetTopics.GLOBAL, this);
		client.subscribe(KNetTopics.CONNECTION, this);
		
		return this;
	}

	public void stop() throws IOException {
		client.stop();
	}

	@Override
	public void messageReceived(Message message) {
		Object obj = message.get(kryo);
		if(!(obj instanceof KNetEvent))
			return;
		received((KNetEvent) obj);
	}
	
	protected void controlled(Control control) {
		
	}
	
	protected void metad(Meta meta) {
		
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
		System.err.println(Arrays.asList(args));
		fireTCP(event(args));
	}

	public void fireTCP(KNetEvent e) {
		System.err.println(e);
		e = process(e);
		e.getArgs().remove(UDP);
		issue(e);
		e.getSource();
		Message m = new Message(e.getTopic(), true).set(kryo, e);
		client.send(m);
	}

	public void fireUDP(Object... args) {
		fireUDP(event(args));
	}

	public void fireUDP(KNetEvent e) {
		e = process(e);
		e.getArgs().put(UDP, true);
		issue(e);
		Message m = new Message(e.getTopic(), false).set(kryo, e);
		client.send(m);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
