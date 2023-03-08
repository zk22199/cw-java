package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {


		// GAME STATE CREATION TESTS:
		if (mrX == null) throw new NullPointerException();
		if (detectives.contains(null)) throw new NullPointerException();
		if (mrX.isDetective()) throw new IllegalArgumentException();


		throw new RuntimeException("Implement me!");

	}

}
