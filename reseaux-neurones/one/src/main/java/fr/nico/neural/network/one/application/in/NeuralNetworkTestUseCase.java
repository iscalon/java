package fr.nico.neural.network.one.application.in;

import fr.nico.neural.network.one.application.shared.NetworkProperties;
import java.util.function.UnaryOperator;

public interface NeuralNetworkTestUseCase {

  UnaryOperator<Number> testNetworkUsing(NetworkProperties properties);
}
