import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;
/**
 * Class that represents a card in the game of Ticket 
 * to Ride
 * 
 * @author Kevin Mango, Marissa Bianchi, Mat Banville
 * Ryan Clancy
 * @version 4-20-2013
 */
public abstract class Card extends JComponent
implements Runnable
{  
    protected Image front, back, display, shadow, displayBorder, selection;
    protected AffineTransform transform, transBorder;
    protected double startX, startY,width, height, origX, origY, sW,sH;
    protected boolean locked, flip, trans, visible, select;
    protected Thread animate;

    /**
     * Constructor for the Card class
     * 
     * @param front Image that represents the front of the 
     * card to be displayed
     * @param back image representing the back of the card
     * to be displayed
     * @param x x-cocrdinate for where the card will be 
     * positioned
     * @param y y-cocrdinate for where the card will be 
     * positioned
     * @param w width of the card object
     * @param h height of the card object
     */
    public Card(Image front, Image back,
    double x, double y, double w, double h){
        this.front = front;
        this.back = back;
        display = back;
        sW = w/250;
        sH = h/161;
        transform = new AffineTransform(sW,0,0,sH,x,y);
        startX = origX = x;
        startY = origY = y;
        width = w;
        height = h;
        locked = flip = trans = select = false;
        animate = null;
        visible = true;
        try{
            File shade = new File("image_data/Cards/T2R_traincard_shadow.png");
            shadow = ImageIO.read(shade);
        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }
        displayBorder = shadow;
        //make the shadow slightly up and back to center based on the 
        //cards x and y
        transBorder = new AffineTransform(sW,0,0,sH,x-(7.5*sW),y-(7*sH));
    }

    /**
     * method for determining if some point(x,y) is contained within
     * this card object
     * 
     * @param x x-coordinate of point to be tested
     * @param y y-coordiante of point to be tested
     * 
     * @return returns whether the given point is 
     * contained within this card object
     */
    public boolean contains(double x, double y){
        //subtract the x value by 8 and the y value
        //by 30, this accounts for insets in the frame
        if(x-8 > startX && y-30 > startY){
            if(x-8 < startX+width && y-30 < startY+height)
                return true;
            else
                return false;
        }
        else
            return false; 
    }

    /**
     * method for determining if some point(x,y) is contained within
     * the right half of this card object
     * 
     * @param x x-coordinate of point to be tested
     * @param y y-coordiante of point to be tested
     * 
     * @return returns whether the given point is 
     * contained within the right half of this card object
     */
    public boolean rightHalfContains(double x, double y){
        if(x-8 > startX+width/2 && y-30 > startY){
            if(x-8 < startX+width && y-30 < startY+height)
                return true;
            else
                return false;
        }
        else
            return false;
    }

    /**
     * method for setting the back Image for the card
     * this method was made to make it possible
     * for subClass cards to load in their own back
     * images after a call to the super constructor
     * 
     * @param back image to be used for this cards back image
     */
    protected void setBackImage(Image back){
        this.back = back;
        if(display == null)
            display = back;
    }

    /**
     * method for translating a card from a point(x1, y1) to
     * (x2,y2) in a fluid motion across the screen
     * 
     * @param x x-coordinate of where to move the card
     * @param y y-coordinate of where to move the card
     */
    public void translateTo(double x, double y){
        //loop that acts as a lock for other thread
        //calls, appears to do nothing
        while(Thread.currentThread() == animate){}
        //store the point we are moving from
        origX = startX;
        origY = startY;
        startX = x-8;
        startY = y-30;
        trans = true;
        //start the thread
        if(animate == null){
            animate = new Thread(this);    
            animate.start();
        }
    }

    /**
     * method for flipping the card from back to front
     * and vice versa in an animation
     * 
     */
    public void flip(){
        while(Thread.currentThread() == animate){}
        flip = true;
        if(animate == null){
            animate = new Thread(this);    
            animate.start();
        }
    }

    /**
     * method for moving a card from one point to another in 
     * a more immediate manner tahn translating
     * 
     * @param x x-coordinate of where to move the card
     * @param y y-coordinate of where to move the card
     */
    public void moveTo(double x, double y){
        while(Thread.currentThread() == animate){}
        startX = x;
        startY = y;
        transform.setTransform(sW,0,0,sH,startX, startY);
        transBorder.setTransform(sW,0,0,sH,startX-(7.5*sW),startY-(7*sH));
        repaint();
    }

    /**
     * method for dragging the card to a point
     * this method moves the card left and up by
     * half of the width and height respectively
     * this creates a better feel of grabbing the card
     * when picking it up with the cursor, the original
     * design of this method was to allow of if(card.contains(x,y))
     * then move it by this much so the mouseDragged of a mouseListener
     * class could continue to move the card with the mouse onec grabbed
     * 
     * @param x x-coordinate of where to move the card
     * @param y y-coordinate of where to move the card
     */
    public void dragTo(double x, double y){
        while(Thread.currentThread() == animate){}
        startX = x-8- width/2;
        startY = y-30- height/2;
        transform.setTransform(sW,0,0,sH,startX, startY);
        transBorder.setTransform(sW,0,0,sH,startX-(7.5*sW),startY-(7*sH));
        repaint();
    }

    /**
     * method for showing the card on the screen 
     */
    public void showCard(){
        while(Thread.currentThread() == animate){}
        visible = true;
        setVisible(visible);
    }

    /**
     * method for hiding the card on the screen 
     */
    public void hideCard(){
        while(Thread.currentThread() == animate){}
        visible = false;
        setVisible(visible);
    }

    /**
     * method for selecting a card, this method shrinks the
     * card to give it a more visual representative state from 
     * the normal state to indicate to users it has been selected
     *  note: this method was mainly designed for use with the 
     *  destination card subClass
     */
    public void select(){
        select = true;
        //scale the card down by a fact of .85
        //and shift it by its coordinates scaled by .15
        transform.setTransform(sW*.85,0,0,sH*.85,startX+(width/2)*.15,
            startY+(height/2)*.15);
        transBorder.setTransform(sW*.85,0,0,sH*.85,
            startX-(7.5*(sW*.85))+(width/2)*.15,
            startY-(7*(sH*.85))+(height/2)*.15);
        repaint();
    }

    /**
     * method for deselecting a card, this method returns the
     * card to its normal state 
     *  note: this method was mainly designed for use with the 
     *  destination card subClass
     */
    public void deselect(){
        select = false;
        //return the card back to normal
        transform.setTransform(sW,0,0,sH,startX,startY);
        transBorder.setTransform(sW,0,0,sH,startX-(7.5*sW),startY-(7*sH));
        repaint();
    }

    /**
     * tells the program if a card is in the selected state
     * 
     * @param boolean representing the selected state of the
     * card object
     */
    public boolean isSelected(){
        return select; 
    }

    /**
     * tells whether a card has been hidden with the hideCard method
     * 
     * @param boolean determining if the card is hidden
     */
    public boolean isHidden(){
        return !visible;
    }

    /**
     * tells whether a card has been shown with the showCard method
     * 
     * @param boolean determining if the card is shown
     */
    public boolean isShown(){
        return visible;
    }

    /**
     * method for getting the cards current x postion
     * 
     * @return returns the cards current x position
     * as a double precision number
     */
    public double getCurrentX(){
        while(Thread.currentThread() == animate){}
        return startX;
    }

    /**
     * method for getting the cards current y postion
     * 
     * @return returns the cards current y position
     * as a double precision number
     */
    public double getCurrentY(){
        while(Thread.currentThread() == animate){}
        return startY;
    }

    /**
     * method for getting the cards current height
     * 
     * @return returns the cards current height
     * as a double precision number
     */
    public double getheight(){
        return height;
    }

    /**
     * method for getting the cards current width
     * 
     * @return returns the cards current width
     * as a double precision number
     */
    public double getwidth(){
        return width;
    }

    /**
     * method for painting this card object which is a component
     * 
     * @param g Graphics object to be drawn to
     */
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(displayBorder, transBorder, this);
        g2d.drawImage(display, transform, this);
        g2d.dispose();
    }

    /**
     * method called by a thread object when the start method
     * in Thread is called and this is passed in as the runnable
     * object for that thread
     */
    public void run(){
        locked = true;
        try{
            //if translateTo was called this portion of
            // the run call will be exceuted
            if(trans){
                /*
                 * test for different scenarios that occur while
                 * translating, which include 
                 * 
                 * the point to be moved to is that same point
                 * we are at
                 * 
                 * the point we are moving to is directly across
                 * along the x plane
                 * 
                 * else the point we are moving to is along a 
                 * diagonal
                 * 
                 */
                if(startX-origX == 0 && startY-origY == 0){
                    locked = false;
                    return;
                }
                else if(origY-startY == 0){
                    double absX = Math.abs(startX-origX);
                    //move along the x plane sequentially
                    for(double mX = 0; mX <= absX; mX++){
                        transform.setTransform(sW,0,0,sH,
                            origX+(startX-origX < 0 ? -mX:mX),origY);
                        transBorder.setTransform(sW,0,0,sH,
                            origX+(startX-origX < 0 ? -mX:mX)-(7.5*sW),
                            origY-(7*sH));
                        repaint();
                        Thread.sleep(1);
                    }
                    transform.setTransform(sW,0,0,sH,startX,startY);
                    transBorder.setTransform(sW,0,0,sH,startX-(7.5*sW),
                        startY-(7*sH));
                    repaint();
                }
                else if(origX-startX == 0){
                    double absY = Math.abs(startY-origY);
                    //move along the y plane sequentially
                    for(double mY = 0; mY <= absY; mY++){
                        transform.setTransform(sW,0,0,sH,origX, 
                            origY+(startY-origY < 0 ? -mY:mY));
                        transBorder.setTransform(sW,0,0,sH,origX-(7.5*sW), 
                            origY+(startY-origY < 0 ? -mY:mY)-(7*sH));
                        repaint();
                        Thread.sleep(1);
                    }
                    //make certain we are at the final point
                    transform.setTransform(sW,0,0,sH,startX,startY);
                    transBorder.setTransform(sW,0,0,sH,startX-(7.5*sW),
                        startY-(7*sH));
                    repaint();
                }
                else{
                    //move along the x plane for the loop
                    //changing the y value as we loop
                    /*
                     * idea is based off the current point we are at being 
                     * the origin of some cartesian plane, then determing
                     * a line along this plane in which to move
                     */
                    double absX = Math.abs(startX-origX);
                    double m = Math.abs((startY-origY)/(startX-origX));
                    //change the rate at which we move the card, based on
                    //distance we are moving to make the animation smoother
                    //for smaller values
                    double incr = (absX < 30 ? .1:1.5);
                    for(double mX = 0; mX <= absX; mX+=incr ){
                        //perform a some linear algebra for determing
                        //the change of y based on x and for each 
                        //determine if we are moving backwards or forwards
                        double tX = origX+(startX-origX < 0 ? -mX:mX);
                        double mY = origY+(startY-origY < 0 ? -(m*(mX)):m*(mX));
                        transform.setTransform(sW,0,0,sH,tX,mY);
                        transBorder.setTransform(sW,0,0,sH,tX-(7.5*sW),
                            mY-(7*sH));
                        repaint();
                        Thread.sleep(1);
                    }
                    transform.setTransform(sW,0,0,sH,startX,startY);
                    transBorder.setTransform(sW,0,0,sH,startX-(7.5*sW),
                        startY-(7*sH));
                    repaint();
                }
                origX = startX;
                origY = startY;
                trans = false;
                locked = false;
                animate = null;
            }
            //if flip was called this portion of
            // the run call will be exceuted
            if(flip){
                //shrink the card along the y plane
                for(double sx = 0;sx < .15;sx +=.01){
                    transform.translate(0,height/12.5);
                    transform.scale(1,1-((10*sx)/3));
                    transBorder.translate(0,height/12.5);
                    transBorder.scale(1,1-((10*sx)/3));
                    repaint();
                    Thread.sleep(15);
                }
                //change the dispaly to front or back 
                if(display == back)
                    display = front;
                else
                    display = back;
                //grow the card back to its original state
                for(double sx = .14;sx >= 0;sx -=.01){
                    transform.scale(1,Math.pow(1-((10*sx)/3), -1));
                    transform.translate(0,-(height/12.5));
                    transBorder.scale(1,Math.pow(1-((10*sx)/3), -1));
                    transBorder.translate(0,-(height/12.5));
                    repaint();
                    Thread.sleep(15);
                }
                flip = false;
                locked = false;
                animate = null;
            }
        }
        catch(InterruptedException e){
            System.err.println(e.getMessage());
        }
    }
}
