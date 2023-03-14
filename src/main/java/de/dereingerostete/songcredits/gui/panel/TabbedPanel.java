/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.panel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class TabbedPanel extends JPanel {
    protected final @NotNull String name;

    public void init(@NotNull JFrame frame) {}

}
