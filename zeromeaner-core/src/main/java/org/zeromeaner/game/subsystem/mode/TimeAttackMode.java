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

import static org.zeromeaner.knet.KNetEventArgs.START_1P;

import org.zeromeaner.game.component.BGMStatus;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;
/**
 * TIME ATTACK mode (Original from NullpoUE build 010210 by Zircean. This mode is heavily modified from the original.)
 */
public class TimeAttackMode extends AbstractNetMode {
	/** Current version of this mode */
	private static final int CURRENT_VERSION = 1;

	/** Gravity tables */
	private static final int tableGravity[][] =
	{
		{ 4, 12, 48, 72, 96, 128, 256, 384, 512, 768, 1024, 1280, -1},	// NORMAL
		{84,128,256,512,768,1024,1280,  -1},							// HIGH SPEED 1
		{-1},															// HIGH SPEED 2
		{-1},															// ANOTHER
		{-1},															// ANOTHER 2
		{ 4, 12, 48, 72, 96, 128, 256, 384, 512, 768, 1024, 1280, -1},	// NORMAL 200
		{-1},															// ANOTHER 200
		{ 1,  3, 15, 30, 60, 120, 180, 240, 300, 300, -1},				// BASIC
		{-1},															// HELL
		{-1},															// HELL-X
		{-1},															// VOID
	};

	/** Denominator table */
	private static final int tableDenominator[] =
	{
		256,	// NORMAL
		256,	// HIGH SPEED 1
		256,	// HIGH SPEED 2
		256,	// ANOTHER
		256,	// ANOTHER2
		256,	// NORMAL 200
		256,	// ANOTHER 200
		60,		// BASIC
		256,	// HELL
		256,	// HELL-X
		256,	// VOID
	};

	/** Max level table */
	private static final int tableGoalLevel[] =
	{
		15,	// NORMAL
		15,	// HIGH SPEED 1
		15,	// HIGH SPEED 2
		15,	// ANOTHER
		15,	// ANOTHER2
		20,	// NORMAL 200
		20,	// ANOTHER 200
		20,	// BASIC
		20,	// HELL
		20,	// HELL-X
		20,	// VOID
	};

	/** Level timer tables */
	private static final int tableLevelTimer[][] =
	{
		{7200, 7200, 5400},										// NORMAL
		{7200, 7200, 5400},										// HIGH SPEED 1
		{7200, 7200, 5400},										// HIGH SPEED 2
		{3600},													// ANOTHER
		{7200, 7200, 5400},										// ANOTHER 2
		{7200, 7200, 5400},										// NORMAL 200
		{3600},													// ANOTHER 200
		{1800, 1800, 1800, 1800, 1800, 1500, 1500, 1500, 1500, 1500,
			1200, 1200, 1200, 1200, 1200, 1200, 1200, 1200, 1200, 900},	// BASIC
		{1800, 1800, 1800, 1800, 1800, 1500, 1500, 1500, 1500, 1500,
			1200, 1200, 1200, 1020, 900, 1200, 1020, 900, 840},	// HELL
		{1800, 1800, 1800, 1800, 1800, 1500, 1500, 1500, 1500, 1500,
			1200, 1200, 1200, 1020, 900, 1200, 1020, 900, 840},	// HELL-X
		{1800, 1200, 900, 900, 900, 840, 840, 840, 840, 840, 720,
			720, 720, 720, 720, 540, 480, 420, 360, 300},		// VOID
	};

	/** Speed table for ANOTHER */
	private static final int tableAnother[][] =
	{
		{18,14,14,14,12,12,10, 8, 7, 6}, // ARE
		{14, 8, 8, 5, 5, 5, 5, 5, 5, 5}, // Line delay
		{28,24,22,20,18,14,14,13,13,13}, // Lock delay
		{10,10, 9, 9, 9, 8, 8, 7, 7, 7}  // DAS
	};

	/** Speed table for NORMAL 200 */
	private static final int tableNormal200[][] =
	{
		{25,25,25,25,25,25,25,25,25,25,25,25,25,25,22,16,16,12,12,10},	// ARE
		{25,25,25,25,25,25,25,25,25,25,25,25,25,25,16,16,16,12,12, 8},	// Line delay
		{30,30,30,30,30,30,30,30,30,30,30,30,30,30,30,25,24,22,20,17},	// Lock delay
		{15,15,15,15,15,15,15,15,15,15,15,15,15,15,12,10, 9, 8, 8, 6}	// DAS
	};

	/** Speed table for VOID */
	private static final int tableVoid[][] =
	{
		{ 2, 2,1,1,1,0,0,0,0,0,0,0,0,0,0,0},	// ARE
		{ 3, 1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},	// Line delay
		{11,10,9,9,9,9,9,9,9,9,9,9,9,9,9,8},	// Lock delay
		{ 7, 5,5,4,4,3,3,3,3,3,3,3,3,3,3,2}		// DAS
	};

