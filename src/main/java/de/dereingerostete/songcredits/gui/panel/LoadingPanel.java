/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.panel;

import de.dereingerostete.songcredits.util.GuiUtil;

import javax.swing.*;

public class LoadingPanel extends JPanel {
    protected final JLabel imageLabel;
    protected final ImageIcon icon;

    public LoadingPanel() {
        setLayout(null);
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        icon = GuiUtil.loadIcon("spinner.gif");
        if (icon != null) imageLabel.setIcon(icon);
        else imageLabel.setText("Loading...");
        add(imageLabel);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        imageLabel.setSize(
                icon != null ? icon.getIconWidth() : getWidth(),
                icon != null ? icon.getIconHeight() : getHeight());
        imageLabel.setBounds(
                icon != null ? getWidth() / 2 - imageLabel.getWidth() / 2 : getWidth(),
                icon != null ? getHeight() / 2 - imageLabel.getHeight() / 2 : getHeight(),
                imageLabel.getWidth(), imageLabel.getHeight());
    }

}
