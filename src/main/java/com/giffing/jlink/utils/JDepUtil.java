package com.giffing.jlink.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;

import com.giffing.jlink.context.ProcessingContext;


public class JDepUtil {

	public Set<String> getModulesForBuildingJavaRuntime(String jarFilePath, String pathToSearch, Log logger, ProcessingContext context) {
		Set<String> modules = new HashSet<>();
		
		List<String> commands = new ArrayList<>();
		commands.add("\"" + context.prependJavaPathToCommand("jdeps") + "\"");
		commands.add("-cp");
		commands.add("'/'");
		commands.add("--multi-release");
		commands.add(String.valueOf(context.getJdkVersion()));
		if(pathToSearch != null) {
//			commands.add("--module-path");
//			commands.add("\"" + pathToSearch + "\"");
		}
		commands.add("-recursive");
		commands.add("-s");
		commands.add("\"" + jarFilePath + "\"");
		
		logger.info(commands.stream().collect(Collectors.joining(" ")));
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(commands);
			Process start;
			start = processBuilder.start();
			StringWriter errors = new StringWriter();
			
			int result = start.waitFor();
			IOUtils.copy(start.getInputStream(), new PrintWriter(errors), "UTF-8");
			
			if (result <= 0) {
				IOUtils.readLines(new StringReader(errors.toString())).forEach(line -> {
					String[] lineParts = line.split("->");
					if (lineParts.length > 1) {
						String modulename = lineParts[1].trim();
						if (!modulename.contains("not found") && !modulename.contains(".jar") && (modulename.startsWith("java.") || modulename.startsWith("jdk."))) {
							modules.add(modulename);
						}
					}
				});
			} else {
				logger.error(errors.toString());
			}
		} catch(Exception e) {
			throw new IllegalStateException(e);
		}
		
		return modules;
	}
	
}
