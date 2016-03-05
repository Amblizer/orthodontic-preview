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
package com.orthodonticpreview.ui.internal;

import com.orthodonticpreview.ui.explorer.OrthoExplorerView;
import com.orthodonticpreview.ui.OrthodonticWin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.DataExplorerViewFactory;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.docking.UIManager;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012 Jul, 18
 */
public class Activator implements BundleActivator, ServiceListener {

    /**
     * Class logger.
     */
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        LOGGER.info("Starting Orthodontic Preview UI Activator.");
        LOGGER.debug("Showing debug infomation.");

        //must be instantiate in the EDT
        GuiExecutor.instance().invokeAndWait(new Runnable() {

            @Override
            public void run() {
                final OrthodonticWin win = OrthodonticWin.getInstance();
                try {
                    win.createMainPanel();
                    win.showWindow();
                    win.openCreateOpDialog();

                } catch (Exception ex) {
                    // Nimbus bug, hangs GUI:
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6785663
                    // It is better to exit than to let run a zombie process
                    LOGGER.error("Could not start GUI: " + ex);
                    System.exit(-1);
                }
            }

        });

        //explorer needs the "execute".
        GuiExecutor.instance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (ServiceReference<DataExplorerViewFactory> serviceReference : bundleContext
                            .getServiceReferences(DataExplorerViewFactory.class, null)) {
                        DataExplorerViewFactory service = bundleContext.getService(serviceReference);
                        registerIfExplorer(service.createDataExplorerView(null));
                    }
                } catch (InvalidSyntaxException e1) {
                    e1.printStackTrace();
                }

                // Add all the service listeners
                try {
                    bundleContext.addServiceListener(Activator.this,
                            String.format("(%s=%s)", Constants.OBJECTCLASS,
                            DataExplorerViewFactory.class.getName()));
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        LOGGER.info("Stopping by Orthodontic Preview UI Activator.");

        //close explorer
        OrthoExplorerView.getService().dispose();
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        GuiExecutor.instance().execute(new Runnable() {

            @Override
            public void run() {

                final ServiceReference<?> m_ref = event.getServiceReference();
                final BundleContext context = FrameworkUtil.getBundle(Activator.this.getClass()).getBundleContext();
                Object service = context.getService(m_ref);
                if (service instanceof DataExplorerViewFactory) {
                    final DataExplorerView explorer = ((DataExplorerViewFactory) service).createDataExplorerView(null);

                    if (event.getType() == ServiceEvent.REGISTERED) {
                        registerIfExplorer(explorer);

                    } else if (event.getType() == ServiceEvent.UNREGISTERING) {
                        uregisterIfExplorer(explorer);
                    }
                }

            }
        });
    }

    /**
     * Register a service as Explorer, if it implements
     * <code>DataExplorerView</code>.
     *
     * @param service a Service.
     */
    private void registerIfExplorer(final Object service) {
        LOGGER.debug("Service = " + service.toString());

        synchronized (UIManager.EXPLORER_PLUGINS) {
            if (service instanceof DataExplorerView
                    && !UIManager.EXPLORER_PLUGINS.contains(
                            (DataExplorerView) service)) {

                final DataExplorerView explorer = (DataExplorerView) service;

                LOGGER.info("Addig Explorer: " + explorer.getUIName());
                UIManager.EXPLORER_PLUGINS.add(explorer);

                if (explorer.getDataExplorerModel() != null) {
                    explorer.getDataExplorerModel().addPropertyChangeListener(
                            OrthodonticWin.getInstance());
                    explorer.getDataExplorerModel().addPropertyChangeListener(
                            explorer);
                }

                if (explorer instanceof DockableTool) {
                    final DockableTool dockable = (DockableTool) explorer;
                    dockable.showDockable();
                }
            }
        }
    }

    /**
     * Unregister service if its an Explorer.
     *
     * @param service the service.
     * @param m_ref service reference.
     */
    private void uregisterIfExplorer(final Object service) {
        if (service instanceof DataExplorerView) {
            final DataExplorerView explorer = (DataExplorerView) service;
            synchronized (UIManager.EXPLORER_PLUGINS) {
                GuiExecutor.instance().execute(new Runnable() {

                    @Override
                    public void run() {
                        if (UIManager.EXPLORER_PLUGINS.contains(explorer)) {
                            if (explorer.getDataExplorerModel() != null) {
                                explorer.getDataExplorerModel()
                                        .removePropertyChangeListener(
                                                OrthodonticWin.getInstance());
                            }
                            UIManager.EXPLORER_PLUGINS.remove(explorer);
                            explorer.dispose();
                        }
                    }
                });
            }
        }
    }

}
