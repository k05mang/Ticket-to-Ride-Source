import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.LookupOp;
import java.awt.image.ByteLookupTable;
/**
 * Creates the path object
 * 
 * @author Kevin Mango, Marissa Bianchi, Mat Banville
 * Ryan Clancy 
 * @version 4-25-2013
 */
public class Path extends JComponent
{
    private String color;
    private City a, b;
    private int pathDistance, mountainCount, imgData;
    private boolean owned, ferries;
    private Player owner;
    private BufferedImage path, pathScaled, claim;
    private double startX, startY, width, height, sH, sW;
    private AffineTransform size;
    private LookupOp colored;

    /**
     * Constructor for the path object. 
     * @param  path  image of the path
     * @param  claimed  image of what the path looks like claimed
     * @param  c  color of the path
     * @param  a  city at start of path
     * @param  b  city at end of path
     * @param  dist  distance of path
     * @param  mountains  number of mountain routes along path
     * @param  ferry  boolean for if it is a ferry route
     * @param  x  x coord of path
     * @param  y  y coord of path
     * @param  w  width of path
     * @param  h  height of path
     */
    public Path(BufferedImage path, BufferedImage claimed, String c, 
    City a, City b, int dist, int mountains, boolean ferry, double x, 
    double y, double w, double h){
        color = c;
        this.a = a;
        this.b = b;
        this.path = path;
        //sclae the images to match the iamge presented on the board
        //this scaled image helps in determining the correct
        //pixel information for the contains method
        claim = new BufferedImage((int)w,(int)h,BufferedImage.TYPE_4BYTE_ABGR);
        Image claimedScale = claimed.getScaledInstance((int)w,
                (int)h,Image.SCALE_DEFAULT);
        claim.getGraphics().drawImage(claimedScale,0,0,this);
        pathScaled = new BufferedImage((int)w,(int)h,
            BufferedImage.TYPE_4BYTE_ABGR);
        Image source = path.getScaledInstance((int)w,(int)h,
                Image.SCALE_DEFAULT);
        pathScaled.getGraphics().drawImage(source,0,0,this);

        pathDistance = dist;
        mountainCount = mountains;
        ferries = ferry;
        owned = false;
        owner = null;
        startX = x;
        startY = y;
        width = w;
        height = h;
        sW = w/path.getWidth();
        sH = h/path.getHeight();
        size = new AffineTransform(sW, 0,0,sH, x, y);
    }

    /**
     * Claimes the path for the player.
     * @param  theOwner  player claiming the path
     */
    public void capture(Player theOwner){
        //sets owned to true and gives owner the correct player
        owned = true;
        owner = theOwner;
        theOwner.claim(this);
        //gives the path the correct color for the owner
        colored = changeColor(theOwner.getColor());
        setVisible(true);
        repaint();
    }