	/** Speed table for BASIC */
	private static final int tableBasic[][] =
	{
		{26,26,26,26,26,26,26,26,26,26,15,11,11,11,11,10, 9, 5, 3, 2}, // ARE
		{40,40,40,30,30,25,25,25,25,25,20,15,12,10, 6, 5, 4, 3, 3, 3}, // Line delay
		{28,28,28,26,26,26,26,26,25,25,25,23,23,23,20,20,18,18,14,11}, // Lock delay
		{15,15,15,15,15,15,15,15,15,15,10, 9, 9, 8, 8, 7, 7, 7, 7, 7}  // DAS
	};

	/** BGM change lines table */
	private static final int tableBGMChange[][] =
	{
		{50, 100},	// NORMAL
		{50, 100},	// HI-SPEED 1
		{50, 100},	// HI-SPEED 2
		{40, 100},	// ANOTHER
		{40, 100},	// ANOTHER2
		{50, 150},	// NORMAL 200
		{40, 150},	// ANOTHER 200
		{50, 150},	// BASIC
		{50, 150},	// HELL
		{50, 150},	// HELL-X
		{},			// VOID
	};

	/** BGM fadeout lines table */
	private static final int tableBGMFadeout[][] =
	{
		{45, 95, 145},	// NORMAL
		{45, 95, 145},	// HI-SPEED 1
		{45, 95, 145},	// HI-SPEED 2
		{35, 95, 145},	// ANOTHER
		{35, 95, 145},	// ANOTHER2
		{45, 145, 195},	// NORMAL 200
		{45, 145, 195},	// ANOTHER 200
		{45, 145, 195},	// BASIC
		{45, 145, 195},	// HELL
		{45, 145, 195},	// HELL-X
		{195},			// VOID
	};

	/** BGM kind table */
	private static final int tableBGMNumber[][] =
	{
		{BGMStatus.BGM_SPECIAL1, BGMStatus.BGM_NORMAL2,  BGMStatus.BGM_NORMAL3},	// NORMAL
		{BGMStatus.BGM_NORMAL1,  BGMStatus.BGM_NORMAL3,  BGMStatus.BGM_NORMAL6},	// HI-SPEED 1
		{BGMStatus.BGM_NORMAL3,  BGMStatus.BGM_NORMAL6,  BGMStatus.BGM_NORMAL4},	// HI-SPEED 2
		{BGMStatus.BGM_NORMAL6,  BGMStatus.BGM_NORMAL4,  BGMStatus.BGM_NORMAL5},	// ANOTHER
		{BGMStatus.BGM_NORMAL6,  BGMStatus.BGM_NORMAL4,  BGMStatus.BGM_NORMAL5},	// ANOTHER2
		{BGMStatus.BGM_SPECIAL1, BGMStatus.BGM_NORMAL2,  BGMStatus.BGM_NORMAL3},	// NORMAL 200
		{BGMStatus.BGM_NORMAL6,  BGMStatus.BGM_NORMAL4,  BGMStatus.BGM_NORMAL5},	// ANOTHER 200
		{BGMStatus.BGM_NORMAL1,  BGMStatus.BGM_NORMAL2,  BGMStatus.BGM_NORMAL3},	// BASIC
		{BGMStatus.BGM_PUZZLE4,  BGMStatus.BGM_SPECIAL4, BGMStatus.BGM_SPECIAL2},	// HELL
		{BGMStatus.BGM_NORMAL4,  BGMStatus.BGM_NORMAL5,  BGMStatus.BGM_SPECIAL3},	// HELL-X
		{BGMStatus.BGM_NORMAL6},	// VOID
	};

	/** Game types */
	private static final int GAMETYPE_NORMAL = 0,
							 GAMETYPE_HIGHSPEED1 = 1,
							 GAMETYPE_HIGHSPEED2 = 2,
							 GAMETYPE_ANOTHER = 3,
							 GAMETYPE_ANOTHER2 = 4,
							 GAMETYPE_NORMAL200 = 5,
							 GAMETYPE_ANOTHER200 = 6,
							 GAMETYPE_BASIC = 7,
							 GAMETYPE_HELL = 8,
							 GAMETYPE_HELLX = 9,
							 GAMETYPE_VOID = 10;

	/** Number of game types */
	private static final int GAMETYPE_MAX = 11;

	/** Game type names (short) */
	private static final String[] GAMETYPE_NAME = {"NORMAL","HISPEED1","HISPEED2","ANOTHER","ANOTHER2",
		"NORM200","ANOTH200","BASIC","HELL","HELL-X","VOID"};

	/** Game type names (long) */
	private static final String[] GAMETYPE_NAME_LONG = {"NORMAL","HIGH SPEED 1","HIGH SPEED 2","ANOTHER","ANOTHER 2",
		"NORMAL 200","ANOTHER 200","BASIC","HELL","HELL-X","VOID"};

	/** HELL-X fade table */
	private static final int tableHellXFade[] = {-1,-1,-1,-1,-1,150,150,150,150,150,
		150,150,150,150,150,120,120,120,120,60};

	/** Ending time limit */
	private static final int ROLLTIMELIMIT = 3238;

	/** Number of ranking records */
	private static final int RANKING_MAX = 10;

	/** Number of ranking types */
	private static final int RANKING_TYPE = 11;

	/** EventRenderer object (This receives many game events, can also be used for drawing the fonts.) */

	/** Remaining level time */
	private int levelTimer;

	/** Original level time */
	private int levelTimerMax;

