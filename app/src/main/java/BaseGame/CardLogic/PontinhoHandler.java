// TODO fix encoding: Add length parameter and only show most-recently discarded card instead of all
package BaseGame.CardLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import BaseGame.App;
import BaseGame.Mode;
import BaseGame.Player;
import BaseGame.Cards.*;
import BaseGame.Rectangles.*;
import processing.core.PConstants;
import processing.core.PImage;

public class PontinhoHandler extends DeckHandler {

    /** Rectangle containing the draw deck */
    private CardRectangle drawRect;
    /** Rectangle showing the wildcard */
    private CardRectangle wildcardRect;
    /** Rectangle in the bottom-right for the current player's hand */
    private MultiOutlineRectangle playerRect;
    /** List of the other players' rectangles to display in the otherPlayerDisplay
     */
    private MultiCardRectangle otherPlayerRect[];
    /** Rectangle in the newSetDisplay to show the potential new or edited set */
    private MultiOutlineRectangle newSetRect;
    /** Rectangle containing the discard pile */
    private CardRectangle discardRect;
    /** Redundant variable for whether the currently-displayed new set is playable */
    private boolean newSetValid;

    /** Bottom-left corner, box to show other players' hands */
    private Rectangle otherPlayerDisplay;
    /** Bottom-left corner, box to show new set and confirm/cancel buttons  */
    private Rectangle newSetDisplay;
    /** Text background in top of the play to show next move */
    private Rectangle textHeader;
    /** Bottom-right corner, box for the revert button */
    private Rectangle revertRect;

    /** PNG containing the confirm button */
    private PictureRectangle confirmButtonRect;
    /** PNG contianing the cancel button */
    private PictureRectangle cancelButtonRect;
    /** PNG containing the revert to last valid state button */
    private PictureRectangle revertButtonRect;
    /** PNG containing the discard marker */
    private PictureRectangle discardMarkerRect;
    
    /** True if it's the first move and must draw from the deck or discard pile */
    private boolean firstMove;
    /** Used for if wildcards are played in ways which are only allowed as a final move */
    private boolean mustEnd;
    /** Stores whether the current set will force the game to end if played */
    private boolean tempGameEnd;

    /** Used to allow undo-ing to a valid state for various rules */
    private String lastValidState;

    /** List of all played sets that can be moved around. Sets are never removed, only
     * added, so the order will be preserved
     */
    private List<MultiCardRectangle> moveableRectangleList;
    /** Indices into moveableRectangleList, to be used for priority in drawing
     * (Earlier elements are drawn before later elements)
     */
    private List<Integer> moveableRectanglePriorityList;
    /** When editing a moveableRect, this is the index to that rectangle.
     * If nothing is being edited, this value defaults to -1
     */
    private int selectedRectIndex = -1;

    /** Reference for which card is the wildcard */
    private Card wildcard;
    /** Reference to which of the moveable rectangles has been picked up */
    private MultiCardRectangle pickedUpRect;
    /** Reference to which CardRectangle has a card taken from it */
    private MultiOutlineRectangle cardOriginRect = null;

    /** Used when dragging cards to not snap to the center always */
    private float xOffset;
    /** Used when dragging cards to not snap to the center always */
    private float yOffset;

    /** Delineates the playArea in the top half of the board */
    private Rectangle playArea;
    /** Delineates the area outside the playArea reserved for moving cards around
     * in the bottom half of the screen */
    private Rectangle playerArea;
    /** Gets the current index in the hand of the dragging card */
    private int pickedUpCardIndex = -1;

    /** Whether or not the discarded card can be drawn */
    private boolean canDrawDiscard;
    /** Used to display to the user if they need to play cards from hand to discard */
    private boolean illegalDiscard;
    /** Indicates whether or not the player is done making their move */
    private boolean moveMade;

    /** Gives the player's score for this round */
    private int roundScore;

    public PontinhoHandler(App app){
        super(app, 2);
        MultiOutlineRectangle.setup();
    }

    @Override
    public String initializeDeck() {
        drawRect = new CardRectangle(0.5f, 0.2f, 1, DeckHelper.createDeck(2, 0), Mode.FLIPPED_SINGLE);
        Collections.shuffle(drawRect.cards);
        startingDeck = encodeStartingDeck(drawRect.cards);
        return startingDeck;
    }

    @Override
    public void initializeDeck(String startingDeck) {
        if(this.startingDeck == null) {
            this.startingDeck = startingDeck;
            drawRect = new CardRectangle(0.5f, 0.2f, 1, decodeStartingDeck(startingDeck), Mode.FLIPPED_SINGLE);
        }
    }

