package org.noerp.gradle.task

import org.gradle.api.DefaultTask
import org.noerp.gradle.conventions.RootProjectConvention

class UpdateTask extends DefaultTask {
	
	/**
	 * 构造方法
	 */
	UpdateTask(){
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