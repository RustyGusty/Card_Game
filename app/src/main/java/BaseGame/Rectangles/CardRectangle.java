package BaseGame.Rectangles;

import java.util.List;

import BaseGame.Mode;
import BaseGame.Cards.Card;

/**
 * For assistance in drawing and handling mouse clicking. CardRectangle is reserved
 * for single-draw draw modes (REVEALED_SINGLE and FLIPPED_SINGLE)
 */
public class CardRectangle extends Rectangle{

    /** Index '0' is treated as the bottom */
    public final List<Card> cards;
    public Mode drawMode;

    public float scale;
    
    /**
     * Rectangle with its center hFactor and vFactor of the way from
     * left to right and top to bottom respectively at the given scale
     * 
     * @param hFactor Factor of distance of center from left to right (0 for center on far-left, 
     * 0.5 for center right at middle, 1 for center on far-right)
     * @param vFactor Factor of distance of center from top to bottom (0 for center on far-top, 
     * 0.5 for center right at middle, 1 for center on far-bottom)
     * @param scale Value to multiply the size of the cards back
     * @param card The card to be drawn
     * @param drawMode See Mode enum
     */
    public CardRectangle(float hFactor, float vFactor, float scale, List<Card> cards, Mode drawMode) {
        this((int) (app.displayWidth * hFactor), (int) (app.displayHeight * vFactor), scale, cards, drawMode);
    }

    /**
     * Rectangle with its center at (xCenter, yCenter)
     * 
     * @param xCenter x-position of the center of the rectangle in pixels
     * @param yCenter y-position of the center of the rectangle in pixels 
     * @param scale Value to multiply the size of the cards back
     * @param card The card to be drawn
     * @param drawMode See Mode enum
     */
    public CardRectangle(int xCenter, int yCenter, float scale, List<Card> cards, Mode drawMode){
        super(xCenter, yCenter);
        this.cards = cards;
        this.drawMode = drawMode;
            
        this.scale = scale;

        this.width = app.defaultWidth * scale;
        this.height = app.defaultHeight * scale;

        super.calculateRectangle();
    }

    public void draw() {
        if(cards.isEmpty())
            return;
        switch(drawMode){
            case FLIPPED_SINGLE:
                drawSingle(app.cardBackIndex);
                return;
            case REVEALED_SINGLE:
                drawSingle(cards.get(cards.size() - 1).hashCode());
                return;
            default:
        }
    }

    private void drawSingle(int index) {
        app.image(app.imageList[index], xLeft, yTop, width, height);
    }

    public void clear() {
        cards.clear();
    }
}