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
package com.orthodonticpreview.datamodel;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Math opperations to vectors (using Line2D to represent a vector).
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012 aug, 6
 */
public final class VectorMath {

    /**
     * Empty private constuctor.
     */
    private VectorMath() {
    }

    /**
     * Adds two lines.
     *
     * @param line1 one line.
     * @param line2 other line.
     * @return Sum of the given lines.
     */
    public static Line2D sum(final Line2D line1, final Line2D line2) {
        final double resX
                = line1.getX1() + getXDistance(line2) + getXDistance(line1);
        final double resY
                = line1.getY1() + getYDistance(line2) + getYDistance(line1);
        return new Line2D.Double(line1.getX1(), line1.getY1(), resX, resY);
    }

    /**
     * Calculates the X distance from point1 to point2 of a line.
     *
     * @param line one line.
     * @return the X distance.
     */
    public static double getXDistance(final Line2D line) {
        return line.getX2() - line.getX1();
    }

    /**
     * Calculates the Y distance from point1 to point2 of a line.
     *
     * @param line one line.
     * @return the Y distance.
     */
    public static double getYDistance(final Line2D line) {
        return line.getY2() - line.getY1();
    }

    /**
     * Adds a list of lines.
     *
     * @param list list with lines do add.
     * @return sum of all lines on list.
     */
    public static Line2D sum(final List<Line2D> list) {

        Line2D store = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            store = sum(store, list.get(i));
        }
        return store;
    }

    /**
     * Get the middle poins of a line.
     *
     * @param line one line.
     * @return the middle point.
     */
    public static Point2D getMiddlePoint(final Line2D line) {
        return new Point2D.Double(
                line.getX1() + ((getXDistance(line)) / 2),
                line.getY1() + ((getYDistance(line)) / 2));
    }

    /**
     * Prints a line as string (util for debug and log).
     *
     * @param line one line
     * @return string with the two point locations.
     */
    public static String lineToString(final Line2D line) {
        return "Line (" + line.getX1() + ", " + line.getY1() + ", "
                + line.getX2() + ", " + line.getY2() + ")";
    }

    /**
     * Multiplies a vector (line) by a scalar. Increasis to the point2
     * direction.
     *
     * @param line one line
     * @param multiplier the multiplier
     * @return the multiplied line.
     */
    public static Line2D multiply(final Line2D line, final double multiplier) {

        return new Line2D.Double(line.getX1(), line.getY1(),
                line.getX1() + (getXDistance(line)) * multiplier,
                line.getY1() + (getYDistance(line)) * multiplier);
    }

    /**
     * Moves a line. The lenght and direction are kept the same, just the place
     * changes.
     *
     * @param line one line.
     * @param xDist dist to move on X.
     * @param yDist dist to move on Y.
     */
    public static void move(final Line2D line,
            final double xDist, final double yDist) {

        line.setLine(line.getX1() + xDist, line.getY1() + yDist,
                line.getX2() + xDist, line.getY2() + yDist);
    }

    /**
     * Uses matrix algebra to calculate the point of intersection between two
     * lines.
     *
     * @param line1 one line
     * @param line2 other line.
     * @return the point where the lines intersect.
     */
    public static Point2D getIntersectionPoint(
            final Line2D line1, final Line2D line2) {

        final double detOne = det(
                line1.getX1(), line1.getY1(), line1.getX2(), line1.getY2());
        final double detTwo = det(
                line2.getX1(), line2.getY1(), line2.getX2(), line2.getY2());
        final double detDiv = det(line1.getX1() - line1.getX2(),
                line1.getY1() - line1.getY2(), line2.getX1() - line2.getX2(),
                line2.getY1() - line2.getY2());

        final double ptX = det(detOne, line1.getX1() - line1.getX2(),
                detTwo, line2.getX1() - line2.getX2()) / detDiv;
        final double ptY = det(detOne, line1.getY1() - line1.getY2(),
                detTwo, line2.getY1() - line2.getY2()) / detDiv;

        return new Point2D.Double(ptX, ptY);
    }

    /**
     * Calculates the determinant of a 2x2 matrix.
     *
     * @param topLeft the top left matrix value
     * @param topRight the top right matrix value
     * @param botLeft the bottom left matrix value
     * @param botRight the bottom right matrix value
     * @return the determinant.
     */
    private static double det(final double topLeft, final double topRight,
            final double botLeft, final double botRight) {
        return topLeft * botRight - topRight * botLeft;
    }

    /**
     * Calculates all points or start/end of lines when they are moved to make
     * the polygon.
     *
     * Calcula os pontos de uniao entre as linhas, quando colocadas uma
     * iniciando ao final da outra, na formação do poligono funicular.
     *
     * O número de pontos no resultado é sempre o número de linhas + 1.
     *
     * @param forceList lista contendo as linhas (vetores) de força.
     * @return lista com pontos de uniao.
     */
    public static List<Point2D> forcePoligonPoints(
            final List<Line2D> forceList) {
        final List<Point2D> list = new ArrayList<Point2D>();

        //pto 1 é o inicio do primeiro
        final Line2D line1 = forceList.get(0);
        list.add((Point2D) line1.getP1().clone());
        //pto 2 é o fim do primeiro
        list.add((Point2D) line1.getP2().clone());

        //pto 3 é o fim do segundo,
        //depois de mover o seu inicio para o fim do primeiro
        for (int index = 1; index < forceList.size(); index++) {
            //segunda linha
            final Line2D line = (Line2D) forceList.get(index).clone();
            VectorMath.move(line,
                    list.get(index).getX() - line.getX1(),
                    list.get(index).getY() - line.getY1());
            list.add((Point2D) line.getP2().clone());
        }
        return list;
    }

    /**
     * Calculates a line based on start point, lenght and angle.
     *
     * @param initPoint first point.
     * @param length lenght for the line.
     * @param angle the angle.
     * @return the line.
     */
    public static Line2D getLine(final Point2D initPoint,
            final double length, final double angle) {
        final double rad = angle * (Math.PI / 180);
        final Point2D endPoint = new Point2D.Double(
                initPoint.getX() + (length * Math.cos(rad)),
                initPoint.getY() + (-length * Math.sin(rad)));

        return new Line2D.Double(initPoint, endPoint);
    }

    /**
     * Calcula o angulo de uma linha, usando um sistema em que o zero fica a
     * direita e sentido anti-horario.
     *
     * @param line any line
     * @return angle
     */
    public static double getAngle(final Line2D line) {
        double angle = -Math.toDegrees(
                Math.atan2(line.getY2() - line.getY1(),
                        line.getX2() - line.getX1()));
        if (angle < 0) {
            angle = angle + 360;
        }
        return angle;
    }

    /**
     * @param line A line.
     * @return the point more at right on viewer.
     */
    public static Point2D rightPoint(final Line2D line) {
        if (line.getX1() > line.getX2()) {
            return line.getP1();
        }
        return line.getP2();
    }

    /**
     * @param line A line.
     * @return the point more at left on viewer.
     */
    public static Point2D leftPoint(final Line2D line) {
        if (line.getX1() <= line.getX2()) {
            return line.getP1();
        }
        return line.getP2();
    }

    /**
     * @param line A line.
     * @return The lower point (on viewer) of the line.
     */
    public static Point2D lowerPoint(final Line2D line) {
        if (line.getY1() > line.getY2()) {
            return line.getP1();
        } else {
            return line.getP2();
        }
    }

    /**
     * @param line A line.
     * @return The higher point (on viewer) of the line.
     */
    public static Point2D upperPoint(final Line2D line) {
        if (line.getY1() > line.getY2()) {
            return line.getP2();
        } else {
            return line.getP1();
        }
    }

    /**
     * Calculates coordX by circunference formula, knowing coordY and distance,
     * and assuming center(0,0).
     *
     * Used to "twist" a line.
     *
     * @param radius distance.
     * @param knownCoord The coordinate coordinate.
     * @return X coordinate.
     */
    public static double circumferenceInverseCoorden(final double radius,
            final double knownCoord) {
        //http://pt.wikipedia.org/wiki/Circunfer%C3%AAncia

        final double radPow = Math.pow(radius, 2);
        final double knownComp = Math.pow(knownCoord, 2);
         // (x-a)^2 + yComp = radPow
        // x = rad(radPow - yComp) + a

        //dois resultados possíveis: center- e center+
        return Math.sqrt(radPow - knownComp);
    }

}
