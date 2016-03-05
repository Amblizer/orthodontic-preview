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

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 23 Aug.
 */
public class PreviewCalculationTest {

    private static List<Line2D> units;

    public PreviewCalculationTest() {
    }

    @BeforeClass
    public static void setup() throws IOException, Exception {
        units = new ArrayList<Line2D>();

        units.add(new Line2D.Double(247.31, 690.76, 205.51, 647.27));
        units.add(new Line2D.Double(271.03, 613.94, 217.94, 570.45));
        units.add(new Line2D.Double(282.89, 556.90, 234.88, 515.10));
        units.add(new Line2D.Double(304.36, 498.16, 264.82, 462.57));
        units.add(new Line2D.Double(311.13, 451.28, 303.79, 425.30));
        units.add(new Line2D.Double(360.27, 449.58, 358.58, 398.19));
        units.add(new Line2D.Double(425.79, 445.06, 430.31, 395.36));
        units.add(new Line2D.Double(473.80, 438.29, 490.74, 415.13));
        units.add(new Line2D.Double(495.26, 480.08, 540.45, 455.23));
        units.add(new Line2D.Double(518.42, 529.79, 572.64, 501.55));
        units.add(new Line2D.Double(542.71, 585.70, 595.23, 563.11));
        units.add(new Line2D.Double(573.77, 643.88, 615.00, 637.10));
        
    }

    /**
     * Test of storeResults method, of class PreviewCalculation.
     */
    @Test
    public void testStoreResults() {
        System.out.println("storeResults");
        TestCalculationModel groupID = new TestCalculationModel(units);
        PreviewCalculation instance = new PreviewCalculation(groupID);
        instance.storeResults();

        System.out.println("storeResults - RESULTANT");
        Line2D result = groupID.getResultant();

        Line2D expResult = new Line2D.Double(360.12, 398.69, 383.27, 1.63);
        //método modificado para posicionar a resultante diferente...
        assertEquals(expResult.getX1(), result.getX1(), 0.01);
        assertEquals(expResult.getY1(), result.getY1(), 0.01);
        assertEquals(expResult.getX2(), result.getX2(), 0.01);
        assertEquals(expResult.getY2(), result.getY2(), 0.01);

        System.out.println("storeResults - POINT_GR");
        Point2D result2 = groupID.getPointGr();
        Point2D exp2 = new Point2D.Double(360.12, 398.69);
        System.out.println(result2);
        assertEquals(exp2.getX(), result2.getX(), 0.01);
        assertEquals(exp2.getY(), result2.getY(), 0.01);
    }

    /**
     * Test of getAuxOriginPoint method, of class PreviewCalculation.
     */
    @Test
    public void testGetAuxOriginPoint() {
        System.out.println("getAuxOriginPoint");
        Line2D line = new Line2D.Double(247.31, 690.76, 270.46, 293.69);
        Point2D expResult = new Point2D.Double(457.41, 503.80);
        Point2D result = PreviewCalculation.getAuxOriginPoint(line);

        assertEquals(expResult.getX(), result.getX(), 0.01);
        assertEquals(expResult.getY(), result.getY(), 0.01);
    }

    /**
     * Test of placeFS1 method, of class PreviewCalculation.
     */
    @Test
    public void testPlaceFS1() {
        System.out.println("placeFS1");
        Line2D f1 = new Line2D.Double(247.31, 690.76, 205.51, 647.27);
        Point2D auxOrigin = new Point2D.Double(457.41, 503.80);

        Line2D expResult = new Line2D.Double(248.40, 286.34, 38.30, 473.30);
        Line2D result = PreviewCalculation.placeFS1(f1, auxOrigin);

        assertEquals(expResult.getX1(), result.getX1(), 0.01);
        assertEquals(expResult.getY1(), result.getY1(), 0.01);
        assertEquals(expResult.getX2(), result.getX2(), 0.01);
        assertEquals(expResult.getY2(), result.getY2(), 0.01);
    }

