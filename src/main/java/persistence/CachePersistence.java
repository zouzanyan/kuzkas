package persistence;

public interface CachePersistence {
    //TODO AOF实现高容错

    // save
    public void save();

    // load
    public boolean load();


}
