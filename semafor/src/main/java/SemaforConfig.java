import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * Created by ramini on 1/18/16.
 */
public class SemaforConfig {
    private final String USER_HOME = System.getProperty("user.home");
    private final String FNM_RESOURCES_FOLDER = "fnmfiles";
    private final String FNM_INSTALLATION_FOLDER = USER_HOME + "/.fnmfiles";
    private static SemaforConfig instance;

    public static SemaforConfig getInstance() throws IOException {
        if(instance == null){
            instance = new SemaforConfig();
        }
        return instance;
    }

    private SemaforConfig() throws IOException {
        URL url = Resources.getResource(FNM_RESOURCES_FOLDER);
        File localFolder = FileUtils.toFile(url);
        FileUtils.copyDirectory(localFolder, new File(getSemaforHome()));
        setDefaultPropertiesRecursively(new File(getSemaforHome()));
    }

    public File getSemaforResource(String name) throws FileNotFoundException {
        File f = new File(getSemaforHome() + "/" + name);
        if(!f.exists()) throw new FileNotFoundException("File " + name + " was not found on temp path " + getSemaforHome() + "/" + name);
        return f;
    }

    public String getSemaforHome(){
        return FNM_INSTALLATION_FOLDER;
    }

    public File getPreprocessorScript() throws FileNotFoundException{
        return getSemaforResource("preprocess.sh");
    }

    private void setDefaultPropertiesRecursively(File root){

        if(!root.isDirectory()){
            root.setReadable(true);
            root.setWritable(true);
            root.setExecutable(true);
            return;
        }
        for(File c : root.listFiles()){
            setDefaultPropertiesRecursively(c);
        }
    }
}