    /**
     * Test of getPointI method, of class PreviewCalculation.
     */
    @Test
    public void testGetPointI() {
        System.out.println("getPointI");
        Point2D auxOrigin = new Point2D.Double(457.42, 503.8);
        Line2D firstS1 = new Line2D.Double(290.21, 329.84, 80.11, 516.8);

        Point2D expResult = new Point2D.Double(368.18, 260.46);
        Point2D result = PreviewCalculation.getPointI(units, auxOrigin, firstS1);

        assertEquals(expResult.getX(), result.getX(), 0.01);
        assertEquals(expResult.getY(), result.getY(), 0.01);
    }

    /**
     * Test of findSxIntersection method, of class PreviewCalculation.
     */
    @Test
    public void testFindSxIntersection() {
        System.out.println("findSxIntersection");
        Line2D nextF = new Line2D.Double(271.03, 613.94, 217.94, 570.45);
        Point2D auxOrigin = new Point2D.Double(457.41, 503.80);
        Point2D auxEnd = new Point2D.Double(205.51, 647.27);
        Point2D lastIntersection = new Point2D.Double(80.10, 516.8);
        Point2D expResult = new Point2D.Double(122.77, 492.49);

        Point2D result = PreviewCalculation.findSxIntersection(
                nextF, auxOrigin, auxEnd, lastIntersection);

        assertEquals(expResult.getX(), result.getX(), 0.01);
        assertEquals(expResult.getY(), result.getY(), 0.01);
    }

    /**
     * Test of FindSxIntersection method, of class OrthodonticModel.
     */
    @Test
    public void testPlaceFS13_Point2D_Point2D_Point2D() {
        System.out.println("placeFS13");
        Point2D lastSEndPt = new Point2D.Double(272.36, 301.04);
        Point2D auxOrigin = new Point2D.Double(454.75, 507.54);
        Point2D lastIntersection = new Point2D.Double(684.21, 617.97);

        Line2D expResult = new Line2D.Double(866.60, 824.47, 684.21, 617.97);
        Line2D result = PreviewCalculation.placeFS13(lastSEndPt, auxOrigin, lastIntersection);

        assertEquals(expResult.getX1(), result.getX1(), 0.01);
        assertEquals(expResult.getY1(), result.getY1(), 0.01);
        assertEquals(expResult.getX2(), result.getX2(), 0.01);
        assertEquals(expResult.getY2(), result.getY2(), 0.01);
    }

    /**
     * Test of getTurnedLinesList method, of class OrthodonticModel.
     */
    @Test
    public void testGetTurnedLinesList_list_int() {
        System.out.println("getTurnedLinesList");
        List<Line2D> list = new ArrayList<Line2D>();
        list.add(new Line2D.Double(290.21, 329.84, 80.11, 516.8));

        Line2D exp1 = new Line2D.Double(290.21, 329.84, 201.73, 596.80);

        List<Line2D> result = PreviewCalculation.getTurnedLinesList(list, 30);
        assertEquals(exp1.getX1(), result.get(0).getX1(), 0.01);
        assertEquals(exp1.getY1(), result.get(0).getY1(), 0.01);
        assertEquals(exp1.getX2(), result.get(0).getX2(), 0.01);
        assertEquals(exp1.getY2(), result.get(0).getY2(), 0.01);

    }

    private static class TestCalculationModel implements PreviewCalculationModel {
        
        List<Line2D> vectorUnits;
        List<Line2D> debugUnits;
        Point2D pointGr;
        private Line2D resultant;
        GeneralPath arc;
        Rectangle resultBounds;

        public TestCalculationModel(List<Line2D> units) {
            vectorUnits = units;
        }

        @Override
        public List<Line2D> getVectorUnits() {
            return vectorUnits;
        }

        @Override
        public void setDebugVectorUnits(List<Line2D> debug) {
            debugUnits = debug;
        }

        @Override
        public void setPointGr(Point2D point) {
            pointGr = point;
        }

        @Override
        public void setResultant(Line2D result) {
            resultant = result;
        }

        @Override
        public GeneralPath getArcPath() {
            return arc;
        }

        @Override
        public Line2D getResultant() {
            return resultant;
        }

        @Override
        public void setResultBounds(Rectangle bounds) {
            resultBounds = bounds;
        }

        @Override
        public Point2D getPointGr() {
            return pointGr;
        }
    }

}
