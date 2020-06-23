/*
 * GeyserLink - The Missing Link
 * Copyright (C) 2020 GeyserLink Developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.com.grieve.geyserlink;

public interface IGeyserLinkLogger {
    /**
     * Logs a severe message to console
     *
     * @param message the message to log
     */
    void severe(String message);

    /**
     * Logs a severe message and an exception to console
     */
    void severe(String message, Throwable error);

    /**
     * Logs an error message to console
     *
     * @param message the message to log
     */
    void error(String message);

    /**
     * Logs an error message and an exception to console
     */
    void error(String message, Throwable error);

    /**
     * Logs a warning message to console
     *
     * @param message the message to log
     */
    void warning(String message);

    /**
     * Logs an info message to console
     *
     * @param message the message to log
     */
    void info(String message);

    /**
     * Logs a debug message to console
     *
     * @param message the message to log
     */
    void debug(String message);

}
