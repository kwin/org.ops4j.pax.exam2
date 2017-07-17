/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.container.eclipse.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Represents the information of a resolvable bundle, it also carry some context that can be used to
 * specify the bundle further
 * 
 * @author Christoph Läubrich
 *
 * @param <Context>
 */
public class BundleInfo<Context> implements Comparable<BundleInfo<Context>> {

    private String symbolicName;
    private Context context;
    private Version version;

    public BundleInfo(Manifest manifest, Context context) {
        Attributes attributes = manifest.getMainAttributes();
        this.context = context;
        this.symbolicName = notNull(attributes, Constants.BUNDLE_SYMBOLICNAME).split(";")[0];
        this.version = Version
            .parseVersion(notNull(attributes, Constants.BUNDLE_VERSION).split(";")[0]);
    }

    public BundleInfo(String symbolicName, Version version, Context context) {
        this.symbolicName = symbolicName;
        this.version = version;
        this.context = context;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public Version getVersion() {
        return version;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int compareTo(BundleInfo<Context> o) {
        int cmp = symbolicName.compareTo(o.symbolicName);
        if (cmp == 0) {
            return version.compareTo(o.version);
        }
        return cmp;
    }

    private static String notNull(Attributes attributes, String attrName) {
        String value = attributes.getValue(attrName);
        if (value == null) {
            throw new IllegalArgumentException(
                "Header-Name " + attrName + " not found in Manifest!");
        }
        return value;
    }

    @Override
    public String toString() {
        if (context == null) {
            return symbolicName + ":" + version;
        }
        else {
            return symbolicName + ":" + version + ":" + context;
        }
    }

    public static boolean isBundle(Manifest manifest) {
        if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            return attributes.containsKey(new Attributes.Name(Constants.BUNDLE_SYMBOLICNAME))
                && attributes.containsKey(new Attributes.Name(Constants.BUNDLE_VERSION));
        }
        return false;
    }

    public static Manifest readManifest(File folder) throws IOException {
        File metaInf = new File(folder, "META-INF");
        if (metaInf.exists()) {
            try (FileInputStream is = new FileInputStream(new File(metaInf, "MANIFEST.MF"))) {
                Manifest manifest = new Manifest(is);
                return manifest;
            }
        }
        else {
            throw new FileNotFoundException(
                "can't find folder META-INF in folder " + folder.getAbsolutePath());
        }
    }

    public static <T> BundleInfo<T> readExplodedBundle(File folder, T context) throws IOException {
        return new BundleInfo<T>(readManifest(folder), context);
    }
}