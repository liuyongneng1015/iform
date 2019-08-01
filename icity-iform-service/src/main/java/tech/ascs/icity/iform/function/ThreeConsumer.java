package tech.ascs.icity.iform.function;

/**
 * @author renjie
 * @since 0.7.3
 **/
@FunctionalInterface
public interface ThreeConsumer<F, F_1, F_2> {

    void accept(F f, F_1 f1, F_2 f2);
}
