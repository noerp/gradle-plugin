package org.noerp.gradle

import javax.inject.Inject;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.internal.reflect.Instantiator;

import org.noerp.gradle.conventions.RootProjectConvention
import org.noerp.gradle.conventions.SubProjectConvention
import org.noerp.gradle.extention.NoerpExtention
import org.noerp.gradle.task.*


/**
 * NoERP插件
 * 
 * @author Kevin
 *
 */
class NoerpPlugin implements Plugin<Project> {

	/**
	 * 插件名称
	 */
	static final String NOERP_PLUGIN_NAME = "noerp"

	/**
	 * 根项目
	 */
	private Project project
	private final Instantiator instantiator;
	
	@Inject
	public NoerpPlugin(Instantiator instantiator) {
		this.instantiator = instantiator;
	}
	
	/**
	 * 应用插件
	 * 
	 * @param project
	 */
	void apply(final Project project) {
		this.project = project
		
		init()
		addConventions()
		setupTasks()
	}

	/**
	 * 默认配置
	 */
	private void addConventions() {
		
		//根项目约定设置
		RootProjectConvention rootProjectConvention = instantiator.newInstance(RootProjectConvention.class, project)
		project.getConvention().getPlugins().put("noerp", rootProjectConvention)
		
		//子项目约定设置
		project.subprojects.each {subproject->
			SubProjectConvention subProjectConvention = instantiator.newInstance(SubProjectConvention.class, project)
			subproject.getConvention().getPlugins().put("noerp-sub", subProjectConvention)
		}
	}

	/**
	 * 初始化
	 */
	void init() {
		
		//子项目配置
		project.subprojects.each {subproject->
		
			//默认启用java插件
			if(!subproject.plugins.hasPlugin(JavaPlugin)){
				subproject.apply(plugin: JavaPlugin)
			}
			
			//默认启用groovy插件
			if(!subproject.plugins.hasPlugin(GroovyPlugin)){
				subproject.apply(plugin: GroovyPlugin)
			}
		}
		
		//顶级项目扩展配置
		project.extensions.create("noerp", NoerpExtention)
	}

	/**
	 * 设置任务
	 */
	void setupTasks(){

		project.task("install", type: InstallTask){
			description = "Install noerp application from a java jar package"
		}
		
		project.task("update", type: UpdateTask){
			description = "Update noerp vendors."
		}

		project.task("start", type: RunTask){
			description = "Start your application."
		}

		project.task("startDebug", type: RunTask){
			description = "Start your application with debug model."
			debug = true
		}

		project.task("stop", type: RunTask){
			description = "Stop your noerp application."
			args = ["-shutdown"]
		}
		
		project.task("status", type: RunTask){
			description = "Get current status of your application."
			args = ["-status"]
		}
		
		project.task("loadSeed", type: RunTask){
			description = "Load ONLY the seed data for your application."
			args = ["load-data", "readers=seed"]
		}
		
		project.task("loadFile", type: RunTask){
			description = "Load data using the command line argument 'data-file' to load data."
			args = ["load-data", "readers=seed"]
		}
		
		project.task('loadAdminUserLogin', type: RunTask){
			description = "Create admin user with temporary password equal to noerp. You must provide userLoginId."
			/*
			args = ["load-data", "file=/runtime/tmp/AdminUserLoginData.xml"]
			
			doFirst {
				copy {
					from ("${rootDir}/vendor/noerp/resources/templates/AdminUserLoginData.xml") {
						filter(ReplaceTokens, tokens: [userLoginId: userLoginId])
					}
					into "${rootDir}/runtime/tmp/"
				}
			}
			
			doLast {
				delete("${rootDir}/runtime/tmp/AdminUserLoginData.xml");
			}
			*/
		}
		
		project.task("runTest", type: RunTask){
			description = "Run NoERP default tests; you have to manually execute 'gradle load-demo' before (and if needed even clear your data before) and see results in runtime/logs/test-results/html/all-tests.html. Use -Dportoffset=portNumber to shift all ports with the portNumber value."
			args = ["test"]
		}

		 project.task("createComponent", type: RunTask){
		 }
		 
		 project.task("generateCRUD", type: RunTask){
		 }
	}
}
