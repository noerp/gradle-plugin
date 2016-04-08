package org.noerp.gradle.task

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.JavaExec
import org.noerp.gradle.NoerpPluginConvention

/**
 * 运行任务
 * 
 * @author Kevin
 *
 */
class RunTask extends JavaExec {
	
	/**
	 * 插件默认配置
	 */
	NoerpPluginConvention pluginConvention

	/**
	 * 配置任务
	 * @param pluginConvention
	 */
	void configTask(NoerpPluginConvention pluginConvention){
		this.pluginConvention = pluginConvention
		this.group = pluginConvention.taskGroup
		
		configRunArgs()
		configDependsOn()
	}
	
	/**
	 * 配置运行参数
	 */
	void configRunArgs(){
		this.classpath = this.pluginConvention.runClasspath
		this.conventionMapping.main = { this.pluginConvention.mainClassName }
		this.conventionMapping.jvmArgs = { this.pluginConvention.runDefaultJvmArgs }
	}
	
	/**
	 * 配置依赖关系
	 */
	void configDependsOn(){
		
	}
	
}