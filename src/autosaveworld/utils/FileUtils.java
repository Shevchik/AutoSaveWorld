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

package autosaveworld.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

	public static void init() {
	}

	public static String getAbsoluteFileName(String path) {
		return new File(path).getAbsolutePath();
	}

	public static List<String> splitPath(String path) {
		List<String> paths = new ArrayList<>();
		for (String split : path.split("[/]")) {
			if (!split.isEmpty()) {
				paths.add(split);
			}
		}
		return paths;
	}

	public static File buildFile(File file, String... childs) {
		File result = file;
		for (String child : childs) {
			result = new File(result, child);
		}
		return result;
	}

	public static void deleteDirectory(File file) throws IOException {
		if (!file.exists()) {
			return;
		}
		Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		file.delete();
	}

	public static File[] safeListFiles(File file) {
		File[] files = file.listFiles();
		return files != null ? files : new File[0];
	}

	public static String[] safeList(File file) {
		String[] files = file.list();
		return files != null ? files : new String[0];
	}

}
