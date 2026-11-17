# Scrabble

Milestone 1:

Text-based Scrabble you play in the terminal. 2-4 players can place words or exchange tiles, and the game checks each move before updating the board and scores. Blank tiles and premium squares aren't in yet (that's Milestone 3).

Run:

java -jar Scrabble.jar

wordlist.txt is included inside the jar.

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
  
Current state:

Milestone 1 is complete. The game supports 2-4 players, shows each player's tiles, lets players place words using Scrabble notation, and checks every move (real words only, no overwriting tiles, reusing tiles already on the board). Scores update after each word, and players can exchange tiles to pass their turn. After every turn the board, scores and the next player's tiles are printed.

Data structures:

Tile bag - an ArrayList<Tile>, since the bag shrinks and grows as tiles are drawn and returned. It gets shuffled when it's created and whenever tiles go back in. We draw from the end of the list because removing the last item doesn't shift anything, and since the bag is shuffled the last tile is still random.

Player rack - also an ArrayList<Tile>, max 7 tiles. getRack() returns a copy so other classes can't change a player's tiles directly. To check if a player can spell a word, hasLetters goes through a copy of the rack and crosses off each letter it uses, so a word like EEL actually needs two E's.

Board - a Tile[15][15] array, so any square can be looked up directly by row and column. Empty squares are null.

Word list - a HashSet<String>, so checking if a word exists is quick instead of searching through 10,000 words every time.


Testing:


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

Known issues:

- Player.addTile doesn't check if the rack is full. It's only used to set up racks in tests, so it doesn't affect the game.

Contributions: Milestone 1

Kosi Chukwuezi: project setup, Tile, TileBag, Player, their JUnit tests, README

Iniodu Jackson: WordList, Scorer and their tests, exchanging tiles sequence diagram

Jordyn Bellamy: Create repository, Board and its tests, UML diagram, building the jar, submitting to Brightspace

Peculiar Ojediran: GameModel, its tests, placing-a-word sequence diagram

---

Milestone 2:

Scrabble now has a graphical interface and is played with the mouse instead of typed commands. 2-4 players take turns placing words, exchanging tiles or passing, and the game still checks each move before updating the board and scores. The game now uses the MVC pattern and the Java Event Model. Blank tiles and premium squares aren't in yet (that's Milestone 3).

Running it:

Make sure wordlist.txt is in the same folder as the jar, then run:

java -jar Scrabble.jar

To run it from IntelliJ, run ScrabbleFrame (it has the main method). GameModel no longer has a main method.

How to play:

- Pick the number of players (2-4) in the box that opens
- To place a word, click the square the word starts on (it turns yellow), type the whole word in the Letters box, pick Across or Down, then click Play Word
- If your word goes through tiles already on the board, type the whole word including those letters. For example, if WORD is on the board, click the W and type WATCH to play WATCH down
- To exchange tiles, type the letters you want to swap in the Letters box (for example AE) and click Exchange. This ends your turn
- To skip your turn, click Pass
- If a move isn't allowed, the reason is shown at the bottom of the window and it's still your turn

What's included:

- Scrabble.jar - source code, compiled classes and JUnit tests
- src/ - the game code
- test/ - JUnit 5 tests
- wordlist.txt - the word list
- UML class diagram, updated for Milestone 2
- 2 sequence diagrams, updated for the GUI: placing a word and exchanging tiles

Current state:

Milestone 2 is complete. The game opens in a JFrame and asks for the number of players. The window shows the board, every player's score, and the current player's tiles. Players place words, exchange tiles and pass using the mouse, and the game moves to the next player after each of these. Placed tiles are grey and the selected starting square is yellow. Every move is still checked by the model (real words only, no overwriting tiles, reusing tiles already on the board, first word on the centre square, words must connect).

The game follows MVC:

- Model: GameModel holds the game and enforces the rules. It doesn't use any Swing code
- View: ScrabbleFrame displays the game. It implements ScrabbleView, so the model can send it updates
- Controller: ScrabbleController handles every button click and calls the matching method on the model

It uses the Java Event Model: after every move, GameModel sends a ScrabbleEvent to each ScrabbleView. If the event's message is empty the move worked and the frame redraws itself from the model. Otherwise the message says why the move was rejected and the frame shows it.

Changes since Milestone 1:

- GameModel no longer has main, play, printState or the Scanner. The console game was replaced by the GUI, so the model doesn't need to read input or print anything anymore
- GameModel has a list of ScrabbleViews, with addScrabbleView, removeScrabbleView and notifyViews. This is how the Java Event Model tells the view the game changed, so the view never has to check the model by itself
- GameModel now notifies the views when a move is rejected too, so the reason can be shown in the window
- Added ScrabbleEvent, which extends EventObject. It carries the model as the source and a message that is empty if the move worked or holds the error if it didn't
- Added the ScrabbleView interface, ScrabbleFrame (the view) and ScrabbleController (the controller) to complete MVC
- The UML class diagram was updated with the four new classes and the new GameModel fields and methods, and the removed console methods were taken out
- The sequence diagrams now start from a mouse click in ScrabbleFrame and go through ScrabbleController, instead of starting from typed commands

New data structures:

Views - GameModel keeps an ArrayList<ScrabbleView>. A list lets more than one view watch the same game, and views can be added and removed.

Board buttons - ScrabbleFrame keeps a JButton[15][15] array that matches the board array, so updating the square at a row and column is a direct lookup.

The tile bag, player rack, board and word list are the same as in Milestone 1.

Testing:

There are no GUI tests, as allowed for this milestone. TileTest, TileBagTest, PlayerTest, BoardTest, WordListTest and ScorerTest are the same as in Milestone 1.

GameModelTest - updated for the new GameModel constructor (it no longer takes a Scanner). New tests check that views get an event with no message after a word, exchange or pass, that views get the error message after a rejected move, and that views can be added and removed.

Failing tests: none, all tests pass.

Known issues:

- Player.addTile still doesn't check if the rack is full. It's only used to set up racks in tests, so it doesn't affect the game.
- The current player's tiles are shown as text, for example A(1), instead of as tile buttons.
- The game has no ending yet. It keeps going until the window is closed, even if every player passes or the bag runs out.
- The word list includes some names and abbreviations that aren't allowed in real Scrabble.

Contributions: Milestone 2

Kosi Chukwuezi - Created ScrabbleView and ScrabbleEvent. Updated GameModel to notify views and removed Scanner input. Added view notification tests to GameModelTest.

Iniodu Jackson: Updated the UML class diagram for Milestone 2.

Jordyn Bellamy: ScrabbleFrame (the view), ScrabbleController (the controller), updating GameModelTest for the event model, README, building the jar, submitting to Brightspace

Peculiar Ojediran: Updated the two sequence diagrams for Milestone 2.
