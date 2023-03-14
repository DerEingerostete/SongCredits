/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source;

public class SourceException extends Exception {

    public SourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SourceException(String message) {
        super(message);
    }

}