	/** Current lines (for levelup) */
	private int norm;

	/** Current BGM number */
	private int bgmlv;

	/** Elapsed ending time */
	private int rolltime;

	/** Ending started flag */
	private boolean rollstarted;

	/** Section time */
	private int[] sectiontime;

	/** Number of sections completed */
	private int sectionscomp;

	/** Average section time */
	private int sectionavgtime;

	/** Game type */
	private int goaltype;

	/** Selected starting level */
	private int startlevel;

	/** Big mode on/off */
	private boolean big;

	/** Show section time */
	private boolean showsectiontime;

	/** Version of this mode */
	private int version;

	/** Your place on leaderboard (-1: out of rank) */
	private int rankingRank;

	/** Line records */
	private int[][] rankingLines;

	/** Time records */
	private int[][] rankingTime;

	/** Game completed flag records */
	private int[][] rankingRollclear;

	/**
	 * Returns the name of this mode
	 */
	@Override
	public String getName() {
		return "TIME ATTACK";
	}

	/**
	 * This function will be called when the game enters the main game screen.
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.getOwner();
		receiver = engine.getOwner().receiver;

		norm = 0;
		goaltype = 0;
		startlevel = 0;
		rolltime = 0;
		rollstarted = false;
		sectiontime = new int[20];
		sectionscomp = 0;
		sectionavgtime = 0;
		big = false;
		showsectiontime = true;

		rankingRank = -1;
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];
		rankingRollclear = new int[RANKING_TYPE][RANKING_MAX];

		engine.tspinEnable = false;
		engine.b2bEnable = false;
		engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
		engine.bighalf = true;
		engine.bigmove = true;
		engine.staffrollEnable = false;
		engine.staffrollNoDeath = false;

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
	 * Set the gravity speed and some other things
	 * @param engine GameEngine object
	 */
	private void setSpeed(GameEngine engine) {
		// Gravity speed
		int gravlv = engine.statistics.level;
		if(gravlv < 0) gravlv = 0;
		if(gravlv >= tableGravity[goaltype].length) gravlv = tableGravity[goaltype].length - 1;
		engine.speed.gravity = tableGravity[goaltype][gravlv];
		engine.speed.denominator = tableDenominator[goaltype];

		// Other speed values
		int speedlv = engine.statistics.level;
		if(speedlv < 0) speedlv = 0;

		switch(goaltype) {
		case GAMETYPE_NORMAL:
		case GAMETYPE_HIGHSPEED1:
		case GAMETYPE_HIGHSPEED2:
			engine.speed.are = 25;
			engine.speed.areLine = 25;
			engine.speed.lineDelay = 41;
			engine.speed.lockDelay = 30;
			engine.speed.das = 15;
			break;
		case GAMETYPE_ANOTHER:
		case GAMETYPE_ANOTHER200:
			if(speedlv >= tableAnother[0].length) speedlv = tableAnother[0].length - 1;
			engine.speed.are = tableAnother[0][speedlv];
			engine.speed.areLine = tableAnother[0][speedlv];
			engine.speed.lineDelay = tableAnother[1][speedlv];
			engine.speed.lockDelay = tableAnother[2][speedlv];
			engine.speed.das = tableAnother[3][speedlv];
			break;
		case GAMETYPE_ANOTHER2:
			engine.speed.are = 6;
			engine.speed.areLine = 6;
			engine.speed.lineDelay = 4;
			engine.speed.lockDelay = 13;
			engine.speed.das = 7;
			break;
		case GAMETYPE_NORMAL200:
			if(speedlv >= tableNormal200[0].length) speedlv = tableNormal200[0].length - 1;
			engine.speed.are = tableNormal200[0][speedlv];
			engine.speed.areLine = tableNormal200[0][speedlv];
			engine.speed.lineDelay = tableNormal200[1][speedlv];
			engine.speed.lockDelay = tableNormal200[2][speedlv];
			engine.speed.das = tableNormal200[3][speedlv];
			break;
		case GAMETYPE_BASIC:
			if(speedlv >= tableBasic[0].length) speedlv = tableBasic[0].length - 1;
			engine.speed.are = tableBasic[0][speedlv];
			engine.speed.areLine = tableBasic[0][speedlv];
			engine.speed.lineDelay = tableBasic[1][speedlv];
			engine.speed.lockDelay = tableBasic[2][speedlv];
			engine.speed.das = tableBasic[3][speedlv];
			break;
		case GAMETYPE_HELL:
		case GAMETYPE_HELLX:
			engine.speed.are = 2;
			engine.speed.areLine = 2;
			engine.speed.lineDelay = 3;
			engine.speed.lockDelay = 11;
			engine.speed.das = 7;
			break;
		case GAMETYPE_VOID:
			if(speedlv >= tableVoid[0].length) speedlv = tableVoid[0].length - 1;
			engine.speed.are = tableVoid[0][speedlv];
			engine.speed.areLine = tableVoid[0][speedlv];
			engine.speed.lineDelay = tableVoid[1][speedlv];
			engine.speed.lockDelay = tableVoid[2][speedlv];
			engine.speed.das = tableVoid[3][speedlv];
			break;
		}

		// Level timer
		int timelv = engine.statistics.level;
		if(timelv < 0) timelv = 0;
		if(timelv >= tableLevelTimer[goaltype].length) timelv = tableLevelTimer[goaltype].length - 1;
		levelTimerMax = levelTimer = tableLevelTimer[goaltype][timelv];

		// Show outline only
		if(goaltype == GAMETYPE_HELL) {
			engine.blockShowOutlineOnly = true;
		}
		// Bone blocks
		if( (goaltype == GAMETYPE_HELLX) || ((goaltype == GAMETYPE_HELL) && (engine.statistics.level >= 15)) || (goaltype == GAMETYPE_VOID) ) {
			engine.bone = true;
		}
		// Block fade for HELL-X
		if(goaltype == GAMETYPE_HELLX) {
			int fadelv = engine.statistics.level;
			if(fadelv < 0) fadelv = 0;
			if(fadelv >= tableHellXFade.length) fadelv = tableHellXFade.length - 1;
			engine.blockHidden = tableHellXFade[fadelv];
		}

		// for test
		/*
		engine.speed.are = 25;
		engine.speed.areLine = 25;
		engine.speed.lineDelay = 10;
		engine.speed.lockDelay = 30;
		engine.speed.das = 12;
		levelTimerMax = levelTimer = 3600 * 3;
		*/
	}

