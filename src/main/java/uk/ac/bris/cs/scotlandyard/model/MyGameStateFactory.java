package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyGameState implements GameState {

		// LOCAL VARIABLES:
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		// CONSTRUCTOR:
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives)
		{
			if (setup != null) this.setup = setup; else throw new IllegalArgumentException();
			this.remaining = remaining;
			this.log = log;
			if (mrX != null) this.mrX = mrX; else throw new NullPointerException();
			if (mrX.isDetective()) throw new IllegalArgumentException();
			if (!detectives.contains(null)) this.detectives = detectives; else throw new NullPointerException();
			this.winner = ImmutableSet.of(); 				// make winners initially empty;
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			for (Player detective : detectives) { 								// check ticket allocation
				if (detective.has(ScotlandYard.Ticket.SECRET) || detective.has(ScotlandYard.Ticket.DOUBLE)) throw new IllegalArgumentException();
			}
			for (int i = 0; i < detectives.size(); i++){						// iterate over detectives to find duplicates/overlaps
				for (int j = i+1; j < detectives.size(); j++){					// start at j=i+1 to avoid more than one check between each detective
					if (detectives.get(i).equals(detectives.get(j)) || detectives.get(i).location() == detectives.get(j).location()) throw new IllegalArgumentException();
				}
			}

		}


		// IMPLEMENTATION METHODS:
		@Override public GameSetup getSetup() {  return setup; }
		@Override  public ImmutableSet<Piece> getPlayers() { return null; }

		@Nonnull @Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			return Optional.empty();
		}

		@Nonnull @Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Nonnull @Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Nonnull @Override
		public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}

		@Override public GameState advance(Move move) {  return null;  }

	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}



}