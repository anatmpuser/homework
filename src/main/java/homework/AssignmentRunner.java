package homework;

class AssignmentRunner {
    public static void main (String []args) {
        if (args.length < 2) usage();
        if (!args[0].equals("-action")) usage();
        if (args[1].equals("search")) {SeleniumSearchProcessor.searchRepos();}
        else if (args[1].equals("split") && args.length == 3) {
            ResultFileSplitter.split(args[2]);}
        else usage();
    }

    private static void usage() {
        System.out.println("Usage: \n java -jar homework.jar -action [search|split <file path>]\n where: <file path> is full path to ready search results file." );
        System.exit(1);
    }
}
