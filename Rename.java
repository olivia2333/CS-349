import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Rename {

    private static LinkedHashMap<String, ArrayList<String>> arguments = new LinkedHashMap<>();

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
        } else {
            parse(args);

            if (!arguments.isEmpty()) {
                renameFiles();
            }
        }
    }

    static void printHelp(){
        System.out.println("(c) 2021 Olivia Ma. Revised: May 19, 2021.");
        System.out.println("Usage: rename [-option argument1 argument2 ...]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("-f|file [filename]          :: file(s) to change.");
        System.out.println("-p|prefix [string]          :: rename [filename] so that it starts with [string]. ");
        System.out.println("-s|suffix [string]          :: rename [filename] so that it ends with [string]. ");
        System.out.println("-r|replace [str1] [str2]    :: rename [filename] by replacing all instances of [str1] with [str2].");
        System.out.println("-h|help                     :: print out this help and exit the program.");
    }

    static void parse(String[] args){
        ArrayList<String> validOption =
                new ArrayList<>(Arrays.asList("f", "file", "p", "prefix", "s", "suffix", "r", "replace", "h", "help"));
        String option = null;
        ArrayList<String> value = new ArrayList<>();

        for (String entry : args) {
            // print help
            if (entry.equals("-h") || entry.equals("-help")) {
                arguments.clear();
                printHelp();
                System.exit(0);
            }
        }

        // iterate through arguments
        for (String entry : args) {
            // options
            if (entry.startsWith("-")){
                if (option != null && value.size() > 0){
                    // check if option near one another
                    // store previous file options
                    if (arguments.containsKey(option)) {
                        System.out.println("ERROR: option \"" + option + "\" being specified more than once");
                        parseInvalid();
                    }
                    if (option.equals("replace")) {
                        if (value.size() != 2) {
                            System.out.println("ERROR: option \"" + option + "\" must have 2 values");
                            parseInvalid();
                        }
                    }
                    arguments.put(option, value);
                    option = null;
                    value = new ArrayList<>();;
                } else if (option != null) {
                    System.out.println("ERROR: option \"" + option + "\" has no values provided");
                    parseInvalid();
                }

                option = entry.substring(1);
                if (!validOption.contains(option)){
                    // check option validity
                    System.out.println("ERROR: invalid option \"" + option + "\"");
                    parseInvalid();
                } else {
                    // translate option to standard ones
                    if (option.equals("f")) {
                        option = "file";
                    } else if (option.equals("p")) {
                        option = "prefix";
                    } else if (option.equals("s")) {
                        option = "suffix";
                    } else if (option.equals("r")) {
                        option = "replace";
                    }
                }

            } else {
                // values
                if (option == null) {
                    // no option
                    System.out.println("ERROR: no option provided for value \"" + entry + "\"");
                    parseInvalid();
                }

                if (entry.equals("@date")) {
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                    entry = date.format(formatter);
                } else if (entry.equals("@time")) {
                    LocalTime time = LocalTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh-mm-ss");
                    entry = time.format(formatter);
                }

                if (option.equals("file")) {
                    if (value.contains(entry)) {
                        System.out.println("ERROR: duplicate file name \"" + entry  + "\" given");
                        parseInvalid();
                    } else {
                        value.add(entry);
                    }
                } else if (option.equals("prefix")) {
                    value.add(entry);
                } else if (option.equals("suffix")) {
                    value.add(entry);
                } else if (option.equals("replace")) {
                    if (value.size() == 2) {
                        System.out.println("ERROR: option \"replace\" can only have two values");
                        parseInvalid();
                    } else {
                        value.add(entry);
                    }
                }
            }

        }

        if (option != null && value.size() > 0){
            // store file options
            if (arguments.containsKey(option)) {
                System.out.println("ERROR: \"" + option + "\" being specified more than once");
                parseInvalid();
            }
            if (option.equals("replace")) {
                if (value.size() != 2) {
                    System.out.println("ERROR: \"replace\" must have 2 values");
                    parseInvalid();
                }
            }
            arguments.put(option, value);
            option = null;
            value = new ArrayList<>();
        } else if (option != null) {
            System.out.println("ERROR: option \"" + option + "\" has no value provided");
            parseInvalid();
        }else if (value.size() > 0) {
            System.out.println("ERROR: value \"" + value + "\" has no option provided");
            parseInvalid();
        }

        if (arguments.containsKey("file") && arguments.size() <= 1) {
            System.out.println("ERROR: no options given");
            parseInvalid();
        } else if (!arguments.containsKey("file")) {
            System.out.println("ERROR: no file name given");
            parseInvalid();
        }

    }

    static void renameFiles(){
        ArrayList<String> fileNames = arguments.get("file");
        LinkedHashMap<String, String> renameMap = new LinkedHashMap<>();
        for (String files : fileNames) {
            renameMap.put(files, files);
        }

        for (String originName : renameMap.keySet()) {
            boolean success = true;

            for (Map.Entry<String, ArrayList<String>> entry : arguments.entrySet()) {
                String option = entry.getKey();
                ArrayList<String> value = entry.getValue();
                String new_name = null;

                if (!option.equals("file")) {
                    String curr_name = renameMap.get(originName);
                    if (option.equals("prefix")) {
                        new_name = curr_name;
                        for (String val : value) {
                            new_name = val.concat(new_name);
                        }
                    } else if (option.equals("suffix")) {
                        new_name = curr_name;
                        for (String val : value) {
                            new_name = new_name.concat(val);
                        }
                    } else if (option.equals("replace")) {
                        String toReplace = value.get(0);
                        String replace = value.get(1);
                        new_name = curr_name.replaceAll(toReplace, replace);
                        if (new_name.equals(curr_name)) {
                            System.out.println("Error: " + toReplace + " is not contained in " + curr_name + ", cannot rename " + originName);
                            parseInvalid();
                        }
                    }

                    renameMap.put(originName, new_name);
                }
            }
        }

        checkRenameValidity(renameMap);
        for (String originName: renameMap.keySet()){
            File file = new File(originName);
            File new_file = new File(renameMap.get(originName));
            if (file.renameTo(new_file)){
                System.out.println("successfully renamed " + originName + " to " + new_file.toString());
            } else {
                System.out.println("failed to rename " + originName + " to " + new_file.toString());
            }
        }
    }

    static void checkRenameValidity(LinkedHashMap<String, String> renameMap){
        for (String origin_name: renameMap.keySet()){
            File origin = new File(origin_name);
            if (!origin.exists()){
                System.out.println(origin + " does not exist");
                parseInvalid();
            }
            if (origin.equals((renameMap.get(origin)))){
                System.out.println("ERROR: cannot rename " + origin + " to" + origin + ", since there is no change");
                parseInvalid();
            }
        }

        String[] new_names = renameMap.values().toArray(new String[renameMap.values().size()]);
        String[] origin_names = renameMap.keySet().toArray(new String[renameMap.keySet().size()]);

        for (int i = 0; i < new_names.length; i++){
            if (renameMap.keySet().contains(new_names[i])){
                System.out.println("ERROR: cannot rename " + origin_names[i] + " to " + new_names[i] + ", " +
                        new_names[i] + " already exists");
                parseInvalid();
            }
            for (int j = i+1; j < new_names.length;j++){
                if (new_names[i].equals(new_names[j])){
                    System.out.println("ERROR: cannot rename both " + origin_names[i] + " and " + origin_names[j] +
                            " to " + new_names[j]);
                    parseInvalid();
                }
            }
        }
    }

    static boolean fileExistence(String fileName) {
        File file = new File(fileName);
        if (file.exists()){
            return true;
        } else {
            return false;
        }
    }

    static boolean rename(String oldFileName, String newFileName) {
        File originFile = new File(oldFileName);
        File newFile = new File(newFileName);

        boolean success = originFile.renameTo(newFile);
        return success;
    }

    static void parseInvalid() {
        arguments.clear();
        System.out.println();
        printHelp();
        System.exit(0);
    }

}
