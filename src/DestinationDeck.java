import java.util.ArrayList;
import java.util.Random;
import javax.swing.JComponent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.awt.*;
import javax.swing.JLayeredPane;
import java.util.Scanner;
import java.util.Hashtable;
import java.awt.image.BufferedImage;
/**
 * Creates a deck of destination cards
 * 
 * @author Kevin Mango, Marissa Bianchi, Mat Banville
 * Ryan Clancy
 * @version 4-30-2013
 */
public class DestinationDeck
{
    private ArrayList<DestinationCard> deck, display, large, removed;
    private Hashtable<String, Integer> cards;
    private Random grab;
    private Scanner read;
    private double deckY, deckX, deckW, deckH;
    public JLayeredPane container;
    private DeckImage face;
    /**
     * Constructor for objects of class DestinationDeck
     * @param  x  x coord of the deck
     * @param  y  y coord of the deck
     * @param  w  width of the deck
     * @param  h  height of the deck
     * @param renderX  the x-coord of the end of the bounds
     * @param renderY  the y-coord of the end of the bounds
     */
    public DestinationDeck(double x, double y, double w, double h, 
    int renderX, int renderY)
    {
        deckX = x;
        deckY = y;
        deckW = w;
        deckH = h;
        face = new DeckImage(x,y,w,h);
        display = new ArrayList<DestinationCard>();
        large = new ArrayList<DestinationCard>();
        deck = new ArrayList<DestinationCard>();
        removed = new ArrayList<DestinationCard>();
        try{
            read = new Scanner(new File("text_info/destination.txt"));
            cards = new Hashtable<String,Integer>();
            //get the card information from the text file
            //this can be used in creating the cards
            while(read.hasNextLine()){
                String line[] = read.nextLine().split(":");
                Integer parsed = Integer.parseInt(line[1].trim());
                cards.put(line[0].trim().toLowerCase(),parsed);
            }
            read.close();
            File path = new File("image_data/Tickets/front");
            //get all the images for the cards and create the cards
            for(File img: path.listFiles()){
                Image front = null;
                String destin = 
                    img.getName().replaceFirst(".png","").toLowerCase();
                front = ImageIO.read(img);
                Integer value = cards.get(destin);
                DestinationCard found;
                if(value > 12){
                    found = new DestinationCard(front,x, y, 
                        w-37*face.getXScale(),h-26*face.getYScale(),
                        value,destin);
                    large.add(found);
                }
                else{
                    found = new DestinationCard(front,x, y, 
                        w-37*face.getXScale(),h-26*face.getYScale(),
                        value,destin); 
                    display.add(found);
                }
                found.setBounds(0,0,renderX, renderY);
            }
        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }
        face.setBounds(0,0,renderX, renderY);
        container = new JLayeredPane();
        grab = new Random();
        for(DestinationCard far:large){
            container.add(far,new Integer(1));
            far.hideCard();
        }
        while(!display.isEmpty()){
            int rand = grab.nextInt(display.size());
            DestinationCard start = display.get(rand);
            deck.add(start);
            container.add(start, new Integer(1));
            start.hideCard();
            display.remove(rand);
            display.trimToSize();
        }
        container.add(face, new Integer(0));
        container.setBounds(0,0,renderX, renderY);
    }

    /**
     * Adds card to the bottom of the deck including graphics
     */
    public void addToBottom(){
        for(DestinationCard add:display){
            if(!add.isSelected()){
                /*
                 * swap the cards between planes of the 
                 * JlayeredPane, designed to make smooth
                 * movement of cards back into the deck
                 * but requires threading to perform as 
                 * planned
                 */
                add.flip();
                container.setLayer(add, new Integer(0));
                add.translateTo(deckX, deckY);
                add.hideCard();
                container.setLayer(add, new Integer(1));
                deck.add(add);
            }
            else{
                container.remove(container.getIndexOf(add));
            }
        }
        face.checkDeck(deck.size());
        display.clear();
        display.trimToSize();
    }

