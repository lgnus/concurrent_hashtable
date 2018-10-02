package cp.articlerep;

import java.util.Arrays;
import java.util.Locale;

public class Benchmark {

    //Para depois visualizar com a pitôn
    //VM Options: -Dcp.articlerep.validate=true
    //Argumentos : 30 0 5 1000 10 10 80 100 100 6
    public static void main(String[] args) {

        if (args.length < 9) {
            System.out
                    .println("usage: "
                            + MainRep.class.getCanonicalName()
                            + " time(sec) nt0 ntf nkeys put(%) del(%) get(%) nauthors nkeywords nfindlist");
            System.exit(1);
        }

        int time = Integer.parseInt(args[0]);
        int nt0 = Integer.parseInt(args[1]);
        int ntf = Integer.parseInt(args[2]);
        int nkeys = Integer.parseInt(args[3]);
        int put = Integer.parseInt(args[4]);
        int del = Integer.parseInt(args[5]);
        int get = Integer.parseInt(args[6]);

        if (put + del + get != 100) {
            System.out.println("Error: "
                    + " put(%) + del(%) + get(%) must add to 100%");
            System.exit(1);
        }
        int nauthors = Integer.parseInt(args[6]);
        int nkeywords = Integer.parseInt(args[7]);
        int nfindlist = Integer.parseInt(args[8]);

        String elapsedTime[] = new String[ntf - nt0 + 1];
        String operationRate[] = new String[ntf - nt0 + 1];

        for(int nt = nt0; nt <=ntf; nt++){

            int nthreads = (int) Math.pow(2,nt);
            System.out.format("Benchmarking %d\n", nthreads);

            Worker run = new Worker(nkeys, "resources/dictionary.txt", put, del,
                    get, nauthors, nkeywords, nfindlist);

            run.spawnThread(nthreads);

            run.startTest();
            long start_time = System.currentTimeMillis();
            try{
                for (int i = 0; i < time; i++) {
                    Thread.sleep(1000);

                    if (i % 5 == 0) {
                        run.pauseTest();

                        if (!run.getRepository().validate()) {
                            System.err.println("[VALIDATION ERROR]");
                            run.stopTest();
                            return;
                        }
                        run.restartTest();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            run.stopTest();
            long end_time = System.currentTimeMillis();

            run.joinThreads();
            elapsedTime[nt] = String.format(Locale.ROOT, "%.4f",(double) (end_time - start_time) / 1000);
            operationRate[nt] = String.format(Locale.ROOT, "%.4f",(double) Math.round(run.getTotalOperations() /
                    ((end_time - start_time) / 1000.0)));
        }
        System.out.format("Elapsed Time: [%s]\n",utils.join(Arrays.asList(elapsedTime), ","));
        System.out.format("Operat. Rate: [%s]\n",utils.join(Arrays.asList(operationRate), ","));
    }
}