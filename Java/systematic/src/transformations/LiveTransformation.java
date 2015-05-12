package transformations;

import static transformations.Constants.*;
import static util.Errors.*;
import static util.Objects.*;

import java.lang.reflect.*;
import java.util.*;

import db.*;
public class LiveTransformation {

	public static void main(final String[] args) throws Exception {
		bombUnless(args.length >= 1, "usage: LiveTransformation <QualifiedLoaderClass> [<loader arg1> <loader arg2> ...]");
		String loaderClassName = args[0];
		List<String> loaderArgs = rest(args);
		exitOnUncaughtExceptions(args, FAILURE_ADDRESS, "LiveTransformation");
		Db.beInReadOnlyMode();
        TransformationLoader loader = loader(loaderClassName, loaderArgs);
		TransformationUpdater updater = new TransformationUpdater(loader.name());
		loader.load(updater);
		TransformationReceiver receiver = new TransformationReceiver(updater);
		receiver.subscribe();
		new ServerHeartBeat(60, "LiveTransformation." + loader.name() + ".HEARTBEAT").start();
		updater.run();
	}

    static SeriesPublisher publisher;
    public static synchronized SeriesPublisher publisher() {
        if (publisher == null) 
            publisher = new LivePublisher();
        return publisher;
    }

	private static TransformationLoader loader(String loaderClassName, List<String> loaderArgs)
		throws NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException {
		Constructor<?> constructor = Class.forName(loaderClassName).getDeclaredConstructor(List.class);
		TransformationLoader loader = (TransformationLoader) constructor.newInstance(loaderArgs);
		return loader;
	}
}