    /**
     * Sets up the game with players, hands, boards, etc.
     * Called after the starting deck is initialized
     */
    public void setup() {
        int iterations = 0;
        int ind = app.curPlayerNumber;
        while(iterations < 3 && ind >= app.curPlayerNumber) {
            app.playerList.get(ind).addFromDeck(drawRect.cards, 3);
            ind++;
            if(ind >= app.numPlayers){
                iterations++;
                ind = 0;
            }
        }
        
        for(Player p : app.playerList)
            Collections.sort(p.hand);
            
        List<Card> wildcardSet = new ArrayList<Card>();
        wildcardSet.add(DeckHelper.draw(drawRect.cards));
        wildcardRect = new CardRectangle(0.5f, 0.15f, 1, wildcardSet, Mode.REVEALED_SINGLE);
        playerRect = new MultiOutlineRectangle((int) (0.65 * app.displayWidth), app.displayHeight - (int) (1.1 * app.defaultHeight), 1.5f, app.thisPlayer.hand, Mode.REVEALED_SELECT);
        otherPlayerRect = new MultiCardRectangle[app.numPlayers - 1];
        int index = 0;
        for(int i = 0; i < app.numPlayers; i++) {
            if(i == app.thisPlayerNumber) continue;
            otherPlayerRect[index] = new MultiCardRectangle((int) (0.1 * app.displayWidth),
            (int) (app.displayHeight - app.defaultHeight * (0.6*index + 0.45)), 0.5f, app.playerList.get(i).hand, Mode.FLIPPED_ALL);
            index++;
        }

        newSetValid = false;
        firstMove = true;
        mustEnd = false;
        moveableRectangleList = new ArrayList<MultiCardRectangle>();
        moveableRectanglePriorityList = new ArrayList<Integer>();
        cardOriginRect = null;
        canDrawDiscard = false;
        illegalDiscard = false;
        moveMade = false;
        roundScore = 0;
        winningPlayerNumber = -1;
        lastValidState = null;

        float height = app.defaultHeight * 0.6f * ((app.numPlayers - 1) + 0.4f);
        otherPlayerDisplay = new Rectangle((int) (app.displayWidth * 0.15f), (int) (app.displayHeight - height / 2), (int) (app.displayWidth * 0.3f), (int) height);
        int playHeight = (int) Math.min(app.displayHeight - height, app.displayHeight - playerRect.height * 1.5);
        playArea = new Rectangle(app.displayWidth / 2, playHeight / 2, app.displayWidth, playHeight);
        int playerAreaHeight = app.displayHeight - playHeight;
        playerArea = new Rectangle((int) (app.displayWidth / 2), (int) (app.displayHeight - playerAreaHeight / 2), app.displayWidth, playerAreaHeight);
        
        newSetRect = new MultiOutlineRectangle((int) otherPlayerDisplay.xCenter, (int) playerRect.yCenter, 1.5f, new ArrayList<Card>(), Mode.REVEALED_ALL);
        newSetDisplay = new Rectangle((int) otherPlayerDisplay.xCenter, (int) playerRect.yCenter, otherPlayerDisplay.width, (app.displayHeight - playerRect.yCenter) * 2);
        confirmButtonRect = new PictureRectangle((int) (otherPlayerDisplay.xRight - 70 * app.scaleFactor), (int) (playerRect.yCenter + 30 * app.scaleFactor), 120 * app.scaleFactor, 44 * app.scaleFactor, "app/src/main/resources/Button/unpressed_confirm.png");
        cancelButtonRect = new PictureRectangle((int) confirmButtonRect.xCenter, (int) (confirmButtonRect.yCenter - 60 * app.scaleFactor), 120 * app.scaleFactor, 44 * app.scaleFactor, "app/src/main/resources/Button/unpressed_cancel.png");
        revertRect = new Rectangle((int) (app.displayWidth - 80 * app.scaleFactor), (int) (app.displayHeight - 44 * app.scaleFactor), 160 * app.scaleFactor, 84 * app.scaleFactor);
        revertButtonRect = new PictureRectangle((int) (app.displayWidth - 80 * app.scaleFactor), (int) (app.displayHeight - 44 * app.scaleFactor), 120 * app.scaleFactor, 44 * app.scaleFactor, "app/src/main/resources/Button/unpressed_revert.png");
        
        discardRect = new CardRectangle(0.65f, 0.19f, 1, new ArrayList<Card>(), Mode.REVEALED_SINGLE);
        discardMarkerRect = new PictureRectangle(0.65f, 0.2f, 126 * app.scaleFactor, 202 * app.scaleFactor, "app/src/main/resources/discard_pile.png");

        textHeader = new Rectangle(0.5f, 0.03f, 0.5f * app.displayWidth, 0.06f * app.displayHeight);
        int wildcardValue = wildcardSet.get(0).value + 1;
        if(wildcardValue > 12) 
            wildcardValue = 0;
        wildcard = new Card(wildcardValue, wildcardSet.get(0).suit);
    }
      
