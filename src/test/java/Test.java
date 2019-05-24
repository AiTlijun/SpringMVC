import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jun.top.dispatchServlet.DispatchServlet;


public class Test {

	private static Map<String, Object> fileMap = new HashMap<String, Object>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String packagePath ="com.jun.top";
		//packagePath = packagePath.replaceAll("\\.", "/");
		//File file = new File(packagePath);
		packageScan(packagePath);
		//System.out.println("packagePath==:"+packagePath);
		//System.out.println("file==:"+file.isDirectory());
		System.out.println("fileMap.size==:"+fileMap.size());
		for(Entry<String, Object> entry: fileMap.entrySet())
        {
         System.out.println("Key: "+ entry.getKey()+ " Value: "+entry.getValue());
        }
		

	}

	private static void packageScan(String packagePath) {
		DispatchServlet dispatch = new DispatchServlet();
		packagePath = packagePath.replaceAll("\\.", "\\\\");
		URL url = dispatch.getClass().getClassLoader().getResource("\\\\"+packagePath);
		String pathFile = url.getFile();
		File file = new File(pathFile);
		if (file.isFile()) {
			fileMap.put(file.getName().replace(".class", ""), packagePath);
		} else {
			File[] listFiles = file.listFiles();
			if(listFiles != null){
				for (File fileNew : listFiles) {
					String path = fileNew.getAbsolutePath();
					packageScan(path);
				}
			}
			else{
				String path = file.getAbsolutePath();
				packageScan(path);
			}
		}
		System.out.println("branch");
	}
}
