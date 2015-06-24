import java.awt.*;
import java.io.Serializable;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
/**
 * Creates the destination card
 * 
 * @author Kevin Mango, Marissa Bianchi, Mat Banville
 * Ryan Clancy 
 * @version 4-20-2013
 */
public class DestinationCard extends Card
{
    private int value;
    private String destin;
    private Image back;
    private boolean select;
    /**
     * Constructor for objects of class DestinationCard
     * 
     * @param front Image of the front of the card
     * @param x the x coord of the card
     * @param y the y coord of the card
     * @param w width of the card
     * @param h height of the card
     * @param value 
     * @param toFrom String of the start and end city
     */
    public DestinationCard(Image front, double x, double y, double w,
    double h, int value, String toFrom)
    {
        super(front, null, x,y, w, h);
        try{
            File retrieve = new File("image_data/Tickets/T2R-ticket-back.png");
            back = ImageIO.read(retrieve);
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
        super.setBackImage(back);
        this.value = value;
        destin = toFrom;
        select = false;
    }

    /**
     * Tests to see if the destinations match
     * 
     * @param  test  DestinationCard
     * @return boolean of if the two destinations match
     */
    public boolean equals(DestinationCard test){
        if(destin.equals(test.getDestination()))
            return true;
        else 
            return false;
    }
    
    /**
     * Accessor method for the value variable
     * 
     * @return the int value of vaule
     */
    public int value(){
        return value;
    }
    
    /**
     * Splits the destin variable on the - character and makes
     * the result all lowercase
     * 
     * @return String[] array for city names
     */
    public String[] destinations(){
        return destin.toLowerCase().trim().split("-");
    }

    /**
     * Accessor method for the destin variable
     * 
     * @return String of destin variable
     */
    public String getDestination(){
        return destin;
    }
}
