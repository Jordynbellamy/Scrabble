# Scrabble

Milestone 1

Text-based Scrabble you play in the terminal. 2-4 players can place words or exchange tiles, and the game checks each move before updating the board and scores. Blank tiles and premium squares aren't in yet (that's Milestone 3).

Running it

Make sure wordlist.txt is in the same folder as the jar, then run:

java -jar Scrabble.jar

Moves use Scrabble notation:

- PLAY 8H HELLO places HELLO across starting at row 8, column H
- PLAY H8 HELLO places it down instead
- EXCHANGE AEI swaps those tiles and ends your turn
- PASS skips your turn
  
What's included: 

- Scrabble.jar - source code, compiled classes and JUnit tests
- src/ - the game code
- test/ - JUnit 5 tests
- wordlist.txt - the word list
- UML class diagram
- 2 sequence diagrams: exchanging tiles and placing a word
  
Current state

Milestone 1 is complete. The game supports 2-4 players, shows each player's tiles, lets players place words using Scrabble notation, and checks every move (real words only, no overwriting tiles, reusing tiles already on the board). Scores update after each word, and players can exchange tiles to pass their turn. After every turn the board, scores and the next player's tiles are printed.

Data structures

Tile bag - an ArrayList<Tile>, since the bag shrinks and grows as tiles are drawn and returned. It gets shuffled when it's created and whenever tiles go back in. We draw from the end of the list because removing the last item doesn't shift anything, and since the bag is shuffled the last tile is still random.

Player rack - also an ArrayList<Tile>, max 7 tiles. getRack() returns a copy so other classes can't change a player's tiles directly. To check if a player can spell a word, hasLetters goes through a copy of the rack and crosses off each letter it uses, so a word like EEL actually needs two E's.

Board - a Tile[15][15] array, so any square can be looked up directly by row and column. Empty squares are null.

Word list - a HashSet<String>, so checking if a word exists is quick instead of searching through 10,000 words every time.

Testing

JUnit 5 tests are in test/:

TileTest - letter and points, lowercase gets converted to uppercase, toString
TileBagTest - starts with 98 tiles, correct letter count, drawing and returning tiles, drawing every tile until the bag is empty
PlayerTest - name and score, refilling the rack, hasLetters with repeated letters, removing letters, exchanging (normal case, missing letters, not enough tiles in the bag), adding to the score
BoardTest - placing tiles, not overwriting tiles, first word covering the centre, board labels matching Scrabble notation
WordListTest - valid and invalid words, upper and lowercase input
ScorerTest - letter values, word scores, scoring a new word, extending an existing word and the seven-tile bonus
GameModelTest - number of players, reading Scrabble notation, turn order
  
We didn't test the shuffle directly since it's random and a test for it could fail by chance.

Failing tests: none, all tests pass.

Known issues

- Player.addTile doesn't check if the rack is full. It's only used to set up racks in tests, so it doesn't affect the game.

Contributions: Milestone 1

Kosi Chukwuezi: project setup, Tile, TileBag, Player, their JUnit tests, README

Iniodu Jackson: WordList, Scorer and their tests, exchanging tiles sequence diagram

Jordyn Bellamy: Create repository, Board and its tests, UML diagram, building the jar, submitting to Brightspace

Peculiar Ojediran: GameModel, its tests, placing-a-word sequence diagram
