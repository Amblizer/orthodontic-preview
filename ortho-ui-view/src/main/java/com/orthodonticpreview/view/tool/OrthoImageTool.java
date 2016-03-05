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
package com.orthodonticpreview.view.tool;

import bibliothek.gui.dock.common.CLocation;
import com.orthodonticpreview.view.OrthoEventManager;
import com.orthodonticpreview.view.internal.Messages;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.weasis.base.viewer2d.dockable.ImageTool;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.gui.util.JSliderW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.graphic.Graphic;
import org.weasis.core.ui.graphic.model.AbstractLayerModel;

/**
 * Place image tools on a plugin tool, so they can be used even if user have
 * only left mouse button.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2013, 02 Jan
 */
public class OrthoImageTool extends PluginTool {

    /**
     * Tab button name.
     */
    public static final String BUTTON_NAME
            = Messages.getString("OrthoImageTool.Image");

    private final Border spaceY = BorderFactory.createEmptyBorder(10, 3, 0, 3);
    private final Font TIP_FONT = FontTools.getFont10();

    public OrthoImageTool(final String pluginName) {
        super(BUTTON_NAME, pluginName, PluginTool.Type.TOOL, 20);
        dockable.setTitleIcon(new ImageIcon(
                ImageTool.class.getResource("/icon/16x16/image.png")));
        setDockableWidth(290);
        init();

    }

    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(getTransformPanel());
        add(getMoveAndDrawPanel());

        final JPanel ghostPanel = new JPanel();
        ghostPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        ghostPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ghostPanel.setLayout(new GridBagLayout());
        add(ghostPanel);
    }

    public JPanel getTransformPanel() {
        final JPanel transform = new JPanel();
        transform.setAlignmentY(Component.TOP_ALIGNMENT);
        transform.setAlignmentX(Component.LEFT_ALIGNMENT);
        transform.setLayout(new BoxLayout(transform, BoxLayout.Y_AXIS));
        transform.setBorder(BorderFactory.createCompoundBorder(
                spaceY, new TitledBorder(null,
                        Messages.getString("OrthoImageTool.Image"),
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION)));
        ActionState zoomAction = OrthoEventManager.getInstance().getAction(
                ActionW.ZOOM);
        if (zoomAction instanceof SliderChangeListener) {
            final JSliderW zoomSlider = ((SliderChangeListener) zoomAction)
                    .createSlider(0, false);
            JMVUtils.setPreferredWidth(zoomSlider, 100);
            JLabel tip = new JLabel(
                    Messages.getString("OrthoImageTool.ZoomMouseTip"));
            tip.setFont(TIP_FONT);
            zoomSlider.getParent().add(tip);
            transform.add(zoomSlider.getParent());
        }
        ActionState rotateAction = OrthoEventManager.getInstance().getAction(
                ActionW.ROTATION);
        if (rotateAction instanceof SliderChangeListener) {
            final JSliderW rotationSlider = ((SliderChangeListener) rotateAction).createSlider(4, false);
            JMVUtils.setPreferredWidth(rotationSlider, 100);
            JLabel tip = new JLabel(
                    Messages.getString("OrthoImageTool.RotationMouseTip"));
            tip.setPreferredSize(new Dimension(250, 50));
            tip.setFont(TIP_FONT);
            rotationSlider.getParent().add(tip);
            transform.add(rotationSlider.getParent());
        }
        ActionState flipAction = OrthoEventManager.getInstance().getAction(
                ActionW.FLIP);
        if (flipAction instanceof ToggleButtonListener) {
            JPanel pane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            pane.add(((ToggleButtonListener) flipAction)
                    .createCheckBox(Messages.getString("OrthoImageTool.Flip")));
            transform.add(pane);
        }
        return transform;
    }

    /**
     * @return builds Move & Draw Panel.
     */
    private JPanel getMoveAndDrawPanel() {
        final JPanel panel = new JPanel();
        panel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                spaceY, new TitledBorder(null,
                        Messages.getString("OrthoImageTool.Move&Draw"))));

        final JLabel tip = new JLabel(
                Messages.getString("OrthoImageTool.MDMouseTip"));
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);
        tip.setFont(TIP_FONT);
        panel.add(tip);

        final JPanel movePanel = new JPanel();
        movePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        movePanel.setBorder(BorderFactory.createCompoundBorder(
                spaceY, new TitledBorder(null,
                        Messages.getString("OrthoImageTool.Move"))));
        movePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JButton moveButton = new JButton(new ImageIcon(
                PluginTool.class.getResource("/icon/22x22/pan.png")));
        moveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                OrthoEventManager.getInstance().setMouseLeftAction(
                        ActionW.PAN.cmd());

            }
        });
        movePanel.add(moveButton);
        panel.add(movePanel);

        final JPanel drawPanel = new JPanel();
        drawPanel.setBorder(BorderFactory.createCompoundBorder(
                spaceY, new TitledBorder(null,
                        Messages.getString("OrthoImageTool.Draw"))));
        drawPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (final Graphic graph : OrthoEventManager.graphicList) {
            JButton grButton = new JButton(graph.getIcon());
            grButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    OrthoEventManager.getInstance().setMouseLeftAction(
                            ActionW.MEASURE.cmd());
                    ActionState measure = OrthoEventManager.getInstance()
                            .getAction(ActionW.DRAW_MEASURE);
                    if (measure instanceof ComboItemListener) {
                        ((ComboItemListener) measure).setSelectedItem(graph);
                    }
                }
            });
            grButton.setToolTipText(graph.toString());
            drawPanel.add(grButton);
        }

        JButton jButtondelete = new JButton(new ImageIcon(
                PluginTool.class.getResource(
                        "/icon/22x22/none.png")));
        jButtondelete.setToolTipText(
                Messages.getString("OrthoImageTool.del"));
        jButtondelete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractLayerModel model = getCurrentLayerModel();
                if (model != null) {
                    if (model.getSelectedGraphics().isEmpty()) {
                        model.setSelectedGraphics(model.getAllGraphics());
                    }
                    model.deleteSelectedGraphics(true);
                }
            }
        });
        drawPanel.add(jButtondelete);
        panel.add(drawPanel);

        return panel;
    }

    /**
     * @return Layer model from selected ImageView.
     */
    protected AbstractLayerModel getCurrentLayerModel() {
        DefaultView2d view
                = OrthoEventManager.getInstance().getSelectedViewPane();
        if (view != null) {
            return view.getLayerModel();
        }
        return null;
    }

    @Override
    protected void changeToolWindowAnchor(CLocation clocation) {
    }

}
