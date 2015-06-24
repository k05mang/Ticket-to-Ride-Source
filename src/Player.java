import java.util.Hashtable;
import java.util.ArrayList;
import javax.swing.JLayeredPane;
import javax.swing.JComponent;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.util.Enumeration;
import java.awt.image.LookupOp;
/**
 * Player object, stores information on the player as well
 * as handling the displaying of the cards in the hand
 * 
 * @author Kevin Mango, Mat Banville, Ryan Clancy
 * Marissa Bianchi
 * @version 5-4-2013
 */
public class Player
{
    protected String color, name;
    private Hashtable<String, Count> hashhand;
    private ArrayList<DestinationCard> destHand;
    private ArrayList<TrainCard> cardHand;
    private ArrayList<String> orderAdded;
    private int score, trains, numCardType, destCount;
    public JLayeredPane hand;
    private double startX, startY, width, height, cardW, cardH, handPos[];
    private ArrayList<Path> pathes;
    private TokenImg tokens;
    /**
     * Contructor for the player object
     * 
     * @param str String representing the players name
     * @param x x-coordinates for the players hand
     * @param y y-coordinates for the players hand
     * @param w width for the players hand
     * @param h height for the players hand
     * @param cardW card width of the cards being passed in
     * @param cardH card height of the cards being passed in
     * @param color String representing the players color
     * @param renderX integer representing the width of the area
     * this player object is being displayed to
     * @param renderY integer representing the height of the area
     * this player object is being displayed to
     */
    public Player(String str, double x, double y, double w, double h, 
    double cardW, double cardH,String color, int renderX, int renderY)
    {
        name = str;
        this.color = color;
        score = 0;
        trains = 45;
        startX = x;
        startY = y;
        width = w;
        height = h+10;
        this.cardW = cardW;
        this.cardH = cardH;
        tokens = new TokenImg(x+width,y, 105, color,trains);
        tokens.setBounds(0,0,renderX,renderY);
        pathes = new ArrayList<Path>();
        numCardType = 0;
        destCount = 0;
        hashhand = new Hashtable<String, Count>();
        orderAdded = new ArrayList<String>();
        ArrayList<Count> counters = new ArrayList<Count>();
        hashhand.put("black",new Count(x,y,50,x));
        hashhand.put("blue",new Count(x,y,50,x));
        hashhand.put("green",new Count(x,y,50,x));
        hashhand.put("loco",new Count(x,y,50,x));
        hashhand.put("orange",new Count(x,y,50,x));
        hashhand.put("purple",new Count(x,y,50,x));
        hashhand.put("red",new Count(x,y,50,x));
        hashhand.put("white",new Count(x,y,50,x));
        hashhand.put("yellow",new Count(x,y,50,x));
        hand = new JLayeredPane();
        Enumeration<Count> bounds = hashhand.elements();
        //modify the count object
        while(bounds.hasMoreElements()){
            Count next = bounds.nextElement();
            next.setBounds(0,0,renderX,renderY);
            next.setVisible(false);
            hand.add(next,new Integer(10));
        }
        destHand = new ArrayList<DestinationCard>();
        cardHand = new ArrayList<TrainCard>();
        hand.add(tokens,new Integer(0));
        hand.setBounds(0,0,renderX,renderY);
    }

