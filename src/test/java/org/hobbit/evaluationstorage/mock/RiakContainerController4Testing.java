package org.hobbit.evaluationstorage.mock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hobbit.evaluationstorage.ContainerController;
import org.hobbit.utils.docker.DockerHelper;

public class RiakContainerController4Testing implements ContainerController {

    private String containerId = null;

    @Override
    public String createContainer(String imageName, String[] envVariables) {
        /*
         * Starts the Riak node and stores its containerId. Note that the method returns
         * the docker host since the program needs the host for communication instead of
         * the docker container id.
         */
        try {
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("run");
            command.add("--rm");
            command.add("-d");
            command.add("-p");
            command.add("8098:8098");
            command.add("-p");
            command.add("8087:8087");
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("docker   ");
            for (int i = 0; i < envVariables.length; ++i) {
                command.add("-e");
                command.add(envVariables[i]);
            }
            command.add(imageName);
            ProcessBuilder builder = new ProcessBuilder(command);
            Process p = builder.start();
            InputStream in = p.getInputStream();
            int exit = p.waitFor();
            if (exit != 0) {
                return null;
            }
            containerId = IOUtils.toString(in).trim();
            IOUtils.closeQuietly(in);
            return DockerHelper.getHost();
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't create container.", e);
        }
    }

    @Override
    public void stopContainer(String containerId) {
        /*
         * Stops the Riak Node. Note that the internal container id is used instead of
         * the given id (which is the host name of the docker host).
         */
        try {
            System.out.println("Shutting down container " + this.containerId);
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("stop");
            command.add(this.containerId);
            ProcessBuilder builder = new ProcessBuilder(command);
            Process p = builder.start();
            int exit = p.waitFor();
            if (exit != 0) {
                throw new IllegalStateException("Couldn't stop container. Exit code = " + exit);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't stop container.", e);
        }
    }
}
