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

import org.apache.log4j.Logger;
import org.zeromeaner.game.component.BGMStatus;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.component.SpeedParam;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

/**
 * DIG RACE Mode
 */
public class DigRaceMode extends AbstractNetMode {
	public static class Stats {
		private Statistics statistics;
		private int goalType;
		private boolean gameActive;
		private boolean timerActive;
		private int meterColor;
		private int meterValue;
		
		public Statistics getStatistics() {
			return statistics;
		}
		public void setStatistics(Statistics statistics) {
			this.statistics = statistics;
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
		public boolean isTimerActive() {
			return timerActive;
		}
		public void setTimerActive(boolean timerActive) {
			this.timerActive = timerActive;
		}
		public int getMeterColor() {
			return meterColor;
		}
		public void setMeterColor(int meterColor) {
			this.meterColor = meterColor;
		}
		public int getMeterValue() {
			return meterValue;
		}
		public void setMeterValue(int meterValue) {
			this.meterValue = meterValue;
		}
	}
	
	public static class EndGameStats {
		private Statistics statistics;
		private int garbageRemaining;
		private int garbageGoal;
		public Statistics getStatistics() {
			return statistics;
		}
		public void setStatistics(Statistics statistics) {
			this.statistics = statistics;
		}
		public int getGarbageRemaining() {
			return garbageRemaining;
		}
		public void setGarbageRemaining(int garbageRemaining) {
			this.garbageRemaining = garbageRemaining;
		}
		public int getGarbageGoal() {
			return garbageGoal;
		}
		public void setGarbageGoal(int garbageGoal) {
			this.garbageGoal = garbageGoal;
		}
	}
	
	public static class Options {
		private SpeedParam speed;
		private int bgmno;
		private int goalType;
		private int presetNumber;
		
		public SpeedParam getSpeed() {
			return speed;
		}
		public void setSpeed(SpeedParam speed) {
			this.speed = speed;
		}
		public int getBgmno() {
			return bgmno;
		}
		public void setBgmno(int bgmno) {
			this.bgmno = bgmno;
		}
		public int getGoalType() {
			return goalType;
		}
		public void setGoalType(int goalType) {
			this.goalType = goalType;
		}
		public int getPresetNumber() {
			return presetNumber;
		}
		public void setPresetNumber(int presetNumber) {
			this.presetNumber = presetNumber;
		}
	}
	
	/* ----- Main variables ----- */
	/** Logger */
	static Logger log = Logger.getLogger(DigRaceMode.class);

	/** Current version */
	private static final int CURRENT_VERSION = 1;

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of goal type */
	private static final int GOALTYPE_MAX = 3;

	/** Table of garbage lines */
	private static final int[] GOAL_TABLE = {5, 10, 18};

	/** BGM number */
	private int bgmno;

	/** Big (leftover from old versions) */
	private boolean big;

	/** Goal type (0=5 garbages, 1=10 garbages, 2=18 garbages) */
	private int goaltype;

	/** Current version */
	private int version;

	/** Last preset number used */
	private int presetNumber;

	/** Current round's ranking rank */
	private int rankingRank;

	/** Rankings' times */
	private int[][] rankingTime;

	/** Rankings' line counts */
	private int[][] rankingLines;

	/** Rankings' piece counts */
	private int[][] rankingPiece;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "DIG RACE";
	}

	/*
	 * Initialization for each player
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.getOwner();
		receiver = engine.getOwner().receiver;

		bgmno = 0;
		big = false;
		goaltype = 0;
		presetNumber = 0;

		rankingRank = -1;
		rankingTime = new int[GOALTYPE_MAX][RANKING_MAX];
		rankingLines = new int[GOALTYPE_MAX][RANKING_MAX];
		rankingPiece = new int[GOALTYPE_MAX][RANKING_MAX];

		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;

		netPlayerInit(engine, playerID);

		if(engine.getOwner().replayMode == false) {
			version = CURRENT_VERSION;
			presetNumber = engine.getOwner().modeConfig.getProperty("digrace.presetNumber", 0);
			loadPreset(engine, engine.getOwner().modeConfig, -1);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
		} else {
			version = engine.getOwner().replayProp.getProperty("digrace.version", 0);
			presetNumber = 0;
			loadPreset(engine, engine.getOwner().replayProp, -1);

			// NET: Load name
			netPlayerName = engine.getOwner().replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}
	}

	/**
	 * Load options from a preset
	 * @param engine GameEngine
	 * @param prop Property file to read from
	 * @param preset Preset number
	 */
	private void loadPreset(GameEngine engine, CustomProperties prop, int preset) {
		engine.speed.gravity = prop.getProperty("digrace.gravity." + preset, 4);
		engine.speed.denominator = prop.getProperty("digrace.denominator." + preset, 256);
		engine.speed.are = prop.getProperty("digrace.are." + preset, 0);
		engine.speed.areLine = prop.getProperty("digrace.areLine." + preset, 0);
		engine.speed.lineDelay = prop.getProperty("digrace.lineDelay." + preset, 0);
		engine.speed.lockDelay = prop.getProperty("digrace.lockDelay." + preset, 30);
		engine.speed.das = prop.getProperty("digrace.das." + preset, 14);
		bgmno = prop.getProperty("digrace.bgmno." + preset, 0);
		big = prop.getProperty("digrace.big." + preset, false);
		goaltype = prop.getProperty("digrace.goaltype." + preset, 1);
	}

