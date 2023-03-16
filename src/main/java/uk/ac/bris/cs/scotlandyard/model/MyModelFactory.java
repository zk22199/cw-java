package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.List;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {

		return new MyModel(setup, mrX, ImmutableList.of());
	}

	private final class MyModel implements Model {

		private List<Observer> observers;
		private Board.GameState game;

		private MyModel(
				final GameSetup setup,
				final Player mrX,
				final ImmutableList<Player> detectives) {

			this.observers = List.of();
			this.game = new MyGameStateFactory().build(setup, mrX, detectives);

		}



		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return game;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if (observers.contains(observer)) throw new IllegalArgumentException("Already added this observer!");
			observers = new ImmutableList.Builder<Observer>().addAll(observers).add(observer).build();
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if (!observers.contains(observer)) throw new IllegalArgumentException("Observer wasn't observing anyway!");
			observers = observers.stream().filter(o -> !o.equals(observer)).collect(ImmutableList.toImmutableList());
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			game.advance(move);
		}
	}

}