    public boolean setIsValid(List<Card> set) {
        tempGameEnd = false;
        if(set.size() <= 2) return false; // Sets must be 3 or greater
        
        // Ensure that this set is in non-decreasing order for runCheck
        List<Card> properSet = new ArrayList<Card>(set);
        int firstNonWild = -1;
        while(set.get(++firstNonWild).equals(wildcard));
        int lastNonWild = set.size();
        while(set.get(--lastNonWild).equals(wildcard));
        if(set.get(firstNonWild).compareTo(set.get(lastNonWild)) > 0)
            Collections.reverse(properSet);
        if(setCheck(properSet) || runCheck(properSet))
            return true;
        return false;
    }

    private boolean setCheck(List<Card> set) {
        // Will check how many suits there are if no wildcards existed
        Set<Suit> wildcardFreeSuitSet = new HashSet<Suit>();
        Set<Suit> suitSet = new HashSet<Suit>();
        // Get the first non-wild card as the chosen value here
        int i;
        for(i = 0; set.get(i).equals(wildcard); i++);
        int chosenValue = set.get(i).value;
        int wildcardsUsed = 0;

        // Rule: All same number, must be exactly 3 different suits.
        // If using wildcard, must win round (unless used as its face value)
        for(Card c : set) {
            // If it's a wildcard, and is not being played as its face value,
            // check if the rest of the played hand is good, but the wildcard
            // must have been used as a wildcard
            if(c.equals(wildcard)){
                wildcardsUsed++;
                if(c.value != chosenValue) {
                    tempGameEnd = true;
                    continue;
                }
            } else
                wildcardFreeSuitSet.add(c.suit);

            // If a wildcard could be played for its face value,
            // then add its suit anyways to see if it can count as a non-wildcard play
            // (while not updating wildcardFreeSuitSet)
            suitSet.add(c.suit);
            if(suitSet.size() > 3 || c.value != chosenValue)
                return false;
        }

        // If exactly 3 suits are in play, then return true always (not updating
        // wildcardUsed in case a non-set wildcard was skipped)
        if (suitSet.size() == 3)
            return true;
        
        // If the wildcardFreeSuitSet has more than 3 suits, it is automatically
        // disqualified
        if(wildcardFreeSuitSet.size() > 3)
            return false;
        
        // If suits can be added to wildcardFreeSuitSet to get it to 3,
        // then it's good but uses a wildcard
        if(3 - wildcardFreeSuitSet.size() <= wildcardsUsed) {
            tempGameEnd = true;
            return true;
        }

        // Otherwise, return false
        return false;
    }

    private boolean runCheck(List<Card> set) {
        tempGameEnd = false;
        // Keep picking cards until first non-wildcard is found
        // (If wildcard is played at an end, then the game must end)
        int nonWildIndex = 0;
        while(set.get(nonWildIndex).equals(wildcard)){
            nonWildIndex++;
            tempGameEnd = true;
        }
        Suit chosenSuit = set.get(nonWildIndex).suit;
        int curNum = set.get(nonWildIndex).value;

        // If there's an Ace first and numbers are skipped,
        // ignore the first non-wildcard but make sure it ends properly
        boolean aceCheck = (set.get(0).value == 0);
        int requiredEnd = 12; // For K at the end, lower if more wildcard are involved

        // Rule: All same suit, ascending order (A can be QKA or A23 but not KA2)
        for(int i = nonWildIndex + 1; i < set.size(); i++) {
            if(set.get(i).equals(wildcard)){
                if(aceCheck){
                    requiredEnd--;
                }
                curNum++;
                continue;
            }
            if(set.get(i).suit != chosenSuit) return false;
            int newNum = set.get(i).value;
            if(newNum != curNum + 1) {
                if(aceCheck) {
                    if(set.get(set.size() - 1).value != requiredEnd)
                        return false;
                } else 
                    return false;
            }
            aceCheck = false;
            curNum = newNum;
        }

        if(set.get(set.size() - 1).equals(wildcard)) 
            tempGameEnd = true;

        return true;
    }

    private int getMoveableRectangleIndex(int mouseX, int mouseY) {
        for(int i = 0; i < moveableRectangleList.size(); i++){
            MultiCardRectangle cr = moveableRectangleList.get(i);
            if(cr.mouseInRectangle(mouseX, mouseY))
                return i;
        }
        return -1;
    }

    private boolean selectedCardPicked = false;
    private boolean outlinedCardPicked = false;
    private boolean returnedCardPicked = false;
    private Set<Integer> returnedCardsIndices = new HashSet<Integer>();

