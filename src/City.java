import javax.swing.JComponent;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
/**
 * Class representing a city on the board of ticket to ride
 * this class also acts as a node on a graph of cities
 * containing its own adjacency list, though this class 
 * extends the JComponent it does not actually draw anything 
 * the original intention of this class was to allow for
 * highlightable cities and display a clearer name for it
 * but the idea was scrapped and the reason it was kept a 
 * JComponent was to minimize code modification in 
 * the board class
 * 
 * @author Kevin Mango, Marissa Bianchi, Mat Banville
 * Ryan Clancy
 * @version 4-25-2013
 */
public class City extends JComponent
{
    private String name;
    // location and dimensions on board
    private double startX, startY, diam, radius, sW;
    private BufferedImage displayRing;
    private AffineTransform transform;
    private boolean select;
    // array list containing all adjacent cities 
    private ArrayList<City> adjacent;
    /**
     * Constructor for each individual city
     * 
     * @param str city's name
     * @param x x position
     * @param y y position
     * @param diameter city diameter
     */
    public City(String str, double x, double y, double diameter)
    {
        name = str;
        startX = x;
        startY = y;
        diam = diameter;
        radius = diameter/2;
        sW = diameter/70;
        select = false;
        try{
            File ring = new File("image_data/cityHighLight.png");
            displayRing = ImageIO.read(ring);
        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }
        transform = new AffineTransform(sW,0,0,sW,x-radius, y-radius);
        adjacent = new ArrayList<City>();
    }

    /**
     * Checks to see if city contains pointer on map
     *
     * @param x pointer x position 
     * @param y pointer y position 
     * 
     * @return true if pointer hovers city, false otherwise
     */
    public boolean contains(double x, double y){
        double xDis = Math.abs(startX-x+8);
        double yDis = Math.abs(startY-y+30);
        double distance = Math.floor(Math.hypot(xDis, yDis));
        if(distance < radius){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Returns the name of the city
     * 
     * @return name of city
     */
    public String getName(){
        return name;
    }
    
    public void printAdj(){
        for(City adj:adjacent){
            System.out.print(adj.getName()+" ,");
        }
    }

    /**
     * Makes city visible when hovered
     * 
     * @param visible true if contains method is true
     */
    public void makeVisible(boolean visible){
        if(!select)
            setVisible(visible);
    }

    /**
     * Selecting of city for path capturing
     */
    public void select(){
        setVisible(true);
        select = true;
    }

    /**
     * Deselects city after veingg selected
     */
    public void deselect(){
        setVisible(false);
        select = false;
    }

    /**
     * @return true if city is currently selected
     */
    public boolean isSelected(){
        return select;
    }

    /**
     * @return the array list containing all adjacent cities
     */
    public ArrayList<City> getAdj(){
        return adjacent;
    }

    /**
     * Adds adjacent cities to array list
     * 
     * @param near city to be added to array list
     */
    public void addAdjacent(City near){
        if(!adjacent.contains(near))
            adjacent.add(near);
    }

    /**
     * @param c city to be compared to
     * 
     * @return if the city being checked matches the current city
     */
    public boolean equals(City c){
        if(c.getName().equals(name)){
            return true;
        }
        else{
            return false;
        }
    }
}
