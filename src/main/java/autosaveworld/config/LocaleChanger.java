/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package autosaveworld.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import autosaveworld.config.loader.ConfigLoader;
import autosaveworld.config.localefiles.LocaleFiles;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;

public class LocaleChanger {

	// available locales
	public List<String> getAvailableLocales() {
		List<String> locales = new LinkedList<>();
		// add additional locales based on files in the jar.
		try (final ZipFile zipFile = new ZipFile(AutoSaveWorld.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getFile());) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();
				if (!ze.isDirectory() && ze.getName().contains("/")) {
					String pathname = ze.getName().substring(0, ze.getName().lastIndexOf('/'));
					String filename = ze.getName().substring(ze.getName().lastIndexOf('/') + 1, ze.getName().length());
					if (pathname.equalsIgnoreCase(LocaleFiles.getPackageName())) {
						if (filename.endsWith(".yml") && filename.contains("_")) {
							locales.add(filename.substring(0, filename.length() - 4).split("[_]")[1]);
						}
					}
				}
			}
			zipFile.close();
		} catch (IOException e) {
			locales.add("Error occured while scanning for available locales");
		}
		return locales;
	}

	public void loadLocale(String locale) {
		// load messages file from package
		MessageLogger.debug("switching to " + locale);
		loadLocaleFile(locale);
		// now load it
		MessageLogger.debug("loading configs");
		ConfigLoader.loadAndSave(AutoSaveWorld.getInstance().getMessageConfig());
	}

	// load needed locale file
	private void loadLocaleFile(String locale) {
		try (InputStream is = LocaleFiles.class.getResourceAsStream("configmsg_" + locale + ".yml")) {
			Files.copy(is, GlobalConstants.getMessageConfigPath().toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
		}
	}

}