    /**
     * Changes the color of the trains using LookupOp to modify
     * the image colors.
     * @param  paint  the correct color of the trains
     */
    public static LookupOp changeColor(String paint){
        //create the byte arrays that store the rgb
        //values of the new colors
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];
        byte[][] rgb;
        ByteLookupTable data;
        LookupOp colors = null;
        if(paint.equals("white")){
            //the images are inherently white so 
            //they need not be changed
            colors = null;
        }
        else if(paint.equals("orange")){
            for(int col = 0;col < 256; col++){
                //don't modifiy the alpha values
                if(col < 64){
                    red[col] = (byte)(col);
                    green[col] = (byte)(col);
                    blue[col] = 0;   
                }
                else{
                    red[col] = (byte)(col);
                    green[col] = (byte)(col-80);
                    blue[col] = 0;
                }
            }
            rgb = new byte[][]{red,green,blue};
            data = new ByteLookupTable(0,rgb);
            colors = new LookupOp(data,null);
        }
        else if(paint.equals("blue")){
            for(int col = 0;col < 256; col++){
                red[col] = 0;
                if(col < 127){
                    green[col] = (byte)(col);
                }
                else{
                    green[col] = (byte)(col-127);
                }
                blue[col] = (byte)(col);
            }
            rgb = new byte[][]{red,green,blue};
            data = new ByteLookupTable(0,rgb);
            colors = new LookupOp(data,null);
        }
        else if(paint.equals("gold")){
            for(int col = 0;col < 256; col++){
                if(col < 64){
                    red[col] = (byte)(col);
                    green[col] = (byte)(col);
                    blue[col] = 0;   
                }
                else{
                    red[col] = (byte)(col-50);
                    green[col] = (byte)(col-70);
                    blue[col] = 0; 
                }
            }
            rgb = new byte[][]{red,green,blue};
            data = new ByteLookupTable(0,rgb);
            colors = new LookupOp(data,null);
        }
        else if(paint.equals("purple")){
            for(int col = 0;col < 256; col++){
                if(col < 64){
                    red[col] = (byte)(col);
                }
                else{
                    red[col] = (byte)(col-75);
                }
                green[col] = 0;
                blue[col] = (byte)(col);
            }
            rgb = new byte[][]{red,green,blue};
            data = new ByteLookupTable(0,rgb);
            colors = new LookupOp(data,null);
        }
        return colors;
    }

    /**
     * Accessor method for the owner variable
     * @return  the player that owns the particular path
     */
    public Player getOwner(){
        return owner;
    }

    /**
     * Accessor method for the owned boolean
     * @return  true if path is owned, false if not
     */
    public boolean isOwned(){
        return owned;
    }

    /**
     * Accessor method for city a
     * @return  the city that the path starts at
     */
    public City getCityA(){
        return a;
    }

    /**
     * Accessor method for city b
     * @return  the city that the path ends at
     */
    public City getCityB(){
        return b;
    }

    /**
     * Accessor method for the path distance
     * @return  the distance of the path
     */
    public int getLength(){
        return pathDistance;
    }

    /**
     * Accessor method for mountainCount
     * @return  the number of mountain routes on the path
     */
    public int getMountains(){
        return mountainCount;
    }

    /**
     * Accessor method for the ferries
     * @return  true if the path contains a ferry, false if not
     */
    public boolean hasFerry(){
        return ferries;
    }

    /**
     * Accessor method for the color of the path
     * @return  the string containing the color of the path
     */
    public String getColor(){
        return color;
    }
    
    /**
     * gets the string representing this paths image
     * this method is mainly used in comparing identical
     * grey routes with no unique characteristics between 
     * them
     * 
     * 
     * @return A string object representing the path image
     */
    public String getImageString(){
        return path.toString();
    }

    /**
     * Tests to see if the cities of two path
     * objects are equivalent
     * 
     * @param  equity  path object to be tested
     * @return  boolean for if the cities are equal
     */
    public boolean citiesEqual(Path equity){
        if(equity.getCityA().equals(a) &&
        equity.getCityB().equals(b)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Tests to see if to paths are absolutely equivalent
     * 
     * @param  equals  path object to be tested
     * @return  boolean for if the paths are the same
     */
    public boolean equals(Path equals){
        if(citiesEqual(equals) && equals.getMountains() == mountainCount &&
        equals.getLength() == pathDistance && equals.isOwned() == owned){
            //grey colored routes have a special case
            //due to the exactness of these paths
            //the only disinguishing element of these is
            //their image data
            if(!equals.getColor().equals("grey")){
                if(equals.getColor().equals(color)){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                //test image data
                if(equals.getImageString().equals(path.toString())){
                    return true;
                }else{
                    return false;
                }
            }
        }
        else{
            return false;
        }
    }

    /**
     * Makes the paths visible depending on value of reveal
     * @param  reveal  boolean for if path is to be visible or not
     */
    public void makeVisible(boolean reveal){
        if(!owned)
            setVisible(reveal);
    }

    /**
     * Checks to see if the x and y coordinates are in the image
     * 
     * @param  x  x-coord
     * @param  y  y-coord
     */
    public boolean contains(double x, double y){
        //check if input is potentially valid
        if(x-8 > startX && y-30 > startY){
            if(x-8 < startX+width && y-30 < startY+height){
                //modify the input to make the coordinates 
                //relative to the top left corner of the 
                //image and check if get pixel data
                //yield out of bounds errors
                if((int)(x-8-startX) > 0 &&
                (int)(x-8-startX) < pathScaled.getWidth()
                && (int)(y-30-startY) > 0 && 
                (int)(y-30-startY) < pathScaled.getHeight()){     
                    //get pixel information from the image
                    imgData = pathScaled.getRGB((int)(x-8-startX),
                        (int)(y-30-startY));
                    //check the pixels transparency based on the 
                    //alphas value, if !transparent it is in the 
                    //image portion that is visible
                    if((imgData >> 24) != 0){
                        return true;
                    }
                    else{
                        return false;
                    }
                }
                return false;
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
     * Draws the paths
     * 
     * @param  g  graphics parameter
     */
    public void paint(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        if(!owned){
            g2d.drawImage(path,size,this);
        }
        else{
            g2d.drawImage(claim,colored,(int)startX, (int)startY);
        }
        g2d.dispose();
    }
}