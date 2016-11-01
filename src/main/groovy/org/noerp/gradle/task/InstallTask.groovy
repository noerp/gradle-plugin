package org.noerp.gradle.task

import org.gradle.api.DefaultTask
import org.noerp.gradle.conventions.RootProjectConvention

class InstallTask extends DefaultTask {
	
	/**
	 * 构造方法
	 */
	InstallTask(){
		super()
		configTask()
	}

	/**
	 * 配置任务
	 */
	void configTask(){
		
		RootProjectConvention pluginConvention = project.convention.plugins.noerp
		
		this.group = pluginConvention.taskGroup
	}

}