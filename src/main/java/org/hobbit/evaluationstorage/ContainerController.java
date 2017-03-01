/**
 * This file is part of evaluation-storage.
 *
 * evaluation-storage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * evaluation-storage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with evaluation-storage.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hobbit.evaluationstorage;

/**
 * Allows containers to be controlled.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public interface ContainerController {

    /**
     * Start an instance of the given image using the given
     * environment variables.
     * @param imageName the name of the image of the docker container
     * @param envVariables environment variables that should be added to the created container
     * @return the id of the container or null if an error occurred
     */
    String createContainer(String imageName, String[] envVariables);
    /**
     * @param containerId the id of the container that should be stopped
     */
    void stopContainer(String containerId);

}
