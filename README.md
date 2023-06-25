# Card_Game
 Playble Card Game through discord bot (Pontinho, more to be added later)

# Pontinho Rules
 This is a Brazilian Card game that my family likes to play, and I wanted a way to play with them from overseas (a niche game even in Brazil, so no such service exists already and I don't wanna pay for something like TableTop Simulator)

## Setup
 2+ players can play, though currently this only works for 2 players exactly (more players functionality to be added later)
 
 2 poker decks are shuffled (no jokers), and one player is assigned as the dealer. That player gives 9 cards face down to all players, done as 3 sets of 3 to each. The player right before the dealer cuts the deck before dealing and reveals a random card face up on the board (this is automated in the game obviously). The card 1 point above the revealed card of the same suit will act as the wildcard for this round. (E.g. a revealed 2 of hearts means the 3 of hearts is the wildcard, and a revealed K of spades means the A of spades is the wildcard)

## Gameplay
Play starts with the player after the dealer and rotates clockwise. On their turn, a player can either draw the top card from the deck or draw the top card from the discard pile (the revealed card counts as "discarded" for the first round). To end their turn, a player either discards a card from their hand or plays all cards from their hand, which ends the round.

### Sets and runs
A player can, on their turn (with some exceptions with discarded cards) play a set or run from their hand if they have one. (Note that no such distinction exists, but this is just to assign some names).

A set is a group of 3 or more cards all of the same value. 3 of these cards must be of unique suits, and any additional cards must match these 3 suits. (K of hearts, K of spades, K of diamonds is valid. You can add another K of hearts, but not a K of clubs).

A run is a group of 3 more or more cards all of the same suit and in sequential order (Like 3, 4, 5). The Ace can either be in an A,2,3 run or an A,K,Q run, but not a K,A,2 run.

On their turn (again, some exceptions with discarded cards), a player may play any number of cards from their hand on any played set or run prior. In doing so, they must keep the sets or runs as sets or runs.

### Out-of-order discarded cards (NOT IMPLEMENTED)
Whenever a card is discarded, any player may take that discarded card, with priority given to players earlier in turn order (e.g. next-to-act gets first priority). However, a player that will win the round if they take the discarded is given the highest priority (though if two are able to win, then turn order takes priority again). If a player does take that card, it must be used and played immediately in a set of exactly 3 cards (exception given below). A run of three cards can either be appended to an existing set if applicable or played as its own (sets do not matter in this case, but appending runs can block cards from being played). If the next-to-act player took the discarded card, then they do not need to draw at the start of the round (and can add to their newly played set as usual), though if another player takes the discarded card, they will still need to draw on their turn.

#### Exception: Extending existing runs with discarded cards (DEFINITELY NOT IMPLEMENTED)
A very specific case has an exception. If a run exists and a card is discarded and you have a card you can use to extend that set and then play that discarded card, you are allowed to use that card and your own to extend that run. For example, if A23 is on the board and someone discards a 5 while you have a 4 in hand, you are allowed to take that discarded card to extend the run to A2345. (This is most common with wildcards, see below)

### Wildcards
The card one above the revealed card and of the same suit is the wildcard for this round. It can be played as its face value, after which none of these rules apply. A wildcard can replace any card in a set or run. However, to legally play a wildcard it must be played in a run with non-wild cards on either side (such as 3, Wild, 5 and not 3, 4, Wild). True wildcards in any set or run can only be played if you are going to win on your turn (or after taking the discarded card).

If a wildcard is in effect on the board, pn a player's turn (or when taking a discarded card out of turn, though normal 3-card rules still apply), they can replace that wildcard with the card it replaced. If a player does replace it, it must be used immediately before their turn ends (they can't replace the wildcard and keep it in their hand).

Wildcards cannot be discarded (NOT IMPLEMENTED), nor can cards that can replace an in-effect wildcard

## Scoring
Once a player plays all cards from their hand, scoring begins. Each player counts up the score of their hand (A is worth 15 -- unless they have exactly 98 points, where A is worth 1 --, JQK are each worth 10, wildcards are worth a flat 20, and all other numbered cards are worth their face value) and adds it to their running total, trying to keep the total as low as possible. (EVERYTHING PAST HERE NOT IMPLEMENTED) Whenever a player passes 100 points, they go bust. If all players except for one go bust in a round, then that surviving player is the winner of the game. Otherwise, all busted players return to the game with the score of the highest surviving player (paying the entry fee again if this were a legit cash game)

## Coding Philosophy
Each player will run an instance of the PApplet and will be connected to the same discord bot. One player hosts the game, and the rest join it. Once the game starts, each player has an independent instance of the game running, and each time a turn is played, the bot will communicate their moves to everyone else in the game. Thus, each player will always have the same board state at every stage of the game.