    /**
     * Adds the given card to the players hand
     * 
     * @param card Card to be added to the players hand
     */
    public void addToHand(Card card){
        if(card instanceof TrainCard){
            final TrainCard drawn = (TrainCard)card;
            Count change = hashhand.get(drawn.getType());
            int numCards = change.count;
            if(numCards == 0){
                hand.add(drawn, new Integer(9-orderAdded.size()));
                //shift the card over that are already
                //in the hand
                for(TrainCard move : cardHand){
                    Count shift = hashhand.get(move.getType());
                    double cardx = move.getCurrentX();
                    move.translateTo(cardx-cardW/2.5,startY+5);
                    shift.setCardX(cardx-cardW/2.5);
                    shift.setPos(cardx-cardW/2.5+2*cardW,startY+5+cardH);
                }
                numCardType++;
                drawn.translateTo(startX+width/2+(numCardType-2)*(cardW/2),
                    startY+5);
                orderAdded.add(drawn.getType());
                hashhand.get(drawn.getType()).setCardX(
                    startX+width/2+(numCardType-2)*(cardW/2));
                change.setPos(startX+width/2+(numCardType-2)*(cardW/2)+
                    2*cardW,startY+5+cardH);
                change.setVisible(true);
            }
            else{
                hand.add(drawn, new Integer(9-orderAdded.indexOf(
                drawn.getType())));
                drawn.translateTo(change.cardX,startY+5);
                Thread animate = new Thread(new Runnable(){
                            public void run(){
                                try{
                                    Thread.sleep(500);
                                    drawn.hideCard();
                                }
                                catch(InterruptedException e){
                                    System.err.println(e.getMessage());
                                }
                            }
                        }
                    );
                animate.start();
                animate = null;
            }
            change.increment();
            cardHand.add(drawn);
        }
        else if(card instanceof DestinationCard){
            DestinationCard drawn = (DestinationCard)card;
            hand.add(drawn, new Integer(0));
            hand.moveToFront(drawn);
            drawn.translateTo(startX-2*cardW-10, startY+height/10);
            destHand.add(drawn);
            drawn.deselect();
        }
        hand.validate();
    }

    /**
     * gets the card at x, y, on the game area
     * for the player to interact with
     * 
     * @param x x-coordinates to check for containment
     * @param y y-coordinates to check for containment
     * 
     * @return returns the TrainCard at the specified
     * coordinates
     */
    public TrainCard getCard(double x, double y){
        for(TrainCard move : cardHand){
            if(orderAdded.indexOf(move.getType()) == 0){
                if(move.contains(x, y) && move.isShown()){
                    return move;
                }
            }
            else{
                if(move.rightHalfContains(x, y) && move.isShown()){
                    return move;
                }
            }
        }
        return null;
    }

    /**
     *removes the given type and amount of cards from the players hand
     *
     *@param type string representing the type of card to be removed from
     *the players hand
     *
     *@param amount number representing the amount of cards of the given type
     *to be removed from the hand
     *
     *@return returns an arrayList of cards that were discarded
     *from the hand, this ist is to be added to the discard pile
     * of the game deck
     */
    public ArrayList<TrainCard> removeCards(String type, int amount){
        ArrayList<TrainCard> discarded = new ArrayList<TrainCard>(amount);
        for(int rem = 0;rem < amount; rem++){
            for(int m = 0;m < cardHand.size(); m++){
                TrainCard remove = cardHand.get(m);
                TrainCard hidden = null;
                if(remove.getType().equals(type) && remove.isShown()){
                    Count change = hashhand.get(remove.getType());
                    change.decrement();
                    if(change.count == 0){
                      //adjust the positions of all the cards in the hand
                      for(int g = 0;g < orderAdded.size();g++){
                        if(orderAdded.get(g).equals(remove.getType())){
                          Count update;
                          //shift the cards in the left half of the hand
                          //to the right
                          for(int left = g-1;left >= 0;left--){
                            update = hashhand.get(orderAdded.get(left));
                            for(TrainCard mL : cardHand){
                              if(mL.getType().equals(orderAdded.get(left))){
                                mL.translateTo(update.cardX+cardW/2,startY+5);                               }
                            }
                            update.setCardX(update.cardX+cardW/2);
                            update.setPos(update.xGet()+cardW/2,startY+5+cardH);
                          }
                          //shift the cards in the right half of the hand
                          //to the left
                          for(int right = g+1;right < orderAdded.size();right++){
                            update = hashhand.get(orderAdded.get(right));
                            for(TrainCard mR : cardHand){
                              if(mR.getType().equals(orderAdded.get(right))){
                                mR.translateTo(update.cardX-cardW/2,startY+5);
                                hand.putLayer(mR, new Integer(9-right));
                              }
                            }
                            update.setCardX(update.cardX-cardW/2);
                            update.setPos(update.xGet()-cardW/2,startY+5+cardH);
                          }
                          orderAdded.remove(g);
                          break;
                        }
                      }
                      numCardType--;
                      change.setVisible(false);
                    }
                    //find the card that was behind the card removed
                    for(int hid = 0;hid < cardHand.size(); hid++){
                        TrainCard hiding = cardHand.get(hid);
                        if(hiding.getType().equals(type) && hiding.isHidden()){
                            hidden = hiding;
                            break;
                        }
                    }
                    discarded.add(cardHand.remove(m));
                    hand.remove(hand.getIndexOf(remove));
                    if(hidden != null)
                        hidden.showCard();
                    break;
                }
            }
        }
        orderAdded.trimToSize();
        return discarded;
    }

