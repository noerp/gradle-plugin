package org.noerp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.Delete
import org.gradle.api.artifacts.Configuration
import org.gradle.internal.reflect.Instantiator
import org.gradle.plugins.ide.api.XmlFileContentMerger
import org.gradle.plugins.ide.eclipse.GenerateEclipseProject
import org.gradle.plugins.ide.eclipse.GenerateEclipseJdt
import org.gradle.plugins.ide.eclipse.GenerateEclipseClasspath
import org.gradle.plugins.ide.eclipse.internal.EclipseNameDeduper
import org.gradle.plugins.ide.eclipse.model.BuildCommand
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath
import org.gradle.plugins.ide.eclipse.model.EclipseModel

import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

/**
 * <p>A plugin which generates Eclipse files.</p>
 */
class NoerpEclipsePlugin implements Plugin<Project> {

	static final String ECLIPSE_TASK_NAME = "eclipse"
	static final String ECLIPSE_JDT_TASK_NAME = "eclipseJdt"
	static final String ECLIPSE_CP_TASK_NAME = "eclipseClasspath"

	private Task lifecycleTask
	private Task cleanTask
	private Project project
	private EclipseModel model

	private final Instantiator instantiator

	@Inject
	NoerpEclipsePlugin(Instantiator instantiator) {
		this.instantiator = instantiator
	}

	void apply(final Project target) {
		project = target
		lifecycleTask = project.tasks.findByName(ECLIPSE_TASK_NAME)
		cleanTask = project.tasks.findByName(cleanName(ECLIPSE_TASK_NAME))

		NoerpEclipsePluginConvention pluginConvention = instantiator.newInstance(NoerpEclipsePluginConvention.class)
		project.getConvention().getPlugins().put("noerp-eclipse", pluginConvention)

		model = project.extensions.create("noerp-eclipse", EclipseModel)

		configureEclipseProject()
		configureEclipseJdt()
		configureEclipseClasspath()
	}

	/**
	 * 创建项目.project文件
	 */
	private void configureEclipseProject() {
		project.getTasks().withType(GenerateEclipseProject.class){GenerateEclipseProject task ->
			def projectModel = task.projectModel
			projectModel.buildCommand "org.eclipse.jdt.core.javabuilder"
			projectModel.natures "org.eclipse.jdt.core.javanature"
			projectModel.natures.add(projectModel.natures.indexOf("org.eclipse.jdt.core.javanature"), "org.eclipse.jdt.groovy.core.groovyNature")
		}
	}

	/**
	 * 配置classspath
	 */
	private void configureEclipseClasspath() {

		model.classpath = instantiator.newInstance(EclipseClasspath, project)
		model.classpath.conventionMapping.defaultOutputDir = { new File(project.projectDir, 'bin/tmp') }

		maybeAddTask(ECLIPSE_CP_TASK_NAME, GenerateEclipseClasspath) { task ->

			//task properties:
			description = "Generates the Eclipse classpath file."
			inputFile = project.file('.classpath')
			outputFile = project.file('.classpath')

			//model properties:
			classpath = model.classpath
			classpath.file = new XmlFileContentMerger(xmlTransformer)

			//add subproject source dir
			classpath.sourceSets = {
				def sourceSetContainer = new ArrayList<SourceSet>()
				project.subprojects.each {subproject->
					sourceSetContainer.addAll(subproject.sourceSets)
				}
				sourceSetContainer.iterator()
			}

			project.afterEvaluate {
				// keep the ordering we had in earlier gradle versions
				Set<String> containers = new LinkedHashSet<String>()

				String container = "org.eclipse.jdt.launching.JRE_CONTAINER/" +
						"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/${model.jdt.getJavaRuntimeName()}/"

				containers.add(container)
				containers.addAll(classpath.containers)
				classpath.containers = containers
			}

			classpath.conventionMapping.plusConfigurations = {
				def configurationContainer = new ArrayList<Configuration>()
				project.subprojects.each {subproject->
					configurationContainer.addAll([
						subproject.configurations.testRuntime,
						//subproject.configurations.compileOnly,
						//subproject.configurations.testCompileOnly
					])
				}
				return configurationContainer
			}

			classpath.conventionMapping.classFolders = {
				def filesContainer = new ArrayList<File>()
				project.subprojects.each {subproject->
					def outputDirs = (subproject.sourceSets.main.output.dirs + subproject.sourceSets.test.output.dirs) as List
					filesContainer.addAll(outputDirs)
				}
				return filesContainer
			}

			task.dependsOn {
				def dependFilesContainer = new ArrayList<File>()
				project.subprojects.each {subproject->
					dependFilesContainer.add(subproject.sourceSets.main.output.dirs)
					dependFilesContainer.add(subproject.sourceSets.test.output.dirs)
				}
				return dependFilesContainer
			}
		}
	}

	/**
	 * 配置settings文件
	 */
	private void configureEclipseJdt() {
		maybeAddTask(ECLIPSE_JDT_TASK_NAME, GenerateEclipseJdt) {
			//task properties:
			description = "Generates the Eclipse JDT settings file."
			outputFile = project.file('.settings/org.eclipse.jdt.core.prefs')
			inputFile = project.file('.settings/org.eclipse.jdt.core.prefs')
			//model properties:
			model.jdt = jdt
			jdt.conventionMapping.sourceCompatibility = {
				project.convention.getPlugin(NoerpEclipsePluginConvention).sourceCompatibility
			}
			jdt.conventionMapping.targetCompatibility = {
				project.convention.getPlugin(NoerpEclipsePluginConvention).targetCompatibility
			}
			jdt.conventionMapping.javaRuntimeName = {
				String.format("JavaSE-%s", project.convention.getPlugin(NoerpEclipsePluginConvention).targetCompatibility)
			}
		}
	}

	private void maybeAddTask(String taskName, Class taskType, Closure action) {
		if (project.tasks.findByName(taskName)) {
			return
		}
		def task = project.tasks.create(taskName, taskType)
		project.configure(task, action)

		addWorker(task)
	}

	private void addWorker(Task worker, boolean includeInClean = true) {
		lifecycleTask.dependsOn(worker)
		Delete cleanWorker = project.tasks.create(cleanName(worker.name), Delete.class)
		cleanWorker.delete(worker.getOutputs().getFiles())
		if (includeInClean) {
			cleanTask.dependsOn(cleanWorker)
		}
	}

	public Task getCleanTask() {
		return cleanTask;
	}

	public Task getCleanTask(Task worker) {
		return project.getTasks().getByName(cleanName(worker.getName()));
	}

	private String cleanName(String taskName) {
		return String.format("clean%s", StringUtils.capitalize(taskName));
	}
}
