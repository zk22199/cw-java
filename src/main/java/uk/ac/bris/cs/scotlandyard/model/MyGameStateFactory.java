package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.Move.*;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Piece.Detective;
import uk.ac.bris.cs.scotlandyard.model.Piece.MrX;

import javax.annotation.Nonnull;
import java.util.*;
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
        private ImmutableSet<Piece> remaining;          // pieces remaining on the board
        private ImmutableList<LogEntry> log;
        private Player mrX;
        private List<Player> detectives;
        private ImmutableSet<Move> moves;               // currently possible/ available moves
        private ImmutableSet<Piece> winner;


        // CONSTRUCTOR:
        private MyGameState(
                final GameSetup setup,
                final ImmutableSet<Piece> remaining,
                final ImmutableList<LogEntry> log,
                final Player mrX,
                final List<Player> detectives) {

            // setup checks (check available moves and graph are not empty)
            if (setup != null) this.setup = setup;
            else throw new IllegalArgumentException("Setup is null!");
            if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
            if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty!");

            // initialise log, remaining, and the players by checking they are not null
            this.remaining = remaining;
            this.log = log;
            if (mrX != null) this.mrX = mrX;
            else throw new NullPointerException("MrX is null!");
            if (mrX.isDetective()) throw new IllegalArgumentException("MrX cannot be a detective!");
            if (!detectives.contains(null)) this.detectives = detectives;
            else throw new NullPointerException("At least one detective is null!");

            // make some fields initially empty (MAY NEED TO CHANGE LATER)
            this.winner = ImmutableSet.of();
            this.moves = ImmutableSet.of();

            // check detectives have correct tickets
            for (Player detective : detectives) {
                if (detective.has(ScotlandYard.Ticket.SECRET) || detective.has(ScotlandYard.Ticket.DOUBLE))
                    throw new IllegalArgumentException("Detectives have wrong tickets!");
            }

            // check detectives do not share the same location or colour
            for (int i = 0; i < detectives.size(); i++) {                        // iterate over detectives to find duplicates/overlaps
                for (int j = i + 1; j < detectives.size(); j++) {                    // start at j=i+1 to avoid more than one check between each detective
                    if (detectives.get(i).equals(detectives.get(j)) || detectives.get(i).location() == detectives.get(j).location())  // MAYBE CHANGE THE .equals TO APPLY TO COLOUR ATTRIBUTE
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
            Set<Piece> playerSet = this.detectives.stream().map(Player::piece).collect(Collectors.toSet());
            playerSet.add(mrX.piece());

            // then return an immutable copy of the new set
            return ImmutableSet.copyOf(playerSet);
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

            /*
            TODO: code the winning conditions.
             DETECTIVES win if MrX cannot safely travel to another destination
             or if a detective finishes his move on MrX's station
             --
             MRX wins if the log is full and detectives do not land on him on the last move
             or if detectives can no longer move any of their pieces
            */

            ImmutableSet<Piece> winnerDetectives = ImmutableSet.copyOf(this.detectives.stream().map(Player::piece).collect(Collectors.toSet()));

            // detectives win:
            for (Player d : detectives) if (d.location() == mrX.location()) return winnerDetectives;
            //if (getAvailableMoves().isEmpty()) return winnerDetectives;

            // mrX win:
            else if (log.size() == setup.moves.size()) return ImmutableSet.of(mrX.piece());

            return ImmutableSet.of();
        }

        @Nonnull
        @Override
        public ImmutableSet<Move> getAvailableMoves() {
            return ImmutableSet.of();
        }

        @Nonnull
        @Override
        public GameState advance(Move move) {
            //TODO: uncomment below code when getAvailableMoves() is operational
            //if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
            move.accept(new Visitor<Void>(){  //DOES THIS NEED TO BE ... = move.accept(...) ???
                @Override public Void visit(SingleMove singleMove){

                    // MrX move:
                    if (singleMove.commencedBy().isMrX()){
                        // move mrX and use tickets
                        mrX = mrX.use(singleMove.ticket).at(singleMove.destination);

                        // Create log entry (either a reveal or hidden entry based on the setup.moves map):
                        List<LogEntry> logAsArray = new ArrayList<>(log);
                        if (setup.moves.get(log.size())) logAsArray.add(LogEntry.reveal(singleMove.ticket, singleMove.destination));
                        else logAsArray.add(LogEntry.hidden(singleMove.ticket));
                        log = ImmutableList.copyOf(logAsArray);

                    }

                    // Each detective move:
                    else for (int i = 0, detectivesSize = detectives.size(); i < detectivesSize; i++) {
                            Player d = detectives.get(i);
                            if (d.piece().equals(singleMove.commencedBy())) {
                                //move the detective and give the used ticket to mrX
                                mrX = mrX.give(singleMove.ticket);
                                List<Player> detsArrayList = new ArrayList<>(detectives);
                                detsArrayList.set(i, d.use(singleMove.ticket).at(singleMove.destination));
                                detectives = detsArrayList;

                                //TODO: remove available moves from this detective,
                                // i.e. when getAvailableMoves() is called there are no moves for this Player

                                /*
                                The reason we have to use an iterative loop instead of a foreach loop such as:
                                for ( Player d : detectives ) { ... }
                                is because java will create a new variable d each time that does not actually
                                reflect the live value of that detective, so any changes we make to it e.g. updating
                                location will not be passed to the real detective.

                                Then, with our iterative loop we have to create a new ArrayList to edit the original,
                                as even though it was not explicitly declared as an ImmutableList the .copyOf function
                                would have returned one, which means we wouldn't be able to perform .set() on its elements.
                                An ArrayList does not have this problem.
                                 */

                            }
                        }

                    return null;
                }
                @Override public Void visit(DoubleMove doubleMove){

                    // perform two single moves derived from the double move
                    mrX = mrX.use(Ticket.DOUBLE);
                    visit(new SingleMove(doubleMove.commencedBy(), doubleMove.source(), doubleMove.ticket1, doubleMove.destination1));
                    visit(new SingleMove(doubleMove.commencedBy(), doubleMove.destination1, doubleMove.ticket2, doubleMove.destination2));

                    return null;
                }



            });

            return new MyGameState(setup, remaining, log, mrX, detectives);
        }



    }



}