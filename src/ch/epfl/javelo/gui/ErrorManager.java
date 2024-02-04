package ch.epfl.javelo.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * This class manages the display of error messages.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class ErrorManager {
	private final BorderPane pane;
	private final Text errorTextContainer;
	private final SequentialTransition sequentialTransition;
	private static final double TRANSPARENT = 0.0;
	private static final double OPAQUE = 0.8;
	private static final double TRANS1_DURATION = 0.2;
	private static final double TRANS2_DURATION = 0.5;
	private static final double PAUSE_DURATION = 2.0;

	/**
	 * Creates an ErrorManager.
	 * An error message is composed of a borderPane, a vBox that belongs to the pane
	 * and animations that determine the display of the error message.
	 */
	public ErrorManager() {
		this.pane = new BorderPane();
		VBox vBox = new VBox();
		this.errorTextContainer = new Text();

		vBox.getStylesheets().add("error.css");
		vBox.getChildren().add(errorTextContainer);
		this.pane.setCenter(vBox);

		// The pane displaying the error message should be transparent to the mouse
		this.pane.setMouseTransparent(true);

		FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(TRANS1_DURATION));
		FadeTransition fadeTransition2 = new FadeTransition(Duration.seconds(TRANS2_DURATION));
		PauseTransition pauseTransition = new PauseTransition(Duration.seconds(PAUSE_DURATION));
		this.sequentialTransition = new SequentialTransition(vBox);

		// Describing transition 1
		fadeTransition1.setFromValue(TRANSPARENT);
		fadeTransition1.setToValue(OPAQUE);

		// Describing transition 2
		fadeTransition2.setFromValue(OPAQUE);
		fadeTransition2.setToValue(TRANSPARENT);

		// Putting all the transitions in one => describing the entire display of the error
		this.sequentialTransition.getChildren().addAll(fadeTransition1, pauseTransition, fadeTransition2);
	}

	/**
	 * Gives the JavaFX pane displaying the error message.
	 *
	 * @return the pane.
	 */
	public Pane pane() {
		return this.pane;
	}

	/**
	 * Displays temporarily the error message.
	 * A sound is accompanied indicating the error.
	 *
	 * @param errorMessage the String representing the error message.
	 */
	public void displayError(String errorMessage) {
		// When there is an error message there should be a sound
		java.awt.Toolkit.getDefaultToolkit().beep();

		// Putting the error message in the vBox
		this.errorTextContainer.setText(errorMessage);

		// we stop before playing so there is no conflict when you try to play an animation but another is already activated
		this.sequentialTransition.stop();
		this.sequentialTransition.play();
	}
}
