package org.mule.tools.devkit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by estebanwasinger on 7/28/15.
 */
public class MavenUtil2 {
    public static final String MAVEN_NOTIFICATION_GROUP = "Maven";
    public static final String SETTINGS_XML = "settings.xml";
    public static final String DOT_M2_DIR = ".m2";
    public static final String PROP_USER_HOME = "user.home";
    public static final String ENV_M2_HOME = "M2_HOME";
    public static final String M2_DIR = "m2";
    public static final String BIN_DIR = "bin";
    public static final String CONF_DIR = "conf";
    public static final String M2_CONF_FILE = "m2.conf";
    public static final String REPOSITORY_DIR = "repository";
    public static final String LIB_DIR = "lib";


    private static volatile Map<String, String> ourPropertiesFromMvnOpts;

    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_VERSION = System.getProperty("os.version").toLowerCase(Locale.US);

    public static final boolean isMac = OS_NAME.toLowerCase().startsWith("mac");
    public static final boolean isLinux = OS_NAME.toLowerCase().startsWith("linux");

    @Nullable
    public static File resolveMavenHomeDirectory() {

        String m2home = System.getenv(ENV_M2_HOME);
        if (!isEmptyOrSpaces(m2home)) {
            final File homeFromEnv = new File(m2home);
            if (isValidMavenHome(homeFromEnv)) {
                return homeFromEnv;
            }
        }

        String mavenHome = System.getenv("MAVEN_HOME");
        if (!isEmptyOrSpaces(mavenHome)) {
            final File mavenHomeFile = new File(mavenHome);
            if (isValidMavenHome(mavenHomeFile)) {
                return mavenHomeFile;
            }
        }

        String userHome = getUserHome();
        if (!isEmptyOrSpaces(userHome)) {
            final File underUserHome = new File(userHome, M2_DIR);
            if (isValidMavenHome(underUserHome)) {
                return underUserHome;
            }
        }

        if (isMac) {
            File home = fromBrew();
            if (home != null) {
                return home;
            }

            if ((home = fromMacSystemJavaTools()) != null) {
                return home;
            }
        }
        else if (isLinux) {
            File home = new File("/usr/share/maven");
            if (isValidMavenHome(home)) {
                return home;
            }

            home = new File("/usr/share/maven2");
            if (isValidMavenHome(home)) {
                return home;
            }
        }
        return null;
    }

    @Nullable
    private static File fromMacSystemJavaTools() {
        final File symlinkDir = new File("/usr/share/maven");
        if (isValidMavenHome(symlinkDir)) {
            return symlinkDir;
        }

        // well, try to search
        final File dir = new File("/usr/share/java");
        final String[] list = dir.list();
        if (list == null || list.length == 0) {
            return null;
        }

        String home = null;
        final String prefix = "maven-";
        final int versionIndex = prefix.length();
        for (String path : list) {
            if (path.startsWith(prefix) &&
                    (home == null || compareVersionNumbers(path.substring(versionIndex), home.substring(versionIndex)) > 0)) {
                home = path;
            }
        }

        if (home != null) {
            File file = new File(dir, home);
            if (isValidMavenHome(file)) {
                return file;
            }
        }

        return null;
    }

    @Nullable
    private static File fromBrew() {
        final File brewDir = new File("/usr/local/Cellar/maven");
        final String[] list = brewDir.list();
        if (list == null || list.length == 0) {
            return null;
        }

        if (list.length > 1) {
            Arrays.sort(list, new Comparator<String>() {

                public int compare(String o1, String o2) {
                    return compareVersionNumbers(o2, o1);
                }
            });
        }

        final File file = new File(brewDir, list[0] + "/libexec");
        return isValidMavenHome(file) ? file : null;
    }

    public static boolean isEmptyOrSpaces(@Nullable String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static boolean isValidMavenHome(File home) {
        return getMavenConfFile(home).exists();
    }

    public static File getMavenConfFile(File mavenHome) {
        return new File(new File(mavenHome, BIN_DIR), M2_CONF_FILE);
    }

    @NotNull
    public static File resolveM2Dir() {
        return new File(getUserHome(), DOT_M2_DIR);
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }


    public static int compareVersionNumbers(@Nullable String v1, @Nullable String v2) {
        // todo duplicates com.intellij.util.text.VersionComparatorUtil.compare
        // todo please refactor next time you make changes here
        if (v1 == null && v2 == null) {
            return 0;
        }
        if (v1 == null) {
            return -1;
        }
        if (v2 == null) {
            return 1;
        }

        String[] part1 = v1.split("[\\.\\_\\-]");
        String[] part2 = v2.split("[\\.\\_\\-]");

        int idx = 0;
        for (; idx < part1.length && idx < part2.length; idx++) {
            String p1 = part1[idx];
            String p2 = part2[idx];

            int cmp;
            if (p1.matches("\\d+") && p2.matches("\\d+")) {
                cmp = new Integer(p1).compareTo(new Integer(p2));
            }
            else {
                cmp = part1[idx].compareTo(part2[idx]);
            }
            if (cmp != 0) return cmp;
        }

        if (part1.length == part2.length) {
            return 0;
        }
        else {
            boolean left = part1.length > idx;
            String[] parts = left ? part1 : part2;

            for (; idx < parts.length; idx++) {
                String p = parts[idx];
                int cmp;
                if (p.matches("\\d+")) {
                    cmp = new Integer(p).compareTo(0);
                }
                else {
                    cmp = 1;
                }
                if (cmp != 0) return left ? cmp : -cmp;
            }
            return 0;
        }
    }

}
