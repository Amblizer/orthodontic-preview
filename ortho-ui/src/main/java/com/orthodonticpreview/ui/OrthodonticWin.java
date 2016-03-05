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
package com.orthodonticpreview.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.util.StringUtil;
import org.weasis.core.api.util.StringUtil.Suffix;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerPlugin;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.extension.gui.dock.theme.eclipse.EclipseTabDockActionLocation;
import bibliothek.extension.gui.dock.theme.eclipse.EclipseTabStateInfo;
import bibliothek.extension.gui.dock.theme.eclipse.rex.RexSystemColor;
import bibliothek.gui.DockUI;
import bibliothek.gui.dock.ScreenDockStation;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.predefined.CCloseAction;
import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.common.theme.eclipse.CommonEclipseThemeConnector;
import bibliothek.gui.dock.station.screen.BoundaryRestriction;
import bibliothek.gui.dock.util.DirectWindowProvider;
import bibliothek.gui.dock.util.DockUtilities;
import bibliothek.gui.dock.util.Priority;
import bibliothek.gui.dock.util.color.ColorManager;
import bibliothek.gui.dock.util.laf.LookAndFeelColors;
import bibliothek.util.Colors;
import com.orthodonticpreview.ui.explorer.CreateOPDialog;
import com.orthodonticpreview.ui.internal.Messages;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.LookAndFeel;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.service.BundleTools;

