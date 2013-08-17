package org.zeromeaner.mq;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromeaner.mq.Control.Command;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class MqServer extends Listener {
	private static final Logger log = LoggerFactory.getLogger(MqServer.class);
	
	protected int port;
	
	protected Server server;
	
	protected TopicRegistry<Connection> registry = new TopicRegistry<>();
	protected TopicRegistry<Connection> privileged = new TopicRegistry<>();
	
	protected Map<Connection, String> origins = new ConcurrentHashMap<>();
	
	public MqServer(int port) {
		this.port = port;
			
	}
	
	public int getPort() {
		return port;
	}
	
	public void start() throws IOException {
		log.debug("{} starting server on port {}", this, port);
		server = new Server(1024*256, 1024*256, new KryoSerialization(new MqKryo()));
		server.start();
		server.bind(port, port);
		server.addListener(this);
		log.debug("{} started server on port {}", this, port);
	}
	
	public void stop() throws IOException {
		log.debug("{} stopping server on port {}", this, port);
		server.close();
		server.stop();
	}
	
	protected String personalTopic(Connection connection) {
		return Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
	}
	
	@Override
	public void connected(Connection connection) {
		String personalTopic = personalTopic(connection);
		privileged.subscribe(personalTopic, connection);
		server.sendToTCP(connection.getID(), new Control(Command.PERSONAL_TOPIC, personalTopic));
		origins.put(connection, personalTopic);
	}
	
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Message) {
			Message m = (Message) object;
			log.trace("{} dispatching message {}", this, m);
			m.origin = origins.get(connection);
			Set<Connection> subscribers = registry.get(m.topic);
			for(Connection c : subscribers) {
				if(m.reliable)
					server.sendToTCP(c.getID(), m);
				else
					server.sendToUDP(c.getID(), m);
			}
		}
		if(object instanceof Control) {
			Control c = (Control) object;
			switch(c.command) {
			case SUBSCRIBE:
				if(!c.topic.startsWith(Topics.PRIVILEGED) || privileged.get(c.topic).contains(connection)) {
					log.trace("{} subscribing {} to topic {}", this, connection, c.topic);
					registry.subscribe(c.topic, connection);
				} else {
					log.trace("{} not subscribing {} to privileged topic {}", this, connection, c.topic);
				}
				break;
			case UNSUBSCRIBE:
				log.trace("{} unsubscribing {} from topic {}", this, connection, c.topic);
				registry.unsubscribe(c.topic, connection);
				break;
			case PRIVILEGED_SUBSCRIBE:
				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
					log.trace("{} subscribing privileged {} to topic {}", this, connection, c.topic);
					registry.subscribe(c.topic, connection);
				} else {
					log.trace("{} not subscribing privileged {} to topic {}", this, connection, c.topic);
				}
				break;
			case PRIVILEGED_SET_ORIGIN:
				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
					log.trace("{} setting privileged origin of {} to topic {}", this, connection, c.topic);
					origins.put(connection, c.topic);
				} else {
					log.trace("{} not setting privileged origin of {} to topic {}", this, connection, c.topic);
				}
				break;
			}
		}
	}
}
