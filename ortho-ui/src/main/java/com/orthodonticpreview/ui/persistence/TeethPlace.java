/**
 * *****************************************************************************
 * Copyright (c) 2012 Cesar Moreira.
 *
 * This file is part of Orthodontic Preview.
 *
 * Orthodontic Preview is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Orthodontic Preview is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Orthodontic Preview. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.orthodonticpreview.ui.persistence;

import com.orthodonticpreview.datamodel.VectorMath;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import org.weasis.core.ui.graphic.Graphic;

/**
 * A place unit on TeethTable.
 *
 * Always check for backward compatibility when making changes on this class.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 16 Oct.
 */
public class TeethPlace implements Serializable {

    /**
     * Serial as required by <code>Serializable</code>.
     */
    private static final long serialVersionUID = 7937109728433265864L;

    public static final String[] SUP_RIGHT = new String[]{
        "11", "12", "13", "14", "15", "16", "17"
    };
    public static final String[] SUP_LEFT = new String[]{
        "21", "22", "23", "24", "25", "26", "27"
    };

    public static final String[] SUP_PLACE_NAMES = new String[]{
        "27", "26", "25", "24", "23", "22", "21", "mid",
        "11", "12", "13", "14", "15", "16", "17"
    };

    public static final String[] INF_LEFT = new String[]{
        "31", "32", "33", "34", "35", "36", "37"
    };
    public static final String[] INF_RIGHT = new String[]{
        "41", "42", "43", "44", "45", "46", "47"
    };

    public static final String[] INF_PLACE_NAMES = new String[]{
        "37", "36", "35", "34", "33", "32", "31", "mid",
        "41", "42", "43", "44", "45", "46", "47"
    };

    private String place;
    private double placeLength;
    private Point2D centerPoint;
    private double mmLength;

    /**
     * Corresponding LenghtVector on viewer.
     */
    private transient Graphic vector;

    /**
     * Corresponding ForceVector on viewer.
     */
    private transient Graphic force;

    public TeethPlace() {
    }

    public TeethPlace(String placeName) {
        //tableModel = model;
        place = placeName;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String string) {
        place = string;
    }

    /**
     * @return the placeLength
     */
    public double getPlaceLength() {
        return placeLength;
    }

    /**
     * @param placeLength the placeLength to set
     */
    public void setPlaceLength(double length) {
        placeLength = length;
    }

    public Point2D getCenter() {
        return centerPoint;
    }

    public void setCenter(Point2D point) {
        centerPoint = point;
    }

    public Graphic getLinkedVector() {
        return vector;
    }

    public void setLinkedVector(Graphic newVector, double scale) {
        vector = newVector;
        if (vector == null) {
            placeLength = 0;
            if (scale > 0 && mmLength > 0) {
                placeLength = mmLength / scale;
            }
        }
    }

    /**
     * The middle poind tor linked line, if linked line exists.
     *
     * @return The middle poind tor linked line,, ore null.
     */
    public Point2D getMiddleOfLinePoint() {
        if (vector != null && vector.getShape() instanceof Line2D) {
            return VectorMath.getMiddlePoint(((Line2D) vector.getShape()));
        }
        return null;
    }

    /**
     * @return the force
     */
    public Graphic getForceLinkedVec() {
        return force;
    }

    /**
     * @param force the force to set
     */
    public void setForceLinkedVec(Graphic forceVec) {
        force = forceVec;
    }

    public double getForceVectorLen() {
        if (force != null) {
            Shape shape = force.getShape();
            if (shape instanceof Line2D) {
                Line2D line = (Line2D) shape;
                return line.getP1().distance(line.getP2());
            }
        }
        return 0;
    }

    /**
     * @return the mmLength
     */
    public double getMmLength() {
        return mmLength;
    }

    /**
     * @param mmLength the mmLength to set
     */
    public void setMmLength(double length, double scale) {
        mmLength = length;

        if (vector == null && scale > 0) {
            setPlaceLength(mmLength / scale);
        }
    }
}