    /**
     * Picks up a rectangle to be moved if necessary
     */
    @Override 
    public void handleMousePress(int mouseX, int mouseY) {
        if(playerArea.mouseInRectangle(mouseX, mouseY)){
            if((pickedUpCardIndex = playerRect.hideCard(mouseX, mouseY)) != 0) {
                cardOriginRect = playerRect;
            } else if((pickedUpCardIndex = newSetRect.hideCard(mouseX, mouseY)) != 0) {
                cardOriginRect = newSetRect;
            } else 
                return;
            int yShift = pickedUpCardIndex > 0 ? 0 : (int) (-app.defaultVSpacing * 1.5);
            pickedUpCardIndex = pickedUpCardIndex > 0 ? pickedUpCardIndex - 1 : - pickedUpCardIndex - 1;

            pickedUpRect = new MultiOutlineRectangle((int) cardOriginRect.getMultiCardXPos(pickedUpCardIndex) + 1, (int) cardOriginRect.yCenter + yShift, 1.5f, new ArrayList<Card>(), Mode.REVEALED_SINGLE);
            pickedUpRect.cards.add(cardOriginRect.cards.get(pickedUpCardIndex));
            pickedUpRect.calculateRectangle();
            if(selectedCardPicked = (cardOriginRect.shownCardsIndices.contains(pickedUpCardIndex)))
                cardOriginRect.shownCardsIndices.remove(pickedUpCardIndex);
            if(outlinedCardPicked = (cardOriginRect.outlinedCardsSet.contains(pickedUpCardIndex)))
                cardOriginRect.outlinedCardsSet.remove(pickedUpCardIndex);
            if(cardOriginRect == playerRect && (returnedCardPicked = (returnedCardsIndices.contains(pickedUpCardIndex))))
                returnedCardsIndices.remove(pickedUpCardIndex);
            xOffset = pickedUpRect.xCenter - mouseX;
            yOffset = pickedUpRect.yCenter - mouseY;
            return;
        }
        int moveableRectIndex = getMoveableRectangleIndex(mouseX, mouseY);
        if(moveableRectIndex != -1) {
            MultiCardRectangle cr = moveableRectangleList.get(moveableRectIndex);
            pickedUpRect = cr;
            // Make the newly-clicked rectangle appear on top
            moveableRectanglePriorityList.remove((Integer) moveableRectIndex);
            moveableRectanglePriorityList.add((Integer) moveableRectIndex);
            xOffset = cr.xCenter - mouseX;
            yOffset = cr.yCenter - mouseY;
            return;
        }
    }

    @Override
    public void handleMouseDrag(int mouseX, int mouseY) {
        if(pickedUpRect != null) {
            if(cardOriginRect == null)
                pickedUpRect.updatePosition(mouseX + xOffset, mouseY+yOffset, playArea);
            else {
                pickedUpRect.updatePosition(mouseX + xOffset, mouseY+yOffset, playerArea);
                int newIndex = cardOriginRect.getMultiCardIndex(pickedUpRect.xCenter);
                // Cards shifted right
                if(newIndex < cardOriginRect.hiddenCardIndex)
                    for(int i = cardOriginRect.hiddenCardIndex - 1; i >= newIndex; i--){
                        cardOriginRect.cards.set(i + 1, cardOriginRect.cards.get(i));
                        shift(cardOriginRect.shownCardsIndices, i, 1);
                        shift(cardOriginRect.outlinedCardsSet, i, 1);
                        if(cardOriginRect == playerRect)
                            shift(returnedCardsIndices, i, 1);
                    } 
                else
                    for(int i = cardOriginRect.hiddenCardIndex + 1; i <= newIndex; i++) {
                        cardOriginRect.cards.set(i - 1, cardOriginRect.cards.get(i));
                        shift(cardOriginRect.shownCardsIndices, i, -1);
                        shift(cardOriginRect.outlinedCardsSet, i, -1);
                        if(cardOriginRect == playerRect)
                            shift(returnedCardsIndices, i, -1);
                    }
                cardOriginRect.hiddenCardIndex = newIndex;
                cardOriginRect.cards.set(newIndex, pickedUpRect.cards.get(0));
                updateNewSet();
            }
        }            
    }

    /**
     * If the element index is found in indexSet, shifts it left if dir == -1,
     * right if dir == 1 (other dir are undefined behavior)
     * @param indexSet
     * @param index
     * @param dir 1 if shifting right, -1 if shifting left
     */
    private void shift(Set<Integer> indexSet, int index, int dir) {
        if(indexSet.remove(index))
            indexSet.add(index + dir);
    }

