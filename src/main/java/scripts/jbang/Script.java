package scripts.jbang;

public interface Script {
    void execute(String[] args) throws Exception;
    String getCommandName();
}
