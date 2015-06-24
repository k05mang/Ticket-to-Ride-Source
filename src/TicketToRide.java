import java.util.*;
import javax.swing.JFrame;
import java.awt.event.*;
import javax.swing.JLayeredPane;
import javax.swing.JTextField;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import java.awt.*;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
/**
 * Ticket to Ride game
 * 
 * @author Kevin Mango, Marissa Bianchi, Ryan Clancy, Mathew Banville
 * @version 5-6-2013
 */
public class TicketToRide extends MouseAdapter implements ActionListener
{
    private int width, height, numDraws, numPlayers, numPlayersInit, 
    firstTurnsLeft, next;
    private TrainCard display, claimsSelect, hand, auto, cardGotten;
    private TrainDeck deck;
    private ArrayList<Player> playerlist;
    private DestinationDeck destDeck;
    private ArrayList<DestinationCard> fromDeck;
    private Board board; 
    private boolean drawTrain, endPhase, claimRoute, init, firstDraw, 
    isFullGame, endGame; 
    private ArrayList<String> colorsSelection;
    private JInternalFrame playerInit;
    private JTextField name;
    private JButton done;
    private JList colors;
    private Player current;
    private JLayeredPane container;
    private FinishedButton finish, cancel;
    private ClaimCards claimCards;
    private Path claim, claimSister;
    private Hashtable<String, Integer> playerInfo;
    private Hashtable<Integer,Integer> scoreTally;
    private ArrowButton up, down;
    private ScoreScreen scores;
    private Scores displayScore;
    private ArrayList<Integer> destComplete, destPoints, asianExplorerIndices;
    private Timer doubleClick;
    private Hashtable<City, Boolean> dfs;
    private Player winner;
    /**
     * Constructs a game of Ticket to Ride 
     */
    public TicketToRide(){
        colorsSelection = new ArrayList<String>(
            Arrays.asList(new String[]{"gold","purple","white","orange","blue"}));
        JFrame application = new JFrame();
        application.setExtendedState(JFrame.MAXIMIZED_BOTH);
        width = (int)application.getToolkit().getScreenSize().getWidth();
        height = (int)application.getToolkit().getScreenSize().getHeight();
        application.setPreferredSize(new Dimension(width,height));
        container = new JLayeredPane();
        board = new Board(0,0,width,height,width, height);
        deck = new TrainDeck(7*width/8,5*height/8,287*board.getXScale(),
            (950*board.getYScale())/5.75,width, height);
        destDeck =  new DestinationDeck(20,20,287*board.getXScale(),
            173*board.getYScale(),width,height);
        finish = new FinishedButton(82*board.getXScale(),860*board.getYScale(),
            219*board.getXScale(),123*board.getYScale(), false);
        cancel = new FinishedButton(82*board.getXScale(),
            860*board.getYScale()-123*board.getYScale()-10,
            219*board.getXScale(),123*board.getYScale(), true);
        dfs = new Hashtable<City, Boolean>(39);
        for(City faux: board.getCities()){
            dfs.put(faux,false);
        }
        claimCards = new ClaimCards(0,height/5);
        up = new ArrowButton(10,1047*board.getYScale(),40,false);
        down = new ArrowButton(10,1047*board.getYScale()+50,40,true);
        scores = new ScoreScreen();
        displayScore = new Scores(2287.8*board.getXScale(),1047*board.getYScale());
        displayScore.setBounds(0,0,width, height);
        displayScore.setVisible(false);
        scores.setBounds(0,0,width, height);
        scores.setVisible(false);
        up.setBounds(0,0,width, height);
        down.setBounds(0,0,width, height);
        finish.setBounds(0,0,width,height);
        cancel.setBounds(0,0,width, height);
        cancel.setVisible(false);
        claimCards.setBounds(0,0,width,height);
        claimCards.setVisible(false);
        init = true;
        endPhase = false;
        numDraws = 0;
        firstTurnsLeft = 0;
        doubleClick = new Timer(300, this);
        doubleClick.setRepeats(false);
        doubleClick.setActionCommand("doubleClick");
        endGame = false;
        scoreTally = new Hashtable<Integer,Integer>();
        scoreTally.put(new Integer(1),new Integer(1));
        scoreTally.put(new Integer(2),new Integer(2));
        scoreTally.put(new Integer(3),new Integer(4));
        scoreTally.put(new Integer(4),new Integer(7));
        scoreTally.put(new Integer(5),new Integer(10));
        scoreTally.put(new Integer(6),new Integer(15));
        container.add(board.container, new Integer(0));
        container.add(deck.container, new Integer(1));
        container.add(destDeck.container, new Integer(1));
        container.add(up, new Integer(1));
        container.add(down, new Integer(1));
        container.add(claimCards, new Integer(2));
        container.add(finish, new Integer(3));
        container.add(cancel, new Integer(3));
        container.add(claimCards.storedCards, new Integer(4));
        container.add(scores, new Integer(7));
        container.add(displayScore, new Integer(3));
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.addMouseMotionListener(this);
        application.addMouseListener(this);
        application.setContentPane(container);
        application.pack();
        application.setVisible(true);
    }

    /**
     * Method used for starting the game asking for the number of players 
     * and determining if the game will be a full game or not
     */
    public void startGame(){
        numPlayers = 0;
        while(numPlayers < 2 || numPlayers > 5){
            String input = JOptionPane.showInputDialog(null,
                    "How many players will there be?");
            if(input == null)
                input = "";
            if(input.length() > 0 && input.length() < 10){
                if(Character.isDigit(input.charAt(0))){
                    numPlayers = Integer.parseInt(input);
                }
                else{
                    JOptionPane.showMessageDialog(null,
                        "That is not a number please put in a numerical value to use");
                }
            }
            else{
                JOptionPane.showMessageDialog(null,
                    "The value you put in is too long to be valid or"+
                    "\nis a number too big or small for the game");
            }
        }
        if(numPlayers > 3)
            isFullGame = true;
        else
            isFullGame = false;
        firstTurnsLeft = numPlayersInit = numPlayers;
        next = 1;
        playerlist = new ArrayList<Player>(numPlayers);
        destComplete =  new ArrayList<Integer>(numPlayers);
        destPoints =  new ArrayList<Integer>(numPlayers);
        createUserInfo();
    }

