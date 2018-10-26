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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class BatchDownload {

	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.println("Usage: java BatchDownload <FILE_INPUT> <OUTPUT_PATH>\n");
			throw new Exception("Args error.");
		}
		
		// set user input
		final String linksFile = args[0].toLowerCase().trim();
		final String downloadPath = args[1].toLowerCase().trim();

		// get download links
		System.out.println("Reading links from input file...");
		final List<String> links = getLinksFromFile(linksFile);

		System.out.println("Getting final download links...");
		final List<String> downloads = getDownloadLinks(links);

		// download
		System.out.println("Finally starting batch download...\n");
		MultiThreadDownloader downloader = new MultiThreadDownloader();
		downloader.go(downloads, downloadPath);
	}

	private static List<String> getDownloadLinks(List<String> links) {
		final List<String> dlinks = new ArrayList<>();
		links.forEach(l -> {
			String dlink = evaluateUrl(l);
			if (dlink != null)
				dlinks.add(dlink);
		});
		return dlinks;
	}

	private static List<String> getLinksFromFile(String filename) {
		final List<String> lines = new ArrayList<>();
		try {
		    String filePath = new File(filename).getAbsolutePath();
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {
				lines.add(st);
			}
			br.close();
		} catch (IOException e) {
			System.out.println("\t**Error reading input file. "+e);
		}
		return lines;
	}

	private static String evaluateUrl(String link) {
		try {
			URL url = new URL(link);
			String prefix = link.substring(0, link.indexOf("/v/"));
			String body = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			for (String line; (line = reader.readLine()) != null;) {
				body = body + line;
			}
			String part = body.substring(body.indexOf("/d/") - 1);
			part = part.substring(0, part.indexOf(";"));

			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			return prefix + ((String) engine.eval(part));
		} catch (MalformedURLException e) {
			System.out.println("\t**Error reading download url: "+ link);
		} catch (IOException e) {
			System.out.println("\t**Error reading url content: " + link);
		} catch (ScriptException e) {
			System.out.println("\t**Error evaluating download url: " + link);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("\t**Error getting download url: " + link);
		}
		return null;
	}

}

final class MultiThreadDownloader {

	private class Downloader implements Runnable {
		private final URL url;
		private final String filename;

		public Downloader(final URL url, final String filename) {
			this.url = url;
			this.filename = filename;
		}

		@Override
		public void run() {
			try {
				final BufferedInputStream in = new BufferedInputStream(this.url.openStream());
				final FileOutputStream fileOutputStream = new FileOutputStream(this.filename);
				final byte dataBuffer[] = new byte[1024];
				int bytesRead;
				System.out.println("Downloading from link: " + url);
				while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
					fileOutputStream.write(dataBuffer, 0, bytesRead);
				}
				fileOutputStream.close();
				System.out.println("Downloaded to file: " + filename);
			} catch (IOException e) {
				System.out.println("\t**Error writing output file. "+e);
			}

		}
	}

	public void go(final List<String> downloads, final String downloadPath) {
		BlockingQueue<Runnable> runnables = new ArrayBlockingQueue<Runnable>(1024);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, runnables);

		try {
			for (String d : downloads) {
				String filename = downloadPath + d.substring(d.lastIndexOf("/") + 1);
				executor.submit(new Downloader(new URL(d), filename));
			}
		} catch (MalformedURLException e) {
			System.out.println("\t**Error parsing download urls.");
		}

		executor.shutdown();
	}

}
