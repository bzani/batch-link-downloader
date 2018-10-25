package program;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class BatchDownload {
    
	public static final String DEFAULT_LINKS_FILE = "c:/dev/scripts/teste.txt";
	public static final String DEFAULT_DOWNLOAD_PATH = "c:/dev/scripts/";
	public static final int DEFAULT_THREADS = 5;
	
	public static void main(String[] args) {
        
    	// set default input
		String linksFile = DEFAULT_LINKS_FILE;
		String downloadPath = DEFAULT_DOWNLOAD_PATH;
		int threads = DEFAULT_THREADS;
		
		// set user input
		int i=0;
		for (String arg : args) {
    		switch(i) {
    			case 0: linksFile = arg.toLowerCase().trim(); break;
    			case 1: downloadPath = arg.toLowerCase().trim(); break;
    			case 2: threads = Integer.parseInt(arg); break;
    		}
    		i++;
    	}
		
		// get download links
		final List<String> links = getLinksFromFile(linksFile);
        final List<String> downloads = getDownloadLinks(links);
        
        // download
		for (String d : downloads) {
			String filename = downloadPath + d.substring(d.lastIndexOf("/")+1);
            downloadFile(d, filename, threads);
        }
    }
    
    private static List<String> getDownloadLinks(List<String> links) {
    	final List<String> dlinks = new ArrayList<>();
    	links.forEach(l->dlinks.add(evaluateUrl(l)));
		return dlinks;
	}

	private static List<String> getLinksFromFile(String filename) {
    	final List<String> lines = new ArrayList<>();
        try {
	    	File file = new File(filename); 
	        BufferedReader br = new BufferedReader(new FileReader(file)); 
	        String st; 
			while ((st = br.readLine()) != null) {
				lines.add(st);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return lines;
	}

	private static void downloadFile(String url, String filename, int threads) {
		try {
			final BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			final FileOutputStream fileOutputStream = new FileOutputStream(filename);
			final byte dataBuffer[] = new byte[1024];
			int bytesRead;
			//int mb = 0;
			System.out.println("============================");
			System.out.println("Starting download from link: " + url);
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
				//if (mb >= 1000) { mb = 0; System.out.print("."); } else { mb++; }
			}
			fileOutputStream.close();
			System.out.println("\nDownloaded to file: " + filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String evaluateUrl(String link) {
		try {
	        URL url = new URL(link);
	        String prefix = link.substring(0,link.indexOf("/v/"));
	        String body = "";
	        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
	        for (String line; (line = reader.readLine()) != null;) {
	            body = body + line;
	        }
	        String part = body.substring(body.indexOf("/d/")-1);
	        part = part.substring(0, part.indexOf(";"));
	        
	        ScriptEngineManager mgr = new ScriptEngineManager();
	        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        	return prefix + ( (String) engine.eval(part) );
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return "";
    }
}
