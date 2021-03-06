package org.zeromeaner.util;

import static org.zeromeaner.util.PropertyConstant.BOOLEAN;
import static org.zeromeaner.util.PropertyConstant.DOUBLE;
import static org.zeromeaner.util.PropertyConstant.INTEGER;
import static org.zeromeaner.util.PropertyConstant.STRING;

import org.zeromeaner.util.PropertyConstant.Constant;
import org.zeromeaner.util.io.PropertyStore;

public class Options {
	public static final CustomProperties ROOT_PROPERTIES = new CustomProperties();
	public static final CustomProperties GLOBAL_PROPERTIES = new CustomProperties(ROOT_PROPERTIES, "zeromeaner");
	public static final CustomProperties GUI_PROPERTIES = new CustomProperties(GLOBAL_PROPERTIES, "gui");
	public static final CustomProperties RUNTIME_PROPERTIES = new CustomProperties(GUI_PROPERTIES, "runtime");
	
	
	public static GeneralOptions general() {
		return new GeneralOptions();
	}

	public static PlayerOptions player(int playerId) {
		return new PlayerOptions(playerId);
	}
	
	public static ModeOptions mode(String modeName) {
		return new ModeOptions(modeName);
	}
	
	public static StandaloneOptions standalone() {
		return new StandaloneOptions();
	}
	
	private static Boolean davEnabled;
	
	public static boolean isDavEnabled() {
		if(davEnabled == null)
			davEnabled = Boolean.parseBoolean(PropertyStore.get().get("dav.enabled"));
		return davEnabled;
	}
	
	public static void setDavEnabled(boolean davEnabled) {
		PropertyStore.get().put("dav.enabled", "" + davEnabled);
	}
	
	public static class GeneralOptions {
		public final Constant<String> MODE_NAME = GLOBAL_PROPERTIES.create(STRING, ".mode.name", "");
		public final Constant<String> RULE_NAME = GLOBAL_PROPERTIES.create(STRING, ".0.rule", "");
		public final Constant<String> RULE_NAME_P2 = GLOBAL_PROPERTIES.create(STRING, ".1.rule", "");
		
		private GeneralOptions() {}
	}
	
	public static class PlayerOptions {
		private final int playerId;
		
		public final TuningOptions tuning;
		public final AIOptions ai;
		
		public final Constant<String> RULE_NAME;

		public Constant<String> RULE_NAME_FOR_STYLE(int styleId) {
			return GLOBAL_PROPERTIES.create(STRING, "." + playerId + ".rule." + styleId, "");
		}
		
		private PlayerOptions(int playerId) {
			this.playerId = playerId;
			tuning = new TuningOptions(playerId);
			ai = new AIOptions(playerId);
			
			RULE_NAME = GLOBAL_PROPERTIES.create(STRING, "." + playerId + ".rule", "");
		}
	}
	
	public static class TuningOptions {
		public final Constant<Integer> ROTATE_BUTTON_DEFAULT_RIGHT;
		public final Constant<Integer> SKIN;
		public final Constant<Integer> MIN_DAS;
		public final Constant<Integer> MAX_DAS;
		public final Constant<Integer> DAS_DELAY;
		public final Constant<Boolean> REVERSE_UP_DOWN;
		public final Constant<Integer> MOVE_DIAGONAL;
		public final Constant<Integer> BLOCK_OUTLINE_TYPE;
		public final Constant<Integer> BLOCK_SHOW_OUTLINE_ONLY;
		
		private TuningOptions(int playerId) {
			CustomProperties backing = GLOBAL_PROPERTIES.subProperties("." + playerId + ".tuning.ow");
			ROTATE_BUTTON_DEFAULT_RIGHT = backing.create(INTEGER, "RotateButtonDefaultRight", -1);
			SKIN = backing.create(INTEGER, "Skin", -1);
			MIN_DAS = backing.create(INTEGER, "MinDAS", -1);
			MAX_DAS = backing.create(INTEGER, "MaxDAS", -1);
			DAS_DELAY = backing.create(INTEGER, "DasDelay", -1);
			REVERSE_UP_DOWN = backing.create(BOOLEAN, "ReverseUpDown", false);
			MOVE_DIAGONAL = backing.create(INTEGER, "MoveDiagonal", -1);
			BLOCK_OUTLINE_TYPE = backing.create(INTEGER, "BLockOutlineType", -1);
			BLOCK_SHOW_OUTLINE_ONLY = backing.create(INTEGER, "BlockShowOutlineOnly", -1);
		}
	}
	
	public static class AIOptions {
		public final CustomProperties BACKING;
		public final Constant<String> NAME;
		public final Constant<Integer> MOVE_DELAY;
		public final Constant<Integer> THINK_DELAY;
		public final Constant<Boolean> USE_THREAD;
		public final Constant<Boolean> SHOW_HINT;
		public final Constant<Boolean> PRETHINK;
		public final Constant<Boolean> SHOW_STATE;
		