	/**
	 * Set the starting bgmlv
	 * @param engine GameEngine
	 */
	private void setStartBgmlv(GameEngine engine) {
		bgmlv = 0;
		while((bgmlv < tableBGMChange[goaltype].length) && (norm >= tableBGMChange[goaltype][bgmlv])) bgmlv++;
	}

	/**
	 * Calculates average section time
	 */
	private void setAverageSectionTime() {
		if(sectionscomp > 0) {
			int temp = 0;
			for(int i = startlevel; i < sectionscomp; i++) temp += sectiontime[i];
			sectionavgtime = temp / sectionscomp;
		} else {
			sectionavgtime = 0;
		}
	}

	/**
	 * Main routine for game setup screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(engine.getOwner().replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 3);

			if(change != 0) {
				receiver.playSE("change");

				switch(menuCursor) {
				case 0:
					goaltype += change;
					if(goaltype < 0) goaltype = GAMETYPE_MAX - 1;
					if(goaltype > GAMETYPE_MAX - 1) goaltype = 0;
					if(startlevel > tableGoalLevel[goaltype] - 1) startlevel = tableGoalLevel[goaltype] - 1;
					engine.getOwner().backgroundStatus.bg = startlevel;
					break;
				case 1:
					startlevel += change;
					if(startlevel < 0) startlevel = tableGoalLevel[goaltype] - 1;
					if(startlevel > tableGoalLevel[goaltype] - 1) startlevel = 0;
					engine.getOwner().backgroundStatus.bg = startlevel;
					break;
				case 2:
					showsectiontime = !showsectiontime;
					break;
				case 3:
					big = !big;
					break;
				}

				// NET: Signal options change
				if(netIsNetPlay && (netNumSpectators() > 0)) {
					netSendOptions(engine);
				}
			}

			// Check for A button, when pressed this will begin the game
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (menuTime >= 5)) {
				receiver.playSE("decide");
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);

				// NET: Signal start of the game
				if(netIsNetPlay) 
					knetClient().fireTCP(START_1P);

				return false;
			}

			// Check for B button, when pressed this will shutdown the game engine.
			if(engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
				engine.quitflag = true;
			}

			menuTime++;
		} else {
			menuTime++;
			menuCursor = -1;

			if(menuTime >= 60) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Renders game setup screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
			drawMenu(engine, playerID, receiver, 0, EventRenderer.COLOR_BLUE, 0,
					"DIFFICULTY", GAMETYPE_NAME[goaltype],
					"LEVEL", String.valueOf(startlevel + 1),
					"SHOW STIME", GeneralUtil.getONorOFF(showsectiontime),
					"BIG",  GeneralUtil.getONorOFF(big));
	}

	/**
	 * Ready screen
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) {
			engine.statistics.level = startlevel;
			engine.statistics.levelDispAdd = 1;
			engine.big = big;
			norm = startlevel * 10;
			setSpeed(engine);
			setStartBgmlv(engine);
		}

		return false;
	}

	/**
	 * This function will be called before the game actually begins (after Ready&Go screen disappears)
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		if(netIsWatch()) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		} else {
			owner.bgmStatus.bgm = tableBGMNumber[goaltype][bgmlv];
		}
	}

	/**
	 * Renders HUD (leaderboard or game statistics)
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, "TIME ATTACK", EventRenderer.COLOR_PURPLE);
		receiver.drawScoreFont(engine, playerID, 0, 1, "("+GAMETYPE_NAME_LONG[goaltype]+")", EventRenderer.COLOR_PURPLE);

		if( (engine.stat == GameEngine.Status.SETTING) || ((engine.stat == GameEngine.Status.RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (startlevel == 0) && (big == false) && (engine.ai == null) && (!netIsWatch())) {
				receiver.drawScoreFont(engine, playerID, 3, 3, "LINE TIME", EventRenderer.COLOR_BLUE);

				for(int i = 0; i < RANKING_MAX; i++) {
					int gcolor = EventRenderer.COLOR_WHITE;
					if(rankingRollclear[goaltype][i] == 1) gcolor = EventRenderer.COLOR_GREEN;
					if(rankingRollclear[goaltype][i] == 2) gcolor = EventRenderer.COLOR_ORANGE;

					receiver.drawScoreFont(engine, playerID, 0, 4 + i, String.format("%2d", i + 1),
							(i == rankingRank) ? EventRenderer.COLOR_RED : EventRenderer.COLOR_YELLOW);
					receiver.drawScoreFont(engine, playerID, 3, 4 + i, String.valueOf(rankingLines[goaltype][i]), gcolor);
					receiver.drawScoreFont(engine, playerID, 8, 4 + i, GeneralUtil.getTime(rankingTime[goaltype][i]), gcolor);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "LEVEL", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 4, String.valueOf(engine.statistics.level + 1));

			receiver.drawScoreFont(engine, playerID, 0, 6, "TIME LIMIT", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 7, GeneralUtil.getTime(levelTimer),
									((levelTimer > 0) && (levelTimer < 600) && (levelTimer % 4 == 0)));

			receiver.drawScoreFont(engine, playerID, 0, 9, "TOTAL TIME", EventRenderer.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, GeneralUtil.getTime(engine.statistics.time));

			receiver.drawScoreFont(engine, playerID, 0, 12, "NORM", EventRenderer.COLOR_BLUE);
			String strLevel = String.format("%3d", norm);
			receiver.drawScoreFont(engine, playerID, 0, 13, strLevel);

			int speed = engine.speed.gravity / (tableDenominator[goaltype]/2);
			if(engine.speed.gravity < 0) speed = 40;
			receiver.drawSpeedMeter(engine, playerID, 0, 14, speed);

			receiver.drawScoreFont(engine, playerID, 0, 15, String.format("%3d", (engine.statistics.level + 1)*10));

			// Remaining ending time
			if((engine.gameActive) && (engine.ending == 2) && (engine.staffrollEnable)) {
				int time = ROLLTIMELIMIT - rolltime;
				if(time < 0) time = 0;
				receiver.drawScoreFont(engine, playerID, 0, 17, "ROLL TIME", EventRenderer.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
			}

			// Section time
			if((showsectiontime == true) && (sectiontime != null) && (!netIsWatch())) {
				int y = (receiver.getNextDisplayType() == 2) ? 6 : 3;
				int x = (receiver.getNextDisplayType() == 2) ? 22 : 12;
				int x2 = (receiver.getNextDisplayType() == 2) ? 10 : 12;
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;

				receiver.drawScoreFont(engine, playerID, x, y, "SECTION TIME", EventRenderer.COLOR_BLUE, scale);

				for(int i = 0; i < sectiontime.length; i++) {
					if(sectiontime[i] > 0) {
						String strSeparator = " ";
						if((i == engine.statistics.level) && (engine.ending == 0)) strSeparator = "b";

						String strSectionTime;
						strSectionTime = String.format("%2d%s%s", i + 1, strSeparator, GeneralUtil.getTime(sectiontime[i]));

						int pos = i - Math.max(engine.statistics.level-9,0);

						if (pos >= 0) receiver.drawScoreFont(engine, playerID, x+1, y + 1 + pos, strSectionTime, scale);
					}
				}

				if((sectionavgtime > 0) && (!netIsWatch())) {
					receiver.drawScoreFont(engine, playerID, x2, 15, "AVERAGE", EventRenderer.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, x2, 16, GeneralUtil.getTime(sectionavgtime));
				}
			}
		}

		// NET: Number of spectators
		netDrawSpectatorsCount(engine, 0, 20);
		// NET: All number of players
		if(playerID == getPlayers() - 1) {
			netDrawGameRate(engine);
		}
		// NET: Player name (It may also appear in offline replay)
		netDrawPlayerName(engine);
	}

	/**
	 * This function will be called when the piece is active
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// Enable timer again after the levelup
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (engine.ending == 0)) {
			engine.timerActive = true;
		}
		// Ending start
		if((engine.ending == 2) && (engine.staffrollEnable == true) && (rollstarted == false) && (!netIsWatch())) {
			rollstarted = true;
			owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
			owner.bgmStatus.fadesw = false;

			// VOID ending
			if(goaltype == GAMETYPE_VOID) {
				engine.blockHidden = engine.ruleopt.lockflash;
				engine.blockHiddenAnim = false;
				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;
			}
		}

		return super.onMove(engine, playerID);
	}

	/**
	 * This function will be called when the game timer updates
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		// Level timer
		if((engine.timerActive) && (engine.ending == 0)) {
			if(levelTimer > 0) {
				levelTimer--;
				if((levelTimer <= 600) && (levelTimer % 60 == 0)) {
					receiver.playSE("countdown");
				}
			} else if(!netIsWatch()) {
				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.Status.GAMEOVER;
			}
		}

		// Update meter
		if((tableGoalLevel[goaltype] >= 20) && (engine.ending == 0) && (levelTimerMax != 0)) {
			engine.meterValue = (levelTimer * receiver.getMeterMax(engine)) / levelTimerMax;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(levelTimer <= 25*60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(levelTimer <= 15*60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(levelTimer <= 10*60) engine.meterColor = GameEngine.METER_COLOR_RED;
		}

		// Section time
		if((engine.timerActive) && (engine.ending == 0)) {
			if((engine.statistics.level >= 0) && (engine.statistics.level < sectiontime.length)) {
				sectiontime[engine.statistics.level]++;
				setAverageSectionTime();
			}
		}

		// Hebo Hidden for HELL
		if((goaltype == GAMETYPE_HELL) && (engine.timerActive) && (engine.ending == 0)) {
			if((engine.statistics.level >= 5) && (engine.statistics.level <= 6)) {
				engine.heboHiddenEnable = true;
				engine.heboHiddenYLimit = 19;
				engine.heboHiddenTimerMax = engine.heboHiddenYNow * 30 + 45;
			} else if((engine.statistics.level >= 7) && (engine.statistics.level <= 14)) {
				engine.heboHiddenEnable = true;
				engine.heboHiddenYLimit = 19;
				engine.heboHiddenTimerMax = engine.heboHiddenYNow * 10 + 30;
			} else {
				engine.heboHiddenEnable = false;
			}
		}

		// Ending
		if((engine.gameActive) && (engine.ending == 2)) {
			rolltime++;

			// Update meter
			int remainRollTime = ROLLTIMELIMIT - rolltime;
			engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / ROLLTIMELIMIT;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(remainRollTime <= 30*60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(remainRollTime <= 20*60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(remainRollTime <= 10*60) engine.meterColor = GameEngine.METER_COLOR_RED;

			// Completed
			if((rolltime >= ROLLTIMELIMIT) && (!netIsWatch())) {
				engine.statistics.rollclear = 2;
				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.Status.EXCELLENT;
			}
		}
	}

	/**
	 * Calculates line-clear score
	 * (This function will be called even if no lines are cleared)
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Don't do anything during the ending
		if(engine.ending != 0) return;

		// Add lines to norm
		norm += lines;

		// Decrease Hebo Hidden
		if((engine.heboHiddenEnable) && (lines > 0)) {
			engine.heboHiddenTimerNow = 0;
			engine.heboHiddenYNow -= lines;
			if(engine.heboHiddenYNow < 0) engine.heboHiddenYNow = 0;
		}

		// Update meter
		if(tableGoalLevel[goaltype] < 20) {
			engine.meterValue = ((norm % 10) * receiver.getMeterMax(engine)) / 9;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(norm % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(norm % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(norm % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;
		}

		// BGM change
		if((bgmlv < tableBGMChange[goaltype].length) && (norm >= tableBGMChange[goaltype][bgmlv])) {
			bgmlv++;
			owner.bgmStatus.bgm = tableBGMNumber[goaltype][bgmlv];
			owner.bgmStatus.fadesw = false;
		}
		// BGM fadeout
		else if((bgmlv < tableBGMFadeout[goaltype].length) && (norm >= tableBGMFadeout[goaltype][bgmlv])) {
			owner.bgmStatus.fadesw = true;
		}

		// Game completed
		if(norm >= tableGoalLevel[goaltype] * 10) {
			receiver.playSE("levelup");

			// Update section time
			if(engine.timerActive) {
				sectionscomp++;
				setAverageSectionTime();
			}

			norm = tableGoalLevel[goaltype] * 10;
			engine.ending = 1;
			engine.timerActive = false;

			if((goaltype == GAMETYPE_HELLX) || (goaltype == GAMETYPE_VOID)) {
				// HELL-X ending & VOID ending
				engine.staffrollEnable = true;
				engine.statistics.rollclear = 1;
			} else {
				engine.gameEnded();
				engine.statistics.rollclear = 2;
			}
		}
		// Level up
		else if((norm >= (engine.statistics.level + 1) * 10) && (engine.statistics.level < tableGoalLevel[goaltype] - 1)) {
			receiver.playSE("levelup");
			engine.statistics.level++;

			owner.backgroundStatus.fadesw = true;
			owner.backgroundStatus.fadecount = 0;
			owner.backgroundStatus.fadebg = engine.statistics.level;

			sectionscomp++;

			engine.timerActive = false;	// Stop timer until the next piece becomes active

			setSpeed(engine);
		}
	}

	/**
	 * Renders game result screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		if(!netIsWatch()) {
			receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventRenderer.COLOR_RED);
		}

		if(engine.statc[1] == 0) {
			int gcolor = EventRenderer.COLOR_WHITE;
			if(engine.statistics.rollclear == 1) gcolor = EventRenderer.COLOR_GREEN;
			if(engine.statistics.rollclear == 2) gcolor = EventRenderer.COLOR_ORANGE;

			receiver.drawMenuFont(engine, playerID,  0, 2, "NORM", EventRenderer.COLOR_BLUE);
			String strLines = String.format("%10d", norm);
			receiver.drawMenuFont(engine, playerID,  0, 3, strLines, gcolor);

			drawResultStats(engine, playerID, receiver, 4, EventRenderer.COLOR_BLUE,
					Statistic.LEVEL, Statistic.TIME, Statistic.PIECE, Statistic.LPM, Statistic.PPS);
			drawResultRank(engine, playerID, receiver, 14, EventRenderer.COLOR_BLUE, rankingRank);
			drawResultNetRank(engine, playerID, receiver, 16, EventRenderer.COLOR_BLUE, netRankingRank[0]);
			drawResultNetRankDaily(engine, playerID, receiver, 18, EventRenderer.COLOR_BLUE, netRankingRank[1]);
		} else if(engine.statc[1] == 1) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventRenderer.COLOR_BLUE);

			for(int i = 0; i < 10; i++) {
				if(sectiontime[i] > 0) {
					receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectiontime[i]));
				}
			}

			if(sectionavgtime > 0) {
				receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventRenderer.COLOR_BLUE);
				receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
			}
		} else if(engine.statc[1] == 2) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventRenderer.COLOR_BLUE);

			for(int i = 10; i < sectiontime.length; i++) {
				if(sectiontime[i] > 0) {
					receiver.drawMenuFont(engine, playerID, 2, i - 7, GeneralUtil.getTime(sectiontime[i]));
				}
			}

			if(sectionavgtime > 0) {
				receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventRenderer.COLOR_BLUE);
				receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
			}
		}

		if(netIsPB) {
			receiver.drawMenuFont(engine, playerID, 2, 20, "NEW PB", EventRenderer.COLOR_ORANGE);
		}
		if(netIsNetPlay && (netReplaySendStatus == 1)) {
			receiver.drawMenuFont(engine, playerID, 0, 21, "SENDING...", EventRenderer.COLOR_PINK);
		} else if(netIsNetPlay && !netIsWatch() && (netReplaySendStatus == 2)) {
			receiver.drawMenuFont(engine, playerID, 1, 21, "A: RETRY", EventRenderer.COLOR_RED);
		}
	}

	/**
	 * Additional routine for game result screen
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		if(!netIsWatch()) {
			if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
				engine.statc[1]--;
				if(engine.statc[1] < 0) engine.statc[1] = 2;
				receiver.playSE("change");
			}
			if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
				engine.statc[1]++;
				if(engine.statc[1] > 2) engine.statc[1] = 0;
				receiver.playSE("change");
			}
		}

		return super.onResult(engine, playerID);
	}

	/**
	 * This function will be called when the replay data is going to be saved
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(prop);

		// NET: Save name
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
		}

		if((owner.replayMode == false) && (startlevel == 0) && (big == false) && (engine.ai == null)) {
			updateRanking(norm, engine.statistics.time, goaltype, engine.statistics.rollclear);

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Load the settings
	 * @param prop CustomProperties
	 */
	protected void loadSetting(CustomProperties prop) {
		goaltype = prop.getProperty("timeattack.gametype", 0);
		startlevel = prop.getProperty("timeattack.startlevel", 0);
		big = prop.getProperty("timeattack.big", false);
		showsectiontime = prop.getProperty("timeattack.showsectiontime", true);
		version = prop.getProperty("timeattack.version", 0);
	}

