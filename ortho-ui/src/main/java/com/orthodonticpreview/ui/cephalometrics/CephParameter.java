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
package com.orthodonticpreview.ui.cephalometrics;

import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.internal.Messages;
import java.util.ArrayList;
import java.util.List;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;

/**
 * Cephalometric parameters implementation and static list of them.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 5 Nov
 */
public class CephParameter extends AbstractCephParameter {

    private final double[] concLimits;
    private final String[] contTexts;
    private MediaSeriesGroup owner;
    private final TagW tagW;
    private static List<CephParameter> list;

    public CephParameter(TagW tag, String name, double[] limits,
            String[] conclusions) {
        super(name);
        concLimits = limits.clone();
        contTexts = conclusions.clone();
        tagW = tag;
    }

    @Override
    public String getConclusion() {
        return getConclusionFor(value);
    }

    public String getConclusionFor(double oneValue) {
        String conclusion = "";
        if (concLimits.length == 2) {
            if (oneValue < concLimits[0]) {
                conclusion = contTexts[0];
            } else if (oneValue >= concLimits[0] && oneValue < concLimits[1]) {
                conclusion = contTexts[1];
            } else {
                conclusion = contTexts[2];
            }
        } else {
            throw new IllegalArgumentException(
                    "Limits cant be different than 2");
        }
        return conclusion;
    }

    public void setOwnerGroup(MediaSeriesGroup series) {
        owner = series;
        if (owner.containTagKey(tagW)) {
            double val = (Double) owner.getTagValue(tagW);
            field.setText(Double.toString(val));
        }
    }

    @Override
    public void setValue(double val) {
        if (val != value) {
            super.setValue(val);
            owner.setTag(tagW, value);
        }
    }

    /* ----------- STATIC UTILS ----------------------------------- */
    public static List<CephParameter> getCPList() {
        if (list == null) {
            ArrayList<CephParameter> arrayList = new ArrayList<CephParameter>();

            arrayList.add(new CephParameter(TagO.FAC_ANGLE,
                    Messages.getString("CephParameter.1"),
                    new double[]{87, 89}, new String[]{
                        Messages.getString("CephParameter.1c1"),
                        Messages.getString("CephParameter.1c2"),
                        Messages.getString("CephParameter.1c3")
                    }));
            arrayList.add(new CephParameter(TagO.CONV_ANGLE,
                    Messages.getString("CephParameter.2"),
                    new double[]{0, 1}, new String[]{
                        Messages.getString("CephParameter.2c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.2c3")
                    }));
            arrayList.add(new CephParameter(TagO.Y_ANGLE,
                    Messages.getString("CephParameter.3"),
                    new double[]{67, 68}, new String[]{
                        Messages.getString("CephParameter.3c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.3c3")
                    }));
            arrayList.add(new CephParameter(TagO.INTER_ANGLE,
                    Messages.getString("CephParameter.4"),
                    new double[]{121, 142}, new String[]{
                        Messages.getString("CephParameter.4c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.4c3")
                    }));
            arrayList.add(new CephParameter(TagO.SNA,
                    Messages.getString("CephParameter.5"),
                    new double[]{82, 83}, new String[]{
                        Messages.getString("CephParameter.5c1"),
                        Messages.getString("CephParameter.5c2"),
                        Messages.getString("CephParameter.5c3")
                    }));
            arrayList.add(new CephParameter(TagO.SNB,
                    Messages.getString("CephParameter.6"),
                    new double[]{80, 81}, new String[]{
                        Messages.getString("CephParameter.6c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.6c3")
                    }));
            arrayList.add(new CephParameter(TagO.ANB,
                    Messages.getString("CephParameter.7"),
                    new double[]{2, 3}, new String[]{
                        Messages.getString("CephParameter.7c1"),
                        Messages.getString("CephParameter.7c2"),
                        Messages.getString("CephParameter.7c3")
                    }));
            arrayList.add(new CephParameter(TagO.DOT_NS,
                    Messages.getString("CephParameter.8"),
                    new double[]{103, 104}, new String[]{
                        Messages.getString("CephParameter.8c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.8c3")
                    }));
            arrayList.add(new CephParameter(TagO.DOT_NA,
                    Messages.getString("CephParameter.9"),
                    new double[]{22, 23}, new String[]{
                        Messages.getString("CephParameter.8c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.8c3")
                    }));
            arrayList.add(new CephParameter(TagO.DOT_NB,
                    Messages.getString("CephParameter.10"),
                    new double[]{25, 26}, new String[]{
                        Messages.getString("CephParameter.8c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.8c3")
                    }));
            arrayList.add(new CephParameter(TagO.HIF_NA,
                    Messages.getString("CephParameter.11"),
                    new double[]{4, 5}, new String[]{
                        Messages.getString("CephParameter.11c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.11c3")
                    }));
            arrayList.add(new CephParameter(TagO.HIF_NB,
                    Messages.getString("CephParameter.12"),
                    new double[]{4, 5}, new String[]{
                        Messages.getString("CephParameter.11c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.11c3")
                    }));
            arrayList.add(new CephParameter(TagO.ORBIT,
                    Messages.getString("CephParameter.13"),
                    new double[]{5, 6}, new String[]{
                        Messages.getString("CephParameter.11c1"),
                        Messages.getString("CephParameter.2c2"),
                        Messages.getString("CephParameter.11c3")
                    }));
            list = arrayList;
        }

        return list;
    }

    public static CephParameter getCephParByTag(final TagW tag) {
        for (CephParameter cephParameter : getCPList()) {
            if (cephParameter.tagW.equals(tag)) {
                return cephParameter;
            }
        }
        return null;
    }

}
