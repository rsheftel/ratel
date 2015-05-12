package db;

public abstract class SyntheticColumn<T> extends Column<T> {

    private static final long serialVersionUID = 1L;
    
	public SyntheticColumn(String name, String type, boolean nullable, String identity) {
		super(name, type, nullable, identity);
	}
	
	public @Override abstract String string(T t);

	public @Override boolean isConcrete() { 
	    return false;
	}
}
