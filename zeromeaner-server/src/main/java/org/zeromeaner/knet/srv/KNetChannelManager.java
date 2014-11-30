package org.zeromeaner.knet.srv;

import static org.zeromeaner.knet.KNetEventArgs.AUTOSTART;
import static org.zeromeaner.knet.KNetEventArgs.AUTOSTART_BEGIN;
import static org.zeromeaner.knet.KNetEventArgs.AUTOSTART_STOP;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_CREATE;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_DELETE;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_ID;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_INFO;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_JOIN;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_LEAVE;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_LIST;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_SPECTATE;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_UPDATE;
import static org.zeromeaner.knet.KNetEventArgs.DEAD;
import static org.zeromeaner.knet.KNetEventArgs.DEAD_PLACE;
import static org.zeromeaner.knet.KNetEventArgs.DISCONNECTED;
import static org.zeromeaner.knet.KNetEventArgs.ERROR;
import static org.zeromeaner.knet.KNetEventArgs.FINISH;
import static org.zeromeaner.knet.KNetEventArgs.FINISH_WINNER;
import static org.zeromeaner.knet.KNetEventArgs.GAME_ENDING;
import static org.zeromeaner.knet.KNetEventArgs.PAYLOAD;
import static org.zeromeaner.knet.KNetEventArgs.PLAYER_ENTER;
import static org.zeromeaner.knet.KNetEventArgs.READY;
import static org.zeromeaner.knet.KNetEventArgs.START;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.mmmq.Topic;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.KNetTopics;
import org.zeromeaner.knet.obj.KNStartInfo;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetPlayerInfo;

public class KNetChannelManager extends KNetClient implements KNetListener {
	private static final Logger log = Logger.getLogger(KNetChannelManager.class);
	
	protected class ChannelState {
		protected KNetChannelInfo channel;
		protected Set<KNetEventSource> requiredAutostartResponses = new HashSet<KNetEventSource>();
		protected Set<KNetEventSource> living = new HashSet<KNetEventSource>();
		
		public ChannelState(KNetChannelInfo channel) {
			this.channel = channel;
		}
	}
	
	protected Map<Integer, KNetChannelInfo> channels = Collections.synchronizedMap(new TreeMap<Integer, KNetChannelInfo>());
	protected Map<KNetChannelInfo, ChannelState> states = new HashMap<KNetChannelInfo, ChannelState>();
	protected AtomicInteger nextChannelId = new AtomicInteger(-1);
	protected KNetChannelInfo lobby;
	
	public KNetChannelManager(int port) {
		this("localhost", port);
	}
	
	public KNetChannelManager(String host, int port) {
		super("RoomManager", host, port);
		
		lobby = new KNetChannelInfo(nextChannelId.incrementAndGet(), "lobby");
		
		channels.put(lobby.getId(), lobby);
		states.put(lobby, new ChannelState(lobby));
		
		addKNetListener(this);
	}

	@Override
	public KNetChannelManager start() throws IOException, InterruptedException {
		super.start();
		
		client.claimOwnership(new Topic(KNetTopics.CHANNEL));
		client.subscribe(new Topic(KNetTopics.CHANNEL), this);
		client.subscribe(new Topic(lobby.getTopic()), this);
//		client.setOrigin(KNetTopics.CHANNEL);
		origin = new Topic(KNetTopics.CHANNEL);
		
		return this;
	}
	
