package analyse;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class FakeKataGo extends Process {
    public FakeKataGo() {
        try {
            readFromOut = new PipedInputStream();
            out = new PipedOutputStream(readFromOut);
            outReader = new BufferedReader(new InputStreamReader(readFromOut));

            writeToIn = new PipedOutputStream();
            in = new PipedInputStream(writeToIn);
            inWriter = new PrintWriter(new OutputStreamWriter(writeToIn));

            writeToErr = new PipedOutputStream();
            err = new PipedInputStream(writeToErr);
            errWriter = new PrintWriter(new OutputStreamWriter(writeToErr));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final AtomicBoolean shouldWriteMoveMetrics = new AtomicBoolean(false);
    private static final AtomicBoolean shouldWriteProtocolVersion = new AtomicBoolean(false);
    private static final AtomicBoolean exit = new AtomicBoolean(false);

    public void start() {

        ExecutorService writeErrorNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
        writeErrorNewSingleThreadExecutor.execute(() -> {
            do {
                errWriter.println("GTP ready, beginning main protocol loop");
                errWriter.flush();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (!shouldWriteMoveMetrics.get());
        });
        ExecutorService readOutNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
        readOutNewSingleThreadExecutor.execute(() -> {
            while (true) {
                try {
                    String line = "";
                    while ((line = outReader.readLine()) != null) {
                        if (line.startsWith("kata-analyze")) {
                            shouldWriteMoveMetrics.set(true);
                        }
                        if (line.startsWith("protocol_version")) {
                            shouldWriteMoveMetrics.set(false);
                            shouldWriteProtocolVersion.set(true);
                        }
                        if (line.equals("quit")) {
                            exit.set(true);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        ExecutorService writeInNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
        writeInNewSingleThreadExecutor.execute(() -> {

            while (true) {
                if (shouldWriteMoveMetrics.get()) {
                    BigDecimal winrate = BigDecimal.valueOf(Math.random());
                    BigDecimal scoreLead = BigDecimal.valueOf(20 * ((2 * Math.random()) - 1));
                    String out = "info move L15 visits 121 utility -1.07502 winrate " + winrate + " scoreMean " + scoreLead + " scoreStdev 19.5573 scoreLead " + scoreLead + " scoreSelfplay -28.0574 prior 0.209432 lcb -0.0022216 utilityLcb -1.10799 order 0 pv L15 E5 M15 C13 C17 C18 D16 info move H2 visits 153 utility -1.08721 winrate 0.0115132 scoreMean -29.8217 scoreStdev 20.0817 scoreLead -29.8217 scoreSelfplay -29.389 prior 0.373352 lcb -0.00294137 utilityLcb -1.12768 order 1 pv H2 H8 L15 L10 N12 G9 M15 M10 M11 C13 info move G2 visits 130 utility -1.08636 winrate 0.0108509 scoreMean -29.2966 scoreStdev 20.1815 scoreLead -29.2966 scoreSelfplay -29.0387 prior 0.299997 lcb -0.000797726 utilityLcb -1.11897 order 2 pv G2 H8 L15 G9 M15 L10 M11 M10 N11 N15 info move H8 visits 49 utility -1.07285 winrate 0.0129406 scoreMean -27.5248 scoreStdev 17.6893 scoreLead -27.5248 scoreSelfplay -27.4359 prior 0.0198256 lcb -0.0161538 utilityLcb -1.15431 order 3 pv H8 D5 L15 L16 M15 N15 M16 info move L10 visits 25 utility -1.10385 winrate 0.00399732 scoreMean -28.1726 scoreStdev 15.6698 scoreLead -28.1726 scoreSelfplay -28.2602 prior 0.0379175 lcb -0.00740477 utilityLcb -1.13578 order 4 pv L10 H8 G2 G9 C13 info move B4 visits 5 utility -1.16511 winrate 0.00396565 scoreMean -40.3397 scoreStdev 21.6219 scoreLead -40.3397 scoreSelfplay -39.5244 prior 0.0176333 lcb -0.0933563 utilityLcb -1.43761 order 5 pv B4 D5 L15 M15 info move L9 visits 3 utility -1.17294 winrate 0.00463912 scoreMean -37.4653 scoreStdev 19.8482 scoreLead -37.4653 scoreSelfplay -36.7008 prior 0.00232647 lcb -1.17633 utilityLcb -4.47965 order 6 pv L9 H8 Q15 info move Q15 visits 1 utility -1.15063 winrate 0.00702433 scoreMean -37.7183 scoreStdev 18.1302 scoreLead -37.7183 scoreSelfplay -36.0593 prior 0.00409325 lcb -0.992976 utilityLcb -2.8 order 7 pv Q15 info move C5 visits 1 utility -1.20293 winrate 0.00577182 scoreMean -48.6297 scoreStdev 20.2033 scoreLead -48.6297 scoreSelfplay -45.184 prior 0.00357019 lcb -0.994228 utilityLcb -2.8 order 8 pv C5";
                    inWriter.println(out);
                    inWriter.flush();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (shouldWriteProtocolVersion.getAndSet(false)) {
                    String out = "= 2";
                    inWriter.println(out);
                    inWriter.flush();
                }
            }
        });
        while (!exit.get()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        writeInNewSingleThreadExecutor.shutdown();
        readOutNewSingleThreadExecutor.shutdown();
        writeErrorNewSingleThreadExecutor.shutdown();
    }

    // receive input from client
    private final PipedInputStream readFromOut;
    private final PipedOutputStream out;
    private final BufferedReader outReader;
    // send analyse output to client
    private final PipedInputStream in;
    private final PipedOutputStream writeToIn;
    private final PrintWriter inWriter;
    // send read to use output to client
    private final PipedInputStream err;
    private final PipedOutputStream writeToErr;
    private final PrintWriter errWriter;

    @Override
    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public InputStream getErrorStream() {
        return err;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public void destroy() {
        try {
            out.close();
        } catch (IOException e) {
            log.error("out.close() error", e);
        }
        try {
            in.close();
        } catch (IOException e) {
            log.error("in.close() error", e);
        }
        try {
            err.close();
        } catch (IOException e) {
            log.error("err.close() error", e);
        }
    }
}
