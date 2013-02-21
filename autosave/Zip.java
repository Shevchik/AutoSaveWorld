package autosave;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    List<String> fileList;

    Zip(){}
private String prepend;
private ZipOutputStream zipOutStream;
//well, now i'm using another one copied function, but added some code to follow WEsnapshot zip file structure
public void ZipFolder(final File srcDir, final File destFile) throws FileNotFoundException, IOException {
prepend = null;
destFile.getParentFile().mkdirs();
final FileOutputStream outStream = new FileOutputStream(destFile);

try {
final BufferedOutputStream bufOutStream = new BufferedOutputStream(outStream, 2156);
try {
zipOutStream = new ZipOutputStream(bufOutStream);
try {

if (prepend != null) {
prepend += File.separator;
zipOutStream.putNextEntry(new ZipEntry(prepend));
zipOutStream.closeEntry();
}
else prepend = "";
zipOutStream.putNextEntry(new ZipEntry(srcDir.getName()+File.separator));
zipOutStream.closeEntry();

zipDir(srcDir, "");
}
finally {
zipOutStream.close();
}
}
finally {
bufOutStream.close();
}
}
finally {
outStream.close();
}
}

private void zipDir(final File srcDir, String currentDir) throws IOException {
if (!"".equals(currentDir)) {
currentDir += File.separator;
zipOutStream.putNextEntry(new ZipEntry(srcDir.getName()+File.separator+prepend + currentDir));
zipOutStream.closeEntry();
}

final File zipDir = new File(srcDir, currentDir);

for (final String child : zipDir.list()) {
final File srcFile = new File(zipDir, child);

if (srcFile.isDirectory()) zipDir(srcDir, currentDir + child);
else zipFile(srcFile,srcDir.getName()+File.separator + prepend + currentDir + child);
}
}

private  void zipFile(final File srcFile, final String entry) throws IOException {
final InputStream inStream = new FileInputStream(srcFile);
try {
final ZipEntry zipEntry = new ZipEntry(entry);
zipEntry.setTime(srcFile.lastModified());
zipOutStream.putNextEntry(zipEntry);

final byte[] buf = new byte[2156];
int len;

try {
while ((len = inStream.read(buf)) > -1)
if (len > 0) zipOutStream.write(buf, 0, len);
}
catch (final IOException e) {
}
finally {
zipOutStream.closeEntry();
}
}
finally {
inStream.close();
}
}
}


