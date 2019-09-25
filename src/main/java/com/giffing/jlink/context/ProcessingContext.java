package com.giffing.jlink.context;

import java.io.File;

public class ProcessingContext {

	private File buildDirectory;
	
	private String finalBuildname;
	
	private File javaDirectory;
	
	private Integer jdkVersion;

	private String deployDirectoryname;
	
	public String getBuildDirectoryWithFilenalBuildName() {
		return buildDirectory.toString() + "/" + finalBuildname;
	}
	
	public String getAbsolutTargetJar() {
		return getBuildDirectoryWithFilenalBuildName() + ".jar";
	}
	
	public String getJarFilename() {
		return this.finalBuildname + ".jar";
	}
	
	public String getDeployDirectory() {
		return getBuildDirectory() + "/" + getDeployDirectoryname() + "/";
	}
	
	public File getBuildDirectory() {
		return buildDirectory;
	}

	public ProcessingContext setBuildDirectory(File buildDirectory) {
		this.buildDirectory = buildDirectory;
		return this;
	}

	public String getFinalBuildname() {
		return finalBuildname;
	}

	public ProcessingContext setFinalBuildname(String finalBuildname) {
		this.finalBuildname = finalBuildname;
		return this;
	}

	public Integer getJdkVersion() {
		return jdkVersion;
	}

	public ProcessingContext setJdkVersion(Integer jdkVersion) {
		this.jdkVersion = jdkVersion;
		return this;
	}

	public File getJavaDirectory() {
		return javaDirectory;
	}

	public ProcessingContext setJavaDirectory(File javaDirectory) {
		this.javaDirectory = javaDirectory;
		return this;
	}
	
	public String prependJavaPathToCommand(String command) {
		return javaDirectory != null ? javaDirectory.getAbsolutePath() + "/bin/" + command : command;
	}

	public String getDeployDirectoryname() {
		return deployDirectoryname;
	}

	public ProcessingContext setDeployDirectoryname(String deployDirectoryname) {
		this.deployDirectoryname = deployDirectoryname;
		return this;
	}
	
}
