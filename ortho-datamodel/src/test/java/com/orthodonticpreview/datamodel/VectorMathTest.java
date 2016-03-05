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
import java.util.List;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author gabriela
 */
public class VectorMathTest {

    public VectorMathTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of sum method, of class VectorMath.
     */
    @Test
    public void testSum_Line2D_Line2D() {
        System.out.println("sum");
        Line2D v1 = new Line2D.Double(10, 10, 10, 7);
        Line2D v2 = new Line2D.Double(10, 10, 8, 7);
        Line2D expResult = new Line2D.Double(10, 10, 8, 4);
        Line2D result = VectorMath.sum(v1, v2);
        assertEquals(expResult.getP1(), result.getP1());
        assertEquals(expResult.getP2(), result.getP2());
    }

    /**
     * Test of getXDistance method, of class VectorMath.
     */
    @Test
    public void testGetXDistance() {
        System.out.println("getXDistance");
        Line2D line = new Line2D.Double(-2, 10, 8, 7);
        double expResult = 10.0;
        double result = VectorMath.getXDistance(line);
        assertEquals(expResult, result, 0.0);

    }

    /**
     * Test of getYDistance method, of class VectorMath.
     */
    @Test
    public void testGetYDistance() {
        System.out.println("getYDistance");
        Line2D line = new Line2D.Double(-2, 10, 8, 7);
        double expResult = -3.0;
        double result = VectorMath.getYDistance(line);
        assertEquals(expResult, result, 0.0);

    }

    /**
     * Test of sum method, of class VectorMath.
     */
    @Test
    public void testSum_List() {
        System.out.println("sum");

        List<Line2D> list = new ArrayList<Line2D>();
        list.add(new Line2D.Double(247.31, 690.76, 205.51, 647.27));
        list.add(new Line2D.Double(271.03, 613.94, 217.94, 570.45));
        list.add(new Line2D.Double(282.89, 556.90, 234.88, 515.10));
        list.add(new Line2D.Double(304.36, 498.16, 264.82, 462.57));

        Line2D result = VectorMath.sum(list);
        Line2D expResult = new Line2D.Double(247.31, 690.76, 64.87, 526.39);
        assertEquals(expResult.getP1(), result.getP1());
        assertEquals(expResult.getP2(), result.getP2());
    }

    /**
     * Test of getMiddlePoint method, of class VectorMath.
     */
    @Test
    public void testGetMiddlePoint() {
        System.out.println("getMiddlePoint");
        Line2D line = new Line2D.Double(80, 100, 90, 60);
        Point2D expResult = new Point2D.Double(85, 80);
        Point2D result = VectorMath.getMiddlePoint(line);
        assertEquals(expResult, result);
    }

    /**
     * Test of multiply method, of class VectorMath.
     */
    @Test
    public void testMultiply() {
        System.out.println("multiply");
        Line2D line = new Line2D.Double(80, 100, 90, 60);
        double multiplier = 2.0;
        Line2D expResult = new Line2D.Double(80, 100, 100, 20);
        Line2D result = VectorMath.multiply(line, multiplier);
        assertEquals(expResult.getP1(), result.getP1());
        assertEquals(expResult.getP2(), result.getP2());

        System.out.println("dragOut2");
        result = VectorMath.multiply(result, 1.5);
        expResult = new Line2D.Double(80, 100, 110, -20);
        assertEquals(expResult.getP2(), result.getP2());
    }

    /**
     * Test of move method, of class VectorMath.
     */
    @Test
    public void testMove() {
        System.out.println("move");
        Line2D line = new Line2D.Double(2, 6, 6, 2);
        VectorMath.move(line, -2, 1);
        Line2D expResult = new Line2D.Double(0, 7, 4, 3);
        assertEquals(expResult.getP1(), line.getP1());
        assertEquals(expResult.getP2(), line.getP2());
    }

    /**
     * Test of getIntersectionPoint method, of class VectorMath.
     */
    @Test
    public void testGetIntersectionPoint() {
        System.out.println("getIntersectionPoint");
        Line2D line1 = new Line2D.Double(266.49, 406.66, 80.10, 516.8);
        Line2D line2 = new Line2D.Double(271.03, 613.94, 58.67, 439.98);
        Point2D result = VectorMath.getIntersectionPoint(line1, line2);

        Point2D expResult = new Point2D.Double(122.13, 491.96);
        assertEquals(expResult.getX(), result.getX(), 0.01);
        assertEquals(expResult.getY(), result.getY(), 0.01);
    }