    @Override
    public boolean handleMouseClick(int mouseX, int mouseY){
        if(moveMade || winningPlayerNumber != -1) return false;
        illegalDiscard = false;
        if((app.thisPlayerNumber + 1) % app.numPlayers == app.curPlayerNumber) 
            return false;
        boolean notMyTurn = app.thisPlayerNumber != app.curPlayerNumber;
        if(notMyTurn && !canDrawDiscard)
            return false;
        int selectedCardIndex;
        // Select card from playerRect branch
        if((selectedCardIndex = playerRect.updateSelect(mouseX, mouseY)) != 0) {
            // Unselect card
            if(selectedCardIndex < 0){
                if(firstMove && newSetRect.cards.size() <= 2)
                    newSetRect.clear();
                else {
                    selectedCardIndex = -(selectedCardIndex + 1);
                    Card removedCard = playerRect.cards.get(selectedCardIndex);
                    newSetRect.cards.remove(removedCard);
                }
                updateNewSet();
            // Select card
            } else {
                if(firstMove && newSetRect.cards.size() <= 0)
                    if(discardRect.cards.isEmpty())
                        newSetRect.addOutlinedCard(wildcardRect.cards.get(0));
                    else
                        newSetRect.addOutlinedCard(discardRect.cards.get(discardRect.cards.size() - 1));
                selectedCardIndex -= 1;
                Card addedCard = playerRect.cards.get(selectedCardIndex);
                newSetRect.cards.add(addedCard);
                updateNewSet();
            }
            return false;
        }
        // Select card from newSetRect branch
        if((selectedCardIndex = newSetRect.updateSelect(mouseX, mouseY)) != 0) {
            if(newSetRect.outlinedCardsSet.contains(--selectedCardIndex) || outlinedCardPicked) {
                if(newSetRect.cards.get(selectedCardIndex).equals(wildcard)) {
                    returnedCardsIndices.add(playerRect.cards.size());
                    playerRect.addOutlinedCard(newSetRect.removeCard(selectedCardIndex));
                    for(int i = newSetRect.cards.size(); i > selectedCardIndex; i--)
                        shift(newSetRect.outlinedCardsSet, i, -1);
                }
            }
            else {
                if(firstMove && newSetRect.cards.size() <= 2) {
                    newSetRect.clear();
                    playerRect.shownCardsIndices.clear();
                }
                else {
                    Card removedCard = newSetRect.removeCard(selectedCardIndex);
                    playerRect.shownCardsIndices.remove(playerRect.cards.indexOf(removedCard));
                }
            }
            updateNewSet();
            return false;
        }
        // Cancel check
        if (cancelButtonRect.mouseInRectangle(mouseX, mouseY)){
            newSetRect.clear();
            updateNewSet();
            playerRect.shownCardsIndices.clear();
            List<Integer> sortedList = new ArrayList<Integer>(returnedCardsIndices);
            sortedList.sort(null);
            for(int i = sortedList.size() - 1; i >= 0; i--)
                playerRect.removeCard(sortedList.get(i));
            returnedCardsIndices.clear();
            selectedRectIndex = -1;
            return false;
        }
        // New set check
        if(newSetValid && confirmButtonRect.mouseInRectangle(mouseX, mouseY)) {
            if(!mustEnd && playerRect.outlinedCardsSet.isEmpty())
                lastValidState = encodeGameState();
            if(selectedRectIndex == -1){
                MultiCardRectangle newRect = new MultiCardRectangle((int) newSetRect.xCenter, (int) newSetRect.yCenter, 1, new ArrayList<Card>(newSetRect.cards), Mode.REVEALED_ALL);
                newRect.updatePosition(playArea);
                moveableRectanglePriorityList.add((Integer) moveableRectangleList.size());
                moveableRectangleList.add(newRect);
                if(firstMove && !discardRect.cards.isEmpty())
                    discardRect.cards.remove(discardRect.cards.size() - 1);
            } else {
                setRectList(moveableRectangleList.get(selectedRectIndex), newSetRect.cards);
                selectedRectIndex = -1;
            }

            returnedCardsIndices.clear();
            removeSelectedCards();
            newSetRect.clear();
            
            if(tempGameEnd)
                mustEnd = true;
            tempGameEnd = false;
            firstMove = false;
            updateNewSet();
            boolean res;
            if(res = playerRect.cards.isEmpty())
                resetRoundVariables();
            return res;
        }
        // First move draw check
        if(firstMove) {
            if (drawRect.mouseInRectangle(mouseX, mouseY)) {
                newSetRect.clear();
                playerRect.shownCardsIndices.clear();
                app.thisPlayer.addFromDeck(drawRect.cards);
                playerRect.selectCard();
                playerRect.calculateRectangle();
                newSetRect.cards.add(playerRect.cards.get(playerRect.cards.size() - 1));
                updateNewSet();
                firstMove = false;
                return false;
            }
        } 
        // Discard check
        if(!firstMove && discardMarkerRect.mouseInRectangle(mouseX, mouseY) && newSetRect.cards.size() == 1) {
            if(mustEnd && playerRect.cards.size() != 1) {
                return false;
            }  
            if(!playerRect.outlinedCardsSet.isEmpty()) {
                illegalDiscard = true;
                return false;
            }
            discardRect.cards.add(newSetRect.cards.get(0));
            newSetRect.clear();
            updateNewSet();
            playerRect.cards.remove((int) playerRect.shownCardsIndices.iterator().next());
            playerRect.shownCardsIndices.clear();
            resetRoundVariables();
            return true;
        }
        // Select existing set check
        if(selectedRectIndex == -1 && (selectedRectIndex = getMoveableRectangleIndex(mouseX, mouseY)) != -1) {
            for(Card addedCard : moveableRectangleList.get(selectedRectIndex).cards) 
                newSetRect.addOutlinedCard(addedCard);            
            updateNewSet();
            return false;
        }
        // Revert check
        if(lastValidState != null && revertButtonRect.mouseInRectangle(mouseX, mouseY)) {
            int prevDiscardSize = discardRect.cards.size();
            decodeGameState(lastValidState);
            if(discardRect.cards.size() != prevDiscardSize)
                firstMove = true;
            playerRect.shownCardsIndices.clear();
            newSetRect.clear();
            updateNewSet();
            mustEnd = false;
            lastValidState = null;
            return false;
        }
        return false;
    }

