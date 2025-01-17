/*
 * Copyright 2021 Laszlo Balazs-Csiki and Contributors
 *
 * This file is part of Pixelitor. Pixelitor is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License, version 3 as published by the Free
 * Software Foundation.
 *
 * Pixelitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pixelitor. If not, see <http://www.gnu.org/licenses/>.
 */

package pixelitor.layers;

import pixelitor.Composition;
import pixelitor.ConsistencyChecks;
import pixelitor.OpenImages;
import pixelitor.RunContext;
import pixelitor.colors.FgBgColors;
import pixelitor.gui.View;
import pixelitor.history.History;
import pixelitor.menus.MenuAction;
import pixelitor.menus.MenuAction.AllowedOnLayerType;
import pixelitor.menus.PMenu;
import pixelitor.menus.edit.FadeMenuItem;
import pixelitor.tools.Tools;
import pixelitor.utils.test.Events;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static pixelitor.utils.Keys.*;

/**
 * Whether the layer or its mask is visible/edited,
 * and whether the mask editing is done in "rubylith" mode.
 */
public enum MaskViewMode {
    NORMAL("Show and Edit Layer", false, false, false,
        AllowedOnLayerType.ANY, CTRL_1) {
    }, SHOW_MASK("Show and Edit Mask", true, true, false,
        AllowedOnLayerType.HAS_LAYER_MASK, CTRL_2) {
    }, EDIT_MASK("Show Layer, but Edit Mask", false, true, false,
        AllowedOnLayerType.HAS_LAYER_MASK, CTRL_3) {
    }, RUBYLITH("Show Mask as Rubylith, Edit Mask", false, true, true,
        AllowedOnLayerType.HAS_LAYER_MASK, CTRL_4) {
    };

    private final String guiName;
    private final boolean showRuby;
    private final AllowedOnLayerType allowedOnLayerType;
    private final KeyStroke keyStroke;
    private final boolean showMask;
    private final boolean editMask;

    MaskViewMode(String guiName, boolean showMask, boolean editMask, boolean showRuby,
                 AllowedOnLayerType allowedOnLayerType, KeyStroke keyStroke) {
        this.guiName = guiName;
        this.showMask = showMask;
        this.editMask = editMask;
        this.showRuby = showRuby;
        this.allowedOnLayerType = allowedOnLayerType;
        this.keyStroke = keyStroke;
    }

    /**
     * Adds a menu item that acts on the active layer of the active image
     */
    public void addToMenuBar(PMenu sub) {
        var action = new MenuAction(guiName, allowedOnLayerType) {
            @Override
            public void onClick() {
                OpenImages.onActiveLayer(layer -> activate(layer));
            }
        };
        sub.addActionWithKey(action, keyStroke);
    }

    /**
     * Adds a menu item that acts on the given layer and its image
     */
    public void addToPopupMenu(JMenu menu, Layer layer) {
        var action = new AbstractAction(guiName) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activate(layer);
            }
        };
        var menuItem = new JMenuItem(action);
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);
    }

    public void activate(Layer activeLayer) {
        View view = activeLayer.getComp().getView();
        activate(view, activeLayer);
    }

    public void activate(Composition comp, Layer activeLayer) {
        activate(comp.getView(), activeLayer);
    }

    public void activate(View view, Layer layer) {
        assert view != null;
        if (RunContext.isDevelopment()) {
            Events.postMaskViewActivate(this, view, layer);
        }

        boolean change = view.setMaskViewMode(this);
        layer.setMaskEditing(editMask);
        if (change) {
            FgBgColors.setLayerMaskEditing(editMask);

            if (!view.isMock()) {
                Tools.setupMaskEditing(editMask);
            }

            boolean canFade;
            if (editMask) {
                canFade = History.canFade(layer.getMask());
            } else {
                if (layer instanceof ImageLayer) {
                    canFade = History.canFade((ImageLayer) layer);
                } else {
                    canFade = false;
                }
            }
            FadeMenuItem.INSTANCE.refresh(canFade);

            if (RunContext.isDevelopment()) {
                assert ConsistencyChecks.fadeWouldWorkOn(layer.getComp());
            }
        }
    }

    public boolean editMask() {
        return editMask;
    }

    public boolean showMask() {
        return showMask;
    }

    public boolean showRuby() {
        return showRuby;
    }

    // used in asserts
    public boolean canBeAssignedTo(Layer layer) {
        if (editMask || showMask) {
            return layer.hasMask();
        }
        return true;
    }

    @Override
    public String toString() {
        return guiName;
    }
}
