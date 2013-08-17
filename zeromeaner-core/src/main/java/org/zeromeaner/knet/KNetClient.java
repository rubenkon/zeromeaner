package org.zeromeaner.knet;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import javax.swing.event.EventListenerList;

import org.zeromeaner.mq.Control;
import org.zeromeaner.mq.Message;
import org.zeromeaner.mq.MessageListener;
import org.zeromeaner.mq.MqClient;
import org.zeromeaner.mq.ObjectMqClient;
import org.zeromeaner.mq.Topics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetClient implements MessageListener {
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
		client = new MqClient(host, port) {
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
			}
		};
	}

	public KNetClient start() throws IOException, InterruptedException {
		client.start();
		source = new KNetEventSource(client.getPersonalTopic(), client.getPersonalId());
		source.setType(type);
		source.setName(type + source.getTopic());
		issue(source.event(CONNECTED, true));
		
		client.subscribe(client.getPersonalTopic(), this);
		client.subscribe(Topics.GLOBAL, this);
		
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
