package org.zeromeaner.knet;

import java.util.ArrayList;
import java.util.Properties;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.component.SpeedParam;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.ComboRaceMode;
import org.zeromeaner.game.subsystem.mode.DigChallengeMode;
import org.zeromeaner.game.subsystem.mode.DigRaceMode;
import org.zeromeaner.game.subsystem.mode.ExtremeMode;
import org.zeromeaner.game.subsystem.mode.MarathonMode;
import org.zeromeaner.game.subsystem.mode.MarathonPlusMode;
import org.zeromeaner.game.subsystem.mode.NetVSBattleMode;
import org.zeromeaner.game.subsystem.mode.TGMNetVSBattleMode;
import org.zeromeaner.knet.obj.KNStartInfo;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetGameInfo;
import org.zeromeaner.knet.obj.KNetPlayerInfo;
import org.zeromeaner.knet.obj.PieceHold;
import org.zeromeaner.knet.obj.PieceMovement;
import org.zeromeaner.knet.ser.DiffFieldSerializer;
import org.zeromeaner.knet.ser.PropertiesSerializer;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

import com.esotericsoftware.kryo.Kryo;

public class KNetKryo {
	public static Kryo configure(Kryo kryo) {
		kryo.setReferences(false);
		kryo.setAutoReset(true);
		
		kryo.register(String[].class);
		
		kryo.register(KNetEvent.class);
		kryo.register(KNetEventSource.class);
		diffFieldSerializer(kryo, KNetChannelInfo.class);
		kryo.register(KNetChannelInfo[].class);
		diffFieldSerializer(kryo, KNetGameInfo.class);
		kryo.register(KNetGameInfo.TSpinEnableType.class);
		diffFieldSerializer(kryo, KNetPlayerInfo.class);
		diffFieldSerializer(kryo, Field.class);
		kryo.register(Block[][].class);
		kryo.register(Block[].class);
		diffFieldSerializer(kryo, Block.class);
		diffFieldSerializer(kryo, SpeedParam.class);
		kryo.register(boolean[].class);
		kryo.register(ArrayList.class);
		diffFieldSerializer(kryo, Piece.class);
		kryo.register(int[][].class);
		kryo.register(int[].class);
		kryo.register(Piece[].class);
		kryo.register(PieceHold.class);
		kryo.register(PieceMovement.class);
		kryo.register(Properties.class, new PropertiesSerializer());
		kryo.register(CustomProperties.class, new PropertiesSerializer());
		diffFieldSerializer(kryo, Statistics.class);
		diffFieldSerializer(kryo, RuleOptions.class, GeneralUtil.loadRule("config/rule/Standard.rul"));
		kryo.register(KNStartInfo.class);
		
		diffFieldSerializer(kryo, AbstractNetMode.DefaultStats.class);
		diffFieldSerializer(kryo, AbstractNetMode.DefaultOptions.class);
		
		kryo.register(NetVSBattleMode.AttackInfo.class);
		kryo.register(NetVSBattleMode.StatsInfo.class);
		diffFieldSerializer(kryo, NetVSBattleMode.EndGameStats.class);
		
		kryo.register(TGMNetVSBattleMode.TGMAttackInfo.class);
		
		diffFieldSerializer(kryo, ComboRaceMode.Stats.class);
		diffFieldSerializer(kryo, ComboRaceMode.Options.class);
		
		diffFieldSerializer(kryo, DigChallengeMode.Stats.class);
		diffFieldSerializer(kryo, DigChallengeMode.Options.class);
		
		diffFieldSerializer(kryo, DigRaceMode.Stats.class);
		diffFieldSerializer(kryo, DigRaceMode.EndGameStats.class);
		diffFieldSerializer(kryo, DigRaceMode.Options.class);
		
		diffFieldSerializer(kryo, ExtremeMode.Stats.class);
		diffFieldSerializer(kryo, ExtremeMode.Options.class);

		diffFieldSerializer(kryo, MarathonMode.Options.class);
		diffFieldSerializer(kryo, MarathonPlusMode.Stats.class);
		diffFieldSerializer(kryo, MarathonPlusMode.Options.class);
		
		return kryo;
	}
	
	private static <T> void diffFieldSerializer(Kryo kryo, Class<T> clazz, T typical) {
		kryo.register(clazz, new DiffFieldSerializer<T>(kryo, clazz, typical));
	}
	
	private static <T> void diffFieldSerializer(Kryo kryo, Class<T> clazz) {
		diffFieldSerializer(kryo, clazz, typical(clazz));
	}
	
	private static <T> T typical(final Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch(Exception ex) {
			if(ex instanceof RuntimeException)
				throw (RuntimeException) ex;
			throw new RuntimeException(ex);
		}
	}
	
}
