package autosave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    List<String> fileList;

    Zip(){}
    
    //don't ask me how did this works, i didn't have time to write my own function, so i copied one.
    
    public void ZipFolder (File file)
    {         try {
        //create a ZipOutputStream to zip the data to 
        ZipOutputStream zos = new 
               ZipOutputStream(new FileOutputStream(file+".zip")); 
        zipDir(file.getName(), zos); 
		zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
    } 
    public void zipDir(String dir2zip, ZipOutputStream zos) 
    { 
        try 
       { 
                File  zipDir = new File(dir2zip); 
            String[] dirList = zipDir.list(); 
            byte[] readBuffer = new byte[2156]; 
            int bytesIn = 0; 
            for(int i=0; i<dirList.length; i++) 
            { 
                File f = new File(zipDir, dirList[i]); 
            if(f.isDirectory()) 
            { 
                String filePath = f.getPath(); 
                zipDir(filePath, zos); 
                continue; 
            } 
              FileInputStream fis = new FileInputStream(f); 
          ZipEntry anEntry = new ZipEntry(f.getPath()); 
            zos.putNextEntry(anEntry); 
                while((bytesIn = fis.read(readBuffer)) != -1) 
                { 
                    zos.write(readBuffer, 0, bytesIn); 
                } 
               fis.close(); 
        } 
    } 
    catch(Exception e) 
    { 

    } 
    }
}
