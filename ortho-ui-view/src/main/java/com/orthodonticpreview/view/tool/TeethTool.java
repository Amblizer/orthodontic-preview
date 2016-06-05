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
import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.OrthodonticDataExtractor;
import com.orthodonticpreview.ui.explorer.OrthoExplorerView;
import com.orthodonticpreview.ui.persistence.TeethPlace;
import com.orthodonticpreview.view.OrthoEventManager;
import com.orthodonticpreview.view.OrthoView;
import com.orthodonticpreview.view.ViewContainer;
import com.orthodonticpreview.view.graphics.ArcGraphic;
import com.orthodonticpreview.view.graphics.DotGraphic;
import com.orthodonticpreview.view.graphics.VectorGraphic;
import com.orthodonticpreview.view.internal.Messages;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.graphic.Graphic;
import org.weasis.core.ui.graphic.MeasureItem;
import org.weasis.core.ui.graphic.model.GraphicsListener;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 16 Oct
 */
public class TeethTool extends PluginTool implements SeriesViewerListener,
        OrthodonticDataExtractor {

    public static final String BUTTON_NAME
            = Messages.getString("TeethTool.ButtName");

    /**
     * Icon.
     */
    public static final Icon ICON
            = new ImageIcon(PluginTool.class.getResource(
                            "/icon/22x22/measure.png"));

    private static final String MAXILLA = "Maxilla";
    private static final String MANDIBLE = "Mandible";

    private DefaultView2d selectedImagePane;

    /**
     * Data model of table (holds all data of this tool).
     */
    private TeethTableModel model;

    /**
     * Format for decimal numbers on scale label.
     */
    private final DecimalFormat scFormat = new DecimalFormat("#.#####");
    private JLabel scaleLable;
    private JTable table;

    /**
     * Listener to first get the line of scale.
     */
    private final GraphicsListener scaleLineListener = new GraphicsListener() {

        @Override
        public void handle(List<Graphic> selectedGraphics, ImageLayer layer) {

            if (isNewVector(selectedGraphics)) {
                VectorGraphic vector = (VectorGraphic) selectedGraphics.get(0);
                Line2D line = (Line2D) vector.getShape();
                double distance = line.getP1().distance(line.getP2());

                model.setScalePixelValue(distance);
                updateScaleLabel();
                vector.setLinkedOwner(
                        "scale", selectedImagePane.getLayerModel());
                vector.addPropertyChangeListener(graphicsChangeListener);

                //remove o listener depois de vincular o
                //grafico
                selectedImagePane.getLayerModel()
                        .removeGraphicSelectionListener(this);
            } else {
                setDrawingTool(OrthoEventManager.vectorGraphic);
            }
        }

        @Override
        public void updateMeasuredItems(List<MeasureItem> measureList) {
            // Empty
        }
    };

    /**
     * Listener to first get the FrontLine.
     */
    private final GraphicsListener frontLineListener = new GraphicsListener() {
        @Override
        public void handle(List<Graphic> selectedGraphics, ImageLayer layer) {
            if (isNewVector(selectedGraphics)) {
                VectorGraphic vector = (VectorGraphic) selectedGraphics.get(0);
                Line2D line = (Line2D) vector.getShape();
                double limit = Math.min(line.getY1(), line.getY2());

                MediaSeries series = selectedImagePane.getSeries();
                if (series != null) {
                    series.setTag(TagO.FRONT_LIMIT, limit);
                }
                vector.setLinkedOwner("frontLine",
                        selectedImagePane.getLayerModel());
                vector.addPropertyChangeListener(graphicsChangeListener);

                //remove o listener depois de vincular o
                //grafico
                selectedImagePane.getLayerModel()
                        .removeGraphicSelectionListener(this);

            } else {
                setDrawingTool(OrthoEventManager.vectorGraphic);
            }

        }

        @Override
        public void updateMeasuredItems(List<MeasureItem> measureList) {
            // Empty
        }
    };

    /**
     * Change listener for graphics and view changes. *
     */
    private final PropertyChangeListener graphicsChangeListener
            = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("bounds".equalsIgnoreCase(evt.getPropertyName())) {
                        if (evt.getSource() instanceof VectorGraphic
                        && evt.getNewValue() instanceof Line2D) {
                            Line2D line = (Line2D) evt.getNewValue();
                            String linkedOwner
                            = ((VectorGraphic) evt.getSource()).getLinkedOwner();
                            if ("scale".equals(linkedOwner)) {
                                model.setScalePixelValue(
                                        line.getP1().distance(line.getP2()));
                                updateScaleLabel();
                            } else if ("frontLine".equals(linkedOwner)) {
                                double limit = Math.min(line.getY1(), line.getY2());

                                MediaSeries series = selectedImagePane.getSeries();
                                if (series != null) {
                                    series.setTag(TagO.FRONT_LIMIT, limit);
                                }
                            }
                        }
                    }
                }
            };

    public TeethTool(String pluginName, Icon icon) {
        super(BUTTON_NAME, pluginName,
                PluginTool.Type.TOOL, 30);
        dockable.setTitleIcon(ICON);
        setDockableWidth(310);
    }

    private void initGui(MediaSeries series) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final JPanel main = new JPanel(new GridBagLayout());

        final GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.WEST;
        con.insets = new Insets(8, 2, 8, 2);
        con.weightx = 1;
        con.weighty = 1;
        con.gridx = con.gridy = 0;

        main.add(buildTopPanel(), con);

        if (selectedImagePane != null && series != null
                && OrthoExplorerView.CALC_IMAGE.equalsIgnoreCase(
                        (String) series.getTagValue(TagO.SERIE_ROLE))) {
            if (series.containTagKey(TagO.TEETH_TABLE_MODEL)) {
                Object tagValue = series.getTagValue(TagO.TEETH_TABLE_MODEL);
                if (tagValue instanceof TeethTableModel) {
                    model = (TeethTableModel) tagValue;
                }
            } else if (series.containTagKey(TagO.TEETH_PLACE_LIST)) {
                Object tagValue = series.getTagValue(TagO.TEETH_PLACE_LIST);
                if (tagValue instanceof List) {
                    List receivedList = (List) tagValue;
                    model = new TeethTableModel();
                    model.populateModel(receivedList, selectedImagePane);
                    series.setTag(TagO.TEETH_TABLE_MODEL, model);
                    series.setTag(TagO.TEETH_PLACE_LIST, model.getPlaceList());
                }
            } else {
                model = new TeethTableModel();
                model.populateModel(
                        getPlaceNamesList(series), selectedImagePane);
                series.setTag(TagO.TEETH_TABLE_MODEL, model);
                series.setTag(TagO.TEETH_PLACE_LIST, model.getPlaceList());
            }
        } else {
            //empty model if its not a calcSerie
            model = new TeethTableModel();
        }
        table = new JTable(model);
        table.setMaximumSize(new Dimension(270, 400));
        //Coluna de N
        table.getColumnModel().getColumn(TeethTableModel.N_COL).setMaxWidth(40);
        table.getColumnModel().getColumn(
                TeethTableModel.FORCEVEC_COL).setMaxWidth(60);

        //Coluna de comprimentos em pixel
        TableColumn col = table.getColumnModel().getColumn(
                TeethTableModel.LENPIX_COL);
        col.setCellEditor(new LenTeethTableCellEditor());
        col.setMaxWidth(60);

        //Coluna de centros
        col = table.getColumnModel().getColumn(TeethTableModel.CENTER_COL);
        col.setCellEditor(new CenterTeethTableCellEditor());
        col.setMaxWidth(80);

        col = table.getColumnModel().getColumn(TeethTableModel.MM_COL);
        col.setCellEditor(new DefaultCellEditor(new JTextField()));
        col.setMaxWidth(60);

        con.gridy = 1;
        main.add(new JScrollPane(table), con);

        model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                updateScaleLabel();
            }
        });

        final JScrollPane jScrollPane = new JScrollPane(main,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setSize(getWidth(), getHeight());
        add(jScrollPane);

        updateScaleLabel();

        validate();
    }

    private JPanel buildTopPanel() {
        GridBagLayout layout = new GridBagLayout();
        JPanel topPanel = new JPanel(layout);
        layout.columnWidths = new int[]{85, 85, 85};

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = gbc.gridy = 0;
        gbc.gridwidth = 3;

        JLabel labelScale = buildLabel("TeethTool.msgScale", 70);
        topPanel.add(labelScale, gbc);
        gbc.gridy++;
        topPanel.add(buildScalePanel(), gbc);

        JLabel label1 = buildLabel("TeethTool.msg1", 70);
        gbc.gridy++;
        topPanel.add(label1, gbc);

        JButton lineButton = new JButton(new ImageIcon(
                TeethTool.class.getResource("/icon/line.png")));
        lineButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //table.editCellAt(6, TeethTableModel.LENPIX_COL);
                OrthoEventManager.getInstance().setMouseLeftAction(
                        ActionW.MEASURE.cmd());
                setDrawingTool(OrthoEventManager.vectorGraphic);
                //remove se jah tiver adicionado!
                selectedImagePane.getLayerModel()
                        .removeGraphicSelectionListener(frontLineListener);

                selectedImagePane.getLayerModel()
                        .addGraphicSelectionListener(frontLineListener);
            }
        });

        gbc.gridy++;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        topPanel.add(lineButton, gbc);

        JLabel label2 = buildLabel("TeethTool.msg2", 70);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        topPanel.add(label2, gbc);

        JLabel label3 = buildLabel("TeethTool.msg3", 30);
        gbc.gridy++;
        topPanel.add(label3, gbc);

        JButton transferButton = new JButton(
                Messages.getString("TeethTool.TransferButton"));
        transferButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    //Twice to solve bug #1318
                    model.transferPlaces((OrthoView) selectedImagePane);
                    model.transferPlaces((OrthoView) selectedImagePane);
                } catch (TransferError ex) {
                    JOptionPane.showMessageDialog(selectedImagePane,
                            ex.getMessage(),
                            Messages.getString("TeethTool.TransferError"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        gbc.gridy++;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        topPanel.add(transferButton, gbc);

        JLabel label4 = buildLabel("TeethTool.msg4", 100);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        topPanel.add(label4, gbc);

        return topPanel;
    }

    private JLabel buildLabel(String msg, int height) {
        JLabel label = new JLabel("<html>"
                + Messages.getString(msg) + "</html>");
        label.setFont(FontTools.getFont11());
        label.setMinimumSize(new Dimension(250, height));
        label.setMaximumSize(new Dimension(250, height));
        label.setPreferredSize(new Dimension(250, height));
        return label;
    }

    private Component buildScalePanel() {
        JPanel scPanel = new JPanel();

        JButton lineButton = new JButton(new ImageIcon(
                TeethTool.class.getResource("/icon/line.png")));
        lineButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                OrthoEventManager.getInstance().setMouseLeftAction(
                        ActionW.MEASURE.cmd());
                setDrawingTool(OrthoEventManager.vectorGraphic);
                //remove se jah tiver adicionado!
                selectedImagePane.getLayerModel()
                        .removeGraphicSelectionListener(scaleLineListener);

                selectedImagePane.getLayerModel()
                        .addGraphicSelectionListener(scaleLineListener);
            }
        });
        scPanel.add(lineButton);
        final JTextField field = new JTextField(5);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                updateModel();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                updateModel();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                updateModel();
            }

            private void updateModel() {
                String text = field.getText();
                try {
                    double parseDouble = Double.parseDouble(text);
                    model.setScaleMmValue(parseDouble);
                    updateScaleLabel();
                } catch (NumberFormatException ex) {
                    //igonre
                }
            }
        });
        scPanel.add(field);

        scaleLable = new JLabel();
        scPanel.add(scaleLable);

        return scPanel;
    }

    @Override
    public void changingViewContentEvent(final SeriesViewerEvent event) {
//        System.out.println("changingSeries event = " + event.getEventType());
        if (event.getEventType().equals(SeriesViewerEvent.EVENT.SELECT_VIEW)) {

            MediaSeriesGroup groupID = event.getSeriesViewer().getGroupID();
            if (groupID instanceof MediaSeries) {
                MediaSeries series = (MediaSeries) groupID;
                if (selectedImagePane == null
                        || (selectedImagePane != null
                        && series != selectedImagePane.getSeries()
                        && event.getSeriesViewer() instanceof ViewContainer)) {

                    selectedImagePane = ((ViewContainer) event.getSeriesViewer()).getSelectedImagePane();
                    removeAll();
                    initGui(series);
                }

            }
        }
    }

    @Override
    public JButton getButton(int dataTypeButton) {
        JButton button = null;
        if (dataTypeButton == OrthodonticDataExtractor.ARC_TYPE_BUTTON) {
            for (Graphic graph : OrthoEventManager.graphicList) {
                if (graph instanceof ArcGraphic) {
                    button = new JButton(graph.getIcon());
                    button.addActionListener(
                            (ActionListener) OrthoEventManager.getInstance());
                    button.setActionCommand(((ArcGraphic) graph).toString());
                    button.setToolTipText(graph.toString());
                }
            }
        } else if (dataTypeButton
                == OrthodonticDataExtractor.FLIP_TYPE_BUTTON) {
            button = new JButton(new ImageIcon(
                    TeethTool.class.getResource("/icon/22x22/flip.png")));
            ActionState action
                    = OrthoEventManager.getInstance().getAction(ActionW.FLIP);
            if (action instanceof ToggleButtonListener) {
                ToggleButtonListener listener = (ToggleButtonListener) action;
                listener.registerActionState(button);
            }
        }
        return button;
    }

    @Override
    public List<Line2D> getForceVectorList() {
        return model.getForceVectorList();
    }

    @Override
    public GeneralPath getOrthodonticArc() {
        return model.getArc();
    }

    /**
     * Activates a given draing tool.
     *
     * @param graphicTool tool to activate.
     */
    private void setDrawingTool(Graphic graphicTool) {
        final OrthoEventManager eventManager = OrthoEventManager.getInstance();
        final ActionState action = eventManager.getAction(
                ActionW.DRAW_MEASURE);

        final ImageViewerPlugin<ImageElement> view
                = eventManager.getSelectedView2dContainer();
        final MouseActions mouseActions
                = eventManager.getMouseActions();
        if (isCommandActive(mouseActions, ActionW.MEASURE.cmd())) {
            mouseActions.setAction(MouseActions.LEFT, ActionW.MEASURE.cmd());
            if (view instanceof ViewContainer) {
                ((ViewContainer) view).setMouseActions(mouseActions);
            }
        }

        if (action instanceof ComboItemListener) {
            ComboItemListener measure = (ComboItemListener) action;
            measure.setSelectedItem(graphicTool);
            action.enableAction(true);
        }
    }

    private void updateScaleLabel() {
        if (model != null) {
            double averageScale = model.getScale();
            if (averageScale <= 0) {
                scaleLable.setText(Messages.getString("TeethTool.NoScale"));
            } else {
                scaleLable.setText(Messages.getString("TeethTool.Scale")
                        + " " + scFormat.format(averageScale));
            }
        }
    }

    @Override
    public double getMidX() {
        return model.getMidX();
    }

    private static String[] getPlaceNamesList(MediaSeries series) {
        String tagValue = (String) series.getTagValue(TagO.STUDY_ROLE);
        if (MAXILLA.equals(tagValue)) {
            return TeethPlace.SUP_PLACE_NAMES;
        } else if (MANDIBLE.equals(tagValue)) {
            return TeethPlace.INF_PLACE_NAMES;
        }
        throw new IllegalArgumentException("No STUDY_ROLE tag found.");
    }

    /**
     * Decides if selected grapfic is only one and is a new vector, not linked
     * to any model element.
     *
     * @param selectedGraphics List of selected graphics.
     * @return True it its a vector and its not linked.
     */
    public static boolean isNewVector(final List<Graphic> selectedGraphics) {
        if (selectedGraphics.size() == 1) {
            Graphic get = selectedGraphics.get(0);
            if (get instanceof VectorGraphic) {
                VectorGraphic vector = (VectorGraphic) get;
                if (vector.getLinkedOwner() == null) {
                    Shape shape = get.getShape();
                    if (shape instanceof Line2D) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public double getVectorsScale() {
        if (model != null) {
            return model.getScale();
        }
        return 0;
    }

    @Override
    protected void changeToolWindowAnchor(CLocation clocation) {
    }

    private boolean isCommandActive(MouseActions mouseActions, String cmd) {
        if (cmd != null) {
            if (mouseActions.getLeft().equals(cmd) || mouseActions.getMiddle().equals(cmd)
                    || mouseActions.getRight().equals(cmd)
                    || mouseActions.getWheel().equals(cmd)) {
                return true;
            }
        }
        return false;
    }


    /* ****************  Aux Classes ****************************************/
    private class LenTeethTableCellEditor extends AbstractCellEditor
            implements TableCellEditor, GraphicsListener {

        JComponent component = new JLabel(
                Messages.getString("TeethTool.drawLine"));
        private double distance;
        private Graphic graphReff;

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected,
                int rowIndex, int vColIndex) {
            graphReff = null;
            distance = 0;
            OrthoEventManager.getInstance().setMouseLeftAction(
                    ActionW.MEASURE.cmd());
            setDrawingTool(OrthoEventManager.vectorGraphic);
            selectedImagePane.getLayerModel().addGraphicSelectionListener(this);
            return component;
        }

        /**
         * Chamado quando component perde o foco!
         *
         * @return
         */
        @Override
        public Object getCellEditorValue() {
            if (distance != 0) {
                return graphReff;
            }
            return null;
        }

        @Override
        public void handle(List<Graphic> selectedGraphics, ImageLayer layer) {
            if (isNewVector(selectedGraphics)) {
                Graphic vector = selectedGraphics.get(0);
                Line2D line = (Line2D) vector.getShape();
                graphReff = selectedGraphics.get(0);
                //Line2D line = (Line2D) shape;
                distance = line.getP1().distance(line.getP2());
                fireEditingStopped();

                //remove se jah tiver adicionado!
                selectedImagePane.getLayerModel()
                        .removeGraphicSelectionListener(this);

            } else {
                setDrawingTool(OrthoEventManager.vectorGraphic);
            }

        }

        @Override
        public void updateMeasuredItems(List<MeasureItem> measureList) {
            // Empty
        }
    }

    private class CenterTeethTableCellEditor extends AbstractCellEditor
            implements TableCellEditor, GraphicsListener {

        JComponent component = new JLabel(
                Messages.getString("TeethTool.drawPoint"));
        private Graphic graphReff;

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected,
                int rowIndex, int vColIndex) {

            graphReff = null;
            OrthoEventManager.getInstance().setMouseLeftAction(
                    ActionW.MEASURE.cmd());
            setDrawingTool(OrthoEventManager.dotGraphic);
            selectedImagePane.getLayerModel().addGraphicSelectionListener(this);
            return component;
        }

        /**
         * Chamado quando component perde o foco!
         *
         * @return
         */
        @Override
        public Object getCellEditorValue() {
            return graphReff;
        }

        @Override
        public void handle(List<Graphic> selectedGraphics, ImageLayer layer) {
            if (selectedGraphics.size() == 1) {
                Graphic get = selectedGraphics.get(0);
                if (get instanceof DotGraphic) {
                    if (((DotGraphic) get).getLinkedOwner() == null) {
                        graphReff = get;
                        fireEditingStopped();

                        //remove se jah tiver adicionado!
                        selectedImagePane.getLayerModel()
                                .removeGraphicSelectionListener(this);
                    } else {
                        setDrawingTool(OrthoEventManager.dotGraphic);
                    }
                }
            }
        }

        @Override
        public void updateMeasuredItems(List<MeasureItem> measureList) {
            // Empty
        }
    }

}
