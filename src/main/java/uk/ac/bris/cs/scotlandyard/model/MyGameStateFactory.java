package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Piece.Detective;
import uk.ac.bris.cs.scotlandyard.model.Piece.MrX;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;
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
                        @Override               // ticketboard passes each ticket type it wants to know about to this function during its construction
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


}