    /**
     * method for creating the user info window to get
     * info on the players and if all the players have 
     * been initialized then it begins the game
     */
    protected void createUserInfo(){
        if(numPlayersInit > 0){
            if(numPlayersInit == numPlayers){
                JOptionPane.showMessageDialog(null,
                    "Make sure this player is the most experienced player");
            }
            playerInit = new JInternalFrame("Player Start",false,true);
            playerInit.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            playerInit.setPreferredSize(new Dimension(200,175));
            name = new JTextField(1);
            done = new JButton("Done");
            done.setActionCommand("end");
            playerInit.add(name,BorderLayout.NORTH);
            playerInit.add(done,BorderLayout.SOUTH);
            colors = new JList(colorsSelection.toArray());
            playerInit.add(colors,BorderLayout.CENTER);
            container.add(playerInit, new Integer(5));
            container.validate();
            playerInit.pack();
            playerInit.setVisible(true);
            done.addActionListener(this);
        }
        else{
            current = playerlist.get(0);
            current.show();
            firstDraw = true;
            init = false;
            beginGame();
        }
    }

    /**
     * starts the game 
     */
    public void beginGame(){
        firstTurn();
        displayScore.setVisible(true);
        displayScore.repaint();
        deck.addToDisplay();
    }

    /**
     * sets up the interface for the players first turn
     */
    public void firstTurn(){
        firstDraw = true;
        Thread bufferDraw = new Thread(new Runnable(){
                    public void run(){
                        try{
                            for(int draw = 0;draw < 4;draw++){
                                TrainCard temp = deck.draw();
                                Thread.sleep(1000);
                                current.addToHand(temp);
                            }
                        }catch(InterruptedException e){
                            System.err.println(e.getMessage());
                        }
                    }
                }
            );
        bufferDraw.start();
        bufferDraw = null;
        fromDeck = destDeck.draw(destDeck.getX()+30, 
            destDeck.getY()+destDeck.getHeight()+30, true);
    }

    /**
     * called when a turn ends setting state variables to false
     * and performing checks on the previous players state before
     * switching to a new player this determines if the game needs 
     * to end
     */
    public void endTurn(){
        claimRoute = false;
        fromDeck = null;
        drawTrain = false;
        firstDraw = false;
        numDraws = 0;
        JOptionPane.showMessageDialog(null,"You have completed your turn"+
            "\nwe will now be moving onto the next player");
        if(current.getTokens() < 3){
            endPhase = true;
            JOptionPane.showMessageDialog(null,"You have too few tokens left"+
                " end game mode will begin, everyone will have one last turn");
        }
        current.hide();
        if(deck.discardNotEmpty() && deck.deckIsEmpty()){
            deck.shuffle();
            if(deck.getDisplay().size() < 5){
                deck.addToDisplay();
            }
        }
        if(numPlayers > 0){
            if(next < playerlist.size()){
                current = playerlist.get(next);
                next++;
                current.show();
            }
            else{
                next = 0;
                current = playerlist.get(next);
                next++;
                current.show();
            }
            if(firstTurnsLeft > 1){
                firstTurnsLeft--;
                firstTurn();
            }
            if(endPhase){
                numPlayers--;
            }
        }
        else{
            endGame();
        }
    }

    /**
     * method called to finalize players scores and display the score screen
     */
    public void endGame(){
        endGame = true;
        int asianExplorer = 0;
        asianExplorerIndices = new ArrayList<Integer>();
        ArrayList<Integer> mountainCounts = new ArrayList<Integer>();
        //create all the final scores
        for(int player = 0;player< playerlist.size();player++){
            Player finalScore = playerlist.get(player);
            ArrayList<Integer> scores = destPaths(finalScore);
            mountainCounts.add(finalScore.getMountains());
            if(asianExplorer < scores.get(0)){
                asianExplorerIndices.clear();
                asianExplorerIndices.add(player);
                asianExplorer = scores.get(0);
            }
            else if(asianExplorer == scores.get(0)){
                asianExplorerIndices.add(player);
            }
            destComplete.add(scores.get(1));
            destPoints.add(scores.get(2));
            finalScore.setScore(finalScore.getScore()+scores.get(2));
        }
        
        for(Integer getIndex: asianExplorerIndices){
            playerlist.get(getIndex).setScore(
                playerlist.get(getIndex).getScore()+10);
        }
        int playerWin = 0;
        ArrayList<Integer> winnerIndices = new ArrayList<Integer>();
        //find a winner
        for(int get = 0; get < playerlist.size();get++){
            if(playerlist.get(get).getScore() > playerWin){
                playerWin = playerlist.get(get).getScore();
                winnerIndices.clear();
                winnerIndices.add(get);
            }
            else if(playerlist.get(get).getScore() == playerWin){
                winnerIndices.add(get);
            }
        }
        //if there are more than one potential winners
        //check rule conditions for resolving this
        if(winnerIndices.size() > 1){
            ArrayList<Integer> destCountIndices = new ArrayList<Integer>();
            int destCount = 0;
            for(int dest = 0;dest < winnerIndices.size(); dest++){
                if(destCount < destComplete.get(dest)){
                    destCountIndices.clear();
                    destCountIndices.add(dest);
                    destCount = destComplete.get(dest);
                }
                else if(destCount == destComplete.get(dest)){
                    destCountIndices.add(dest);
                }
            }
            if(destCountIndices.size() > 1){
                int mntCount = 0;
                int win = 0;
                for(int dest = 0;dest < destCountIndices.size(); dest++){
                    int pos = destCountIndices.get(dest);
                    if(mntCount < mountainCounts.get(pos)){
                        win = pos;
                    }
                }  
                winner = playerlist.get(win);
            }
            else{
                winner = playerlist.get(destCountIndices.get(0));
            }
        }
        else{
            winner = playerlist.get(winnerIndices.get(0));
        }
        scores.setVisible(true);
        scores.draw();
    }

