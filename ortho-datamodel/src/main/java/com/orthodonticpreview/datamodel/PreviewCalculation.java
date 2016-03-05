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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculation to get preview results.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 23 Aug.
 */
public class PreviewCalculation {

    /**
     * Class logger.
     */
    private static final Logger LOGGER
            = LoggerFactory.getLogger(PreviewCalculation.class);

    /**
     * Multiplyer to extend F1.
     */
    protected static final int MULTIPLIER = 5;
    /**
     * Turn angle.
     */
    protected static final int TURN = 30;
    /**
     * Margin for the result bounds.
     */
    protected static final int BOUND_BORDER = 40;

    /**
     * Series this calculation is about.
     */
    private PreviewCalculationModel dataSeries;

    /**
     * Creates a new instance for calculation.
     *
     * @param groupID groupID to calculate preview.
     */
    public PreviewCalculation(final PreviewCalculationModel groupID) {
        if (groupID == null) {
            throw new IllegalArgumentException("groupID cannot be null.");
        }
        dataSeries = groupID;
    }

    /**
     * Calculates and stores all relevant results at tags on dataSeries.
     *
     * @throws IllegalArgumentException if the series does not have a valid list
     * of vectors on TagO.FORCE_VECTOR_UNITS.
     */
    public void storeResults() {

        List<Line2D> forceList = dataSeries.getVectorUnits();
        if (forceList != null) {
            if (forceList.isEmpty()) {
                throw new IllegalArgumentException(
                        "Force vector units list cannot be empty.");
            }

            if (forceList.get(0) instanceof Line2D) {

                final Line2D result = VectorMath.sum(forceList);

                final Point2D auxOrigin = getAuxOriginPoint(result);

                Line2D f1 = (Line2D) (forceList).get(0);
                Line2D firstS1 = placeFS1(f1, auxOrigin);
                //System.out.println("f1 = " + VectorMath.lineToString(f1));

                Point2D pointI = getPointI(forceList, auxOrigin, firstS1);
                LOGGER.info("pointI = " + pointI);

                VectorMath.move(result,
                        pointI.getX() - result.getX2(),
                        pointI.getY() - result.getY2());
                //dataSeries.setTag(TagO.RESULTANT, result);
                LOGGER.info("moved resultant = "
                        + VectorMath.lineToString(result));

                //Girar 30 graus as linhas F
                final List<Line2D> debug = new ArrayList<Line2D>();
                final List<Line2D> turnedFs = getTurnedLinesList(
                        forceList, TURN);
                //localizar turnedS1:
                final Line2D result2 = VectorMath.sum(turnedFs);

                final Point2D auxOrigin2 = getAuxOriginPoint(result2);
                f1 = (Line2D) (turnedFs).get(0);
                debug.add(f1);
                firstS1 = placeFS1(f1, auxOrigin2);
                debug.add(firstS1);

                pointI = getPointI(turnedFs, auxOrigin2, firstS1);

                VectorMath.move(result2,
                        pointI.getX() - result2.getX2(),
                        pointI.getY() - result2.getY2());
                debug.add(result2);
                dataSeries.setDebugVectorUnits(debug);
                final Point2D pointGr = VectorMath.getIntersectionPoint(
                        result, result2);
                dataSeries.setPointGr(pointGr);
                LOGGER.info("GR = " + pointGr);

                //replace result to start on POINT_GR
                // (requisito do relatorio: #1020)
                VectorMath.move(result,
                        pointGr.getX() - result.getX1(),
                        pointGr.getY() - result.getY1());
                dataSeries.setResultant(result);

                storeBounds();

            } else {
                throw new IllegalArgumentException(
                        "Series needs to have valid lines on"
                        + " TagO.FORCE_VECTOR_UNITS to calculate.");
            }
        } else {
            throw new IllegalArgumentException(
                    "Series needs to have a valid TagO.FORCE_VECTOR_UNITS"
                    + " to calculate.");
        }
    }

    /**
     * Calcula o ponto das forças auxiliares. Distante 1/2 resultante da
     * resultante em um angulo de 90.
     *
     * @param result Resultanto calculada a partir dos vetores originais.
     * @return Ponto de origem para os vetores S
     */
    protected static Point2D getAuxOriginPoint(final Line2D result) {
        //ponto de origem das forças auxiliares:
        //posiçao: distante 1/2 resultante da resultante em um angulo de 90
        final Point2D middle = VectorMath.getMiddlePoint(result);
        final Point2D auxOrigin = new Point2D.Double(
                middle.getX() + (result.getY1() - middle.getY()),
                middle.getY() - (result.getX1() - middle.getX()));
        return auxOrigin;
    }

    /**
     * Posiciona a primeira reta S, transferida para o ultimo ponto da F1, sendo
     * esta estendida [MULTIPLIER]X.
     *
     * @param f1 vetor F1.
     * @param auxOrigin Ponto de origem das forças auxiliares (S).
     * @return o vetor S1 transferido.
     */
    protected static Line2D placeFS1(
            final Line2D f1, final Point2D auxOrigin) {
        //s1 - do pto auxOringin até o inicio da força 1,
        final Line2D s1 = new Line2D.Double(auxOrigin.getX(), auxOrigin.getY(),
                f1.getX1(), f1.getY1());
        //transfere o s1 para: pto final no prolongamento de f1
        final Line2D mf1 = VectorMath.multiply(f1, MULTIPLIER);
        //f1.dragOut(4); //pto inicial == f1.pto2
        //mover s1(dist entre f1.pto2 e s1.pto2)
        VectorMath.move(s1, mf1.getX2() - s1.getX2(), mf1.getY2() - s1.getY2());
        return s1;
    }

