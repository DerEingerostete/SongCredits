/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.component;

import de.dereingerostete.songcredits.util.GeneralConfig;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

@RequiredArgsConstructor
public class PlaceholderSaver extends KeyAdapter {
    protected final @NotNull GeneralConfig config;
    protected final @NotNull JTextField textField;
    protected Timer timer;

    @Override
    public void keyReleased(KeyEvent event) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                String text = textField.getText();
                config.setPlaceholderText(text);
                config.save();
                timer.cancel();
                timer = null;
            }

        }, 2000L);
    }

}
