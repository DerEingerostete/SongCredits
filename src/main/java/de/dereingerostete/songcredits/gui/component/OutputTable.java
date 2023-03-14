/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.component;

import com.formdev.flatlaf.extras.components.FlatTable;
import de.dereingerostete.songcredits.song.SongFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.List;

public class OutputTable extends FlatTable {
    protected final @NotNull List<SongFormatter> formatters;

    public OutputTable(@NotNull List<SongFormatter> formatters) {
        this.formatters = formatters;
        setModel(createModel(formatters));
        getColumnModel().getColumn(0).setResizable(true);
        setShowGrid(false);
    }

    @Nullable
    public SongFormatter getSelected() {
        int row = getSelectedRow();
        if (row < 0 || row > formatters.size()) return null;
        else return formatters.get(row);
    }

    public void updateModel(@NotNull List<SongFormatter> formatters) {
        setModel(createModel(formatters));
    }

    @NotNull
    protected TableModel createModel(@NotNull List<SongFormatter> formatters) {
        Object[][] data = new Object[formatters.size()][3];
        for (int i = 0; i < formatters.size(); i++) {
            SongFormatter formatter = formatters.get(i);
            data[i][0] = formatter.getFormatText();
            data[i][1] = formatter.isAppend() ? "Yes" : "No";
            data[i][2] = formatter.getFile().getAbsolutePath();
        }

        String[] names = {"Song format", "Append", "Song info path"};
        return new DefaultTableModel(data, names) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
    }

}
