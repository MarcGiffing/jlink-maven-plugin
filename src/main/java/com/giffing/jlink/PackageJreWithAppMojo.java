package com.giffing.jlink;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.giffing.jlink.context.ProcessingContext;
import com.giffing.jlink.utils.JDepUtil;
import com.giffing.jlink.utils.ZipUtil;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "jlink")
public class PackageJreWithAppMojo extends AbstractMojo {

	
	@Parameter(defaultValue = "${project.build.directory}/", readonly = true)
	private File buildDirectory;
	
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String finalBuildname;
	
	@Parameter(required = false)
	private File javaDirectory;
	
	@Parameter(required = false)
	private Integer jdkVersion = 13;
	
	private String deployDirectoryname = "java_deploy";
	
	final private JDepUtil jdepHelper;
	
	public PackageJreWithAppMojo() {
		jdepHelper = new JDepUtil();
	}

	public void execute() throws MojoExecutionException {
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ProcessingContext context = new ProcessingContext()
				.setBuildDirectory(buildDirectory)
				.setFinalBuildname(finalBuildname)
				.setJavaDirectory(javaDirectory)
				.setJdkVersion(jdkVersion)
				.setDeployDirectoryname(deployDirectoryname);
		
		

		try {
			FileUtils.deleteDirectory(new File(context.getBuildDirectoryWithFilenalBuildName()));
			FileUtils.deleteDirectory(new File(context.getDeployDirectory()));
			
			getLog().info(context.getAbsolutTargetJar());
			getLog().info(context.getBuildDirectoryWithFilenalBuildName());
			
			ZipUtil.unzipFile(context.getAbsolutTargetJar(), context.getBuildDirectoryWithFilenalBuildName());

			
			Set<String> projectModules = jdepHelper.getModulesForBuildingJavaRuntime(context.getBuildDirectoryWithFilenalBuildName(), null, getLog(), context);
			Set<String> dependencyModules = Stream.of(new File(context.getBuildDirectoryWithFilenalBuildName() + "/BOOT-INF/lib/").listFiles())
					.filter(f -> f.getName().toLowerCase().endsWith(".jar"))
					.<Set<String>>map(f -> jdepHelper.getModulesForBuildingJavaRuntime(f.getPath(), f.getParent(), getLog(), context))
					.flatMap(e -> e.stream())
					.collect(Collectors.toSet());
			
			getLog().info("Project modules:");
			getLog().info(projectModules.stream().collect(Collectors.joining(",")));
			getLog().info("Dependency modules:");
			getLog().info(dependencyModules.stream().collect(Collectors.joining(",")));
			
			Set<String> modules = Stream.concat(projectModules.stream(), dependencyModules.stream()).collect(Collectors.toSet());
			getLog().info("All modules:");
			getLog().info(modules.stream().collect(Collectors.joining(",")));
			
			buildJavaRuntime(modules, context);
			copyJarToJavaDirectory(context);
			createBatchFileToCallJavaProgram(context);
			
			FileUtils.deleteDirectory(new File(context.getBuildDirectoryWithFilenalBuildName()));
			
		} catch (Exception e) {
			getLog().error(e.getCause());
		}
	}

	private void createBatchFileToCallJavaProgram(ProcessingContext context) {
		String batchContent = "\"./bin/java.exe\" -jar " + context.getAbsolutTargetJar() + "\r\n pause";
		File destFile = new File(context.getDeployDirectory() + "/" + finalBuildname + ".cmd");
		try {
			FileUtils.write(destFile, batchContent, "UTF-8");
		} catch (IOException e) {
			getLog().error("Couldn't create command file in java target directory", e);
		}
		
	}

	private void copyJarToJavaDirectory(ProcessingContext context) {
		String sourceJar = context.getBuildDirectoryWithFilenalBuildName() + ".jar";
		getLog().info(finalBuildname);
		File destFile = new File(context.getDeployDirectory() + "/" + context.getJarFilename());
		try {
			FileUtils.copyFile(new File(sourceJar), destFile);
		} catch (IOException e) {
			getLog().error("Problem coping jar (" + sourceJar + ") to java directory (" + destFile.toString() + "): ", e);
		}
		
	}

	private File buildJavaRuntime(Set<String> modules, ProcessingContext context) throws IOException, InterruptedException {

		List<String> commands = new ArrayList<>();
		commands.add(context.prependJavaPathToCommand("jlink"));
		commands.add("--no-header-files");
		commands.add("--no-man-pages");
		commands.add("--compress=2");
		commands.add("--add-modules");
		commands.add(modules.stream().collect(Collectors.joining(",")));
		commands.add("--output");
		commands.add(context.getDeployDirectory());
		
		getLog().info(commands.stream().collect(Collectors.joining(" ")));
		
		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		Process start;
		start = processBuilder.start();
		StringWriter errors = new StringWriter();

		int result = start.waitFor();
		IOUtils.copy(start.getInputStream(), new PrintWriter(errors), "UTF-8");

		if (result <= 0) {
			getLog().info("Java Runtime successfully builded");
		} else {
			getLog().error("Problem building Java Runtime");
		}
		
		return new File(context.getDeployDirectory());
	}



}