	public List<KNetEventSource> getMembers(int channelId) {
		List<KNetEventSource> ret = new ArrayList<KNetEventSource>();
		if(channels.containsKey(channelId))
			ret.addAll(channels.get(channelId).getMembers());
		return ret;
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(client.isLocal(e))
			return;
//		System.out.println(e);
		try {
			if(e.is(CHANNEL_LIST)) {
				client.reply(e,
						CHANNEL_LIST,
						CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
			}
			if(e.is(CHANNEL_JOIN) && e.is(CHANNEL_ID)) {
				int id = (Integer) e.get(CHANNEL_ID);
				if(!channels.containsKey(id)) {
					client.reply(e, ERROR, "Unknown channel id " + id);
					return;
				}
				KNetChannelInfo info = channels.get(id);
				if(info.getMembers().contains(e.getSource())) {
					boolean alreadyJoined = false;
					if(e.is(CHANNEL_SPECTATE) && !info.getPlayers().contains(e.getSource()))
						alreadyJoined = true;
					else if(!e.is(CHANNEL_SPECTATE) && info.getPlayers().contains(e.getSource()))
						alreadyJoined = true;
					if(alreadyJoined) {
						client.reply(e, ERROR, "Already joined channel with id " + id);
						return;
					}
				}
				join(info, e);
			}
			if(e.is(CHANNEL_CREATE)) {
				KNetChannelInfo request = (KNetChannelInfo) e.get(CHANNEL_CREATE);
				for(KNetChannelInfo ci : channels.values()) {
					if(request.getName().equals(ci.getName())) {
						client.reply(e, ERROR, "Cannot create duplicate channel named " + request.getName());
						return;
					}
				}
				request.setId(nextChannelId.incrementAndGet());

				this.client.subscribe(new Topic(request.getTopic()), this);
				
				channels.put(request.getId(), request);
				states.put(request, new ChannelState(request));
				client.fireTCP(CHANNEL_LIST, CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
//				reply(e, CHANNEL_JOIN, CHANNEL_ID, request.getId(), PAYLOAD, request.getId());
			}
			if(e.is(CHANNEL_DELETE)) {
				int id = (Integer) e.get(CHANNEL_DELETE);
				KNetChannelInfo info = channels.get(id);
				if(id == KNetChannelInfo.LOBBY_CHANNEL_ID) {
					client.reply(e, ERROR, "Cannot delete lobby");
				} else if(info.getMembers().size() == 0) {
					channels.remove(id);
					states.remove(info);
					client.fireTCP(CHANNEL_LIST, CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
				} else
					client.reply(e, ERROR, "Cannot delete channel with members");
			}
			if(e.is(CHANNEL_LEAVE) && e.is(CHANNEL_ID)) {
				int id = (Integer) e.get(CHANNEL_ID);
				if(!channels.containsKey(id)) {
					client.reply(e, ERROR, "Unknown channel id " + id);
					return;
				}
				KNetChannelInfo info = channels.get(id);
				if(!info.getMembers().contains(e.getSource())) {
					client.reply(e, ERROR, "Not in channel with id " + id);
					return;
				}
				depart(info, e.getSource(), false);
			}
			if(e.is(DISCONNECTED)) {
				for(KNetChannelInfo info : channels.values().toArray(new KNetChannelInfo[channels.size()])) {
					if(!info.getMembers().contains(e.getSource()))
						continue;
					depart(info, e.getSource(), true);
				}
			}
			if(e.is(AUTOSTART) || e.is(READY, Boolean.class)) {
				//			client.fireTCP(START, CHANNEL_ID, e.get(CHANNEL_ID));
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				ChannelState s = states.get(c);
				s.requiredAutostartResponses.remove(e.getSource());
				if(s.requiredAutostartResponses.size() == 0) {
					c.setPlaying(true);
					client.fireTCP(CHANNEL_UPDATE, c);
					KNStartInfo startInfo = new KNStartInfo();
					startInfo.setPlayerCount(c.getPlayers().size());
					startInfo.setSeed(Double.doubleToRawLongBits(Math.random()));
					client.fireTCP(START, startInfo, CHANNEL_ID, c.getId());
					s.living.clear();
					s.living.addAll(c.getPlayers());
				}
			} else if(e.is(READY) && !e.is(READY, Boolean.class)) {
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				ChannelState s = states.get(c);
				s.requiredAutostartResponses.add(e.getSource());
			}
			if(e.is(DEAD)) {
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				dead(c, c.getPlayers().get(e.get(DEAD, Integer.class)));
			}
			if(e.is(GAME_ENDING)) {
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				c.setPlaying(false);
				client.fireTCP(CHANNEL_UPDATE, c);
				if(c.getPlayers().size() >= 2 && c.isAutoStart()) {
					client.fireTCP(AUTOSTART_BEGIN, 10, CHANNEL_ID, c.getId());
					states.get(c).requiredAutostartResponses.addAll(c.getPlayers());
				}
			}
		} catch(Throwable t) {
			log.error(t);
		}
	}
	
	protected void join(KNetChannelInfo channel, KNetEvent e) {
		log.info(e.getSource() + " joining " + channel);
		if(!channel.getMembers().contains(e.getSource())) {
			List<KNetEventSource> members = new ArrayList<KNetEventSource>(channel.getMembers());
			members.add(e.getSource());
			channel.setMembers(members);
//			channel.getMembers().add(e.getSource());
		}
		KNetPlayerInfo newPlayer = null;
		if(channel.getPlayers().size() < channel.getMaxPlayers() && !channel.isPlaying() && !e.is(KNetEventArgs.CHANNEL_SPECTATE)) {
			channel.getPlayers().add(e.getSource());
			newPlayer = new KNetPlayerInfo();
			newPlayer.setChannelId(channel.getId());
			newPlayer.setPlayer(e.getSource());
			newPlayer.setTeam(e.getSource().getName() + e.getSource().getTopic());
			channel.getPlayerInfo().add(newPlayer);
		} else if(e.is(CHANNEL_SPECTATE)) {
			channel.getPlayers().remove(e.getSource());
			ChannelState s = states.get(channel);
			KNetEventSource user = e.getSource();
			boolean isPlayer = channel.getPlayers().contains(user);
			// declare the player dead
			if(isPlayer) {
				fireTCP(DEAD, channel.getPlayers().indexOf(user), CHANNEL_ID, channel.getId(), DEAD_PLACE, channel.getPlayers().size());
				dead(channel, user);
				maybeAutostart(channel);
			}
		}
//		reply(e, 
		fireTCP(
				CHANNEL_JOIN,
				CHANNEL_ID, channel.getId(),
				PAYLOAD, e.getSource(),
				CHANNEL_INFO, new KNetChannelInfo[] { channel });
//		reply(e,
		fireTCP(
//				CHANNEL_ID, channel.getId(),
				CHANNEL_LIST,
				CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
		if(newPlayer != null) {
			fireTCP(PLAYER_ENTER, newPlayer, CHANNEL_ID, channel.getId());
			maybeAutostart(channel);
		}
	}
	
	protected void maybeAutostart(KNetChannelInfo channel) {
		if(channel.getPlayers().size() == channel.getMaxPlayers() && channel.isAutoStart()) {
			log.info("Autostarting " + channel);
			fireTCP(AUTOSTART_BEGIN, 10, CHANNEL_ID, channel.getId());
			states.get(channel).requiredAutostartResponses.addAll(channel.getPlayers());
		} else {
			log.info("Autostopping " + channel);
			fireTCP(AUTOSTART_STOP, CHANNEL_ID, channel.getId());
		}
	}
	
	protected void dead(KNetChannelInfo channel, KNetEventSource user) {
		ChannelState s = states.get(channel);
		s.living.remove(user);
		if(s.living.size() == 1) {
			fireTCP(FINISH, false, FINISH_WINNER, s.living.iterator().next(), CHANNEL_ID, channel.getId());
			channel.setPlaying(false);
			fireTCP(CHANNEL_UPDATE, channel);
			maybeAutostart(channel);
		}
	}
	
	protected void depart(KNetChannelInfo channel, KNetEventSource user, boolean force) {
		ChannelState s = states.get(channel);
		boolean isPlayer = channel.getPlayers().contains(user);
		// declare the player dead
		if(isPlayer) {
			fireTCP(DEAD, channel.getPlayers().indexOf(user), CHANNEL_ID, channel.getId(), DEAD_PLACE, channel.getPlayers().size());
			dead(channel, user);
		}
		if(channel.getId() != KNetChannelInfo.LOBBY_CHANNEL_ID || force)
			channel.depart(user);
		fireTCP(CHANNEL_LEAVE, CHANNEL_ID, channel.getId(), PAYLOAD, user, CHANNEL_INFO, new KNetChannelInfo[] {channel});
		if(isPlayer) {
			maybeAutostart(channel);
		}
		if(channel.getId() != KNetChannelInfo.LOBBY_CHANNEL_ID && channel.getMembers().size() == 0) {
			channels.remove(channel.getId());
			states.remove(channel);
			fireTCP(CHANNEL_LIST, CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
		}
		
	}

}
