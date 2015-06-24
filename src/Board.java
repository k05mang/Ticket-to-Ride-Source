import java.io.Serializable;
import javax.swing.JLayeredPane;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import java.awt.geom.AffineTransform;
import java.awt.Image;
import java.awt.*;
import java.util.Arrays;
/**
 * Establishes Board with cities and paths read in from a file
 * 
 * @author Kevin Mango, Mat Banville, Marissa Bianchi, Ryan Clancy
 * @version 4-30-2013
 */
public class Board 
{
    private ArrayList<Path> paths;
    private ArrayList<City> cities;
    private double startX, startY, width, height, sW, sH, laySW,laySH;
    public JLayeredPane container;
    private Map map;
    private Layout background;
    /**
     *constructor for the board object, its layout and its
     *sub components, such as the cities, and the paths
     *in addition this class also create the border layout that appears 
     *on the edge of the board
     *
     *@param x x position to draw this board to
     *@param y y position to draw this board to
     *@param w width of this board object
     *@param h height of this board object
     *@param renderX the width of the space this
     *object is being rendered to
     *@param renderY the height this board is being rendered to
     */
    public Board(double x, double y, double w, double h, int renderX, 
    int renderY)
    {
        paths = new ArrayList<Path>();
        cities = new ArrayList<City>();
        File[] pathImgs= (new File("image_data/paths")).listFiles();
        File[] claimsImgs = (new File("image_data/claims")).listFiles();
        ArrayList<File> pathFiles = new ArrayList<File>(Arrays.asList
        (pathImgs));
        
        ArrayList<File> claims = new ArrayList<File>(Arrays.asList
        (claimsImgs));
        startX = x;
        startY = y;
        width = w;
        height = h;
        laySW = w/2459;
        laySH = h/1297;
        container = new JLayeredPane();
        map = new Map((x+406)*laySW,y,1701*laySW,995*laySH);
        sW = map.width/1701;
        sH = map.height/1097;
        background = new Layout(x,y,w,h);
        map.setBounds(0,0,renderX, renderY);
        background.setBounds(0,0,renderX,renderY);
        container.add(map, new Integer(0));
        container.add(background, new Integer(0));

        try{
            String[] parseCity,parseInfo, parseCoor, parseAll, cityInfo, 
            cityCoor;
            Scanner cityTxt = new Scanner(new File("text_info/cities.txt"));
            //get information about each of the cities
            while(cityTxt.hasNextLine()){
                cityInfo = cityTxt.nextLine().trim().split(":");
                cityCoor = cityInfo[1].trim().split(",");
                double xPos = Double.parseDouble(cityCoor[0]);
                double yPos = Double.parseDouble(cityCoor[1]);
                City retrieved = new City(cityInfo[0].toLowerCase().trim(),map
                
                .startX+xPos*sW,y+yPos*sH,70*sH);
                cities.add(retrieved);
                retrieved.setBounds(0,0,renderX, renderY);
                container.add(retrieved, new Integer(2));
            }
            cityTxt.close();
            Scanner text = new Scanner(new File("text_info/edges.txt"));
            //get the edge info in the text file
            while(text.hasNextLine()){
                int numMnts = 0, distance = 0;
                boolean isFerry = false;
                double tX = 0, tY = 0;
                String color = "";
                parseAll = text.nextLine().trim().split(":");
                parseCity = parseAll[0].trim().split("-");
                parseInfo = parseAll[1].trim().split(" ");
                parseCoor = parseAll[2].trim().split(",");
                tX = Double.parseDouble(parseCoor[0]);
                tY = Double.parseDouble(parseCoor[1]);
                //get the information for a path
                for(int in = 0;in < parseInfo.length;in++){
                    String get = parseInfo[in].toLowerCase();
                    if(in == 0){
                        distance = Integer.parseInt(get);
                    }
                    else if(in == 1){
                        color = get;
                    }
                    else if(in == 2){ 
                        if(get.equals("ferry")){
                            isFerry = true;
                        }
                    }
                    else if(in == 3){
                        numMnts = Integer.parseInt(get);
                    }
                }
                String city = parseAll[0].trim().toLowerCase();
                BufferedImage pathImg = null, claimImg = null;
                //get the image data for a path
                for(int f = 0; f < pathFiles.size();f++){
                    String fileName = pathFiles.get(f).getName().replaceFirst
                    (".png","").toLowerCase();
                    String[] type = fileName.split(" ");
                    if(city.equals(type[0])){
                        if(type.length > 1){
                            //handle cases where special identifies are 
                            //used in the image
                            if(type[1].equals("top") || (type[1].equals
                            ("bottom"))){
                                pathImg = ImageIO.read(pathFiles.get(f));
                                pathFiles.remove(f);
                                break;
                            }
                            else if(type[1].equals("mountain")){
                                if(numMnts != 0){
                                    pathImg = ImageIO.read(pathFiles.get(f));
                                    pathFiles.remove(f);
                                    break;
                                }
                            }
                            else if(type[1].equals("left")){
                                if(numMnts == 0){
                                    pathImg = ImageIO.read(pathFiles.get(f));
                                    pathFiles.remove(f);
                                    break;
                                }
                            }

                            else if(type[1].equals(color)){
                                pathImg = ImageIO.read(pathFiles.get(f));
                                pathFiles.remove(f);
                                break;
                            }
                        }
                        else {
                            pathImg = ImageIO.read(pathFiles.get(f));
                            pathFiles.remove(f);
                            break;
                        }
                    }
                }
                
                //perform the same task above but for the claiming
                //image
                for(int f = 0; f < claims.size();f++){
                    String fileName = claims.get(f).getName().replaceFirst
                    (".png","").toLowerCase();
                    String[] type = fileName.split(" ");
                    if(city.equals(type[0])){
                        if(type.length > 1){
                            if(type[1].equals("top") || (type[1].equals
                            ("bottom"))){
                                claimImg = ImageIO.read(claims.get(f));
                                claims.remove(f);
                                break;
                            }
                            else if(type[1].equals("mountain")){
                                if(numMnts != 0){
                                    claimImg = ImageIO.read(claims.get(f));
                                    claims.remove(f);
                                    break;
                                }
                            }
                            else if(type[1].equals("left")){
                                if(numMnts == 0){
                                    claimImg = ImageIO.read(claims.get(f));
                                    claims.remove(f);
                                    break;
                                }
                            }
                            else if(type[1].equals(color)){
                                claimImg = ImageIO.read(claims.get(f));
                                claims.remove(f);
                                break;
                            }
                        } 
                        else{
                            claimImg = ImageIO.read(claims.get(f));
                            claims.remove(f);
                            break;
                        }
                    }
                }
                City a = null,b = null;
                //find the cities for the path
                for(City get:cities){
                    if(parseCity[0].toLowerCase().equals(get.getName())){
                        a = get;
                    }
                    else if(parseCity[1].toLowerCase().equals(get.getName())){
                        b = get;
                    }
                }
                if(a != null && b != null){
                    a.addAdjacent(b);
                    b.addAdjacent(a);
                }
                //finally create the path
                Path created = new Path(pathImg, claimImg, color, a, b, 
                distance,
                        numMnts, isFerry, map.startX+tX*sW, y+tY*sH, 
                        pathImg.getWidth()*sW,pathImg.getHeight()*sH);
                created.setBounds(0,0,renderX, renderY);
                created.makeVisible(false);
                paths.add(created);
                container.add(created, new Integer(1));
            }
            text.close();
        }
        catch(IOException e){            
            System.err.println(e.getMessage());
        }
        container.setBounds(0,0,renderX, renderY);
    }

