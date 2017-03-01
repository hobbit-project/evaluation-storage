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
 * This class defines constants of the hobbit platform.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public final class Constants {

    private Constants() {
    }

    //=============== ENVIRONMENT CONSTANTS ===============

    public static final String RIAK_NODES = "HOBBIT_RIAK_NODES";
    public static final String RIAK_HTTP_PORT_BASE = "HOBBIT_RIAK_HTTP_PORT_BASE";
    public static final String RIAK_PB_PORT_BASE = "HOBBIT_RIAK_PB_PORT_BASE";

}
