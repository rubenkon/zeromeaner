/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package org.zeromeaner.game.subsystem.mode;

import static org.zeromeaner.knet.KNetEventArgs.GAME_END_STATS;
import static org.zeromeaner.knet.KNetEventArgs.GAME_OPTIONS;
import static org.zeromeaner.knet.KNetEventArgs.GAME_STATS;
import static org.zeromeaner.knet.KNetEventArgs.START_1P;

import org.zeromeaner.game.component.BGMStatus;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

/**
 * EXTREME Mode
 */
public class ExtremeMode extends AbstractNetMode {
	public static class Stats {
		private Statistics statistics;
		private boolean endless;
		private boolean gameActive;
		private boolean timerActive;
		private int lastScore;
		private int scGetTime;
		private int lastEvent;
		private boolean lastB2b;
		private int lastCombo;
		private int lastPiece;
		private int bg;
		private int rollTime;
		private int meterValue;
		private int meterColor;
		public Statistics getStatistics() {
			return statistics;
		}
		public void setStatistics(Statistics statistics) {
			this.statistics = statistics;
		}
		public boolean isEndless() {
			return endless;
		}
		public void setEndless(boolean endless) {
			this.endless = endless;
		}
		public boolean isGameActive() {
			return gameActive;
		}
		public void setGameActive(boolean gameActive) {
			this.gameActive = gameActive;
		}
		public boolean isTimerActive() {
			return timerActive;
		}
		public void setTimerActive(boolean timerActive) {
			this.timerActive = timerActive;
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
		public boolean isLastB2b() {
			return lastB2b;
		}
		public void setLastB2b(boolean lastB2b) {
			this.lastB2b = lastB2b;
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
		public int getRollTime() {
			return rollTime;
		}
		public void setRollTime(int rollTime) {
			this.rollTime = rollTime;
		}
		public int getMeterValue() {
			return meterValue;
		}
		public void setMeterValue(int meterValue) {
			this.meterValue = meterValue;
		}
		public int getMeterColor() {
			return meterColor;
		}
		public void setMeterColor(int meterColor) {
			this.meterColor = meterColor;
		}
	}
	public static class Options {
		private int startLevel;
		private int tspinEnableType;
		private boolean enableTSpinKick;
		private boolean enableB2B;
		private boolean enableCombo;
		private boolean endless;
		private boolean big;
		private int spinCheckType;
		private boolean tspinEnableEZ;
		
		public int getStartLevel() {
			return startLevel;
		}
		public void setStartLevel(int startLevel) {
			this.startLevel = startLevel;
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
		public boolean isEndless() {
			return endless;
		}
		public void setEndless(boolean endless) {
			this.endless = endless;
		}
		public boolean isBig() {
			return big;
		}
		public void setBig(boolean big) {
			this.big = big;
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
	}
	/** Current version */
	private static final int CURRENT_VERSION = 1;

	/** Ending time */
	protected static final int ROLLTIMELIMIT = 2968;

	/** ARE table */
	private static final int tableARE[] = {25,20,15,10,10,10, 8, 6, 5, 4, 4, 3, 2, 2, 1, 1, 0, 0, 0, 0};

	/** ARE after line clear table */
	private static final int tableARELine[] = {25,20,15,10, 6, 4, 4, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0};

	/** Line clear time table */
	private static final int tableLineDelay[] = {40,20,10, 5, 6, 4, 4, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0};

	/** Lock delay table */
	private static final int tableLockDelay[] = {30,25,25,20,18,18,17,16,15,15,14,14,14,13,13,13,13,12,11,11};

	/** DAS table */
	private static final int tableDAS[] = {16,10,10, 8, 8, 6, 6, 6, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3};

	/** Line counts when BGM changes occur */
	private static final int tableBGMChange[] = {50, 100, 150, -1};

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of ranking types */
	private static final int RANKING_TYPE = 2;

	/** Most recent scoring event type constants */
	private static final int EVENT_NONE = 0,
							 EVENT_SINGLE = 1,
							 EVENT_DOUBLE = 2,
							 EVENT_TRIPLE = 3,
							 EVENT_FOUR = 4,
							 EVENT_TSPIN_ZERO_MINI = 5,
							 EVENT_TSPIN_ZERO = 6,
							 EVENT_TSPIN_SINGLE_MINI = 7,
							 EVENT_TSPIN_SINGLE = 8,
							 EVENT_TSPIN_DOUBLE_MINI = 9,
							 EVENT_TSPIN_DOUBLE = 10,
							 EVENT_TSPIN_TRIPLE = 11,
							 EVENT_TSPIN_EZ = 12;

	/** Most recent increase in score */
	private int lastscore;

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

	/** Ending time */
	private int rolltime;

	/** Current BGM */
	private int bgmlv;

	/** Level at start time */
	private int startlevel;

	/** Flag for types of T-Spins allowed (0=none, 1=normal, 2=all spin) */
	private int tspinEnableType;

	/** Old flag for allowing T-Spins */
	private boolean enableTSpin;

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

	/** Endless flag */
	private boolean endless;

	/** Big */
	private boolean big;

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

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "EXTREME";
	}

	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.getOwner();
		receiver = engine.getOwner().receiver;
		lastscore = 0;
		scgettime = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;
		bgmlv = 0;
		rolltime = 0;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

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

		engine.staffrollEnable = true;
		engine.staffrollNoDeath = true;
		engine.staffrollEnableStatistics = true;

		engine.getOwner().backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_RED;
	}

	/**
	 * Set the gravity rate
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		int lv = engine.statistics.level;

		if(lv < 0) lv = 0;
		if(lv >= tableARE.length) lv = tableARE.length - 1;

		engine.speed.gravity = -1;
		engine.speed.are = tableARE[lv];
		engine.speed.areLine = tableARELine[lv];
		engine.speed.lineDelay = tableLineDelay[lv];
		engine.speed.lockDelay = tableLockDelay[lv];
		engine.speed.das = tableDAS[lv];
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(engine.getOwner().replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 8);

			if(change != 0) {
				engine.playSE("change");

				switch(menuCursor) {
				case 0:
					startlevel += change;
					if(startlevel < 0) startlevel = 19;
					if(startlevel > 19) startlevel = 0;
					engine.getOwner().backgroundStatus.bg = startlevel;
					break;
				case 1:
					//enableTSpin = !enableTSpin;
					tspinEnableType += change;
					if(tspinEnableType < 0) tspinEnableType = 2;
					if(tspinEnableType > 2) tspinEnableType = 0;
					break;
				case 2:
					enableTSpinKick = !enableTSpinKick;
					break;
				case 3:
					spinCheckType += change;
					if(spinCheckType < 0) spinCheckType = 1;
					if(spinCheckType > 1) spinCheckType = 0;
					break;
				case 4:
					tspinEnableEZ = !tspinEnableEZ;
					break;
				case 5:
					enableB2B = !enableB2B;
					break;
				case 6:
					enableCombo = !enableCombo;
					break;
				case 7:
					endless = !endless;
					break;
				case 8:
					big = !big;
					break;
				}

				// NET: Signal options change
				if(netIsNetPlay && (netNumSpectators() > 0)) {
					netSendOptions(engine);
				}
			}

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (menuTime >= 5)) {
				engine.playSE("decide");
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
	 * Render the settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		String strTSpinEnable = "";
		if(version >= 1) {
			if(tspinEnableType == 0) strTSpinEnable = "OFF";
			if(tspinEnableType == 1) strTSpinEnable = "T-ONLY";
			if(tspinEnableType == 2) strTSpinEnable = "ALL";
		} else {
			strTSpinEnable = GeneralUtil.getONorOFF(enableTSpin);
		}
		drawMenu(engine, playerID, receiver, 0, EventRenderer.COLOR_BLUE, 0,
				"LEVEL", String.valueOf(startlevel + 1),
				"SPIN BONUS", strTSpinEnable,
				"EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick),
				"SPIN TYPE", (spinCheckType == 0) ? "4POINT" : "IMMOBILE",
				"EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
				"B2B", GeneralUtil.getONorOFF(enableB2B),
				"COMBO",  GeneralUtil.getONorOFF(enableCombo),
				"ENDLESS", GeneralUtil.getONorOFF(endless),
				"BIG", GeneralUtil.getONorOFF(big));
	}

	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.statistics.levelDispAdd = 1;
		engine.b2bEnable = enableB2B;
		if(enableCombo == true) {
			engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
		} else {
			engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		}
		engine.big = big;

		if(netIsWatch()) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		} else {
			owner.bgmStatus.bgm = bgmlv + 2;
		}

		if(version >= 1) {
			engine.tspinAllowKick = enableTSpinKick;
			if(tspinEnableType == 0) {
				engine.tspinEnable = false;
			} else if(tspinEnableType == 1) {
				engine.tspinEnable = true;
			} else {
				engine.tspinEnable = true;
				engine.useAllSpinBonus = true;
			}
		} else {
			engine.tspinEnable = enableTSpin;
		}

		engine.spinCheckType = spinCheckType;
		engine.tspinEnableEZ = tspinEnableEZ;

		setSpeed(engine);
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, "EXTREME", EventRenderer.COLOR_RED);

		if( (engine.stat == GameEngine.Status.SETTING) || ((engine.stat == GameEngine.Status.RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  LINE TIME", EventRenderer.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					int endlessIndex = 0;
					if(endless) endlessIndex = 1;

					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventRenderer.COLOR_YELLOW, scale);
					receiver.drawScoreFont(engine, playerID,  3, topY+i, String.valueOf(rankingScore[endlessIndex][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLines[endlessIndex][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[endlessIndex][i]), (i == rankingRank), scale);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 2, "SCORE", EventRenderer.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf(engine.statistics.score) + "(+" + String.valueOf(lastscore) + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 3, strScore);

			receiver.drawScoreFont(engine, playerID, 0, 5, "LINE", EventRenderer.COLOR_BLUE);
			if((engine.statistics.level < 19) || ((endless == false) && (engine.ending == 0)))
				receiver.drawScoreFont(engine, playerID, 0, 6, engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10));
			else
				receiver.drawScoreFont(engine, playerID, 0, 6, engine.statistics.lines + "");

			receiver.drawScoreFont(engine, playerID, 0, 8, "LEVEL", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 9, String.valueOf(engine.statistics.level + 1));

			receiver.drawScoreFont(engine, playerID, 0, 11, "TIME", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 12, GeneralUtil.getTime(engine.statistics.time));

			if((engine.gameActive) && (engine.ending == 2)) {
				int remainRollTime = ROLLTIMELIMIT - rolltime;
				if(remainRollTime < 0) remainRollTime = 0;

				receiver.drawScoreFont(engine, playerID, 0, 14, "ROLL TIME", EventRenderer.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(remainRollTime),
						((remainRollTime > 0) && (remainRollTime < 10 * 60)));
			}

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
				case EVENT_TSPIN_ZERO_MINI:
					receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventRenderer.COLOR_PURPLE);
					break;
				case EVENT_TSPIN_ZERO:
					receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventRenderer.COLOR_PINK);
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

				if((lastcombo >= 2) && (lastevent != EVENT_TSPIN_ZERO_MINI) && (lastevent != EVENT_TSPIN_ZERO))
					receiver.drawMenuFont(engine, playerID, 2, 22, (lastcombo - 1) + "COMBO", EventRenderer.COLOR_CYAN);
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
		// Ending
		if((engine.gameActive) && (engine.ending == 2)) {
			rolltime++;

			// Time meter
			int remainRollTime = ROLLTIMELIMIT - rolltime;
			if(remainRollTime < 0) remainRollTime = 0;
			engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / ROLLTIMELIMIT;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(remainRollTime <= 30*60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(remainRollTime <= 20*60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(remainRollTime <= 10*60) engine.meterColor = GameEngine.METER_COLOR_RED;

			// Finished
			if(rolltime >= ROLLTIMELIMIT) {
				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.Status.EXCELLENT;
			}
		}

		scgettime++;
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Line clear bonus
		int pts = 0;

		if(engine.tspin) {
			// T-Spin 0 lines
			if((lines == 0) && (!engine.tspinez)) {
				if(engine.tspinmini) {
					pts += 100 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO_MINI;
				} else {
					pts += 400 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO;
				}
			}
			// Immobile EZ Spin
			else if(engine.tspinez && (lines > 0)) {
				if(engine.b2b) {
					pts += 180 * (engine.statistics.level + 1);
				} else {
					pts += 120 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_EZ;
			}
			// T-Spin 1 line
			else if(lines == 1) {
				if(engine.tspinmini) {
					if(engine.b2b) {
						pts += 300 * (engine.statistics.level + 1);
					} else {
						pts += 200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE_MINI;
				} else {
					if(engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE;
				}
			}
			// T-Spin 2 lines
			else if(lines == 2) {
				if(engine.tspinmini && engine.useAllSpinBonus) {
					if(engine.b2b) {
						pts += 600 * (engine.statistics.level + 1);
					} else {
						pts += 400 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_DOUBLE_MINI;
				} else {
					if(engine.b2b) {
						pts += 1800 * (engine.statistics.level + 1);
					} else {
						pts += 1200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_DOUBLE;
				}
			}
			// T-Spin 3 lines
			else if(lines >= 3) {
				if(engine.b2b) {
					pts += 2400 * (engine.statistics.level + 1);
				} else {
					pts += 1600 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_TRIPLE;
			}
		} else {
			if(lines == 1) {
				pts += 100 * (engine.statistics.level + 1); // 1Column
				lastevent = EVENT_SINGLE;
			} else if(lines == 2) {
				pts += 300 * (engine.statistics.level + 1); // 2Column
				lastevent = EVENT_DOUBLE;
			} else if(lines == 3) {
				pts += 500 * (engine.statistics.level + 1); // 3Column
				lastevent = EVENT_TRIPLE;
			} else if(lines >= 4) {
				// 4 lines
				if(engine.b2b) {
					pts += 1200 * (engine.statistics.level + 1);
				} else {
					pts += 800 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_FOUR;
			}
		}

		lastb2b = engine.b2b;

		// Combo
		if((enableCombo) && (engine.combo >= 1) && (lines >= 1)) {
			pts += ((engine.combo - 1) * 50) * (engine.statistics.level + 1);
			lastcombo = engine.combo;
		}

		// All clear
		if((lines >= 1) && (engine.field.isEmpty())) {
			engine.playSE("bravo");
			pts += 1800 * (engine.statistics.level + 1);
		}

		// Add to score
		if(pts > 0) {
			lastpiece = engine.nowPieceObject.id;
			lastscore = pts;
			scgettime = 0;
			if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
			else engine.statistics.scoreFromOtherBonus += pts;
			engine.statistics.score += pts;
		}

		if(engine.ending == 0) {
			// BGM fade-out effects and BGM changes
			if(tableBGMChange[bgmlv] != -1) {
				if(engine.statistics.lines >= tableBGMChange[bgmlv] - 5) owner.bgmStatus.fadesw = true;

				if(engine.statistics.lines >= tableBGMChange[bgmlv]) {
					bgmlv++;
					owner.bgmStatus.bgm = bgmlv + 2;
					owner.bgmStatus.fadesw = false;
				}
			}

			// Meter
			engine.meterValue = ((engine.statistics.lines % 10) * receiver.getMeterMax(engine)) / 9;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(engine.statistics.lines % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(engine.statistics.lines % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(engine.statistics.lines % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;

			if((engine.statistics.lines >= 200) && (endless == false)) {
				// Ending
				engine.playSE("levelup");
				engine.playSE("endingstart");
				owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
				owner.bgmStatus.fadesw = false;
				engine.bone = true;
				engine.ending = 2;
				engine.timerActive = false;
			} else if((engine.statistics.lines >= (engine.statistics.level + 1) * 10) && (engine.statistics.level < 19)) {
				// Level up
				engine.statistics.level++;

				owner.backgroundStatus.fadesw = true;
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = engine.statistics.level;

				setSpeed(engine);
				engine.playSE("levelup");
			}
		}
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, receiver, 0, EventRenderer.COLOR_BLUE,
				Statistic.SCORE, Statistic.LINES, Statistic.LEVEL, Statistic.TIME, Statistic.SPL, Statistic.LPM);
		drawResultRank(engine, playerID, receiver, 12, EventRenderer.COLOR_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, receiver, 14, EventRenderer.COLOR_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, receiver, 16, EventRenderer.COLOR_BLUE, netRankingRank[1]);

		if(netIsPB) {
			receiver.drawMenuFont(engine, playerID, 2, 21, "NEW PB", EventRenderer.COLOR_ORANGE);
		}

		if(netIsNetPlay && (netReplaySendStatus == 1)) {
			receiver.drawMenuFont(engine, playerID, 0, 22, "SENDING...", EventRenderer.COLOR_PINK);
		} else if(netIsNetPlay && !netIsWatch() && (netReplaySendStatus == 2)) {
			receiver.drawMenuFont(engine, playerID, 1, 22, "A: RETRY", EventRenderer.COLOR_RED);
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
		if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, endless);

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
		startlevel = prop.getProperty("extreme.startlevel", 0);
		tspinEnableType = prop.getProperty("extreme.tspinEnableType", 1);
		enableTSpin = prop.getProperty("extreme.enableTSpin", true);
		enableTSpinKick = prop.getProperty("extreme.enableTSpinKick", true);
		spinCheckType = prop.getProperty("extreme.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("extreme.tspinEnableEZ", false);
		enableB2B = prop.getProperty("extreme.enableB2B", true);
		enableCombo = prop.getProperty("extreme.enableCombo", true);
		endless = prop.getProperty("extreme.endless", false);
		big = prop.getProperty("extreme.big", false);
		version = prop.getProperty("extreme.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	protected void saveSetting(CustomProperties prop) {
		prop.setProperty("extreme.startlevel", startlevel);
		prop.setProperty("extreme.tspinEnableType", tspinEnableType);
		prop.setProperty("extreme.enableTSpin", enableTSpin);
		prop.setProperty("extreme.enableTSpinKick", enableTSpinKick);
		prop.setProperty("extreme.spinCheckType", spinCheckType);
		prop.setProperty("extreme.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("extreme.enableB2B", enableB2B);
		prop.setProperty("extreme.enableCombo", enableCombo);
		prop.setProperty("extreme.endless", endless);
		prop.setProperty("extreme.big", big);
		prop.setProperty("extreme.version", version);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int endlessIndex = 0; endlessIndex < 2; endlessIndex++) {
				rankingScore[endlessIndex][i] = prop.getProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".score." + i, 0);
				rankingLines[endlessIndex][i] = prop.getProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".lines." + i, 0);
				rankingTime[endlessIndex][i] = prop.getProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".time." + i, 0);
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
			for(int endlessIndex = 0; endlessIndex < 2; endlessIndex++) {
				prop.setProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".score." + i, rankingScore[endlessIndex][i]);
				prop.setProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".lines." + i, rankingLines[endlessIndex][i]);
				prop.setProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".time." + i, rankingTime[endlessIndex][i]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, int li, int time, boolean endlessMode) {
		rankingRank = checkRanking(sc, li, time, endlessMode);

		if(rankingRank != -1) {
			int endlessIndex = 0;
			if(endlessMode) endlessIndex = 1;

			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[endlessIndex][i] = rankingScore[endlessIndex][i - 1];
				rankingLines[endlessIndex][i] = rankingLines[endlessIndex][i - 1];
				rankingTime[endlessIndex][i] = rankingTime[endlessIndex][i - 1];
			}

			// Add new data
			rankingScore[endlessIndex][rankingRank] = sc;
			rankingLines[endlessIndex][rankingRank] = li;
			rankingTime[endlessIndex][rankingRank] = time;
		}
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int li, int time, boolean endlessMode) {
		int endlessIndex = 0;
		if(endlessMode) endlessIndex = 1;

		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScore[endlessIndex][i]) {
				return i;
			} else if((sc == rankingScore[endlessIndex][i]) && (li > rankingLines[endlessIndex][i])) {
				return i;
			} else if((sc == rankingScore[endlessIndex][i]) && (li == rankingLines[endlessIndex][i]) && (time < rankingTime[endlessIndex][i])) {
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
		s.setEndless(endless);
		s.setGameActive(engine.gameActive);
		s.setTimerActive(engine.timerActive);
		s.setLastScore(lastscore);
		s.setScGetTime(scgettime);
		s.setLastEvent(lastevent);
		s.setLastB2b(lastb2b);
		s.setLastCombo(lastcombo);
		s.setLastPiece(lastpiece);
		s.setBg(bg);
		s.setRollTime(rolltime);
		s.setMeterColor(engine.meterColor);
		s.setMeterValue(engine.meterValue);
		knetClient().fireUDP(GAME_STATS, s);
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)
	 */
	@Override
	protected void netRecvStats(GameEngine engine, KNetEvent e) {
		Stats s = (Stats) e.get(GAME_STATS);
		
		engine.statistics.copy(s.getStatistics());
		endless = s.isEndless();
		engine.gameActive = s.isGameActive();
		engine.timerActive = s.isTimerActive();
		lastscore = s.getLastScore();
		scgettime = s.getScGetTime();
		lastevent = s.getLastEvent();
		lastb2b = s.isLastB2b();
		lastcombo = s.getLastCombo();
		lastpiece = s.getLastPiece();
		engine.getOwner().backgroundStatus.bg = s.getBg();
		rolltime = s.getRollTime();
		engine.meterValue = s.getMeterValue();
		engine.meterColor = s.getMeterColor();
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
		o.setStartLevel(startlevel);
		o.setTspinEnableType(tspinEnableType);
		o.setEnableTSpinKick(enableTSpinKick);
		o.setEnableB2B(enableB2B);
		o.setEnableCombo(enableCombo);
		o.setEndless(endless);
		o.setBig(big);
		o.setSpinCheckType(spinCheckType);
		o.setTspinEnableEZ(tspinEnableEZ);
		knetClient().fireTCP(GAME_OPTIONS, o);
	}

	/**
	 * NET: Receive game options
	 */
	@Override
	protected void netRecvOptions(GameEngine engine, KNetEvent e) {
		Options o = (Options) e.get(GAME_OPTIONS);
		
		startlevel = o.getStartLevel();
		tspinEnableType = o.getTspinEnableType();
		enableTSpinKick = o.isEnableTSpinKick();
		enableB2B = o.isEnableB2B();
		enableCombo = o.isEnableCombo();
		endless = o.isEndless();
		big = o.isBig();
		spinCheckType = o.getSpinCheckType();
		tspinEnableEZ = o.isTspinEnableEZ();
	}

	/**
	 * NET: Get goal type
	 */
	@Override
	protected int netGetGoalType() {
		return endless ? 1 : 0;
	}

	/**
	 * NET: It returns true when the current settings doesn't prevent leaderboard screen from showing.
	 */
	@Override
	protected boolean netIsNetRankingViewOK(GameEngine engine) {
		return (startlevel == 0) && (!big) && (engine.ai == null);
	}
}
