package org.zeromeaner.game.evil;

import org.eviline.randomizer.Randomizer;
import org.eviline.randomizer.RandomizerFactory;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;
import org.zeromeaner.game.play.GameEngine;

public class TNNetplayRandomizer extends TNConcurrentRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(2, .01, true, 30);
		mp.put(RandomizerFactory.CONCURRENT, "true");
		mp.put(RandomizerFactory.NEXT, "1");
		Randomizer r = new RandomizerFactory().newRandomizer(mp);
		field.setProvider(r);
	}
	
	@Override
	public String getName() {
		return "NETPLAY RUDE";
	}
}