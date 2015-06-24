import java.util.Stack;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JComponent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.awt.*;
import javax.swing.JLayeredPane;
import java.awt.image.BufferedImage;
/**;
 * Write a description of class Deck here.
 * 
 * @author Kevin Mango, Marissa Bianchi, Mat Banville
 * Ryan Clancy 
 * @version 4-23-2013
 */
public class TrainDeck
{
    // two arraylist 'decks', discard pile, and displayed cards
    private ArrayList<TrainCard> discard, display;
    // train cards still in deck
    private Stack<TrainCard> deck;
    private Random grab;
    private double yPos[], deckY, deckX, deckW, deckH, rangeHeight, cardW,cardH;
    private int displayMovedPos;
    public JLayeredPane container;
    private DeckImage cards;
    private boolean addDisp, getCard;
    /**
     * Constructor for objects of class Deck
     * 
     * @param x deck x position
     * @param y deck y position
     * @param w deck's width
     * @param h decks' height
     * @param renderX
     * @param renderY
     */
    public TrainDeck(double x, double y, double w, double h,
    int renderX, int renderY)
    {
        deckX = x;
        deckY = y;
        deckW = w;
        deckH = h;
        cards = new DeckImage(x,y,w,h);
        ArrayList<TrainCard> init = new ArrayList<TrainCard>();
        File path = new File("image_data/Cards/us");
        for(File img: path.listFiles()){
            Image face = null;
            String end = img.getName().replaceFirst(
            "T2R_traincard_us_","").replaceFirst(".png","");
            try{
                face = ImageIO.read(img);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
            int numCards = (end.equals("loco") ? 14:12);
            for(int card = 0; card < numCards; card++){
                TrainCard create = new TrainCard(face,x+10, y, 
                w-36*cards.getXScale(), h-26*cards.getYScale(), end);
                create.setBounds(0,0,renderX, renderY);
                init.add(create);
                cardW = create.getwidth();
                cardH = create.getheight();
            }
        }
        discard = new ArrayList<TrainCard>();
        display = new ArrayList<TrainCard>(5);
        deck = new Stack<TrainCard>();
        grab = new Random();
        cards.setBounds(0,0,renderX, renderY);
        rangeHeight = deckH + init.get(0).getheight()*5 +50;
        yPos = new double[5];
        displayMovedPos = -1;
        for(int m = 0;m < 5;m++)
            yPos[m] = -1;
        int rand = 0;
        container = new JLayeredPane();
        while(!init.isEmpty()){
            rand = grab.nextInt(init.size());
            TrainCard start = init.get(rand);
            deck.push(start);
            container.add(start,new Integer(1));
            start.hideCard();
            init.remove(rand);
            init.trimToSize();
        }
        container.add(cards, new Integer(0));
        container.setBounds(0,0,renderX, renderY);
        addDisp = true;
        getCard = false;
    }

    /**
     * Draws a train card from the deck
     * 
     * @return the train card drawn
     */
    public TrainCard draw(){ 
        if(!deck.isEmpty()){
            TrainCard drawn = deck.pop();
            container.moveToFront(drawn);
            drawn.showCard();
            drawn.flip();
            //shuffle cards back into the deck
            //if it was empty after the draw
            if(deck.isEmpty())
                shuffle(); 
            cards.checkDeck(deck.size());
            return drawn;
        }
        else 
            return null;
    }

    /**
     * Shuffles deck by taking random cards from discard deck and putting them 
     * in the main deck until the discard deck is empty.
     */
    public void shuffle(){
        int rand = 0;
        while(!discard.isEmpty()){
            rand = grab.nextInt(discard.size());
            TrainCard outPlay = discard.get(rand);
            deck.push(outPlay);
            outPlay.hideCard();
            container.add(outPlay, new Integer(2));
            outPlay.moveTo(deckX, deckY);
            discard.remove(rand);
            discard.trimToSize();
        }
        cards.checkDeck(deck.size());
        container.validate();
    }

    /**
     * gets the cards that are in the display group
     * 
     * @return returns an arrayList of train cards representing
     * the group of displayed cards
     */
    public ArrayList<TrainCard> getDisplay(){
        return display;
    }

    /**
     * method for determining if the discard pile is empty
     * or not
     * 
     * @return returns whether the the discard pile is empty
     */
    public boolean discardNotEmpty(){
        return !discard.isEmpty();
    }

    public boolean deckIsEmpty(){
        return deck.isEmpty();
    }

    /**
     * designed for use only with the cards not in the deck, 
     * i.e. the players hand
     * 
     * @param gone card to be added to the deiscard
     */
    public synchronized void addToDiscard(TrainCard gone){
        container.add(gone, new Integer(1));
        gone.moveTo(deckX+10, cards.getDiscY()+deckH/4);
        gone.flip();
        gone.hideCard();
        discard.add(gone);
    }

    /**
     * @param moved card to be removed from the display
     */
    public synchronized void removeFromDisplay(TrainCard moved){
        if(moved != null){
            for(int iter = 0; iter < display.size();iter++){
                TrainCard elim = display.get(iter);
                if(elim.getCurrentY() == moved.getCurrentY()){
                    //get the position of the card we are 
                    //removing so we know where to add
                    //a card to the display
                    displayMovedPos = iter;
                    display.remove(iter);
                    container.remove(container.getIndexOf(moved));
                    addToDisplay();
                    return;
                }
            }
        }
    }

    /**
     * checks for whether the displayed group has 3 
     * or more locomotive cards then clears it out
     * and makes a new one 
     */
    private synchronized void hasTooManyLocos(){
        if(!deck.isEmpty() && deck.size() > 4){
            Thread animate = new Thread(new Runnable(){
                        public void run(){
                            try{
                                int count = 0;
                                //check the locomotive count in the
                                //display group
                                for(TrainCard locos: display){
                                    if(locos.getType().equals("loco"))
                                        count++;
                                }
                                //remove them if there are more than three
                                if(count >= 3){
                                    getCard = false;
                                    for(TrainCard locos: display){
                                        locos.translateTo(locos.getCurrentX(), 
                                        cards.getDiscY());
                                        locos.flip();
                                        Thread.sleep(700);
                                        locos.hideCard();
                                        discard.add(locos);
                                        int index = container.getIndexOf(locos);
                                        if(index >= 0)
                                            container.remove(index);
                                    }
                                    display.clear();
                                    display.trimToSize();
                                    addToDisplay();
                                }
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
    }

    /**
     * adds cards to the displayed group of cards, 
     * when one of them has been removed
     */
    public synchronized void addToDisplay(){
        Thread animate = new Thread(new Runnable(){
                    public void run(){
                        try{
                            getCard = false;
                            /*if the display is empty add all the cards
                             * else check if there are more than 2 missing
                             * else jsut adda  card to the display position
                             * that had one removed
                             */
                            if(display.size() == 0){
                                for(int newDis = 0;newDis < 5; newDis++){
                                    TrainCard dis = draw();
                                    if(yPos[newDis] == -1){
                                        yPos[newDis] = deckY - 
                                        (newDis+1)*dis.getheight()-newDis*10+10;
                                    }
                                    Thread.sleep(700);
                                    if(dis != null){
                                        dis.translateTo(deckX+10, yPos[newDis]);
                                        display.add(dis);
                                    }
                                }
                            }
                            else if(display.size() < 4){
                                for(int reOrder = 0;reOrder < display.size(); 
                                reOrder++){
                                    TrainCard dis = display.get(reOrder);
                                    if(yPos[reOrder] == -1){
                                        yPos[reOrder] = deckY - 
                                        (reOrder+1)*dis.getheight()-
                                        reOrder*10+10;
                                    }
                                    Thread.sleep(700);
                                    dis.translateTo(deckX+10, yPos[reOrder]);
                                }
                                for(int reOrder = display.size();reOrder < 5; 
                                reOrder++){
                                    TrainCard dis = draw();
                                    if(yPos[reOrder] == -1){
                                        yPos[reOrder] = deckY - 
                                        (reOrder+1)*dis.getheight()-
                                        reOrder*10+10;
                                    }
                                    Thread.sleep(700);
                                    if(dis != null){
                                        dis.translateTo(deckX+10, yPos[reOrder]);
                                        display.add(dis);
                                    }
                                }
                            }
                            else{
                                TrainCard dis = draw();
                                Thread.sleep(700);
                                if(dis != null){
                                    dis.translateTo(deckX+10, 
                                    yPos[displayMovedPos]);
                                    display.add(displayMovedPos, dis);
                                }
                            }
                            Thread.sleep(1200);
                            hasTooManyLocos();
                            getCard = true;
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

    /**
     * Checks if deck contains pointer
     * 
     * @param x pointer x position
     * @param y pointer y position
     * 
     * @return true if deck contains pointer, false otherwise
     */
    public boolean contains(double x, double y){
        return cards.contains(x, y);
    }

    /**
     * method to determine if a position is in the range 
     * of the displayed group cards
     * 
     * @param x pointer x position
     * @param y pointer y position
     * 
     * @return true if display range contains pointer,
     * false otherwise
     */
    public boolean rangeContains(double x, double y){
        if(x > deckX && y > cards.getDiscY()-rangeHeight){
            if(x < deckX+deckW && y < cards.getDiscY())
                return true;
            else
                return false;
        }
        else
            return false;
    }

    /**
     * Gets teh card in teh displayed group of cards
     * based on the x,y values, null if not found
     * 
     * @param x pointer x position
     * @param y pointer y position
     * 
     * @return A TrainCard from the group of displayed
     * cards 
     */
    public TrainCard getDisplayCard(double x, double y){
        if(getCard){
            for(int i = 0;i < display.size();i++){
                TrainCard dis = display.get(i);
                if(dis.contains(x,y)){
                    container.moveToFront(dis);
                    displayMovedPos = i;
                    return dis;
                }
            }
        }
        return null;
    }

    /**
     * Mocves train card back to display slot where it came
     * 
     * @param returned card being moved back to display
     */
    public void moveBack(TrainCard returned){
        //returned.translateTo(deckX+10,yPos[displayMovedPos]);
        for(int back = 0;back < display.size();back++){
            TrainCard move = display.get(back);
            if(returned.getCurrentY() == move.getCurrentY() &&
            returned.getCurrentX() == move.getCurrentX()){
                returned.translateTo(deckX+10,yPos[back]);
                break;
            }
        }
    }

    /**
     * @return returns card's width
     */
    public double getCardWidth(){
        return cardW;
    }

    /**
     * @return returns card's height
     */
    public double getCardHeight(){
        return cardH;
    }

    /**
     * This inner class handles the image of the deck on the board.  
     * 
     */
    private class DeckImage extends JComponent{
        private ArrayList<Image> deckSize;
        private Image displayDeck;
        private double deckY, deckX, deckW, deckH, discY, xScale, yScale;
        private AffineTransform deckTrans;

        /**
         * Constructs the image for deck
         * 
         * @param x x position
         * @param y y position 
         * @param w deck width
         * @param h deck height
         */
        public DeckImage(double x, double y, double w, double h){
            deckSize = new ArrayList<Image>(10);
            File deckFace = new File("image_data/Cards/back");
            for(File imgD : deckFace.listFiles()){
                Image size = null;
                try{
                    size = ImageIO.read(imgD);
                }
                catch(IOException e){
                    System.err.println(e.getMessage());
                }
                deckSize.add(size);
            }
            //perform necessary swap for the largest deck size image
            //odd behavior while loading images places the size 100 image at 
            //the second position in the array
            Image swap = deckSize.get(1);
            deckSize.remove(1);
            deckSize.add(swap);
            displayDeck = deckSize.get(9);
            deckX = x;
            deckY = y;
            deckH = ((BufferedImage) swap).getHeight();
            deckW = w;
            discY = y+h+10;
            xScale = w/287;
            yScale = h/187;
            deckTrans = new AffineTransform(xScale,0,0,yScale,deckX, deckY);
        }

        /**
         * Determines whether the image for the different
         * deck sizes needs to be swapped out and redrawn
         * 
         * @param  disp  a number representing the 
         * size of the deck
         */
        public void checkDeck(int disp){
            switch((int)Math.ceil(disp/10.0)){
                case 0: displayDeck = null;break;
                case 1: displayDeck = deckSize.get(0);break;
                case 2: displayDeck = deckSize.get(1);break;
                case 3: displayDeck = deckSize.get(2);break;
                case 4: displayDeck = deckSize.get(3);break;
                case 5: displayDeck = deckSize.get(4);break;
                case 6: displayDeck = deckSize.get(5);break;
                case 7: displayDeck = deckSize.get(6);break;
                case 8: displayDeck = deckSize.get(7);break;
                case 9: displayDeck = deckSize.get(8);break;
                case 10: displayDeck = deckSize.get(9);break;
                default: displayDeck = deckSize.get(9);break;
            }
            repaint();
        }

        /**
         * Checks if pointer is over the deck on the board.
         * 
         * @param x pointer x position
         * @parma y pointer y position
         * 
         * @return true if deck contains pointer, false otherwise
         */
        public boolean contains(double x, double y){
            if(x-8 > deckX && y-30 > deckY){
                if(x-8 < deckX+deckW && y-30 < discY-10)
                    return true;
                else 
                    return false;
            }
            else
                return false;
        }

        /**
         * @return Returns the coordinates of the no longer
         * displayed discard pile, the method was kept
         * for use with the hasTooManyLocos methods
         */
        public double getDiscY(){
            return discY;
        }

        /**
         * Accessor method for xScale variable
         * @return  value of xScale
         */
        public double getXScale(){
            return xScale;
        }

        /**
         * Accessor method for yScale variable
         * @return  value of yScale
         */
        public double getYScale(){
            return yScale;
        }

        /**
         * Paints the iamge of the deck to the
         * given graphics object
         * 
         * @param g Graphics object to be drawn to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(displayDeck, deckTrans, this);
            g2d.dispose();
        }
    }
}
