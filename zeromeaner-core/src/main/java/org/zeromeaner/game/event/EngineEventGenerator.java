package org.zeromeaner.game.event;

import org.zeromeaner.game.subsystem.mode.GameMode;

public interface EngineEventGenerator {
	public void addEngineListener(EngineListener l);
	public void removeEngineListener(EngineListener l);
}
