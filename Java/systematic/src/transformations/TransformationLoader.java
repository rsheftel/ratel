package transformations;

public interface TransformationLoader {

	void load(TransformationUpdater updater);
	String name();

}
