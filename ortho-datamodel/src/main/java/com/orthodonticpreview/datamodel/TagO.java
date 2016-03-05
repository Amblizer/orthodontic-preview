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

import org.weasis.core.api.media.data.TagW;

/**
 * Holds Orthodontic Preview special tags.
 *
 * Don't ever change the order of the tags - brakes java-persistence!
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012 aug, 6.
 */
public final class TagO {

    /**
     * Empty private constructor.
     */
    private TagO() {
    }

    /**
     * Force vectors tag.
     * 
     * Must store a list of Line2D wich represents the force vectors F1 to F12,
     * respecting that order.
     * Can have more or lass force vector, as the number of teeth can be
     * more or less in some cases.
     */
    public static final TagW FORCE_VECTOR_UNITS = new TagW(
            "Force Vector-Units", TagW.TagType.Object, 3);

    /**
     * Resultant of force vectors moved to cross I-point. Its the line that is
     * shown to the user.
     *
     * The first resultant, moved to the right place, wich will be shown to the
     * user.
     */
    public static final TagW RESULTANT = new TagW("Resultant of Force Units",
            TagW.TagType.Object, 3);

    /**
     * Point of conversion between the two resultants.
     */
    public static final TagW POINT_GR = new TagW("Point Gr",
            TagW.TagType.Object, 3);

    /**
     * Debug Graphics.
     */
    public static final TagW DEBUG = new TagW("Debug graphics",
            TagW.TagType.Object, 3);

    /**
     * Othrodontic arc (as placed by the user).
     */
    public static final TagW ARC = new TagW("Orthodontic Arc",
            TagW.TagType.Object, 3);

    /**
     * Bounds to crop image for report.
     */
    public static final TagW RESULT_BOUNDS = new TagW("Result draw boundaries",
            TagW.TagType.Object, 3);

    /**
     * Role of this series on preview study.
     *
     * Roles are: 'Maxilla', 'Mandible'. Only one serie can have each role.
     */
    public static final TagW STUDY_ROLE = new TagW("Study Role",
            TagW.TagType.String, 3);

    /**
     * Role of this series on preview study.
     *
     * Roles are: 'Calculation Image', 'Report Image'. Only one serie can have
     * each role.
     */
    public static final TagW SERIE_ROLE = new TagW("Series Role",
            TagW.TagType.String, 3);

    /**
     * Mid point of body.
     */
    public static final TagW MID_X = new TagW("Mid X", TagW.TagType.Double, 3);

    /**
     * Tendency or turning. None if position relative to Mid-line is inside
     * tolerance value.
     *
     * Values are: side of tendency turn. (not localized: left / right / none).
     */
    public static final TagW TURNING_TENDENCY = new TagW("Turning tendency",
            TagW.TagType.String, 3);

    /**
     * Frontal line to use for PROJECTION_TENCENCY calculation.
     */
    public static final TagW FRONT_LIMIT = new TagW(
            "Frontal limit", TagW.TagType.Double, 3);

    /**
     * Tendency of front pronection: boolean.
     */
    public static final TagW PROJECTION_TENDENCY = new TagW(
            "Projection tendency", TagW.TagType.Boolean, 3);

    /**
     * TeethTableModel object, stored to be recovered when image is closed or
     * loose focus to other image.
     */
    public static TagW TEETH_TABLE_MODEL = new TagW(
            "TeethTableModel", TagW.TagType.Object, 3);

    /**
     * Teeth place list to repopulate TeethTableModel after loading saved serie.
     */
    public static TagW TEETH_PLACE_LIST = new TagW(
            "TeethPlaceList", TagW.TagType.Object, 3);

    /**
     * List of GraphicPack s to rebuild graphics after loading saved serie.
     */
    public static TagW GRAPHIC_PACKS = new TagW(
            "Graphic_Packs", TagW.TagType.Object, 3);

    /**
     * Patient age (any string given by the user).
     */
    public static TagW PATIENT_AGE = new TagW(
            "Patient Age", TagW.TagType.String, 1);

    /**
     * Actions In View (not Serializable safe).
     */
    public static TagW ACTIONS_TAG
            = new TagW("ActionsInView", TagW.TagType.Object, 3);

    /**
     * Actions In View to save (only Serializable).
     */
    public static TagW ACTIONS_SAVE
            = new TagW("ActionsToSave", TagW.TagType.Object, 3);

     //// Cephalometrics tags ****************************
    public static TagW FAC_ANGLE = new TagW("FacAngle", TagW.TagType.Double);
    public static TagW CONV_ANGLE = new TagW("ConvAngle", TagW.TagType.Double);
    public static TagW Y_ANGLE = new TagW("YAngle", TagW.TagType.Double);
    public static TagW INTER_ANGLE = new TagW("InterAngle", TagW.TagType.Double);
    public static TagW SNA = new TagW("SNA", TagW.TagType.Double);
    public static TagW SNB = new TagW("SNB", TagW.TagType.Double);
    public static TagW ANB = new TagW("ANB", TagW.TagType.Double);
    public static TagW DOT_NS = new TagW("1.NS", TagW.TagType.Double);
    public static TagW DOT_NA = new TagW("1.NA", TagW.TagType.Double);
    public static TagW DOT_NB = new TagW("1.NB", TagW.TagType.Double);
    public static TagW HIF_NA = new TagW("1-NA", TagW.TagType.Double);
    public static TagW HIF_NB = new TagW("1-NB", TagW.TagType.Double);
    public static TagW ORBIT = new TagW("Orbit", TagW.TagType.Double);

     /// Med lines *************************
    public static TagW MED_LINE_SUP = new TagW(
            "SuperiorMedLine", TagW.TagType.Double);
    public static TagW MED_LINE_INF = new TagW(
            "InferiorMedLine", TagW.TagType.Double);

    /**
     * Scale: used to calculate tolerance for TURNING_TENDENCY.
     */
    public static final TagW VECTORS_SCALE = new TagW("Vectors Scale",
            TagW.TagType.Double, 3);
}