	/**
	 * Save the settings
	 * @param prop CustomProperties
	 */
	protected void saveSetting(CustomProperties prop) {
		prop.setProperty("timeattack.gametype", goaltype);
		prop.setProperty("timeattack.startlevel", startlevel);
		prop.setProperty("timeattack.big", big);
		prop.setProperty("timeattack.showsectiontime", showsectiontime);
		prop.setProperty("timeattack.version", version);
	}

	/**
	 * Load the ranking
	 * @param prop CustomProperties
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int type = 0; type < GAMETYPE_MAX; type++) {
				rankingLines[type][i] = prop.getProperty("timeattack.ranking." + ruleName + "." + type + ".lines." + i, 0);
				rankingTime[type][i] = prop.getProperty("timeattack.ranking." + ruleName + "." + type + ".time." + i, 0);
				rankingRollclear[type][i] = prop.getProperty("timeattack.ranking." + ruleName + "." + type + ".rollclear." + i, 0);
			}
		}
	}

	/**
	 * Save the ranking
	 * @param prop CustomProperties
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int type = 0; type < GAMETYPE_MAX; type++) {
				prop.setProperty("timeattack.ranking." + ruleName + "." + type + ".lines." + i, rankingLines[type][i]);
				prop.setProperty("timeattack.ranking." + ruleName + "." + type + ".time." + i, rankingTime[type][i]);
				prop.setProperty("timeattack.ranking." + ruleName + "." + type + ".rollclear." + i, rankingRollclear[type][i]);
			}
		}
	}

	/**
	 * Update the ranking
	 * @param li Lines
	 * @param time Time
	 * @param type Game type
	 * @param clear Game completed flag
	 */
	private void updateRanking(int li, int time, int type, int clear) {
		rankingRank = checkRanking(li, time, type, clear);

		if(rankingRank != -1) {
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingLines[type][i] = rankingLines[type][i - 1];
				rankingTime[type][i] = rankingTime[type][i - 1];
				rankingRollclear[type][i] = rankingRollclear[type][i - 1];
			}

			rankingLines[type][rankingRank] = li;
			rankingTime[type][rankingRank] = time;
			rankingRollclear[type][rankingRank] = clear;
		}
	}

	/**
	 * This function will check the ranking and returns which place you are. (-1: Out of rank)
	 * @param li Lines
	 * @param time Time
	 * @param type Game type
	 * @param clear Game completed flag
	 * @return Place (First place is 0. -1 is Out of Rank)
	 */
	private int checkRanking(int li, int time, int type, int clear) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(clear > rankingRollclear[type][i]) {
				return i;
			} else if((clear == rankingRollclear[type][i]) && (li > rankingLines[type][i])) {
				return i;
			} else if((clear == rankingRollclear[type][i]) && (li == rankingLines[type][i]) && (time < rankingTime[type][i])) {
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
		//FIXME: commented out because I'm lazy
//		int bg = engine.getOwner().backgroundStatus.fadesw ? engine.getOwner().backgroundStatus.fadebg : engine.getOwner().backgroundStatus.bg;
//		String msg = "game\tstats\t";
//		msg += engine.statistics.lines + "\t" + engine.statistics.totalPieceLocked + "\t";
//		msg += engine.statistics.time + "\t" + engine.statistics.lpm + "\t";
//		msg += engine.statistics.pps + "\t" + goaltype + "\t";
//		msg += engine.gameActive + "\t" + engine.timerActive + "\t";
//		msg += engine.statistics.level + "\t" + levelTimer + "\t" + levelTimerMax + "\t";
//		msg += rolltime + "\t" + norm + "\t" + bg + "\t" + engine.meterValue + "\t" + engine.meterColor + "\t";
//		msg += engine.heboHiddenEnable + "\t" + engine.heboHiddenTimerNow + "\t" + engine.heboHiddenTimerMax + "\t";
//		msg += engine.heboHiddenYNow + "\t" + engine.heboHiddenYLimit + "\n";
//		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)
	 */
	@Override
	protected void netRecvStats(GameEngine engine, KNetEvent e) {
		//FIXME: commented out because I'm lazy
//		engine.statistics.lines = Integer.parseInt(message[4]);
//		engine.statistics.totalPieceLocked = Integer.parseInt(message[5]);
//		engine.statistics.time = Integer.parseInt(message[6]);
//		engine.statistics.lpm = Float.parseFloat(message[7]);
//		engine.statistics.pps = Float.parseFloat(message[8]);
//		goaltype = Integer.parseInt(message[9]);
//		engine.gameActive = Boolean.parseBoolean(message[10]);
//		engine.timerActive = Boolean.parseBoolean(message[11]);
//		engine.statistics.level = Integer.parseInt(message[12]);
//		levelTimer = Integer.parseInt(message[13]);
//		levelTimerMax = Integer.parseInt(message[14]);
//		rolltime = Integer.parseInt(message[15]);
//		norm = Integer.parseInt(message[16]);
//		engine.getOwner().backgroundStatus.bg = Integer.parseInt(message[17]);
//		engine.meterValue = Integer.parseInt(message[18]);
//		engine.meterColor = Integer.parseInt(message[19]);
//		engine.heboHiddenEnable = Boolean.parseBoolean(message[20]);
//		engine.heboHiddenTimerNow = Integer.parseInt(message[21]);
//		engine.heboHiddenTimerMax = Integer.parseInt(message[21]);
//		engine.heboHiddenYNow = Integer.parseInt(message[22]);
//		engine.heboHiddenYLimit = Integer.parseInt(message[23]);
	}

	/**
	 * NET: Send end-of-game stats
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		//FIXME: commented out because I'm lazy
//		String subMsg = "";
//		subMsg += "NORM;" + norm + "\t";
//		subMsg += "LEVEL;" + (engine.statistics.level + engine.statistics.levelDispAdd) + "\t";
//		subMsg += "TIME;" + GeneralUtil.getTime(engine.statistics.time) + "\t";
//		subMsg += "PIECE;" + engine.statistics.totalPieceLocked + "\t";
//		subMsg += "LINE/MIN;" + engine.statistics.lpm + "\t";
//		subMsg += "PIECE/SEC;" + engine.statistics.pps + "\t";
//		subMsg += "SECTION AVERAGE;" + GeneralUtil.getTime(sectionavgtime) + "\t";
//		for(int i = 0; i < sectiontime.length; i++) {
//			if(sectiontime[i] > 0) {
//				subMsg += "SECTION " + (i+1) + ";" + GeneralUtil.getTime(sectiontime[i]) + "\t";
//			}
//		}
//
//		String msg = "gstat1p\t" + NetUtil.urlEncode(subMsg) + "\n";
//		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Send game options to all spectators
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendOptions(GameEngine engine) {
		//FIXME: commented out because I'm lazy
//		String msg = "game\toption\t";
//		msg += goaltype + "\t" + startlevel + "\t" + showsectiontime + "\t" + big + "\n";
//		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Receive game options
	 */
	@Override
	protected void netRecvOptions(GameEngine engine, KNetEvent e) {
		//FIXME: commented out because I'm lazy
//		goaltype = Integer.parseInt(message[4]);
//		startlevel = Integer.parseInt(message[5]);
//		showsectiontime = Boolean.parseBoolean(message[6]);
//		big = Boolean.parseBoolean(message[7]);
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
		return (startlevel == 0) && (!big) && (engine.ai == null);
	}
}
