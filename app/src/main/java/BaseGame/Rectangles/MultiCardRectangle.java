package BaseGame.Rectangles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import BaseGame.Cards.Card;

/**
 * For assistance in drawing and handling mouse clicking. MultiCardRectangle is
 * reserved for multi-card draw modes (FLIPPED_ALL, REVEALED_ALL, REVEALED_SELECT)
 */
public class MultiCardRectangle extends CardRectangle{

    public Set<Integer> shownCardsIndices; 

    public float hSpacing;
    public float vSpacing;
    public float defaultWidth;
    public float defaultHeight;

    public int hiddenCardIndex = -1;
    
    /**
     * Rectangle with its center hFactor and vFactor of the way from
     * left to right and top to bottom respectively at the given scale
     * 
     * @param hFactor Factor of distance of center from left to right (0 for center on far-left, 
     * 0.5 for center right at middle, 1 for center on far-right)
     * @param vFactor Factor of distance of center from top to bottom (0 for center on far-top, 
     * 0.5 for center right at middle, 1 for center on far-bottom)
     * @param scale Value to multiply the size of the cards back
     * @param deck The cards to be included in this rectangle
     * @param drawMode See Mode enum
     */
    public MultiCardRectangle(float hFactor, float vFactor, float scale, List<Card> deck, Mode drawMode) {
        this((int) (app.displayWidth * hFactor), (int) (app.displayHeight * vFactor), scale, deck, drawMode);
    }

    /**
     * Rectangle with its center at (xCenter, yCenter)
     * 
     * @param xCenter x-position of the center of the rectangle in pixels
     * @param yCenter y-position of the center of the rectangle in pixels 
     * @param scale Value to multiply the size of the cards back
     * @param deck The cards to be included in this rectangle
     * @param drawMode See Mode enum
     */
    public MultiCardRectangle(int xCenter, int yCenter, float scale, List<Card> deck, Mode drawMode){
        super(xCenter, yCenter, scale, deck, drawMode);
        this.shownCardsIndices = new HashSet<Integer>();
        this.hSpacing = app.defaultHSpacing * scale;
        this.vSpacing = app.defaultVSpacing * scale;

        this.defaultWidth = this.width;
        this.defaultHeight = this.height;
        calculateMultiCard();
    }

    @Override
    public void calculateRectangle() {
        calculateMultiCard();
    }

    private void calculateMultiCard(){
        if(cards.size() == 0)
            width = 0;
        else
            width = defaultWidth + (cards.size() - 1) * hSpacing;
        height = app.defaultHeight * scale;
        super.calculateRectangle();
    }

    public float getMultiCardXPos(int index) {
        return xLeft + (defaultWidth / 2 + index * hSpacing);
    }

    public int getMultiCardIndex(float xCenter) {
        int res = (int) ((xCenter - xLeft - defaultWidth / 2) / hSpacing);
        if(res < 0) res = 0;
        if(res > cards.size() - 1) res = cards.size() - 1;
        return res;
    }

    @Override
    public void draw() {
        if(cards.isEmpty())
            return;
        switch(drawMode){
            case FLIPPED_ALL:
                drawMultiFlipped();
                return;
            case REVEALED_ALL:
                drawMulti();
                return;
            case REVEALED_SELECT:
                drawMultiSelect();
                return;
            default:
                super.draw();
        }
    }

    private void drawMulti() {
        for(int i = 0; i < cards.size(); i++){
            if(i == hiddenCardIndex) return;
            app.image(app.imageList[cards.get(i).hashCode()], xLeft + i * hSpacing, yTop, defaultWidth, defaultHeight);
        }
    }

    public void drawCard(Card card, float xLeft, float yTop) {
        app.image(app.imageList[card.hashCode()], xLeft, yTop, defaultWidth, defaultHeight);
    }

    private void drawMultiSelect() {
        for(int i = 0; i < cards.size(); i++){
            if(i == hiddenCardIndex) return;
            int offset = shownCardsIndices.contains(i) ? (int) vSpacing : 0;
            app.image(app.imageList[cards.get(i).hashCode()], xLeft + i * hSpacing, yTop - offset, defaultWidth, defaultHeight);
        }
    }

    private void drawMultiFlipped(){
        for(int i = 0; i < cards.size(); i++){
            app.image(app.imageList[app.cardBackIndex], xLeft + i * hSpacing, yTop, defaultWidth, defaultHeight);
        }
    }

    /**
     * Given a mouse click, updates the selected cards list appropriately,
     * returning 0 if no card was changed, and (in a 1-based index) a positive
     * or negative index if a card was selected/deselcted respectively
     * @param mouseX X-position of the mouse on click
     * @param mouseY Y-position of the mouse on click
     * @return 0 if no card was changed, a positive index + 1 if a card
     * was selected, and a negative index - 1 if a card was deselected
     */
    public int updateSelect(int mouseX, int mouseY) {
        int res = selectCard(mouseX, mouseY);
        if(res > 0)
            shownCardsIndices.add((Integer) res - 1);
        else if (res < 0)
            shownCardsIndices.remove((Integer) (-res - 1));
        return res;
    }

    /**
     * Given a mouse click, hides the chosen card appropriately,
     * returning 0 if no card was changed, and (in a 1-based index) a positive
     * or negative index if a card was selected/deselcted respectively
     * @param mouseX X-position of the mouse on click
     * @param mouseY Y-position of the mouse on click
     * @return 0 if no card was changed, a positive index + 1 if a card
     * was selected, and a negative index - 1 if a card was deselected
     */
    public int hideCard(int mouseX, int mouseY){
        int res = selectCard(mouseX, mouseY);
        if(res > 0)
            hiddenCardIndex = res - 1;
        else if(res < 0)
            hiddenCardIndex = -res - 1;
        return res;
    }

    /**
     * Given a mouse click, returns the index of the card being chosen,
     * returning 0 if no card was chosen, and (in a 1-based index) a positive
     * or negative index if a card was selected/deselcted respectively
     * @param mouseX X-position of the mouse on click
     * @param mouseY Y-position of the mouse on click
     * @return 0 if no card chosen, a positive index + 1 if a card
     * was selected, and a negative index - 1 if a card was deselected
     */
    public int selectCard(int mouseX, int mouseY){
        int xIndex = (int) ((mouseX - xLeft) / hSpacing);
        boolean topHalfClicked = mouseInRectangle(mouseX, mouseY + (int) (app.defaultHeight * scale / 4));
        if(!mouseInRectangle(mouseX, mouseY)){
            if(!topHalfClicked) return 0;
            int largestSelect = 1;
            for(Integer curIndex : shownCardsIndices) {
                if(Math.abs(xIndex - 2 - curIndex) <= 2)
                    largestSelect = -(curIndex + 1);
            }
            return largestSelect >= 1 ? 0 : largestSelect;
        } else {
            for(int i = cards.size() - 1; i >= 0; i--)
                if(Math.abs(xIndex - 2 - i) <= 2) {
                    if(shownCardsIndices.contains(i)){
                        if(!topHalfClicked) 
                            continue;
                        else {
                            return -(i + 1);
                        }

                    } else {
                        return (i + 1);
                    }
                }
        }
        return 0;
    }

    /**
     * Selects the last card in this hand
     */
    public void selectCard(){
        shownCardsIndices.add(Integer.valueOf(cards.size() - 1));
    }

    public float getVerticalOffset(int index) {
        return shownCardsIndices.contains(index) ? vSpacing : 0;
    }

    @Override
    public void clear() {
        shownCardsIndices.clear();
        cards.clear();
    }
}