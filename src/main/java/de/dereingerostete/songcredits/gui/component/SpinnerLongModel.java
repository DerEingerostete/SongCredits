/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.component;

import javax.swing.*;

public class SpinnerLongModel extends SpinnerNumberModel {

    public SpinnerLongModel(long value, long minimum, long maximum, long step) {
        super(Long.valueOf(value), Long.valueOf(minimum), Long.valueOf(maximum), Long.valueOf(step));
    }

}
