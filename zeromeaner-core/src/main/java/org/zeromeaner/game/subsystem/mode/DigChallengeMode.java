package org.zeromeaner.game.subsystem.mode;

import static org.zeromeaner.knet.KNetEventArgs.GAME_END_STATS;
import static org.zeromeaner.knet.KNetEventArgs.GAME_OPTIONS;
import static org.zeromeaner.knet.KNetEventArgs.GAME_STATS;
import static org.zeromeaner.knet.KNetEventArgs.START_1P;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import org.zeromeaner.game.component.BGMStatus;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.SpeedParam;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

/**
 * DIG CHALLENGE mode
 */
public class DigChallengeMode extends AbstractNetMode {
	public static class Stats {
		private Statistics statistics;
		private int garbageTimer;
		private int garbageTotal;
		private int goalType;
		private boolean gameActive;
		private boolean timerActive;
		private int lastScore;
		private int scGetTime;
		private int lastEvent;
		private boolean lastb2b;
		private int lastCombo;
		private int lastPiece;
		private int bg;
		private int garbagePending;
		
		public Statistics getStatistics() {
			return statistics;
		}
		public void setStatistics(Statistics statistics) {
			this.statistics = statistics;
		}
		public int getGarbageTimer() {
			return garbageTimer;
		}
		public void setGarbageTimer(int garbageTimer) {
			this.garbageTimer = garbageTimer;
		}
		public int getGarbageTotal() {
			return garbageTotal;
		}
		public void setGarbageTotal(int garbageTotal) {
			this.garbageTotal = garbageTotal;
		}
		public int getGoalType() {
			return goalType;
		}
		public void setGoalType(int goalType) {
			this.goalType = goalType;
		}
		public boolean isGameActive() {
			return gameActive;
		}
		public void setGameActive(boolean gameActive) {
			this.gameActive = gameActive;
		}
		public int getLastScore() {
			return lastScore;
		}
		public void setLastScore(int lastScore) {
			this.lastScore = lastScore;
		}
		public int getScGetTime() {
			return scGetTime;
		}
		public void setScGetTime(int scGetTime) {
			this.scGetTime = scGetTime;
		}
		public int getLastEvent() {
			return lastEvent;
		}
		public void setLastEvent(int lastEvent) {
			this.lastEvent = lastEvent;
		}
		public boolean isLastb2b() {
			return lastb2b;
		}
		public void setLastb2b(boolean lastb2b) {
			this.lastb2b = lastb2b;
		}
		public int getLastCombo() {
			return lastCombo;
		}
		public void setLastCombo(int lastCombo) {
			this.lastCombo = lastCombo;
		}
		public int getLastPiece() {
			return lastPiece;
		}
		public void setLastPiece(int lastPiece) {
			this.lastPiece = lastPiece;
		}
		public int getBg() {
			return bg;
		}
		public void setBg(int bg) {
			this.bg = bg;
		}
		public int getGarbagePending() {
			return garbagePending;
		}
		public void setGarbagePending(int garbagePending) {
			this.garbagePending = garbagePending;
		}
		public boolean isTimerActive() {
			return timerActive;
		}
		public void setTimerActive(boolean timerActive) {
			this.timerActive = timerActive;
		}
	}
	
	public static class Options {
		private SpeedParam speed;
		private int goalType;
		private int startLevel;
		private int bgmno;
		private int tspinEnableType;
		private boolean enableTSpinKick;
		private int spinCheckType;
		private boolean tspinEnableEZ;
		private boolean enableB2B;
		private boolean enableCombo;
		
		public SpeedParam getSpeed() {
			return speed;
		}
		public void setSpeed(SpeedParam speed) {
			this.speed = speed;
		}
		public int getGoalType() {
			return goalType;
		}
		public void setGoalType(int goalType) {
			this.goalType = goalType;
		}
		public int getStartLevel() {
			return startLevel;
		}
		public void setStartLevel(int startLevel) {
			this.startLevel = startLevel;
		}
		public int getBgmno() {
			return bgmno;
		}
		public void setBgmno(int bgmno) {
			this.bgmno = bgmno;
		}
		public int getTspinEnableType() {
			return tspinEnableType;
		}
		public void setTspinEnableType(int tspinEnableType) {
			this.tspinEnableType = tspinEnableType;
		}
		public boolean isEnableTSpinKick() {
			return enableTSpinKick;
		}
		public void setEnableTSpinKick(boolean enableTSpinKick) {
			this.enableTSpinKick = enableTSpinKick;
		}
		public int getSpinCheckType() {
			return spinCheckType;
		}
		public void setSpinCheckType(int spinCheckType) {
			this.spinCheckType = spinCheckType;
		}
		public boolean isTspinEnableEZ() {
			return tspinEnableEZ;
		}
		public void setTspinEnableEZ(boolean tspinEnableEZ) {
			this.tspinEnableEZ = tspinEnableEZ;
		}
		public boolean isEnableB2B() {
			return enableB2B;
		}
		public void setEnableB2B(boolean enableB2B) {
			this.enableB2B = enableB2B;
		}
		public boolean isEnableCombo() {
			return enableCombo;
		}
		public void setEnableCombo(boolean enableCombo) {
			this.enableCombo = enableCombo;
		}
	}
	
