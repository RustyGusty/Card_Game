package BaseGame.Rectangles;

import BaseGame.App;

public class Rectangle {
    public static App app;

    public float width;
    public float height;
    public float xCenter;
    public float yCenter;

    public float xRight;
    public float yBot;
    public float xLeft;
    public float yTop;

    public Rectangle(float hFactor, float vFactor, float width, float height){
        this(hFactor, vFactor);
        this.width = width;
        this.height = height;
        calculateRectangle();
    }
    
    public Rectangle(float hFactor, float vFactor) {
        this((int) (app.displayWidth * hFactor), (int) (app.displayHeight * vFactor));
    }
    public Rectangle(int xCenter, int yCenter, float width, float height) {
        this(xCenter, yCenter);
        this.width = width;
        this.height = height;
        calculateRectangle();
    }

    public Rectangle(int xCenter, int yCenter) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
    }

    public boolean mouseInRectangle(int mouseX, int mouseY) {
        return (mouseX <= xRight && mouseX >= xLeft) && (mouseY <= yBot && mouseY >= yTop);
    }

    public void calculateRectangle() {
        xLeft = xCenter - width / 2;
        xRight = xCenter + width / 2;
        yTop = yCenter - height / 2;
        yBot = yCenter + height / 2;
    }

    public void draw() {
        app.rect(xLeft, yTop, width, height);
    }

    public void updatePosition(Rectangle boundingRectangle) {
        updatePosition(xCenter, yCenter, boundingRectangle);
    }
    
    public void updatePosition(float newXCenter, float newYCenter, Rectangle boundingRectangle){
        updateXPosition(newXCenter, boundingRectangle.xLeft, boundingRectangle.xRight);
        updateYPosition(newYCenter, boundingRectangle.yTop, boundingRectangle.yBot);
    }

    public void updateYPosition(float newYCenter, float topBound, float botBound) {
        yCenter = newYCenter;
        calculateRectangle();
        if(yTop < topBound)
            yCenter += (topBound - yTop);
        else {
            if (yBot > botBound)
                yCenter -= (yBot - botBound);
        }
        calculateRectangle();
    }

    public void updateXPosition(float newXCenter, float leftBound, float rightBound) {
        xCenter = newXCenter;
        calculateRectangle();
        if(xLeft < leftBound)
            xCenter += (leftBound - xLeft);
        else if (xRight > rightBound)
            xCenter -= (xRight - rightBound); 
        calculateRectangle();
    }
}
