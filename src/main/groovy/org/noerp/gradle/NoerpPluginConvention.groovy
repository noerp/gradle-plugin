package org.noerp.gradle

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

/**
 * rootProject默认配置
 * 
 * @author Kevin
 *
 */
class NoerpPluginConvention {
	
	/**
	 * 任务分组
	 */
	String taskGroup = "NoERP"
	
	/**
	 * 主方法全名
	 */
	String mainClassName
	
	/**
	 * 查找主方法的classpath
	 */
	FileCollection runClasspath
	
	/**
	 * 默认jvm参数
	 */
	Iterable<String> runDefaultJvmArgs = []
	
	/**
	 * 构造方法
	 * 
	 * @param project
	 */
	public NoerpPluginConvention(Project project) {
		runClasspath = project.files("bin/noerp.jar")
		mainClassName = "org.noerp.base.start.Start"
		runDefaultJvmArgs = ["-Xms128M", "-Xmx512M", "-Dfile.encoding=UTF-8"]
	}
	
}