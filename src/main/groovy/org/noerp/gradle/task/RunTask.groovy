package org.noerp.gradle.task

import org.gradle.api.tasks.JavaExec
import org.noerp.gradle.conventions.RootProjectConvention

/**
 * 运行任务
 * 
 * @author Kevin
 *
 */
class RunTask extends JavaExec {
	
	/**
	 * 构造方法
	 */
	RunTask(){
		super()
		configTask()
	}
	
	@Override
	public void exec(){
		
		//add each project runtime classpath to root classpath
		project.subprojects.each {subproject->
			//classpath = classpath + subproject.sourceSets.main.runtimeClasspath
		}
		
		classpath.each{
			println it
		}
		
		println getCommandLine()
		
		super.exec()
	}

	/**
	 * 配置任务
	 */
	void configTask(){
		
		RootProjectConvention pluginConvention = project.convention.plugins.noerp
		
		group = pluginConvention.taskGroup
		classpath += pluginConvention.runClasspath
		conventionMapping.main = { pluginConvention.mainClassName }
		conventionMapping.jvmArgs = { pluginConvention.runDefaultJvmArgs }
	}
}