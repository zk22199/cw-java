package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.Detective;
import uk.ac.bris.cs.scotlandyard.model.Piece.MrX;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull
	@Override
	public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}

	private final class MyGameState implements GameState {

		// LOCAL VARIABLES:
		private final GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		// CONSTRUCTOR:
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			if (setup != null) this.setup = setup;
			else throw new IllegalArgumentException("Setup is null!");
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty!");

			this.remaining = remaining;
			this.log = log;
			if (mrX != null) this.mrX = mrX;
			else throw new NullPointerException("MrX is null!");
			if (mrX.isDetective()) throw new IllegalArgumentException("MrX cannot be a detective!");
			if (!detectives.contains(null)) this.detectives = detectives;
			else throw new NullPointerException("At least one detective is null!");
			this.winner = ImmutableSet.of();                                    // make winners initially empty;

			for (Player detective : detectives) {                                // check ticket allocation
				if (detective.has(ScotlandYard.Ticket.SECRET) || detective.has(ScotlandYard.Ticket.DOUBLE))
					throw new IllegalArgumentException("Detectives have wrong tickets!");

			}
			for (int i = 0; i < detectives.size(); i++) {                        // iterate over detectives to find duplicates/overlaps
				for (int j = i + 1; j < detectives.size(); j++) {                    // start at j=i+1 to avoid more than one check between each detective
					if (detectives.get(i).equals(detectives.get(j)) || detectives.get(i).location() == detectives.get(j).location())
						throw new IllegalArgumentException("Duplicate/overlapping detectives!");
				}
			}

			moves = ImmutableSet.of();

			Set<SingleMove> singleMoves = new HashSet<>(makeSingleMoves(setup, detectives, mrX, mrX.location()));
			Set<DoubleMove> doubleMoves = new HashSet<>(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
			Set<Move> bothMoves = new HashSet<>();


			bothMoves.addAll(singleMoves);
			bothMoves.addAll(doubleMoves);

			moves = ImmutableSet.copyOf(bothMoves);

		}


		// IMPLEMENTATION METHODS:
		@Nonnull
		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {

			// stream the detectives into a set and add mrX.
			Set<Piece> mySet = this.detectives.stream().map(Player::piece).collect(Collectors.toSet());
			mySet.add(mrX.piece());

			return ImmutableSet.copyOf(mySet);
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			Optional<Integer> location = Optional.empty();

			for (Player d : detectives) {       //check if the player is a detective and return their location
				if (d.piece().equals(detective)) location = Optional.of(d.location());
			}
			return location;
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {

			// if piece is a detective or mrX return their tickets via the getCount function
			//TODO make this a lambda expression

			for (Player d : detectives) {
				if (d.piece().equals(piece)) {
					return Optional.of(new TicketBoard() {
						@Override
						// ticketboard passes each ticket type it wants to know about to this function during its construction
						public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
							return d.tickets().get(ticket);
						}
					});
				}
			}
			if (piece.isMrX()) return Optional.of(new TicketBoard() {
				@Override
				public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
					return mrX.tickets().get(ticket);
				}
			});
			return Optional.empty();            // if not a player, return empty

		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}

		@Override
		public GameState advance(Move move) {
			return null;
		}

	}

	// SINGLE MOVES
	private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

		// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate

		HashSet<SingleMove> singleMoves = new HashSet();

		for (int destination : setup.graph.adjacentNodes(source)) {

			// TODO find out if destination is occupied by a detective
			//  if the location is occupied, don't add to the collection of moves to return

			for (Player a : detectives) {

				if (a.location() == destination) {

				} else {

					for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {

						// TODO find out if the player has the required tickets
						// if it does, construct a SingleMove and add it the collection of moves to return

						if (player.has(t.requiredTicket())) {

							singleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));

						}

						// TODO consider the rules of secret moves here
						//  add moves to the destination via a secret ticket if there are any left with the player

						if (a.has(Ticket.SECRET)) {

							singleMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));

						}

						// TODO return the collection of moves

					}

				}

			}

		}

		return singleMoves;

	}

	// DOUBLE MOVES

	private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

		// TODO create an empty collection of some sort, say, HashSet, to store all the DoubleMove we generate

		HashSet<DoubleMove> doubleMoves = new HashSet();

		int movesLeft = setup.moves.size();

		if (player.has(Ticket.DOUBLE) && (movesLeft > 1)) {

			for (int destination1 : setup.graph.adjacentNodes(source)) {

				for (int destination2 : setup.graph.adjacentNodes(source)) {

					for (Player a : detectives) {

						if ((a.location() == destination1) || (a.location() == destination2)) {

						}

						else {

							for (Transport t1 : setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of())) {

								for (Transport t2 : setup.graph.edgeValueOrDefault(source, destination2, ImmutableSet.of())) {

									if ((player.has(t1.requiredTicket())) && (player.has(t2.requiredTicket()))) {

										if (t1.requiredTicket().equals(t2.requiredTicket())) {

//											if (a.hasAtLeast(t1.requiredTicket()

											doubleMoves.add(new DoubleMove(player.piece(), source, t1.requiredTicket(), destination1, t2.requiredTicket(), destination2));

										}
									}
								}
							}

						}
					}
				}
			}
		}

						// TODO consider the rules of secret moves here
						//  add moves to the destination via a secret ticket if there are any left with the player

//						if (a.has(Ticket.SECRET)) {

//							doubleMoves.add(new DoubleMove(player.piece(), source, Ticket.SECRET, destination1, destination2));

		return doubleMoves;

	}



}