    /**
     * method for checking if one of the paths in on this board
     * contains the given point and making this path object appear 
     * on the screen
     * 
     * @param x x value of the point being passed in
     * @param y y value of the point being passed in
     * 
     * @return returns whether the given point was contained
     * in one of the path objects on this board
     */
    public boolean pathContains(double x, double y){
        for(Path p : paths){
            if(p.contains(x, y)){
                p.makeVisible(true);
                return true;
            }
            else{
                p.makeVisible(false); 
            }
        }
        return false;
    }

    /**
     * method for getting the path object at a given point on the board
     * 
     * @param x x value of the point being tested
     * @param y y value of the point being tested
     * 
     * @return returns the path object that contains the 
     * given point
     */
    public Path getPath(double x, double y){
        for(Path p : paths){
            if(p.contains(x, y)){
                return p; 
            }
        }
        return null;
    }

    /**
     * method for retrieving the cities this board object contains
     * 
     * @return returns the arrayList that contains all the cities on 
     * this board
     */
    public ArrayList<City> getCities(){
        return cities;
    }

    public ArrayList<Path> getPaths(){
        return paths;
    }

    /**
     * method for retrieving a path that is similar to the given
     * path but not the samem, this method is designed for returning 
     * the second path on a double route path on the board, if there 
     * is such a path
     * 
     * @param secondary path object to get the duplicate of
     * 
     * @return returns the path object that represents a path similar to the
     * secondary input
     */
    public Path getDuplicatePath(Path secondary){
        for(Path p: paths){
            if(p.citiesEqual(secondary)){
                if(!p.equals(secondary)){
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * method for getting the boards scale factor
     * this can be used in creating other game objects so
     * that everything scales relative to this
     * 
     * @return returns this objects scale factor in the x plane
     */
    public double getXScale(){
        return laySW;
    }

    /**
     * method for getting the boards scale factor
     * this can be used in creating other game objects so
     * that everything scales relative to this
     * 
     * @return returns this objects scale factor in the y plane
     */
    public double getYScale(){
        return laySH;
    }

    /**
     * class for drawing the map portion of the board to the screen
     */
    private class Map extends JComponent {
        protected double startX, startY, width, height, sW, sH;
        protected AffineTransform changed;
        protected Image map;
        /**
         * constructor for the map object
         * 
         * @param x  x position to draw this map to
         * @parm y y position to draw this map to
         * @param w width of this map 
         * @param h height of this map
         */
        public Map(double x, double y, double w, double h){
            startX = x;
            startY = y;
            width = w;
            height = h;
            sW = w/1701;
            sH = h/1097;
            changed = new AffineTransform(sW,0,0,sH,x,y);
            try{
                File mapFile = new File("image_data/board.jpg");
                map = ImageIO.read(mapFile);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
        }

        /**
         * method to draw this map object to the screen
         * 
         * @param g graphics object to draw this to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(map,changed,this);
            g2d.dispose();
        }
    }
    
    /**
     * class for displaying the border layout of the game
     * 
     */
    private class Layout extends JComponent{
        private double sX, sY, width, height, sW, sH;
        private AffineTransform transform;
        private BufferedImage layout;
        /**
         * constructor for the layout object 
         * 
         * @param x x-coordinate for this layout object
         * @param y y-coordinate for this layout object
         * @param w width for this layout object
         * @param h height for this layout object
         */
        public Layout(double x, double y, double w, double h){
            sX = x;
            sY = y;
            width = w;
            height = h;
            sW = w/2459;
            sH = h/1297;
            transform = new AffineTransform(sW,0,0,sH,sX,sY);
            try{
                File layoutFile = new File("image_data/layout.png");
                layout = ImageIO.read(layoutFile);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
        }

        /**
         * method for drawing the layout to the screen
         * 
         * @param g graphics object to draw this to
         */
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(layout,transform,this);
            g2d.dispose();
        }
    }
}
