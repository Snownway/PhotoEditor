package com.photoeditor;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Line {
    private final List<Point> points;
    private final Color color;
    private final double width;

    public Line(Color color, double width) {
        this.points = new ArrayList<>();
        this.color = color;
        this.width = width;
    }

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    public List<Point> getPoints() {
        return points;
    }

    public Color getColor() {
        return color;
    }

    public double getWidth() {
        return width;
    }

    public record Point(double x, double y) {
    }
}