    /** Resets round variables */
    private void resetRoundVariables() {
        newSetValid = false;
        firstMove = true;
        mustEnd = false;
        canDrawDiscard = false;
        illegalDiscard = false;
        moveMade = false;
        lastValidState = null;
    }

    /** Recalcualtes the newSet rectangle parameters */
    private void updateNewSet() {
        newSetRect.calculateRectangle();
        newSetRect.updateXPosition(0, 20 * app.scaleFactor, app.displayWidth);
        newSetValid = setIsValid(newSetRect.cards);
    }

    private void removeSelectedCards() {
        for(int i = playerRect.cards.size() - 1; i >= 0; i--) {
            if(playerRect.shownCardsIndices.remove(i))
                playerRect.removeCard(i);
        }
    }

    @Override
    public void handleMouseRelease(int mouseX, int mouseY) {
        if(cardOriginRect != null) {
            int newIndex = cardOriginRect.getMultiCardIndex(pickedUpRect.xCenter);
            if(cardOriginRect == playerRect && selectedCardPicked)
                cardOriginRect.shownCardsIndices.add(newIndex);
            else if (cardOriginRect == newSetRect)
                updateNewSet();
            if(outlinedCardPicked)
                cardOriginRect.outlinedCardsSet.add(newIndex);
            if(returnedCardPicked)
                returnedCardsIndices.add(newIndex);
            cardOriginRect.hiddenCardIndex = -1;
        }
        selectedCardPicked = false;
        outlinedCardPicked = false;
        returnedCardPicked = false;
        pickedUpRect = null;
        cardOriginRect = null;
    }

    @Override
    public void draw() {
        wildcardRect.draw();
        drawRect.draw();

        playerRect.draw();
        if(cardOriginRect == playerRect) {
            ((MultiOutlineRectangle) pickedUpRect).drawSingle(outlinedCardPicked);
            playerRect.drawEnd();
        }
        if(winningPlayerNumber != -1 || !moveMade && app.curPlayerNumber == app.thisPlayerNumber) {
            app.fill(100f, 180f);
            textHeader.draw();
            app.textAlign(PConstants.CENTER, PConstants.CENTER);
            app.textSize(30f * app.scaleFactor);
            app.fill(256f);
            String printText;
            if (winningPlayerNumber == app.thisPlayerNumber)
                printText = "You won the round! Total points: " + app.thisPlayer.getScore();
            else if (winningPlayerNumber != -1) 
                if(app.thisPlayer.getScore() >= 100)
                    printText = app.playerList.get(winningPlayerNumber).toString() + "won. You passed 100 points!";
                else
                    printText = String.format("%s won. Your points: %d. Total points: %d",
                        app.playerList.get(winningPlayerNumber).toString(),
                        roundScore,
                        app.thisPlayer.getScore());
            else if(firstMove)
                printText = "Your turn: Draw a card from the draw or discard deck";
            else if(mustEnd)
                printText = "Wildcard used! You must play all cards from hand, or revert";
            else if (illegalDiscard)
                printText = "You have outlined cards in your hand that must be played!";
            else
                printText = "Your turn: Discard a card to end turn";
            app.text(printText, textHeader.xCenter, textHeader.yCenter);
            app.textAlign(PConstants.LEFT);
        }

        app.fill(200f);
        revertRect.draw();
        if(lastValidState != null)
            revertButtonRect.draw();

        if(newSetRect.cards.isEmpty()) {
            otherPlayerDisplay.draw();
            app.fill(0f);
            app.textSize(30f * app.scaleFactor);
            
            int curPlayerPassed = 0;
            for(int i = 0; i < otherPlayerRect.length; i++) {
                otherPlayerRect[i].draw();
                if(i == app.thisPlayerNumber)
                    curPlayerPassed = 1;
                String toPrint = app.playerList.get(i + curPlayerPassed).toString();
                if(i + curPlayerPassed == app.curPlayerNumber)
                    toPrint += " <-";
                app.text(toPrint, app.displayWidth * 0.2f, otherPlayerRect[i].yTop + 0.01f * app.displayHeight, app.displayWidth * 0.1f, app.defaultHeight);
            }
        } else {
            newSetDisplay.draw();
            newSetRect.draw();
            if(cardOriginRect == newSetRect)
                ((MultiOutlineRectangle) pickedUpRect).drawSingle(outlinedCardPicked);
                newSetRect.drawEnd();
            cancelButtonRect.draw();
            if(newSetValid)
                confirmButtonRect.draw();
        }
        
        for(int i = 0; i < moveableRectangleList.size(); i++){
            moveableRectangleList.get(moveableRectanglePriorityList.get(i)).draw();
        }

        discardMarkerRect.draw();
        discardRect.draw();

        if(pickedUpRect != null && cardOriginRect == null) pickedUpRect.draw();
    }