    /**
     * method for acquiring information about a player in the end game
     * which includes their longest chain of cities, the points they
     * earn and lose from the destination cards completed and incomplete
     * and the number of cards completed
     * 
     * @param curPlay the player this method is working on determing about
     * @return returns an arrayList of integers representing the values 
     * determined by the code
     */
    public ArrayList<Integer> destPaths(Player curPlay){
        // paths owned by the player
        ArrayList<Path> paths = curPlay.getPathes();
        // dsetination cards in players hand
        ArrayList<DestinationCard> destHand = curPlay.getDestHand();
        ArrayList<String> citiesNames = new ArrayList<String>();
        ArrayList<City> cities = new ArrayList<City>();
        ArrayList<Integer> score = new ArrayList<Integer>();
        //create the players graph
        for(Path getCities:paths){
            String aName = getCities.getCityA().getName();
            String bName = getCities.getCityB().getName();
            City a = getCities.getCityA();
            City b = getCities.getCityB();
            if(!citiesNames.contains(aName)){
                citiesNames.add(aName);  
            }
            if(!citiesNames.contains(bName)){
                citiesNames.add(bName); 
            }
            if(!cities.contains(a)){
                cities.add(a);  
            }
            if(!cities.contains(b)){
                cities.add(b); 
            }
        }
        int countCompleted = 0;
        int pointTotal = 0;
        for (DestinationCard destCard : destHand){
            String[] curCities = destCard.destinations();
            String startCity = curCities[0];
            String destCity = curCities[1];
            int points = destCard.value();
            if(citiesNames.contains(startCity) && citiesNames.contains(destCity)){
                City begin = cities.get(citiesNames.indexOf(startCity));
                City end = cities.get(citiesNames.indexOf(destCity));
                boolean found = findEnd(cities, begin, end);
                if(found){
                    pointTotal += points;
                    countCompleted++;
                }
                else{
                    pointTotal -= points;
                }
            }
            else{
                pointTotal -= points;
            }
        }
        for(City reset: board.getCities()){
            dfs.put(reset, false);
        }
        score.add(asianExplorer(cities));
        for(City reset: board.getCities()){
            dfs.put(reset, false);
        }
        score.add(countCompleted);
        score.add(pointTotal);
        return score;
    }