	/** Current version */
	private static final int CURRENT_VERSION = 2;

	/** Number of goal type */
	private static final int GOALTYPE_MAX = 2;

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of garbage lines for each level */
	private static final int LEVEL_GARBAGE_LINES = 10;

	/** Goal type constants */
	private static final int GOALTYPE_NORMAL = 0,
							 GOALTYPE_REALTIME = 1;

	/** Most recent scoring event type constants */
	private static final int EVENT_NONE = 0,
							 EVENT_SINGLE = 1,
							 EVENT_DOUBLE = 2,
							 EVENT_TRIPLE = 3,
							 EVENT_FOUR = 4,
							 EVENT_TSPIN_SINGLE_MINI = 5,
							 EVENT_TSPIN_SINGLE = 6,
							 EVENT_TSPIN_DOUBLE_MINI = 7,
							 EVENT_TSPIN_DOUBLE = 8,
							 EVENT_TSPIN_TRIPLE = 9,
							 EVENT_TSPIN_EZ = 10;

	/** Combo bonus table */
	private final int[] COMBO_ATTACK_TABLE = {0,0,1,1,2,2,3,3,4,4,4,5};

	/** Garbage speed table */
	private final int[][] GARBAGE_TIMER_TABLE =
	{
		{180,170,160,150,140,130,120,110,100, 90, 80, 70, 60, 50, 40, 30, 20, 10,  5,  0},	// Normal (OLD)
		{180,170,160,150,140,130,120,110,100, 90, 80, 70, 60, 50, 45, 40, 35, 30, 25, 20},	// Realtime
	};

	/** Fall velocity table (numerators) */
	private static final int tableGravity[]     = { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 465, 731, 1280, 1707,  -1,  -1,  -1};

	/** Fall velocity table (denominators) */
	private static final int tableDenominator[] = {63, 50, 39, 30, 22, 16, 12,  8,  6,  4,  3,  2,  1, 256, 256,  256,  256, 256, 256, 256};

	/** Most recent increase in score */
	private int lastscore;

	/** Most recent increase in bonus score */
	private int lastbonusscore;

	/** Time to display the most recent increase in score */
	private int scgettime;

	/** Most recent scoring event type */
	private int lastevent;

	/** True if most recent scoring event is a B2B */
	private boolean lastb2b;

	/** Combo count for most recent scoring event */
	private int lastcombo;

	/** Piece ID for most recent scoring event */
	private int lastpiece;

	/** Previous garbage hole */
	private int garbageHole;

	/** Garbage timer */
	private int garbageTimer;

	/** Number of total garbage lines rised */
	private int garbageTotal;

	/** Number of garbage lines needed for next level */
	private int garbageNextLevelLines;

	/** Number of garbage lines waiting to appear (Normal type) */
	private int garbagePending;

	/** Game type */
	private int goaltype = GOALTYPE_REALTIME;

	/** Level at the start of the game */
	private int startlevel;

	/** BGM number */
	private int bgmno;

	/** Flag for types of T-Spins allowed (0=none, 1=normal, 2=all spin) */
	private int tspinEnableType;

	/** Flag for enabling wallkick T-Spins */
	private boolean enableTSpinKick;

	/** Spin check type (4Point or Immobile) */
	private int spinCheckType;

	/** Immobile EZ spin */
	private boolean tspinEnableEZ;

	/** Flag for enabling B2B */
	private boolean enableB2B;

	/** Flag for enabling combos */
	private boolean enableCombo;

	/** Version */
	private int version;

	/** Current round's ranking rank */
	private int rankingRank;

	/** Rankings' scores */
	private int[][] rankingScore;

	/** Rankings' line counts */
	private int[][] rankingLines;

	/** Rankings' times */
	private int[][] rankingTime;

	private long lastFrameTime;
	
