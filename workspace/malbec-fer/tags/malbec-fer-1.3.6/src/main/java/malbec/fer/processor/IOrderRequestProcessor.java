package malbec.fer.processor;

import java.util.Map;

import malbec.fer.FerretState;
import malbec.fer.IOrderDestination;

public interface IOrderRequestProcessor {

    Map<String, String> process(Map<String, String> sourceMessage, Map<String, IOrderDestination> orderDestinations, FerretState ferretState);
}
