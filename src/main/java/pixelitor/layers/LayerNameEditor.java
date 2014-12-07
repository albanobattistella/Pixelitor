/*
 * Copyright 2009-2014 Laszlo Balazs-Csiki
 *
 * This file is part of Pixelitor. Pixelitor is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License, version 3 as published by the Free
 * Software Foundation.
 *
 * Pixelitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pixelitor.  If not, see <http://www.gnu.org/licenses/>.
 */
package pixelitor.layers;

import javax.swing.*;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A JTextField for layer names that becomes editable if double-clicked
 */
public class LayerNameEditor extends JTextField {
    private LayerButton layerButton;

    public LayerNameEditor(LayerButton layerButton, final Layer layer) {
        super(layer.getName());
        this.layerButton = layerButton;
        disableEditing();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {   // enable if double-clicked
                    enableEditing();
                } else {                        // a simple click selects the layer
                    requestFocus();
                    Container container = getParent();
                    if (container != null) {
                        LayerButton b = (LayerButton) container;
                        b.setSelected(true);
                    }
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                disableEditing();
                layer.setName(getText(), true);
            }
        });

        // disable if enter pressed
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableEditing();
                layer.setName(getText(), true);
            }
        });

    }

    private void enableEditing() {
        setEnabled(true);
        setEditable(true);
        requestFocus();
        selectAll();
        getCaret().setVisible(true);
    }

    private void disableEditing() {
        setEnabled(false);
        setEditable(false);
    }

    public LayerButton getLayerButton() {
        return layerButton;
    }
}
