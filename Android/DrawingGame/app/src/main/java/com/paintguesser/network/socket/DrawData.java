package com.paintguesser.network.socket;

import com.paintguesser.view.ui.Point;

import java.util.ArrayList;
import java.util.List;

public class DrawData {
    public final List<Point> points;
    public final float screenWidth;
    public final float screenHeight;

    public DrawData(List<Point> points, float screenWidth, float screenHeight) {
        this.points = points;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public DrawData (DrawData drawData) {
        this.points = new ArrayList<>(drawData.points);
        this.screenWidth = drawData.screenWidth;
        this.screenHeight = drawData.screenHeight;
    }
}