     /**
     * Draw a card from destination deck
     * 
     * @param  x  x-coordinate to place the first card
     * @param  y  y-coordinate to place the first card
     * @param  firstDraw  boolean if it is the first draw 
     * for the game which is a special case wth 4 cards
     */
    public ArrayList<DestinationCard> draw(double x, double y, 
    boolean firstDraw){
        DestinationCard drawn;
        display.clear();
        display.trimToSize();
        if(firstDraw){
            int rand = grab.nextInt(large.size());
            drawn = large.get(rand);
            display.add(drawn);
            large.remove(rand);
            drawn.showCard();
            drawn.flip();
            drawn.translateTo(x,y);
        }
        int numCards = (deck.size() <3 ? deck.size():3);
        for(int norm = 0; norm < numCards;norm++){
            drawn = deck.get(0);
            drawn.showCard();
            drawn.flip();
            drawn.translateTo(x, 
                y+(norm+(firstDraw ? 1:0))*(drawn.getheight()+10));
            display.add(drawn);
            deck.remove(0);
        }
        face.checkDeck(deck.size());
        return display;
    }

    /**
     * Removes cards that were drawn with an initial draw
     * which are removed from the game, this is different 
     * from how a normal discard occurs
     */
    public void removeInit(){
        for(DestinationCard remove: display){
            if(!remove.isSelected()){
                removed.add(remove);
                remove.hideCard();
                remove.moveTo(-deckW,0);
            }
            else{
                container.remove(container.getIndexOf(remove));
            }
        }
    }

    /**
     * Checks to see if the card contains the x and y coordinates
     * @param  x  x-coordinate
     * @param  y  y-coordinate
     * @return return true if is contained, false if not
     */
    public boolean contains(double x, double y){
        return face.contains(x, y);
    }

    /**
     * Accessor method for the x-coordinate of the deck
     * @return x-coordinate of deck
     */
    public double getX(){
        return deckX;
    }

    /**
     * Accessor method for the y-coordinate of the deck
     * @return y-coordinate of deck
     */
    public double getY(){
        return deckY;
    }

    /**
     * Accessor method for the width of the deck
     * @return width of deck
     */
    public double getWidth(){
        return deckW;
    }

    /**
     * Accessor method for the height of the deck
     * @return height of deck
     */
    public double getHeight(){
        return deckH;
    }

    /**
     * Class designed to represent the visual component of
     * the deck
     */
    private class DeckImage extends JComponent{
        private ArrayList<Image> deckSize;
        private Image displayDeck;
        private double deckY, deckX, deckW, deckH, xScale, yScale;
        private AffineTransform deckTrans;
        public DeckImage(double x, double y, double w, double h){
            deckSize = new ArrayList<Image>(10);
            File deckFace = new File("image_data/Tickets/backdeck_size");
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
            displayDeck = deckSize.get(3);
            deckX = x;
            deckY = y;
            xScale = w/287;
            yScale = h/187;
            deckH = h;
            deckW = w;
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
            //since deck is inherently smaller
            //than the largest image, only go 
            //as far as the max size
            switch((int)Math.ceil(disp/10.0)){
                case 0: displayDeck = null;break;
                case 1: displayDeck = deckSize.get(0);break;
                case 2: displayDeck = deckSize.get(1);break;
                case 3: displayDeck = deckSize.get(2);break;
                case 4: displayDeck = deckSize.get(3);break;
            }
            repaint();
        }

        /**
         * Checks if the x and y coords are contained in the DeckImage
         * @param  x  x-coordinate to be checked
         * @param  y  y-coordinate to be checked
         */
        public boolean contains(double x, double y){
            if(x-8 > deckX && y-30 > deckY){
                if(x-8 < deckX+deckW && y-30 < deckY+deckH)
                    return true;
                else 
                    return false;
            }
            else
                return false;
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
         * Paints the DeckImage
         * @param  g  graphics object to be painted to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(displayDeck, deckTrans, this);
            g2d.dispose();
        }
    }
}
