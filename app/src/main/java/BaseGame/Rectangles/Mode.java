package BaseGame.Rectangles;

/**
 * Different drawing modes for a Rectangle
 */

public enum Mode {
    /** Only show one card face down */
    FLIPPED_SINGLE,
    /** Show all cards face down */
    FLIPPED_ALL,
    /** Show top card face up */
    REVEALED_SINGLE,
    /** Show all cards face up */
    REVEALED_ALL,
    /** Show all cards face up, with cards potentially selected */
    REVEALED_SELECT
}