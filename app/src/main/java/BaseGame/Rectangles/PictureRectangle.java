package BaseGame.Rectangles;

import processing.core.PImage;

public class PictureRectangle extends Rectangle {
    private PImage img;

    public PictureRectangle(float hFactor, float vFactor, float width, float height, String filename) {
        this((int) (hFactor * app.displayWidth), (int) (vFactor * app.displayHeight), width, height, filename); 
    }

    public PictureRectangle(int xCenter, int yCenter, float width, float height, String filename){
        super(xCenter, yCenter, width, height);
        img = app.loadImage(filename);
    }

    @Override
    public void draw(){
        app.image(img, xLeft, yTop, width, height);
    }
}
