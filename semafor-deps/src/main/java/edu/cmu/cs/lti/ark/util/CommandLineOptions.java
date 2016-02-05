/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das Language Technologies Institute, Carnegie Mellon University, All Rights Reserved.
 *
 * CommandLineOptions.java is part of SEMAFOR 2.0.
 *
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SEMAFOR 2.0.  If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.util;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import edu.cmu.cs.lti.ark.util.ds.Range;
import edu.cmu.cs.lti.ark.util.ds.map.SingleAssignmentHashMap;

/**
 * Processes command-line arguments and stores a variety of configuration parameters as option-value pairs. Options should be
 * formatted as optname:value (or simply the optname if boolean). See the code for details.
 *
 * @author Nathan Schneider (nschneid)
 */
public abstract class CommandLineOptions {
    public class InvalidOptionsException extends Exception {
        private static final long serialVersionUID = -4353285681883730567L;

        public InvalidOptionsException(String s) {
            super(s);
        }
    }

    public abstract class Option {
        public final String name;

        abstract void set(String value) throws InvalidOptionsException;

        public Option(String name) {
            this.name = name;
            opts.put(name, this);
        }

        public boolean present() {
            return args.containsKey(name);
        }

        public boolean absent() {
            return !present();
        }

        public abstract String toString();    // should call get(); necessary for string concatenation

        public String make() {
            return (present()) ? make(toString()) : "";
        }

        public String make(String v) {
            return name + ":" + v;
        }
    }

    public class StringOption extends Option {
        public String get() {
            return (String) args.get(name);
        }

        public StringOption(String name) {
            super(name);
        }

        public void set(String value) {
            args.put(name, value);
        }

        public String toString() {
            return toString(get());
        }

        public String toString(String v) {
            return v;
        }
    }

    /**
     * Option which is a path in the local file system
     */
    public class PathOption extends Option {
        public boolean isDirectory = false;

        public String get() {
            return toString();
        }

        public File getFile() {
            return (File) args.get(name);
        }

        public PathOption(String name) {
            super(name);
        }

        public void set(String path) throws InvalidOptionsException {
            set(new File(path));
        }

        public void set(File path) {
            args.put(name, path);
        }

        public String toString() {
            return toString(getFile());
        }

        public String toString(File v) {
            return v.getAbsolutePath();
        }

        public boolean exists() {
            return getFile().exists();
        }

        public boolean exists(String path) {
            return new File(path).exists();
        }

        public boolean parentDirectoryExists() {
            return getFile().getParentFile().exists();
        }

        public boolean parentDirectoryExists(String path) {
            return new File(path).getParentFile().exists();
        }
    }

    /**
     * Option which is a path that should already exist in the local file system
     */
    public class ExistingPathOption extends PathOption {
        public ExistingPathOption(String name) {
            super(name);
        }

        public void set(String path) throws InvalidOptionsException {
            if (!exists(path)) {
                throw new InvalidOptionsException("Path value of '" + name + "' option does not exist: " + path);
            } else {
                super.set(path);
            }
        }
    }

    /**
     * Option which is a path that should not yet exist in the local file system
     */
    public class NewPathOption extends PathOption {
        public NewPathOption(String name) {
            super(name);
        }

        public void set(String path) throws InvalidOptionsException {
            if (exists(path)) {
                throw new InvalidOptionsException("Path value of '" + name + "' option already exists: " + path);
            } else {
                super.set(path);
            }
        }
    }

    /**
     * Option which is a path to a file that should not exist yet, but whose parent directory should already exist in the local file
     * system
     */
    public class NewFilePathOption extends NewPathOption {
        public NewFilePathOption(String name) {
            super(name);
        }

        public void set(String path) throws InvalidOptionsException {
            if (!parentDirectoryExists(path)) {
                throw new InvalidOptionsException("Parent directory of the value of '" + name + "' option does not exist: " + path);
            } else {
                super.set(path);
            }
        }
    }

    public class IntOption extends Option {
        protected Range range;

        public int get() {
            return (Integer) args.get(name);
        }

        public IntOption(String name) {
            this(name, null);
        }

        public IntOption(String name, Range validRange) {
            super(name);
            range = validRange;
        }

        public void set(String value) throws InvalidOptionsException {
            int v = new Integer(value);
            if (range != null && !range.contains(v)) {
                throw new InvalidOptionsException("Integer value " + v + " for option " + this.name + " falls outside the legal range: " + range.toString());
            }
            args.put(name, v);
        }

        public String toString() {
            return toString(get());
        }

        public String toString(int v) {
            return Integer.toString(v);
        }

        public String make(int v) {
            return make(toString(v));
        }
    }

    public class DoubleOption extends Option {
        public double get() {
            return (Double) args.get(name);
        }

        public DoubleOption(String name) {
            super(name);
        }

        public void set(String value) {
            args.put(name, new Double(value));
        }

        public String toString() {
            return toString(get());
        }

        public String toString(double v) {
            return Double.toString(v);
        }

        public String make(double v) {
            return make(toString(v));
        }
    }

    public class BoolOption extends Option {
        public final boolean DEFAULT_VALUE = false;

        public boolean get() {
            return (Boolean) args.get(name);
        }

        public BoolOption(String name) {
            super(name);
            args.put(name, DEFAULT_VALUE);
        }

        public void set(String val) {
            assert (get() == false);
            args.reset(name);
            args.put(name, true);
        }

        public boolean present() {
            return get() == true;
        }

        public String toString() {
            return toString(get());
        }

        public String toString(boolean v) {
            return Boolean.toString(v);
        }

        public String make() {
            return make(present());
        }

        public String make(boolean v) {
            return (v) ? name : "";
        }
    }

    protected SingleAssignmentHashMap<String, Object> args = new SingleAssignmentHashMap<String, Object>();
    protected Map<String, Option> opts = new SingleAssignmentHashMap<String, Option>();

    protected CommandLineOptions() {
    }

    protected void init(String[] args, boolean ignoreOptions) {
        boolean ok = true;
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            String[] pair = args[i].split(":");

            String optName = pair[0];
            Option opt = getOptionByName(optName);
            if (opt == null) {
                System.err.println("Invalid option name: " + optName);
                ok = false;
            } else {
                String val = (pair.length > 1) ? pair[1].trim() : null;
                try {
                    opt.set(val);
                } catch (InvalidOptionsException ex) {
                    ex.printStackTrace();
                    ok = false;
                }
            }
        }
        if (!ok) {
            System.err.println("Exiting (invalid set of options)");
            System.exit(1);
        }
    }

    public Option getOptionByName(String optName) {
        return opts.get(optName);
    }

    /**
     * @return An iterator over ALL options (including ones without values)
     */
    public Iterator<Option> getIterator() {
        return opts.values().iterator();
    }

    /**
     * @return Generated argument string specifying all stored option-value pairs, in no particular order
     */
    public String make() {
        String s = "";
        for (Iterator<Option> iter = getIterator(); iter.hasNext(); ) {
            Option opt = iter.next();
            s += opt.make() + " ";
        }
        return s.trim();
    }

    /**
     * Generates a string specifying option-value pairs for particular options
     *
     * @param optionsToInclude The options, in the order they are to be listed in the string
     * @return Generated argument string
     */
    public static String make(Option[] optionsToInclude) {
        String s = "";
        for (Option opt : optionsToInclude) {
            s += opt.make() + " ";
        }
        return s.trim();
    }
}
