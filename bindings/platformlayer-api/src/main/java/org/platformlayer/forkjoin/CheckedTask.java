//package org.platformlayer.forkjoin;
//
//import java.util.concurrent.ForkJoinTask;
//
//public abstract class CheckedTask<V> extends ForkJoinTask<V> {
//    private static final long serialVersionUID = 1L;
//
//    V result;
//
//    @Override
//    public V getRawResult() {
//        return result;
//    }
//
//    @Override
//    protected void setRawResult(V value) {
//        result = value;
//    }
//
//    protected abstract V compute() throws Exception;
//
//    @Override
//    protected boolean exec() {
//        try {
//            result = compute();
//        } catch (Exception e) {
//            completeExceptionally(e);
//        }
//        return true;
//    }
// }
