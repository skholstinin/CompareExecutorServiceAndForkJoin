package test1;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewClass {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        List<Integer> integers = IntStream.range(1, 1_000_000).boxed().collect(toList());
        task1(integers);
        task2(integers);
    }

    public static void task1(List<Integer> integers) {

        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Integer> results = integers.stream()
                .map(i -> CompletableFuture.supplyAsync(() -> i * 2, executor))
                .collect(collectingAndThen(toList(), list -> list.stream()
                        .map(CompletableFuture::join)
                        .collect(toList())));
        executor.shutdown();
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("T1: " + endTime);

    }

    public static void task2(List<Integer> integers) {
        long startTime = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(8);

        ForkJoinTask<?> task = pool.submit(() -> integers.stream().parallel()
                .map(i -> i * 2)
                .collect(toList()));
        try {
            List<Integer> result = (List<Integer>) task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("T2: " + endTime);

    }
}
