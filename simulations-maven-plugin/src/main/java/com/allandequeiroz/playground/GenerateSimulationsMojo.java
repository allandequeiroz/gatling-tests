package com.allandequeiroz.playground;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(goal = "generate", phase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateSimulationsMojo extends AbstractMojo {

   @Parameter(defaultValue = "${project}", readonly = true, required = true)
   private MavenProject mavenProject;

   private String sourceRoot;

   private static final String SCENARIOS_CONTENT_DIRECTORY = "scenarios";
   private static final String SIMULATIONS_CONTENT_DIRECTORY = "com/allandequeiroz/playground/simulations";
   private static final String CLASS_TOKEN = "<CLASS>";
   private static final String JSON_TOKEN = "<JSON>";
   private static final String SIMULATIONS_TEMPLATE = "SimulationsTemplate";
   private static final String JSON_EXTENSION = ".json";
   private static final String SCALA_EXTENSION = ".scala";

   public void execute() throws MojoExecutionException {
      sourceRoot = loadSourceRoot();

      final String resourcesRoot = loadResourcesRoot();

      try {

         final String simulationTemplate = readSimulationTemplate();

         final Path scenariosPath = Paths.get(resourcesRoot, SCENARIOS_CONTENT_DIRECTORY);

         Files.list(scenariosPath)
               .filter(path -> path.toString().endsWith(JSON_EXTENSION))
               .forEach(path -> writeToFiles(path.getFileName().toString(), simulationTemplate));

      } catch (IOException | FileGenerationFailedException ex) {
         throw new MojoExecutionException("Failed to generate test files", ex);
      }
   }

   private String loadSourceRoot() throws MojoExecutionException {
      if (mavenProject.getTestCompileSourceRoots().isEmpty()) {
         throw new MojoExecutionException("Test source root not found");
      }

      return mavenProject.getTestCompileSourceRoots().iterator().next();
   }

   private String loadResourcesRoot() throws MojoExecutionException {
      if (mavenProject.getTestResources().isEmpty()) {
         throw new MojoExecutionException("Test resources directory not found");
      }

      return mavenProject.getTestResources().iterator().next().getDirectory();
   }

   private void writeToFiles(final String scenarioFileName, final String simulationTemplate) {
      try {
         createSimulationsDirIfNotExists();

         final String simulationContent = parseSimulationTemplate(scenarioFileName, simulationTemplate);
         final String scenarioClassFile = scenarioFileName.substring(0, scenarioFileName.indexOf(JSON_EXTENSION)) + SCALA_EXTENSION;

         getLog().info("Generating test class: " + scenarioClassFile);

         final Path simulationsFilePath = Paths.get(sourceRoot, SIMULATIONS_CONTENT_DIRECTORY, scenarioClassFile);

         try (final BufferedWriter simulationWriter = Files.newBufferedWriter(simulationsFilePath)) {

            simulationWriter.write(simulationContent);
         }
      } catch (IOException ex) {
         throw new FileGenerationFailedException(ex.getMessage(), ex);
      }
   }

   private void createSimulationsDirIfNotExists() {
      final String simulationsPath = Paths.get(sourceRoot, SIMULATIONS_CONTENT_DIRECTORY).toString();
      final File files = new File(simulationsPath);

      if (!files.exists()) {
         if (files.mkdirs()) {
            getLog().info("Creating simulations directory");
         } else {
            getLog().error("Failed to create simulations directory");
         }
      }
   }

   private String parseSimulationTemplate(final String scenarioFileName, final String simulationTemplate) {
      final String baseName = scenarioFileName.substring(0, scenarioFileName.indexOf(JSON_EXTENSION));
      final String safeName = baseName.replaceAll("[^a-zA-Z0-9]", "_");
      return simulationTemplate.replace(CLASS_TOKEN, safeName).replace(JSON_TOKEN, scenarioFileName);
   }

   private String readSimulationTemplate() throws IOException {
      final ClassLoader classLoader = this.getClass().getClassLoader();
      final InputStream simulationTemplateInputStream = classLoader.getResourceAsStream(SIMULATIONS_TEMPLATE);
      return IOUtils.toString(simulationTemplateInputStream);
   }

   private static class FileGenerationFailedException extends RuntimeException {

      FileGenerationFailedException(final String message, final Throwable cause) {
         super(message, cause);
      }
   }

}
