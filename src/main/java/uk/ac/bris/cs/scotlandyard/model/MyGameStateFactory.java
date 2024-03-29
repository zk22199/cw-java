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
        private ImmutableSet<Piece> remaining;          // pieces remaining on this turn
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
            for (Player d : detectives) {
                if (d.piece().equals(piece)) {
                    // ticketboard passes each ticket type it wants to know about to this function during its construction
                    return Optional.of(ticket -> d.tickets().get(ticket));
                }
            }
            if (piece.isMrX()) return Optional.of(ticket -> mrX.tickets().get(ticket));

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

            // store the detectives as a set
            ImmutableSet<Piece> winnerDetectives = ImmutableSet.copyOf(detectives.stream().map(Player::piece).collect(Collectors.toSet()));

            // update the moves for this round
            moves = getAvailableMoves();

            // winner is initially empty
            winner = ImmutableSet.of();

            // DETECTIVE MAJOR W:
            // a detective captures MrX:
            for (Player d : detectives) if (d.location() == mrX.location()) {
                    winner = winnerDetectives;
                    break; // save us checking more; the detectives have already won
                }

            // MrX is stuck (nowhere to go):
            if (remaining.contains(mrX.piece()) && moves.isEmpty()) winner =  winnerDetectives;

            // COMMON MRX DUB:
            // all detectives are stuck (no tickets):
            if (detectives.stream().allMatch(d -> d.tickets().values().stream().allMatch(v -> v == 0))) winner = ImmutableSet.of(mrX.piece());

            // MrX evades capture for the whole game:
            if (log.size() >= setup.moves.size() && remaining.contains(mrX.piece())) winner = ImmutableSet.of(mrX.piece());

            // return winner
            return winner;
        }

        @Nonnull
        @Override
        public ImmutableSet<Move> getAvailableMoves() {

            // create new set to store the moves
            Set<Move> availableMoves = new HashSet<>();

            if (winner.isEmpty()){
                // add MrX moves if it is his turn and there are still moves to play
                if (remaining.contains(mrX.piece()) && log.size() < setup.moves.size()) {
                    availableMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
                    availableMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
                }

                // add the detectives moves if it is their turn:
                for (Player d : detectives) if (remaining.contains(d.piece())) {
                    Set<SingleMove> thisDetMove = makeSingleMoves(setup, detectives, d, d.location());
                    if (thisDetMove.isEmpty()) remainingRemove(d);      // if detective cannot move then remove them from remaining
                    else availableMoves.addAll(thisDetMove);
                }
            }

            // return the moves
            return ImmutableSet.copyOf(availableMoves);

        }

        /**
         * @param piece the players piece to be moved
         * @param destination the final node for the player to move to
         * @param ticket the ticket used for the transport
         * @return the player after performing these actions
         */
        private Player movePlayer(Piece piece, int destination, Ticket ticket){

            // get the player associated with the piece
            Player player = getPlayer(piece);

            // use players ticket and move them to their destination
            player = player.use(ticket).at(destination);

            if (player.isMrX()){

                // use MrX's ticket:
                //mrX = mrX.use(ticket).at(destination);

                // switch to the detectives turn by adding all detectives to the 'remaining' list
                remaining = ImmutableSet.<Piece>builder().addAll(detectives.stream().map(Player::piece).collect(Collectors.toSet())).build();

                // Create log entry (either a reveal or hidden entry based on the setup.moves map)
                LogEntry newLogEntry = LogEntry.hidden(ticket);
                if (setup.moves.get(log.size())) newLogEntry = LogEntry.reveal(ticket, destination);

                // add the log entry to the logbook
                log = ImmutableList.<LogEntry>builder().addAll(log).add(newLogEntry).build();
            }

            else {
                // award the used ticket to MrX
                mrX = mrX.give(ticket);

                // remove the detective from the list of players waiting to make their move
                remainingRemove(player);

                // if all detectives have moved it is now the detectives go:
                if (remaining.isEmpty()) remaining = ImmutableSet.<Piece>builder().add(mrX.piece()).build();

            }
            return player;
        }


        /**
         * @param piece the players piece to be moved
         * @param doubleMove the doubleMove to be executed
         * @return the player after performing both these moves
         */
        private Player movePlayerTwice(Piece piece, DoubleMove doubleMove){

            // perform a full single move, inclusive of the updatePlayer function. use a DoubleMove ticket
            Player player = movePlayer(piece, doubleMove.destination1, doubleMove.ticket1);
            player = player.use(Ticket.DOUBLE);
            updatePlayer(player);

            // perform the second single move and return the player as it is
            player = movePlayer(piece, doubleMove.destination2, doubleMove.ticket2);
            return player;
        }

        @Nonnull
        @Override
        public GameState advance(Move move) {

            // check move is legal
            moves = getAvailableMoves();
            if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);

            // use the visitor pattern to return a player that has executed the move(s):
            Player player = move.accept(new Visitor<>(){
                @Override public Player visit(SingleMove singleMove){ return movePlayer(singleMove.commencedBy(), singleMove.destination, singleMove.ticket); }
                @Override public Player visit(DoubleMove doubleMove){ return movePlayerTwice(doubleMove.commencedBy(), doubleMove); }
            });

            // call a subroutine to overwrite the current mrX/detective with the new, moved piece.
            updatePlayer(player);

            // update the gamestate
            return new MyGameState(setup, remaining, log, mrX, detectives);
        }


        // HELPER METHODS:

        /** Removes a player from the list of remaining players */
        private void remainingRemove(Player player){
            HashSet<Piece> playerSet = new HashSet<>(remaining);
            playerSet.remove(player.piece());
            remaining = ImmutableSet.copyOf(playerSet);
        }

        /** Returns a copy of the player based off their piece */
        private Player getPlayer(Piece piece){
            for (Player d : detectives) {
                if (d.piece().equals(piece)) { return d; }
            }
            if (piece.isMrX()) return mrX;
            else throw new IllegalArgumentException("Not a player!");
        }

        /** Replaces the players instance by identifying whether it is a detective or mrX */
        private void updatePlayer(Player player){
            if (player.piece().isDetective()) {
                // replace the detective that has been moved with the new one, keep others the same
                detectives = ImmutableList.copyOf(detectives.stream().map(d -> d.piece().equals(player.piece()) ? player : d).toList());
            }
            else if (player.piece().isMrX()) mrX = player;
            else throw new IllegalArgumentException("Not a player!");
        }
        }



    private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

        // create an empty collection to store all the SingleMove we generate
        HashSet<SingleMove> singleMoves = new HashSet<>();

        for(int destination : setup.graph.adjacentNodes(source)) {
            // make sure that no detectives are present in the destination
            if (detectives.stream().allMatch(d -> d.location() != destination)) {
                for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
                    // if the player has the required tickets add the move
                    if (player.has(t.requiredTicket())) singleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
                    if (player.has(Ticket.SECRET)) singleMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
                }
            }
        }

        // return the collection of moves
        return singleMoves;
    }

    private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

        // create an empty collection to store all the DoubleMove we generate
        HashSet<DoubleMove> doubleMoves = new HashSet<>();

        // check there are any moves left after a single move, and that player has double ticket
        if (!makeSingleMoves(setup, detectives, player, source).isEmpty() && player.has(Ticket.DOUBLE) && setup.moves.size() > 1) {
            for (int d1 : setup.graph.adjacentNodes(source)) if (detectives.stream().allMatch(d -> d.location() != d1)){
                for (Transport t1 : setup.graph.edgeValueOrDefault(source, d1, ImmutableSet.of())) {

                    // create a copy of the player to simulate using tickets
                    Player p1 = new Player(player.piece(), player.tickets(), player.location());
                    Ticket t1used = null;

                    // store the ticket they use for the first move
                    if (p1.has(t1.requiredTicket())) t1used = t1.requiredTicket();

                    // now for the second move; this kind of follows a decision tree:
                    // if the player has at least one secret ticket they can use it for the first or second move
                    // and if they have at least 2 it is possible to use it for both
                    if (t1used != null) {
                        p1 = p1.use(t1used);
                        for (int d2 : setup.graph.adjacentNodes(d1)) if (detectives.stream().allMatch(d -> d.location() != d2)){
                            for (Transport t2 : setup.graph.edgeValueOrDefault(d1, d2, ImmutableSet.of())) {
                                if (p1.has(t2.requiredTicket())) doubleMoves.add(new DoubleMove(player.piece(), source, t1used, d1, t2.requiredTicket(), d2));                  // normal ticket use
                                if (p1.has(Ticket.SECRET)) {
                                    doubleMoves.add(new DoubleMove(player.piece(), source, t1used, d1, Ticket.SECRET, d2));                                                     // second move secret
                                    if (p1.has(t2.requiredTicket())) doubleMoves.add(new DoubleMove(player.piece(), source, Ticket.SECRET, d1, t2.requiredTicket(), d2));       // first move secret
                                }
                                if (p1.hasAtLeast(Ticket.SECRET, 2)) doubleMoves.add(new DoubleMove(player.piece(), source, Ticket.SECRET, d1, Ticket.SECRET, d2));       // two secret moves
                            }
                        }
                    }
                }
            }
        }

        // return collection of double moves
        return doubleMoves;
    }

}