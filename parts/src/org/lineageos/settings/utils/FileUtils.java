/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    public static boolean fileWritable(String path) {
        return fileExists(path) && new File(path).canWrite();
    }

    public static boolean isFileReadable(String path) {
        return fileExists(path) && new File(path).canRead();
    }

    public static String readLine(String path) {
        if (!isFileReadable(path)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return reader.readLine();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from " + path, e);
            return null;
        }
    }

    public static boolean writeLine(String path, String value) {
        if (!fileWritable(path)) {
            return false;
        }

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(value);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to " + path, e);
            return false;
        }
    }

    public static int readInt(String path, int defaultValue) {
        String line = readLine(path);
        if (line == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse int from " + path, e);
            return defaultValue;
        }
    }

    public static boolean writeInt(String path, int value) {
        return writeLine(path, String.valueOf(value));
    }
}
