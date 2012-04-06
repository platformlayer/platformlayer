//package org.platformlayer.forkjoin;
//
//import java.util.concurrent.Callable;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.Future;
//
//public class RealForkJoinStrategy extends ForkJoinStrategy {
//    final ForkJoinPool forkJoinPool;
//
//    public RealForkJoinStrategy(ForkJoinPool forkJoinPool) {
//        super();
//        this.forkJoinPool = forkJoinPool;
//    }
//
//    @Override
//    public <T> Future<T> execute(final Callable<T> callable) {
//        CheckedTask<T> task = new CheckedTask<T>() {
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            protected T compute() throws Exception {
//                return callable.call();
//            }
//        };
//        forkJoinPool.execute(task);
//        return task;
//    }
//
// }