public class OrthodonticWin implements PropertyChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrthodonticWin.class);

    private static ViewerPlugin selectedPlugin = null;

    private static final OrthodonticWin instance = new OrthodonticWin();

    private final List<Runnable> runOnClose = new ArrayList<Runnable>();

    private final JFrame frame;

    private final CFocusListener selectionListener = new CFocusListener() {

        @Override
        public void focusGained(CDockable dockable) {
            if (dockable != null && dockable.getFocusComponent() instanceof ViewerPlugin) {
                setSelectedPlugin((ViewerPlugin) dockable.getFocusComponent());
            }
        }

        @Override
        public void focusLost(CDockable dockable) {
        }
    };

    private OrthodonticWin() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });

        frame.setTitle("Orthodontic Preview" + " v"
                + BundleTools.SYSTEM_PREFERENCES.getProperty("orthodontic.version", "1.x"));
        ImageIcon icon = new ImageIcon(OrthodonticWin.class.getResource("/icon/icon-logo.png"));
        frame.setIconImage(icon.getImage());
    }

    public static OrthodonticWin getInstance() {
        return instance;
    }

    public JFrame getFrame() {
        return frame;
    }

    public boolean closeWindow() {
        int option = JOptionPane.showConfirmDialog(frame,
                Messages.getString("OrthodonticWin.exit_mes"));
        if (option == JOptionPane.YES_OPTION) {
            closeAllRunnable();
            System.exit(0);
            return true;
        }
        return false;
    }

    private void closeAllRunnable() {
        for (Runnable onClose : runOnClose) {
            onClose.run();
        }
    }

    public void runOnClose(Runnable run) {
        runOnClose.add(run);
    }

    public void destroyOnClose(final CControl control) {
        runOnClose(new Runnable() {
            @Override
            public void run() {
                control.destroy();
            }
        });
    }

    public void createMainPanel() throws Exception {

        // Do not disable check when debugging
        if (System.getProperty("maven.localRepository") == null) {
            DockUtilities.disableCheckLayoutLocked();
        }
        CControl control = UIManager.DOCKING_CONTROL;
        control.setRootWindow(new DirectWindowProvider(frame));
        destroyOnClose(control);
        ThemeMap themes = control.getThemes();
        themes.select(ThemeMap.KEY_ECLIPSE_THEME);
        control.getController().getProperties().set(EclipseTheme.PAINT_ICONS_WHEN_DESELECTED, true);
        control.putProperty(ScreenDockStation.BOUNDARY_RESTRICTION, BoundaryRestriction.HARD);
        control.putProperty(EclipseTheme.THEME_CONNECTOR, new HidingEclipseThemeConnector(control));

        control.addFocusListener(selectionListener);

        fixSubstance(control);

        frame.getContentPane().add(UIManager.BASE_AREA, BorderLayout.CENTER);
        // Allow to drop series into the empty main area
        UIManager.MAIN_AREA.setLocation(CLocation.base().normalRectangle(0, 0, 1, 1));
        UIManager.MAIN_AREA.setVisible(true);
    }

    private void fixSubstance(CControl control) {
        LookAndFeel laf = javax.swing.UIManager.getLookAndFeel();
        if (laf.getClass().getName().startsWith("org.pushingpixels")) {
            ColorManager colors = control.getController().getColors();

            Color selection = javax.swing.UIManager.getColor("TextArea.selectionBackground");
            Color inactiveColor = DockUI.getColor(LookAndFeelColors.TITLE_BACKGROUND).darker();
            Color inactiveColorGradient = DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND);
            Color activeColor = selection.darker();
            Color ActiveTextColor = javax.swing.UIManager.getColor("TextArea.selectionForeground");

            colors.put(Priority.CLIENT, "stack.tab.border.selected", inactiveColorGradient);
            colors.put(Priority.CLIENT, "stack.tab.border.selected.focused", selection);
            colors.put(Priority.CLIENT, "stack.tab.border.selected.focuslost", inactiveColor);

            colors.put(Priority.CLIENT, "stack.tab.top.selected", inactiveColor);
            colors.put(Priority.CLIENT, "stack.tab.top.selected.focused", activeColor);
            colors.put(Priority.CLIENT, "stack.tab.top.selected.focuslost", inactiveColor);

            colors.put(Priority.CLIENT, "stack.tab.bottom.selected", inactiveColorGradient);
            colors.put(Priority.CLIENT, "stack.tab.bottom.selected.focused", selection);
            colors.put(Priority.CLIENT, "stack.tab.bottom.selected.focuslost", inactiveColor);

            colors.put(Priority.CLIENT, "stack.tab.text.selected", RexSystemColor.getInactiveTextColor());
            colors.put(Priority.CLIENT, "stack.tab.text.selected.focused", ActiveTextColor);
            colors.put(Priority.CLIENT, "stack.tab.text.selected.focuslost", RexSystemColor.getInactiveTextColor());

            colors.put(Priority.CLIENT, "title.flap.active", selection);
            colors.put(Priority.CLIENT, "title.flap.active.text", ActiveTextColor);
            colors.put(Priority.CLIENT, "title.flap.active.knob.highlight", Colors.brighter(selection));
            colors.put(Priority.CLIENT, "title.flap.active.knob.shadow", Colors.darker(selection));
        }
    }

    public void openSeriesInViewerPlugin(ViewerPluginBuilder builder, MediaSeriesGroup group) {
        if (builder == null) {
            return;
        }
        SeriesViewerFactory factory = builder.getFactory();
        DataExplorerModel model = builder.getModel();
        List<MediaSeries<? extends MediaElement<?>>> seriesList = builder.getSeries();
        Map<String, Object> props = builder.getProperties();

        if (factory != null && group != null) {
            synchronized (UIManager.VIEWER_PLUGINS) {
                for (int i = UIManager.VIEWER_PLUGINS.size() - 1; i >= 0; i--) {
                    final ViewerPlugin p = UIManager.VIEWER_PLUGINS.get(i);
                    if (p instanceof ImageViewerPlugin && p.getName().equals(factory.getUIName())
                            && group.equals(p.getGroupID())) {
                        ImageViewerPlugin viewer = ((ImageViewerPlugin) p);
                        viewer.addSeries(seriesList.get(0));
                        viewer.setSelectedAndGetFocus();
                        return;
                    }
                }
            }
        }
        // Pass the DataExplorerModel to the viewer
        props.put(DataExplorerModel.class.getName(), model);

        SeriesViewer seriesViewer = factory.createSeriesViewer(props);
        if (seriesViewer instanceof ViewerPlugin) {
            ViewerPlugin viewer = (ViewerPlugin) seriesViewer;
            String title;

            if (group == null && model instanceof TreeModel && seriesList.size() > 0
                    && model.getTreeModelNodeForNewPlugin() != null) {
                TreeModel treeModel = (TreeModel) model;
                MediaSeries s = seriesList.get(0);
                group = treeModel.getParent(s, model.getTreeModelNodeForNewPlugin());
            }
            if (group != null) {
                title = group.toString();
                viewer.setGroupID(group);
                viewer.getDockable().setTitleToolTip(title);
                viewer.setPluginName(StringUtil.getTruncatedString(
                        title, 25, Suffix.THREE_PTS));
            }

            if (registerPlugin(viewer)) {
                viewer.setSelectedAndGetFocus();
                if (seriesViewer instanceof ImageViewerPlugin) {
                    ((ImageViewerPlugin) viewer).selectLayoutPositionForAddingSeries(seriesList);
                }
                for (MediaSeries m : seriesList) {
                    viewer.addSeries(m);
                }
                viewer.setSelected(true);
            } else {
                viewer.close();
            }
        }
    }

    public boolean registerPlugin(final ViewerPlugin plugin) {
        if (plugin == null || UIManager.VIEWER_PLUGINS.contains(plugin)) {
            return false;
        }
        plugin.showDockable();
        return true;
    }

    public synchronized ViewerPlugin getSelectedPlugin() {
        return selectedPlugin;
    }

    public synchronized void setSelectedPlugin(ViewerPlugin plugin) {
        if (plugin == null) {
            selectedPlugin = null;
            return;
        }
        if (selectedPlugin == plugin) {
            plugin.requestFocusInWindow();
            return;
        }
        ViewerPlugin oldPlugin = selectedPlugin;
        if (selectedPlugin != null) {
            selectedPlugin.setSelected(false);
        }
        selectedPlugin = plugin;
        selectedPlugin.setSelected(true);

        List<DockableTool> tool = selectedPlugin.getToolPanel();
        List<DockableTool> oldTool = oldPlugin == null ? null : oldPlugin.getToolPanel();

        if (tool != oldTool) {
            if (oldTool != null) {
                for (DockableTool p : oldTool) {
                    p.closeDockable();
                }
            }
            if (tool != null) {
                for (int i = 0; i < tool.size(); i++) {
                    DockableTool p = tool.get(i);
                    if (p.isComponentEnabled()) {
                        p.showDockable();
                    }
                }
            }
        }
    }

    public void showWindow() throws Exception {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Toolkit kit = Toolkit.getDefaultToolkit();

        Rectangle bound = null;

        GraphicsConfiguration config = ge.getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle b;
        if (config != null) {
            b = config.getBounds();
            Insets inset = kit.getScreenInsets(config);
            b.x += inset.left;
            b.y += inset.top;
            b.width -= (inset.left + inset.right);
            b.height -= (inset.top + inset.bottom);
        } else {
            b = new Rectangle(new Point(0, 0), kit.getScreenSize());
        }
        bound = b;

        LOGGER.debug("Max main screen bound: {}", bound.toString());

        // set a valid size, insets of screen is often non consistent
        frame.setBounds(bound.x, bound.y, bound.width - 150, bound.height - 150);
        frame.setVisible(true);

        frame.setExtendedState((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
                ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);

        LOGGER.info("End of loading the GUI...");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ObservableEvent) {
            final ObservableEvent event = (ObservableEvent) evt;
            final ObservableEvent.BasicAction action = event.getActionCommand();
            final Object source = event.getNewValue();
            if (evt.getSource() instanceof DataExplorerModel) {
                if (ObservableEvent.BasicAction.Select.equals(action)) {
                    processSelectEvent(source);

                } else if (ObservableEvent.BasicAction.Register
                        .equals(action)) {
                    processRegisterEvent(source);

                } else if (ObservableEvent.BasicAction.Unregister
                        .equals(action)) {
                    processUnregisterEvent(source);

                }
            }
        }
    }

    private void processSelectEvent(Object source) {
        if (source instanceof MediaSeriesGroup) {
            MediaSeriesGroup group = (MediaSeriesGroup) source;
            // If already selected do not reselect or select a second window
            if (selectedPlugin == null || !group.equals(selectedPlugin.getGroupID())) {
                synchronized (UIManager.VIEWER_PLUGINS) {
                    for (int i = UIManager.VIEWER_PLUGINS.size() - 1; i >= 0; i--) {
                        ViewerPlugin p = UIManager.VIEWER_PLUGINS.get(i);
                        if (group.equals(p.getGroupID())) {
                            p.setSelectedAndGetFocus();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void processRegisterEvent(Object source) {
        if (source instanceof ViewerPlugin) {
            registerPlugin((ViewerPlugin) source);
        } else if (source instanceof ViewerPluginBuilder) {
            ViewerPluginBuilder builder = (ViewerPluginBuilder) source;
            DataExplorerModel model = builder.getModel();
            List<MediaSeries<? extends MediaElement<?>>> series = builder.getSeries();
            Map<String, Object> props = builder.getProperties();
            if (series != null
                    && JMVUtils.getNULLtoTrue(props.get(ViewerPluginBuilder.CMP_ENTRY_BUILD_NEW_VIEWER))
                    && model.getTreeModelNodeForNewPlugin() != null && model instanceof TreeModel) {
                TreeModel treeModel = (TreeModel) model;
                MediaSeries s = series.get(0);
                MediaSeriesGroup group = treeModel.getParent(s, model.getTreeModelNodeForNewPlugin());
                openSeriesInViewerPlugin(builder, group);
            } else {
                openSeriesInViewerPlugin(builder, null);
            }
        }
    }

    private void processUnregisterEvent(Object source) {
        if (source instanceof SeriesViewerFactory) {
            SeriesViewerFactory viewerFactory = (SeriesViewerFactory) source;
            final List<ViewerPlugin<?>> pluginsToRemove = new ArrayList<ViewerPlugin<?>>();
            String name = viewerFactory.getUIName();
            synchronized (UIManager.VIEWER_PLUGINS) {
                for (final ViewerPlugin<?> plugin : UIManager.VIEWER_PLUGINS) {
                    if (name.equals(plugin.getName())) {
                        // Do not close Series directly
                        pluginsToRemove.add(plugin);
                    }
                }
            }
            UIManager.closeSeriesViewer(pluginsToRemove);
        }
    }

    /**
     * Opens the dialog to create a new Preview.
     */
    public void openCreateOpDialog() {
        final JDialog createOP
                = new CreateOPDialog(getFrame());
        JMVUtils.showCenterScreen(createOP);
    }

    public static class HidingEclipseThemeConnector extends CommonEclipseThemeConnector {

        public HidingEclipseThemeConnector(CControl control) {
            super(control);
        }

        @Override
        protected EclipseTabDockActionLocation getLocation(CAction action, EclipseTabStateInfo tab) {
            if (action instanceof CCloseAction) {
                /*
                 * By redefining the behavior of the close-action, we can hide it if the tab is not selected
                 */
                if (tab.isSelected()) {
                    return EclipseTabDockActionLocation.TAB;
                } else {
                    return EclipseTabDockActionLocation.HIDDEN;
                }
            }
            return super.getLocation(action, tab);
        }
    }
}