	private double growthRate;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "DIG CHALLENGE";
	}

	/*
	 * Initialization for each player
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.getOwner();
		receiver = engine.getOwner().receiver;
		
		lastscore = 0;
		lastbonusscore = 0;
		scgettime = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;

		garbageHole = -1;
		garbageTimer = 0;
		garbageTotal = 0;
		garbageNextLevelLines = 0;
		garbagePending = 0;

		growthRate = 1 / (9.5 * 60);
		
		rankingRank = -1;
		rankingScore = new int[GOALTYPE_MAX][RANKING_MAX];
		rankingLines = new int[GOALTYPE_MAX][RANKING_MAX];
		rankingTime = new int[GOALTYPE_MAX][RANKING_MAX];

		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
		engine.statistics.levelDispAdd = 1;

		netPlayerInit(engine, playerID);

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);

			// NET: Load name
			netPlayerName = engine.getOwner().replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.getOwner().backgroundStatus.bg = startlevel;
	}

	/**
	 * Set the gravity rate
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		if(goaltype == GOALTYPE_REALTIME) {
			engine.speed.gravity = 0;
			engine.speed.denominator = 1;
		} else {
			int lv = engine.statistics.level;

			if(lv < 0) lv = 0;
			if(lv >= tableGravity.length) lv = tableGravity.length - 1;

			engine.speed.gravity = tableGravity[lv];
			engine.speed.denominator = tableDenominator[lv];
		}

		engine.speed.are = 0;
		engine.speed.areLine = 0;
		engine.speed.lineDelay = 0;
		engine.speed.lockDelay = 30;
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(engine.getOwner().replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 9, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(menuCursor) {
				case 0:
					goaltype += change;
					if(goaltype < 0) goaltype = GOALTYPE_MAX - 1;
					if(goaltype > GOALTYPE_MAX - 1) goaltype = 0;
					break;
				case 1:
					startlevel += change;
					if(startlevel < 0) startlevel = 19;
					if(startlevel > 19) startlevel = 0;
					engine.getOwner().backgroundStatus.bg = startlevel;
					break;
				case 2:
					bgmno += change;
					if(bgmno < -1) bgmno = BGMStatus.BGM_COUNT - 1;
					if(bgmno > BGMStatus.BGM_COUNT - 1) bgmno = -1;
					break;
				case 3:
					tspinEnableType += change;
					if(tspinEnableType < 0) tspinEnableType = 2;
					if(tspinEnableType > 2) tspinEnableType = 0;
					break;
				case 4:
					enableTSpinKick = !enableTSpinKick;
					break;
				case 5:
					spinCheckType += change;
					if(spinCheckType < 0) spinCheckType = 1;
					if(spinCheckType > 1) spinCheckType = 0;
					break;
				case 6:
					tspinEnableEZ = !tspinEnableEZ;
					break;
				case 7:
					enableB2B = !enableB2B;
					break;
				case 8:
					enableCombo = !enableCombo;
					break;
				case 9:
					engine.speed.das += change;
					if(engine.speed.das < 0) engine.speed.das = 99;
					if(engine.speed.das > 99) engine.speed.das = 0;
					break;
				}

				// NET: Signal options change
				if(netIsNetPlay && (netNumSpectators() > 0)) netSendOptions(engine);
			}

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (menuTime >= 5)) {
				engine.playSE("decide");

				// Save settings
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);

				// NET: Signal start of the game
				if(netIsNetPlay) 
					knetClient().fireTCP(START_1P, true);

				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
				engine.quitflag = true;
			}

			menuTime++;
		}
		// Replay
		else {
			menuTime++;
			menuCursor = -1;

			if(menuTime >= 60) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Render settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		String strTSpinEnable = "";
		if(tspinEnableType == 0) strTSpinEnable = "OFF";
		if(tspinEnableType == 1) strTSpinEnable = "T-ONLY";
		if(tspinEnableType == 2) strTSpinEnable = "ALL";

		drawMenu(engine, playerID, receiver, 0, EventRenderer.COLOR_BLUE, 0,
				"GAME TYPE", (goaltype == 0) ? "NORMAL" : "REALTIME",
				"LEVEL", String.valueOf(startlevel + 1),
				"BGM", String.valueOf(bgmno),
				"SPIN BONUS", strTSpinEnable,
				"EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick),
				"SPIN TYPE", (spinCheckType == 0) ? "4POINT" : "IMMOBILE",
				"EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
				"B2B", GeneralUtil.getONorOFF(enableB2B),
				"COMBO", GeneralUtil.getONorOFF(enableCombo),
				"DAS", String.valueOf(engine.speed.das));
	}

	/*
	 * This function will be called before the game actually begins (after Ready&Go screen disappears)
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.b2bEnable = enableB2B;
		if(enableCombo == true) {
			engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
		} else {
			engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		}

		engine.tspinAllowKick = enableTSpinKick;
		if(tspinEnableType == 0) {
			engine.tspinEnable = false;
		} else if(tspinEnableType == 1) {
			engine.tspinEnable = true;
		} else {
			engine.tspinEnable = true;
			engine.useAllSpinBonus = true;
		}

		if(version >= 1) {
			engine.spinCheckType = spinCheckType;
			engine.tspinEnableEZ = tspinEnableEZ;
		}

		garbageTotal = LEVEL_GARBAGE_LINES * startlevel;
		garbageNextLevelLines = LEVEL_GARBAGE_LINES * (startlevel + 1);

		setSpeed(engine);

		if(netIsWatch()) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		} else {
			owner.bgmStatus.bgm = bgmno;
		}
		lastFrameTime = System.nanoTime();
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, "DIG CHALLENGE", EventRenderer.COLOR_GREEN);
		if(goaltype == 0) {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(NORMAL GAME)", EventRenderer.COLOR_GREEN);
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(REALTIME GAME)", EventRenderer.COLOR_GREEN);
		}

		if( (engine.stat == GameEngine.Status.SETTING) || ((engine.stat == GameEngine.Status.RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (startlevel == 0) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  LINE TIME", EventRenderer.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventRenderer.COLOR_YELLOW, scale);
					receiver.drawScoreFont(engine, playerID,  3, topY+i, String.valueOf(rankingScore[goaltype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLines[goaltype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[goaltype][i]), (i == rankingRank), scale);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventRenderer.COLOR_BLUE);
			String strScore = null;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else if(lastbonusscore == 0) {
				strScore = String.valueOf(engine.statistics.score) + "(+" + String.valueOf(lastscore) + ")";
			} else {
				strScore = engine.statistics.score + "(+" + lastscore + "+" + lastbonusscore + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

			receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(engine.statistics.lines));

			receiver.drawScoreFont(engine, playerID, 0, 9, "GARBAGE", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(garbageTotal));

			receiver.drawScoreFont(engine, playerID, 0, 12, "LEVEL", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, String.valueOf(engine.statistics.level + 1));

			receiver.drawScoreFont(engine, playerID, 0, 15, "TIME", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 16, GeneralUtil.getTime(engine.statistics.time));

			if((lastevent != EVENT_NONE) && (scgettime < 120)) {
				String strPieceName = Piece.getPieceName(lastpiece);

				switch(lastevent) {
				case EVENT_SINGLE:
					receiver.drawMenuFont(engine, playerID, 2, 21, "SINGLE", EventRenderer.COLOR_DARKBLUE);
					break;
				case EVENT_DOUBLE:
					receiver.drawMenuFont(engine, playerID, 2, 21, "DOUBLE", EventRenderer.COLOR_BLUE);
					break;
				case EVENT_TRIPLE:
					receiver.drawMenuFont(engine, playerID, 2, 21, "TRIPLE", EventRenderer.COLOR_GREEN);
					break;
				case EVENT_FOUR:
					if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventRenderer.COLOR_RED);
					else receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventRenderer.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_SINGLE_MINI:
					if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventRenderer.COLOR_RED);
					else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventRenderer.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_SINGLE:
					if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventRenderer.COLOR_RED);
					else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventRenderer.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_DOUBLE_MINI:
					if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventRenderer.COLOR_RED);
					else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventRenderer.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_DOUBLE:
					if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventRenderer.COLOR_RED);
					else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventRenderer.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_TRIPLE:
					if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventRenderer.COLOR_RED);
					else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventRenderer.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_EZ:
					if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventRenderer.COLOR_RED);
					else receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventRenderer.COLOR_ORANGE);
					break;
				}

				if(lastcombo >= 2)
					receiver.drawMenuFont(engine, playerID, 2, 22, (lastcombo - 1) + "COMBO", EventRenderer.COLOR_CYAN);
			}

			if(garbagePending > 0) {
				int x = receiver.getFieldDisplayPositionX(engine, playerID);
				int y = receiver.getFieldDisplayPositionY(engine, playerID);
				int fontColor = EventRenderer.COLOR_WHITE;

				if(garbagePending >= 1) fontColor = EventRenderer.COLOR_YELLOW;
				if(garbagePending >= 3) fontColor = EventRenderer.COLOR_ORANGE;
				if(garbagePending >= 4) fontColor = EventRenderer.COLOR_RED;

				String strTempGarbage = String.format("%5d", garbagePending);
				receiver.drawDirectFont(engine, playerID, x + 96, y + 372, strTempGarbage, fontColor);
			}
		}

		// NET: Number of spectators
		netDrawSpectatorsCount(engine, 0, 18);
		// NET: All number of players
		if(playerID == getPlayers() - 1) {
			netDrawGameRate(engine);
		}
		// NET: Player name (It may also appear in offline replay)
		netDrawPlayerName(engine);
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;

		if(engine.gameActive && engine.timerActive) {
			garbageTimer++;

			// Update meter
			updateMeter(engine);

			// Add pending garbage (Normal)
			if((garbageTimer >= getGarbageMaxTime(engine.statistics.level)) && (goaltype == GOALTYPE_NORMAL) && (!netIsWatch())) {
				if(version >= 1) {
					garbagePending++;
					garbageTimer = 0;

					// NET: Send stats
					if(netIsNetPlay && !netIsWatch() && (netNumSpectators() > 0)) {
						netSendStats(engine);
					}
				} else {
					garbagePending = 1;
				}
			}

			// Add Garbage (Realtime)
			if((garbageTimer >= getGarbageMaxTime(engine.statistics.level) || fieldShift <= 0) && (goaltype == GOALTYPE_REALTIME) &&
			   (engine.stat != GameEngine.Status.LINECLEAR) && (!netIsWatch()))
			{
				addGarbage(engine);
				garbageTimer = 0;

				// NET: Send field and stats
				if(netIsNetPlay && !netIsWatch() && (netNumSpectators() > 0)) {
					netSendField(engine, false);
					netSendStats(engine);
				}

				if((engine.stat == GameEngine.Status.MOVE) && (engine.nowPieceObject != null)) {
					if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field)) {
						// Push up the current piece
						while(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field)) {
							engine.nowPieceY--;
						}

						// Pushed out from the visible part of the field
						if(!engine.nowPieceObject.canPlaceToVisibleField(engine.nowPieceX, engine.nowPieceY, engine.field)) {
							engine.stat = GameEngine.Status.GAMEOVER;
							engine.resetStatc();
							engine.gameEnded();
						}
					}

					// Update ghost position
					engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

					// NET: Send piece movement
					if(netIsNetPlay && !netIsWatch() && (netNumSpectators() > 0)) netSendPieceMovement(engine, true);
				}
			}
		}
		if(engine.field != null) {
			if(engine.field.getHighestGarbageBlockY() == engine.field.getHeight()) {
				engine.fieldShift = 0;
			}
		}
	}
	
	private double fieldShift;

	/**
	 * Update timer meter
	 * @param engine GameEngine
	 */
	private void updateMeter(GameEngine engine) {
		int limitTime = getGarbageMaxTime(engine.statistics.level);
		int remainTime = limitTime - garbageTimer;
		if(remainTime < 0) remainTime = 0;
		if(fieldShift > 0) {
			long last = lastFrameTime;
			long now = System.nanoTime();
			fieldShift -= (growthRate * (now - last)) / TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
			engine.fieldShift = fieldShift;
			lastFrameTime = now;
		} else {
			
		}
		engine.meterValue = (int)(receiver.getMeterMax(engine) * (1-fieldShift));
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.meterValue > receiver.getMeterMax(engine) / 2) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.meterValue > (receiver.getMeterMax(engine) * 2) / 3) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.meterValue > (receiver.getMeterMax(engine) * 3) / 4) engine.meterColor = GameEngine.METER_COLOR_RED;
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Add Garbage (Normal)
		if((goaltype == GOALTYPE_NORMAL) && (garbagePending > 0)) {
			if((version <= 1) || (lines <= 0)) {
				addGarbage(engine, garbagePending);
				garbagePending = 0;
			}
		}

		// Line clear bonus
		if(lines > 0) {
			int pts = 0;
			scgettime = 0;

			if(engine.tspin) {
				// Immobile EZ Spin
				if(engine.tspinez) {
					if(!engine.useAllSpinBonus) {
						pts += 1;
					}
					lastevent = EVENT_TSPIN_EZ;
				}
				// T-Spin 1 line
				if(lines == 1) {
					if(engine.tspinmini) {
						if(!engine.useAllSpinBonus) {
							pts += 1;
						}
						lastevent = EVENT_TSPIN_SINGLE_MINI;
					} else {
						pts += 2;
						lastevent = EVENT_TSPIN_SINGLE;
					}
				}
				// T-Spin 2 lines
				else if(lines == 2) {
					if(engine.tspinmini && engine.useAllSpinBonus) {
						pts += 3;
						lastevent = EVENT_TSPIN_DOUBLE_MINI;
					} else {
						pts += 4;
						lastevent = EVENT_TSPIN_DOUBLE;
					}
				}
				// T-Spin 3 lines
				else if(lines >= 3) {
					pts += 6;
					lastevent = EVENT_TSPIN_TRIPLE;
				}
			} else {
				if(lines == 1) {
					lastevent = EVENT_SINGLE;	// 1 line
				} else if(lines == 2) {
					pts += 1;
					lastevent = EVENT_DOUBLE;	// 2 lines
				} else if(lines == 3) {
					pts += 2;
					lastevent = EVENT_TRIPLE;	// 3 lines
				} else if(lines >= 4) {
					pts += 4;
					lastevent = EVENT_FOUR;		// 4 lines
				}
			}

			// B2B
			if(engine.b2b) {
				lastb2b = true;
				if(pts > 0) {
					if((lastevent == EVENT_TSPIN_TRIPLE) && (!engine.useAllSpinBonus)) {
						pts += 2;
					} else {
						pts += 1;
					}
				}
			} else {
				lastb2b = false;
			}

			// Combo
			if(engine.comboType != GameEngine.COMBO_TYPE_DISABLE) {
				int cmbindex = engine.combo - 1;
				if(cmbindex < 0) cmbindex = 0;
				if(cmbindex >= COMBO_ATTACK_TABLE.length) cmbindex = COMBO_ATTACK_TABLE.length - 1;
				pts += COMBO_ATTACK_TABLE[cmbindex];
				lastcombo = engine.combo;
			}

			// All clear
			if((lines >= 1) && (engine.field.isEmpty())) {
				engine.playSE("bravo");
				pts += 6;
			}

			// Add to score
			lastscore = pts;
			lastpiece = engine.nowPieceObject.id;
			lastbonusscore = 0;
			if(pts > 0) {
				if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
				else engine.statistics.scoreFromOtherBonus += pts;
				engine.statistics.score += pts;
			}

			// Decrease waiting garbage lines (normal type)
			if((goaltype == GOALTYPE_NORMAL) && (version >= 2)) {
				garbagePending -= pts;
				if(garbagePending < 0) {
					int bonus = Math.abs(garbagePending);
					lastbonusscore = bonus;
					if(lines >= 1) engine.statistics.scoreFromLineClear += bonus;
					else engine.statistics.scoreFromOtherBonus += bonus;
					engine.statistics.score += bonus;
					garbagePending = 0;
				}
			}
		}
	}

	/**
	 * Get garbage time limit
	 * @param lv Level
	 * @return Garbage time limi
	 */
	private int getGarbageMaxTime(int lv) {
		int t = goaltype;

		int limitTime;
		if(t == GOALTYPE_NORMAL) {
			if(lv > GARBAGE_TIMER_TABLE[t].length - 1) lv = GARBAGE_TIMER_TABLE[t].length - 1;
			limitTime = GARBAGE_TIMER_TABLE[t][lv];
		} 
		else
			limitTime = Integer.MAX_VALUE;

		return limitTime;
	}

	/**
	 * Add an new garbage line
	 * @param engine GameEngine
	 */
	private void addGarbage(GameEngine engine) {
		addGarbage(engine, 1);
		fieldShift = engine.fieldShift = 1;
	}

	/**
	 * Add garbage line(s)
	 * @param engine GameEngine
	 * @param lines Number of garbage lines to add
	 */
	private void addGarbage(GameEngine engine, int lines) {
		// Add garbages
		Field field = engine.field;
		int w = field.getWidth();
		int h = field.getHeight();

		engine.playSE("garbage");

		int prevHole = garbageHole;

		for(int i = 0; i < lines; i++) {
			do {
				garbageHole = engine.random.nextInt(w);
			} while (garbageHole == prevHole);

			field.pushUp();

			for(int x = 0; x < w; x++) {
				if(x != garbageHole) {
					field.setBlock(x, h-1,
						new Block(Block.BLOCK_COLOR_GRAY,engine.getSkin(),Block.BLOCK_ATTRIBUTE_VISIBLE|Block.BLOCK_ATTRIBUTE_GARBAGE)
					);
				}
			}

			// Set connections
			if(receiver.isStickySkin(engine)) {
				for(int x = 0; x < w; x++) {
					if(x != garbageHole) {
						Block blk = field.getBlock(x, h-1);
						if(blk != null) {
							if(!field.getBlockEmpty(x-1, h-1)) blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
							if(!field.getBlockEmpty(x+1, h-1)) blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
						}
					}
				}
			}
		}

		// Levelup
		boolean lvupflag = false;
		garbageTotal += lines;

		while((garbageTotal >= garbageNextLevelLines) && (engine.statistics.level < 19)) {
			garbageNextLevelLines += LEVEL_GARBAGE_LINES;
			engine.statistics.level++;
			lvupflag = true;
		}

		if(lvupflag) {
			owner.backgroundStatus.fadesw = true;
			owner.backgroundStatus.fadecount = 0;
			owner.backgroundStatus.fadebg = engine.statistics.level;
			setSpeed(engine);
			engine.playSE("levelup");
		}
		
		double nextTime = 22.9416 * (Math.sqrt(garbageTotal + 0.644737) - 0.802955);
		double prevTime = 22.9416 * (Math.sqrt(garbageTotal - 1 + 0.644737) - 0.802955);
		// assume 60 FPS
		growthRate = 1 / (nextTime - prevTime);
	}

	/*
	 * Results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, receiver, 0, EventRenderer.COLOR_BLUE,
				Statistic.SCORE, Statistic.LINES);
		drawResult(engine, playerID, receiver, 4, EventRenderer.COLOR_BLUE,
				"GARBAGE", String.format("%10d", garbageTotal));
		drawResultStats(engine, playerID, receiver, 6, EventRenderer.COLOR_BLUE,
				Statistic.PIECE, Statistic.LEVEL, Statistic.TIME);
		drawResultRank(engine, playerID, receiver, 12, EventRenderer.COLOR_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, receiver, 14, EventRenderer.COLOR_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, receiver, 16, EventRenderer.COLOR_BLUE, netRankingRank[1]);

		if(netIsPB) {
			receiver.drawMenuFont(engine, playerID, 2, 18, "NEW PB", EventRenderer.COLOR_ORANGE);
		}

		if(netIsNetPlay && (netReplaySendStatus == 1)) {
			receiver.drawMenuFont(engine, playerID, 0, 19, "SENDING...", EventRenderer.COLOR_PINK);
		} else if(netIsNetPlay && !netIsWatch() && (netReplaySendStatus == 2)) {
			receiver.drawMenuFont(engine, playerID, 1, 19, "A: RETRY", EventRenderer.COLOR_RED);
		}
	}

	/*
	 * Called when saving replay
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(prop);

		// NET: Save name
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
		}

		// Update rankings
		if((owner.replayMode == false) && (startlevel == 0) && (engine.ai == null)) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, goaltype);

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	protected void loadSetting(CustomProperties prop) {
		goaltype = prop.getProperty("digchallenge.goaltype", GOALTYPE_NORMAL);
		startlevel = prop.getProperty("digchallenge.startlevel", 0);
		bgmno = prop.getProperty("digchallenge.bgmno", 0);
		tspinEnableType = prop.getProperty("digchallenge.tspinEnableType", 2);
		enableTSpinKick = prop.getProperty("digchallenge.enableTSpinKick", true);
		spinCheckType = prop.getProperty("digchallenge.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("digchallenge.tspinEnableEZ", false);
		enableB2B = prop.getProperty("digchallenge.enableB2B", true);
		enableCombo = prop.getProperty("digchallenge.enableCombo", true);
		owner.engine[0].speed.das = prop.getProperty("digchallenge.das", 11);
		version = prop.getProperty("digchallenge.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	protected void saveSetting(CustomProperties prop) {
		prop.setProperty("digchallenge.goaltype", goaltype);
		prop.setProperty("digchallenge.startlevel", startlevel);
		prop.setProperty("digchallenge.bgmno", bgmno);
		prop.setProperty("digchallenge.tspinEnableType", tspinEnableType);
		prop.setProperty("digchallenge.spinCheckType", spinCheckType);
		prop.setProperty("digchallenge.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("digchallenge.enableTSpinKick", enableTSpinKick);
		prop.setProperty("digchallenge.enableB2B", enableB2B);
		prop.setProperty("digchallenge.enableCombo", enableCombo);
		prop.setProperty("digchallenge.das", owner.engine[0].speed.das);
		prop.setProperty("digchallenge.version", version);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GOALTYPE_MAX; j++) {
				rankingScore[j][i] = prop.getProperty("digchallenge.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("digchallenge.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("digchallenge.ranking." + ruleName + "." + j + ".time." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GOALTYPE_MAX; j++) {
				prop.setProperty("digchallenge.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("digchallenge.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("digchallenge.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, int li, int time, int type) {
		rankingRank = checkRanking(sc, li, time, type);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[type][i] = rankingScore[type][i - 1];
				rankingLines[type][i] = rankingLines[type][i - 1];
				rankingTime[type][i] = rankingTime[type][i - 1];
			}

			// Add new data
			rankingScore[type][rankingRank] = sc;
			rankingLines[type][rankingRank] = li;
			rankingTime[type][rankingRank] = time;
		}
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int li, int time, int type) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(time > rankingTime[type][i]) {
				return i;
			} else if((time == rankingTime[type][i]) && (li > rankingLines[type][i])) {
				return i;
			} else if((time == rankingTime[type][i]) && (li == rankingLines[type][i]) && (sc > rankingScore[type][i])) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * NET: Send various in-game stats (as well as goaltype)
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendStats(GameEngine engine) {
		int bg = engine.getOwner().backgroundStatus.fadesw ? engine.getOwner().backgroundStatus.fadebg : engine.getOwner().backgroundStatus.bg;
		Stats s = new Stats();
		s.setStatistics(engine.statistics);
		s.setGarbageTimer(garbageTimer);
		s.setGarbageTotal(garbageTotal);
		s.setGoalType(goaltype);
		s.setGameActive(engine.gameActive);
		s.setTimerActive(engine.timerActive);
		s.setLastScore(lastscore);
		s.setScGetTime(scgettime);
		s.setLastEvent(lastevent);
		s.setLastb2b(lastb2b);
		s.setLastCombo(lastcombo);
		s.setLastPiece(lastpiece);
		s.setBg(bg);
		s.setGarbagePending(garbagePending);
		knetClient().fireUDP(GAME_STATS, s);
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)
	 */
	@Override
	protected void netRecvStats(GameEngine engine, KNetEvent e) {
		Stats s = (Stats) e.get(GAME_STATS);
		
		engine.statistics.copy(s.getStatistics());
		garbageTimer = s.getGarbageTimer();
		garbageTotal = s.getGarbageTotal();
		goaltype = s.getGoalType();
		engine.gameActive = s.isGameActive();
		engine.timerActive = s.isTimerActive();
		lastscore = s.getLastScore();
		scgettime = s.getScGetTime();
		lastevent = s.getLastEvent();
		lastb2b = s.isLastb2b();
		lastcombo = s.getLastCombo();
		lastpiece = s.getLastPiece();
		engine.getOwner().backgroundStatus.bg = s.getBg();
		garbagePending = s.getGarbagePending();

		// Meter
		updateMeter(engine);
	}

	/**
	 * NET: Send end-of-game stats
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		knetClient().fireTCP(GAME_END_STATS, engine.statistics);
	}

	/**
	 * NET: Send game options to all spectators
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendOptions(GameEngine engine) {
		Options o = new Options();
		o.setGoalType(goaltype);
		o.setStartLevel(startlevel);
		o.setBgmno(bgmno);
		o.setTspinEnableType(tspinEnableType);
		o.setEnableTSpinKick(enableTSpinKick);
		o.setSpinCheckType(spinCheckType);
		o.setTspinEnableEZ(tspinEnableEZ);
		o.setEnableB2B(enableB2B);
		o.setEnableCombo(enableCombo);
		o.setSpeed(engine.speed);
		knetClient().fireTCP(GAME_OPTIONS, o);
	}

	/**
	 * NET: Receive game options
	 */
	@Override
	protected void netRecvOptions(GameEngine engine, KNetEvent e) {
		Options o = (Options) e.get(GAME_OPTIONS);
		goaltype = o.getGoalType();
		startlevel = o.getStartLevel();
		bgmno = o.getBgmno();
		tspinEnableType = o.getTspinEnableType();
		enableTSpinKick = o.isEnableTSpinKick();
		spinCheckType = o.getSpinCheckType();
		tspinEnableEZ = o.isTspinEnableEZ();
		enableB2B = o.isEnableB2B();
		enableCombo = o.isEnableCombo();
		engine.speed.copy(o.getSpeed());
	}

	/**
	 * NET: Get goal type
	 */
	@Override
	protected int netGetGoalType() {
		return goaltype;
	}

	/**
	 * NET: It returns true when the current settings doesn't prevent leaderboard screen from showing.
	 */
	@Override
	protected boolean netIsNetRankingViewOK(GameEngine engine) {
		return ((startlevel == 0) && (engine.ai == null));
	}
}
