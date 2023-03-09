package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.Optional;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {


		// PRE GAME STATE CREATION TESTS:
		if (mrX == null) throw new NullPointerException();    				// check mrX not null
		if (detectives.contains(null)) throw new NullPointerException();  	// check no detective is null
		if (mrX.isDetective()) throw new IllegalArgumentException();   		// check mrX is not a detective
		for (Player detective : detectives) { 								// check ticket allocation
			if (detective.has(ScotlandYard.Ticket.SECRET) || detective.has(ScotlandYard.Ticket.DOUBLE)) throw new IllegalArgumentException();    }
		if (mrX.has(ScotlandYard.Ticket.TAXI) || mrX.has(ScotlandYard.Ticket.BUS) || mrX.has(ScotlandYard.Ticket.UNDERGROUND)) throw new IllegalArgumentException();


		// GAME STATE CREATION:
		//GameState nextGameState = new GameState(); 						// contains 8 methods to implement
		//return nextGameState;

		throw new RuntimeException("Implement me!");

	}

}
