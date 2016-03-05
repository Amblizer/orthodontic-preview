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
package com.orthodonticpreview.ui.explorer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import javax.swing.JComboBox;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.ui.util.ArrayListComboBoxModel;

/**
 * Patient Combo box.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public class GroupComboBox extends JComboBox {

    /**
     * Parent explorer.
     */
    private final OrthoExplorerView explorer;

    /**
     * Patient Comparator.
     */
    private static final Comparator ALPHABETIC_COMPARATOR = new Comparator() {

        @Override
        public int compare(Object o1, Object o2) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    };

    /**
     * model for comboPatient.
     */
    private final ArrayListComboBoxModel groupModel
            = new ArrayListComboBoxModel() {

                @Override
                public void addElement(Object anObject) {
                    int index = binarySearch(anObject, ALPHABETIC_COMPARATOR);
                    if (index < 0) {
                        super.insertElementAt(anObject, -(index + 1));
                    } else {
                        super.insertElementAt(anObject, index);
                    }
                }
            };

    public final transient ItemListener groupChangeListener
            = new ItemListener() {

                @Override
                public void itemStateChanged(final ItemEvent event) {
                    groupChanged(event);
                }
            };

    public GroupComboBox(OrthoExplorerView explorerView) {
        explorer = explorerView;
        setModel(groupModel);
        addItemListener(groupChangeListener);
    }

    public void groupChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            Object item = groupModel.getSelectedItem();
            if (item instanceof MediaSeriesGroup) {
                MediaSeriesGroup group = (MediaSeriesGroup) item;
                explorer.selectPatient(group);
            } else if (item != null) {
                explorer.selectPatient(null);
            }
            revalidate();
            repaint();
        }
    }

    /**
     * Adds a patient (and selects it).
     *
     * @param group patient to add.
     */
    public void addGroup(final MediaSeriesGroup group) {
        groupModel.addElement(group);
        groupModel.setSelectedItem(group);
    }

    public void removeGroup(final MediaSeriesGroup group) {
        groupModel.removeElement(group);
        revalidate();
        repaint();
    }

}
