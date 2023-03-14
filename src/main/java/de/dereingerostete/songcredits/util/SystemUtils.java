/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.util;

import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class SystemUtils {
    private static final @NotNull OperatingSystem OPERATING_SYSTEM = loadOS();

    @NotNull
    private static OperatingSystem loadOS() {
        String osName = System.getProperty("os.name");
        if (osName == null) return OperatingSystem.UNKNOWN;
        osName = osName.toLowerCase();
        if (osName.startsWith("windows")) return OperatingSystem.WINDOWS;
        else if (osName.startsWith("linux")) return OperatingSystem.LINUX;
        else if (osName.startsWith("mac os")) return OperatingSystem.MAC;
        else if (osName.equals("freebsd")) return OperatingSystem.FREE_BSD;
        else if (osName.equals("sunos")) return OperatingSystem.SUN_OS;
        else return OperatingSystem.OTHER;
    }

    @NotNull
    public static OperatingSystem getOS() {
        return OPERATING_SYSTEM;
    }

    @NotNull
    public static String getSystemInfo() {
        RuntimeMXBean runtimeMX = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean systemMX = ManagementFactory.getOperatingSystemMXBean();

        if (runtimeMX != null && systemMX != null) {
            String javaVersion = runtimeMX.getSpecVersion();
            String vmName = runtimeMX.getVmName();
            String vmVersion = runtimeMX.getVmVersion();

            String systemName = systemMX.getName();
            String systemVersion = systemMX.getVersion();
            String systemArch = systemMX.getArch();

            Runtime runtime = Runtime.getRuntime();
            long memory = runtime.maxMemory();

            String javaInfo = "Java " + javaVersion + " (" + vmName + " " + vmVersion + ")";
            String systemInfo = "HostOS: " + systemName  + " " + systemVersion + " (" + systemArch + ")";
            String memoryInfo = "Max. Memory: " + makeHumanReadable(memory);

            return "System Information's:\n" +
                    javaInfo + "\n" +
                    systemInfo + "\n" +
                    memoryInfo;
        } else return "Unable to read system info";
    }

    public static void printSystemInfo() {
        for (String line : getSystemInfo().split("\n"))
            Logging.info(line);
    }

    @NotNull
    private static String makeHumanReadable(long bytes) {
        long min = Long.MIN_VALUE;
        long max = Long.MAX_VALUE;
        long absBytes = bytes == min ? max : Math.abs(bytes);
        if (absBytes < 1024) return bytes + " B";

        CharacterIterator iterator = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absBytes > 0xfffccccccccccccL >> i; i -= 10) {
            absBytes >>= 10;
            iterator.next();
        }

        absBytes *= Long.signum(bytes);
        char current = iterator.current();
        return String.format("%.1f %ciB", absBytes / 1024.0, current);
    }


}
