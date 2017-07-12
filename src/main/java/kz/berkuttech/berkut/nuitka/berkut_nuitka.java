/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Universe Voids
 */

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;


public class berkut_nuitka {
    
    //private static Logger log = Logger.getLogger(berkut_nuitka.class.getName()); 
    //использовать для записи/вывода, для изменений с гитом, и при коде возврата
    //log.info("Крч, пытаемся вывести path и прочее");
    
    
    public static class Directory {
    String path;
    String modulePrefix;
    ArrayList<String> modules;
    
    public String out(){
        return "path =" + this.path + "\nmodulePrefix =" + this.modulePrefix + "\nmodules =" + this.modules;
    }
    
    public Directory(String path, String modulePrefix,ArrayList<String> modules){
        this.path = path;
        this.modulePrefix = modulePrefix;
        this.modules = modules;
        }
    }

    
    public static class NuitkaSettings{
    ArrayList<Directory> directories;
    ArrayList<String> nuitka_options;

    public NuitkaSettings(ArrayList<Directory> directories, ArrayList<String> nuitka_options){
        this.directories = directories;
        this.nuitka_options = nuitka_options;
    }
    public String out(){
        String s = "directories =";
        for (Directory directory : directories) {
                s = s + directory.out() + "\n"; 
        }
        s = s + "nuitka_options =";
        for (String no: nuitka_options){
            s = s + no;
        }
        return s;
    }
    }
    
    public static void run_command(String... args) 
            throws IOException, InterruptedException{
            ProcessBuilder prBuild = new ProcessBuilder(args).inheritIO();  
            Process process = prBuild.start();
            int exitVal = process.waitFor();
        }
    
    public static String clone_repo(String url) throws IOException, InterruptedException{
        Path temp_directory = Files.createTempDirectory("nuitka"); 
        System.out.println(temp_directory);
        Path repo_dir = Paths.get(temp_directory.toString(), "repo");
        run_command("git", "clone", url, repo_dir.toString());
        return temp_directory.toString();
    }
    
    public static String clone_repo(String url, String branch)throws IOException, InterruptedException{
        Path temp_directory = Files.createTempDirectory("nuitka"); 
        System.out.println(temp_directory);
        Path repo_dir = Paths.get(temp_directory.toString(), "repo"); //формируют путь к дер.
        run_command("git", "clone", url, repo_dir.toString());
        Path git_dir = Paths.get(repo_dir.toString(), ".git");
        run_command("git", "--git-dir=" + git_dir, "--work-tree=" + repo_dir.toString(), "checkout", branch);
        return temp_directory.toString();
    }
    
    public static void nuitka_compile(String temp_dir) throws IOException{
        String repo = Paths.get(temp_dir, "repo").toString();
        
            String nuitka_json = Paths.get(repo, "nuitka.json").toString();
            File file = new File(nuitka_json);
            byte[] data;
            try (FileInputStream fis = new FileInputStream(file)) {
                data = new byte[(int) file.length()];
                fis.read(data);
            }
            
            String json = new String(data, "UTF-8");
            Gson gson = new Gson();
            NuitkaSettings ns = gson.fromJson(json, NuitkaSettings.class);
            System.out.println(ns.out());
            String st_git = Paths.get(repo, ".git").toString();
            rmdir(st_git);
            
            for( Directory directory: ns.directories){
                String st = Paths.get(repo, directory.path).toString();
                for (String module: directory.modules){
                    String module_path = Paths.get(st, module).toString();
                    ArrayList<String> command = new ArrayList<>();
                    command.add("nuitka");
                    command.addAll(ns.nuitka_options);
                    if(directory.modulePrefix != null){
                        command.add("--module-prefix=" + directory.modulePrefix);
                    }else{
                        String module_prefix = directory.path.replace('/' , '.');
                        command.add("--module-prefix=" + module_prefix);
                    }
                    command.add(module_path);
                    System.out.println(command);
                
                }
            
            }
            
    
    }
    
    public static void rmdir(String dir_name) throws IOException{
        FileUtils.deleteDirectory(new File(dir_name));
    }
    
    public static void main(String[] args) {
        
        
        try {
            String temp_dir = clone_repo("https://github.com/UniverseVoids/KEK.git", "master");
            nuitka_compile(temp_dir);
            rmdir(temp_dir);

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(berkut_nuitka.class.getName()).log(Level.SEVERE, "ТАКИ КОСЯК", ex);
        } 
        
        
    }
}