    /**
     * Test of ForcePoligonPoints method, of class VectorMath.
     */
    @Test
    public void testForcePoligonPoints_List() {
        System.out.println("ForcePoliconPoints_List");

        List<Line2D> units = new ArrayList<Line2D>();
        units.add(new Line2D.Double(247.31, 690.76, 205.51, 647.27));
        units.add(new Line2D.Double(271.03, 613.94, 217.94, 570.45));
        units.add(new Line2D.Double(282.89, 556.90, 234.88, 515.10));
        units.add(new Line2D.Double(304.36, 498.16, 264.82, 462.57));
        units.add(new Line2D.Double(311.13, 451.28, 303.79, 425.30));

        List<Point2D> expPoints = new ArrayList<Point2D>();
        expPoints.add(new Point2D.Double(247.31, 690.76));
        expPoints.add(new Point2D.Double(205.51, 647.27));
        expPoints.add(new Point2D.Double(152.42, 603.78));
        expPoints.add(new Point2D.Double(104.41, 561.98));
        expPoints.add(new Point2D.Double(64.87, 526.39));
        expPoints.add(new Point2D.Double(57.53, 500.41));

        List<Point2D> resPoints = VectorMath.forcePoligonPoints(units);

        for (int i = 0; i < resPoints.size(); i++) {
            Point2D expResult = expPoints.get(i);
            Point2D result = resPoints.get(i);
            assertEquals(expResult.getX(), result.getX(), 0.01);
            assertEquals(expResult.getY(), result.getY(), 0.01);

        }
    }

    /**
     * Test of getLine method, of class VectorMath.
     */
    @Test
    public void testGetLine_Point2D_double_int() {
        System.out.println("getLine 0.0");
        Point2D initPoint = new Point2D.Double(0, 0);
        double length = 5.0;
        double angle = -90;
        Line2D expResult = new Line2D.Double(0, 0, 0, 5);

        Line2D result = VectorMath.getLine(initPoint, length, angle);
        assertEquals(expResult.getX1(), result.getX1(), 0.01);
        assertEquals(expResult.getY1(), result.getY1(), 0.01);
        assertEquals(expResult.getX2(), result.getX2(), 0.01);
        assertEquals(expResult.getY2(), result.getY2(), 0.01);

        System.out.println("getLine 2");
        initPoint = new Point2D.Double(518.6, 853.5);
        length = 146.6;
        angle = 148.9;//301.1;
        expResult = new Line2D.Double(518.6, 853.5, 393.07, 777.77);

        result = VectorMath.getLine(initPoint, length, angle);
        assertEquals(expResult.getX1(), result.getX1(), 0.01);
        assertEquals(expResult.getY1(), result.getY1(), 0.01);
        assertEquals(expResult.getX2(), result.getX2(), 0.01);
        assertEquals(expResult.getY2(), result.getY2(), 0.01);

        //testar soh o ponto final
        angle = 47.5;
        expResult = new Line2D.Double(518.6, 853.5, 617.64, 745.41);
        result = VectorMath.getLine(initPoint, length, angle);
        assertEquals(expResult.getX2(), result.getX2(), 0.01);
        assertEquals(expResult.getY2(), result.getY2(), 0.01);
    }

    /**
     * Test of getAngle method, of class VectorMath.
     */
    @Test
    public void testGetAngle_Line2D() {
        System.out.println("getAngle");
        Line2D line = new Line2D.Double(531.32, 693.90, 497.63, 665.82);
        double expResult = 140.19;

        double result = VectorMath.getAngle(line);
        assertEquals(expResult, result, 0.01);

    }

    /**
     * Test of CircumferenceInverseCoorden method, of class VectorMath.
     */
    @Test
    public void CircumferenceInverseCoorden_double_double() {
        System.out.println("CircumferenceCoordenX_double_double");

        Point2D center = new Point2D.Double(190, 92);
        Point2D left = new Point2D.Double(156, 99);
        double radius = center.distance(left);
        System.out.println("init dist = " + radius);

        double distY = left.getY() - center.getY() + 1;
        double distX = VectorMath.circumferenceInverseCoorden(radius, distY);

        double x = center.getX() - distX;
        Point2D newLeft = new Point2D.Double(x, center.getY() + distY);

        double newDist = center.distance(newLeft);
        assertEquals(radius, newDist, 0.01);

    }

}