    public String encodeStartingDeck(List<Card> deck) {
        String res = "";
        for(int i = 0; i < deck.size(); i++){
            res += deck.get(i).toHexString();
        }
        return res;
    }

    /**
     * Encodes the current board state. Each string is separated
     * by the letter "t" and is a hexadecimal representation of:
     * the current player number, this player's hand, the discard
     * pile, and all the played sets
     * @return
     */
    @Override
    public String encodeGameState(){
        String res = Integer.toHexString(app.curPlayerNumber);        
        res += encodeCardRect(playerRect) + "t";
        res += encodeCardRect(discardRect) + "t";
        for(int i = 0; i < moveableRectangleList.size(); i++) {
            res += encodeCardRect(moveableRectangleList.get(i)) + "t";
        }
        return res;
    }

    private String encodeCardRect(CardRectangle rect) {
        String res = "";
        List<Card> curCards = rect.cards;
        for(int i = 0; i < curCards.size(); i++)
            res += curCards.get(i).toHexString();
        return res;
    }

    public List<Card> decodeStartingDeck(String startingDeck) {
        List<Card> res = new ArrayList<Card>();
        for(int i = 0; i < startingDeck.length(); i += 2) {
            int cardVal = Integer.parseInt(startingDeck.substring(i, i+2), 16);
            res.add(Card.toCard(cardVal));
        }
        return res;
    }

    public void decodeGameState(String boardState) {
        app.noLoop();
        int updatedPlayerNum = boardState.charAt(0) - '0';
        if(updatedPlayerNum != app.curPlayerNumber)
            canDrawDiscard = false;
        if(updatedPlayerNum != app.thisPlayerNumber)
            DeckHelper.draw(drawRect.cards);
        String[] cardLists = boardState.substring(1).split("t", -1);
        List<Card> updatedList = app.playerList.get(updatedPlayerNum).hand;
        updatedList.clear();
        updatedList.addAll(decodeCardList(cardLists[0]));
        if(updatedList.isEmpty()) {
            declareWinner(updatedPlayerNum);
        }
        setRectList(discardRect, decodeCardList(cardLists[1]));
        
        int rectInd = 0;
        while(rectInd < cardLists.length - 2) {
            if(cardLists[rectInd + 2].isEmpty())
                break;

            if(rectInd >= moveableRectangleList.size()){
                moveableRectanglePriorityList.add(moveableRectangleList.size());
                moveableRectangleList.add(new MultiCardRectangle((int) drawRect.xCenter, (int) drawRect.yCenter, 1, new ArrayList<Card>(), Mode.REVEALED_ALL));
            }
            setRectList(moveableRectangleList.get(rectInd), decodeCardList(cardLists[rectInd + 2]));
            rectInd++;
        }
        for(int i = moveableRectangleList.size() - 1; i >= rectInd; i--) {
            moveableRectangleList.remove(i);
            moveableRectanglePriorityList.remove(Integer.valueOf(i));
        }

        returnedCardsIndices.clear();
        playerRect.outlinedCardsSet.clear();

        app.loop();
    }

    private void setRectList(CardRectangle rect, List<Card> newList) {
        rect.clear();
        rect.cards.addAll(newList);
        rect.calculateRectangle();
    }

    private List<Card> decodeCardList(String list) {
        List<Card> res = new ArrayList<Card>();
        for(int i = 0; i < list.length(); i += 2) {
            res.add(Card.toCard(Integer.parseInt(list.substring(i, i + 2), 16)));

        }
        return res;
    }

    @Override
    public void nextTurn(String boardState) {
        lastValidState = null;
        decodeGameState(boardState);
    }