    /**
     * hides anything pertaining to the player that was displayed to
     * the screen
     */
    public void hide(){
        hand.setVisible(false);
    }

    /**
     * shows anything pertaining to the player that was displayed to
     * the screen
     */
    public void show(){
        hand.setVisible(true);
    }

    /**
     *used in getting the information of the players hand which can be used
     *in determing ability to claim paths, or automated path claiming
     *where it removes all the cards needed
     *
     *@return returns a hash table of the type <string, integer>
     *where the string is the card type and the the integer
     *is the amount of that card type in the players hand
     */
    public Hashtable<String, Integer> getHandContents(){
        Hashtable<String, Integer> retrieve = new Hashtable<String, Integer>();
        retrieve.put("black",hashhand.get("black").count);
        retrieve.put("blue",hashhand.get("blue").count);
        retrieve.put("green",hashhand.get("green").count);
        retrieve.put("loco",hashhand.get("loco").count);
        retrieve.put("orange",hashhand.get("orange").count);
        retrieve.put("purple",hashhand.get("purple").count);
        retrieve.put("red",hashhand.get("red").count);
        retrieve.put("white",hashhand.get("white").count);
        retrieve.put("yellow",hashhand.get("yellow").count);
        return retrieve;
    }

    /**
     * @param x x-coordinate to check
     * @param y y-coordinate to check
     * 
     * @return boolean value telling if the point 
     * was contained in the hand
     */
    public boolean handContains(double x, double y){
        if(x > startX-cardW && y > startY){
            if(x < startX+width && y < startY+height){
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
     * moves the displayed destination card group
     * forward on the display
     */
    public void destForward(){
        destCount++;
        if(destCount > destHand.size()-1)
            destCount = 0;
        hand.moveToFront(destHand.get(destCount));
    }

    /**
     * moves the displayed destination card group
     * backward on the display
     */
    public void destBack(){
        destCount--;
        if(destCount == -1)
            destCount = destHand.size()-1;
        hand.moveToFront(destHand.get(destCount));
    }

    /**
     * returns the number of mountains the player has passed through
     * while collecting paths in the game, this method is used for
     * breaking ties of players going for the asian bonus
     * 
     * @return the number of mountains the player has currently
     * gone through with their path claiming
     */
    public int getMountains(){
        int mntSum = 0;
        for(Path mount:pathes){
            mntSum += mount.getMountains();
        }
        return mntSum;
    }

    /**
     * Accessor method for getting the players color
     * 
     * @return string representing the players color
     */
    public String getColor(){
        return color;
    }

    /**
     * addes the given path to the players list of acquired paths
     * 
     * @param claimed path to be added to the claimed list
     */
    public void claim(Path claimed){
        pathes.add(claimed);
    }

    /**
     * Accessor method for retrieving the paths currently owned
     * by this player
     * 
     * @return returns an arraylist of path objects that the player owns
     */
    public ArrayList<Path> getPathes(){
        return pathes;
    }

    /**
     * accessor method for getting the number of train
     * tokens the player has
     * 
     * @return returns an integer representing the number of train tokens 
     * this player has
     */
    public int getTokens(){
        return trains;
    }

    /**
     * mutator method for changing the token count of the 
     * player
     * 
     * @param the number to set the players token count to
     */
    public void tokenCount(int token){
        tokens.changeCount(token);
        trains = token;
    }

    /**
     * accessor method for getting the players name
     * 
     * @return string representing the players name
     */
    public String getName(){
        return name;
    }

    /**
     * method for setting the players score 
     * 
     * @param newScore integer representing the score to 
     * set this players score to
     */
    public void setScore(int newScore){
        score = newScore;
    }

    /**
     * method for getting the players current score
     * 
     * @return returns an int representing the the players
     * current score
     */
    public int getScore(){
        return score;
    }

    /**
     * method to test whether two players are equals
     * 
     * @param equals player to be tested against for equality
     * 
     * @return a boolean defining whether the given player
     * is equivalent to this player
     */
    public boolean equals(Player equals){
        if(equals.getColor().equals(color)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * method for repositioning a train card back in the players 
     * hand
     * 
     * @param moveBack card to be moved back into the players hand
     */
    public void rePosTrainCard(TrainCard moveBack){
        Count back = hashhand.get(moveBack.getType());
        moveBack.translateTo(back.cardX,startY+5);
    }

    /**
     * method for getting the destination cards of a players hand
     * 
     * @return returns and arrayList of the destination cards
     * this player currently owns
     */
    public ArrayList<DestinationCard> getDestHand(){
        return destHand;
    }

    /**
     * Class for displaying a visible counter
     */
    private class Count extends JComponent{
        public double sX, sY, width, cardX, scale,sW;
        public Integer count;
        private AffineTransform digit1, digit2;
        private BufferedImage digitOne, digitTwo, digits[];
        /**
         * constructor for a count object
         * 
         * @param x x-coordinate to place the count object
         * @param y y-coordinate to place the count object
         * @param w width of the count object
         * @param cardX value for storing the x coordinate of the
         * card object that this count object is associated with
         * this allows for easy placement of cards that are
         * already in the players hand 
         */
        public Count(double x, double y, double w, double cardX){
            sX = x;
            sY = y;
            width = w;
            this.cardX = cardX;
            count = 0;
            digits = new BufferedImage[10];
            //get all the images that represent the numbes to be displayed
            try{
                File digit0 = new File("image_data/numbers/zero.png");
                File digitON = new File("image_data/numbers/one.png");
                File digitT = new File("image_data/numbers/two.png");
                File digit3 = new File("image_data/numbers/three.png");
                File digit4 = new File("image_data/numbers/four.png");
                File digit5 = new File("image_data/numbers/five.png");
                File digit6 = new File("image_data/numbers/six.png");
                File digit7 = new File("image_data/numbers/seven.png");
                File digit8 = new File("image_data/numbers/eight.png");
                File digit9 = new File("image_data/numbers/nine.png");
                digits[0] = ImageIO.read(digit0);
                digits[1] = ImageIO.read(digitON);
                digits[2] = ImageIO.read(digitT);
                digits[3] = ImageIO.read(digit3);
                digits[4] = ImageIO.read(digit4);
                digits[5] = ImageIO.read(digit5);
                digits[6] = ImageIO.read(digit6);
                digits[7] = ImageIO.read(digit7);
                digits[8] = ImageIO.read(digit8);
                digits[9] = ImageIO.read(digit9);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
            digitOne = null;
            digitTwo = null;
            scale = w/50;
            //determines if the count object is being used to display a 
            //card count or as a token image counter
            if(cardX > 0){
                sW = 2*cardW/250;
                digit1 = new AffineTransform(sW,0,0,sW,x-w,y-w);
                digit2 = new AffineTransform(sW,0,0,sW,x-2*w,y-w);
            }
            else{
                digit1 = new AffineTransform(scale,0,0,scale,x+25*scale,y);
                digit2 = new AffineTransform(scale,0,0,scale,x,y);
            }
        }

        /**
         * method for incrementing the display of this count
         * object
         */
        public void increment(){
            count++;
            changeDisplay();
            repaint();
        }

        /**
         * method for decrementing the display of this count
         * object
         */
        public void decrement(){
            count--;
            changeDisplay();
            repaint();
        }

        /**
         * method to change the count parameter directly for 
         * faster change in the number count instead of 
         * calling increment or decrement mulitple times
         * 
         * @param num number to change the display to
         */
        public void changeCount(int num){
            count = num;
            changeDisplay();
            repaint();
        }

        /**
         * method for determing what images should be used to 
         * display the count amount
         */
        public void changeDisplay(){
            if(count < 10){
                digitTwo = null;
                digitOne = digits[count];
            }
            else{
                digitOne = digits[count%10];
                digitTwo  = digits[count/10];
            }
        }

        /**
         * changes the position of the card object to a new position
         * mainly used with a card display instead of the token display
         * 
         * @param x x-coordinate to be moved to
         * @param y y-coordinate to be moved to
         * 
         */
        public void setPos(double x, double y){
            sX = x;
            sY = y;
            digit1.setTransform(sW,0,0,sW,x-.85*width,y-2*width);
            digit2.setTransform(sW,0,0,sW,x-1.15*width,y-2*width);
            repaint();
        }

        /**
         * sets the cardX value, used when count object is changing
         * its position so the card add knows the new position to 
         * move to
         * 
         * @param x x-coordinate to set the cardX stored in this
         * count object to
         */
        public void setCardX(double x){
            cardX = x; 
        }

        /**
         * method for getting the current x-coordinate of this
         * count object
         * 
         * @return returns a double representing this count objects
         * x position
         */
        public double xGet(){
            return sX;
        }

        /**
         * method for getting the current y-coordinate of this
         * count object
         * 
         * @return returns a double representing this count objects
         * y position
         */
        public double yGet(){
            return sY;
        }

        /**
         * Paints this count object to the given
         * graphics object
         * 
         * @param g graphics object to draw to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(digitOne,digit1,this);
            g2d.drawImage(digitTwo,digit2,this);
            g2d.dispose();
        }
    }

    /**
     * class for displaying a visual count of the players
     * tokens as well as the color of their tokens
     * for easier identification of the current player 
     */
    private class TokenImg extends JComponent{
        private BufferedImage token, frame;
        private LookupOp tokenColor;
        private Count digits;
        private double sX, sY, sW;
        private AffineTransform change;

        /**
         * constructor for a TokenImg object
         * 
         * @param x x-coordinate to place the TokenImg object
         * @param y y-coordinate to place the TokenImg object
         * @param w width of the TokenImg object
         * @param color string representing the color
         * of this tokenImgs token
         * @param start integer determining what to start this tokenImgs
         * count at
         */
        public TokenImg(double x, double y, double w, String color, int start){
            tokenColor = Path.changeColor(color);
            sX = x;
            sY = y;
            Image tokenSource = null;
            try{
                File tokenFrame = new File("image_data/tokenFrame.png");
                File tokenCar = new File("image_data/tokenIcon.png");
                tokenSource = ImageIO.read(tokenCar);
                frame = ImageIO.read(tokenFrame);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
            sW = w/105;
            token = new BufferedImage((int)(70*sW),(int)(50*sW),
                BufferedImage.TYPE_4BYTE_ABGR);
            Image newToken = tokenSource.getScaledInstance((int)(70*sW),
                    (int)(50*sW),Image.SCALE_DEFAULT);
            token.getGraphics().drawImage(newToken,0,0,this);
            digits = new Count(x+39*sW, y+69*sW, 17*sW,0);
            digits.changeCount(start);
            change = new AffineTransform(sW,0,0,sW,x,y);
        }
        
        /**
         * method for changing the count of the tokens displayed
         * 
         * @param num number to change the curent count object to for
         * this tokenImg
         */
        public void changeCount(int num){
            digits.changeCount(num);
            repaint();
        }

        /**
         * Paints the tokenImg to the given graphics
         * 
         * @param g graphics object to draw to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(frame,change,this);
            g2d.drawImage(token, tokenColor, (int)(sX+18*sW),(int)(sY+15*sW));
            digits.paint(g);
            g2d.dispose();
        }
    }
}