    /**
     * method for finding the end city from a start city
     * , this is used in finding out if a plyer has completed
     * a path on a destination card
     * 
     * @param cities a list of the players cities that are a part of 
     * their paths claimed
     * @param start the city to start looking from 
     * @param end the city to find 
     */
    public boolean findEnd(ArrayList<City> cities, City start, City end){
        asianExplorerDfs(cities, start);
        if(dfs.get(end)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * method for finding the longest chain of cities for 
     * a given list of cities representing a graph 
     * 
     * @param cities arrayList of cities to search through
     * 
     * @return a integer representing the number of connected 
     * cities in a component of the graph representative of
     * the city list
     */
    public int asianExplorer(ArrayList<City> cities){
        int result = 0;
        for(City root: cities){
            if(!dfs.get(root)){
                int explored = asianExplorerDfs(cities, root);
                result = (explored > result ? explored:result);
            }
        }
        return result;
    }

    /**
     * depth first search for summing the number of cities along 
     * a given connected component of the graph of cities given
     * 
     * @param cities list of cities representing the graph of cities
     * @param root the node to start the dfs from
     * 
     * @return the number of cities along a component rooted at root
     */
    public int asianExplorerDfs(ArrayList<City> cities, City root){
        dfs.put(root, true);
        int sum = 0;
        for(City adj:root.getAdj()){ 
            if(cities.contains(adj) && !dfs.get(adj)){
                sum += asianExplorerDfs(cities, adj);
            }
        }
        return sum+1;
    }

    /**
     * method for listening to a action preformed from a button
     * object
     * 
     * @param e an action event 
     */
    public void actionPerformed(ActionEvent e){
        if(e.getActionCommand().equals("end")){
            String playerName = name.getText();
            String color = (String)colors.getSelectedValue();
            if(playerName.length() != 0 && playerName.length() < 10 && 
            color != null){
                boolean sameName = false;
                //check if the name has already been used
                for(Player init:playerlist){
                    if(init.getName().equals(playerName)){
                        JOptionPane.showMessageDialog(null,"You have selected a name"
                            +" that has already been taken");
                        sameName = true;
                        break;
                    }
                }
                if(!sameName){
                    colorsSelection.remove(color);
                    Player add = new Player(playerName,404*board.getXScale(),
                            1047*board.getYScale(),
                            1701*board.getXScale(),
                            222*board.getYScale(),
                            deck.getCardWidth()/2,
                            deck.getCardHeight(),
                            color, width, height);
                    add.hide();
                    playerlist.add(add);
                    playerInit.dispose();
                    container.add(add.hand, new Integer(4));
                    container.validate();
                    numPlayersInit--;
                    playerInit = null;
                    name = null;
                    done = null;
                    createUserInfo();
                }
            }
            else{
                JOptionPane.showMessageDialog(null,"You have not selected a "+
                    "name or color \nor the name"+
                    " you inputed was too long, a name can only be 10 cahracters "+
                    " long\nplease select a name and color");
            }
        }
    }

    /**
     * method for an event of pressing the mouse button
     * performing various actions based on the parameter
     * 
     * @param e mouse event that contains information 
     * about the current mouse action
     */
    public void mousePressed(MouseEvent e){
        double x = e.getPoint().getX();
        double y = e.getPoint().getY();
        boolean twoClicks = false;
        if(doubleClick.isRunning()){
            twoClicks = true;
            doubleClick.stop();
        }
        else{
            doubleClick.start();
        }
        //check all boolean values representing the different
        //states for the game, performing different task based 
        //on the state
        if(!init){
            if(!endGame){
                if(!claimRoute){
                    if(fromDeck == null){
                        if(deck.contains(x,y)){
                            if(numDraws < 2){
                                if(!deck.deckIsEmpty()){
                                    final TrainCard blind = deck.draw();
                                    Thread buffered = new Thread(new Runnable(){
                                                public void run(){
                                                    try{
                                                        TrainCard buffer = blind;
                                                        Thread.sleep(500);
                                                        current.addToHand(buffer);
                                                    }
                                                    catch(InterruptedException e){
                                                        System.err.println(e.getMessage());
                                                    }
                                                }
                                            }
                                        );
                                    buffered.start();
                                    numDraws++;
                                    if(deck.getDisplay().size() < 5 && 
                                    deck.deckIsEmpty()){
                                        int normCount = 0;
                                        for(TrainCard dis: deck.getDisplay()){
                                            if(!dis.getType().equals("loco")){
                                                normCount++;
                                            }
                                        }
                                        if(normCount == 0){
                                            numDraws++;
                                        }
                                    }
                                    drawTrain = true;
                                    if(numDraws > 1){
                                        endTurn();
                                    }
                                }
                                else{
                                    JOptionPane.showMessageDialog(null,
                                        "There are no cards left in the deck to "+
                                        "draw from");
                                }
                                if(numDraws > 1){
                                    endTurn();
                                }
                            }
                        }
                        else if(destDeck.contains(x,y) && !drawTrain && 
                        display == null){
                            fromDeck = destDeck.draw(destDeck.getX()+30, 
                                destDeck.getY()+destDeck.getHeight()+30, false);
                            if(fromDeck.size() == 0){
                                fromDeck = null;
                            }
                        }
                        else if(board.pathContains(x,y) && !drawTrain && 
                        display == null){
                            claim = board.getPath(x,y);
                            claimSister = board.getDuplicatePath(claim);
                            int length = claim.getLength();
                            int tokenCost = claim.getMountains() + length;
                            boolean isFerry = claim.hasFerry();
                            boolean possible = true;
                            String color = claim.getColor();
                            playerInfo = current.getHandContents();
                            if(claim.isOwned()){
                                JOptionPane.showMessageDialog(null,
                                    "This path is already claimed\n"+
                                    "Please claim another route or perform "+
                                    "another turn action");
                            }
                            else if(current.getTokens() < tokenCost){
                                JOptionPane.showMessageDialog(null,
                                    "You do not have enough"+
                                    " tokens to claim this route");
                            }
                            else if(playerInfo.get("loco") == 0 && isFerry){
                                JOptionPane.showMessageDialog(null,
                                    "You do not have a locomotive"+
                                    " card which is required to claim this route");
                            }
                            else if(claimSister != null && isFullGame &&
                            claimSister.getOwner() != null && 
                            claimSister.getOwner().equals(current)){
                                JOptionPane.showMessageDialog(null,
                                    "You already own the opposite route"+
                                    " ,you may only own one "+
                                    "of these routes at a time");
                            }
                            else{
                                //check hand validity
                                if(claim.getColor().equals("grey")){
                                    int loco = playerInfo.get("loco");
                                    int black = playerInfo.get("black");
                                    int blue = playerInfo.get("blue");
                                    int green = playerInfo.get("green");
                                    int orange = playerInfo.get("orange");
                                    int purple = playerInfo.get("purple");
                                    int red = playerInfo.get("red");
                                    int white = playerInfo.get("white");
                                    int yellow = playerInfo.get("yellow");
                                    possible = loco+black >=length || 
                                    loco+blue >=length ||
                                    loco+green >= length || loco+orange >=length 
                                    ||loco+red >=length || loco+white >=length 
                                    || loco+yellow >=length
                                    || loco+purple >=length;
                                }
                                else if(playerInfo.get(color)+playerInfo.get(
                                    "loco") < length){
                                    possible = false;
                                }
                                if(possible){
                                    claimRoute = true; 
                                    claimCards.setVisible(true);
                                    cancel.setVisible(true);
                                }
                                else{
                                    JOptionPane.showMessageDialog(null,
                                        "You are unable to claim this route"+
                                        " with your current hand");
                                }
                            }
                        }
                        else if(deck.rangeContains(x,y)){
                            if(!doubleClick.isRunning()){
                                if(twoClicks && display == null){
                                    auto = deck.getDisplayCard(x,y);
                                    if(auto != null){
                                        if(auto.getType().equals("loco") &&
                                        numDraws > 0){
                                            auto = null;
                                        }
                                        else{
                                            deck.removeFromDisplay(auto);
                                            current.addToHand(auto);
                                            if(auto.getType().equals("loco")){
                                                numDraws += 2;
                                            }
                                            else{
                                                if(deck.getDisplay().size() < 5){
                                                    int normCount = 0;
                                                    for(TrainCard dis: deck.getDisplay()){
                                                        if(!dis.getType().equals("loco")){
                                                            normCount++;
                                                        }
                                                    }
                                                    if(normCount > 0){
                                                        numDraws++;
                                                    }
                                                    else{
                                                        numDraws += 2;
                                                    }
                                                }
                                                else{
                                                    numDraws++;
                                                }  
                                            }
                                        }
                                        drawTrain = true;
                                        if(numDraws > 1){
                                            endTurn();
                                        }
                                    }
                                    auto = null;
                                }
                            }
                        }
                        else if(up.contains(x,y)){
                            up.press();
                            current.destForward();
                        }
                        else if(down.contains(x,y)){
                            down.press();
                            current.destBack();
                        }
                    }
                    else{
                        ArrayList<DestinationCard> toHand = 
                            new ArrayList<DestinationCard>();
                        for(DestinationCard dest : fromDeck){
                            if(dest.contains(x,y)){
                                if(!dest.isSelected()){
                                    dest.select();
                                }
                                else{
                                    dest.deselect();
                                }
                            }
                            if(dest.isSelected()){
                                toHand.add(dest);
                            }
                            else{
                                if(toHand.contains(dest))
                                    toHand.remove(dest);
                            }
                        }
                        if(finish.contains(x,y)){
                            finish.press();
                            if(toHand.size() > (firstDraw ? 1:0)){
                                if(firstDraw){
                                    destDeck.removeInit();
                                }
                                else{
                                    destDeck.addToBottom();
                                }
                                for(DestinationCard dest : toHand){
                                    current.addToHand(dest);
                                }
                                fromDeck = null;
                                if(firstDraw){
                                    firstDraw = false;
                                }
                                else{
                                    endTurn();
                                }
                            }
                            else{
                                JOptionPane.showMessageDialog(null,
                                    "You do not have enough"+
                                    " destination tickets selected, at least "+
                                    (firstDraw ? 2:1)
                                    +" must be chosen");
                            }
                        }
                    }
                }
                else{
                    if(current.handContains(x-8,y-30)){
                        if(!doubleClick.isRunning()){
                            if(twoClicks && hand == null){
                                cardGotten = current.getCard(x,y);
                                if(cardGotten != null){
                                    if(claimCards.getSelected().size() < 6){
                                        current.removeCards(
                                            cardGotten.getType(),1);
                                        claimCards.addCard(cardGotten);
                                    }
                                }
                                cardGotten = null;
                            }
                        }
                    }
                    else if(claimCards.regionContains(x-8,y-30)){
                        if(!doubleClick.isRunning()){
                            if(twoClicks && claimsSelect == null){
                                cardGotten = claimCards.getCard(x,y);
                                if(cardGotten != null){
                                    claimCards.removeCard(cardGotten);
                                    current.addToHand(cardGotten);
                                }
                                cardGotten = null;
                            }
                        }
                    }
                    else if(finish.contains(x,y)){
                        finish.press();
                        ArrayList<TrainCard> cardForClaim = 
                            claimCards.getSelected();
                        int length = claim.getLength();
                        int tokenCost = claim.getMountains() + length;
                        boolean isFerry = claim.hasFerry();
                        String color = claim.getColor();
                        int cardsOfPathType = 0, typeLoco = 0, 
                        cardOfOtherType = 0;
                        String greyType = "";
                        if(color.equals("grey")){
                            for(TrainCard grey:cardForClaim){
                                if(!grey.getType().equals("loco")){
                                    greyType = grey.getType();
                                    break;
                                }
                            }
                        }
                        for(TrainCard check : cardForClaim){
                            String type = check.getType();
                            if(type.equals(color) || type.equals(greyType)){
                                cardsOfPathType++;
                            }
                            else if(type.equals("loco")){
                                typeLoco++;
                            }
                            else{
                                cardOfOtherType++;
                            }
                        }

                        if(cardOfOtherType > 0){
                            JOptionPane.showMessageDialog(null,
                                "You have selected too many different"+
                                " types of cards\n please select only one card type"+
                                " with any number of locmotive types");
                        }
                        else if(typeLoco == 0 && isFerry){
                            JOptionPane.showMessageDialog(null,
                                "You require a locomotive card"+
                                " to claim this route please add a "+
                                "locomotive card to your card selection");
                        }
                        else if(cardsOfPathType+typeLoco != length){
                            JOptionPane.showMessageDialog(null,
                                "You have not selected"+
                                " the right amount of cards"+
                                " to claim this route\nPlease choose the "+
                                "right amount of cards");
                        }
                        else{
                            for(TrainCard route : cardForClaim){
                                deck.addToDiscard(route);
                            }
                            claimCards.setVisible(false);
                            cancel.setVisible(false);
                            claimCards.eraseSelected();
                            claim.capture(current);
                            current.claim(claim);
                            if(!isFullGame && claimSister != null){
                                claimSister.capture(current);
                                current.claim(claimSister);
                            }
                            claimRoute = false;
                            current.tokenCount(current.getTokens()-tokenCost);
                            current.setScore(current.getScore()+
                                scoreTally.get(length)+2*claim.getMountains());
                            endTurn();
                        }
                    }
                    else if(cancel.contains(x,y)){
                        cancel.press();
                        ArrayList<TrainCard> backToHand = 
                            claimCards.getSelected();
                        if(backToHand.size() != 0 && backToHand != null){
                            for(TrainCard back:backToHand){
                                current.addToHand(back);
                            }
                        }
                        claimCards.eraseSelected();
                        cancel.setVisible(false);
                        claimCards.setVisible(false);
                        claimRoute = false;
                    }
                    else if(up.contains(x,y)){
                        up.press();
                        current.destForward();
                    }
                    else if(down.contains(x,y)){
                        down.press();
                        current.destBack();
                    }
                }
            }
        }
        e.consume();
    }

    /**
     * method for an event of releasing the mouse button
     * performing various actions based on the parameter
     * 
     * @param e mouse event that contains information 
     * about the current mouse action
     */
    public void mouseReleased(MouseEvent e){
        double x = e.getPoint().getX();
        double y = e.getPoint().getY();
        finish.unpress();
        up.unpress(); 
        down.unpress();
        cancel.unpress();
        if(display != null){
            if(current.handContains(display.getCurrentX()-8,
                display.getCurrentY()-30)){
                if(display.getType().equals("loco") && numDraws > 0){
                    TrainCard buffer = display;
                    deck.moveBack(buffer);
                }
                else{
                    deck.removeFromDisplay(display);
                    current.addToHand(display);
                    if(display.getType().equals("loco")){
                        numDraws += 2;
                    }
                    else{
                        if(deck.getDisplay().size() < 5){
                            int normCount = 0;
                            for(TrainCard dis: deck.getDisplay()){
                                if(!dis.getType().equals("loco")){
                                    normCount++;
                                }
                            }
                            if(normCount > 0){
                                numDraws++;
                            }
                            else{
                                numDraws += 2;
                            }
                        }
                        else{
                            numDraws++;
                        }  
                    }
                }
                if(numDraws > 1){
                    endTurn();
                }
            }
            else{
                TrainCard buffer = display;
                deck.moveBack(buffer);
            }
            display = null;
        }
        else if(claimsSelect != null){
            if(current.handContains(claimsSelect.getCurrentX(),
                claimsSelect.getCurrentY())){
                claimCards.removeCard(claimsSelect);
                current.addToHand(claimsSelect);
            }
            else{
                TrainCard buffer = claimsSelect;
                claimCards.moveBack(buffer);
            }
            claimsSelect = null;
        }
        else if(hand != null){
            if(claimRoute){
                if(claimCards.regionContains(hand.getCurrentX(),
                    hand.getCurrentY())){
                    if(claimCards.getSelected().size() < 6){
                        current.removeCards(hand.getType(),1);
                        claimCards.addCard(hand);
                    }
                    else{
                        TrainCard buffer = hand;
                        current.rePosTrainCard(buffer);
                    }
                }
                else{
                    TrainCard buffer = hand;
                    current.rePosTrainCard(buffer);
                }
            }
            else{
                TrainCard buffer = hand;
                current.rePosTrainCard(buffer);
            }
            hand = null;
        }
    }

    /**
     * method for an event of dragging the mouse button
     * performing various actions based on the parameter
     * 
     * @param e mouse event that contains information 
     * about the current mouse action
     */
    public void mouseDragged(MouseEvent e){
        double x = e.getPoint().getX();
        double y = e.getPoint().getY();
        if(!init && !claimRoute && fromDeck == null && !endGame){
            if(deck.rangeContains(x,y) && display == null && hand == null &&
            cardGotten == null && auto == null){
                display = deck.getDisplayCard(x,y);
            }
            else if(current.handContains(x-8,y-30) && hand == null &&
            display == null && cardGotten == null && auto == null){
                hand = current.getCard(x,y);
            }
        }
        else if(claimRoute){
            if(current.handContains(x-8,y-30) && hand == null &&
            claimsSelect == null && cardGotten == null){
                hand = current.getCard(x,y);
            }
            else if(claimCards.regionContains(x-8,y-30) && claimsSelect == null 
            && hand == null && cardGotten == null){
                claimsSelect = claimCards.getCard(x,y);
            }
        }
        if(display != null){
            display.dragTo(x,y);
        }
        else if(claimsSelect !=  null){
            claimsSelect.dragTo(x,y);
        }
        else if(hand != null){
            hand.dragTo(x,y);
        }
        e.consume();
    }

    /**
     * method for an event of moving the mouse button
     * performing various actions based on the parameter
     * 
     * @param e mouse event that contains information 
     * about the current mouse action
     */
    public void mouseMoved(MouseEvent e){
        double x = e.getPoint().getX();
        double y = e.getPoint().getY();
        board.pathContains(x,y);
        e.consume();
    }

    /**
     * method for starting the game of ticket to ride
     * 
     * @args string representing command line arguments
     */
    public static void main (String[] args){
        TicketToRide ttr = new TicketToRide();
        ttr.startGame();
    }

    /**
     * Class for handling TrainCards passed around
     * during a route claiming scenario in ticket to 
     * ride and for creating a display for easy visual
     * understanding for a player
     */
    private class ClaimCards extends JComponent{
        private String display;
        private int startX,startY, grabbedPos;
        private ArrayList<TrainCard> selected;
        public JLayeredPane storedCards;
        private double regionWidth = (deck.getCardWidth()+10)*6;
        /**
         * Constrctor for the claimCards object
         * 
         * @param x x-coordinate to place the object
         * @param y y-coordinate to place the object
         */
        public ClaimCards(int x, int y){
            startX = x+((int)(width/2-regionWidth/2));
            startY = y;
            display = "Please select the cards used for claiming this route";
            selected =  new ArrayList<TrainCard>(0);
            grabbedPos = 0;
            storedCards = new JLayeredPane();
            storedCards.setBounds(0,0,width,height);
        }

        /**
         * method for determining if a point is in the range of this object
         * card insertion region
         * 
         * @param x x position of a point to be tested
         * @param y y position of a point to be tested
         * 
         * @return returns a boolean determining if a 
         * point was contained in the region
         */
        public boolean regionContains(double x, double y){
            if(x > startX && y > startY){
                if(x < startX+regionWidth && y < startY+deck.getCardHeight()+10){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                return false;
            }
        }

        /**
         * clears this objects contained list of cards and clears 
         * its JLayeredPane used in storing them graphically
         */
        public void eraseSelected(){
            selected.clear();
            selected.trimToSize();
            storedCards.removeAll();
        }

        /**
         * method for getting a trainCard at a the given coordinates
         * in the claimCards object
         * 
         * @param x x of the point being tested
         * @param y y of the point being tested
         * 
         * @return returns the card at the given point
         * if there is one there otherwise null
         */
        public TrainCard getCard(double x, double y){
            for(int get = 0;get < selected.size();get++){
                TrainCard returned = selected.get(get);
                if(returned.contains(x,y)){
                    grabbedPos = get;
                    return returned;
                }
            }
            return null;
        }

        /**
         * method for adding a card to this objects graphics container
         * and to its arraylist to hold it until an event fires in the game
         * 
         * @param addCard card to be added to this ClaimCards object
         */
        public void addCard(TrainCard addCard){
            storedCards.add(addCard, new Integer(0));
            addCard.translateTo(startX+(
                    deck.getCardWidth()+10)*selected.size(),startY+35);
            selected.add(addCard);
            storedCards.validate();
        }

        /**
         * method to remove a card from this claimCards object
         * 
         * @param gone card to be removed from this objects containment
         */
        public void removeCard(TrainCard gone){
            int shift = 0;
            for(int delete = 0;delete < selected.size();delete++){
                TrainCard get = selected.get(delete);
                if(get.getCurrentY() == gone.getCurrentY() &&
                get.getCurrentX() == gone.getCurrentX()){
                    storedCards.remove(storedCards.getIndexOf(gone));
                    selected.remove(delete);
                    shift = delete;
                    break;
                }
            }
            for(;shift < selected.size();shift++){
                TrainCard left = selected.get(shift);
                left.translateTo(left.getCurrentX()-left.getwidth(),startY+35);
            }
            selected.trimToSize();
        }

        /**
         * method to move a card back to its position after it had 
         * been moved
         * 
         * @param moveBack card object to move back into its proper
         * position
         */
        public void moveBack(TrainCard moveBack){
            for(int move = 0; move < selected.size();move++){
                TrainCard back = selected.get(move);
                if(back.getCurrentY() == moveBack.getCurrentY() &&
                back.getCurrentX() == moveBack.getCurrentX()){
                    moveBack.translateTo(startX+(
                            deck.getCardWidth()+10)*move,startY+35);
                    break;
                }
            }
        }

        /**
         * method for retrieving this objects containment
         * of the card objects
         * 
         * @return returns the cards that this claimCards object
         * is holding 
         */
        public ArrayList<TrainCard> getSelected(){
            return selected;
        }

        /**
         * Paint method for drawing elements related to this objects
         * graphical representation
         * 
         * @param g graphics object to be drawn to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            Composite original = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,.5F));
            g2d.setColor(Color.black);
            g2d.fillRect(0,0,width,height);
            g2d.setComposite(original);
            Font orig = g2d.getFont();
            g2d.setFont(new Font(orig.getName(), orig.getStyle(), 20));
            FontMetrics fm = g2d.getFontMetrics();
            int sHeight = fm.getAscent();
            int sWidth = fm.stringWidth(display);
            g2d.setColor(Color.white);
            g2d.drawString(display,width/2-sWidth/2,startY-2*sHeight);
            g2d.setFont(orig);
            g2d.drawRect((int)startX-15,(int)startY,(int)regionWidth+10,
                (int)deck.getCardHeight()+10);
            g2d.dispose();
        }
    }

    /**
     * class for representing a button for interaction that can take 
     * on two forms the done button and the cancel button
     */
    private class FinishedButton extends JComponent{
        private double startX, startY, width, height, sW, sH;
        private BufferedImage button, pressed, cancel, cancelPress, display;
        private AffineTransform transform;
        private boolean select, notNormal;

        /**
         * constructor for the FinishButton object
         * 
         * @param x x position to place this object
         * @param y y position to place this object
         * @param w width of this object
         * @oaram h height of this object
         * @param notNormal boolean for determining if the 
         * button is going to be a normal finish button 
         * or a cancel button
         */
        public FinishedButton(double x, double y, double w, double h, 
        boolean notNormal){
            startX = x;
            startY = y;
            width = w;
            height = h;
            sW = w/219;
            sH = h/123;
            transform = new AffineTransform(sW,0,0,sH,startX,startY);
            this.notNormal = notNormal;
            try{
                File buttonNorm = new File("image_data/button.png");
                File buttonPressed = new File("image_data/pressed.png");
                File cancelNorm = new File("image_data/cancelButton.png");
                File cancelPressed = 
                    new File("image_data/cancelButtonPress.png");
                button = ImageIO.read(buttonNorm);
                pressed = ImageIO.read(buttonPressed);
                cancel = ImageIO.read(cancelNorm);
                cancelPress = ImageIO.read(cancelPressed);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
            display = (notNormal ? cancel:button);
            select = false;
        }

        /**
         * method for determining if a point is contained in this object
         * 
         * @param x x-coordinate of point 
         * @param y y-coordinate of point
         * 
         * @return returns a boolean value representing 
         * if the given point was contained in this object
         */
        public boolean contains(double x, double y){
            if(x-8 > startX+(34*sW) && y-30 > startY+(40*sH)){
                if(x-8 < startX+(34*sW)+152*sW && y-30 < startY+(40*sH)+44*sH){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                return false;
            }
        }

        /**
         * method for creating a graphical pressing affect
         */
        public void press(){
            display = (notNormal ? cancelPress:pressed);
            select = true;
            repaint();
        }

        /**
         * method for creating a graphical unpressing affect
         * essentially returing the buttons state to normal
         */
        public void unpress(){
            display = (notNormal ? cancel:button);
            select = false;
            repaint();
        }

        /**
         * method for determining if a button has been pressed
         * 
         * @return returns a boolean that represents if this button
         * had been pressed
         */
        public boolean isPressed(){
            return select;
        }

        /**
         * method for drawing this button object to a screen
         * 
         * @param g graphics object of which this button will be painted to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(display, transform,this);
            g2d.dispose();
        }
    }

    /**
     * Class to show arrow button of up or down configuration
     * on the screen to be used with moving the destination cards
     * around 
     */
    private class ArrowButton extends JComponent{
        public double sX, sY, diameter, scale, width;
        private AffineTransform up_down;
        private BufferedImage button, pressed;
        private boolean isPressed;
        /**
         * constructor for ArrowButton object
         * 
         * @param x x position to place this object
         * @param y y position to place this object
         * @param w width of this object
         * @param down boolean for determing if the object 
         * should be drawn in an up or down state
         */
        public ArrowButton(double x, double y, double w, boolean down){
            sX = x;
            sY = y;
            width = w;
            scale = w/77;
            diameter = 58*scale;
            isPressed = false;
            try{
                File norm = new File("image_data/arrowUpButton.png");
                File press = new File("image_data/arrowUpButtonPress.png");
                pressed = ImageIO.read(press);
                button = ImageIO.read(norm);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }

            if(down){
                up_down = new AffineTransform(scale,0,0,scale,x,y);
                up_down.rotate(Math.PI,width,width);
            }
            else{
                up_down = new AffineTransform(scale,0,0,scale,x,y);
            }
        }

        /**
         * method for determining if a point is contained in this object
         * 
         * @param x x-coordinate of point 
         * @param y y-coordinate of point
         * 
         * @return returns a boolean value representing 
         * if the given point was contained in this object
         */
        public boolean contains(double x, double y){
            double xDis = Math.abs(sX+width/2-x+8);
            double yDis = Math.abs(sY+width/2-y+30);
            double distance = Math.floor(Math.hypot(xDis, yDis));
            if(distance < diameter/2){
                return true;
            }
            else{
                return false;
            }
        }

        /**
         * method for determining if a button has been pressed
         * 
         * @return returns a boolean that represents if this button
         * had been pressed
         */
        public boolean isPressed(){
            return isPressed;
        }

        /**
         * method for creating a graphical pressing affect
         */
        public void press(){
            isPressed = true;
            repaint();
        }

        /**
         * method for creating a graphical unpressing affect
         * essentially returing the buttons state to normal
         */
        public void unpress(){
            isPressed = false;
            repaint();
        }

        /**
         * method for drawing this button object to a screen
         * 
         * @param g graphics object of which this button will be painted to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            if(isPressed){
                g2d.drawImage(pressed,up_down,this);
            }
            else{
                g2d.drawImage(button,up_down,this);
            }
            g2d.dispose();
        }
    }

    /**
     * class for displaying the final score Screen 
     */
    private class ScoreScreen extends JComponent{
        private boolean draw;
        /**
         * constructor for a scoreScreen
         */
        public ScoreScreen(){
            draw = false;
        }

        /**
         * method for determing if the score screen should de drawn
         */
        public void draw(){
            draw = true;
            repaint();
        }

        /**
         * method for painting the score screen
         * 
         * @param g graphics object to draw to
         */
        public void paint(Graphics g){
            if(draw){
                Graphics2D g2d = (Graphics2D) g;
                Composite original = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,.5F));
                g2d.setColor(Color.black);
                g2d.fillRect(0,0,width,height);
                g2d.setComposite(original);
                Font orig = g2d.getFont();
                g2d.setColor(Color.white);
                FontMetrics fm = g2d.getFontMetrics();
                for(int print = 0;print < playerlist.size();print++){
                    Player printing  = playerlist.get(print);
                    g2d.setFont(new Font(orig.getName(), orig.getStyle(), 20));
                    String name = printing.getName();
                    String scoreString = printing.getScore()+"";
                    String destComp = destComplete.get(print)+"";
                    String destScore = destPoints.get(print)+"";
                    int nameWidth = fm.stringWidth(name);
                    int scoreWidth = fm.stringWidth(scoreString);
                    int destCompWidth = fm.stringWidth(destComp);
                    int sHeight = fm.getAscent();
                    g2d.drawString(name,(int)(width/4),
                        (int)(height/4+(30*print))+sHeight);
                    g2d.drawString(scoreString,(int)(width/4)+100,
                        (int)(height/4+(30*print))+sHeight);
                    g2d.drawString(destComp,(int)(width/4)+200,
                        (int)(height/4+(30*print))+sHeight);
                    g2d.drawString(destScore,(int)(width/4)+400,
                        (int)(height/4+(30*print))+sHeight);
                }
                g2d.drawString("Name",(int)(width/4),(int)(height/4)-10);
                g2d.drawString("Score",(int)(width/4)+100,
                    (int)(height/4)-10);
                g2d.drawString("Dest Complete",(int)(width/4)+200,
                    (int)(height/4)-10);
                g2d.drawString("Dest points",(int)(width/4)+400,
                    (int)(height/4)-10);
                g2d.drawString("The winner is:",(int)(width/4),
                    (int)(height/4+(30*playerlist.size()))+100);
                g2d.drawString(winner.getName(),(int)(width/4),
                    (int)(height/4+(30*playerlist.size()))+150);
                g2d.setFont(orig);
                g2d.dispose();
            }
        }
    }

    /**
     * Class for displaying the scores of the players throughout the game
     */
    private class Scores extends JComponent{
        private double startX, startY, width ,height;
        /**
         * constructor for the scores object
         * 
         * @param x x position to place this object
         * @param y y position to place this object
         */
        public Scores(double x, double y){
            startX = x;
            startY = y;
        }

        /**
         * method for painting the scores
         * 
         * @param g graphics object to draw to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setBackground(Color.black);
            g2d.clearRect((int)startX,(int)startY,
                (int)(115*board.getXScale())+20,(int)(125*board.getYScale()));
            Font orig = g2d.getFont();
            g2d.setColor(Color.white);
            FontMetrics fm = g2d.getFontMetrics();
            for(int print = 0;print < playerlist.size();print++){
                Player printing  = playerlist.get(print);
                if(printing.equals(current)){
                    g2d.drawRect((int)startX, (int)(startY+(15*print)),
                        (int)(115*board.getXScale()),13);
                }
                g2d.setFont(new Font(orig.getName(), orig.getStyle(), 12));
                String display = (printing.getName()+"  "+printing.getScore());
                int sWidth = fm.stringWidth(display);
                int sHeight = fm.getAscent();
                g2d.drawString(display,(int)startX+2, 
                    (int)(startY+10+(15*print)));
            }
            g2d.setFont(orig);
            g2d.dispose();
        }
    }
}
