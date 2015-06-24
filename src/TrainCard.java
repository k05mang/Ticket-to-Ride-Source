import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.Serializable;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
/**
 * Write a description of class TrainCard here.
 * 
 * @author Kevin Mango, Marissa Bianchi, Mat Banville
 * Ryan Clancy 
 * @version (a version number or a date)
 */
public class TrainCard extends Card
{
    // color of train card
    private String color;
    // image of backside of card
    private BufferedImage back;
    /**
     * Constructor for objects of class TrainCard
     * 
     * @param front the front image of the card
     * @param x x position of card
     * @param y y position of card
     * @param w width of card
     * @param h height of card
     * @param color color of card
     */
    public TrainCard(Image front,double x, double y,
    double w, double h, String color)
    {
        super(front, null, x,y, w, h);
        this.color = color;
        //get the back image associated with this card type
        try{
            File retrieve = new File(
            "image_data/Cards/T2R_traincard_us_back.png");
            back = ImageIO.read(retrieve);
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
        super.setBackImage(back);
    }

    /**
     * @return the color(type) of card
     */
    public String getType(){
        return color;
    }
}