	/**
	 * Save options to a preset
	 * @param engine GameEngine
	 * @param prop Property file to save to
	 * @param preset Preset number
	 */
	private void savePreset(GameEngine engine, CustomProperties prop, int preset) {
		prop.setProperty("digrace.gravity." + preset, engine.speed.gravity);
		prop.setProperty("digrace.denominator." + preset, engine.speed.denominator);
		prop.setProperty("digrace.are." + preset, engine.speed.are);
		prop.setProperty("digrace.areLine." + preset, engine.speed.areLine);
		prop.setProperty("digrace.lineDelay." + preset, engine.speed.lineDelay);
		prop.setProperty("digrace.lockDelay." + preset, engine.speed.lockDelay);
		prop.setProperty("digrace.das." + preset, engine.speed.das);
		prop.setProperty("digrace.bgmno." + preset, bgmno);
		prop.setProperty("digrace.big." + preset, big);
		prop.setProperty("digrace.goaltype." + preset, goaltype);
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(engine.getOwner().replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 10, playerID);

			if(change != 0) {
				engine.playSE("change");

				int m = 1;
				if(engine.ctrl.isPress(Controller.BUTTON_E)) m = 100;
				if(engine.ctrl.isPress(Controller.BUTTON_F)) m = 1000;

				switch(menuCursor) {
				case 0:
					engine.speed.gravity += change * m;
					if(engine.speed.gravity < -1) engine.speed.gravity = 99999;
					if(engine.speed.gravity > 99999) engine.speed.gravity = -1;
					break;
				case 1:
					engine.speed.denominator += change * m;
					if(engine.speed.denominator < -1) engine.speed.denominator = 99999;
					if(engine.speed.denominator > 99999) engine.speed.denominator = -1;
					break;
				case 2:
					engine.speed.are += change;
					if(engine.speed.are < 0) engine.speed.are = 99;
					if(engine.speed.are > 99) engine.speed.are = 0;
					break;
				case 3:
					engine.speed.areLine += change;
					if(engine.speed.areLine < 0) engine.speed.areLine = 99;
					if(engine.speed.areLine > 99) engine.speed.areLine = 0;
					break;
				case 4:
					engine.speed.lineDelay += change;
					if(engine.speed.lineDelay < 0) engine.speed.lineDelay = 99;
					if(engine.speed.lineDelay > 99) engine.speed.lineDelay = 0;
					break;
				case 5:
					engine.speed.lockDelay += change;
					if(engine.speed.lockDelay < 0) engine.speed.lockDelay = 99;
					if(engine.speed.lockDelay > 99) engine.speed.lockDelay = 0;
					break;
				case 6:
					engine.speed.das += change;
					if(engine.speed.das < 0) engine.speed.das = 99;
					if(engine.speed.das > 99) engine.speed.das = 0;
					break;
				case 7:
					bgmno += change;
					if(bgmno < 0) bgmno = BGMStatus.BGM_COUNT - 1;
					if(bgmno > BGMStatus.BGM_COUNT - 1) bgmno = 0;
					break;
				case 8:
					goaltype += change;
					if(goaltype < 0) goaltype = 2;
					if(goaltype > 2) goaltype = 0;
					break;
				case 9:
				case 10:
					presetNumber += change;
					if(presetNumber < 0) presetNumber = 99;
					if(presetNumber > 99) presetNumber = 0;
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

				if(menuCursor == 9) {
					// Load preset
					loadPreset(engine, owner.modeConfig, presetNumber);

					// NET: Signal options change
					if(netIsNetPlay && (netNumSpectators() > 0)) {
						netSendOptions(engine);
					}
				} else if(menuCursor == 10) {
					// Save preset
					savePreset(engine, owner.modeConfig, presetNumber);
					receiver.saveModeConfig(owner.modeConfig);
				} else {
					// Save settings
					owner.modeConfig.setProperty("digrace.presetNumber", presetNumber);
					savePreset(engine, owner.modeConfig, -1);
					receiver.saveModeConfig(owner.modeConfig);

					// NET: Signal start of the game
					if(netIsNetPlay) 
						knetClient().fireTCP(START_1P, true);

					// Start game
					return false;
				}
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
		drawMenu(engine, playerID, receiver, 0, EventRenderer.COLOR_BLUE, 0,
				"GRAVITY", String.valueOf(engine.speed.gravity),
				"G-MAX", String.valueOf(engine.speed.denominator),
				"ARE", String.valueOf(engine.speed.are),
				"ARE LINE", String.valueOf(engine.speed.areLine),
				"LINE DELAY", String.valueOf(engine.speed.lineDelay),
				"LOCK DELAY", String.valueOf(engine.speed.lockDelay),
				"DAS", String.valueOf(engine.speed.das),
				"BGM", String.valueOf(bgmno),
				"GOAL", String.valueOf(GOAL_TABLE[goaltype]));
		if (!engine.getOwner().replayMode)
		{
			menuColor = EventRenderer.COLOR_GREEN;
			drawMenuCompact(engine, playerID, receiver,
				"LOAD", String.valueOf(presetNumber),
				"SAVE", String.valueOf(presetNumber));
		}
	}

	/*
	 * Ready
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) {
			if((!netIsNetPlay) || (!netIsWatch())) {
				engine.createFieldIfNeeded();
				fillGarbage(engine, goaltype);

				// Update meter
				engine.meterValue = GOAL_TABLE[goaltype] * receiver.getBlockGraphicsHeight(engine, playerID);
				engine.meterColor = GameEngine.METER_COLOR_GREEN;

				// NET: Send field
				if(netNumSpectators() > 0) {
					netSendField(engine, false);
				}
			}
		}
		return false;
	}

	/*
	 * This function will be called before the game actually begins (after Ready&Go screen disappears)
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		if(version <= 0) {
			engine.big = big;
		}

		if(netIsWatch()) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		} else {
			owner.bgmStatus.bgm = bgmno;
		}
	}

	/**
	 * Fill the playfield with garbage
	 * @param engine GameEngine
	 * @param height Garbage height level number
	 */
	private void fillGarbage(GameEngine engine, int height) {
		int w = engine.field.getWidth();
		int h = engine.field.getHeight();
		int hole = -1;

		for(int y = h - 1; y >= h - GOAL_TABLE[height]; y--) {
			int newhole = -1;
			do {
				newhole = engine.random.nextInt(w);
			} while(newhole == hole);
			hole = newhole;

			int prevColor = -1;
			for(int x = 0; x < w; x++) {
				if(x != hole) {
					int color = Block.BLOCK_COLOR_GRAY;
					if(y == h - 1) {
						do {
							color = Block.BLOCK_COLOR_GEM_RED + engine.random.nextInt(7);
						} while(color == prevColor);
						prevColor = color;
					}
					engine.field.setBlock(x,y,new Block(color,engine.getSkin(),Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_GARBAGE));
				}
			}

			// Set connections
			if(receiver.isStickySkin(engine) && (y != h - 1)) {
				for(int x = 0; x < w; x++) {
					if(x != hole) {
						Block blk = engine.field.getBlock(x, y);
						if(blk != null) {
							if(!engine.field.getBlockEmpty(x-1, y)) blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
							if(!engine.field.getBlockEmpty(x+1, y)) blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
						}
					}
				}
			}
		}
	}

	private int getRemainGarbageLines(GameEngine engine, int height) {
		if((engine == null) || (engine.field == null)) return -1;

		int w = engine.field.getWidth();
		int h = engine.field.getHeight();
		int lines = 0;

		for(int y = h - 1; y >= h - GOAL_TABLE[height]; y--) {
			if(!engine.field.getLineFlag(y)) {
				for(int x = 0; x < w; x++) {
					Block blk = engine.field.getBlock(x, y);

					if((blk != null) && (blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE))) {
						lines++;
						break;
					}
				}
			}
		}

		return lines;
	}

	/*
	 * Score display
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, "DIG RACE", EventRenderer.COLOR_GREEN);
		receiver.drawScoreFont(engine, playerID, 0, 1, "(" + GOAL_TABLE[goaltype] + " GARBAGE GAME)", EventRenderer.COLOR_GREEN);

		if( (engine.stat == GameEngine.Status.SETTING) || ((engine.stat == GameEngine.Status.RESULT) && (owner.replayMode == false)) ) {
			if(!owner.replayMode && (engine.ai == null) && !netIsWatch()) {
				String strPieceTemp = (owner.receiver.getNextDisplayType() == 2) ? "P." : "PIECE";
				receiver.drawScoreFont(engine, playerID, 3, 3, "TIME     LINE " + strPieceTemp, EventRenderer.COLOR_BLUE);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID, 0, 4 + i, String.format("%2d", i + 1), EventRenderer.COLOR_YELLOW);
					receiver.drawScoreFont(engine, playerID, 3, 4 + i, GeneralUtil.getTime(rankingTime[goaltype][i]), (rankingRank == i));
					receiver.drawScoreFont(engine, playerID, 12, 4 + i, String.valueOf(rankingLines[goaltype][i]), (rankingRank == i));
					receiver.drawScoreFont(engine, playerID, 17, 4 + i, String.valueOf(rankingPiece[goaltype][i]), (rankingRank == i));
				}
			}
		} else {
			int remainLines = getRemainGarbageLines(engine, goaltype);
			String strLines = String.valueOf(remainLines);
			if(remainLines < 0) strLines = "0";
			int fontcolor = EventRenderer.COLOR_WHITE;
			if((remainLines <= 14) && (remainLines > 0)) fontcolor = EventRenderer.COLOR_YELLOW;
			if((remainLines <=  8) && (remainLines > 0)) fontcolor = EventRenderer.COLOR_ORANGE;
			if((remainLines <=  4) && (remainLines > 0)) fontcolor = EventRenderer.COLOR_RED;

			if(remainLines > 0) {
				if(strLines.length() == 1) {
					receiver.drawMenuFont(engine, playerID, 4, 21, strLines, fontcolor, 2.0f);
				} else if(strLines.length() == 2) {
					receiver.drawMenuFont(engine, playerID, 3, 21, strLines, fontcolor, 2.0f);
				} else if(strLines.length() == 3) {
					receiver.drawMenuFont(engine, playerID, 2, 21, strLines, fontcolor, 2.0f);
				}
			}

			receiver.drawScoreFont(engine, playerID, 0, 3, "LINE", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 4, String.valueOf(engine.statistics.lines));

			receiver.drawScoreFont(engine, playerID, 0, 6, "PIECE", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(engine.statistics.totalPieceLocked));

			receiver.drawScoreFont(engine, playerID, 0, 9, "LINE/MIN", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.lpm));

			receiver.drawScoreFont(engine, playerID, 0, 12, "PIECE/SEC", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, String.valueOf(engine.statistics.pps));

			receiver.drawScoreFont(engine, playerID, 0, 15, "TIME", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 16, GeneralUtil.getTime(engine.statistics.time));
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
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Update meter
		int remainLines = getRemainGarbageLines(engine, goaltype);
		engine.meterValue = remainLines * receiver.getBlockGraphicsHeight(engine, playerID);
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(remainLines <= 14) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(remainLines <= 8) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(remainLines <= 4) engine.meterColor = GameEngine.METER_COLOR_RED;

		// Game is completed when there is no gem blocks
		if((lines > 0) && (remainLines == 0)) {
			engine.ending = 1;
			engine.gameEnded();
		}
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, receiver, 1, EventRenderer.COLOR_BLUE,
				Statistic.LINES, Statistic.PIECE, Statistic.TIME, Statistic.LPM, Statistic.PPS);
		drawResultRank(engine, playerID, receiver, 11, EventRenderer.COLOR_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, receiver, 13, EventRenderer.COLOR_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, receiver, 15, EventRenderer.COLOR_BLUE, netRankingRank[1]);

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
	 * Save replay file
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		engine.getOwner().replayProp.setProperty("digrace.version", version);
		savePreset(engine, engine.getOwner().replayProp, -1);

		// NET: Save name
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
		}

		// Update rankings
		if((!owner.replayMode) && (getRemainGarbageLines(engine, goaltype) == 0) && (engine.ending != 0) && (engine.ai == null) && (!netIsWatch())) {
			updateRanking(engine.statistics.time, engine.statistics.lines, engine.statistics.totalPieceLocked);

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < GOALTYPE_MAX; i++) {
			for(int j = 0; j < RANKING_MAX; j++) {
				rankingTime[i][j] = prop.getProperty("digrace.ranking." + ruleName + "." + i + ".time." + j, -1);
				rankingLines[i][j] = prop.getProperty("digrace.ranking." + ruleName + "." + i + ".lines." + j, 0);
				rankingPiece[i][j] = prop.getProperty("digrace.ranking." + ruleName + "." + i + ".piece." + j, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < GOALTYPE_MAX; i++) {
			for(int j = 0; j < RANKING_MAX; j++) {
				prop.setProperty("digrace.ranking." + ruleName + "." + i + ".time." + j, rankingTime[i][j]);
				prop.setProperty("digrace.ranking." + ruleName + "." + i + ".lines." + j, rankingLines[i][j]);
				prop.setProperty("digrace.ranking." + ruleName + "." + i + ".piece." + j, rankingPiece[i][j]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param time Time
	 * @param piece Piececount
	 */
	private void updateRanking(int time, int lines, int piece) {
		rankingRank = checkRanking(time, lines, piece);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingTime[goaltype][i] = rankingTime[goaltype][i - 1];
				rankingLines[goaltype][i] = rankingLines[goaltype][i - 1];
				rankingPiece[goaltype][i] = rankingPiece[goaltype][i - 1];
			}

			// Add new data
			rankingTime[goaltype][rankingRank] = time;
			rankingLines[goaltype][rankingRank] = lines;
			rankingPiece[goaltype][rankingRank] = piece;
		}
	}

	/**
	 * Calculate ranking position
	 * @param time Time
	 * @param piece Piececount
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int time, int lines, int piece) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if((time < rankingTime[goaltype][i]) || (rankingTime[goaltype][i] < 0)) {
				return i;
			} else if((time == rankingTime[goaltype][i]) && (lines < rankingLines[goaltype][i])) {
				return i;
			} else if((time == rankingTime[goaltype][i]) && (lines == rankingLines[goaltype][i]) && (piece < rankingPiece[goaltype][i])) {
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
		Stats s = new Stats();
		s.setStatistics(engine.statistics);
		s.setGoalType(goaltype);
		s.setGameActive(engine.gameActive);
		s.setTimerActive(engine.timerActive);
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
		goaltype = s.getGoalType();
		goaltype = s.getGoalType();
		engine.gameActive = s.isGameActive();
		engine.timerActive = s.isTimerActive();
		engine.meterColor = s.getMeterColor();
		engine.meterValue = s.getMeterValue();
	}

	/**
	 * NET: Send end-of-game stats
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		EndGameStats egs = new EndGameStats();
		egs.setStatistics(engine.statistics);
		egs.setGarbageRemaining(GOAL_TABLE[goaltype] - getRemainGarbageLines(engine, goaltype));
		egs.setGarbageGoal(GOAL_TABLE[goaltype]);
		knetClient().fireTCP(GAME_END_STATS, egs);
	}
	
	/**
	 * NET: Send game options to all spectators
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendOptions(GameEngine engine) {
		Options o = new Options();
		o.setSpeed(engine.speed);
		o.setBgmno(bgmno);
		o.setGoalType(goaltype);
		o.setPresetNumber(presetNumber);
		knetClient().fireTCP(GAME_OPTIONS, o);
	}

	/**
	 * NET: Receive game options
	 */
	@Override
	protected void netRecvOptions(GameEngine engine, KNetEvent e) {
		Options o = (Options) e.get(GAME_OPTIONS);
		
		engine.speed.copy(o.getSpeed());
		bgmno = o.getBgmno();
		goaltype = o.getGoalType();
		presetNumber = o.getPresetNumber();
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
		return (engine.ai == null);
	}

	/**
	 * NET: It returns true when the current settings doesn't prevent replay data from sending.
	 */
	@Override
	protected boolean netIsNetRankingSendOK(GameEngine engine) {
		return netIsNetRankingViewOK(engine) && (getRemainGarbageLines(engine, goaltype) == 0) && (engine.ending != 0);
	}
}