    /**
     * Calculates pointI.
     *
     * @param forceList list of force polygon.
     * @param auxOrigin origin point for all S-vectors.
     * @param firstS1 the first S-vector.
     * @return the point I.
     */
    protected static Point2D getPointI(final List forceList,
            final Point2D auxOrigin, final Line2D firstS1) {

        final List<Point2D> lastSPoits
                = VectorMath.forcePoligonPoints(forceList);
        //s2 do pto original até o inicio da força 2
        Point2D lastIntersection
                = new Point2D.Double(firstS1.getX2(), firstS1.getY2());

        //loop para chegar à interseccao do s12 com f12
        for (int index = 1; index < forceList.size(); index++) {
            final Line2D f2 = (Line2D) forceList.get(index);
            lastIntersection = findSxIntersection(f2,
                    auxOrigin, lastSPoits.get(index), lastIntersection);
        }

        final Line2D firstS13 = placeFS13(lastSPoits.get(lastSPoits.size() - 1),
                auxOrigin, lastIntersection);
        //System.out.println("S13 = " + VectorMath.lineToString(firstS13));

        //calculo da interseccao entre s13 e s1 ou prolongamentos = pto I
        return VectorMath.getIntersectionPoint(firstS1, firstS13);
    }

    /**
     * Finds the intersection between one vector S and de next vector F, after
     * its own placement.
     *
     * @param nextF the next vector F
     * @param auxOrigin origin point for all generated S.
     * @param auxEnd end point for this S vector (when on the polygon).
     * @param lastIntersection will be the end point for transfered S.
     * @return Intersection tu use as end point for next S.
     */
    protected static Point2D findSxIntersection(final Line2D nextF,
            final Point2D auxOrigin, final Point2D auxEnd,
            final Point2D lastIntersection) {

        final Line2D s2 = new Line2D.Double(auxOrigin.getX(), auxOrigin.getY(),
                auxEnd.getX(), auxEnd.getY());
        //transfere o s2 para: pto final no prolongamento de f1
        //mover s1(dist entre f1.pto2 e s1.pto2)
        VectorMath.move(s2, lastIntersection.getX() - s2.getX2(),
                lastIntersection.getY() - s2.getY2());

        return VectorMath.getIntersectionPoint(s2, nextF);
    }

    /**
     * Place S13, with the end point on the last intersection calculated.
     *
     * @param lastSEndPt end point for this S vector (when on the polygon).
     * @param auxOrigin origin point for all generated S.
     * @param lastIntersection last calculated intersection.
     * @return the S13.
     */
    protected static Line2D placeFS13(final Point2D lastSEndPt,
            final Point2D auxOrigin, final Point2D lastIntersection) {
        final Line2D s13 = new Line2D.Double(auxOrigin.getX(), auxOrigin.getY(),
                lastSEndPt.getX(), lastSEndPt.getY());
        VectorMath.move(s13, lastIntersection.getX() - s13.getX2(),
                lastIntersection.getY() - s13.getY2());
        return s13;
    }

    /**
     * Turns all lines at list by a given angle.
     *
     * @param list original lines.
     * @param angle angle to use.
     * @return list of turned lines.
     */
    protected static List<Line2D> getTurnedLinesList(
            final List<Line2D> list, final int angle) {

        final List<Line2D> turned = new ArrayList<Line2D>();
        for (int index = 0; index < list.size(); index++) {
            final Line2D oringLine = list.get(index);
            final double originAngle = VectorMath.getAngle(oringLine);
            final double originLen
                    = oringLine.getP1().distance(oringLine.getP2());
            final Line2D turnedLine = VectorMath.getLine(
                    oringLine.getP1(), originLen, originAngle + angle);

            turned.add(turnedLine);
        }
        return turned;
    }

    /**
     * Bounds for the calculation area (to cut the result image later).
     */
    private void storeBounds() {
        final Rectangle bounds = new Rectangle();

        GeneralPath arc = dataSeries.getArcPath();
        if (arc != null) {
            bounds.setBounds(arc.getBounds());
        }

        Rectangle resultant = null;
        Line2D res = dataSeries.getResultant();
        if (res != null) {
            resultant = ((Line2D) res).getBounds();
            LOGGER.info("Resultant = " + resultant);
        }

        if (resultant != null) {
            if (resultant.x < bounds.x) {
                bounds.setBounds(resultant.x, bounds.y,
                        bounds.width + (bounds.x - resultant.x), bounds.height);
            }
            if (resultant.y < bounds.y) {
                bounds.setBounds(bounds.x, resultant.y,
                        bounds.width, bounds.height + (bounds.y - resultant.y));
            }
            if (resultant.x + resultant.width > bounds.x + bounds.width) {
                bounds.setBounds(resultant.x, bounds.y,
                        resultant.x + resultant.width - bounds.x,
                        bounds.height);
            }
            if (resultant.y + resultant.height > bounds.y + bounds.height) {
                bounds.setBounds(resultant.x, bounds.y,
                        bounds.width,
                        resultant.y + resultant.height - bounds.y);
            }
        }

        bounds.setBounds(bounds.x - BOUND_BORDER, bounds.y - BOUND_BORDER,
                bounds.width + (2 * BOUND_BORDER),
                bounds.height + (2 * BOUND_BORDER));

        LOGGER.info("result bounds = " + bounds);
        dataSeries.setResultBounds(bounds);
    }

}
