/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.component;

import de.dereingerostete.songcredits.util.GuiUtil;
import de.dereingerostete.songcredits.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IconButton extends JLabel {
    protected ImageIcon defaultIcon;
    protected ImageIcon hoverIcon;
    protected boolean eventRunning;

    public IconButton(@NotNull String iconName, @NotNull String description) {
        setIcon(iconName, description);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent event) {
                if (isEnabled()) setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                if (isEnabled()) setIcon(defaultIcon);
            }

        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) setIcon(defaultIcon);
    }

    public void setIcon(@NotNull String iconName, @NotNull String description) {
        defaultIcon = fetchIcon(iconName, description);
        hoverIcon = fetchIcon(iconName + "-hover", description);
        setIcon(defaultIcon);
    }

    public void addActionListener(ActionListener listener) {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent event) {
                if (eventRunning || !isEnabled() || !SwingUtilities.isLeftMouseButton(event)) return;
                Object source = event.getSource();
                int id = event.getID();
                eventRunning = true;
                Utils.runAsync(() -> {
                    listener.actionPerformed(new ActionEvent(source, id, "pressed"));
                    eventRunning = false;
                });
            }

        });
    }

    @NotNull
    protected ImageIcon fetchIcon(@NotNull String name, @NotNull String description) {
        ImageIcon imageIcon = GuiUtil.loadIcon(name + ".png");
        if (imageIcon == null) imageIcon = new ImageIcon();
        imageIcon.setDescription(description);
        return imageIcon;
    }

}
