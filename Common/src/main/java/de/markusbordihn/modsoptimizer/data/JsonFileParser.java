/*
 * Copyright 2022 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.modsoptimizer.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import de.markusbordihn.modsoptimizer.Constants;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JsonFileParser {

  private JsonFileParser() {}

  public static JsonObject readJsonFile(JarFile jarFile, Path path) {
    ZipEntry modsFile = jarFile.getEntry(path.toString().replace("\\", "/"));
    if (modsFile != null && !modsFile.isDirectory()) {
      try (InputStream inputStream = jarFile.getInputStream(modsFile)) {
        return parseJson(inputStream, path, jarFile);
      } catch (Exception e) {
        Constants.LOG.error("Error reading json file {} from {}: {}", path, jarFile, e);
      }
    } else {
      Constants.LOG.error(
          "Json file {} not found in {}", path.toString().replace("\\", "/"), jarFile);
    }
    return new JsonObject();
  }

  private static JsonObject parseJson(InputStream inputStream, Path path, JarFile jarFile) {
    try {
      JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
      return jsonElement.getAsJsonObject();
    } catch (JsonSyntaxException e) {
      Constants.LOG.warn("Invalid json file {} from {}:", path, jarFile, e);
      return tryParsingWithLenient(inputStream, path, jarFile);
    } catch (Exception e) {
      Constants.LOG.error("Error parsing json file {} from {}: {}", path, jarFile, e);
    }
    return new JsonObject();
  }

  private static JsonObject tryParsingWithLenient(
      InputStream inputStream, Path path, JarFile jarFile) {
    try {
      JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
      reader.setLenient(true);
      JsonElement jsonElement = JsonParser.parseReader(reader);
      return jsonElement.getAsJsonObject();
    } catch (Exception e) {
      Constants.LOG.error("Unable to parse invalid json file {} from {}: {}", path, jarFile, e);
    }
    return new JsonObject();
  }
}