    private void declareWinner(int winningPlayerNumber) {
        this.winningPlayerNumber = winningPlayerNumber;
        roundScore = calculateHandScore(playerRect.cards);
        for(Player p : app.playerList) {
            p.incrementScore(calculateHandScore(p.hand));
        }
    }

    private int calculateHandScore(List<Card> hand) {
        int res = 0;
        for(Card c : hand) {
            // Wildcards in hand are worth 20 points
            if(c.equals(wildcard)) {
                res += 20;
                continue;
            }
            switch(c.value) {
                // Aces worth 15 normally, or just 1 if your score is exactly 98
                case 0:
                    if(app.thisPlayer.getScore() == 98)
                        res += 1;
                    else
                        res += 15;
                    break;
                // Figures are worth 10
                case 10:
                case 11:
                case 12:
                    res += 10;
                    break;
                // Number cards are worth their face value
                default:
                    res += c.value + 1;
                    break;
            }
        }
        return res;
    }
}

class MultiOutlineRectangle extends MultiCardRectangle {

    private static PImage outlineImage;
    private static int defaultOutlineHeight;
    private static int defaultOutlineWidth;

    private int outlineWidth;
    private int outlineHeight;
    private int horizontalOutlineOffset;

    final Set<Integer> outlinedCardsSet = new HashSet<Integer>();

    public MultiOutlineRectangle(int xCenter, int yCenter, float scale, List<Card> deck, Mode drawMode) {
        super(xCenter, yCenter, scale, deck, drawMode);
        outlineWidth = (int) (defaultOutlineWidth * scale);
        outlineHeight = (int) (defaultOutlineHeight * scale);
        horizontalOutlineOffset = (int) ((outlineWidth - defaultWidth) / 2);
    }    

    public static void setup() {
        outlineImage = app.loadImage("app/src/main/resources/Poker Cards PNG/card_outline.png");
        defaultOutlineWidth = (int) (app.defaultWidth + 10 * app.scaleFactor);
        defaultOutlineHeight = (int) (app.defaultHeight + 10 * app.scaleFactor);
    }

    @Override
    public void draw() {
        if(cards.isEmpty())
            return;
        switch(drawMode){
            case REVEALED_SELECT:
                drawMultiSelect(0);
                return;
            case REVEALED_ALL:
                drawMulti(0);
                return;
            default:
                super.draw();
                return;
        }
    }

    private void drawMulti(int startIndex) {
        for(int i = startIndex; i < cards.size(); i++){
            if(i == hiddenCardIndex) return;
            app.image(app.imageList[cards.get(i).hashCode()], xLeft + i * hSpacing, yTop, defaultWidth, defaultHeight);
            if(outlinedCardsSet.contains(i))
                app.image(outlineImage, xLeft - horizontalOutlineOffset + i * hSpacing, yCenter - outlineHeight / 2, outlineWidth, outlineHeight);
        }
    }

    private void drawMultiSelect(int startIndex) {
        for(int i = startIndex; i < cards.size(); i++){
            if(i == hiddenCardIndex) return;
            int offset = shownCardsIndices.contains(i) ? (int) vSpacing : 0;
            app.image(app.imageList[cards.get(i).hashCode()], xLeft + i * hSpacing, yTop - offset, defaultWidth, defaultHeight);
            if(outlinedCardsSet.contains(i))
                app.image(outlineImage, xLeft - horizontalOutlineOffset + i * hSpacing, yCenter - outlineHeight / 2 - offset, outlineWidth, outlineHeight);
        }
    }

    public void drawEnd() {
        switch(drawMode){
            case REVEALED_ALL:
                drawMulti(hiddenCardIndex + 1);
                return;
            case REVEALED_SELECT:
                drawMultiSelect(hiddenCardIndex + 1);
                return;
            default:
                System.out.println("Unsupported draw mode");
                System.exit(1);
        }
    }

    /**
     * Removes the card at the given index while updating the outlinedCardsSet
     * @param index
     * @return
     */
    public Card removeCard(int index) {
        if(outlinedCardsSet.remove(index));
            for(int i = index + 1; i < cards.size(); i++)
                if(outlinedCardsSet.remove(i))
                    outlinedCardsSet.add(i - 1);
        return cards.remove(index);
    }

    public void addOutlinedCard(Card addedCard) {
        outlinedCardsSet.add(cards.size());
        cards.add(addedCard);
    }

    public void clear() {
        outlinedCardsSet.clear();
        shownCardsIndices.clear();
        cards.clear();
    }

    public void drawSingle(boolean outline) {
        app.image(app.imageList[cards.get(0).hashCode()], xLeft, yTop, defaultWidth, defaultHeight);
        if(outline)
            app.image(outlineImage, xLeft - horizontalOutlineOffset, yCenter - outlineHeight / 2, outlineWidth, outlineHeight);
    }
}