		private AIOptions(int playerId) {
			BACKING = GLOBAL_PROPERTIES.subProperties("." + playerId + ".ai");
			NAME = BACKING.create(STRING, "", "");
			MOVE_DELAY = BACKING.create(INTEGER, "MoveDelay", 0);
			THINK_DELAY = BACKING.create(INTEGER, "ThinkDelay", 0);
			USE_THREAD = BACKING.create(BOOLEAN, "UseThread", true);
			SHOW_HINT = BACKING.create(BOOLEAN, "ShowHint", false);
			PRETHINK = BACKING.create(BOOLEAN, "Prethink", false);
			SHOW_STATE = BACKING.create(BOOLEAN, "ShowState", false);
		}
	}
	
	public static class ModeOptions {
		public final Constant<String> RULE_RSOURCE;
		
		private ModeOptions(String modeName) {
			CustomProperties p = GLOBAL_PROPERTIES.subProperties(".mode." + modeName + ".");
			RULE_RSOURCE = p.create(STRING, "rule", null);
		}
	}
	
	public static class StandaloneOptions {
		public final Constant<Boolean> SHOW_BG;
		public final Constant<Boolean> SHOW_LINE_EFFECT;
		public final Constant<Boolean> SHOW_METER;
		public final Constant<Boolean> SHOW_FIELD_BLOCK_GRAPHICS;
		public final Constant<Boolean> SIMPLE_BLOCK;
		public final Constant<Boolean> SHOW_FIELD_BG_GRID;
		public final Constant<Boolean> DARK_NEXT_AREA;
		public final Constant<Boolean> NEXT_SHADOW;
		public final Constant<Integer> LINE_EFFECT_SPEED;
		public final Constant<Boolean> OUTLINE_GHOST;
		public final Constant<Boolean> SIDE_NEXT;
		public final Constant<Boolean> BIG_SIDE_NEXT;
		public final Constant<Integer> MAX_FPS;
		public final Constant<Integer> SCREEN_WIDTH;
		public final Constant<Integer> SCREEN_HEIGHT;
		public final Constant<Boolean> ENABLE_FRAME_STEP;
		public final Constant<Boolean> SHOW_FPS;
		public final Constant<Boolean> SYNC_RENDER;
		public final Constant<Boolean> SYNC_DISPLAY;
		public final Constant<Boolean> FULL_SCREEN;
		public final Constant<Boolean> BGM_ENABLE;
		public final Constant<String> BGM_SELECTION;
		public final Constant<Double> SE_VOLUME;
		public final Constant<Boolean> SE_ENABLED;
		public final Constant<Boolean> SHOW_INPUT;
		public final Constant<Boolean> INCREASE_EQ_PRIORITY;
		public final Constant<Boolean> HIDE_UNPOPULAR;
		public final Constant<Boolean> SHOW_FINESSE;
		public final Constant<Boolean> NEXT_ON_FIELD;
		
		private StandaloneOptions() {
			CustomProperties p = GUI_PROPERTIES.subProperties(".options.");
			SHOW_BG = p.create(BOOLEAN, "showbg", true);
			SHOW_LINE_EFFECT = p.create(BOOLEAN, "showlineeffect", false);
			SHOW_METER = p.create(BOOLEAN, "showmeter", true);
			SHOW_FIELD_BLOCK_GRAPHICS = p.create(BOOLEAN, "showfieldblockgraphics", true);
			SIMPLE_BLOCK = p.create(BOOLEAN, "simpleblock", false);
			SHOW_FIELD_BG_GRID = p.create(BOOLEAN, "showfieldbggrid", true);
			DARK_NEXT_AREA = p.create(BOOLEAN, "darknextarea", true);
			NEXT_SHADOW = p.create(BOOLEAN, "nextshadow", false);
			LINE_EFFECT_SPEED = p.create(INTEGER, "lineeffectspeed", 0);
			OUTLINE_GHOST = p.create(BOOLEAN, "outlineghost", false);
			SIDE_NEXT = p.create(BOOLEAN, "sidenext", false);
			BIG_SIDE_NEXT = p.create(BOOLEAN, "bigsidenext", false);
			MAX_FPS = p.create(INTEGER, "maxfps", 60);
			SCREEN_WIDTH = p.create(INTEGER, "screenwidth", 640);
			SCREEN_HEIGHT = p.create(INTEGER, "screenheight", 480);
			ENABLE_FRAME_STEP = p.create(BOOLEAN, "enableframestep", false);
			SHOW_FPS = p.create(BOOLEAN, "showfps", true);
			SYNC_DISPLAY = p.create(BOOLEAN, "syncDisplay", false);
			SYNC_RENDER = p.create(BOOLEAN, "syncRender", false);
			FULL_SCREEN = p.create(BOOLEAN, "fullscreen", false);
			BGM_ENABLE = p.create(BOOLEAN, "bgm.enable", true);
			BGM_SELECTION = p.create(STRING, "bgm.selection", null);
			SE_VOLUME = p.create(DOUBLE, "se.volume", 1.0d);
			SE_ENABLED = p.create(BOOLEAN, "se.enabled", true);
			SHOW_INPUT = p.create(BOOLEAN, "showInput", false);
			INCREASE_EQ_PRIORITY = p.create(BOOLEAN, "event_queue.priority.increase", true);
			HIDE_UNPOPULAR = p.create(BOOLEAN, "hide_unpopular", true);
			SHOW_FINESSE = p.create(BOOLEAN, "showFinesse", false);
			NEXT_ON_FIELD = p.create(BOOLEAN, "next_on_field", false);
		}
	}
	
	private Options() {}
}
