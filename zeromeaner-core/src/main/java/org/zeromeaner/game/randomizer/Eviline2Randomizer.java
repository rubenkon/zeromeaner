package org.zeromeaner.game.randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.eviline.core.Field;
import org.eviline.core.ShapeSource;
import org.eviline.core.ShapeType;
import org.eviline.core.ai.DefaultAIKernel;
import org.eviline.core.ai.DefaultAIKernel.Best;
import org.eviline.core.ai.DefaultAIKernel.BestAdjuster;
import org.eviline.core.ai.DefaultFitness;
import org.eviline.core.ai.NextFitness;
import org.eviline.core.ai.ScoreFitness;
import org.eviline.core.conc.SubtaskExecutor;
import org.eviline.core.ss.Bag7NShapeSource;
import org.eviline.core.ss.EvilBag7NShapeSource;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.eviline.EngineAdapter;
import org.zeromeaner.game.eviline.FieldAdapter;
import org.zeromeaner.game.eviline.XYShapeAdapter;
import org.zeromeaner.game.play.GameEngine;

public class Eviline2Randomizer extends Randomizer {
	public static final int DEFAULT_LOOKAHEAD = 2;
	public static final int DEFAULT_BAG_N = 4;
	
	protected static final ExecutorService EXEC = Executors.newFixedThreadPool(
			Math.max(2, Runtime.getRuntime().availableProcessors()-1),
			new ThreadFactory() {
				private ThreadFactory f = Executors.defaultThreadFactory();
				@Override
				public Thread newThread(Runnable r) {
					Thread t = f.newThread(r);
					t.setName(t.getName() + ": " + Eviline2Randomizer.class.getSimpleName());
					return t;
				}
			});
	protected static final SubtaskExecutor SUBTASKS = new SubtaskExecutor(EXEC, 0);
	
	protected static final BestAdjuster ADJUSTER = new BestAdjuster() {
		@Override
		public Best adjust(Best best) {
			List<Long> counts = new ArrayList<>();
			long min = Long.MAX_VALUE;
			long max = Long.MIN_VALUE;
			for(long c : best.after.getTypeBlitCounts()) {
				counts.add(c);
				if(c < min)
					min = c;
				if(c > max)
					max = c;
			}
			double variance = 0;
			for(long c : counts)
				variance += Math.max(c - min, max - c);
			double score = best.score;
			score += Math.abs(score) * Math.pow(0.9, variance) - Math.abs(score);
			return new Best(best.graph, best.shape, score, best.after, best.type, best.deeper);
		}
	}; 
	
	protected GameEngine engine;
	protected EngineAdapter evilEngine;

	protected DefaultAIKernel ai;
	protected EvilBag7NShapeSource shapes;
	
	protected int count;
	
	public Eviline2Randomizer() {
	}

	public Eviline2Randomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);
	}
	
	@Override
	public void init() {
		evilEngine = new EngineAdapter();
		ai = new DefaultAIKernel(SUBTASKS, new DefaultFitness());
		ai.setAdjuster(ADJUSTER);
		shapes = new EvilBag7NShapeSource(DEFAULT_BAG_N, DEFAULT_LOOKAHEAD);
		shapes.setAi(ai);
		evilEngine.setShapes(shapes);
	}
	
	@Override
	public void setEngine(GameEngine e) {
		engine = e;
		engine.nextPieceArraySize = 1;
		engine.nextPieceArrayID = new int[engine.nextPieceArraySize];
		engine.nextPieceArrayObject = new Piece[engine.nextPieceArraySize];
		count = 0;
	}

	@Override
	public int next() {
		engine.nextPieceArraySize = 1;
		evilEngine.update(engine);
		if(evilEngine.getShape() != -1) {
			DefaultAIKernel.Best best = ai.bestPlacement(evilEngine.getField(), evilEngine.getField(), evilEngine.getShape(), ShapeType.NONE, 1, 1);
			evilEngine.getField().blit(best.shape, 0);
			evilEngine.getField().clearLines();
		}
		ShapeType worst = shapes.next(evilEngine);
		int id = XYShapeAdapter.fromShapeType(worst);
		return id;
	}

}
